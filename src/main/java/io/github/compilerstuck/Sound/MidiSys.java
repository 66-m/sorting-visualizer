package io.github.compilerstuck.Sound;

import io.github.compilerstuck.Control.ArrayController;
import io.github.compilerstuck.Control.MainController;
import io.github.compilerstuck.Visual.Marker;

import javax.sound.midi.*;


public class MidiSys extends Sound {

    MidiChannel synthesizerChannel;

    public MidiSys(ArrayController arrayController) throws MidiUnavailableException {
        super(arrayController);
        proc = MainController.processing;
        Synthesizer synthesizer = MidiSystem.getSynthesizer();
        synthesizer.open();
        synthesizer.loadAllInstruments(synthesizer.getDefaultSoundbank());
        synthesizerChannel = synthesizer.getChannels()[10];

        for (Instrument i : synthesizer.getLoadedInstruments())
            if (i.getName().toLowerCase().trim().contains("square")) {
                synthesizerChannel.programChange(i.getPatch().getProgram());
                break;
            }

        //prev sound without following line
        synthesizerChannel.programChange(synthesizer.getLoadedInstruments()[4].getPatch().getProgram());


    }

    @Override
    public void playSound(int index) {

        if (index >= 0 && arrayController.getMarker(index) == Marker.SET) {

            synthesizerChannel.allSoundOff();
            synthesizerChannel.allNotesOff();

            //int barHeight = (arrayController.get(index) + 1) * (proc.height - 5) / arrayController.getLength();
            synthesizerChannel.noteOn(28 + 40 * (arrayController.get(index) + 1) / arrayController.getLength(), 90);

        }

    }

    @Override
    public void mute(boolean mute) {
        synthesizerChannel.allNotesOff();
        synthesizerChannel.setMute(mute);
    }
}
