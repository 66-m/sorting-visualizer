package io.github.compilerstuck.visual.gradient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import io.github.compilerstuck.control.config.AppConfig;
import io.github.compilerstuck.visual.Marker;
import java.awt.Color;
import org.junit.jupiter.api.Test;

class ColorGradientTest {

  @Test
  void whiteSetMarkerRemapsOnLightBackground() {
    ColorGradient gradient = new ColorGradient(Color.RED, Color.BLUE, Color.WHITE, "test", 8);
    assertEquals(Color.WHITE, gradient.getMarkerColor(0, Marker.SET));

    Color dark =
        new Color(
            AppConfig.CANVAS_BACKGROUND_DARK,
            AppConfig.CANVAS_BACKGROUND_DARK,
            AppConfig.CANVAS_BACKGROUND_DARK);
    gradient.setLightBackgroundMarkerOverride(dark);
    assertEquals(dark, gradient.getMarkerColor(0, Marker.SET));

    gradient.setLightBackgroundMarkerOverride(null);
    assertEquals(Color.WHITE, gradient.getMarkerColor(0, Marker.SET));
  }

  @Test
  void nonWhiteSetMarkerUnchangedOnLightBackground() {
    ColorGradient gradient = new ColorGradient(Color.WHITE, Color.WHITE, Color.RED, "White", 8);
    Color dark =
        new Color(
            AppConfig.CANVAS_BACKGROUND_DARK,
            AppConfig.CANVAS_BACKGROUND_DARK,
            AppConfig.CANVAS_BACKGROUND_DARK);
    gradient.setLightBackgroundMarkerOverride(dark);
    assertSame(Color.RED, gradient.getMarkerColor(0, Marker.SET));
    assertNotEquals(dark, gradient.getMarkerColor(0, Marker.SET));
  }
}
