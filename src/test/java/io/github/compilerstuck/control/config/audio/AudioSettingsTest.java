package io.github.compilerstuck.control.config.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AudioSettingsTest {

  @Test
  void defaultsMatchCurrentHardcodedBehavior() {
    AudioSettings d = AudioSettings.defaults();
    assertEquals(4, d.instrumentProgram());
    assertEquals(100, d.volume());
    assertEquals(90, d.velocity());
    assertEquals(64, d.pan());
    assertEquals(28, d.lowNote());
    assertEquals(68, d.highNote());
    assertEquals(31, d.reverb());
    assertEquals(0, d.chorus());
    assertEquals(64, d.attackTime());
    assertEquals(64, d.releaseTime());
    assertEquals(64, d.brightness());
  }

  @Test
  void clampsOutOfRangeFieldsToMidiRange() {
    AudioSettings s = new AudioSettings(-5, 500, 0, -1, -10, 200, 999, -3, 400, -1, 128);
    assertEquals(0, s.instrumentProgram());
    assertEquals(127, s.volume());
    assertEquals(1, s.velocity());
    assertEquals(0, s.pan());
    assertEquals(0, s.lowNote());
    assertEquals(127, s.highNote());
    assertEquals(127, s.reverb());
    assertEquals(0, s.chorus());
    assertEquals(127, s.attackTime());
    assertEquals(0, s.releaseTime());
    assertEquals(127, s.brightness());
  }

  @Test
  void highNoteNeverBelowLowNote() {
    AudioSettings s = new AudioSettings(4, 100, 90, 64, 80, 20, 0, 0, 64, 64, 64);
    assertEquals(80, s.lowNote());
    assertEquals(80, s.highNote());
  }

  @Test
  void noteForMapsValueRangeToPitchRange() {
    AudioSettings s = AudioSettings.defaults(); // low=28, high=68
    assertEquals(28 + 40 / 100, s.noteFor(0, 100));
    assertEquals(68, s.noteFor(99, 100));
  }

  @Test
  void noteForHandlesZeroLengthWithoutThrowing() {
    AudioSettings s = AudioSettings.defaults();
    assertEquals(s.lowNote(), s.noteFor(0, 0));
  }
}
