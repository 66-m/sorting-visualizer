package io.github.compilerstuck.control.config;

import io.github.compilerstuck.visual.gradient.ColorGradient;
import io.github.compilerstuck.visual.gradient.MultiGradient;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/** Gradient preset catalog (shared by Appearance UI and preference restore). */
public final class GradientPresets {

  public static final String CUSTOM_NAME = "Custom Gradient";

  private GradientPresets() {}

  /** Fresh list; last entry is always the mutable {@link #CUSTOM_NAME}. */
  public static List<ColorGradient> createDefaultList() {
    List<ColorGradient> list = new ArrayList<>();
    list.add(new ColorGradient(new Color(200, 0, 0), new Color(200, 0, 0), Color.WHITE, "Red"));
    list.add(new ColorGradient(new Color(0, 200, 0), new Color(0, 200, 0), Color.WHITE, "Green"));
    list.add(new ColorGradient(new Color(0, 0, 200), new Color(0, 0, 200), Color.WHITE, "Blue"));
    list.add(new ColorGradient(Color.WHITE, Color.WHITE, Color.RED, "White"));
    list.add(new ColorGradient(Color.WHITE, Color.BLACK, Color.WHITE, "White -> Black"));
    list.add(new ColorGradient(Color.RED, Color.BLACK, Color.WHITE, "Red -> Black"));
    list.add(new ColorGradient(Color.BLUE, Color.RED, Color.WHITE, "Blue -> Red"));
    list.add(new ColorGradient(Color.BLACK, Color.WHITE, Color.WHITE, "Black -> White"));
    list.add(new ColorGradient(Color.BLACK, Color.RED, Color.WHITE, "Black -> Red"));
    list.add(new MultiGradient(Color.WHITE, "Rainbow"));
    list.add(new ColorGradient(Color.PINK, Color.BLACK, Color.WHITE, CUSTOM_NAME));
    return list;
  }

  /** Best-effort match of an existing gradient by name; falls back to index 0. */
  public static int indexOfName(List<ColorGradient> presets, String name) {
    if (name == null) {
      return 0;
    }
    for (int i = 0; i < presets.size(); i++) {
      if (name.equals(presets.get(i).getName())) {
        return i;
      }
    }
    return 0;
  }
}
