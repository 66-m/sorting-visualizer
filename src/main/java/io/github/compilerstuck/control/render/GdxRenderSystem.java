package io.github.compilerstuck.control.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.compilerstuck.control.LaunchArgs;
import io.github.compilerstuck.control.render.asset.AppAssets;
import io.github.compilerstuck.control.render.asset.GdxImageRepository;
import io.github.compilerstuck.control.render.asset.ImageHandle;
import io.github.compilerstuck.control.render.asset.ImageRemapRenderer;
import io.github.compilerstuck.control.render.asset.ImageRepository;
import io.github.compilerstuck.control.render.asset.ImageStripRemap;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Production {@link RenderSystem}: World2D (Y-up) ShapeRenderer/SpriteBatch + instanced World3D
 * meshes (or legacy ModelBatch when {@code --legacy-3d} / no GL30). 3D lines use {@link
 * LineRenderer3D}. Overlay text/pixels use screen Y-down.
 *
 * <p><b>ModelBatch rule (legacy path):</b> do not bind shaders/textures or issue unrelated GL draws
 * between {@link ModelBatch#begin} and {@link ModelBatch#end}. See <a
 * href="https://libgdx.com/wiki/graphics/3d/modelbatch">ModelBatch wiki</a>.
 */
public final class GdxRenderSystem implements RenderSystem, Disposable {
  private static final Logger LOGGER = Logger.getLogger(GdxRenderSystem.class.getName());

  private final OrthographicCamera cam2d = new OrthographicCamera();
  private final ScreenViewport viewport2d = new ScreenViewport(cam2d);
  private final ShapeRenderer shapes = new ShapeRenderer();
  private final SpriteBatch sprites = new SpriteBatch();
  private final AppAssets assets;
  private ImageRepository imageRepository;
  private ImageRemapRenderer imageRemapRenderer;
  private GeometryBatch2D geometryBatch2D;
  private int[] imageRemapScratch;
  private int imageRemapRevision = Integer.MIN_VALUE;

  private final PerspectiveCamera cam3d = new PerspectiveCamera();
  private final Matrix4 tmpMatrix = new Matrix4();
  private final Vector3 tmpPos = new Vector3();
  private final Vector3 tmpPos2 = new Vector3();
  private final Color tmpColor = new Color();

  private final FrameStats frameStats = new FrameStats();
  private final FrameStats lastFrameStats = new FrameStats();
  private final FrameTimeWindow frameTimeWindow = new FrameTimeWindow(120);
  private final FramePipeline pipeline = new FramePipeline();
  private final InstanceTransform sceneTransform = new InstanceTransform();
  private final InstanceDepthSort depthSort = new InstanceDepthSort();
  private float sceneH = 720f;

  /** Phase 2 instancing path; null when using legacy ModelInstance draws. */
  private final InstanceRenderer3D instanceRenderer;

  private final boolean useInstancing;

  /** World-space 3D lines; null only if GL30 / shader init failed. */
  private final LineRenderer3D lineRenderer;

  // Phase 2: remove after soak (kept for --legacy-3d / GL30 fallback).
  private final ModelBatch modelBatch;
  private final Environment environment;
  private final Model boxModel;
  private final Model quadModel;
  private final Model sphereModel;
  private final Array<ModelInstance> instancePool;

  private Pixmap pixelPixmap;
  private Texture pixelTexture;
  private int pixelTexW = -1;
  private int pixelTexH = -1;

  /** Cached remapped overlay texture (Wave D); reused when ARGB unchanged. */
  private Texture overlayArgbTexture;

  private int overlayArgbW = -1;
  private int overlayArgbH = -1;
  private int overlayArgbRevision = Integer.MIN_VALUE;

  private float strokeWeightPx = 1f;
  private boolean shapesOpen;
  private boolean spritesOpen;
  private boolean modelBatchOpen;
  private boolean modelBatchEndedThisFrame;
  private ShapeRenderer.ShapeType shapeType;

  public GdxRenderSystem(AppAssets assets) {
    this.assets = Objects.requireNonNull(assets, "assets");

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

    GeometryBatch2D geo2d = null;
    if (!LaunchArgs.legacy2d() && Gdx.gl30 != null) {
      try {
        geo2d = new GeometryBatch2D();
      } catch (RuntimeException e) {
        LOGGER.log(
            Level.SEVERE, "Failed to init GeometryBatch2D; using ShapeRenderer for circles", e);
      }
    } else if (LaunchArgs.legacy2d()) {
      LOGGER.info("Using legacy 2D ShapeRenderer path (--legacy-2d)");
    }
    geometryBatch2D = geo2d;

    ImageRemapRenderer remap = null;
    if (Gdx.gl30 != null) {
      try {
        remap = new ImageRemapRenderer();
      } catch (RuntimeException e) {
        LOGGER.log(Level.SEVERE, "Failed to init ImageRemapRenderer; using CPU image remap", e);
      }
    }
    imageRemapRenderer = remap;

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

    resize(Math.max(1, Gdx.graphics.getWidth()), Math.max(1, Gdx.graphics.getHeight()));
  }

  /** Stats for the last completed frame (safe to read after {@link #endFrame()}). */
  public FrameStats lastFrameStats() {
    return lastFrameStats;
  }

  @Override
  public int framesPerSecond() {
    return lastFrameStats.fps;
  }

  /** {@code true} when the hardware-instanced 3D path is active. */
  public boolean usesInstancing() {
    return useInstancing;
  }

  /** {@code true} when GeometryBatch2D handles circles/ellipses (not ShapeRenderer legacy). */
  public boolean usesGeometryBatch2D() {
    return geometryBatch2D != null;
  }

  @Override
  public void resize(int width, int height) {
    if (width <= 0 || height <= 0) {
      return;
    }
    viewport2d.update(width, height, true);
    applyWorld2dCamera(width, height);
    cam3d.viewportWidth = width;
    cam3d.viewportHeight = height;
    cam3d.fieldOfView = 60f;
    cam3d.near = 0.1f;
    cam3d.far = Math.max(width, height) * 20f;
    syncEngineScene(width, height);
  }

  @Override
  public void setCoordinateSpace(CoordinateSpace space) {
    // World-only; keep method for Visualization.render compatibility.
  }

  /** Engine-owned scene extents + 3D camera framing from window size. */
  private void syncEngineScene(float width, float height) {
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
    float eyeZ = (sceneH / 2f) / (float) Math.tan(Math.toRadians(30));
    cam3d.position.set(0f, 0f, eyeZ);
    cam3d.up.set(0f, 1f, 0f);
    cam3d.lookAt(0f, 0f, 0f);
    cam3d.update();
  }

  private void applyWorld2dCamera(int width, int height) {
    cam2d.setToOrtho(false, width, height);
    cam2d.update();
  }

  private void applyOverlayCamera() {
    cam2d.setToOrtho(true, Math.max(1, getWidth()), Math.max(1, getHeight()));
    cam2d.update();
  }

  @Override
  public void beginFrame() {
    endShapes();
    endSprites();
    pipeline.beginFrame();
    modelBatchOpen = false;
    modelBatchEndedThisFrame = false;
    frameStats.reset();
    syncEngineScene(getWidth(), getHeight());
  }

  @Override
  public void clear(float r, float g, float b) {
    endShapes();
    endSprites();
    if (pipeline.inWorld3D()) {
      end3D();
    }
    ScreenUtils.clear(r, g, b, 1f, true);
  }

  @Override
  public void endWorld() {
    if (pipeline.inWorld3D()) {
      end3D();
    }
    endShapes();
    endSprites();
    pipeline.endWorld();
  }

  @Override
  public void endFrame() {
    endWorld();
    Gdx.gl.glLineWidth(1f);
    frameStats.frameMs = Gdx.graphics.getDeltaTime() * 1000f;
    frameStats.fps = Gdx.graphics.getFramesPerSecond();
    frameTimeWindow.add(frameStats.frameMs);
    frameStats.avgFrameMs = frameTimeWindow.avgMs();
    frameStats.onePercentLowFps = frameTimeWindow.onePercentLowFps();
    Runtime rt = Runtime.getRuntime();
    frameStats.heapUsedMb = (rt.totalMemory() - rt.freeMemory()) / (1024f * 1024f);
    frameStats.heapMaxMb = rt.maxMemory() / (1024f * 1024f);
    frameStats.copyTo(lastFrameStats);
  }

  @Override
  public int getWidth() {
    return Gdx.graphics.getWidth();
  }

  @Override
  public int getHeight() {
    return Gdx.graphics.getHeight();
  }

  @Override
  public float deltaTime() {
    return Math.min(0.05f, Gdx.graphics.getDeltaTime());
  }

  @Override
  public void fillRects(float[] xywh, int[] argb, int count) {
    if (count <= 0) {
      return;
    }
    beginShapes(ShapeRenderer.ShapeType.Filled);
    for (int i = 0; i < count; i++) {
      setShapeColor(argb[i]);
      int o = i * 4;
      shapes.rect(xywh[o], xywh[o + 1], xywh[o + 2], xywh[o + 3]);
    }
  }

  @Override
  public void fillCircles(float[] xyd, int[] argb, int count) {
    if (count <= 0) {
      return;
    }
    if (geometryBatch2D != null) {
      endShapes();
      endSprites();
      enterWorld2DPass();
      if (geometryBatch2D.drawCircles(xyd, argb, count, cam2d.combined)) {
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

  @Override
  public void strokeLines(float[] xyxy, int[] argb, int count) {
    if (count <= 0) {
      return;
    }
    beginShapes(ShapeRenderer.ShapeType.Line);
    Gdx.gl.glLineWidth(strokeWeightPx);
    for (int i = 0; i < count; i++) {
      setShapeColor(argb[i]);
      int o = i * 4;
      shapes.line(xyxy[o], xyxy[o + 1], xyxy[o + 2], xyxy[o + 3]);
    }
  }

  @Override
  public void strokeEllipses(float[] xywh, int[] argb, int count) {
    if (count <= 0) {
      return;
    }
    if (geometryBatch2D != null) {
      endShapes();
      endSprites();
      enterWorld2DPass();
      if (geometryBatch2D.drawEllipsesStroke(xywh, argb, count, strokeWeightPx, cam2d.combined)) {
        frameStats.geo2dDraws++;
        frameStats.geo2dPrimitives += count;
      }
      return;
    }
    beginShapes(ShapeRenderer.ShapeType.Line);
    Gdx.gl.glLineWidth(strokeWeightPx);
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

  @Override
  public void strokeWeight(float weightPx) {
    strokeWeightPx = Math.max(0.1f, weightPx);
  }

  @Override
  public void drawText(String text, float x, float y, float sizePx) {
    if (text == null) {
      return;
    }
    endShapes();
    enterOverlayPass();
    beginSprites();
    frameStats.textDraws++;
    BitmapFont font = assets.font(sizePx);
    font.setColor(Color.WHITE);
    float drawY = y - font.getCapHeight();
    font.draw(sprites, text, x, drawY);
  }

  @Override
  public void drawTexts(String[] texts, float x, float[] ys, float sizePx, int count) {
    if (texts == null || ys == null || count <= 0) {
      return;
    }
    endShapes();
    enterOverlayPass();
    beginSprites();
    BitmapFont font = assets.font(sizePx);
    font.setColor(Color.WHITE);
    float cap = font.getCapHeight();
    int n = Math.min(count, Math.min(texts.length, ys.length));
    for (int i = 0; i < n; i++) {
      String text = texts[i];
      if (text == null) {
        continue;
      }
      frameStats.textDraws++;
      font.draw(sprites, text, x, ys[i] - cap);
    }
  }

  @Override
  public void drawTexts(String[] texts, float[] xs, float[] ys, float sizePx, int count) {
    if (texts == null || xs == null || ys == null || count <= 0) {
      return;
    }
    endShapes();
    enterOverlayPass();
    beginSprites();
    BitmapFont font = assets.font(sizePx);
    font.setColor(Color.WHITE);
    float cap = font.getCapHeight();
    int n = Math.min(count, Math.min(texts.length, Math.min(xs.length, ys.length)));
    for (int i = 0; i < n; i++) {
      String text = texts[i];
      if (text == null) {
        continue;
      }
      frameStats.textDraws++;
      font.draw(sprites, text, xs[i], ys[i] - cap);
    }
  }

  public void setImageRepository(ImageRepository imageRepository) {
    this.imageRepository = imageRepository;
  }

  @Override
  public void drawImageRemap(
      ImageHandle image,
      int[] stripIndices,
      boolean[] stripHighlight,
      int length,
      boolean horizontal,
      int contentRevision) {
    if (image == null || stripIndices == null || length <= 0) {
      return;
    }
    Texture source = null;
    if (imageRepository instanceof GdxImageRepository gdxRepo && gdxRepo.current() == image) {
      source = gdxRepo.sourceTexture();
    }
    if (source != null && imageRemapRenderer != null) {
      endShapes();
      endSprites();
      enterOverlayPass();
      boolean uploaded =
          imageRemapRenderer.uploadIndicesIfNeeded(
              stripIndices, stripHighlight, length, contentRevision);
      if (uploaded) {
        frameStats.pixelUploads++;
      }
      imageRemapRenderer.draw(source, horizontal, length);
      return;
    }
    // CPU fallback (no GL30 / missing source texture)
    int w = image.width();
    int h = image.height();
    int need = w * h;
    if (imageRemapScratch == null || imageRemapScratch.length < need) {
      imageRemapScratch = new int[need];
    }
    if (imageRemapRevision != contentRevision) {
      ImageStripRemap.remap(
          image.argb(), imageRemapScratch, w, h, stripIndices, stripHighlight, length, horizontal);
      imageRemapRevision = contentRevision;
    }
    drawArgbPixels(imageRemapScratch, w, h, contentRevision);
  }

  @Override
  public void drawArgbPixels(int[] argb, int width, int height) {
    drawArgbPixels(argb, width, height, System.identityHashCode(argb));
  }

  /**
   * Overlay blit. When {@code contentRevision} matches the last upload for this size, skips CPU→GPU
   * upload and redraws the cached texture ({@code pixelUploads} stays unchanged).
   */
  public void drawArgbPixels(int[] argb, int width, int height, int contentRevision) {
    if (argb == null || width <= 0 || height <= 0 || argb.length < width * height) {
      return;
    }
    endShapes();
    enterOverlayPass();
    boolean needUpload =
        overlayArgbTexture == null
            || overlayArgbW != width
            || overlayArgbH != height
            || overlayArgbRevision != contentRevision;
    if (needUpload) {
      ensurePixelSurface(width, height);
      ByteBuffer pixels = pixelPixmap.getPixels();
      pixels.clear();
      int count = width * height;
      for (int i = 0; i < count; i++) {
        int c = argb[i];
        int a = (c >>> 24) & 0xFF;
        if (a == 0) {
          a = 0xFF;
        }
        pixels.put((byte) ((c >>> 16) & 0xFF));
        pixels.put((byte) ((c >>> 8) & 0xFF));
        pixels.put((byte) (c & 0xFF));
        pixels.put((byte) a);
      }
      pixels.flip();
      pixelTexture.draw(pixelPixmap, 0, 0);
      if (overlayArgbTexture != null && overlayArgbTexture != pixelTexture) {
        overlayArgbTexture.dispose();
      }
      overlayArgbTexture = pixelTexture;
      overlayArgbW = width;
      overlayArgbH = height;
      overlayArgbRevision = contentRevision;
      frameStats.pixelUploads++;
    }
    beginSprites();
    Texture tex = overlayArgbTexture != null ? overlayArgbTexture : pixelTexture;
    sprites.draw(tex, 0, 0, width, height, 0, 0, width, height, false, true);
  }

  /** Draws a pre-uploaded Overlay texture (image remap GPU path). */
  public void drawOverlayTexture(Texture texture, float x, float y, float w, float h) {
    if (texture == null) {
      return;
    }
    endShapes();
    enterOverlayPass();
    beginSprites();
    sprites.draw(texture, x, y, w, h, 0, 0, texture.getWidth(), texture.getHeight(), false, true);
  }

  /**
   * Starts the 3D pass (depth/blend once). ModelBatch on the legacy path opens lazily on the first
   * mesh draw so {@link #strokeLines3D} can run without interrupting it.
   */
  @Override
  public void begin3D() {
    endShapes();
    endSprites();
    if (!pipeline.enterWorld3D()) {
      return;
    }
    Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
  }

  @Override
  public void end3D() {
    if (!pipeline.inWorld3D()) {
      return;
    }
    if (!useInstancing && modelBatchOpen) {
      endModelBatch();
    }
    Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
    pipeline.endWorld();
  }

  @Override
  public void drawBoxes(InstanceData data) {
    if (data == null || data.count <= 0) {
      return;
    }
    if (!pipeline.inWorld3D()) {
      begin3D();
    }
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

  @Override
  public void drawQuads(InstanceData data) {
    drawInstancedOrLegacy(InstanceRenderer3D.Kind.QUAD, quadModel, data, null);
  }

  @Override
  public void drawSpheres(InstanceData data) {
    drawInstancedOrLegacy(InstanceRenderer3D.Kind.SPHERE, sphereModel, data, null);
  }

  private void drawInstancedOrLegacy(
      InstanceRenderer3D.Kind kind, Model legacyModel, InstanceData data, int[] order) {
    if (data == null || data.count <= 0) {
      return;
    }
    if (!pipeline.inWorld3D()) {
      begin3D();
    }
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
  @Override
  public void strokeLines3D(float[] xyzxyz, int[] argb, int count) {
    strokeLines3D(xyzxyz, argb, count, true);
  }

  @Override
  public void strokeLines3D(float[] xyzxyz, int[] argb, int count, boolean depthTest) {
    if (count <= 0) {
      return;
    }
    if (!pipeline.inWorld3D()) {
      begin3D();
    }
    boolean depthWasEnabled = Gdx.gl.glIsEnabled(GL20.GL_DEPTH_TEST);
    if (!depthTest) {
      Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
    }
    try {
      if (lineRenderer != null) {
        if (lineRenderer.draw(xyzxyz, argb, count, strokeWeightPx, cam3d.combined)) {
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
      Gdx.gl.glLineWidth(strokeWeightPx);
      for (int i = 0; i < count; i++) {
        setShapeColor(argb[i]);
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
      frameStats.modelBatchRestarts++;
    }
    modelBatch.begin(cam3d);
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
    if (!pipeline.inWorld3D()) {
      begin3D();
    }
    if (!modelBatchOpen) {
      beginModelBatch();
    }
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
      unpackArgb(data.argb[i], tmpColor);
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

  private void setShapeColor(int argb) {
    unpackArgb(argb, tmpColor);
    shapes.setColor(tmpColor);
  }

  private static void unpackArgb(int argb, Color out) {
    // Honor alpha 0 as transparent (must match InstanceTransform.unpackArgb).
    out.set(
        ((argb >>> 16) & 0xFF) / 255f,
        ((argb >>> 8) & 0xFF) / 255f,
        (argb & 0xFF) / 255f,
        ((argb >>> 24) & 0xFF) / 255f);
  }

  private void enterWorld2DPass() {
    if (pipeline.inWorld3D()) {
      end3D();
    }
    pipeline.enterWorld2D();
  }

  private void enterOverlayPass() {
    if (pipeline.inWorld3D()) {
      end3D();
    }
    endShapes();
    endSprites();
    pipeline.enterOverlay();
    applyOverlayCamera();
  }

  private void beginShapes(ShapeRenderer.ShapeType type) {
    enterWorld2DPass();
    endSprites();
    if (shapesOpen && shapeType == type) {
      return;
    }
    endShapes();
    viewport2d.apply();
    applyWorld2dCamera(getWidth(), getHeight());
    shapes.setProjectionMatrix(cam2d.combined);
    shapes.begin(type);
    shapesOpen = true;
    shapeType = type;
    frameStats.shapeBegins++;
  }

  private void endShapes() {
    if (shapesOpen) {
      shapes.end();
      shapesOpen = false;
    }
  }

  private void beginSprites() {
    if (spritesOpen) {
      return;
    }
    if (pipeline.current() != RenderPass.OVERLAY) {
      enterWorld2DPass();
      applyWorld2dCamera(getWidth(), getHeight());
    } else {
      applyOverlayCamera();
    }
    endShapes();
    viewport2d.apply();
    sprites.setProjectionMatrix(cam2d.combined);
    sprites.begin();
    spritesOpen = true;
  }

  private void endSprites() {
    if (spritesOpen) {
      sprites.end();
      frameStats.spriteEnds++;
      frameStats.spriteRenderCalls += sprites.renderCalls;
      spritesOpen = false;
    }
  }

  private void ensurePixelSurface(int width, int height) {
    if (pixelPixmap != null && pixelTexW == width && pixelTexH == height) {
      return;
    }
    if (pixelTexture != null) {
      if (overlayArgbTexture == pixelTexture) {
        overlayArgbTexture = null;
      }
      pixelTexture.dispose();
    }
    if (pixelPixmap != null) {
      pixelPixmap.dispose();
    }
    pixelPixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
    pixelTexture = new Texture(pixelPixmap);
    pixelTexW = width;
    pixelTexH = height;
    overlayArgbRevision = Integer.MIN_VALUE;
  }

  @Override
  public void dispose() {
    endFrame();
    shapes.dispose();
    sprites.dispose();
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
    if (pixelTexture != null) {
      pixelTexture.dispose();
      pixelTexture = null;
    }
    if (pixelPixmap != null) {
      pixelPixmap.dispose();
      pixelPixmap = null;
    }
    overlayArgbTexture = null;
    if (imageRemapRenderer != null) {
      imageRemapRenderer.dispose();
      imageRemapRenderer = null;
    }
    if (geometryBatch2D != null) {
      geometryBatch2D.dispose();
      geometryBatch2D = null;
    }
  }
}
