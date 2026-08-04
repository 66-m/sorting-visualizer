package io.github.compilerstuck.control.config.visual;

import io.github.compilerstuck.control.config.json.JsonObject;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Per-visualization JSON encode/decode, keyed by visualization id. Package-private; used by {@link
 * VisualizationSettingsCodec}.
 */
final class VisualizationSettingsCodecs {

  private VisualizationSettingsCodecs() {}

  /** Decode JSON object → settings; encode settings → JSON object body. */
  record Entry(
      Function<JsonObject, VisualizationSettings> decoder,
      BiConsumer<StringBuilder, VisualizationSettings> encoder) {}

  private static final Map<String, Entry> BY_ID;

  static {
    Map<String, Entry> m = new LinkedHashMap<>();
    m.put(
        CubeSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodeCubeSettings,
            (sb, s) -> encodeCubeSettings(sb, (CubeSettings) s)));
    m.put(
        CircleSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodeCircleSettings,
            (sb, s) -> encodeCircleSettings(sb, (CircleSettings) s)));
    m.put(
        ColorGradientGraphSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodeColorGradientGraphSettings,
            (sb, s) -> encodeColorGradientGraphSettings(sb, (ColorGradientGraphSettings) s)));
    m.put(
        CubicLinesSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodeCubicLinesSettings,
            (sb, s) -> encodeCubicLinesSettings(sb, (CubicLinesSettings) s)));
    m.put(
        DisparityChordsSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodeDisparityChordsSettings,
            (sb, s) -> encodeDisparityChordsSettings(sb, (DisparityChordsSettings) s)));
    m.put(
        DisparityCircleSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodeDisparityCircleSettings,
            (sb, s) -> encodeDisparityCircleSettings(sb, (DisparityCircleSettings) s)));
    m.put(
        DisparityCircleScatterSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodeDisparityCircleScatterSettings,
            (sb, s) ->
                encodeDisparityCircleScatterSettings(sb, (DisparityCircleScatterSettings) s)));
    m.put(
        DisparityCircleScatterLinkedSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodeDisparityCircleScatterLinkedSettings,
            (sb, s) ->
                encodeDisparityCircleScatterLinkedSettings(
                    sb, (DisparityCircleScatterLinkedSettings) s)));
    m.put(
        DisparityPlaneSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodeDisparityPlaneSettings,
            (sb, s) -> encodeDisparityPlaneSettings(sb, (DisparityPlaneSettings) s)));
    m.put(
        DisparitySphereHoopsSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodeDisparitySphereHoopsSettings,
            (sb, s) -> encodeDisparitySphereHoopsSettings(sb, (DisparitySphereHoopsSettings) s)));
    m.put(
        DisparitySquareScatterSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodeDisparitySquareScatterSettings,
            (sb, s) ->
                encodeDisparitySquareScatterSettings(sb, (DisparitySquareScatterSettings) s)));
    m.put(
        HoopsSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodeHoopsSettings,
            (sb, s) -> encodeHoopsSettings(sb, (HoopsSettings) s)));
    m.put(
        ImageVerticalSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodeImageVerticalSettings,
            (sb, s) -> encodeImageVerticalSettings(sb, (ImageVerticalSettings) s)));
    m.put(
        ImageHorizontalSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodeImageHorizontalSettings,
            (sb, s) -> encodeImageHorizontalSettings(sb, (ImageHorizontalSettings) s)));
    m.put(
        MorphingShellSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodeMorphingShellSettings,
            (sb, s) -> encodeMorphingShellSettings(sb, (MorphingShellSettings) s)));
    m.put(
        MosaicSquaresSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodeMosaicSquaresSettings,
            (sb, s) -> encodeMosaicSquaresSettings(sb, (MosaicSquaresSettings) s)));
    m.put(
        NumberPlotSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodeNumberPlotSettings,
            (sb, s) -> encodeNumberPlotSettings(sb, (NumberPlotSettings) s)));
    m.put(
        PhyllotaxisSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodePhyllotaxisSettings,
            (sb, s) -> encodePhyllotaxisSettings(sb, (PhyllotaxisSettings) s)));
    m.put(
        PlaneSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodePlaneSettings,
            (sb, s) -> encodePlaneSettings(sb, (PlaneSettings) s)));
    m.put(
        PyramidSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodePyramidSettings,
            (sb, s) -> encodePyramidSettings(sb, (PyramidSettings) s)));
    m.put(
        ScatterPlotSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodeScatterPlotSettings,
            (sb, s) -> encodeScatterPlotSettings(sb, (ScatterPlotSettings) s)));
    m.put(
        ScatterPlotLinkedSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodeScatterPlotLinkedSettings,
            (sb, s) -> encodeScatterPlotLinkedSettings(sb, (ScatterPlotLinkedSettings) s)));
    m.put(
        SphereSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodeSphereSettings,
            (sb, s) -> encodeSphereSettings(sb, (SphereSettings) s)));
    m.put(
        SphereHoopsSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodeSphereHoopsSettings,
            (sb, s) -> encodeSphereHoopsSettings(sb, (SphereHoopsSettings) s)));
    m.put(
        SphericDisparityLinesSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodeSphericDisparityLinesSettings,
            (sb, s) -> encodeSphericDisparityLinesSettings(sb, (SphericDisparityLinesSettings) s)));
    m.put(
        SwirlDotsSettings.ID,
        new Entry(
            VisualizationSettingsCodecs::decodeSwirlDotsSettings,
            (sb, s) -> encodeSwirlDotsSettings(sb, (SwirlDotsSettings) s)));
    BY_ID = Collections.unmodifiableMap(m);
  }

  static VisualizationSettings decode(String id, JsonObject obj) {
    Entry entry = BY_ID.get(id);
    return entry == null ? null : entry.decoder().apply(obj);
  }

  static void encode(StringBuilder sb, VisualizationSettings settings) {
    Entry entry = BY_ID.get(settings.visualizationId());
    if (entry == null) {
      throw new IllegalArgumentException("Unsupported settings type: " + settings.getClass());
    }
    entry.encoder().accept(sb, settings);
  }

  private static VisualizationSettings decodeCubeSettings(JsonObject obj) {
    CubeSettings defaults = CubeSettings.defaults();
    double rot = obj.getDouble("rotationSpeedRadPerSec", defaults.rotationSpeedRadPerSec());
    int opacity = (int) Math.round(obj.getDouble("fillOpacity", defaults.fillOpacity()));
    boolean wire = obj.getBoolean("wireframeEnabled", defaults.wireframeEnabled());
    double scale = obj.getDouble("sceneScaleDivisor", defaults.sceneScaleDivisor());
    if (rot != Numbers.clamp(rot, CubeSettings.ROTATION_SPEED_MIN, CubeSettings.ROTATION_SPEED_MAX)
        || opacity < CubeSettings.FILL_OPACITY_MIN
        || opacity > CubeSettings.FILL_OPACITY_MAX
        || scale
            != Numbers.clamp(
                scale,
                CubeSettings.SCENE_SCALE_DIVISOR_MIN,
                CubeSettings.SCENE_SCALE_DIVISOR_MAX)) {
      obj.hadOutOfRangeHint = true;
    }
    return new CubeSettings(rot, opacity, wire, scale);
  }

  private static VisualizationSettings decodeCircleSettings(JsonObject obj) {
    CircleSettings d = CircleSettings.defaults();
    double radiusScale = obj.getDouble("radiusScale", d.radiusScale());
    double startAngleDeg = obj.getDouble("startAngleDeg", d.startAngleDeg());
    double lineThickness = obj.getDouble("lineThickness", d.lineThickness());
    if (radiusScale
            != Numbers.clamp(
                radiusScale, CircleSettings.RADIUS_SCALE_MIN, CircleSettings.RADIUS_SCALE_MAX)
        || startAngleDeg
            != Numbers.clamp(
                startAngleDeg,
                CircleSettings.START_ANGLE_DEG_MIN,
                CircleSettings.START_ANGLE_DEG_MAX)
        || lineThickness
            != Numbers.clamp(
                lineThickness,
                CircleSettings.LINE_THICKNESS_MIN,
                CircleSettings.LINE_THICKNESS_MAX)) {
      obj.hadOutOfRangeHint = true;
    }
    return new CircleSettings(radiusScale, startAngleDeg, lineThickness);
  }

  private static VisualizationSettings decodeColorGradientGraphSettings(JsonObject obj) {
    ColorGradientGraphSettings d = ColorGradientGraphSettings.defaults();
    boolean showIndexDividers = obj.getBoolean("showIndexDividers", d.showIndexDividers());
    return new ColorGradientGraphSettings(showIndexDividers);
  }

  private static VisualizationSettings decodeCubicLinesSettings(JsonObject obj) {
    CubicLinesSettings d = CubicLinesSettings.defaults();
    double rotationSpeedRadPerSec =
        obj.getDouble("rotationSpeedRadPerSec", d.rotationSpeedRadPerSec());
    double sceneScaleDivisor = obj.getDouble("sceneScaleDivisor", d.sceneScaleDivisor());
    double markerSize = obj.getDouble("markerSize", d.markerSize());
    int lineOpacity = (int) Math.round(obj.getDouble("lineOpacity", d.lineOpacity()));
    if (rotationSpeedRadPerSec
            != Numbers.clamp(
                rotationSpeedRadPerSec,
                CubicLinesSettings.ROTATION_SPEED_RAD_PER_SEC_MIN,
                CubicLinesSettings.ROTATION_SPEED_RAD_PER_SEC_MAX)
        || sceneScaleDivisor
            != Numbers.clamp(
                sceneScaleDivisor,
                CubicLinesSettings.SCENE_SCALE_DIVISOR_MIN,
                CubicLinesSettings.SCENE_SCALE_DIVISOR_MAX)
        || markerSize
            != Numbers.clamp(
                markerSize, CubicLinesSettings.MARKER_SIZE_MIN, CubicLinesSettings.MARKER_SIZE_MAX)
        || lineOpacity < CubicLinesSettings.LINE_OPACITY_MIN
        || lineOpacity > CubicLinesSettings.LINE_OPACITY_MAX) {
      obj.hadOutOfRangeHint = true;
    }
    return new CubicLinesSettings(
        rotationSpeedRadPerSec, sceneScaleDivisor, markerSize, lineOpacity);
  }

  private static VisualizationSettings decodeDisparityChordsSettings(JsonObject obj) {
    DisparityChordsSettings d = DisparityChordsSettings.defaults();
    double radiusScale = obj.getDouble("radiusScale", d.radiusScale());
    double lineThickness = obj.getDouble("lineThickness", d.lineThickness());
    double coincidentMarkerSize = obj.getDouble("coincidentMarkerSize", d.coincidentMarkerSize());
    int chordOpacity = (int) Math.round(obj.getDouble("chordOpacity", d.chordOpacity()));
    if (radiusScale
            != Numbers.clamp(
                radiusScale,
                DisparityChordsSettings.RADIUS_SCALE_MIN,
                DisparityChordsSettings.RADIUS_SCALE_MAX)
        || lineThickness
            != Numbers.clamp(
                lineThickness,
                DisparityChordsSettings.LINE_THICKNESS_MIN,
                DisparityChordsSettings.LINE_THICKNESS_MAX)
        || coincidentMarkerSize
            != Numbers.clamp(
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

  private static VisualizationSettings decodeDisparityCircleSettings(JsonObject obj) {
    DisparityCircleSettings d = DisparityCircleSettings.defaults();
    double radiusScale = obj.getDouble("radiusScale", d.radiusScale());
    double lineThickness = obj.getDouble("lineThickness", d.lineThickness());
    double startAngleDeg = obj.getDouble("startAngleDeg", d.startAngleDeg());
    if (radiusScale
            != Numbers.clamp(
                radiusScale,
                DisparityCircleSettings.RADIUS_SCALE_MIN,
                DisparityCircleSettings.RADIUS_SCALE_MAX)
        || lineThickness
            != Numbers.clamp(
                lineThickness,
                DisparityCircleSettings.LINE_THICKNESS_MIN,
                DisparityCircleSettings.LINE_THICKNESS_MAX)
        || startAngleDeg
            != Numbers.clamp(
                startAngleDeg,
                DisparityCircleSettings.START_ANGLE_DEG_MIN,
                DisparityCircleSettings.START_ANGLE_DEG_MAX)) {
      obj.hadOutOfRangeHint = true;
    }
    return new DisparityCircleSettings(radiusScale, lineThickness, startAngleDeg);
  }

  private static VisualizationSettings decodeDisparityCircleScatterSettings(JsonObject obj) {
    DisparityCircleScatterSettings d = DisparityCircleScatterSettings.defaults();
    double pointSize = obj.getDouble("pointSize", d.pointSize());
    double radiusScale = obj.getDouble("radiusScale", d.radiusScale());
    double startAngleDeg = obj.getDouble("startAngleDeg", d.startAngleDeg());
    if (pointSize
            != Numbers.clamp(
                pointSize,
                DisparityCircleScatterSettings.POINT_SIZE_MIN,
                DisparityCircleScatterSettings.POINT_SIZE_MAX)
        || radiusScale
            != Numbers.clamp(
                radiusScale,
                DisparityCircleScatterSettings.RADIUS_SCALE_MIN,
                DisparityCircleScatterSettings.RADIUS_SCALE_MAX)
        || startAngleDeg
            != Numbers.clamp(
                startAngleDeg,
                DisparityCircleScatterSettings.START_ANGLE_DEG_MIN,
                DisparityCircleScatterSettings.START_ANGLE_DEG_MAX)) {
      obj.hadOutOfRangeHint = true;
    }
    return new DisparityCircleScatterSettings(pointSize, radiusScale, startAngleDeg);
  }

  private static VisualizationSettings decodeDisparityCircleScatterLinkedSettings(JsonObject obj) {
    DisparityCircleScatterLinkedSettings d = DisparityCircleScatterLinkedSettings.defaults();
    double lineThickness = obj.getDouble("lineThickness", d.lineThickness());
    double radiusScale = obj.getDouble("radiusScale", d.radiusScale());
    if (lineThickness
            != Numbers.clamp(
                lineThickness,
                DisparityCircleScatterLinkedSettings.LINE_THICKNESS_MIN,
                DisparityCircleScatterLinkedSettings.LINE_THICKNESS_MAX)
        || radiusScale
            != Numbers.clamp(
                radiusScale,
                DisparityCircleScatterLinkedSettings.RADIUS_SCALE_MIN,
                DisparityCircleScatterLinkedSettings.RADIUS_SCALE_MAX)) {
      obj.hadOutOfRangeHint = true;
    }
    return new DisparityCircleScatterLinkedSettings(lineThickness, radiusScale);
  }

  private static VisualizationSettings decodeDisparityPlaneSettings(JsonObject obj) {
    DisparityPlaneSettings d = DisparityPlaneSettings.defaults();
    double rotationSpeedRadPerSec =
        obj.getDouble("rotationSpeedRadPerSec", d.rotationSpeedRadPerSec());
    double maxExtrusionFraction = obj.getDouble("maxExtrusionFraction", d.maxExtrusionFraction());
    double planeScale = obj.getDouble("planeScale", d.planeScale());
    double tileGap = obj.getDouble("tileGap", d.tileGap());
    if (rotationSpeedRadPerSec
            != Numbers.clamp(
                rotationSpeedRadPerSec,
                DisparityPlaneSettings.ROTATION_SPEED_RAD_PER_SEC_MIN,
                DisparityPlaneSettings.ROTATION_SPEED_RAD_PER_SEC_MAX)
        || maxExtrusionFraction
            != Numbers.clamp(
                maxExtrusionFraction,
                DisparityPlaneSettings.MAX_EXTRUSION_FRACTION_MIN,
                DisparityPlaneSettings.MAX_EXTRUSION_FRACTION_MAX)
        || planeScale
            != Numbers.clamp(
                planeScale,
                DisparityPlaneSettings.PLANE_SCALE_MIN,
                DisparityPlaneSettings.PLANE_SCALE_MAX)
        || tileGap
            != Numbers.clamp(
                tileGap,
                DisparityPlaneSettings.TILE_GAP_MIN,
                DisparityPlaneSettings.TILE_GAP_MAX)) {
      obj.hadOutOfRangeHint = true;
    }
    return new DisparityPlaneSettings(
        rotationSpeedRadPerSec, maxExtrusionFraction, planeScale, tileGap);
  }

  private static VisualizationSettings decodeDisparitySphereHoopsSettings(JsonObject obj) {
    DisparitySphereHoopsSettings d = DisparitySphereHoopsSettings.defaults();
    double globeScale = obj.getDouble("globeScale", d.globeScale());
    if (globeScale
        != Numbers.clamp(
            globeScale,
            DisparitySphereHoopsSettings.GLOBE_SCALE_MIN,
            DisparitySphereHoopsSettings.GLOBE_SCALE_MAX)) {
      obj.hadOutOfRangeHint = true;
    }
    return new DisparitySphereHoopsSettings(globeScale);
  }

  private static VisualizationSettings decodeDisparitySquareScatterSettings(JsonObject obj) {
    DisparitySquareScatterSettings d = DisparitySquareScatterSettings.defaults();
    double pointSize = obj.getDouble("pointSize", d.pointSize());
    double perimeterScale = obj.getDouble("perimeterScale", d.perimeterScale());
    if (pointSize
            != Numbers.clamp(
                pointSize,
                DisparitySquareScatterSettings.POINT_SIZE_MIN,
                DisparitySquareScatterSettings.POINT_SIZE_MAX)
        || perimeterScale
            != Numbers.clamp(
                perimeterScale,
                DisparitySquareScatterSettings.PERIMETER_SCALE_MIN,
                DisparitySquareScatterSettings.PERIMETER_SCALE_MAX)) {
      obj.hadOutOfRangeHint = true;
    }
    return new DisparitySquareScatterSettings(pointSize, perimeterScale);
  }

  private static VisualizationSettings decodeHoopsSettings(JsonObject obj) {
    HoopsSettings d = HoopsSettings.defaults();
    double radiusScale = obj.getDouble("radiusScale", d.radiusScale());
    if (radiusScale
        != Numbers.clamp(
            radiusScale, HoopsSettings.RADIUS_SCALE_MIN, HoopsSettings.RADIUS_SCALE_MAX)) {
      obj.hadOutOfRangeHint = true;
    }
    return new HoopsSettings(radiusScale);
  }

  private static VisualizationSettings decodeImageVerticalSettings(JsonObject obj) {
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
        != Numbers.clamp(
            highlightStrength,
            ImageVerticalSettings.HIGHLIGHT_STRENGTH_MIN,
            ImageVerticalSettings.HIGHLIGHT_STRENGTH_MAX)) {
      obj.hadOutOfRangeHint = true;
    }
    return new ImageVerticalSettings(fitMode, highlightStrength);
  }

  private static VisualizationSettings decodeImageHorizontalSettings(JsonObject obj) {
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
        != Numbers.clamp(
            highlightStrength,
            ImageHorizontalSettings.HIGHLIGHT_STRENGTH_MIN,
            ImageHorizontalSettings.HIGHLIGHT_STRENGTH_MAX)) {
      obj.hadOutOfRangeHint = true;
    }
    return new ImageHorizontalSettings(fitMode, highlightStrength);
  }

  private static VisualizationSettings decodeMorphingShellSettings(JsonObject obj) {
    MorphingShellSettings d = MorphingShellSettings.defaults();
    double rotationSpeedRadPerSec =
        obj.getDouble("rotationSpeedRadPerSec", d.rotationSpeedRadPerSec());
    double sphereSize = obj.getDouble("sphereSize", d.sphereSize());
    double shellRadiusScale = obj.getDouble("shellRadiusScale", d.shellRadiusScale());
    if (rotationSpeedRadPerSec
            != Numbers.clamp(
                rotationSpeedRadPerSec,
                MorphingShellSettings.ROTATION_SPEED_RAD_PER_SEC_MIN,
                MorphingShellSettings.ROTATION_SPEED_RAD_PER_SEC_MAX)
        || sphereSize
            != Numbers.clamp(
                sphereSize,
                MorphingShellSettings.SPHERE_SIZE_MIN,
                MorphingShellSettings.SPHERE_SIZE_MAX)
        || shellRadiusScale
            != Numbers.clamp(
                shellRadiusScale,
                MorphingShellSettings.SHELL_RADIUS_SCALE_MIN,
                MorphingShellSettings.SHELL_RADIUS_SCALE_MAX)) {
      obj.hadOutOfRangeHint = true;
    }
    return new MorphingShellSettings(rotationSpeedRadPerSec, sphereSize, shellRadiusScale);
  }

  private static VisualizationSettings decodeMosaicSquaresSettings(JsonObject obj) {
    MosaicSquaresSettings d = MosaicSquaresSettings.defaults();
    double tileGapPx = obj.getDouble("tileGapPx", d.tileGapPx());
    if (tileGapPx
        != Numbers.clamp(
            tileGapPx,
            MosaicSquaresSettings.TILE_GAP_PX_MIN,
            MosaicSquaresSettings.TILE_GAP_PX_MAX)) {
      obj.hadOutOfRangeHint = true;
    }
    return new MosaicSquaresSettings(tileGapPx);
  }

  private static VisualizationSettings decodeNumberPlotSettings(JsonObject obj) {
    NumberPlotSettings d = NumberPlotSettings.defaults();
    double fontSize = obj.getDouble("fontSize", d.fontSize());
    if (fontSize
        != Numbers.clamp(
            fontSize, NumberPlotSettings.FONT_SIZE_MIN, NumberPlotSettings.FONT_SIZE_MAX)) {
      obj.hadOutOfRangeHint = true;
    }
    return new NumberPlotSettings(fontSize);
  }

  private static VisualizationSettings decodePhyllotaxisSettings(JsonObject obj) {
    PhyllotaxisSettings d = PhyllotaxisSettings.defaults();
    double angleStepDeg = obj.getDouble("angleStepDeg", d.angleStepDeg());
    double scaleDivisor = obj.getDouble("scaleDivisor", d.scaleDivisor());
    double pointSize = obj.getDouble("pointSize", d.pointSize());
    if (angleStepDeg
            != Numbers.clamp(
                angleStepDeg,
                PhyllotaxisSettings.ANGLE_STEP_DEG_MIN,
                PhyllotaxisSettings.ANGLE_STEP_DEG_MAX)
        || scaleDivisor
            != Numbers.clamp(
                scaleDivisor,
                PhyllotaxisSettings.SCALE_DIVISOR_MIN,
                PhyllotaxisSettings.SCALE_DIVISOR_MAX)
        || pointSize
            != Numbers.clamp(
                pointSize,
                PhyllotaxisSettings.POINT_SIZE_MIN,
                PhyllotaxisSettings.POINT_SIZE_MAX)) {
      obj.hadOutOfRangeHint = true;
    }
    return new PhyllotaxisSettings(angleStepDeg, scaleDivisor, pointSize);
  }

  private static VisualizationSettings decodePlaneSettings(JsonObject obj) {
    PlaneSettings d = PlaneSettings.defaults();
    double rotationSpeedRadPerSec =
        obj.getDouble("rotationSpeedRadPerSec", d.rotationSpeedRadPerSec());
    double planeScale = obj.getDouble("planeScale", d.planeScale());
    double tileGap = obj.getDouble("tileGap", d.tileGap());
    if (rotationSpeedRadPerSec
            != Numbers.clamp(
                rotationSpeedRadPerSec,
                PlaneSettings.ROTATION_SPEED_RAD_PER_SEC_MIN,
                PlaneSettings.ROTATION_SPEED_RAD_PER_SEC_MAX)
        || planeScale
            != Numbers.clamp(
                planeScale, PlaneSettings.PLANE_SCALE_MIN, PlaneSettings.PLANE_SCALE_MAX)
        || tileGap
            != Numbers.clamp(tileGap, PlaneSettings.TILE_GAP_MIN, PlaneSettings.TILE_GAP_MAX)) {
      obj.hadOutOfRangeHint = true;
    }
    return new PlaneSettings(rotationSpeedRadPerSec, planeScale, tileGap);
  }

  private static VisualizationSettings decodePyramidSettings(JsonObject obj) {
    PyramidSettings d = PyramidSettings.defaults();
    double rotationSpeedRadPerSec =
        obj.getDouble("rotationSpeedRadPerSec", d.rotationSpeedRadPerSec());
    double stackScale = obj.getDouble("stackScale", d.stackScale());
    if (rotationSpeedRadPerSec
            != Numbers.clamp(
                rotationSpeedRadPerSec,
                PyramidSettings.ROTATION_SPEED_RAD_PER_SEC_MIN,
                PyramidSettings.ROTATION_SPEED_RAD_PER_SEC_MAX)
        || stackScale
            != Numbers.clamp(
                stackScale, PyramidSettings.STACK_SCALE_MIN, PyramidSettings.STACK_SCALE_MAX)) {
      obj.hadOutOfRangeHint = true;
    }
    return new PyramidSettings(rotationSpeedRadPerSec, stackScale);
  }

  private static VisualizationSettings decodeScatterPlotSettings(JsonObject obj) {
    ScatterPlotSettings d = ScatterPlotSettings.defaults();
    double pointSize = obj.getDouble("pointSize", d.pointSize());
    if (pointSize
        != Numbers.clamp(
            pointSize, ScatterPlotSettings.POINT_SIZE_MIN, ScatterPlotSettings.POINT_SIZE_MAX)) {
      obj.hadOutOfRangeHint = true;
    }
    return new ScatterPlotSettings(pointSize);
  }

  private static VisualizationSettings decodeScatterPlotLinkedSettings(JsonObject obj) {
    ScatterPlotLinkedSettings d = ScatterPlotLinkedSettings.defaults();
    double lineThickness = obj.getDouble("lineThickness", d.lineThickness());
    if (lineThickness
        != Numbers.clamp(
            lineThickness,
            ScatterPlotLinkedSettings.LINE_THICKNESS_MIN,
            ScatterPlotLinkedSettings.LINE_THICKNESS_MAX)) {
      obj.hadOutOfRangeHint = true;
    }
    return new ScatterPlotLinkedSettings(lineThickness);
  }

  private static VisualizationSettings decodeSphereSettings(JsonObject obj) {
    SphereSettings d = SphereSettings.defaults();
    double rotationSpeedRadPerSec =
        obj.getDouble("rotationSpeedRadPerSec", d.rotationSpeedRadPerSec());
    double globeScale = obj.getDouble("globeScale", d.globeScale());
    double pointSize = obj.getDouble("pointSize", d.pointSize());
    if (rotationSpeedRadPerSec
            != Numbers.clamp(
                rotationSpeedRadPerSec,
                SphereSettings.ROTATION_SPEED_RAD_PER_SEC_MIN,
                SphereSettings.ROTATION_SPEED_RAD_PER_SEC_MAX)
        || globeScale
            != Numbers.clamp(
                globeScale, SphereSettings.GLOBE_SCALE_MIN, SphereSettings.GLOBE_SCALE_MAX)
        || pointSize
            != Numbers.clamp(
                pointSize, SphereSettings.POINT_SIZE_MIN, SphereSettings.POINT_SIZE_MAX)) {
      obj.hadOutOfRangeHint = true;
    }
    return new SphereSettings(rotationSpeedRadPerSec, globeScale, pointSize);
  }

  private static VisualizationSettings decodeSphereHoopsSettings(JsonObject obj) {
    SphereHoopsSettings d = SphereHoopsSettings.defaults();
    double globeScale = obj.getDouble("globeScale", d.globeScale());
    if (globeScale
        != Numbers.clamp(
            globeScale, SphereHoopsSettings.GLOBE_SCALE_MIN, SphereHoopsSettings.GLOBE_SCALE_MAX)) {
      obj.hadOutOfRangeHint = true;
    }
    return new SphereHoopsSettings(globeScale);
  }

  private static VisualizationSettings decodeSphericDisparityLinesSettings(JsonObject obj) {
    SphericDisparityLinesSettings d = SphericDisparityLinesSettings.defaults();
    double rotationSpeedRadPerSec =
        obj.getDouble("rotationSpeedRadPerSec", d.rotationSpeedRadPerSec());
    double globeScale = obj.getDouble("globeScale", d.globeScale());
    int lineOpacity = (int) Math.round(obj.getDouble("lineOpacity", d.lineOpacity()));
    double markerSize = obj.getDouble("markerSize", d.markerSize());
    if (rotationSpeedRadPerSec
            != Numbers.clamp(
                rotationSpeedRadPerSec,
                SphericDisparityLinesSettings.ROTATION_SPEED_RAD_PER_SEC_MIN,
                SphericDisparityLinesSettings.ROTATION_SPEED_RAD_PER_SEC_MAX)
        || globeScale
            != Numbers.clamp(
                globeScale,
                SphericDisparityLinesSettings.GLOBE_SCALE_MIN,
                SphericDisparityLinesSettings.GLOBE_SCALE_MAX)
        || lineOpacity < SphericDisparityLinesSettings.LINE_OPACITY_MIN
        || lineOpacity > SphericDisparityLinesSettings.LINE_OPACITY_MAX
        || markerSize
            != Numbers.clamp(
                markerSize,
                SphericDisparityLinesSettings.MARKER_SIZE_MIN,
                SphericDisparityLinesSettings.MARKER_SIZE_MAX)) {
      obj.hadOutOfRangeHint = true;
    }
    return new SphericDisparityLinesSettings(
        rotationSpeedRadPerSec, globeScale, lineOpacity, markerSize);
  }

  private static VisualizationSettings decodeSwirlDotsSettings(JsonObject obj) {
    SwirlDotsSettings d = SwirlDotsSettings.defaults();
    double spiralTurns = obj.getDouble("spiralTurns", d.spiralTurns());
    double radiusScale = obj.getDouble("radiusScale", d.radiusScale());
    double pointSize = obj.getDouble("pointSize", d.pointSize());
    if (spiralTurns
            != Numbers.clamp(
                spiralTurns, SwirlDotsSettings.SPIRAL_TURNS_MIN, SwirlDotsSettings.SPIRAL_TURNS_MAX)
        || radiusScale
            != Numbers.clamp(
                radiusScale, SwirlDotsSettings.RADIUS_SCALE_MIN, SwirlDotsSettings.RADIUS_SCALE_MAX)
        || pointSize
            != Numbers.clamp(
                pointSize, SwirlDotsSettings.POINT_SIZE_MIN, SwirlDotsSettings.POINT_SIZE_MAX)) {
      obj.hadOutOfRangeHint = true;
    }
    return new SwirlDotsSettings(spiralTurns, radiusScale, pointSize);
  }

  private static void encodeCubeSettings(StringBuilder sb, CubeSettings cube) {
    sb.append('{');
    appendNumber(sb, "rotationSpeedRadPerSec", cube.rotationSpeedRadPerSec());
    sb.append(',');
    sb.append(quote("fillOpacity")).append(':').append(cube.fillOpacity());
    sb.append(',');
    sb.append(quote("wireframeEnabled")).append(':').append(cube.wireframeEnabled());
    sb.append(',');
    appendNumber(sb, "sceneScaleDivisor", cube.sceneScaleDivisor());
    sb.append('}');
  }

  private static void encodeCircleSettings(StringBuilder sb, CircleSettings s) {
    sb.append('{');
    appendNumber(sb, "radiusScale", s.radiusScale());
    sb.append(',');
    appendNumber(sb, "startAngleDeg", s.startAngleDeg());
    sb.append(',');
    appendNumber(sb, "lineThickness", s.lineThickness());
    sb.append('}');
  }

  private static void encodeColorGradientGraphSettings(
      StringBuilder sb, ColorGradientGraphSettings s) {
    sb.append('{');
    sb.append(quote("showIndexDividers")).append(':').append(s.showIndexDividers());
    sb.append('}');
  }

  private static void encodeCubicLinesSettings(StringBuilder sb, CubicLinesSettings s) {
    sb.append('{');
    appendNumber(sb, "rotationSpeedRadPerSec", s.rotationSpeedRadPerSec());
    sb.append(',');
    appendNumber(sb, "sceneScaleDivisor", s.sceneScaleDivisor());
    sb.append(',');
    appendNumber(sb, "markerSize", s.markerSize());
    sb.append(',');
    sb.append(quote("lineOpacity")).append(':').append(s.lineOpacity());
    sb.append('}');
  }

  private static void encodeDisparityChordsSettings(StringBuilder sb, DisparityChordsSettings s) {
    sb.append('{');
    appendNumber(sb, "radiusScale", s.radiusScale());
    sb.append(',');
    appendNumber(sb, "lineThickness", s.lineThickness());
    sb.append(',');
    appendNumber(sb, "coincidentMarkerSize", s.coincidentMarkerSize());
    sb.append(',');
    sb.append(quote("chordOpacity")).append(':').append(s.chordOpacity());
    sb.append('}');
  }

  private static void encodeDisparityCircleSettings(StringBuilder sb, DisparityCircleSettings s) {
    sb.append('{');
    appendNumber(sb, "radiusScale", s.radiusScale());
    sb.append(',');
    appendNumber(sb, "lineThickness", s.lineThickness());
    sb.append(',');
    appendNumber(sb, "startAngleDeg", s.startAngleDeg());
    sb.append('}');
  }

  private static void encodeDisparityCircleScatterSettings(
      StringBuilder sb, DisparityCircleScatterSettings s) {
    sb.append('{');
    appendNumber(sb, "pointSize", s.pointSize());
    sb.append(',');
    appendNumber(sb, "radiusScale", s.radiusScale());
    sb.append(',');
    appendNumber(sb, "startAngleDeg", s.startAngleDeg());
    sb.append('}');
  }

  private static void encodeDisparityCircleScatterLinkedSettings(
      StringBuilder sb, DisparityCircleScatterLinkedSettings s) {
    sb.append('{');
    appendNumber(sb, "lineThickness", s.lineThickness());
    sb.append(',');
    appendNumber(sb, "radiusScale", s.radiusScale());
    sb.append('}');
  }

  private static void encodeDisparityPlaneSettings(StringBuilder sb, DisparityPlaneSettings s) {
    sb.append('{');
    appendNumber(sb, "rotationSpeedRadPerSec", s.rotationSpeedRadPerSec());
    sb.append(',');
    appendNumber(sb, "maxExtrusionFraction", s.maxExtrusionFraction());
    sb.append(',');
    appendNumber(sb, "planeScale", s.planeScale());
    sb.append(',');
    appendNumber(sb, "tileGap", s.tileGap());
    sb.append('}');
  }

  private static void encodeDisparitySphereHoopsSettings(
      StringBuilder sb, DisparitySphereHoopsSettings s) {
    sb.append('{');
    appendNumber(sb, "globeScale", s.globeScale());
    sb.append('}');
  }

  private static void encodeDisparitySquareScatterSettings(
      StringBuilder sb, DisparitySquareScatterSettings s) {
    sb.append('{');
    appendNumber(sb, "pointSize", s.pointSize());
    sb.append(',');
    appendNumber(sb, "perimeterScale", s.perimeterScale());
    sb.append('}');
  }

  private static void encodeHoopsSettings(StringBuilder sb, HoopsSettings s) {
    sb.append('{');
    appendNumber(sb, "radiusScale", s.radiusScale());
    sb.append('}');
  }

  private static void encodeImageVerticalSettings(StringBuilder sb, ImageVerticalSettings s) {
    sb.append('{');
    sb.append(quote("fitMode")).append(':').append(quote(s.fitMode().name()));
    sb.append(',');
    appendNumber(sb, "highlightStrength", s.highlightStrength());
    sb.append('}');
  }

  private static void encodeImageHorizontalSettings(StringBuilder sb, ImageHorizontalSettings s) {
    sb.append('{');
    sb.append(quote("fitMode")).append(':').append(quote(s.fitMode().name()));
    sb.append(',');
    appendNumber(sb, "highlightStrength", s.highlightStrength());
    sb.append('}');
  }

  private static void encodeMorphingShellSettings(StringBuilder sb, MorphingShellSettings s) {
    sb.append('{');
    appendNumber(sb, "rotationSpeedRadPerSec", s.rotationSpeedRadPerSec());
    sb.append(',');
    appendNumber(sb, "sphereSize", s.sphereSize());
    sb.append(',');
    appendNumber(sb, "shellRadiusScale", s.shellRadiusScale());
    sb.append('}');
  }

  private static void encodeMosaicSquaresSettings(StringBuilder sb, MosaicSquaresSettings s) {
    sb.append('{');
    appendNumber(sb, "tileGapPx", s.tileGapPx());
    sb.append('}');
  }

  private static void encodeNumberPlotSettings(StringBuilder sb, NumberPlotSettings s) {
    sb.append('{');
    appendNumber(sb, "fontSize", s.fontSize());
    sb.append('}');
  }

  private static void encodePhyllotaxisSettings(StringBuilder sb, PhyllotaxisSettings s) {
    sb.append('{');
    appendNumber(sb, "angleStepDeg", s.angleStepDeg());
    sb.append(',');
    appendNumber(sb, "scaleDivisor", s.scaleDivisor());
    sb.append(',');
    appendNumber(sb, "pointSize", s.pointSize());
    sb.append('}');
  }

  private static void encodePlaneSettings(StringBuilder sb, PlaneSettings s) {
    sb.append('{');
    appendNumber(sb, "rotationSpeedRadPerSec", s.rotationSpeedRadPerSec());
    sb.append(',');
    appendNumber(sb, "planeScale", s.planeScale());
    sb.append(',');
    appendNumber(sb, "tileGap", s.tileGap());
    sb.append('}');
  }

  private static void encodePyramidSettings(StringBuilder sb, PyramidSettings s) {
    sb.append('{');
    appendNumber(sb, "rotationSpeedRadPerSec", s.rotationSpeedRadPerSec());
    sb.append(',');
    appendNumber(sb, "stackScale", s.stackScale());
    sb.append('}');
  }

  private static void encodeScatterPlotSettings(StringBuilder sb, ScatterPlotSettings s) {
    sb.append('{');
    appendNumber(sb, "pointSize", s.pointSize());
    sb.append('}');
  }

  private static void encodeScatterPlotLinkedSettings(
      StringBuilder sb, ScatterPlotLinkedSettings s) {
    sb.append('{');
    appendNumber(sb, "lineThickness", s.lineThickness());
    sb.append('}');
  }

  private static void encodeSphereSettings(StringBuilder sb, SphereSettings s) {
    sb.append('{');
    appendNumber(sb, "rotationSpeedRadPerSec", s.rotationSpeedRadPerSec());
    sb.append(',');
    appendNumber(sb, "globeScale", s.globeScale());
    sb.append(',');
    appendNumber(sb, "pointSize", s.pointSize());
    sb.append('}');
  }

  private static void encodeSphereHoopsSettings(StringBuilder sb, SphereHoopsSettings s) {
    sb.append('{');
    appendNumber(sb, "globeScale", s.globeScale());
    sb.append('}');
  }

  private static void encodeSphericDisparityLinesSettings(
      StringBuilder sb, SphericDisparityLinesSettings s) {
    sb.append('{');
    appendNumber(sb, "rotationSpeedRadPerSec", s.rotationSpeedRadPerSec());
    sb.append(',');
    appendNumber(sb, "globeScale", s.globeScale());
    sb.append(',');
    sb.append(quote("lineOpacity")).append(':').append(s.lineOpacity());
    sb.append(',');
    appendNumber(sb, "markerSize", s.markerSize());
    sb.append('}');
  }

  private static void encodeSwirlDotsSettings(StringBuilder sb, SwirlDotsSettings s) {
    sb.append('{');
    appendNumber(sb, "spiralTurns", s.spiralTurns());
    sb.append(',');
    appendNumber(sb, "radiusScale", s.radiusScale());
    sb.append(',');
    appendNumber(sb, "pointSize", s.pointSize());
    sb.append('}');
  }

  static void appendNumber(StringBuilder sb, String key, double value) {
    sb.append(quote(key)).append(':');
    if (Double.isFinite(value)) {
      // Full precision; Locale.ROOT avoids comma decimals.
      sb.append(Double.toString(value));
    } else {
      sb.append('0');
    }
  }

  static String quote(String s) {
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
}
