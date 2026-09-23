package io.github._66_m.sound;

import io.github._66_m.control.model.ArrayModel;

/** No-op sound for tests and headless runs. Delegates to {@link SilentSound}. */
public class HeadlessSound extends SilentSound {
  public HeadlessSound(ArrayModel arrayModel) {
    super(arrayModel);
  }
}
