package io.github.compilerstuck.Sound;

import io.github.compilerstuck.Control.ArrayModel;

/**
 * No-op sound implementation used when audio output is not desired (e.g. in
 * tests or headless runs).
 */
public class HeadlessSound extends Sound {
    public HeadlessSound(ArrayModel arrayController) {
        super(arrayController);
    }

    @Override
    public void playSound(int value) {
        // nothing
    }

    @Override
    public void mute(boolean mute) {
        // ignore
    }
}
