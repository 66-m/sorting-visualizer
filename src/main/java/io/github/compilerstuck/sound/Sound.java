package io.github.compilerstuck.sound;

import io.github.compilerstuck.control.model.ArrayModel;
import processing.core.PApplet;

public abstract class Sound {
    protected PApplet proc;
    ArrayModel arrayController;
    protected boolean isMuted;

    public Sound(ArrayModel arrayController) {
        this.arrayController = arrayController;
    }

    public abstract void playSound(int value);

    public abstract void mute(boolean mute);

    /**
     * Releases native audio resources. Default is a no-op; MIDI implementations
     * should close the synthesizer.
     */
    public void dispose() {
        // no-op
    }

    public void setIsMuted(boolean muted) {
        isMuted = muted;
        mute(isMuted);
    }
}
