package io.github.compilerstuck.sound;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.visual.Marker;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import javax.sound.sampled.SourceDataLine;

public class MidiSys extends Sound {

  private static final Logger LOGGER = Logger.getLogger(MidiSys.class.getName());

  /** SoftSynthesizer default is 200ms; that makes monophonic retriggers smear and click. */
  private static final long TARGET_LATENCY_MICROS = 20_000L;

  private final Synthesizer synthesizer;
  private final MidiChannel synthesizerChannel;

  public MidiSys(ArrayModel arrayController) throws MidiUnavailableException {
    super(arrayController);
    synthesizer = MidiSystem.getSynthesizer();
    openSynthesizer(synthesizer);
    synthesizer.loadAllInstruments(synthesizer.getDefaultSoundbank());
    synthesizerChannel = synthesizer.getChannels()[10];

    for (Instrument i : synthesizer.getLoadedInstruments())
      if (i.getName().toLowerCase(Locale.ROOT).trim().contains("square")) {
        synthesizerChannel.programChange(i.getPatch().getProgram());
        break;
      }

    // prev sound without following line
    synthesizerChannel.programChange(synthesizer.getLoadedInstruments()[4].getPatch().getProgram());
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

    if (!isMuted && index >= 0 && arrayController.getMarker(index) == Marker.SET) {
      // Hard cut (same as main): Electric Piano release tails from noteOff change the timbre.
      synthesizerChannel.allSoundOff();
      synthesizerChannel.allNotesOff();

      synthesizerChannel.noteOn(
          28 + 40 * (arrayController.get(index) + 1) / arrayController.getLength(), 90);
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
  public void dispose() {
    synthesizerChannel.allSoundOff();
    synthesizerChannel.allNotesOff();
    if (synthesizer.isOpen()) {
      synthesizer.close();
    }
  }
}
