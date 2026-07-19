package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;

/** Overlay image visualization remapped by horizontal strips. */
public class ImageHorizontal extends AbstractImageVisualization {

  public ImageHorizontal(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayController, colorGradient, sound, rs);
    name = "Image - Horizontal Sorting";
  }

  @Override
  protected boolean horizontalMode() {
    return true;
  }
}
