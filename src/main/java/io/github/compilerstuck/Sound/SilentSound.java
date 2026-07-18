package io.github.compilerstuck.Sound;

import io.github.compilerstuck.Control.model.ArrayModel;

/**
 * Null-object sound implementation used when MIDI is unavailable or audio
 * output is not desired. Never throws; safe to call from UI and draw loops.
 */
public class SilentSound extends Sound {
    public SilentSound(ArrayModel arrayController) {
        super(arrayController);
    }

    @Override
    public void playSound(int value) {
        // no-op
    }

    @Override
    public void mute(boolean mute) {
        // no-op
    }
}
