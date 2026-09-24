package io.github._66_m.visual;

import io.github._66_m.control.config.MediaKind;
import io.github._66_m.control.config.visual.VideoSettings;
import io.github._66_m.control.config.visual.VisualizationSettings;
import io.github._66_m.control.model.ArrayModel;
import io.github._66_m.control.render.RenderSystem;
import io.github._66_m.control.render.media.GridShape;
import io.github._66_m.control.render.media.MediaRemap;
import io.github._66_m.control.render.media.PixelFrame;
import io.github._66_m.control.render.media.RemapLayout;
import io.github._66_m.control.render.media.VideoPlayer;
import io.github._66_m.sound.Sound;
import io.github._66_m.visual.gradient.ColorGradient;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Plays a video (with its soundtrack) cut into columns, rows or grid tiles; element {@code i} shows
 * the piece of its value. Sorted = the video plays normally. Decoding runs in {@code ffmpeg}
 * ({@link VideoPlayer}); the video clock never depends on the sort.
 */
public class Video extends Visualization
    implements ConfigurableVisualization, MediaSourceVisualization {

  private static final Logger LOGGER = Logger.getLogger(Video.class.getName());

  /** Overrides the ffmpeg executable (else {@code ffmpeg} from the PATH). */
  static final String FFMPEG_PROPERTY = "sortvis.ffmpeg";

  private volatile VideoSettings settings = VideoSettings.defaults();

  private final Object playerLock = new Object();
  private VideoPlayer player;
  private volatile String mediaPath = "";
  private volatile String status = "";
  private volatile boolean loading;
  private int loadGeneration;

  private boolean sessionRunning;
  private boolean priming;

  /** Guards against respawning ffmpeg in a tight loop (at most one automatic start per second). */
  private long lastAutoStartNanos;

  private boolean autoStarted;

  private static final long MIN_AUTO_START_INTERVAL_NANOS = 1_000_000_000L;

  private final MediaRemap remap = new MediaRemap();
  private int[] indices;
  private boolean[] highlight;
  private long cachedRevision = Long.MIN_VALUE;
  private int cachedLength = -1;

  public Video(ArrayModel arrayModel, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayModel, colorGradient, sound, rs);
    name = "Video";
  }

  @Override
  public VisualizationSettings currentSettings() {
    return settings;
  }

  @Override
  public void applySettings(VisualizationSettings next) {
    if (next instanceof VideoSettings s) {
      VideoSettings previous = settings;
      settings = s;
      VideoPlayer p = currentPlayer();
      if (p != null) {
        p.setVolume((float) s.soundtrackVolume());
        if (previous.playback() != s.playback() && s.playback() == VideoSettings.Playback.LOOP) {
          priming = false;
        }
      }
    }
  }

  @Override
  public MediaKind mediaKind() {
    return MediaKind.VIDEO;
  }

  @Override
  public String mediaPath() {
    return mediaPath;
  }

  @Override
  public boolean loadMedia(String path) {
    if (path == null || path.isBlank()) {
      return false;
    }
    int gen;
    synchronized (playerLock) {
      gen = ++loadGeneration;
      if (player != null) {
        player.close();
        player = null;
      }
    }
    mediaPath = path;
    loading = true;
    status = "Loading video…";
    Thread t =
        new Thread(
            () -> {
              try {
                VideoPlayer opened = VideoPlayer.open(ffmpegExecutable(), path);
                synchronized (playerLock) {
                  if (gen != loadGeneration) {
                    opened.close();
                    return;
                  }
                  player = opened;
                  opened.setVolume((float) settings.soundtrackVolume());
                  priming = settings.playback() == VideoSettings.Playback.SYNC_WITH_RUN;
                }
                status = "";
              } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Cannot open video " + path, e);
                status = e.getMessage();
              } finally {
                loading = false;
              }
            },
            "video-open");
    t.setDaemon(true);
    t.start();
    return true;
  }

  static String ffmpegExecutable() {
    String configured = System.getProperty(FFMPEG_PROPERTY);
    if (configured == null || configured.isBlank()) {
      configured = System.getenv("SORTVIS_FFMPEG");
    }
    return configured == null || configured.isBlank() ? "ffmpeg" : configured.trim();
  }

  private VideoPlayer currentPlayer() {
    synchronized (playerLock) {
      return player;
    }
  }

  @Override
  public void onSessionState(boolean running) {
    boolean started = running && !sessionRunning;
    boolean ended = !running && sessionRunning;
    sessionRunning = running;
    VideoPlayer p = currentPlayer();
    if (p == null || settings.playback() != VideoSettings.Playback.SYNC_WITH_RUN) {
      return;
    }
    if (started) {
      priming = false;
      p.setVolume((float) settings.soundtrackVolume());
      p.start(0);
    } else if (ended) {
      p.stop();
    }
  }

  @Override
  public void deactivate() {
    VideoPlayer p = currentPlayer();
    if (p != null) {
      p.stop();
      priming = settings.playback() == VideoSettings.Playback.SYNC_WITH_RUN;
    }
    sound.setVolumeScale(1f);
  }

  @Override
  public void update(float delta) {
    VideoSettings s = settings;
    sound.setVolumeScale((float) s.sortToneVolume());
    int length = arrayModel.getLength();
    if (length <= 0) {
      return;
    }
    rebuildIndices(length);

    VideoPlayer p = currentPlayer();
    if (p == null) {
      drawStatus(
          loading
              ? status
              : !status.isBlank() ? status : "Pick a video file in Settings (needs ffmpeg)");
      return;
    }
    managePlayback(p, s);

    PixelFrame frame = p.currentFrame();
    if (priming && frame != null && s.playback() == VideoSettings.Playback.SYNC_WITH_RUN) {
      // Idle preview: keep the first frame on screen until the next Run.
      p.stop();
      priming = false;
    }
    if (frame == null) {
      drawStatus(!p.error().isBlank() ? p.error() : "Loading video…");
      return;
    }

    remap.frame = frame;
    remap.indices = indices;
    remap.highlight = highlight;
    remap.length = length;
    remap.indexRevision = cachedRevision;
    remap.highlightStrength = (float) s.highlightStrength();
    remap.contain = s.fitMode() == VideoSettings.FitMode.CONTAIN;
    remap.gridLines = s.gridLines();
    remap.layout = layoutOf(s.layout());
    if (remap.layout == RemapLayout.GRID) {
      GridShape grid = GridShape.forCount(length, frame.width() / (double) frame.height());
      remap.cols = grid.cols();
      remap.rows = grid.rows();
    } else {
      remap.cols = 1;
      remap.rows = 1;
    }
    rs.drawMediaRemap(remap);
  }

  private void managePlayback(VideoPlayer p, VideoSettings s) {
    if (!p.error().isBlank()) {
      return; // Do not respawn ffmpeg every frame after a failure.
    }
    long now = System.nanoTime();
    boolean mayStart = !autoStarted || now - lastAutoStartNanos >= MIN_AUTO_START_INTERVAL_NANOS;
    if (s.playback() == VideoSettings.Playback.LOOP) {
      if ((!p.isPlaying() || p.isFinished()) && mayStart) {
        lastAutoStartNanos = now;
        autoStarted = true;
        p.start(0);
      }
      return;
    }
    if (priming && !p.isPlaying() && mayStart) {
      // First frame for the idle preview; stopped again as soon as it arrives.
      lastAutoStartNanos = now;
      autoStarted = true;
      p.setVolume(0f);
      p.start(0);
    } else if (sessionRunning && p.isFinished()) {
      p.stop(); // Hold the last frame until the session ends.
    }
  }

  private void rebuildIndices(int length) {
    long rev = arrayModel.getVisualRevision();
    if (rev == cachedRevision && length == cachedLength && indices != null) {
      return;
    }
    if (indices == null || indices.length < length) {
      indices = new int[length];
      highlight = new boolean[length];
    }
    for (int i = 0; i < length; i++) {
      indices[i] = arrayModel.get(i);
      boolean hl = arrayModel.getMarker(i) == Marker.SET;
      highlight[i] = hl;
      if (hl) {
        sound.playSound(i);
      }
    }
    cachedRevision = rev;
    cachedLength = length;
  }

  static RemapLayout layoutOf(VideoSettings.Layout layout) {
    return switch (layout) {
      case COLUMNS -> RemapLayout.COLUMNS;
      case ROWS -> RemapLayout.ROWS;
      case GRID -> RemapLayout.GRID;
    };
  }

  private void drawStatus(String text) {
    if (text == null || text.isBlank()) {
      return;
    }
    float size = Math.max(16f, screenHeight / 30f);
    float w = rs.measureTextWidth(text, size);
    rs.drawText(text, (screenWidth - w) / 2f, screenHeight / 2f - size / 2f, size);
  }
}
