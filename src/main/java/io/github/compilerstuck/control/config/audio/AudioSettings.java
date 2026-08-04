package io.github.compilerstuck.control.config.audio;

/**
 * Customizable MIDI audio settings: instrument/voice, levels, pitch mapping, and advanced GM2 sound
 * controllers. All fields are MIDI-range (0-127); the compact constructor clamps every field so a
 * decoded/imported instance is always valid to feed straight into {@code MidiChannel}.
 *
 * @param instrumentProgram General MIDI program number (0-127)
 * @param volume channel volume, CC7 (0-127)
 * @param velocity note-on strength (1-127)
 * @param pan CC10 (0-127, 64 = center)
 * @param lowNote MIDI note for the smallest array value (0-127)
 * @param highNote MIDI note for the largest array value (0-127, clamped to be &ge; lowNote)
 * @param reverb reverb send, CC91 (0-127)
 * @param chorus chorus send, CC93 (0-127)
 * @param attackTime GM2 attack time, CC73 (0-127, 64 = no change)
 * @param releaseTime GM2 release time, CC72 (0-127, 64 = no change)
 * @param brightness GM2 brightness/filter cutoff, CC74 (0-127, 64 = no change)
 */
public record AudioSettings(
    int instrumentProgram,
    int volume,
    int velocity,
    int pan,
    int lowNote,
    int highNote,
    int reverb,
    int chorus,
    int attackTime,
    int releaseTime,
    int brightness) {

  public static final int MIDI_MIN = 0;
  public static final int MIDI_MAX = 127;
  public static final int VELOCITY_MIN = 1;

  public static final int DEFAULT_INSTRUMENT_PROGRAM = 4; // "Electric Piano 1" - current sound
  public static final int DEFAULT_VOLUME = 100;
  public static final int DEFAULT_VELOCITY = 90;
  public static final int DEFAULT_PAN = 64;
  public static final int DEFAULT_LOW_NOTE = 28;
  public static final int DEFAULT_HIGH_NOTE = 68;
  public static final int DEFAULT_REVERB = 0;
  public static final int DEFAULT_CHORUS = 0;
  public static final int DEFAULT_ATTACK_TIME = 64;
  public static final int DEFAULT_RELEASE_TIME = 64;
  public static final int DEFAULT_BRIGHTNESS = 64;

  public AudioSettings {
    instrumentProgram = clampMidi(instrumentProgram);
    volume = clampMidi(volume);
    velocity = clampVelocity(velocity);
    pan = clampMidi(pan);
    lowNote = clampMidi(lowNote);
    highNote = Math.max(lowNote, clampMidi(highNote));
    reverb = clampMidi(reverb);
    chorus = clampMidi(chorus);
    attackTime = clampMidi(attackTime);
    releaseTime = clampMidi(releaseTime);
    brightness = clampMidi(brightness);
  }

  public static AudioSettings defaults() {
    return new AudioSettings(
        DEFAULT_INSTRUMENT_PROGRAM,
        DEFAULT_VOLUME,
        DEFAULT_VELOCITY,
        DEFAULT_PAN,
        DEFAULT_LOW_NOTE,
        DEFAULT_HIGH_NOTE,
        DEFAULT_REVERB,
        DEFAULT_CHORUS,
        DEFAULT_ATTACK_TIME,
        DEFAULT_RELEASE_TIME,
        DEFAULT_BRIGHTNESS);
  }

  /** Maps an array value (0-based, exclusive upper bound {@code length}) to a MIDI note. */
  public int noteFor(int value, int length) {
    if (length <= 0) {
      return lowNote;
    }
    int span = highNote - lowNote;
    int note = lowNote + span * (value + 1) / length;
    return clampMidi(note);
  }

  public static int clampMidi(int value) {
    return Math.max(MIDI_MIN, Math.min(MIDI_MAX, value));
  }

  public static int clampVelocity(int value) {
    return Math.max(VELOCITY_MIN, Math.min(MIDI_MAX, value));
  }
}
