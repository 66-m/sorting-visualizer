package io.github.compilerstuck.Sound;

import io.github.compilerstuck.Control.model.ArrayModel;

/**
 * No-op sound for tests and headless runs. Delegates to {@link SilentSound}.
 */
public class HeadlessSound extends SilentSound {
    public HeadlessSound(ArrayModel arrayController) {
        super(arrayController);
    }
}
