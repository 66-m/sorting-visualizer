package io.github.compilerstuck.sound;

import io.github.compilerstuck.control.config.audio.AudioSettings;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.visual.Marker;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import javax.sound.sampled.SourceDataLine;

public class MidiSys extends Sound {

  private static final Logger LOGGER = Logger.getLogger(MidiSys.class.getName());

  /** SoftSynthesizer default is 200ms; that makes monophonic retriggers smear and click. */
  private static final long TARGET_LATENCY_MICROS = 20_000L;

  /** Test tone hold duration before cutting and restoring committed settings. */
  private static final long TEST_TONE_HOLD_MS = 400L;

  /** Simulated shuffle preview duration; retrigger cadence matches the real per-frame rate. */
  private static final long SHUFFLE_PREVIEW_DURATION_MS = 500L;

  private static final long SHUFFLE_PREVIEW_INTERVAL_MS = 16L; // ~60Hz, matches TARGET_FRAME_RATE
  private static final int SHUFFLE_PREVIEW_SIMULATED_LENGTH = 5000;

  private final Synthesizer synthesizer;
  private final MidiChannel synthesizerChannel;
  private final ScheduledExecutorService previewExecutor =
      Executors.newSingleThreadScheduledExecutor(
          r -> {
            Thread t = new Thread(r, "midi-preview");
            t.setDaemon(true);
            return t;
          });
  private final Random random = new Random();
  private volatile ScheduledFuture<?> activePreview;

  public MidiSys(ArrayModel arrayModel) throws MidiUnavailableException {
    super(arrayModel);
    synthesizer = MidiSystem.getSynthesizer();
    openSynthesizer(synthesizer);
    synthesizer.loadAllInstruments(synthesizer.getDefaultSoundbank());
    synthesizerChannel = synthesizer.getChannels()[10];
    applySettings(AudioSettings.defaults());
  }

  /**
   * Prefer a low SoftSynthesizer buffer when the JVM opens {@code com.sun.media.sound} (manifest
   * {@code Add-Opens} or {@code --add-opens}). Falls back to the default {@link
   * Synthesizer#open()}.
   */
  private static void openSynthesizer(Synthesizer synthesizer) throws MidiUnavailableException {
    try {
      Method open = synthesizer.getClass().getMethod("open", SourceDataLine.class, Map.class);
      open.setAccessible(true);
      Map<String, Object> info = new HashMap<>();
      info.put("latency", TARGET_LATENCY_MICROS);
      open.invoke(synthesizer, null, info);
      LOGGER.log(Level.FINE, "Opened SoftSynthesizer with latency={0}µs", synthesizer.getLatency());
      return;
    } catch (ReflectiveOperationException | RuntimeException e) {
      LOGGER.log(
          Level.FINE,
          "Low-latency SoftSynthesizer open unavailable; using default synthesizer.open()",
          e);
    }
    synthesizer.open();
  }

  @Override
  public void playSound(int index) {
    if (!isMuted && index >= 0 && arrayModel.getMarker(index) == Marker.SET) {
      // Hard cut (same as main): Electric Piano release tails from noteOff change the timbre.
      synthesizerChannel.allSoundOff();
      synthesizerChannel.allNotesOff();

      int note = settings.noteFor(arrayModel.get(index), arrayModel.getLength());
      synthesizerChannel.noteOn(note, settings.velocity());
    }
  }

  @Override
  public void mute(boolean mute) {
    // Hard cut: allNotesOff alone leaves SoftSynth release tails sounding after sort end.
    synthesizerChannel.allSoundOff();
    synthesizerChannel.allNotesOff();
    synthesizerChannel.setMute(mute);
  }

  @Override
  public void cutNotes() {
    synthesizerChannel.allSoundOff();
    synthesizerChannel.allNotesOff();
  }

  @Override
  public void applySettings(AudioSettings newSettings) {
    super.applySettings(newSettings);
    pushToChannel(this.settings);
  }

  @Override
  public void previewTestTone(AudioSettings draft) {
    AudioSettings effective = draft != null ? draft : AudioSettings.defaults();
    cancelActivePreview();
    activePreview =
        previewExecutor.schedule(
            () -> {
              pushToChannel(effective);
              int midNote = (effective.lowNote() + effective.highNote()) / 2;
              synthesizerChannel.allSoundOff();
              synthesizerChannel.allNotesOff();
              synthesizerChannel.noteOn(midNote, effective.velocity());
              sleepQuietly(TEST_TONE_HOLD_MS);
              synthesizerChannel.allSoundOff();
              synthesizerChannel.allNotesOff();
              pushToChannel(this.settings);
            },
            0,
            TimeUnit.MILLISECONDS);
  }

  @Override
  public void previewShuffle(
      AudioSettings draft, int simulatedLength, long durationMs, Runnable onFinished) {
    AudioSettings effective = draft != null ? draft : AudioSettings.defaults();
    int length = simulatedLength > 0 ? simulatedLength : SHUFFLE_PREVIEW_SIMULATED_LENGTH;
    long duration = durationMs > 0 ? durationMs : SHUFFLE_PREVIEW_DURATION_MS;
    cancelActivePreview();
    activePreview =
        previewExecutor.schedule(
            () -> runShuffleBurst(effective, length, duration, onFinished),
            0,
            TimeUnit.MILLISECONDS);
  }

  @Override
  public void previewPitchSweep(
      AudioSettings draft, int simulatedLength, long durationMs, Runnable onFinished) {
    AudioSettings effective = draft != null ? draft : AudioSettings.defaults();
    int length = simulatedLength > 0 ? simulatedLength : SHUFFLE_PREVIEW_SIMULATED_LENGTH;
    long duration = durationMs > 0 ? durationMs : SHUFFLE_PREVIEW_DURATION_MS;
    cancelActivePreview();
    activePreview =
        previewExecutor.schedule(
            () -> runPitchSweepBurst(effective, length, duration, onFinished),
            0,
            TimeUnit.MILLISECONDS);
  }

  private void runShuffleBurst(
      AudioSettings effective, int length, long durationMs, Runnable onFinished) {
    pushToChannel(effective);
    long deadline = System.currentTimeMillis() + durationMs;
    while (System.currentTimeMillis() < deadline) {
      synthesizerChannel.allSoundOff();
      synthesizerChannel.allNotesOff();
      int value = random.nextInt(length);
      int note = effective.noteFor(value, length);
      synthesizerChannel.noteOn(note, effective.velocity());
      sleepQuietly(SHUFFLE_PREVIEW_INTERVAL_MS);
    }
    synthesizerChannel.allSoundOff();
    synthesizerChannel.allNotesOff();
    pushToChannel(this.settings);
    if (onFinished != null) {
      onFinished.run();
    }
  }

  private void runPitchSweepBurst(
      AudioSettings effective, int length, long durationMs, Runnable onFinished) {
    pushToChannel(effective);
    long start = System.currentTimeMillis();
    long deadline = start + durationMs;
    while (System.currentTimeMillis() < deadline) {
      long elapsed = System.currentTimeMillis() - start;
      int value =
          length <= 1 ? 0 : (int) Math.min(length - 1, (elapsed * (length - 1L)) / durationMs);
      synthesizerChannel.allSoundOff();
      synthesizerChannel.allNotesOff();
      int note = effective.noteFor(value, length);
      synthesizerChannel.noteOn(note, effective.velocity());
      sleepQuietly(SHUFFLE_PREVIEW_INTERVAL_MS);
    }
    synthesizerChannel.allSoundOff();
    synthesizerChannel.allNotesOff();
    pushToChannel(this.settings);
    if (onFinished != null) {
      onFinished.run();
    }
  }

  @Override
  public void stopPreview() {
    cancelActivePreview();
    synthesizerChannel.allSoundOff();
    synthesizerChannel.allNotesOff();
    pushToChannel(this.settings);
  }

  private void cancelActivePreview() {
    ScheduledFuture<?> previous = activePreview;
    if (previous != null) {
      previous.cancel(false);
    }
  }

  private void pushToChannel(AudioSettings s) {
    synthesizerChannel.programChange(s.instrumentProgram());
    synthesizerChannel.controlChange(7, s.volume()); // channel volume
    synthesizerChannel.controlChange(10, s.pan()); // pan
    synthesizerChannel.controlChange(91, s.reverb()); // reverb send
    synthesizerChannel.controlChange(93, s.chorus()); // chorus send
    synthesizerChannel.controlChange(73, s.attackTime()); // GM2 attack time
    synthesizerChannel.controlChange(72, s.releaseTime()); // GM2 release time
    synthesizerChannel.controlChange(74, s.brightness()); // GM2 brightness/cutoff
  }

  private static void sleepQuietly(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public void dispose() {
    cancelActivePreview();
    previewExecutor.shutdownNow();
    synthesizerChannel.allSoundOff();
    synthesizerChannel.allNotesOff();
    if (synthesizer.isOpen()) {
      synthesizer.close();
    }
  }
}
