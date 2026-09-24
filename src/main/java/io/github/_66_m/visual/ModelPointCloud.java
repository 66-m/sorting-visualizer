package io.github._66_m.visual;

import io.github._66_m.control.config.visual.ModelOptions;
import io.github._66_m.control.config.visual.ModelPointCloudSettings;
import io.github._66_m.control.config.visual.ModelPointCloudSettings.Mode;
import io.github._66_m.control.config.visual.VisualizationSettings;
import io.github._66_m.control.model.ArrayModel;
import io.github._66_m.control.render.InstanceData;
import io.github._66_m.control.render.RenderSystem;
import io.github._66_m.control.render.mesh.AsyncValue;
import io.github._66_m.control.render.mesh.MeshData;
import io.github._66_m.control.render.mesh.SurfaceSamples;
import io.github._66_m.sound.Sound;
import io.github._66_m.visual.gradient.ColorGradient;

/**
 * A 3D model sampled into N surface dots (one per element). See {@link Mode} for how the value
 * shows: scattered along the assembly axis, as a color, or as outward displacement by disparity.
 */
public class ModelPointCloud extends AbstractModelVisualization {

  private volatile ModelPointCloudSettings settings = ModelPointCloudSettings.defaults();

  private record Key(long generation, int count, ModelOptions.AssemblyOrder order) {}

  private record Cloud(SurfaceSamples samples, boolean hasColor) {}

  private final AsyncValue<Key, Cloud> cloud = new AsyncValue<>("model-points-sample");
  private final InstanceData dots = new InstanceData();
  private final float[] world = new float[3];

  public ModelPointCloud(
      ArrayModel arrayModel, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayModel, colorGradient, sound, rs);
    name = "3D - Model Point Cloud";
  }

  @Override
  public VisualizationSettings currentSettings() {
    return settings;
  }

  @Override
  public void applySettings(VisualizationSettings next) {
    if (next instanceof ModelPointCloudSettings s) {
      settings = s;
      syncUpAxis();
    }
  }

  @Override
  protected ModelOptions.UpAxis upAxis() {
    return settings.upAxis();
  }

  private static Cloud buildCloud(MeshData mesh, int count, ModelOptions.AssemblyOrder order) {
    return new Cloud(SurfaceSamples.sample(mesh, count, order), mesh.hasColor());
  }

  @Override
  public void update(float delta) {
    ModelPointCloudSettings s = settings;
    int length = arrayModel.getLength();
    beginTransform(delta, s.rotationSpeedRadPerSec(), s.tiltDeg(), s.sceneScale());
    rs.begin3D();
    Cloud c = null;
    if (length > 0) {
      MeshData mesh = source.mesh();
      Key key = new Key(source.generation(), length, s.order());
      c = cloud.get(key, () -> buildCloud(mesh, key.count(), key.order()));
    }
    if (c != null && c.samples().count == length) {
      fill(c, s, length);
      rs.drawSpheres(dots);
    }
    rs.end3D();
    drawStatus(c == null || c.samples().count != length ? "Sampling model…" : null);
  }

  private void fill(Cloud c, ModelPointCloudSettings s, int n) {
    SurfaceSamples smp = c.samples();
    boolean modelColor = s.colorSource() == ModelOptions.ColorSource.MODEL && c.hasColor();
    float explode = (float) s.explode();
    float size = (float) s.dotSize();
    dots.ensureCapacity(n);
    for (int i = 0; i < n; i++) {
      int v = Math.max(0, Math.min(n - 1, arrayModel.get(i)));
      float x;
      float y;
      float z;
      switch (s.mode()) {
        case SCATTER -> {
          scatterPoint(smp.pos, i, v, s.order(), world);
          x = world[0];
          y = world[1];
          z = world[2];
        }
        case COLOR -> {
          x = smp.pos[i * 3];
          y = smp.pos[i * 3 + 1];
          z = smp.pos[i * 3 + 2];
        }
        default -> {
          float d = disparity(i, v, n) * explode;
          x = smp.pos[v * 3] + smp.nrm[v * 3] * d;
          y = smp.pos[v * 3 + 1] + smp.nrm[v * 3 + 1] * d;
          z = smp.pos[v * 3 + 2] + smp.nrm[v * 3 + 2] * d;
        }
      }
      int color = modelColor ? modelOrMarker(i, v, smp.color[v]) : elementColor(i, v);
      toWorld(x, y, z, world, 0);
      dots.set(i, world[0], world[1], world[2], size, size, size, 0, 0, 0, color);
    }
    dots.count = n;
  }
}
