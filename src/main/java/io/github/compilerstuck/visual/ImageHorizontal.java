package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.config.visual.ImageHorizontalSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;

/** Overlay image visualization remapped by horizontal strips. */
public class ImageHorizontal extends AbstractImageVisualization
    implements ConfigurableVisualization {

  private volatile ImageHorizontalSettings settings = ImageHorizontalSettings.defaults();

  public ImageHorizontal(
      ArrayModel arrayModel, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayModel, colorGradient, sound, rs);
    name = "Image - Horizontal Sorting";
  }

  @Override
  public VisualizationSettings currentSettings() {
    return settings;
  }

  @Override
  public void applySettings(VisualizationSettings next) {
    if (!(next instanceof ImageHorizontalSettings s)) {
      return;
    }
    settings = s;
    applyImageLook(
        s.fitMode() == ImageHorizontalSettings.FitMode.CONTAIN, (float) s.highlightStrength());
  }

  @Override
  protected boolean horizontalMode() {
    return true;
  }
}
