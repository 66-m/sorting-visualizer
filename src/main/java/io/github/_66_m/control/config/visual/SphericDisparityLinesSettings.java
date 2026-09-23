package io.github._66_m.control.config.visual;

/** Tunable parameters for the `spheric-disparity-lines` visualization. */
public record SphericDisparityLinesSettings(
    double rotationSpeedRadPerSec, double globeScale, int lineOpacity, double markerSize)
    implements VisualizationSettings {

  public static final String ID = "spheric-disparity-lines";

  public static final double DEFAULT_ROTATION_SPEED_RAD_PER_SEC = Math.PI / 10;
  public static final double ROTATION_SPEED_RAD_PER_SEC_MIN = 0.0;
  public static final double ROTATION_SPEED_RAD_PER_SEC_MAX = Math.PI / 2;
  public static final double DEFAULT_GLOBE_SCALE = 1.0 / 2.3;
  public static final double GLOBE_SCALE_MIN = 0.2;
  public static final double GLOBE_SCALE_MAX = 0.5;
  public static final int DEFAULT_LINE_OPACITY = 255;
  public static final int LINE_OPACITY_MIN = 50;
  public static final int LINE_OPACITY_MAX = 255;
  public static final double DEFAULT_MARKER_SIZE = 2.0;
  public static final double MARKER_SIZE_MIN = 1.0;
  public static final double MARKER_SIZE_MAX = 8.0;

  public SphericDisparityLinesSettings {
    rotationSpeedRadPerSec =
        Numbers.clamp(
            rotationSpeedRadPerSec, ROTATION_SPEED_RAD_PER_SEC_MIN, ROTATION_SPEED_RAD_PER_SEC_MAX);
    globeScale = Numbers.clamp(globeScale, GLOBE_SCALE_MIN, GLOBE_SCALE_MAX);
    lineOpacity = (int) Math.round(Numbers.clamp(lineOpacity, LINE_OPACITY_MIN, LINE_OPACITY_MAX));
    markerSize = Numbers.clamp(markerSize, MARKER_SIZE_MIN, MARKER_SIZE_MAX);
  }

  /** Customize-panel layout, ranges and JSON keys for these settings. */
  public static final SettingsSchema<SphericDisparityLinesSettings> SCHEMA =
      SettingsSchema.of(
          ID,
          SphericDisparityLinesSettings.class,
          new SettingsSchema.DoubleParam(
              "globeScale",
              "Scene scale",
              "LAYOUT",
              GLOBE_SCALE_MIN,
              GLOBE_SCALE_MAX,
              DEFAULT_GLOBE_SCALE,
              "%.2f"),
          new SettingsSchema.DoubleParam(
              "rotationSpeedRadPerSec",
              "Rotation speed",
              "LAYOUT",
              ROTATION_SPEED_RAD_PER_SEC_MIN,
              ROTATION_SPEED_RAD_PER_SEC_MAX,
              DEFAULT_ROTATION_SPEED_RAD_PER_SEC,
              "%.2f rad/s"),
          new SettingsSchema.DoubleParam(
              "markerSize",
              "Marker size",
              "LAYOUT",
              MARKER_SIZE_MIN,
              MARKER_SIZE_MAX,
              DEFAULT_MARKER_SIZE,
              "%.1f"),
          new SettingsSchema.IntParam(
              "lineOpacity",
              "Line opacity",
              "LAYOUT",
              LINE_OPACITY_MIN,
              LINE_OPACITY_MAX,
              DEFAULT_LINE_OPACITY));

  public static SphericDisparityLinesSettings defaults() {
    return SCHEMA.defaults();
  }

  @Override
  public String visualizationId() {
    return ID;
  }
}
