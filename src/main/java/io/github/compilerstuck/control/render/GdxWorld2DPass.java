package io.github.compilerstuck.control.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * World2D draw path: {@link GeometryBatch2D} for colored geometry + {@link SpriteBatch} for text /
 * overlay sprites. Requires {@link Gdx#gl30}.
 */
final class GdxWorld2DPass implements Disposable {
  private final GdxRenderSystem host;
  private final SpriteBatch sprites = new SpriteBatch();
  private final Color tmpColor = new Color();

  private GeometryBatch2D geometryBatch2D;
  private float strokeWeightPx = 1f;
  private boolean spritesOpen;

  GdxWorld2DPass(GdxRenderSystem host) {
    this.host = host;
    if (Gdx.gl30 == null) {
      throw new GdxRuntimeException("World2D requires OpenGL ES 3.0 / GL 3.0+ (Gdx.gl30)");
    }
    geometryBatch2D = new GeometryBatch2D();
  }

  boolean usesGeometryBatch2D() {
    return true;
  }

  float strokeWeightPx() {
    return strokeWeightPx;
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
    endSprites();
    enterWorld2DPass();
    host.viewport2d().apply();
    if (geometryBatch2D.drawRects(xywh, argb, count, camWorld2d.combined)) {
      frameStats.geo2dDraws++;
      frameStats.geo2dPrimitives += count;
    }
  }

  void fillCircles(float[] xyd, int[] argb, int count) {
    if (count <= 0) {
      return;
    }
    OrthographicCamera camWorld2d = host.camWorld2d();
    FrameStats frameStats = host.frameStats();
    endSprites();
    enterWorld2DPass();
    host.viewport2d().apply();
    if (geometryBatch2D.drawCircles(xyd, argb, count, camWorld2d.combined)) {
      frameStats.geo2dDraws++;
      frameStats.geo2dPrimitives += count;
    }
  }

  void strokeLines(float[] xyxy, int[] argb, int count) {
    if (count <= 0) {
      return;
    }
    OrthographicCamera camWorld2d = host.camWorld2d();
    FrameStats frameStats = host.frameStats();
    endSprites();
    enterWorld2DPass();
    host.viewport2d().apply();
    if (geometryBatch2D.drawLines(xyxy, argb, count, strokeWeightPx, camWorld2d.combined)) {
      frameStats.geo2dDraws++;
      frameStats.geo2dPrimitives += count;
    }
  }

  void strokeEllipses(float[] xywh, int[] argb, int count) {
    if (count <= 0) {
      return;
    }
    OrthographicCamera camWorld2d = host.camWorld2d();
    FrameStats frameStats = host.frameStats();
    endSprites();
    enterWorld2DPass();
    host.viewport2d().apply();
    if (geometryBatch2D.drawEllipsesStroke(
        xywh, argb, count, strokeWeightPx, camWorld2d.combined)) {
      frameStats.geo2dDraws++;
      frameStats.geo2dPrimitives += count;
    }
  }

  void enterWorld2DPass() {
    if (host.pipeline().inWorld3D()) {
      host.world3d().end3D();
    }
    host.pipeline().enterWorld2D();
  }

  /** No-op retained so overlay / frame plumbing can flush a retired ShapeRenderer path safely. */
  void endShapes() {}

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
    sprites.dispose();
    if (geometryBatch2D != null) {
      geometryBatch2D.dispose();
      geometryBatch2D = null;
    }
  }
}
