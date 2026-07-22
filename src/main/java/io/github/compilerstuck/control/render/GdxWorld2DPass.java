package io.github.compilerstuck.control.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.compilerstuck.control.LaunchArgs;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * World2D draw path: ShapeRenderer / SpriteBatch open-close, GeometryBatch2D rects/circles/ellipses
 * (or legacy ShapeRenderer when {@code --legacy-2d} / no GL30).
 */
final class GdxWorld2DPass implements Disposable {
  private static final Logger LOGGER = Logger.getLogger(GdxWorld2DPass.class.getName());

  private final GdxRenderSystem host;
  private final ShapeRenderer shapes = new ShapeRenderer();
  private final SpriteBatch sprites = new SpriteBatch();
  private final Color tmpColor = new Color();

  private GeometryBatch2D geometryBatch2D;
  private float strokeWeightPx = 1f;
  private boolean shapesOpen;
  private boolean spritesOpen;
  private ShapeRenderer.ShapeType shapeType;

  GdxWorld2DPass(GdxRenderSystem host) {
    this.host = host;
    GeometryBatch2D geo2d = null;
    if (!LaunchArgs.legacy2d() && Gdx.gl30 != null) {
      try {
        geo2d = new GeometryBatch2D();
      } catch (RuntimeException e) {
        LOGGER.log(
            Level.SEVERE, "Failed to init GeometryBatch2D; using ShapeRenderer for 2D geometry", e);
      }
    } else if (LaunchArgs.legacy2d()) {
      LOGGER.info("Using legacy 2D ShapeRenderer path (--legacy-2d)");
    }
    geometryBatch2D = geo2d;
  }

  boolean usesGeometryBatch2D() {
    return geometryBatch2D != null;
  }

  float strokeWeightPx() {
    return strokeWeightPx;
  }

  ShapeRenderer shapes() {
    return shapes;
  }

  SpriteBatch sprites() {
    return sprites;
  }

  Color tmpColor() {
    return tmpColor;
  }

  void strokeWeight(float weightPx) {
    strokeWeightPx = Math.max(0.1f, weightPx);
  }

  void fillRects(float[] xywh, int[] argb, int count) {
    if (count <= 0) {
      return;
    }
    OrthographicCamera camWorld2d = host.camWorld2d();
    FrameStats frameStats = host.frameStats();
    if (geometryBatch2D != null) {
      endShapes();
      endSprites();
      enterWorld2DPass();
      host.viewport2d().apply();
      if (geometryBatch2D.drawRects(xywh, argb, count, camWorld2d.combined)) {
        frameStats.geo2dDraws++;
        frameStats.geo2dPrimitives += count;
      }
      return;
    }
    beginShapes(ShapeRenderer.ShapeType.Filled);
    for (int i = 0; i < count; i++) {
      setShapeColor(argb[i]);
      int o = i * 4;
      shapes.rect(xywh[o], xywh[o + 1], xywh[o + 2], xywh[o + 3]);
    }
  }

  void fillCircles(float[] xyd, int[] argb, int count) {
    if (count <= 0) {
      return;
    }
    OrthographicCamera camWorld2d = host.camWorld2d();
    FrameStats frameStats = host.frameStats();
    if (geometryBatch2D != null) {
      endShapes();
      endSprites();
      enterWorld2DPass();
      host.viewport2d().apply();
      if (geometryBatch2D.drawCircles(xyd, argb, count, camWorld2d.combined)) {
        frameStats.geo2dDraws++;
        frameStats.geo2dPrimitives += count;
      }
      return;
    }
    beginShapes(ShapeRenderer.ShapeType.Filled);
    for (int i = 0; i < count; i++) {
      setShapeColor(argb[i]);
      int o = i * 3;
      shapes.circle(xyd[o], xyd[o + 1], xyd[o + 2] * 0.5f);
    }
  }

  void strokeLines(float[] xyxy, int[] argb, int count) {
    if (count <= 0) {
      return;
    }
    OrthographicCamera camWorld2d = host.camWorld2d();
    FrameStats frameStats = host.frameStats();
    if (geometryBatch2D != null) {
      endShapes();
      endSprites();
      enterWorld2DPass();
      host.viewport2d().apply();
      if (geometryBatch2D.drawLines(xyxy, argb, count, strokeWeightPx, camWorld2d.combined)) {
        frameStats.geo2dDraws++;
        frameStats.geo2dPrimitives += count;
      }
      return;
    }
    // Legacy ShapeRenderer: rectLine for thick strokes (glLineWidth is not portable).
    if (strokeWeightPx > GeometryBatch2D.HAIRLINE_MAX_PX) {
      beginShapes(ShapeRenderer.ShapeType.Filled);
      for (int i = 0; i < count; i++) {
        setShapeColor(argb[i]);
        int o = i * 4;
        shapes.rectLine(xyxy[o], xyxy[o + 1], xyxy[o + 2], xyxy[o + 3], strokeWeightPx);
      }
      return;
    }
    beginShapes(ShapeRenderer.ShapeType.Line);
    for (int i = 0; i < count; i++) {
      setShapeColor(argb[i]);
      int o = i * 4;
      shapes.line(xyxy[o], xyxy[o + 1], xyxy[o + 2], xyxy[o + 3]);
    }
  }

  void strokeEllipses(float[] xywh, int[] argb, int count) {
    if (count <= 0) {
      return;
    }
    OrthographicCamera camWorld2d = host.camWorld2d();
    FrameStats frameStats = host.frameStats();
    if (geometryBatch2D != null) {
      endShapes();
      endSprites();
      enterWorld2DPass();
      host.viewport2d().apply();
      if (geometryBatch2D.drawEllipsesStroke(
          xywh, argb, count, strokeWeightPx, camWorld2d.combined)) {
        frameStats.geo2dDraws++;
        frameStats.geo2dPrimitives += count;
      }
      return;
    }
    beginShapes(ShapeRenderer.ShapeType.Line);
    for (int i = 0; i < count; i++) {
      setShapeColor(argb[i]);
      int o = i * 4;
      float cx = xywh[o];
      float cy = xywh[o + 1];
      float w = xywh[o + 2];
      float h = xywh[o + 3];
      shapes.ellipse(cx - w * 0.5f, cy - h * 0.5f, w, h);
    }
  }

  void enterWorld2DPass() {
    if (host.pipeline().inWorld3D()) {
      host.world3d().end3D();
    }
    host.pipeline().enterWorld2D();
  }

  void beginShapes(ShapeRenderer.ShapeType type) {
    enterWorld2DPass();
    endSprites();
    if (shapesOpen && shapeType == type) {
      return;
    }
    endShapes();
    ScreenViewport viewport2d = host.viewport2d();
    OrthographicCamera camWorld2d = host.camWorld2d();
    viewport2d.apply();
    shapes.setProjectionMatrix(camWorld2d.combined);
    shapes.begin(type);
    shapesOpen = true;
    shapeType = type;
    host.frameStats().shapeBegins++;
  }

  void endShapes() {
    if (shapesOpen) {
      shapes.end();
      shapesOpen = false;
    }
  }

  void beginSprites() {
    if (spritesOpen) {
      return;
    }
    endShapes();
    if (host.pipeline().current() == RenderPass.OVERLAY) {
      host.applyOverlayViewport();
      sprites.setProjectionMatrix(host.camOverlay().combined);
    } else {
      enterWorld2DPass();
      host.viewport2d().apply();
      sprites.setProjectionMatrix(host.camWorld2d().combined);
    }
    sprites.begin();
    spritesOpen = true;
  }

  void endSprites() {
    if (spritesOpen) {
      sprites.end();
      FrameStats frameStats = host.frameStats();
      frameStats.spriteEnds++;
      frameStats.spriteRenderCalls += sprites.renderCalls;
      spritesOpen = false;
    }
  }

  void setShapeColor(int argb) {
    unpackArgb(argb, tmpColor);
    shapes.setColor(tmpColor);
  }

  /** Honor alpha 0 as transparent (must match {@link InstanceTransform#unpackArgb}). */
  static void unpackArgb(int argb, Color out) {
    out.set(
        ((argb >>> 16) & 0xFF) / 255f,
        ((argb >>> 8) & 0xFF) / 255f,
        (argb & 0xFF) / 255f,
        ((argb >>> 24) & 0xFF) / 255f);
  }

  @Override
  public void dispose() {
    shapes.dispose();
    sprites.dispose();
    if (geometryBatch2D != null) {
      geometryBatch2D.dispose();
      geometryBatch2D = null;
    }
  }
}
