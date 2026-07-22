package io.github.compilerstuck.control.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import io.github.compilerstuck.control.LaunchArgs;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * World3D draw path: instanced meshes (or legacy ModelBatch when {@code --legacy-3d} / no GL30),
 * plus {@link LineRenderer3D} strokes.
 *
 * <p><b>ModelBatch rule (legacy path):</b> do not bind shaders/textures or issue unrelated GL draws
 * between {@link ModelBatch#begin} and {@link ModelBatch#end}. See <a
 * href="https://libgdx.com/wiki/graphics/3d/modelbatch">ModelBatch wiki</a>.
 */
final class GdxWorld3DPass implements Disposable {
  private static final Logger LOGGER = Logger.getLogger(GdxWorld3DPass.class.getName());

  private final GdxRenderSystem host;
  private final Matrix4 tmpMatrix = new Matrix4();
  private final Vector3 tmpPos = new Vector3();
  private final Vector3 tmpPos2 = new Vector3();
  private final Color tmpColor = new Color();
  private final InstanceTransform sceneTransform = new InstanceTransform();
  private final InstanceDepthSort depthSort = new InstanceDepthSort();
  private float sceneH = 720f;

  /** Instancing path; null when using legacy ModelInstance draws. */
  private final InstanceRenderer3D instanceRenderer;

  private final boolean useInstancing;

  /** World-space 3D lines; null only if GL30 / shader init failed. */
  private final LineRenderer3D lineRenderer;

  // Legacy ModelBatch path (--legacy-3d / GL30 fallback).
  private final ModelBatch modelBatch;
  private final Environment environment;
  private final Model boxModel;
  private final Model quadModel;
  private final Model sphereModel;
  private final Array<ModelInstance> instancePool;

  private boolean modelBatchOpen;
  private boolean modelBatchEndedThisFrame;

  GdxWorld3DPass(GdxRenderSystem host) {
    this.host = host;

    InstanceRenderer3D instanced = null;
    boolean instancing = false;
    if (!LaunchArgs.legacy3d() && Gdx.gl30 != null) {
      try {
        instanced = new InstanceRenderer3D();
        instancing = true;
      } catch (RuntimeException e) {
        LOGGER.log(Level.SEVERE, "Failed to init InstanceRenderer3D; falling back to legacy 3D", e);
      }
    } else if (LaunchArgs.legacy3d()) {
      LOGGER.info("Using legacy 3D path (--legacy-3d)");
    } else {
      LOGGER.severe("Gdx.gl30 is null; using legacy 3D path");
    }
    instanceRenderer = instanced;
    useInstancing = instancing;

    LineRenderer3D lines = null;
    if (Gdx.gl30 != null) {
      try {
        lines = new LineRenderer3D();
      } catch (RuntimeException e) {
        LOGGER.log(
            Level.SEVERE, "Failed to init LineRenderer3D; 3D lines will use ShapeRenderer", e);
      }
    }
    lineRenderer = lines;

    if (useInstancing) {
      modelBatch = null;
      environment = null;
      boxModel = null;
      quadModel = null;
      sphereModel = null;
      instancePool = null;
    } else {
      modelBatch = new ModelBatch();
      environment = new Environment();
      environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
      environment.add(new DirectionalLight().set(0.85f, 0.85f, 0.85f, -0.4f, -0.8f, -0.3f));
      ModelBuilder mb = new ModelBuilder();
      Material mat =
          new Material(
              ColorAttribute.createDiffuse(Color.WHITE),
              new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
      long attrs = Usage.Position | Usage.Normal | Usage.ColorPacked;
      boxModel = mb.createBox(1f, 1f, 1f, mat, attrs);
      quadModel =
          mb.createRect(
              -0.5f, -0.5f, 0f, 0.5f, -0.5f, 0f, 0.5f, 0.5f, 0f, -0.5f, 0.5f, 0f, 0f, 0f, 1f, mat,
              attrs);
      sphereModel = mb.createSphere(1f, 1f, 1f, 12, 12, mat, attrs);
      instancePool = new Array<>();
    }
  }

  boolean usesInstancing() {
    return useInstancing;
  }

  void resetFrameFlags() {
    modelBatchOpen = false;
    modelBatchEndedThisFrame = false;
  }

  /** Engine-owned scene extents + 3D camera framing from window size. */
  void syncEngineScene(float width, float height) {
    float w = Math.max(1f, width);
    float h = Math.max(1f, height);
    sceneH = h;
    sceneTransform.setSceneSize(w, h);
    if (instanceRenderer != null) {
      instanceRenderer.setSceneSize(w, h);
    }
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

  /**
   * Starts the 3D pass (depth/blend once). ModelBatch on the legacy path opens lazily on the first
   * mesh draw so {@link #strokeLines3D} can run without interrupting it.
   */
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
    if (!useInstancing && modelBatchOpen) {
      endModelBatch();
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
      drawInstancedOrLegacy(InstanceRenderer3D.Kind.BOX, boxModel, data, order);
      // Legacy ModelBatch queues until end(); flush while depth mask is still off.
      if (translucent && !useInstancing && modelBatchOpen) {
        endModelBatch();
      }
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
    drawInstancedOrLegacy(InstanceRenderer3D.Kind.QUAD, quadModel, data, null);
  }

  void drawSpheres(InstanceData data) {
    drawInstancedOrLegacy(InstanceRenderer3D.Kind.SPHERE, sphereModel, data, null);
  }

  private void drawInstancedOrLegacy(
      InstanceRenderer3D.Kind kind, Model legacyModel, InstanceData data, int[] order) {
    if (data == null || data.count <= 0) {
      return;
    }
    if (!host.pipeline().inWorld3D()) {
      begin3D();
    }
    FrameStats frameStats = host.frameStats();
    PerspectiveCamera cam3d = host.cam3d();
    if (useInstancing) {
      frameStats.instancesSubmitted += data.count;
      if (instanceRenderer.draw(kind, data, cam3d.combined, order)) {
        frameStats.instancedDraws++;
      }
    } else {
      drawModelInstances(legacyModel, data, order);
    }
  }

  /**
   * Draws 3D line segments via {@link LineRenderer3D} without ending/restarting ModelBatch or using
   * ShapeRenderer with {@code cam3d}.
   */
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
    ShapeRenderer shapes = world2d.shapes();
    boolean depthWasEnabled = Gdx.gl.glIsEnabled(GL20.GL_DEPTH_TEST);
    if (!depthTest) {
      Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
    }
    try {
      if (lineRenderer != null) {
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
        return;
      }
      // Rare fallback (no GL30 / shader init failed): ShapeRenderer; do not restart ModelBatch.
      if (!useInstancing && modelBatchOpen) {
        endModelBatch();
      }
      shapes.setProjectionMatrix(cam3d.combined);
      shapes.identity();
      shapes.begin(ShapeRenderer.ShapeType.Line);
      frameStats.shapeBegins++;
      frameStats.lineDraws++;
      for (int i = 0; i < count; i++) {
        world2d.setShapeColor(argb[i]);
        int o = i * 6;
        tmpPos.set(xyzxyz[o], xyzxyz[o + 1], xyzxyz[o + 2]);
        tmpPos2.set(xyzxyz[o + 3], xyzxyz[o + 4], xyzxyz[o + 5]);
        shapes.line(tmpPos.x, tmpPos.y, tmpPos.z, tmpPos2.x, tmpPos2.y, tmpPos2.z);
      }
      shapes.end();
    } finally {
      if (!depthTest && depthWasEnabled) {
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
      }
    }
  }

  private void beginModelBatch() {
    if (modelBatch == null) {
      return;
    }
    if (modelBatchEndedThisFrame) {
      host.frameStats().modelBatchRestarts++;
    }
    modelBatch.begin(host.cam3d());
    modelBatchOpen = true;
    modelBatchEndedThisFrame = false;
  }

  private void endModelBatch() {
    if (modelBatch == null || !modelBatchOpen) {
      return;
    }
    modelBatch.end();
    modelBatchOpen = false;
    modelBatchEndedThisFrame = true;
  }

  private void drawModelInstances(Model model, InstanceData data, int[] order) {
    if (data == null || data.count <= 0) {
      return;
    }
    if (!host.pipeline().inWorld3D()) {
      begin3D();
    }
    if (!modelBatchOpen) {
      beginModelBatch();
    }
    FrameStats frameStats = host.frameStats();
    frameStats.instancesSubmitted += data.count;
    while (instancePool.size < data.count) {
      instancePool.add(new ModelInstance(model));
    }
    for (int k = 0; k < data.count; k++) {
      int i = order != null ? order[k] : k;
      ModelInstance inst = instancePool.get(i);
      if (inst.model != model) {
        inst = new ModelInstance(model);
        instancePool.set(i, inst);
      }
      sceneTransform.buildMatrix(data, i, tmpMatrix);
      inst.transform.set(tmpMatrix);
      GdxWorld2DPass.unpackArgb(data.argb[i], tmpColor);
      Material mat = inst.materials.get(0);
      ColorAttribute diffuse = mat.get(ColorAttribute.class, ColorAttribute.Diffuse);
      if (diffuse == null) {
        diffuse = new ColorAttribute(ColorAttribute.Diffuse, tmpColor);
        mat.set(diffuse);
      } else {
        diffuse.color.set(tmpColor);
      }
      BlendingAttribute blend = mat.get(BlendingAttribute.class, BlendingAttribute.Type);
      if (blend != null) {
        blend.opacity = tmpColor.a;
      }
      modelBatch.render(inst, environment);
      frameStats.modelRenders++;
    }
  }

  @Override
  public void dispose() {
    if (lineRenderer != null) {
      lineRenderer.dispose();
    }
    if (instanceRenderer != null) {
      instanceRenderer.dispose();
    }
    if (modelBatch != null) {
      modelBatch.dispose();
    }
    if (boxModel != null) {
      boxModel.dispose();
    }
    if (quadModel != null) {
      quadModel.dispose();
    }
    if (sphereModel != null) {
      sphereModel.dispose();
    }
  }
}
