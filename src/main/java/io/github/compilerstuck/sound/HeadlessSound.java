package io.github.compilerstuck.sound;

import io.github.compilerstuck.control.model.ArrayModel;

/** No-op sound for tests and headless runs. Delegates to {@link SilentSound}. */
public class HeadlessSound extends SilentSound {
  public HeadlessSound(ArrayModel arrayController) {
    super(arrayController);
  }
}
