package io.github.compilerstuck.control.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.util.logging.Logger;

/**
 * World3D draw path: hardware-instanced meshes via {@link InstanceRenderer3D} and strokes via
 * {@link LineRenderer3D}. Requires {@link Gdx#gl30}.
 */
final class GdxWorld3DPass implements Disposable {
  private static final Logger LOGGER = Logger.getLogger(GdxWorld3DPass.class.getName());

  private final GdxRenderSystem host;
  private final InstanceDepthSort depthSort = new InstanceDepthSort();
  private float sceneH = 720f;

  private final InstanceRenderer3D instanceRenderer;
  private final LineRenderer3D lineRenderer;

  GdxWorld3DPass(GdxRenderSystem host) {
    this.host = host;
    if (Gdx.gl30 == null) {
      throw new GdxRuntimeException("World3D requires OpenGL ES 3.0 / GL 3.0+ (Gdx.gl30)");
    }
    instanceRenderer = new InstanceRenderer3D();
    lineRenderer = new LineRenderer3D();
    LOGGER.info("World3D using instanced meshes + LineRenderer3D");
  }

  boolean usesInstancing() {
    return true;
  }

  void resetFrameFlags() {
    // No ModelBatch state to reset.
  }

  /** Engine-owned scene extents + 3D camera framing from window size. */
  void syncEngineScene(float width, float height) {
    float w = Math.max(1f, width);
    float h = Math.max(1f, height);
    sceneH = h;
    instanceRenderer.setSceneSize(w, h);
    update3dCamera();
  }

  private void update3dCamera() {
    PerspectiveCamera cam3d = host.cam3d();
    float eyeZ = (sceneH / 2f) / (float) Math.tan(Math.toRadians(30));
    cam3d.position.set(0f, 0f, eyeZ);
    cam3d.up.set(0f, 1f, 0f);
    cam3d.lookAt(0f, 0f, 0f);
    cam3d.update();
  }

  /** Starts the 3D pass (depth/blend once). */
  void begin3D() {
    GdxWorld2DPass world2d = host.world2d();
    world2d.endShapes();
    world2d.endSprites();
    if (!host.pipeline().enterWorld3D()) {
      return;
    }
    host.applyWorld3DViewport();
    Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
  }

  void end3D() {
    if (!host.pipeline().inWorld3D()) {
      return;
    }
    Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
    host.pipeline().endWorld();
  }

  void drawBoxes(InstanceData data) {
    if (data == null || data.count <= 0) {
      return;
    }
    if (!host.pipeline().inWorld3D()) {
      begin3D();
    }
    PerspectiveCamera cam3d = host.cam3d();
    boolean translucent = InstanceDepthSort.hasTranslucency(data);
    int[] order = null;
    if (translucent) {
      order = depthSort.backToFrontOrder(data, cam3d.position);
      Gdx.gl.glDepthMask(false);
    }
    // Convex boxes: cull back faces so translucent fills do not blend far faces in mesh order.
    // Scoped to boxes only; quads may be viewed from either side.
    boolean cullWasEnabled = Gdx.gl.glIsEnabled(GL20.GL_CULL_FACE);
    Gdx.gl.glEnable(GL20.GL_CULL_FACE);
    Gdx.gl.glCullFace(GL20.GL_BACK);
    try {
      drawInstanced(InstanceRenderer3D.Kind.BOX, data, order);
    } finally {
      if (!cullWasEnabled) {
        Gdx.gl.glDisable(GL20.GL_CULL_FACE);
      }
      if (translucent) {
        Gdx.gl.glDepthMask(true);
      }
    }
  }

  void drawQuads(InstanceData data) {
    drawInstanced(InstanceRenderer3D.Kind.QUAD, data, null);
  }

  void drawSpheres(InstanceData data) {
    drawInstanced(InstanceRenderer3D.Kind.SPHERE, data, null);
  }

  private void drawInstanced(InstanceRenderer3D.Kind kind, InstanceData data, int[] order) {
    if (data == null || data.count <= 0) {
      return;
    }
    if (!host.pipeline().inWorld3D()) {
      begin3D();
    }
    FrameStats frameStats = host.frameStats();
    PerspectiveCamera cam3d = host.cam3d();
    frameStats.instancesSubmitted += data.count;
    if (instanceRenderer.draw(kind, data, cam3d.combined, order)) {
      frameStats.instancedDraws++;
    }
  }

  /** Draws 3D line segments via {@link LineRenderer3D}. */
  void strokeLines3D(float[] xyzxyz, int[] argb, int count) {
    strokeLines3D(xyzxyz, argb, count, true);
  }

  void strokeLines3D(float[] xyzxyz, int[] argb, int count, boolean depthTest) {
    if (count <= 0) {
      return;
    }
    if (!host.pipeline().inWorld3D()) {
      begin3D();
    }
    PerspectiveCamera cam3d = host.cam3d();
    FrameStats frameStats = host.frameStats();
    GdxWorld2DPass world2d = host.world2d();
    boolean depthWasEnabled = Gdx.gl.glIsEnabled(GL20.GL_DEPTH_TEST);
    if (!depthTest) {
      Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
    }
    try {
      float screenH = Math.max(1f, host.getHeight());
      float worldPerPx = sceneH / screenH;
      if (lineRenderer.draw(
          xyzxyz,
          argb,
          count,
          world2d.strokeWeightPx(),
          cam3d.combined,
          cam3d.position,
          worldPerPx)) {
        frameStats.lineDraws++;
      }
    } finally {
      if (!depthTest && depthWasEnabled) {
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
      }
    }
  }

  @Override
  public void dispose() {
    lineRenderer.dispose();
    instanceRenderer.dispose();
  }
}
