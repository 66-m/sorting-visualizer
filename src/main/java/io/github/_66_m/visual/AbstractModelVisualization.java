package io.github._66_m.visual;

import io.github._66_m.control.config.MediaKind;
import io.github._66_m.control.config.visual.ModelOptions;
import io.github._66_m.control.model.ArrayModel;
import io.github._66_m.control.render.CoordinateSpace;
import io.github._66_m.control.render.RenderSystem;
import io.github._66_m.control.render.mesh.ModelSource;
import io.github._66_m.control.render.mesh.PieceFrame;
import io.github._66_m.sound.Sound;
import io.github._66_m.visual.gradient.ColorGradient;

/**
 * Shared plumbing for the 3D model visualizations: model loading ({@link ModelSource}, rocket until
 * an OBJ is picked), spin / tilt / scale into world space, disparity and element↔piece mapping
 * helpers, and the loading / error caption.
 */
abstract class AbstractModelVisualization extends Visualization
    implements ConfigurableVisualization, MediaSourceVisualization {

  protected final ModelSource source = new ModelSource();

  /** Accumulated spin around the model's vertical axis (radians). */
  protected float spin;

  // Per-frame world transform (see beginTransform): world = base · model + (0, 0, centerZ).
  private float cosTilt;
  private float sinTilt;
  private float worldScale;
  private float centerZ;

  private final float[] base = new float[9];

  /** Tilt of the spin axis toward the viewer's right (radians), e.g. Earth's 23.4°. */
  protected float axialTilt;

  /** Sounds play only on frames where the array changed, not every redraw of the spin. */
  private long soundRevision = Long.MIN_VALUE;

  private boolean soundThisFrame;

  AbstractModelVisualization(
      ArrayModel arrayModel, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayModel, colorGradient, sound, rs);
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  @Override
  public MediaKind mediaKind() {
    return MediaKind.MODEL;
  }

  @Override
  public String mediaPath() {
    return source.path();
  }

  @Override
  public boolean loadMedia(String path) {
    if (path == null || path.isBlank() || !MediaKind.MODEL.accepts(path)) {
      return false;
    }
    source.load(path, upAxis() == ModelOptions.UpAxis.Z_UP);
    return true;
  }

  /** Up axis from the concrete settings record. */
  protected abstract ModelOptions.UpAxis upAxis();

  /** Call from {@code applySettings} so a changed up axis re-normalizes the loaded model. */
  protected void syncUpAxis() {
    source.setZUp(upAxis() == ModelOptions.UpAxis.Z_UP);
  }

  /**
   * Prepares the model→world transform for this frame: spin (advanced by {@code speed·delta}), tilt
   * (degrees; 0 = side view, 90 = top view) and scale so the unit-radius model spans {@code
   * sceneScale} of the smaller screen edge.
   */
  protected void beginTransform(float delta, double speed, double tiltDeg, double sceneScale) {
    long rev = arrayModel.getVisualRevision();
    soundThisFrame = rev != soundRevision;
    soundRevision = rev;
    spin += (float) (speed * delta);
    float cosSpin = (float) Math.cos(spin);
    float sinSpin = (float) Math.sin(spin);
    double tilt = Math.toRadians(tiltDeg);
    cosTilt = (float) Math.cos(tilt);
    sinTilt = (float) Math.sin(tilt);
    int screenMin = Math.min(screenWidth, screenHeight);
    worldScale = (float) (sceneScale * screenMin / 2.0);
    centerZ = -(screenMin / 10f);
    // base = scale · Rx(tilt) · Rz(axialTilt) · Ry(spin)
    float[] ry = {cosSpin, 0f, sinSpin, 0f, 1f, 0f, -sinSpin, 0f, cosSpin};
    float ca = (float) Math.cos(axialTilt);
    float sa = (float) Math.sin(axialTilt);
    float[] rz = {ca, -sa, 0f, sa, ca, 0f, 0f, 0f, 1f};
    float[] rx = {1f, 0f, 0f, 0f, cosTilt, -sinTilt, 0f, sinTilt, cosTilt};
    float[] tmp = new float[9];
    mul3(rz, ry, tmp);
    mul3(rx, tmp, base);
    for (int i = 0; i < 9; i++) {
      base[i] *= worldScale;
    }
  }

  /** {@code out = a · b} for row-major 3×3 matrices. */
  static void mul3(float[] a, float[] b, float[] out) {
    for (int r = 0; r < 3; r++) {
      for (int c = 0; c < 3; c++) {
        out[r * 3 + c] = a[r * 3] * b[c] + a[r * 3 + 1] * b[3 + c] + a[r * 3 + 2] * b[6 + c];
      }
    }
  }

  /** World units per model unit for the current frame. */
  protected float worldScale() {
    return worldScale;
  }

  /** Model point → world (into {@code out[o..o+2]}). */
  protected void toWorld(float x, float y, float z, float[] out, int o) {
    out[o] = base[0] * x + base[1] * y + base[2] * z;
    out[o + 1] = base[3] * x + base[4] * y + base[5] * z;
    out[o + 2] = base[6] * x + base[7] * y + base[8] * z + centerZ;
  }

  private final float[] combined = new float[9];

  /**
   * Writes piece {@code p}'s transform into {@code frame}: the piece is rotated by {@code rot}
   * (row-major 3×3, {@code null} = none) around {@code pivot}, moved by {@code offset} (model
   * units), then placed with this frame's spin / tilt / scale ({@link #toWorld}).
   */
  protected void setPieceTransform(
      PieceFrame frame,
      int p,
      float[] rot,
      float px,
      float py,
      float pz,
      float ox,
      float oy,
      float oz) {
    float lx;
    float ly;
    float lz;
    if (rot == null) {
      System.arraycopy(base, 0, combined, 0, 9);
      lx = ox;
      ly = oy;
      lz = oz;
    } else {
      for (int r = 0; r < 3; r++) {
        for (int c = 0; c < 3; c++) {
          combined[r * 3 + c] =
              base[r * 3] * rot[c] + base[r * 3 + 1] * rot[3 + c] + base[r * 3 + 2] * rot[6 + c];
        }
      }
      // Local translation so the rotation pivots around (px, py, pz): pivot + offset − R·pivot.
      lx = px + ox - (rot[0] * px + rot[1] * py + rot[2] * pz);
      ly = py + oy - (rot[3] * px + rot[4] * py + rot[5] * pz);
      lz = pz + oz - (rot[6] * px + rot[7] * py + rot[8] * pz);
    }
    float tx = base[0] * lx + base[1] * ly + base[2] * lz;
    float ty = base[3] * lx + base[4] * ly + base[5] * lz;
    float tz = base[6] * lx + base[7] * ly + base[8] * lz + centerZ;
    frame.setTransform(p, combined, tx, ty, tz);
  }

  /** Rodrigues rotation matrix (row-major) for unit axis {@code (ax, ay, az)} and angle. */
  static void axisAngle(float ax, float ay, float az, float angle, float[] out) {
    float c = (float) Math.cos(angle);
    float s = (float) Math.sin(angle);
    float t = 1f - c;
    out[0] = t * ax * ax + c;
    out[1] = t * ax * ay - s * az;
    out[2] = t * ax * az + s * ay;
    out[3] = t * ax * ay + s * az;
    out[4] = t * ay * ay + c;
    out[5] = t * ay * az - s * ax;
    out[6] = t * ax * az - s * ay;
    out[7] = t * ay * az + s * ax;
    out[8] = t * az * az + c;
  }

  /** Euler angles (radians, Y-up world) matching {@link #toWorld}'s rotation. */
  protected float tiltRad() {
    return (float) Math.atan2(sinTilt, cosTilt);
  }

  /** Normalized circular disparity of element {@code i} holding {@code value}: 0 … 1. */
  static float disparity(int i, int value, int length) {
    if (length <= 1) {
      return 0f;
    }
    float d = VisMath.circularDistance(i, value, length) / (length / 2f);
    return Math.max(0f, Math.min(1f, d));
  }

  /**
   * Element index for piece / slot {@code k} of {@code pieces} when the array has {@code length}
   * elements (several pieces per element or several elements per piece).
   */
  static int elementForPiece(int k, int pieces, int length) {
    if (pieces <= 0 || length <= 0) {
      return 0;
    }
    return (int) Math.min(length - 1, (long) k * length / pieces);
  }

  /**
   * Source piece shown in slot {@code k}: the piece at the same offset inside the value's block.
   * Equals {@code value} when pieces == length.
   */
  static int sourcePiece(int k, int value, int pieces, int length) {
    int e = elementForPiece(k, pieces, length);
    long firstOfElement = ((long) e * pieces + length - 1) / length;
    long firstOfValue = ((long) value * pieces + length - 1) / length;
    long s = firstOfValue + (k - firstOfElement);
    return (int) Math.max(0, Math.min(pieces - 1, s));
  }

  /**
   * 3D scatter: point {@code i}'s home {@code pos[i]} with its coordinate along the assembly axis
   * taken from {@code pos[v]} (height, radius, angle, or height + angle for spiral).
   */
  static void scatterPoint(
      float[] pos, int i, int v, ModelOptions.AssemblyOrder order, float[] out) {
    float x = pos[i * 3];
    float y = pos[i * 3 + 1];
    float z = pos[i * 3 + 2];
    float vx = pos[v * 3];
    float vy = pos[v * 3 + 1];
    float vz = pos[v * 3 + 2];
    switch (order) {
      case HEIGHT -> y = vy;
      case RADIAL -> {
        float ri = (float) Math.sqrt(x * x + y * y + z * z);
        float rv = (float) Math.sqrt(vx * vx + vy * vy + vz * vz);
        float scale = ri > 1e-6f ? rv / ri : 0f;
        x *= scale;
        y *= scale;
        z *= scale;
      }
      case ANGLE, SPIRAL -> {
        float rxz = (float) Math.sqrt(x * x + z * z);
        double theta = Math.atan2(vz, vx);
        x = rxz * (float) Math.cos(theta);
        z = rxz * (float) Math.sin(theta);
        if (order == ModelOptions.AssemblyOrder.SPIRAL) {
          y = vy;
        }
      }
    }
    out[0] = x;
    out[1] = y;
    out[2] = z;
  }

  /** Plays SET markers and returns the marker-aware gradient color for element {@code i}. */
  protected int elementColor(int i, int value) {
    Marker marker = arrayModel.getMarker(i);
    if (marker == Marker.SET && soundThisFrame) {
      sound.playSound(i);
    }
    return colorGradient.getMarkerArgb(value, marker);
  }

  /** Model color unless the element is highlighted (then the marker color). */
  protected int modelOrMarker(int i, int value, int modelArgb) {
    Marker marker = arrayModel.getMarker(i);
    if (marker == Marker.SET) {
      if (soundThisFrame) {
        sound.playSound(i);
      }
      return colorGradient.getMarkerArgb(value, marker);
    }
    return modelArgb;
  }

  /** Result of {@link #pieceStats}: mean disparity, representative value, any SET marker. */
  protected float pieceDisparity;

  protected int pieceValue;
  protected boolean pieceHighlight;

  /**
   * Summarizes the elements covered by piece {@code p} of {@code pieces} (one element, or a block
   * of them when the array is larger): mean disparity, the first element's value, and whether any
   * of them is marked SET (whose sounds play on frames where the array changed).
   */
  protected void pieceStats(int p, int pieces, int length) {
    int first = elementForPiece(p, pieces, length);
    int end =
        p + 1 >= pieces ? length : Math.max(first + 1, elementForPiece(p + 1, pieces, length));
    float sum = 0f;
    boolean highlight = false;
    for (int e = first; e < end; e++) {
      int v = arrayModel.get(e);
      sum += disparity(e, v, length);
      if (arrayModel.getMarker(e) == Marker.SET) {
        highlight = true;
        if (soundThisFrame) {
          sound.playSound(e);
        }
      }
    }
    pieceDisparity = sum / Math.max(1, end - first);
    pieceValue = Math.max(0, Math.min(length - 1, arrayModel.get(first)));
    pieceHighlight = highlight;
  }

  /** Loading / error caption at the bottom of the canvas. */
  protected void drawStatus(String extra) {
    String text = source.status();
    if ((text == null || text.isBlank()) && extra != null) {
      text = extra;
    }
    if (text == null || text.isBlank()) {
      return;
    }
    float size = Math.max(14f, screenHeight / 40f);
    float w = rs.measureTextWidth(text, size);
    rs.drawText(text, (screenWidth - w) / 2f, screenHeight - size * 2.2f, size);
  }
}
