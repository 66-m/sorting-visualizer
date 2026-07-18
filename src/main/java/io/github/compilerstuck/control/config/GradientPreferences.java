package io.github.compilerstuck.control.config;

import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;
import java.util.List;

/** Restores a {@link ColorGradient} from persisted name + RGB ints. */
public final class GradientPreferences {

  private GradientPreferences() {}

  /**
   * Resolves a gradient for startup. Named presets (including Rainbow) are taken from {@link
   * GradientPresets}; {@link GradientPresets#CUSTOM_NAME} or unknown names use stored RGB colors on
   * the Custom entry.
   */
  public static ColorGradient resolve(String name, int color1Rgb, int color2Rgb, int size) {
    List<ColorGradient> presets = GradientPresets.createDefaultList();
    int index = GradientPresets.indexOfName(presets, name);
    ColorGradient selected = presets.get(index);

    if (GradientPresets.CUSTOM_NAME.equals(selected.getName())
        || (name != null && !name.equals(selected.getName()))) {
      ColorGradient custom = presets.get(presets.size() - 1);
      custom.setColor1(new Color(color1Rgb, true));
      custom.setColor2(new Color(color2Rgb, true));
      custom.updateGradient(size);
      return custom;
    }

    selected.updateGradient(size);
    return selected;
  }
}
