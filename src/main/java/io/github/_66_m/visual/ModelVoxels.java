package io.github._66_m.visual;

import io.github._66_m.control.config.visual.ModelOptions;
import io.github._66_m.control.config.visual.ModelVoxelsSettings;
import io.github._66_m.control.config.visual.ModelVoxelsSettings.Direction;
import io.github._66_m.control.config.visual.ModelVoxelsSettings.Fill;
import io.github._66_m.control.config.visual.VisualizationSettings;
import io.github._66_m.control.model.ArrayModel;
import io.github._66_m.control.render.InstanceData;
import io.github._66_m.control.render.RenderSystem;
import io.github._66_m.control.render.mesh.AsyncValue;
import io.github._66_m.control.render.mesh.MeshData;
import io.github._66_m.control.render.mesh.Voxelizer;
import io.github._66_m.sound.Sound;
import io.github._66_m.visual.gradient.ColorGradient;

/**
 * A 3D model voxelized into roughly N cubes. Voxel count rarely equals N exactly, so voxel slots
 * map to elements proportionally ({@link #elementForPiece} / {@link #sourcePiece}).
 */
public class ModelVoxels extends AbstractModelVisualization {

  private volatile ModelVoxelsSettings settings = ModelVoxelsSettings.defaults();

  private record Key(long generation, int count, Fill fill, ModelOptions.AssemblyOrder order) {}

  private record Grid(Voxelizer.Voxels voxels, boolean hasColor) {}

  private final AsyncValue<Key, Grid> grid = new AsyncValue<>("model-voxelize");
  private final InstanceData boxes = new InstanceData();
  private final float[] world = new float[3];
  private final float[] local = new float[3];

  public ModelVoxels(
      ArrayModel arrayModel, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayModel, colorGradient, sound, rs);
    name = "3D - Model Voxels";
  }

  @Override
  public VisualizationSettings currentSettings() {
    return settings;
  }

  @Override
  public void applySettings(VisualizationSettings next) {
    if (next instanceof ModelVoxelsSettings s) {
      settings = s;
      syncUpAxis();
    }
  }

  @Override
  protected ModelOptions.UpAxis upAxis() {
    return settings.upAxis();
  }

  @Override
  public void update(float delta) {
    ModelVoxelsSettings s = settings;
    int length = arrayModel.getLength();
    beginTransform(delta, s.rotationSpeedRadPerSec(), s.tiltDeg(), s.sceneScale());
    rs.begin3D();
    Grid g = null;
    boolean current = false;
    if (length > 0) {
      MeshData mesh = source.mesh();
      Key key = new Key(source.generation(), length, s.fill(), s.order());
      g =
          grid.get(
              key,
              () ->
                  new Grid(
                      Voxelizer.voxelize(mesh, key.count(), key.fill() == Fill.SOLID, key.order()),
                      mesh.hasColor()));
      current = grid.isCurrent();
    }
    if (g != null) {
      fill(g, s, length);
      rs.drawBoxes(boxes);
    }
    rs.end3D();
    drawStatus(!current ? "Voxelizing model…" : null);
  }

  private void fill(Grid g, ModelVoxelsSettings s, int n) {
    Voxelizer.Voxels vox = g.voxels();
    int count = vox.count();
    boolean modelColor = s.colorSource() == ModelOptions.ColorSource.MODEL && g.hasColor();
    float explode = (float) s.explode();
    float edge = vox.size() * worldScale() * (float) (1.0 - s.voxelGap());
    float tilt = tiltRad();
    boxes.ensureCapacity(count);
    int lastElement = -1;
    for (int k = 0; k < count; k++) {
      int e = elementForPiece(k, count, n);
      int value = Math.max(0, Math.min(n - 1, arrayModel.get(e)));
      int src = sourcePiece(k, value, count, n);
      switch (s.mode()) {
        case SCATTER -> scatterPoint(vox.pos(), k, src, s.order(), local);
        case COLOR -> {
          local[0] = vox.pos()[k * 3];
          local[1] = vox.pos()[k * 3 + 1];
          local[2] = vox.pos()[k * 3 + 2];
        }
        default -> displaced(vox, src, disparity(e, value, n) * explode, s.direction(), local);
      }
      int color;
      if (e == lastElement) {
        // Same element as the previous slot: reuse the marker color without replaying sound.
        color =
            arrayModel.getMarker(e) == Marker.SET
                ? colorGradient.getMarkerArgb(value, Marker.SET)
                : modelColor ? vox.color()[src] : colorGradient.getMarkerArgb(value, Marker.NORMAL);
      } else {
        color = modelColor ? modelOrMarker(e, value, vox.color()[src]) : elementColor(e, value);
      }
      lastElement = e;
      toWorld(local[0], local[1], local[2], world, 0);
      boxes.set(k, world[0], world[1], world[2], edge, edge, edge, tilt, spin, 0, color);
    }
    boxes.count = count;
  }

  /** Home of voxel {@code src} moved by {@code d} in {@code direction}. */
  static void displaced(Voxelizer.Voxels vox, int src, float d, Direction direction, float[] out) {
    float x = vox.pos()[src * 3];
    float y = vox.pos()[src * 3 + 1];
    float z = vox.pos()[src * 3 + 2];
    switch (direction) {
      case RADIAL -> {
        float len = (float) Math.sqrt(x * x + y * y + z * z);
        if (len > 1e-6f) {
          x += x / len * d;
          y += y / len * d;
          z += z / len * d;
        }
      }
      case NORMAL -> {
        x += vox.nrm()[src * 3] * d;
        y += vox.nrm()[src * 3 + 1] * d;
        z += vox.nrm()[src * 3 + 2] * d;
      }
      case DOWN -> {
        // Melt: fall toward the floor (model bottom) and spread out a little.
        float floor = -1f + vox.size() * 0.5f;
        float fall = Math.min(1f, d);
        y = y + (floor - y) * fall;
        x *= 1f + fall * 0.6f;
        z *= 1f + fall * 0.6f;
      }
    }
    out[0] = x;
    out[1] = y;
    out[2] = z;
  }
}
