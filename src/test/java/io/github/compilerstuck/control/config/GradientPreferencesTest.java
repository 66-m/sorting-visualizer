package io.github.compilerstuck.control.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;
import org.junit.jupiter.api.Test;

class GradientPreferencesTest {

  @Test
  void resolveNamedPreset() {
    ColorGradient gradient =
        GradientPreferences.resolve("Black -> Red", Color.PINK.getRGB(), Color.BLACK.getRGB(), 64);
    assertEquals("Black -> Red", gradient.getName());
  }

  @Test
  void resolveCustomUsesStoredColors() {
    int c1 = new Color(10, 20, 30).getRGB();
    int c2 = new Color(40, 50, 60).getRGB();
    ColorGradient gradient = GradientPreferences.resolve(GradientPresets.CUSTOM_NAME, c1, c2, 64);
    assertEquals(GradientPresets.CUSTOM_NAME, gradient.getName());
    assertEquals(c1, gradient.getColor1().getRGB());
    assertEquals(c2, gradient.getColor2().getRGB());
  }
}
