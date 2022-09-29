package io.github.compilerstuck.Sound;

import io.github.compilerstuck.Control.ArrayController;
import processing.core.PApplet;

public abstract class Sound {
    protected PApplet proc;
    ArrayController arrayController;

    public Sound(ArrayController arrayController) {
        this.arrayController = arrayController;
    }

    public abstract void playSound(int value);

    public abstract void mute(boolean mute);

    public void notesOff(){
    }
}
