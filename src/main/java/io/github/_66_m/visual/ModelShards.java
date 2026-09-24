package io.github._66_m.visual;

import io.github._66_m.control.config.visual.ModelOptions;
import io.github._66_m.control.config.visual.ModelShardsSettings;
import io.github._66_m.control.config.visual.VisualizationSettings;
import io.github._66_m.control.model.ArrayModel;
import io.github._66_m.control.render.RenderSystem;
import io.github._66_m.control.render.mesh.AsyncValue;
import io.github._66_m.control.render.mesh.MeshData;
import io.github._66_m.control.render.mesh.Partitions;
import io.github._66_m.control.render.mesh.PieceFrame;
import io.github._66_m.control.render.mesh.PieceMesh;
import io.github._66_m.sound.Sound;
import io.github._66_m.visual.gradient.ColorGradient;

/**
 * The real model split into shards. Each shard flies outward from the model center and tumbles
 * around its own axis in proportion to the disparity of its element(s); sorted = the intact model.
 */
public class ModelShards extends AbstractModelVisualization {

  private volatile ModelShardsSettings settings = ModelShardsSettings.defaults();

  private record Key(long generation, int pieces, ModelOptions.AssemblyOrder order) {}

  private final AsyncValue<Key, PieceMesh> shards = new AsyncValue<>("model-shards-split");
  private final PieceFrame frame = new PieceFrame();
  private final float[] rot = new float[9];

  public ModelShards(
      ArrayModel arrayModel, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayModel, colorGradient, sound, rs);
    name = "3D - Model Shards";
  }

  @Override
  public VisualizationSettings currentSettings() {
    return settings;
  }

  @Override
  public void applySettings(VisualizationSettings next) {
    if (next instanceof ModelShardsSettings s) {
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
    ModelShardsSettings s = settings;
    int length = arrayModel.getLength();
    beginTransform(delta, s.rotationSpeedRadPerSec(), s.tiltDeg(), s.sceneScale());
    rs.begin3D();
    PieceMesh mesh = null;
    boolean tooBig = !Partitions.fitsPieces(source.mesh());
    if (length > 0 && !tooBig) {
      MeshData model = source.mesh();
      Key key = new Key(source.generation(), s.effectivePieces(length), s.order());
      mesh = shards.get(key, () -> Partitions.shards(model, key.pieces(), key.order()));
    }
    boolean hasModelColor = source.mesh().hasColor();
    if (mesh != null) {
      fill(mesh, s, length, hasModelColor);
      rs.drawPieces(mesh, frame);
    }
    rs.end3D();
    drawStatus(
        tooBig
            ? "Model too large for shards (max " + Partitions.MAX_PIECE_TRIANGLES + " triangles)"
            : !shards.isCurrent() ? "Cutting shards…" : null);
  }

  private void fill(PieceMesh mesh, ModelShardsSettings s, int length, boolean hasModelColor) {
    int pieces = mesh.pieceCount;
    boolean modelColor = s.colorSource() == ModelOptions.ColorSource.MODEL && hasModelColor;
    frame.colorMode =
        !modelColor
            ? PieceFrame.ColorMode.PIECE
            : mesh.texture != null ? PieceFrame.ColorMode.TEXTURE : PieceFrame.ColorMode.VERTEX;
    frame.ensureCapacity(pieces);
    float explode = (float) s.explode();
    float maxSpin = (float) Math.toRadians(s.maxSpinDeg());
    for (int p = 0; p < pieces; p++) {
      pieceStats(p, pieces, length);
      float d = pieceDisparity;
      float cx = mesh.centers[p * 3];
      float cy = mesh.centers[p * 3 + 1];
      float cz = mesh.centers[p * 3 + 2];
      float len = (float) Math.sqrt(cx * cx + cy * cy + cz * cz);
      float dirX = len > 1e-5f ? cx / len : mesh.axes[p * 3];
      float dirY = len > 1e-5f ? cy / len : mesh.axes[p * 3 + 1];
      float dirZ = len > 1e-5f ? cz / len : mesh.axes[p * 3 + 2];
      float push = d * explode;
      float[] r = null;
      if (d > 0f && maxSpin > 0f) {
        axisAngle(mesh.axes[p * 3], mesh.axes[p * 3 + 1], mesh.axes[p * 3 + 2], d * maxSpin, rot);
        r = rot;
      }
      setPieceTransform(frame, p, r, cx, cy, cz, dirX * push, dirY * push, dirZ * push);
      int color = colorGradient.getMarkerArgb(pieceValue, Marker.NORMAL);
      frame.setColor(p, color, pieceHighlight ? 0.85f : 0f);
      frame.setUv(p, 1f, 1f, 0f, 0f);
    }
    frame.count = pieces;
  }
}
