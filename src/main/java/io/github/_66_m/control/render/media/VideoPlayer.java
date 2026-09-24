package io.github._66_m.control.render.media;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Plays a video file through {@code ffmpeg} subprocesses: one pipes raw RGBA frames, one pipes 48
 * kHz stereo PCM into a {@link SourceDataLine}. The audio line is the master clock (wall clock when
 * there is no audio); {@link #frameAt(double)} returns the newest decoded frame that is due, so
 * playback never slows down with the render loop, it only drops frames.
 *
 * <p>Threading: {@link #start}, {@link #stop} and {@link #close} may be called from any thread;
 * {@link #currentFrame()} / {@link #frameAt} must be called from a single consumer (the render
 * thread).
 */
public final class VideoPlayer implements AutoCloseable {

  private static final Logger LOGGER = Logger.getLogger(VideoPlayer.class.getName());

  public static final int MAX_DECODE_WIDTH = 1920;
  public static final int MAX_DECODE_HEIGHT = 1080;
  private static final int SAMPLE_RATE = 48_000;
  private static final int CHANNELS = 2;
  private static final int FRAME_BUFFERS = 4;
  private static final int AUDIO_CHUNK_BYTES = 4096;

  /** Stream facts from {@code ffprobe}. */
  public record Info(int width, int height, double fps, double durationSec, boolean hasAudio) {}

  private final String ffmpeg;
  private final String path;
  private final Info info;
  private final int decodeWidth;
  private final int decodeHeight;

  private final Object lock = new Object();
  private final BlockingQueue<Frame> free = new ArrayBlockingQueue<>(FRAME_BUFFERS);
  private final BlockingQueue<Frame> filled = new ArrayBlockingQueue<>(FRAME_BUFFERS);
  private Frame current;
  private long revisionCounter;

  private Process videoProcess;
  private Process audioProcess;
  private Thread videoThread;
  private Thread audioThread;
  private SourceDataLine line;
  private volatile boolean playing;
  private volatile boolean videoEnded;
  private volatile boolean audioRunning;
  private volatile String error = "";
  private volatile float volume = 1f;
  private final AtomicInteger generation = new AtomicInteger();

  // Clock: startSec + audio position while audio runs, else wall clock since anchor.
  private double startSec;
  private volatile long anchorNanos;
  private volatile double anchorSec;

  private VideoPlayer(String ffmpeg, String path, Info info) {
    this.ffmpeg = ffmpeg;
    this.path = path;
    this.info = info;
    int[] size = decodeSize(info.width(), info.height(), MAX_DECODE_WIDTH, MAX_DECODE_HEIGHT);
    this.decodeWidth = size[0];
    this.decodeHeight = size[1];
    for (int i = 0; i < FRAME_BUFFERS; i++) {
      free.add(new Frame(decodeWidth, decodeHeight));
    }
  }

  /**
   * Probes {@code path} with {@code ffprobe} (next to {@code ffmpeg}) and prepares a player. Throws
   * with a user-facing message when ffmpeg is missing or the file has no video stream.
   */
  public static VideoPlayer open(String ffmpeg, String path) throws IOException {
    String ffprobe = siblingTool(ffmpeg, "ffprobe");
    Info info = probe(ffprobe, path);
    return new VideoPlayer(ffmpeg, path, info);
  }

  public Info info() {
    return info;
  }

  public String path() {
    return path;
  }

  public int decodeWidth() {
    return decodeWidth;
  }

  public int decodeHeight() {
    return decodeHeight;
  }

  /** Last error message (empty when fine). */
  public String error() {
    return error;
  }

  public boolean isPlaying() {
    return playing;
  }

  /** {@code true} once the last frame was shown and the clock passed the end of the video. */
  public boolean isFinished() {
    return playing && videoEnded && filled.isEmpty() && clock() >= info.durationSec();
  }

  /** Soundtrack volume 0–1 (applied in software, so it works on every mixer). */
  public void setVolume(float volume) {
    this.volume = Math.max(0f, Math.min(1f, volume));
  }

  /** Starts (or restarts) playback at {@code fromSec}. */
  public void start(double fromSec) {
    synchronized (lock) {
      stopLocked();
      error = "";
      videoEnded = false;
      startSec = Math.max(0, fromSec);
      anchorSec = startSec;
      anchorNanos = System.nanoTime();
      int gen = generation.incrementAndGet();
      try {
        videoProcess = new ProcessBuilder(videoCommand(startSec)).start();
        drainStderr(videoProcess, "video");
        videoThread = daemon("video-decode", () -> decodeLoop(gen, videoProcess.getInputStream()));
        if (info.hasAudio()) {
          line = openLine();
          if (line != null) {
            audioProcess = new ProcessBuilder(audioCommand(startSec)).start();
            drainStderr(audioProcess, "audio");
            audioRunning = true;
            SourceDataLine l = line;
            InputStream in = audioProcess.getInputStream();
            audioThread = daemon("video-audio", () -> audioLoop(gen, l, in));
          }
        }
        playing = true;
        videoThread.start();
        if (audioThread != null) {
          audioThread.start();
        }
      } catch (IOException e) {
        error = "Could not start ffmpeg: " + e.getMessage();
        LOGGER.log(Level.WARNING, error, e);
        stopLocked();
      }
    }
  }

  /** Stops playback and releases processes / audio line. The last frame stays available. */
  public void stop() {
    synchronized (lock) {
      stopLocked();
    }
  }

  private void stopLocked() {
    playing = false;
    audioRunning = false;
    generation.incrementAndGet();
    destroy(videoProcess);
    destroy(audioProcess);
    videoProcess = null;
    audioProcess = null;
    join(videoThread);
    join(audioThread);
    videoThread = null;
    audioThread = null;
    if (line != null) {
      line.stop();
      line.flush();
      line.close();
      line = null;
    }
    // Recycle queued frames; keep `current` so the screen does not go black.
    Frame f;
    while ((f = filled.poll()) != null) {
      free.offer(f);
    }
  }

  @Override
  public void close() {
    stop();
  }

  /** Playback position in seconds. */
  public double clock() {
    SourceDataLine l = line;
    if (audioRunning && l != null) {
      double t = startSec + l.getLongFramePosition() / (double) SAMPLE_RATE;
      anchorSec = t;
      anchorNanos = System.nanoTime();
      return t;
    }
    if (!playing) {
      return anchorSec;
    }
    return anchorSec + (System.nanoTime() - anchorNanos) / 1e9;
  }

  /** Newest frame due at the current {@link #clock()}; see {@link #frameAt(double)}. */
  public PixelFrame currentFrame() {
    return frameAt(clock());
  }

  /**
   * Advances to the newest decoded frame whose timestamp is {@code <= t} and returns it (or the
   * previous one when none is due yet). Returns {@code null} before the first frame arrives.
   */
  public PixelFrame frameAt(double t) {
    Frame head;
    while ((head = filled.peek()) != null && head.pts <= t) {
      filled.poll();
      if (current != null) {
        free.offer(current);
      }
      current = head;
      current.revision = ++revisionCounter;
    }
    return current;
  }

  private void decodeLoop(int gen, InputStream in) {
    int frameBytes = decodeWidth * decodeHeight * 4;
    byte[] scratch = new byte[frameBytes];
    double fps = info.fps() > 0 ? info.fps() : 30.0;
    long index = 0;
    try (in) {
      while (gen == generation.get()) {
        Frame f = free.poll(200, TimeUnit.MILLISECONDS);
        if (f == null) {
          continue;
        }
        if (!readFully(in, scratch)) {
          free.offer(f);
          break;
        }
        f.data.clear();
        f.data.put(scratch).flip();
        f.pts = startSec + index / fps;
        index++;
        if (gen != generation.get()) {
          free.offer(f);
          break;
        }
        filled.put(f);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      if (gen == generation.get()) {
        LOGGER.log(Level.FINE, "Video pipe closed", e);
      }
    }
    if (gen == generation.get()) {
      videoEnded = true;
    }
  }

  private void audioLoop(int gen, SourceDataLine l, InputStream in) {
    byte[] buf = new byte[AUDIO_CHUNK_BYTES];
    try (in) {
      l.start();
      while (gen == generation.get()) {
        int n = in.readNBytes(buf, 0, buf.length);
        if (n <= 0) {
          break;
        }
        n -= n % 4;
        applyVolume(buf, n, volume);
        l.write(buf, 0, n);
      }
      if (gen == generation.get()) {
        l.drain();
      }
    } catch (IOException e) {
      if (gen == generation.get()) {
        LOGGER.log(Level.FINE, "Audio pipe closed", e);
      }
    } finally {
      if (gen == generation.get()) {
        // Hand the clock over to the wall clock at the audio's final position.
        clock();
        audioRunning = false;
      }
    }
  }

  /** Scales signed 16-bit little-endian samples in place. */
  static void applyVolume(byte[] buf, int len, float volume) {
    if (volume >= 0.999f) {
      return;
    }
    for (int i = 0; i + 1 < len; i += 2) {
      int sample = (short) ((buf[i] & 0xFF) | (buf[i + 1] << 8));
      int scaled = Math.round(sample * volume);
      buf[i] = (byte) scaled;
      buf[i + 1] = (byte) (scaled >> 8);
    }
  }

  private List<String> videoCommand(double from) {
    List<String> cmd = new ArrayList<>();
    cmd.add(ffmpeg);
    cmd.addAll(List.of("-v", "error", "-nostdin"));
    if (from > 0) {
      cmd.addAll(List.of("-ss", String.format(Locale.ROOT, "%.3f", from)));
    }
    cmd.addAll(List.of("-i", path, "-an", "-sn"));
    double fps = info.fps() > 0 ? info.fps() : 30.0;
    cmd.addAll(
        List.of(
            "-vf",
            String.format(
                Locale.ROOT,
                "fps=%.6f,scale=%d:%d:flags=bicubic",
                fps,
                decodeWidth,
                decodeHeight)));
    cmd.addAll(List.of("-f", "rawvideo", "-pix_fmt", "rgba", "-"));
    return cmd;
  }

  private List<String> audioCommand(double from) {
    List<String> cmd = new ArrayList<>();
    cmd.add(ffmpeg);
    cmd.addAll(List.of("-v", "error", "-nostdin"));
    if (from > 0) {
      cmd.addAll(List.of("-ss", String.format(Locale.ROOT, "%.3f", from)));
    }
    cmd.addAll(List.of("-i", path, "-vn", "-sn"));
    cmd.addAll(
        List.of(
            "-f",
            "s16le",
            "-ac",
            String.valueOf(CHANNELS),
            "-ar",
            String.valueOf(SAMPLE_RATE),
            "-"));
    return cmd;
  }

  private static SourceDataLine openLine() {
    AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, CHANNELS, true, false);
    try {
      SourceDataLine l = AudioSystem.getSourceDataLine(format);
      // ~100 ms buffer: small enough for tight A/V sync, large enough not to underrun.
      l.open(format, SAMPLE_RATE / 10 * CHANNELS * 2);
      return l;
    } catch (LineUnavailableException | IllegalArgumentException | SecurityException e) {
      LOGGER.log(Level.WARNING, "No audio line for video soundtrack; playing silently", e);
      return null;
    }
  }

  /** Probes width, height, frame rate, duration and whether an audio stream exists. */
  static Info probe(String ffprobe, String path) throws IOException {
    String video =
        run(
            List.of(
                ffprobe,
                "-v",
                "error",
                "-select_streams",
                "v:0",
                "-show_entries",
                "stream=width,height,avg_frame_rate,r_frame_rate:format=duration",
                "-of",
                "default=noprint_wrappers=1",
                path));
    String audio =
        run(
            List.of(
                ffprobe,
                "-v",
                "error",
                "-select_streams",
                "a",
                "-show_entries",
                "stream=index",
                "-of",
                "csv=p=0",
                path));
    return parseProbe(video, !audio.isBlank());
  }

  static Info parseProbe(String out, boolean hasAudio) throws IOException {
    int w = 0;
    int h = 0;
    double avgFps = 0;
    double rFps = 0;
    double duration = 0;
    for (String raw : out.lines().toList()) {
      String lineText = raw.trim();
      int eq = lineText.indexOf('=');
      if (eq < 0) {
        continue;
      }
      String key = lineText.substring(0, eq);
      String value = lineText.substring(eq + 1);
      switch (key) {
        case "width" -> w = parseInt(value);
        case "height" -> h = parseInt(value);
        case "avg_frame_rate" -> avgFps = parseRate(value);
        case "r_frame_rate" -> rFps = parseRate(value);
        case "duration" -> duration = Math.max(duration, parseDouble(value));
        default -> {}
      }
    }
    if (w <= 0 || h <= 0) {
      throw new IOException("No video stream found");
    }
    double fps = avgFps > 0 && avgFps < 240 ? avgFps : rFps;
    if (!(fps > 0) || fps > 240) {
      fps = 30.0;
    }
    return new Info(w, h, fps, duration, hasAudio);
  }

  /** Fits {@code w×h} inside {@code maxW×maxH} (never upscales), with even dimensions. */
  static int[] decodeSize(int w, int h, int maxW, int maxH) {
    double scale = Math.min(1.0, Math.min(maxW / (double) w, maxH / (double) h));
    int dw = Math.max(2, (int) Math.round(w * scale) & ~1);
    int dh = Math.max(2, (int) Math.round(h * scale) & ~1);
    return new int[] {dw, dh};
  }

  static double parseRate(String value) {
    String v = value.trim();
    int slash = v.indexOf('/');
    if (slash < 0) {
      return parseDouble(v);
    }
    double num = parseDouble(v.substring(0, slash));
    double den = parseDouble(v.substring(slash + 1));
    return den > 0 ? num / den : 0;
  }

  private static int parseInt(String v) {
    try {
      return Integer.parseInt(v.trim());
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private static double parseDouble(String v) {
    try {
      return Double.parseDouble(v.trim());
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  /** {@code ffprobe} in the same directory as an explicit ffmpeg path, else from PATH. */
  static String siblingTool(String ffmpeg, String tool) {
    int slash = Math.max(ffmpeg.lastIndexOf('/'), ffmpeg.lastIndexOf('\\'));
    if (slash < 0) {
      return tool;
    }
    String name = ffmpeg.substring(slash + 1);
    String ext = name.toLowerCase(Locale.ROOT).endsWith(".exe") ? ".exe" : "";
    return ffmpeg.substring(0, slash + 1) + tool + ext;
  }

  private static String run(List<String> cmd) throws IOException {
    Process p;
    try {
      p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
    } catch (IOException e) {
      throw new IOException(
          "ffmpeg/ffprobe not found. Install ffmpeg and make sure it is on the PATH.", e);
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (InputStream in = p.getInputStream()) {
      in.transferTo(out);
    }
    try {
      if (!p.waitFor(20, TimeUnit.SECONDS)) {
        p.destroyForcibly();
        throw new IOException("ffprobe timed out");
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while probing video", e);
    }
    String text = out.toString(StandardCharsets.UTF_8);
    if (p.exitValue() != 0) {
      String first = text.lines().findFirst().orElse("exit " + p.exitValue());
      throw new IOException("Cannot read video: " + first);
    }
    return text;
  }

  private static boolean readFully(InputStream in, byte[] buf) throws IOException {
    int off = 0;
    while (off < buf.length) {
      int n = in.read(buf, off, buf.length - off);
      if (n < 0) {
        return false;
      }
      off += n;
    }
    return true;
  }

  private static void drainStderr(Process p, String label) {
    daemon(
            "ffmpeg-" + label + "-stderr",
            () -> {
              try (InputStream err = p.getErrorStream()) {
                String text = new String(err.readAllBytes(), StandardCharsets.UTF_8).trim();
                if (!text.isEmpty()) {
                  LOGGER.log(Level.FINE, "ffmpeg {0}: {1}", new Object[] {label, text});
                }
              } catch (IOException ignored) {
                // Process ended.
              }
            })
        .start();
  }

  private static Thread daemon(String name, Runnable body) {
    Thread t = new Thread(body, name);
    t.setDaemon(true);
    return t;
  }

  private static void destroy(Process p) {
    if (p != null) {
      p.destroy();
    }
  }

  private static void join(Thread t) {
    if (t == null || t == Thread.currentThread()) {
      return;
    }
    try {
      t.join(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private static final class Frame implements PixelFrame {
    final int width;
    final int height;
    final ByteBuffer data;
    double pts;
    long revision;

    Frame(int width, int height) {
      this.width = width;
      this.height = height;
      this.data = ByteBuffer.allocateDirect(width * height * 4);
    }

    @Override
    public int width() {
      return width;
    }

    @Override
    public int height() {
      return height;
    }

    @Override
    public ByteBuffer rgba() {
      return data;
    }

    @Override
    public long revision() {
      return revision;
    }
  }
}
