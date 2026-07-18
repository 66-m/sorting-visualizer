package io.github.compilerstuck.sound;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.visual.Marker;
import javax.sound.midi.*;

public class MidiSys extends Sound {

  private final Synthesizer synthesizer;
  private final MidiChannel synthesizerChannel;

  public MidiSys(ArrayModel arrayController) throws MidiUnavailableException {
    super(arrayController);
    synthesizer = MidiSystem.getSynthesizer();
    synthesizer.open();
    synthesizer.loadAllInstruments(synthesizer.getDefaultSoundbank());
    synthesizerChannel = synthesizer.getChannels()[10];

    for (Instrument i : synthesizer.getLoadedInstruments())
      if (i.getName().toLowerCase().trim().contains("square")) {
        synthesizerChannel.programChange(i.getPatch().getProgram());
        break;
      }

    // prev sound without following line
    synthesizerChannel.programChange(synthesizer.getLoadedInstruments()[4].getPatch().getProgram());
  }

  @Override
  public void playSound(int index) {

    if (!isMuted && index >= 0 && arrayController.getMarker(index) == Marker.SET) {
      synthesizerChannel.allSoundOff();
      synthesizerChannel.allNotesOff();

      synthesizerChannel.noteOn(
          28 + 40 * (arrayController.get(index) + 1) / arrayController.getLength(), 90);
    }
  }

  @Override
  public void mute(boolean mute) {
    synthesizerChannel.allNotesOff();
    synthesizerChannel.setMute(mute);
  }

  @Override
  public void dispose() {
    synthesizerChannel.allNotesOff();
    if (synthesizer.isOpen()) {
      synthesizer.close();
    }
  }
}
