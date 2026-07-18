package io.github.compilerstuck.Sound;

import io.github.compilerstuck.Control.model.ArrayController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class SilentSoundTest {

    @Test
    @DisplayName("SilentSound playSound and mute are no-ops")
    void silentSoundIsNoOp() {
        SilentSound sound = new SilentSound(new ArrayController(8));
        assertDoesNotThrow(() -> sound.playSound(3));
        assertDoesNotThrow(() -> sound.mute(true));
        assertDoesNotThrow(() -> sound.mute(false));
        assertDoesNotThrow(() -> sound.setIsMuted(true));
    }

    @Test
    @DisplayName("HeadlessSound is a SilentSound")
    void headlessExtendsSilent() {
        HeadlessSound sound = new HeadlessSound(new ArrayController(4));
        assertInstanceOf(SilentSound.class, sound);
        assertDoesNotThrow(() -> sound.playSound(0));
    }
}
