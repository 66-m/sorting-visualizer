package io.github.compilerstuck.Sound;

import io.github.compilerstuck.Control.ArrayModel;
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

    public void setIsMuted(boolean muted) {
        isMuted = muted;
        mute(isMuted);
    }
}
