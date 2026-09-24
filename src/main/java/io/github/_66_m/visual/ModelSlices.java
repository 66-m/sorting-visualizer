package io.github._66_m.visual;

import io.github._66_m.control.config.visual.ModelOptions;
import io.github._66_m.control.config.visual.ModelSlicesSettings;
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
 * The model cut into parallel slices; slot {@code k} shows the slice of its value, so a shuffled
 * array stacks the layers in random order. The 3D counterpart of Image Horizontal.
 */
public class ModelSlices extends AbstractModelVisualization {

  private volatile ModelSlicesSettings settings = ModelSlicesSettings.defaults();

  private record Key(long generation, int pieces, ModelSlicesSettings.Axis axis) {}

  private final AsyncValue<Key, PieceMesh> slices = new AsyncValue<>("model-slices-cut");
  private final PieceFrame frame = new PieceFrame();
  private static final float[] ZERO = new float[9];

  public ModelSlices(
      ArrayModel arrayModel, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayModel, colorGradient, sound, rs);
    name = "3D - Model Slices";
  }

  @Override
  public VisualizationSettings currentSettings() {
    return settings;
  }

  @Override
  public void applySettings(VisualizationSettings next) {
    if (next instanceof ModelSlicesSettings s) {
      settings = s;
      syncUpAxis();
    }
  }

  @Override
  protected ModelOptions.UpAxis upAxis() {
    return settings.upAxis();
  }

  static int axisIndex(ModelSlicesSettings.Axis axis) {
    return switch (axis) {
      case X -> 0;
      case Y -> 1;
      case Z -> 2;
    };
  }

  @Override
  public void update(float delta) {
    ModelSlicesSettings s = settings;
    int length = arrayModel.getLength();
    beginTransform(delta, s.rotationSpeedRadPerSec(), s.tiltDeg(), s.sceneScale());
    rs.begin3D();
    PieceMesh mesh = null;
    boolean tooBig = !Partitions.fitsPieces(source.mesh());
    if (length > 0 && !tooBig) {
      MeshData model = source.mesh();
      Key key = new Key(source.generation(), s.effectivePieces(length), s.axis());
      mesh = slices.get(key, () -> Partitions.slices(model, key.pieces(), axisIndex(key.axis())));
    }
    if (mesh != null) {
      fill(mesh, s, length, source.mesh().hasColor());
      rs.drawPieces(mesh, frame);
    }
    rs.end3D();
    drawStatus(
        tooBig
            ? "Model too large for slices (max " + Partitions.MAX_PIECE_TRIANGLES + " triangles)"
            : !slices.isCurrent() ? "Slicing model…" : null);
  }

  private void fill(PieceMesh mesh, ModelSlicesSettings s, int length, boolean hasModelColor) {
    int pieces = mesh.pieceCount;
    int axis = axisIndex(s.axis());
    boolean modelColor = s.colorSource() == ModelOptions.ColorSource.MODEL && hasModelColor;
    frame.colorMode =
        !modelColor
            ? PieceFrame.ColorMode.PIECE
            : mesh.texture != null ? PieceFrame.ColorMode.TEXTURE : PieceFrame.ColorMode.VERTEX;
    frame.ensureCapacity(pieces);
    float spread = 1f + (float) s.sliceGap();
    float[] offset = new float[3];
    // With fewer slices than elements two slots can pick the same slice; unplaced slices hide.
    for (int p = 0; p < pieces; p++) {
      frame.setTransform(p, ZERO, 0f, 0f, 0f);
    }
    for (int k = 0; k < pieces; k++) {
      pieceStats(k, pieces, length);
      int src = sourcePiece(k, pieceValue, pieces, length);
      offset[axis] = mesh.centers[k * 3 + axis] * spread - mesh.centers[src * 3 + axis];
      // Every vertex of slice `src` is drawn at slot k, so the piece id in the mesh is `src`.
      setPieceTransform(frame, src, null, 0f, 0f, 0f, offset[0], offset[1], offset[2]);
      frame.setColor(
          src, colorGradient.getMarkerArgb(pieceValue, Marker.NORMAL), pieceHighlight ? 0.85f : 0f);
      frame.setUv(src, 1f, 1f, 0f, 0f);
    }
    frame.count = pieces;
  }
}
