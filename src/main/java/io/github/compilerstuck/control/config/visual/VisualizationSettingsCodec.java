package io.github.compilerstuck.control.config.visual;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Versioned JSON codec for clipboard envelopes and the prefs map of per-visualization settings.
 * Hand-rolled (no JSON library); only the shapes this app emits/accepts.
 */
public final class VisualizationSettingsCodec {

  public static final int SCHEMA_VERSION = 1;

  private VisualizationSettingsCodec() {}

  /** Clipboard / share envelope. */
  public static String encodeEnvelope(VisualizationSettings settings) {
    if (settings == null) {
      throw new IllegalArgumentException("settings");
    }
    StringBuilder sb = new StringBuilder(128);
    sb.append("{\"schemaVersion\":")
        .append(SCHEMA_VERSION)
        .append(",\"visualizationId\":")
        .append(quote(settings.visualizationId()))
        .append(",\"settings\":");
    appendSettingsObject(sb, settings);
    sb.append('}');
    return sb.toString();
  }

  /**
   * Decodes a clipboard envelope. Returns empty on malformed input, unknown schema, or unknown
   * visualization id. Out-of-range numeric fields are clamped by the settings record constructors.
   */
  public static Optional<DecodeResult> decodeEnvelope(String raw) {
    if (raw == null || raw.isBlank()) {
      return Optional.empty();
    }
    try {
      JsonObject root = JsonObject.parse(raw.trim());
      int version = root.getInt("schemaVersion", -1);
      if (version != SCHEMA_VERSION) {
        return Optional.empty();
      }
      String id = root.getString("visualizationId", "");
      if (id.isBlank()) {
        return Optional.empty();
      }
      JsonObject settingsObj = root.getObject("settings");
      if (settingsObj == null) {
        return Optional.empty();
      }
      VisualizationSettings settings = decodeSettings(id, settingsObj);
      if (settings == null) {
        return Optional.empty();
      }
      boolean clamped = settingsObj.hadOutOfRangeHint;
      return Optional.of(new DecodeResult(settings, clamped));
    } catch (IllegalArgumentException ex) {
      return Optional.empty();
    }
  }

  /** Prefs blob: {@code {"cube":{...}, ...}}. */
  public static String encodeStore(Map<String, VisualizationSettings> byId) {
    StringBuilder sb = new StringBuilder(64);
    sb.append('{');
    boolean first = true;
    if (byId != null) {
      for (Map.Entry<String, VisualizationSettings> e : byId.entrySet()) {
        if (e.getKey() == null || e.getKey().isBlank() || e.getValue() == null) {
          continue;
        }
        if (!e.getKey().equals(e.getValue().visualizationId())) {
          continue;
        }
        if (!first) {
          sb.append(',');
        }
        first = false;
        sb.append(quote(e.getKey())).append(':');
        appendSettingsObject(sb, e.getValue());
      }
    }
    sb.append('}');
    return sb.toString();
  }

  /** Decodes prefs blob; skips unknown ids / malformed entries. */
  public static Map<String, VisualizationSettings> decodeStore(String raw) {
    if (raw == null || raw.isBlank() || "{}".equals(raw.trim())) {
      return Map.of();
    }
    try {
      JsonObject root = JsonObject.parse(raw.trim());
      Map<String, VisualizationSettings> out = new LinkedHashMap<>();
      for (String key : root.keys()) {
        JsonObject obj = root.getObject(key);
        if (obj == null) {
          continue;
        }
        VisualizationSettings settings = decodeSettings(key, obj);
        if (settings != null) {
          out.put(key, settings);
        }
      }
      return Collections.unmodifiableMap(out);
    } catch (IllegalArgumentException ex) {
      return Map.of();
    }
  }

  private static VisualizationSettings decodeSettings(String id, JsonObject obj) {
    if (CubeSettings.ID.equals(id)) {
      CubeSettings defaults = CubeSettings.defaults();
      double rot = obj.getDouble("rotationSpeedRadPerSec", defaults.rotationSpeedRadPerSec());
      int opacity = (int) Math.round(obj.getDouble("fillOpacity", defaults.fillOpacity()));
      boolean wire = obj.getBoolean("wireframeEnabled", defaults.wireframeEnabled());
      double scale = obj.getDouble("sceneScaleDivisor", defaults.sceneScaleDivisor());
      if (rot != clamp(rot, CubeSettings.ROTATION_SPEED_MIN, CubeSettings.ROTATION_SPEED_MAX)
          || opacity < CubeSettings.FILL_OPACITY_MIN
          || opacity > CubeSettings.FILL_OPACITY_MAX
          || scale
              != clamp(
                  scale,
                  CubeSettings.SCENE_SCALE_DIVISOR_MIN,
                  CubeSettings.SCENE_SCALE_DIVISOR_MAX)) {
        obj.hadOutOfRangeHint = true;
      }
      return new CubeSettings(rot, opacity, wire, scale);
    }
    if (CircleSettings.ID.equals(id)) {
      CircleSettings d = CircleSettings.defaults();
      double radiusScale = obj.getDouble("radiusScale", d.radiusScale());
      double startAngleDeg = obj.getDouble("startAngleDeg", d.startAngleDeg());
      double lineThickness = obj.getDouble("lineThickness", d.lineThickness());
      if (radiusScale
              != clamp(
                  radiusScale, CircleSettings.RADIUS_SCALE_MIN, CircleSettings.RADIUS_SCALE_MAX)
          || startAngleDeg
              != clamp(
                  startAngleDeg,
                  CircleSettings.START_ANGLE_DEG_MIN,
                  CircleSettings.START_ANGLE_DEG_MAX)
          || lineThickness
              != clamp(
                  lineThickness,
                  CircleSettings.LINE_THICKNESS_MIN,
                  CircleSettings.LINE_THICKNESS_MAX)) {
        obj.hadOutOfRangeHint = true;
      }
      return new CircleSettings(radiusScale, startAngleDeg, lineThickness);
    }
    if (ColorGradientGraphSettings.ID.equals(id)) {
      ColorGradientGraphSettings d = ColorGradientGraphSettings.defaults();
      boolean showIndexDividers = obj.getBoolean("showIndexDividers", d.showIndexDividers());
      return new ColorGradientGraphSettings(showIndexDividers);
    }
    if (CubicLinesSettings.ID.equals(id)) {
      CubicLinesSettings d = CubicLinesSettings.defaults();
      double rotationSpeedRadPerSec =
          obj.getDouble("rotationSpeedRadPerSec", d.rotationSpeedRadPerSec());
      double sceneScaleDivisor = obj.getDouble("sceneScaleDivisor", d.sceneScaleDivisor());
      double markerSize = obj.getDouble("markerSize", d.markerSize());
      int lineOpacity = (int) Math.round(obj.getDouble("lineOpacity", d.lineOpacity()));
      if (rotationSpeedRadPerSec
              != clamp(
                  rotationSpeedRadPerSec,
                  CubicLinesSettings.ROTATION_SPEED_RAD_PER_SEC_MIN,
                  CubicLinesSettings.ROTATION_SPEED_RAD_PER_SEC_MAX)
          || sceneScaleDivisor
              != clamp(
                  sceneScaleDivisor,
                  CubicLinesSettings.SCENE_SCALE_DIVISOR_MIN,
                  CubicLinesSettings.SCENE_SCALE_DIVISOR_MAX)
          || markerSize
              != clamp(
                  markerSize,
                  CubicLinesSettings.MARKER_SIZE_MIN,
                  CubicLinesSettings.MARKER_SIZE_MAX)
          || lineOpacity < CubicLinesSettings.LINE_OPACITY_MIN
          || lineOpacity > CubicLinesSettings.LINE_OPACITY_MAX) {
        obj.hadOutOfRangeHint = true;
      }
      return new CubicLinesSettings(
          rotationSpeedRadPerSec, sceneScaleDivisor, markerSize, lineOpacity);
    }
    if (DisparityChordsSettings.ID.equals(id)) {
      DisparityChordsSettings d = DisparityChordsSettings.defaults();
      double radiusScale = obj.getDouble("radiusScale", d.radiusScale());
      double lineThickness = obj.getDouble("lineThickness", d.lineThickness());
      double coincidentMarkerSize = obj.getDouble("coincidentMarkerSize", d.coincidentMarkerSize());
      int chordOpacity = (int) Math.round(obj.getDouble("chordOpacity", d.chordOpacity()));
      if (radiusScale
              != clamp(
                  radiusScale,
                  DisparityChordsSettings.RADIUS_SCALE_MIN,
                  DisparityChordsSettings.RADIUS_SCALE_MAX)
          || lineThickness
              != clamp(
                  lineThickness,
                  DisparityChordsSettings.LINE_THICKNESS_MIN,
                  DisparityChordsSettings.LINE_THICKNESS_MAX)
          || coincidentMarkerSize
              != clamp(
                  coincidentMarkerSize,
                  DisparityChordsSettings.COINCIDENT_MARKER_SIZE_MIN,
                  DisparityChordsSettings.COINCIDENT_MARKER_SIZE_MAX)
          || chordOpacity < DisparityChordsSettings.CHORD_OPACITY_MIN
          || chordOpacity > DisparityChordsSettings.CHORD_OPACITY_MAX) {
        obj.hadOutOfRangeHint = true;
      }
      return new DisparityChordsSettings(
          radiusScale, lineThickness, coincidentMarkerSize, chordOpacity);
    }
    if (DisparityCircleSettings.ID.equals(id)) {
      DisparityCircleSettings d = DisparityCircleSettings.defaults();
      double radiusScale = obj.getDouble("radiusScale", d.radiusScale());
      double lineThickness = obj.getDouble("lineThickness", d.lineThickness());
      double startAngleDeg = obj.getDouble("startAngleDeg", d.startAngleDeg());
      if (radiusScale
              != clamp(
                  radiusScale,
                  DisparityCircleSettings.RADIUS_SCALE_MIN,
                  DisparityCircleSettings.RADIUS_SCALE_MAX)
          || lineThickness
              != clamp(
                  lineThickness,
                  DisparityCircleSettings.LINE_THICKNESS_MIN,
                  DisparityCircleSettings.LINE_THICKNESS_MAX)
          || startAngleDeg
              != clamp(
                  startAngleDeg,
                  DisparityCircleSettings.START_ANGLE_DEG_MIN,
                  DisparityCircleSettings.START_ANGLE_DEG_MAX)) {
        obj.hadOutOfRangeHint = true;
      }
      return new DisparityCircleSettings(radiusScale, lineThickness, startAngleDeg);
    }
    if (DisparityCircleScatterSettings.ID.equals(id)) {
      DisparityCircleScatterSettings d = DisparityCircleScatterSettings.defaults();
      double pointSize = obj.getDouble("pointSize", d.pointSize());
      double radiusScale = obj.getDouble("radiusScale", d.radiusScale());
      double startAngleDeg = obj.getDouble("startAngleDeg", d.startAngleDeg());
      if (pointSize
              != clamp(
                  pointSize,
                  DisparityCircleScatterSettings.POINT_SIZE_MIN,
                  DisparityCircleScatterSettings.POINT_SIZE_MAX)
          || radiusScale
              != clamp(
                  radiusScale,
                  DisparityCircleScatterSettings.RADIUS_SCALE_MIN,
                  DisparityCircleScatterSettings.RADIUS_SCALE_MAX)
          || startAngleDeg
              != clamp(
                  startAngleDeg,
                  DisparityCircleScatterSettings.START_ANGLE_DEG_MIN,
                  DisparityCircleScatterSettings.START_ANGLE_DEG_MAX)) {
        obj.hadOutOfRangeHint = true;
      }
      return new DisparityCircleScatterSettings(pointSize, radiusScale, startAngleDeg);
    }
    if (DisparityCircleScatterLinkedSettings.ID.equals(id)) {
      DisparityCircleScatterLinkedSettings d = DisparityCircleScatterLinkedSettings.defaults();
      double lineThickness = obj.getDouble("lineThickness", d.lineThickness());
      double radiusScale = obj.getDouble("radiusScale", d.radiusScale());
      if (lineThickness
              != clamp(
                  lineThickness,
                  DisparityCircleScatterLinkedSettings.LINE_THICKNESS_MIN,
                  DisparityCircleScatterLinkedSettings.LINE_THICKNESS_MAX)
          || radiusScale
              != clamp(
                  radiusScale,
                  DisparityCircleScatterLinkedSettings.RADIUS_SCALE_MIN,
                  DisparityCircleScatterLinkedSettings.RADIUS_SCALE_MAX)) {
        obj.hadOutOfRangeHint = true;
      }
      return new DisparityCircleScatterLinkedSettings(lineThickness, radiusScale);
    }
    if (DisparityPlaneSettings.ID.equals(id)) {
      DisparityPlaneSettings d = DisparityPlaneSettings.defaults();
      double rotationSpeedRadPerSec =
          obj.getDouble("rotationSpeedRadPerSec", d.rotationSpeedRadPerSec());
      double maxExtrusionFraction = obj.getDouble("maxExtrusionFraction", d.maxExtrusionFraction());
      double planeScale = obj.getDouble("planeScale", d.planeScale());
      double tileGap = obj.getDouble("tileGap", d.tileGap());
      if (rotationSpeedRadPerSec
              != clamp(
                  rotationSpeedRadPerSec,
                  DisparityPlaneSettings.ROTATION_SPEED_RAD_PER_SEC_MIN,
                  DisparityPlaneSettings.ROTATION_SPEED_RAD_PER_SEC_MAX)
          || maxExtrusionFraction
              != clamp(
                  maxExtrusionFraction,
                  DisparityPlaneSettings.MAX_EXTRUSION_FRACTION_MIN,
                  DisparityPlaneSettings.MAX_EXTRUSION_FRACTION_MAX)
          || planeScale
              != clamp(
                  planeScale,
                  DisparityPlaneSettings.PLANE_SCALE_MIN,
                  DisparityPlaneSettings.PLANE_SCALE_MAX)
          || tileGap
              != clamp(
                  tileGap,
                  DisparityPlaneSettings.TILE_GAP_MIN,
                  DisparityPlaneSettings.TILE_GAP_MAX)) {
        obj.hadOutOfRangeHint = true;
      }
      return new DisparityPlaneSettings(
          rotationSpeedRadPerSec, maxExtrusionFraction, planeScale, tileGap);
    }
    if (DisparitySphereHoopsSettings.ID.equals(id)) {
      DisparitySphereHoopsSettings d = DisparitySphereHoopsSettings.defaults();
      double globeScale = obj.getDouble("globeScale", d.globeScale());
      if (globeScale
          != clamp(
              globeScale,
              DisparitySphereHoopsSettings.GLOBE_SCALE_MIN,
              DisparitySphereHoopsSettings.GLOBE_SCALE_MAX)) {
        obj.hadOutOfRangeHint = true;
      }
      return new DisparitySphereHoopsSettings(globeScale);
    }
    if (DisparitySquareScatterSettings.ID.equals(id)) {
      DisparitySquareScatterSettings d = DisparitySquareScatterSettings.defaults();
      double pointSize = obj.getDouble("pointSize", d.pointSize());
      double perimeterScale = obj.getDouble("perimeterScale", d.perimeterScale());
      if (pointSize
              != clamp(
                  pointSize,
                  DisparitySquareScatterSettings.POINT_SIZE_MIN,
                  DisparitySquareScatterSettings.POINT_SIZE_MAX)
          || perimeterScale
              != clamp(
                  perimeterScale,
                  DisparitySquareScatterSettings.PERIMETER_SCALE_MIN,
                  DisparitySquareScatterSettings.PERIMETER_SCALE_MAX)) {
        obj.hadOutOfRangeHint = true;
      }
      return new DisparitySquareScatterSettings(pointSize, perimeterScale);
    }
    if (HoopsSettings.ID.equals(id)) {
      HoopsSettings d = HoopsSettings.defaults();
      double radiusScale = obj.getDouble("radiusScale", d.radiusScale());
      if (radiusScale
          != clamp(radiusScale, HoopsSettings.RADIUS_SCALE_MIN, HoopsSettings.RADIUS_SCALE_MAX)) {
        obj.hadOutOfRangeHint = true;
      }
      return new HoopsSettings(radiusScale);
    }
    if (ImageVerticalSettings.ID.equals(id)) {
      ImageVerticalSettings d = ImageVerticalSettings.defaults();
      String fitModeRaw = obj.getString("fitMode", d.fitMode().name());
      ImageVerticalSettings.FitMode fitMode;
      try {
        fitMode = ImageVerticalSettings.FitMode.valueOf(fitModeRaw);
      } catch (IllegalArgumentException ex) {
        fitMode = d.fitMode();
        obj.hadOutOfRangeHint = true;
      }
      double highlightStrength = obj.getDouble("highlightStrength", d.highlightStrength());
      if (highlightStrength
          != clamp(
              highlightStrength,
              ImageVerticalSettings.HIGHLIGHT_STRENGTH_MIN,
              ImageVerticalSettings.HIGHLIGHT_STRENGTH_MAX)) {
        obj.hadOutOfRangeHint = true;
      }
      return new ImageVerticalSettings(fitMode, highlightStrength);
    }
    if (ImageHorizontalSettings.ID.equals(id)) {
      ImageHorizontalSettings d = ImageHorizontalSettings.defaults();
      String fitModeRaw = obj.getString("fitMode", d.fitMode().name());
      ImageHorizontalSettings.FitMode fitMode;
      try {
        fitMode = ImageHorizontalSettings.FitMode.valueOf(fitModeRaw);
      } catch (IllegalArgumentException ex) {
        fitMode = d.fitMode();
        obj.hadOutOfRangeHint = true;
      }
      double highlightStrength = obj.getDouble("highlightStrength", d.highlightStrength());
      if (highlightStrength
          != clamp(
              highlightStrength,
              ImageHorizontalSettings.HIGHLIGHT_STRENGTH_MIN,
              ImageHorizontalSettings.HIGHLIGHT_STRENGTH_MAX)) {
        obj.hadOutOfRangeHint = true;
      }
      return new ImageHorizontalSettings(fitMode, highlightStrength);
    }
    if (MorphingShellSettings.ID.equals(id)) {
      MorphingShellSettings d = MorphingShellSettings.defaults();
      double rotationSpeedRadPerSec =
          obj.getDouble("rotationSpeedRadPerSec", d.rotationSpeedRadPerSec());
      double sphereSize = obj.getDouble("sphereSize", d.sphereSize());
      double shellRadiusScale = obj.getDouble("shellRadiusScale", d.shellRadiusScale());
      if (rotationSpeedRadPerSec
              != clamp(
                  rotationSpeedRadPerSec,
                  MorphingShellSettings.ROTATION_SPEED_RAD_PER_SEC_MIN,
                  MorphingShellSettings.ROTATION_SPEED_RAD_PER_SEC_MAX)
          || sphereSize
              != clamp(
                  sphereSize,
                  MorphingShellSettings.SPHERE_SIZE_MIN,
                  MorphingShellSettings.SPHERE_SIZE_MAX)
          || shellRadiusScale
              != clamp(
                  shellRadiusScale,
                  MorphingShellSettings.SHELL_RADIUS_SCALE_MIN,
                  MorphingShellSettings.SHELL_RADIUS_SCALE_MAX)) {
        obj.hadOutOfRangeHint = true;
      }
      return new MorphingShellSettings(rotationSpeedRadPerSec, sphereSize, shellRadiusScale);
    }
    if (MosaicSquaresSettings.ID.equals(id)) {
      MosaicSquaresSettings d = MosaicSquaresSettings.defaults();
      double tileGapPx = obj.getDouble("tileGapPx", d.tileGapPx());
      if (tileGapPx
          != clamp(
              tileGapPx,
              MosaicSquaresSettings.TILE_GAP_PX_MIN,
              MosaicSquaresSettings.TILE_GAP_PX_MAX)) {
        obj.hadOutOfRangeHint = true;
      }
      return new MosaicSquaresSettings(tileGapPx);
    }
    if (NumberPlotSettings.ID.equals(id)) {
      NumberPlotSettings d = NumberPlotSettings.defaults();
      double fontSize = obj.getDouble("fontSize", d.fontSize());
      if (fontSize
          != clamp(fontSize, NumberPlotSettings.FONT_SIZE_MIN, NumberPlotSettings.FONT_SIZE_MAX)) {
        obj.hadOutOfRangeHint = true;
      }
      return new NumberPlotSettings(fontSize);
    }
    if (PhyllotaxisSettings.ID.equals(id)) {
      PhyllotaxisSettings d = PhyllotaxisSettings.defaults();
      double angleStepDeg = obj.getDouble("angleStepDeg", d.angleStepDeg());
      double scaleDivisor = obj.getDouble("scaleDivisor", d.scaleDivisor());
      double pointSize = obj.getDouble("pointSize", d.pointSize());
      if (angleStepDeg
              != clamp(
                  angleStepDeg,
                  PhyllotaxisSettings.ANGLE_STEP_DEG_MIN,
                  PhyllotaxisSettings.ANGLE_STEP_DEG_MAX)
          || scaleDivisor
              != clamp(
                  scaleDivisor,
                  PhyllotaxisSettings.SCALE_DIVISOR_MIN,
                  PhyllotaxisSettings.SCALE_DIVISOR_MAX)
          || pointSize
              != clamp(
                  pointSize,
                  PhyllotaxisSettings.POINT_SIZE_MIN,
                  PhyllotaxisSettings.POINT_SIZE_MAX)) {
        obj.hadOutOfRangeHint = true;
      }
      return new PhyllotaxisSettings(angleStepDeg, scaleDivisor, pointSize);
    }
    if (PlaneSettings.ID.equals(id)) {
      PlaneSettings d = PlaneSettings.defaults();
      double rotationSpeedRadPerSec =
          obj.getDouble("rotationSpeedRadPerSec", d.rotationSpeedRadPerSec());
      double planeScale = obj.getDouble("planeScale", d.planeScale());
      double tileGap = obj.getDouble("tileGap", d.tileGap());
      if (rotationSpeedRadPerSec
              != clamp(
                  rotationSpeedRadPerSec,
                  PlaneSettings.ROTATION_SPEED_RAD_PER_SEC_MIN,
                  PlaneSettings.ROTATION_SPEED_RAD_PER_SEC_MAX)
          || planeScale
              != clamp(planeScale, PlaneSettings.PLANE_SCALE_MIN, PlaneSettings.PLANE_SCALE_MAX)
          || tileGap != clamp(tileGap, PlaneSettings.TILE_GAP_MIN, PlaneSettings.TILE_GAP_MAX)) {
        obj.hadOutOfRangeHint = true;
      }
      return new PlaneSettings(rotationSpeedRadPerSec, planeScale, tileGap);
    }
    if (PyramidSettings.ID.equals(id)) {
      PyramidSettings d = PyramidSettings.defaults();
      double rotationSpeedRadPerSec =
          obj.getDouble("rotationSpeedRadPerSec", d.rotationSpeedRadPerSec());
      double stackScale = obj.getDouble("stackScale", d.stackScale());
      if (rotationSpeedRadPerSec
              != clamp(
                  rotationSpeedRadPerSec,
                  PyramidSettings.ROTATION_SPEED_RAD_PER_SEC_MIN,
                  PyramidSettings.ROTATION_SPEED_RAD_PER_SEC_MAX)
          || stackScale
              != clamp(
                  stackScale, PyramidSettings.STACK_SCALE_MIN, PyramidSettings.STACK_SCALE_MAX)) {
        obj.hadOutOfRangeHint = true;
      }
      return new PyramidSettings(rotationSpeedRadPerSec, stackScale);
    }
    if (ScatterPlotSettings.ID.equals(id)) {
      ScatterPlotSettings d = ScatterPlotSettings.defaults();
      double pointSize = obj.getDouble("pointSize", d.pointSize());
      if (pointSize
          != clamp(
              pointSize, ScatterPlotSettings.POINT_SIZE_MIN, ScatterPlotSettings.POINT_SIZE_MAX)) {
        obj.hadOutOfRangeHint = true;
      }
      return new ScatterPlotSettings(pointSize);
    }
    if (ScatterPlotLinkedSettings.ID.equals(id)) {
      ScatterPlotLinkedSettings d = ScatterPlotLinkedSettings.defaults();
      double lineThickness = obj.getDouble("lineThickness", d.lineThickness());
      if (lineThickness
          != clamp(
              lineThickness,
              ScatterPlotLinkedSettings.LINE_THICKNESS_MIN,
              ScatterPlotLinkedSettings.LINE_THICKNESS_MAX)) {
        obj.hadOutOfRangeHint = true;
      }
      return new ScatterPlotLinkedSettings(lineThickness);
    }
    if (SphereSettings.ID.equals(id)) {
      SphereSettings d = SphereSettings.defaults();
      double rotationSpeedRadPerSec =
          obj.getDouble("rotationSpeedRadPerSec", d.rotationSpeedRadPerSec());
      double globeScale = obj.getDouble("globeScale", d.globeScale());
      double pointSize = obj.getDouble("pointSize", d.pointSize());
      if (rotationSpeedRadPerSec
              != clamp(
                  rotationSpeedRadPerSec,
                  SphereSettings.ROTATION_SPEED_RAD_PER_SEC_MIN,
                  SphereSettings.ROTATION_SPEED_RAD_PER_SEC_MAX)
          || globeScale
              != clamp(globeScale, SphereSettings.GLOBE_SCALE_MIN, SphereSettings.GLOBE_SCALE_MAX)
          || pointSize
              != clamp(pointSize, SphereSettings.POINT_SIZE_MIN, SphereSettings.POINT_SIZE_MAX)) {
        obj.hadOutOfRangeHint = true;
      }
      return new SphereSettings(rotationSpeedRadPerSec, globeScale, pointSize);
    }
    if (SphereHoopsSettings.ID.equals(id)) {
      SphereHoopsSettings d = SphereHoopsSettings.defaults();
      double globeScale = obj.getDouble("globeScale", d.globeScale());
      if (globeScale
          != clamp(
              globeScale,
              SphereHoopsSettings.GLOBE_SCALE_MIN,
              SphereHoopsSettings.GLOBE_SCALE_MAX)) {
        obj.hadOutOfRangeHint = true;
      }
      return new SphereHoopsSettings(globeScale);
    }
    if (SphericDisparityLinesSettings.ID.equals(id)) {
      SphericDisparityLinesSettings d = SphericDisparityLinesSettings.defaults();
      double rotationSpeedRadPerSec =
          obj.getDouble("rotationSpeedRadPerSec", d.rotationSpeedRadPerSec());
      double globeScale = obj.getDouble("globeScale", d.globeScale());
      int lineOpacity = (int) Math.round(obj.getDouble("lineOpacity", d.lineOpacity()));
      double markerSize = obj.getDouble("markerSize", d.markerSize());
      if (rotationSpeedRadPerSec
              != clamp(
                  rotationSpeedRadPerSec,
                  SphericDisparityLinesSettings.ROTATION_SPEED_RAD_PER_SEC_MIN,
                  SphericDisparityLinesSettings.ROTATION_SPEED_RAD_PER_SEC_MAX)
          || globeScale
              != clamp(
                  globeScale,
                  SphericDisparityLinesSettings.GLOBE_SCALE_MIN,
                  SphericDisparityLinesSettings.GLOBE_SCALE_MAX)
          || lineOpacity < SphericDisparityLinesSettings.LINE_OPACITY_MIN
          || lineOpacity > SphericDisparityLinesSettings.LINE_OPACITY_MAX
          || markerSize
              != clamp(
                  markerSize,
                  SphericDisparityLinesSettings.MARKER_SIZE_MIN,
                  SphericDisparityLinesSettings.MARKER_SIZE_MAX)) {
        obj.hadOutOfRangeHint = true;
      }
      return new SphericDisparityLinesSettings(
          rotationSpeedRadPerSec, globeScale, lineOpacity, markerSize);
    }
    if (SwirlDotsSettings.ID.equals(id)) {
      SwirlDotsSettings d = SwirlDotsSettings.defaults();
      double spiralTurns = obj.getDouble("spiralTurns", d.spiralTurns());
      double radiusScale = obj.getDouble("radiusScale", d.radiusScale());
      double pointSize = obj.getDouble("pointSize", d.pointSize());
      if (spiralTurns
              != clamp(
                  spiralTurns,
                  SwirlDotsSettings.SPIRAL_TURNS_MIN,
                  SwirlDotsSettings.SPIRAL_TURNS_MAX)
          || radiusScale
              != clamp(
                  radiusScale,
                  SwirlDotsSettings.RADIUS_SCALE_MIN,
                  SwirlDotsSettings.RADIUS_SCALE_MAX)
          || pointSize
              != clamp(
                  pointSize, SwirlDotsSettings.POINT_SIZE_MIN, SwirlDotsSettings.POINT_SIZE_MAX)) {
        obj.hadOutOfRangeHint = true;
      }
      return new SwirlDotsSettings(spiralTurns, radiusScale, pointSize);
    }
    return null;
  }

  private static void appendSettingsObject(StringBuilder sb, VisualizationSettings settings) {
    if (settings instanceof CubeSettings cube) {
      sb.append('{');
      appendNumber(sb, "rotationSpeedRadPerSec", cube.rotationSpeedRadPerSec());
      sb.append(',');
      sb.append(quote("fillOpacity")).append(':').append(cube.fillOpacity());
      sb.append(',');
      sb.append(quote("wireframeEnabled")).append(':').append(cube.wireframeEnabled());
      sb.append(',');
      appendNumber(sb, "sceneScaleDivisor", cube.sceneScaleDivisor());
      sb.append('}');
      return;
    }
    if (settings instanceof CircleSettings s) {
      sb.append('{');
      appendNumber(sb, "radiusScale", s.radiusScale());
      sb.append(',');
      appendNumber(sb, "startAngleDeg", s.startAngleDeg());
      sb.append(',');
      appendNumber(sb, "lineThickness", s.lineThickness());
      sb.append('}');
      return;
    }
    if (settings instanceof ColorGradientGraphSettings s) {
      sb.append('{');
      sb.append(quote("showIndexDividers")).append(':').append(s.showIndexDividers());
      sb.append('}');
      return;
    }
    if (settings instanceof CubicLinesSettings s) {
      sb.append('{');
      appendNumber(sb, "rotationSpeedRadPerSec", s.rotationSpeedRadPerSec());
      sb.append(',');
      appendNumber(sb, "sceneScaleDivisor", s.sceneScaleDivisor());
      sb.append(',');
      appendNumber(sb, "markerSize", s.markerSize());
      sb.append(',');
      sb.append(quote("lineOpacity")).append(':').append(s.lineOpacity());
      sb.append('}');
      return;
    }
    if (settings instanceof DisparityChordsSettings s) {
      sb.append('{');
      appendNumber(sb, "radiusScale", s.radiusScale());
      sb.append(',');
      appendNumber(sb, "lineThickness", s.lineThickness());
      sb.append(',');
      appendNumber(sb, "coincidentMarkerSize", s.coincidentMarkerSize());
      sb.append(',');
      sb.append(quote("chordOpacity")).append(':').append(s.chordOpacity());
      sb.append('}');
      return;
    }
    if (settings instanceof DisparityCircleSettings s) {
      sb.append('{');
      appendNumber(sb, "radiusScale", s.radiusScale());
      sb.append(',');
      appendNumber(sb, "lineThickness", s.lineThickness());
      sb.append(',');
      appendNumber(sb, "startAngleDeg", s.startAngleDeg());
      sb.append('}');
      return;
    }
    if (settings instanceof DisparityCircleScatterSettings s) {
      sb.append('{');
      appendNumber(sb, "pointSize", s.pointSize());
      sb.append(',');
      appendNumber(sb, "radiusScale", s.radiusScale());
      sb.append(',');
      appendNumber(sb, "startAngleDeg", s.startAngleDeg());
      sb.append('}');
      return;
    }
    if (settings instanceof DisparityCircleScatterLinkedSettings s) {
      sb.append('{');
      appendNumber(sb, "lineThickness", s.lineThickness());
      sb.append(',');
      appendNumber(sb, "radiusScale", s.radiusScale());
      sb.append('}');
      return;
    }
    if (settings instanceof DisparityPlaneSettings s) {
      sb.append('{');
      appendNumber(sb, "rotationSpeedRadPerSec", s.rotationSpeedRadPerSec());
      sb.append(',');
      appendNumber(sb, "maxExtrusionFraction", s.maxExtrusionFraction());
      sb.append(',');
      appendNumber(sb, "planeScale", s.planeScale());
      sb.append(',');
      appendNumber(sb, "tileGap", s.tileGap());
      sb.append('}');
      return;
    }
    if (settings instanceof DisparitySphereHoopsSettings s) {
      sb.append('{');
      appendNumber(sb, "globeScale", s.globeScale());
      sb.append('}');
      return;
    }
    if (settings instanceof DisparitySquareScatterSettings s) {
      sb.append('{');
      appendNumber(sb, "pointSize", s.pointSize());
      sb.append(',');
      appendNumber(sb, "perimeterScale", s.perimeterScale());
      sb.append('}');
      return;
    }
    if (settings instanceof HoopsSettings s) {
      sb.append('{');
      appendNumber(sb, "radiusScale", s.radiusScale());
      sb.append('}');
      return;
    }
    if (settings instanceof ImageVerticalSettings s) {
      sb.append('{');
      sb.append(quote("fitMode")).append(':').append(quote(s.fitMode().name()));
      sb.append(',');
      appendNumber(sb, "highlightStrength", s.highlightStrength());
      sb.append('}');
      return;
    }
    if (settings instanceof ImageHorizontalSettings s) {
      sb.append('{');
      sb.append(quote("fitMode")).append(':').append(quote(s.fitMode().name()));
      sb.append(',');
      appendNumber(sb, "highlightStrength", s.highlightStrength());
      sb.append('}');
      return;
    }
    if (settings instanceof MorphingShellSettings s) {
      sb.append('{');
      appendNumber(sb, "rotationSpeedRadPerSec", s.rotationSpeedRadPerSec());
      sb.append(',');
      appendNumber(sb, "sphereSize", s.sphereSize());
      sb.append(',');
      appendNumber(sb, "shellRadiusScale", s.shellRadiusScale());
      sb.append('}');
      return;
    }
    if (settings instanceof MosaicSquaresSettings s) {
      sb.append('{');
      appendNumber(sb, "tileGapPx", s.tileGapPx());
      sb.append('}');
      return;
    }
    if (settings instanceof NumberPlotSettings s) {
      sb.append('{');
      appendNumber(sb, "fontSize", s.fontSize());
      sb.append('}');
      return;
    }
    if (settings instanceof PhyllotaxisSettings s) {
      sb.append('{');
      appendNumber(sb, "angleStepDeg", s.angleStepDeg());
      sb.append(',');
      appendNumber(sb, "scaleDivisor", s.scaleDivisor());
      sb.append(',');
      appendNumber(sb, "pointSize", s.pointSize());
      sb.append('}');
      return;
    }
    if (settings instanceof PlaneSettings s) {
      sb.append('{');
      appendNumber(sb, "rotationSpeedRadPerSec", s.rotationSpeedRadPerSec());
      sb.append(',');
      appendNumber(sb, "planeScale", s.planeScale());
      sb.append(',');
      appendNumber(sb, "tileGap", s.tileGap());
      sb.append('}');
      return;
    }
    if (settings instanceof PyramidSettings s) {
      sb.append('{');
      appendNumber(sb, "rotationSpeedRadPerSec", s.rotationSpeedRadPerSec());
      sb.append(',');
      appendNumber(sb, "stackScale", s.stackScale());
      sb.append('}');
      return;
    }
    if (settings instanceof ScatterPlotSettings s) {
      sb.append('{');
      appendNumber(sb, "pointSize", s.pointSize());
      sb.append('}');
      return;
    }
    if (settings instanceof ScatterPlotLinkedSettings s) {
      sb.append('{');
      appendNumber(sb, "lineThickness", s.lineThickness());
      sb.append('}');
      return;
    }
    if (settings instanceof SphereSettings s) {
      sb.append('{');
      appendNumber(sb, "rotationSpeedRadPerSec", s.rotationSpeedRadPerSec());
      sb.append(',');
      appendNumber(sb, "globeScale", s.globeScale());
      sb.append(',');
      appendNumber(sb, "pointSize", s.pointSize());
      sb.append('}');
      return;
    }
    if (settings instanceof SphereHoopsSettings s) {
      sb.append('{');
      appendNumber(sb, "globeScale", s.globeScale());
      sb.append('}');
      return;
    }
    if (settings instanceof SphericDisparityLinesSettings s) {
      sb.append('{');
      appendNumber(sb, "rotationSpeedRadPerSec", s.rotationSpeedRadPerSec());
      sb.append(',');
      appendNumber(sb, "globeScale", s.globeScale());
      sb.append(',');
      sb.append(quote("lineOpacity")).append(':').append(s.lineOpacity());
      sb.append(',');
      appendNumber(sb, "markerSize", s.markerSize());
      sb.append('}');
      return;
    }
    if (settings instanceof SwirlDotsSettings s) {
      sb.append('{');
      appendNumber(sb, "spiralTurns", s.spiralTurns());
      sb.append(',');
      appendNumber(sb, "radiusScale", s.radiusScale());
      sb.append(',');
      appendNumber(sb, "pointSize", s.pointSize());
      sb.append('}');
      return;
    }
    throw new IllegalArgumentException("Unsupported settings type: " + settings.getClass());
  }

  private static void appendNumber(StringBuilder sb, String key, double value) {
    sb.append(quote(key)).append(':');
    if (Double.isFinite(value)) {
      // Full precision; Locale.ROOT avoids comma decimals.
      sb.append(Double.toString(value));
    } else {
      sb.append('0');
    }
  }

  private static String quote(String s) {
    StringBuilder sb = new StringBuilder(s.length() + 2);
    sb.append('"');
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\\', '"' -> sb.append('\\').append(c);
        case '\n' -> sb.append("\\n");
        case '\r' -> sb.append("\\r");
        case '\t' -> sb.append("\\t");
        default -> sb.append(c);
      }
    }
    sb.append('"');
    return sb.toString();
  }

  private static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }

  public record DecodeResult(VisualizationSettings settings, boolean valuesWereClamped) {}

  /** Minimal JSON object reader for our emitted shapes. */
  static final class JsonObject {
    private final Map<String, Object> values = new LinkedHashMap<>();
    boolean hadOutOfRangeHint;

    static JsonObject parse(String json) {
      Parser p = new Parser(json);
      Object value = p.parseValue();
      p.skipWs();
      if (!p.eof()) {
        throw new IllegalArgumentException("Trailing input");
      }
      if (!(value instanceof JsonObject obj)) {
        throw new IllegalArgumentException("Expected object");
      }
      return obj;
    }

    Iterable<String> keys() {
      return values.keySet();
    }

    String getString(String key, String defaultValue) {
      Object v = values.get(key);
      return v instanceof String s ? s : defaultValue;
    }

    int getInt(String key, int defaultValue) {
      Object v = values.get(key);
      if (v instanceof Number n) {
        return n.intValue();
      }
      return defaultValue;
    }

    double getDouble(String key, double defaultValue) {
      Object v = values.get(key);
      if (v instanceof Number n) {
        return n.doubleValue();
      }
      return defaultValue;
    }

    boolean getBoolean(String key, boolean defaultValue) {
      Object v = values.get(key);
      if (v instanceof Boolean b) {
        return b;
      }
      return defaultValue;
    }

    JsonObject getObject(String key) {
      Object v = values.get(key);
      return v instanceof JsonObject obj ? obj : null;
    }

    private static final class Parser {
      private final String s;
      private int i;

      Parser(String s) {
        this.s = s;
      }

      boolean eof() {
        return i >= s.length();
      }

      void skipWs() {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
          i++;
        }
      }

      Object parseValue() {
        skipWs();
        if (eof()) {
          throw new IllegalArgumentException("Unexpected end");
        }
        char c = s.charAt(i);
        return switch (c) {
          case '{' -> parseObject();
          case '"' -> parseString();
          case 't' -> parseLiteral("true", true);
          case 'f' -> parseLiteral("false", false);
          case 'n' -> parseLiteral("null", null);
          case '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> parseNumber();
          default -> throw new IllegalArgumentException("Unexpected '" + c + "'");
        };
      }

      JsonObject parseObject() {
        expect('{');
        JsonObject obj = new JsonObject();
        skipWs();
        if (peek('}')) {
          i++;
          return obj;
        }
        while (true) {
          skipWs();
          String key = parseString();
          skipWs();
          expect(':');
          Object value = parseValue();
          obj.values.put(key, value);
          skipWs();
          if (peek('}')) {
            i++;
            return obj;
          }
          expect(',');
        }
      }

      String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (!eof()) {
          char c = s.charAt(i++);
          if (c == '"') {
            return sb.toString();
          }
          if (c == '\\') {
            if (eof()) {
              throw new IllegalArgumentException("Bad escape");
            }
            char e = s.charAt(i++);
            sb.append(
                switch (e) {
                  case '"', '\\', '/' -> e;
                  case 'n' -> '\n';
                  case 'r' -> '\r';
                  case 't' -> '\t';
                  default -> throw new IllegalArgumentException("Bad escape");
                });
          } else {
            sb.append(c);
          }
        }
        throw new IllegalArgumentException("Unterminated string");
      }

      Number parseNumber() {
        int start = i;
        if (peek('-')) {
          i++;
        }
        while (!eof() && Character.isDigit(s.charAt(i))) {
          i++;
        }
        if (!eof() && s.charAt(i) == '.') {
          i++;
          while (!eof() && Character.isDigit(s.charAt(i))) {
            i++;
          }
        }
        if (!eof() && (s.charAt(i) == 'e' || s.charAt(i) == 'E')) {
          i++;
          if (!eof() && (s.charAt(i) == '+' || s.charAt(i) == '-')) {
            i++;
          }
          while (!eof() && Character.isDigit(s.charAt(i))) {
            i++;
          }
        }
        String num = s.substring(start, i);
        if (num.indexOf('.') >= 0 || num.indexOf('e') >= 0 || num.indexOf('E') >= 0) {
          return Double.parseDouble(num);
        }
        long l = Long.parseLong(num);
        if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
          return (int) l;
        }
        return l;
      }

      Object parseLiteral(String lit, Object value) {
        if (s.regionMatches(i, lit, 0, lit.length())) {
          i += lit.length();
          return value;
        }
        throw new IllegalArgumentException("Expected " + lit);
      }

      void expect(char c) {
        skipWs();
        if (eof() || s.charAt(i) != c) {
          throw new IllegalArgumentException("Expected '" + c + "'");
        }
        i++;
      }

      boolean peek(char c) {
        return !eof() && s.charAt(i) == c;
      }
    }
  }
}
