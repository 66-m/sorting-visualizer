package io.github._66_m.visual;

import io.github._66_m.control.config.MediaKind;
import io.github._66_m.control.config.visual.GlobeSettings;
import io.github._66_m.control.config.visual.ModelOptions;
import io.github._66_m.control.config.visual.VisualizationSettings;
import io.github._66_m.control.model.ArrayModel;
import io.github._66_m.control.render.RenderSystem;
import io.github._66_m.control.render.mesh.AsyncValue;
import io.github._66_m.control.render.mesh.GlobeMeshes;
import io.github._66_m.control.render.mesh.ObjLoader;
import io.github._66_m.control.render.mesh.PieceFrame;
import io.github._66_m.control.render.mesh.PieceMesh;
import io.github._66_m.control.render.mesh.ProceduralPlanet;
import io.github._66_m.control.render.mesh.TextureImage;
import io.github._66_m.sound.Sound;
import io.github._66_m.visual.gradient.ColorGradient;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * A textured, spinning planet. {@code MOSAIC}: latitude × longitude tiles, each showing the texture
 * of its value's tile (optionally lifted by disparity). {@code WEDGES}: pole-to-pole slices pushed
 * outward by disparity, showing their value's texture, with a core visible through the gaps. The
 * texture is any equirectangular image (e.g. NASA Blue Marble); a generated planet until one is
 * picked.
 */
public class Globe extends AbstractModelVisualization {

  private static final Logger LOGGER = Logger.getLogger(Globe.class.getName());

  /** Tile / wedge caps; larger arrays share pieces. */
  static final int MAX_TILES = 16_384;

  static final int MAX_WEDGES = 1024;

  private static final float TILE_BORDER_SCALE = 0.94f;
  private static final float CORE_RADIUS = 0.965f;
  private static final int MAGMA = 0xFFFF5A1F;
  private static final int DARK_CORE = 0xFF1A1C22;

  private volatile GlobeSettings settings = GlobeSettings.defaults();

  private record Key(GlobeSettings.Mode mode, GlobeSettings.Tiling tiling, int pieces) {}

  private final AsyncValue<Key, PieceMesh> meshes = new AsyncValue<>("globe-mesh");
  private final PieceFrame frame = new PieceFrame();
  private final float[] scale = new float[9];

  private volatile TextureImage texture;
  private volatile String texturePath = "";
  private volatile String status = "";
  private PieceMesh textured;
  private PieceMesh texturedFrom;
  private TextureImage texturedWith;

  public Globe(ArrayModel arrayModel, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    super(arrayModel, colorGradient, sound, rs);
    name = "3D - Globe";
  }

  @Override
  public VisualizationSettings currentSettings() {
    return settings;
  }

  @Override
  public void applySettings(VisualizationSettings next) {
    if (next instanceof GlobeSettings s) {
      settings = s;
    }
  }

  @Override
  protected ModelOptions.UpAxis upAxis() {
    return ModelOptions.UpAxis.Y_UP;
  }

  @Override
  public MediaKind mediaKind() {
    return MediaKind.TEXTURE;
  }

  @Override
  public String mediaPath() {
    return texturePath;
  }

  @Override
  public boolean loadMedia(String path) {
    if (path == null || path.isBlank() || !MediaKind.TEXTURE.accepts(path)) {
      return false;
    }
    texturePath = path;
    status = "Loading texture…";
    Thread t =
        new Thread(
            () -> {
              try {
                BufferedImage img = ImageIO.read(new File(path));
                if (img == null) {
                  throw new IOException("Unsupported image format");
                }
                if (path.equals(texturePath)) {
                  texture = ObjLoader.fromImage(img, 8192);
                  status = "";
                }
              } catch (IOException | RuntimeException | OutOfMemoryError e) {
                LOGGER.log(Level.WARNING, "Cannot load globe texture " + path, e);
                if (path.equals(texturePath)) {
                  status = "Cannot load texture: " + e.getMessage();
                }
              }
            },
            "globe-texture");
    t.setDaemon(true);
    t.start();
    return true;
  }

  static int pieceCount(GlobeSettings.Mode mode, int length) {
    return mode == GlobeSettings.Mode.WEDGES
        ? Math.max(1, Math.min(MAX_WEDGES, length))
        : Math.max(1, Math.min(MAX_TILES, length));
  }

  private static PieceMesh build(Key key) {
    if (key.mode() == GlobeSettings.Mode.WEDGES) {
      return GlobeMeshes.wedges(key.pieces());
    }
    if (key.tiling() == GlobeSettings.Tiling.EQUAL_AREA) {
      return GlobeMeshes.tiles(GlobeMeshes.equalAreaColumns(key.pieces()));
    }
    int[] shape = GlobeMeshes.equirectShape(key.pieces());
    int[] cols = new int[shape[0]];
    java.util.Arrays.fill(cols, shape[1]);
    return GlobeMeshes.tiles(cols);
  }

  @Override
  public void update(float delta) {
    GlobeSettings s = settings;
    int length = arrayModel.getLength();
    axialTilt = (float) Math.toRadians(-s.axialTiltDeg());
    beginTransform(delta, s.rotationSpeedRadPerSec(), s.viewTiltDeg(), s.sceneScale());
    rs.begin3D();
    PieceMesh mesh = null;
    if (length > 0) {
      Key key = new Key(s.mode(), s.tiling(), pieceCount(s.mode(), length));
      mesh = meshes.get(key, () -> build(key));
    }
    if (mesh != null) {
      PieceMesh withTexture = texturedMesh(mesh);
      fill(withTexture, s, length);
      rs.drawPieces(withTexture, frame);
    }
    rs.end3D();
    drawStatus(!status.isBlank() ? status : !meshes.isCurrent() ? "Building globe…" : null);
  }

  private PieceMesh texturedMesh(PieceMesh mesh) {
    TextureImage tex = texture != null ? texture : ProceduralPlanet.texture();
    if (textured == null || texturedFrom != mesh || texturedWith != tex) {
      textured = mesh.withTexture(tex);
      texturedFrom = mesh;
      texturedWith = tex;
    }
    return textured;
  }

  private void fill(PieceMesh mesh, GlobeSettings s, int length) {
    int corePiece = mesh.pieceCount - 1;
    // Pieces that take part in the sort; equirect grids may have a few extra tiles.
    int active = Math.min(corePiece, pieceCount(s.mode(), length));
    boolean wedges = s.mode() == GlobeSettings.Mode.WEDGES;
    frame.colorMode = PieceFrame.ColorMode.TEXTURE;
    frame.ensureCapacity(mesh.pieceCount);
    float tileScale = !wedges && s.tileBorders() ? TILE_BORDER_SCALE : 1f;
    for (int k = 0; k < corePiece; k++) {
      int src = k;
      float d = 0f;
      boolean highlight = false;
      if (k < active) {
        pieceStats(k, active, length);
        src = sourcePiece(k, pieceValue, active, length);
        d = pieceDisparity;
        highlight = pieceHighlight;
      }
      float cx = mesh.centers[k * 3];
      float cy = mesh.centers[k * 3 + 1];
      float cz = mesh.centers[k * 3 + 2];
      float push;
      float dx = cx;
      float dy = cy;
      float dz = cz;
      if (wedges) {
        // Push horizontally, away from the axis.
        dy = 0f;
        float len = (float) Math.sqrt(dx * dx + dz * dz);
        dx = len > 1e-5f ? dx / len : 0f;
        dz = len > 1e-5f ? dz / len : 0f;
        push = d * (float) s.explode();
      } else {
        push = s.lift() ? d * (float) s.liftHeight() : 0f;
      }
      float[] rot = null;
      if (tileScale != 1f) {
        uniformScale(tileScale);
        rot = scale;
      }
      setPieceTransform(frame, k, rot, cx, cy, cz, dx * push, dy * push, dz * push);
      setUvRemap(mesh, k, src);
      frame.setColor(k, 0xFFFFFFFF, highlight ? (float) s.highlightStrength() : 0f);
    }
    // Core: a slightly smaller sphere, hidden when disabled.
    uniformScale(s.core() == GlobeSettings.Core.NONE ? 0f : CORE_RADIUS);
    setPieceTransform(frame, corePiece, scale, 0f, 0f, 0f, 0f, 0f, 0f);
    frame.setSolidColor(corePiece, s.core() == GlobeSettings.Core.MAGMA ? MAGMA : DARK_CORE);
    frame.setUv(corePiece, 1f, 1f, 0f, 0f);
    frame.count = mesh.pieceCount;
  }

  /** UV transform so piece {@code slot} shows the texture of piece {@code src}. */
  private void setUvRemap(PieceMesh mesh, int slot, int src) {
    float[] r = mesh.uvRects;
    float su = (r[src * 4 + 2] - r[src * 4]) / Math.max(1e-6f, r[slot * 4 + 2] - r[slot * 4]);
    float sv =
        (r[src * 4 + 3] - r[src * 4 + 1]) / Math.max(1e-6f, r[slot * 4 + 3] - r[slot * 4 + 1]);
    float ou = r[src * 4] - r[slot * 4] * su;
    float ov = r[src * 4 + 1] - r[slot * 4 + 1] * sv;
    frame.setUv(slot, su, sv, ou, ov);
  }

  private void uniformScale(float f) {
    java.util.Arrays.fill(scale, 0f);
    scale[0] = f;
    scale[4] = f;
    scale[8] = f;
  }
}
