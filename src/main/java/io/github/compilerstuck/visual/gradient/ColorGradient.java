package io.github.compilerstuck.visual.gradient;

import io.github.compilerstuck.visual.Marker;
import java.awt.*;

public class ColorGradient {

  /** Default gradient size used when none is supplied; matches the app's default array size. */
  public static final int DEFAULT_SIZE = 1024;

  private Color color1;
  private Color color2;
  private final Color markerSetColor;
  private final String name;
  protected Color[] colorGradient;

  /** Parallel ARGB LUT for {@link #getMarkerArgb}; rebuilt with {@link #colorGradient}. */
  private int[] colorGradientArgb;

  /**
   * When non-null, opaque-white {@link Marker#SET} colors resolve to this instead (light canvas
   * contrast).
   */
  private Color lightBackgroundMarkerOverride;

  public ColorGradient(Color color1, Color color2, Color markerSetColor, String name) {
    this(color1, color2, markerSetColor, name, DEFAULT_SIZE);
  }

  public ColorGradient(Color color1, Color color2, Color markerSetColor, String name, int size) {
    this.color1 = color1;
    this.color2 = color2;
    this.markerSetColor = markerSetColor;
    this.name = name;
    this.colorGradient = this.getColorGradient(size);
    rebuildArgbLut();
  }

  protected Color[] getColorGradient(int size) {
    Color[] colorGradient = new Color[size];
    for (int i = 0; i < size; i++) {
      double scalingFactor;

      scalingFactor = (double) i / size;
      int r = (int) (color1.getRed() + (color2.getRed() - color1.getRed()) * scalingFactor);
      int g = (int) (color1.getGreen() + (color2.getGreen() - color1.getGreen()) * scalingFactor);
      int b = (int) (color1.getBlue() + (color2.getBlue() - color1.getBlue()) * scalingFactor);

      colorGradient[i] = new Color(r, g, b);
    }

    return colorGradient;
  }

  public void updateGradient(int size) {
    colorGradient = getColorGradient(size);
    rebuildArgbLut();
  }

  private void rebuildArgbLut() {
    Color[] gradient = colorGradient;
    if (gradient == null) {
      colorGradientArgb = null;
      return;
    }
    int[] argb = new int[gradient.length];
    for (int i = 0; i < gradient.length; i++) {
      Color c = gradient[i];
      argb[i] = c != null ? c.getRGB() : 0xFF000000;
    }
    colorGradientArgb = argb;
  }

  public Color getMarkerColor(int index, Marker m) {
    if (m == Marker.NORMAL) {
      Color[] gradient = colorGradient;
      if (gradient == null || gradient.length == 0) {
        return Color.BLACK;
      }
      int i = index;
      if (i < 0) {
        i = 0;
      } else if (i >= gradient.length) {
        i = gradient.length - 1;
      }
      return gradient[i];
    } else if (m == Marker.SET) {
      return effectiveMarkerSetColor();
    } else {
      return Color.BLACK;
    }
  }

  /**
   * Same resolution as {@link #getMarkerColor} but returns packed ARGB without allocating or boxing
   * through {@link Color#getRGB()} on the hot path.
   */
  public int getMarkerArgb(int index, Marker m) {
    if (m == Marker.NORMAL) {
      int[] argb = colorGradientArgb;
      if (argb == null || argb.length == 0) {
        return 0xFF000000;
      }
      int i = index;
      if (i < 0) {
        i = 0;
      } else if (i >= argb.length) {
        i = argb.length - 1;
      }
      return argb[i];
    } else if (m == Marker.SET) {
      return effectiveMarkerSetColor().getRGB();
    } else {
      return 0xFF000000;
    }
  }

  /**
   * On a light canvas, remaps opaque-white SET markers to {@code override} so highlights stay
   * visible. Pass {@code null} to restore the preset marker color.
   */
  public void setLightBackgroundMarkerOverride(Color override) {
    this.lightBackgroundMarkerOverride = override;
  }

  public Color getMarkerSetColor() {
    return markerSetColor;
  }

  private Color effectiveMarkerSetColor() {
    if (lightBackgroundMarkerOverride != null && isOpaqueWhite(markerSetColor)) {
      return lightBackgroundMarkerOverride;
    }
    return markerSetColor;
  }

  private static boolean isOpaqueWhite(Color c) {
    return c != null && c.getRed() == 255 && c.getGreen() == 255 && c.getBlue() == 255;
  }

  public void setColor1(Color color1) {
    this.color1 = color1;
  }

  public Color getColor1() {
    return color1;
  }

  public void setColor2(Color color2) {
    this.color2 = color2;
  }

  public Color getColor2() {
    return color2;
  }

  public String getName() {
    return name;
  }
}
