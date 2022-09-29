package io.github.compilerstuck.Sound;

import io.github.compilerstuck.Control.ArrayController;
import processing.core.PApplet;

public abstract class Sound {
    protected PApplet proc;
    ArrayController arrayController;
    protected boolean isMuted;

    public Sound(ArrayController arrayController) {
        this.arrayController = arrayController;
    }

    public abstract void playSound(int value);

    public abstract void mute(boolean mute);

    public void setIsMuted(boolean muted) {
        isMuted = muted;
        mute(isMuted);
    }
}
