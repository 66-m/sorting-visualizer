package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.config.visual.ImageVerticalSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;

/** Overlay image visualization remapped by vertical strips. */
public class ImageVertical extends AbstractImageVisualization implements ConfigurableVisualization {

  private volatile ImageVerticalSettings settings = ImageVerticalSettings.defaults();

  public ImageVertical(
      ArrayModel arrayModel, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayModel, colorGradient, sound, rs);
    name = "Image - Vertical Sorting";
  }

  @Override
  public VisualizationSettings currentSettings() {
    return settings;
  }

  @Override
  public void applySettings(VisualizationSettings next) {
    if (!(next instanceof ImageVerticalSettings s)) {
      return;
    }
    settings = s;
    applyImageLook(
        s.fitMode() == ImageVerticalSettings.FitMode.CONTAIN, (float) s.highlightStrength());
  }

  @Override
  protected boolean horizontalMode() {
    return false;
  }
}
