package io.github.compilerstuck.visual;

import static java.lang.Math.floor;
import static java.lang.Math.min;

import io.github.compilerstuck.control.config.visual.CubeSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.InstanceData;
import io.github.compilerstuck.control.render.InstanceTransform;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;

public class Cube extends Visualization implements ConfigurableVisualization {

  /** Unit-box corners matching ModelBuilder/BoxShapeBuilder size 1 (local ±0.5). */
  private static final float[] CORNERS = {
    -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.5f, -0.5f, -0.5f, -0.5f,
    0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f,
  };

  /** 12 edges as pairs of corner indices. */
  private static final int[] EDGE_CORNERS = {
    0, 1, 1, 2, 2, 3, 3, 0,
    4, 5, 5, 6, 6, 7, 7, 4,
    0, 4, 1, 5, 2, 6, 3, 7,
  };

  /** Legacy fixed tilt ({@code Math.sin/cos(-10)} used radians, not degrees). */
  private static final float SIN_TILT = (float) Math.sin(-10);

  private static final float COS_TILT = (float) Math.cos(-10);

  /** Legacy fixed per-box X spin (radians). */
  private static final float BOX_SPIN_X = 45f;

  int radius;
  private float aa = 0;

  private volatile CubeSettings settings = CubeSettings.defaults();

  private final InstanceData boxes = new InstanceData();
  private final InstanceTransform boxXform = new InstanceTransform();
  private final float[] cornerWorld = new float[8 * 3];

  private float[] edgeXyzxyz;
  private int[] edgeArgb;

  private int[] colorsRgb;
  private float[] baseX, baseY, baseZ;
  private float[] sizes;
  private int bufferCapacity;
  private int latticeXSize = -1;
  private int latticeRadius = -1;
  private int latticeDrawCount = -1;

  private long cachedRevision = Long.MIN_VALUE;
  private int cachedWidth = -1;
  private int cachedHeight = -1;
  private int cachedDrawCount = -1;
  private int cachedLength = -1;
  private float cachedMaxBoxSize = -1;
  private ColorGradient cachedGradient;

  public Cube(ArrayModel arrayModel, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayModel, colorGradient, sound, rs);
    name = "3D - Cube";
  }

  @Override
  public VisualizationSettings currentSettings() {
    return settings;
  }

  @Override
  public void applySettings(VisualizationSettings next) {
    if (!(next instanceof CubeSettings cube)) {
      return;
    }
    settings = cube;
    // Scene scale affects lattice radius; force rebuild on next frame.
    latticeXSize = -1;
    latticeRadius = -1;
    latticeDrawCount = -1;
    cachedRevision = Long.MIN_VALUE;
  }

  @Override
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  private void ensureBuffers(int n) {
    if (colorsRgb != null && bufferCapacity >= n) return;
    bufferCapacity = n;
    colorsRgb = new int[n];
    baseX = new float[n];
    baseY = new float[n];
    baseZ = new float[n];
    sizes = new float[n];
    latticeXSize = -1;
    latticeRadius = -1;
    latticeDrawCount = -1;
  }

  private void rebuildLattice(int drawCount, int xSize, int radius) {
    if (latticeDrawCount == drawCount && latticeXSize == xSize && latticeRadius == radius) {
      return;
    }
    latticeDrawCount = drawCount;
    latticeXSize = xSize;
    latticeRadius = radius;

    int xCnt = 0;
    int yCnt = 0;
    int zCnt = 0;
    for (int i = 0; i < drawCount; i++) {
      baseX[i] = VisMath.map(xCnt, 0, xSize, -radius, radius);
      baseY[i] = VisMath.map(yCnt, 0, xSize, -radius, radius);
      baseZ[i] = VisMath.map(zCnt, 0, xSize, -radius, radius);

      zCnt++;
      if (zCnt == xSize) {
        if (xCnt == xSize - 1) {
          yCnt += 1;
          zCnt = 0;
          xCnt = 0;
        } else {
          xCnt += 1;
          zCnt = 0;
        }
      }
    }
  }

  private void ensureSizesAndColors(int drawCount, int length, float maxBoxSize) {
    long rev = arrayModel.getVisualRevision();
    if (cachedRevision == rev
        && cachedWidth == screenWidth
        && cachedHeight == screenHeight
        && cachedDrawCount == drawCount
        && cachedLength == length
        && cachedMaxBoxSize == maxBoxSize
        && cachedGradient == colorGradient) {
      return;
    }
    for (int i = 0; i < drawCount; i++) {
      int value = arrayModel.get(i);
      Color color = colorGradient.getMarkerColor(value, arrayModel.getMarker(i));

      if (arrayModel.getMarker(value) == Marker.SET) {
        sound.playSound(value);
      }

      float barHeight =
          (length
              - 2f
                  * Math.min(
                      Math.min(Math.abs(i - value), Math.abs(i - length - value)),
                      Math.abs(i + length - value)));

      colorsRgb[i] = color.getRGB();
      sizes[i] = VisMath.map(barHeight, 0, length, 0, maxBoxSize);
    }
    cachedRevision = rev;
    cachedWidth = screenWidth;
    cachedHeight = screenHeight;
    cachedDrawCount = drawCount;
    cachedLength = length;
    cachedMaxBoxSize = maxBoxSize;
    cachedGradient = colorGradient;
  }

  private void ensureEdgeBuffers(int drawCount) {
    int edges = drawCount * 12;
    if (edgeXyzxyz != null && edgeXyzxyz.length >= edges * 6) {
      return;
    }
    edgeXyzxyz = new float[edges * 6];
    edgeArgb = new int[edges];
  }

  /** Opaque wireframe edges matching legacy Processing box() stroke. */
  private int fillEdges(int drawCount) {
    ensureEdgeBuffers(drawCount);
    int edgeCount = 0;
    for (int i = 0; i < drawCount; i++) {
      float s = sizes[i];
      if (s <= 0f) {
        continue;
      }
      boxXform.transformLocalPoints(boxes, i, CORNERS, 8, cornerWorld);
      int stroke = 0xFF000000 | (colorsRgb[i] & 0xffffff);
      for (int e = 0; e < 12; e++) {
        int a = EDGE_CORNERS[e * 2];
        int b = EDGE_CORNERS[e * 2 + 1];
        int o = edgeCount * 6;
        int ao = a * 3;
        int bo = b * 3;
        edgeXyzxyz[o] = cornerWorld[ao];
        edgeXyzxyz[o + 1] = cornerWorld[ao + 1];
        edgeXyzxyz[o + 2] = cornerWorld[ao + 2];
        edgeXyzxyz[o + 3] = cornerWorld[bo];
        edgeXyzxyz[o + 4] = cornerWorld[bo + 1];
        edgeXyzxyz[o + 5] = cornerWorld[bo + 2];
        edgeArgb[edgeCount] = stroke;
        edgeCount++;
      }
    }
    return edgeCount;
  }

  @Override
  public void update(float delta) {
    CubeSettings s = settings;
    int screenMin = min(screenHeight, screenWidth);
    radius = (int) (screenMin / s.sceneScaleDivisor());
    // World3D: legacy center (W/2, H/2 - screenMin/10, -screenMin/10) → (0, screenMin/10, …)
    float centerY = screenMin / 10f;
    float centerZ = -(int) (screenMin / 10);

    aa -= (float) (s.rotationSpeedRadPerSec() * delta);
    float sinAa = (float) Math.sin(aa);
    float cosAa = (float) Math.cos(aa);

    int xSize = (int) floor(Math.pow(arrayModel.getLength(), 1 / 3f) + 0.1);
    if (xSize < 1) {
      xSize = 1;
    }
    int drawCount = Math.min(arrayModel.getLength(), xSize * xSize * xSize);
    int length = arrayModel.getLength();
    float maxBoxSize = radius * 2f / xSize;

    ensureBuffers(drawCount);
    rebuildLattice(drawCount, xSize, radius);
    ensureSizesAndColors(drawCount, length, maxBoxSize);

    int fillOpacity = s.fillOpacity();
    int fillAlpha = fillOpacity << 24;
    boolean drawFills = fillOpacity > 0;

    boxes.ensureCapacity(drawCount);
    for (int i = 0; i < drawCount; i++) {
      float xa = baseX[i];
      float ya = baseY[i];
      float za = baseZ[i];

      float zb = sinAa * xa + cosAa * za;
      float x = cosAa * xa - sinAa * za;
      float z = SIN_TILT * ya + COS_TILT * zb;
      float y = COS_TILT * ya - SIN_TILT * zb;

      float size = sizes[i];
      int argb = fillAlpha | (colorsRgb[i] & 0xffffff);
      // World euler: negate legacy X/Z so on-screen spin matches prior Processing path.
      boxes.set(i, x, centerY - y, centerZ + z, size, size, size, -BOX_SPIN_X, 0f, aa, argb);
    }
    boxes.count = drawCount;

    // Edges need filled instance transforms; always build when wireframe is on.
    int edgeCount = s.wireframeEnabled() ? fillEdges(drawCount) : 0;

    rs.begin3D();
    if (drawFills) {
      rs.drawBoxes(boxes);
    }
    if (edgeCount > 0) {
      // Overlay wireframe: skip depth so edges stay readable at opacity 0 and 254.
      rs.strokeLines3D(edgeXyzxyz, edgeArgb, edgeCount, false);
    }
    rs.end3D();
  }
}
