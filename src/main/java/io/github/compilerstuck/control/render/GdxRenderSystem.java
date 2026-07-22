package io.github.compilerstuck.control.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.compilerstuck.control.render.asset.AppAssets;
import io.github.compilerstuck.control.render.asset.ImageHandle;
import io.github.compilerstuck.control.render.asset.ImageRepository;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Production {@link RenderSystem}: World2D (Y-up) ShapeRenderer/SpriteBatch + instanced World3D
 * meshes (or legacy ModelBatch when {@code --legacy-3d} / no GL30). 3D lines use {@link
 * LineRenderer3D}. Overlay text/pixels use a separate Y-down camera.
 *
 * <p>Delegates draw work to package-private pass helpers ({@link GdxWorld2DPass}, {@link
 * GdxWorld3DPass}, {@link GdxOverlayPass}) while owning cameras, {@link FramePipeline}, resize, and
 * dispose.
 *
 * <p><b>ModelBatch rule (legacy path):</b> do not bind shaders/textures or issue unrelated GL draws
 * between ModelBatch begin/end. See <a
 * href="https://libgdx.com/wiki/graphics/3d/modelbatch">ModelBatch wiki</a>.
 */
public final class GdxRenderSystem implements RenderSystem, Disposable {
  private static final Logger LOGGER = Logger.getLogger(GdxRenderSystem.class.getName());

  private final OrthographicCamera camWorld2d = new OrthographicCamera();
  private final OrthographicCamera camOverlay = new OrthographicCamera();
  private final ScreenViewport viewport2d = new ScreenViewport(camWorld2d);
  private final PerspectiveCamera cam3d = new PerspectiveCamera();

  private final FrameStats frameStats = new FrameStats();
  private final FrameStats lastFrameStats = new FrameStats();
  private final FrameTimeWindow frameTimeWindow = new FrameTimeWindow(120);
  private final FramePipeline pipeline = new FramePipeline();

  private final GdxWorld2DPass world2d;
  private final GdxWorld3DPass world3d;
  private final GdxOverlayPass overlay;

  private volatile Thread renderThread;

  public GdxRenderSystem(AppAssets assets) {
    Objects.requireNonNull(assets, "assets");
    world2d = new GdxWorld2DPass(this);
    world3d = new GdxWorld3DPass(this);
    overlay = new GdxOverlayPass(this, assets);
    resize(Math.max(1, Gdx.graphics.getWidth()), Math.max(1, Gdx.graphics.getHeight()));
  }

  OrthographicCamera camWorld2d() {
    return camWorld2d;
  }

  OrthographicCamera camOverlay() {
    return camOverlay;
  }

  ScreenViewport viewport2d() {
    return viewport2d;
  }

  PerspectiveCamera cam3d() {
    return cam3d;
  }

  FramePipeline pipeline() {
    return pipeline;
  }

  FrameStats frameStats() {
    return frameStats;
  }

  GdxWorld2DPass world2d() {
    return world2d;
  }

  GdxWorld3DPass world3d() {
    return world3d;
  }

  GdxOverlayPass overlay() {
    return overlay;
  }

  /** Full-framebuffer viewport for overlay draws (Y-down camera). */
  void applyOverlayViewport() {
    int w = Math.max(1, getWidth());
    int h = Math.max(1, getHeight());
    Gdx.gl.glViewport(0, 0, w, h);
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
    return world3d.usesInstancing();
  }

  /**
   * {@code true} when GeometryBatch2D handles rects/circles/ellipses (not ShapeRenderer legacy).
   */
  public boolean usesGeometryBatch2D() {
    return world2d.usesGeometryBatch2D();
  }

  @Override
  public void resize(int width, int height) {
    if (width <= 0 || height <= 0) {
      return;
    }
    viewport2d.update(width, height, true);
    camWorld2d.setToOrtho(false, width, height);
    camWorld2d.update();
    camOverlay.setToOrtho(true, width, height);
    camOverlay.update();
    cam3d.viewportWidth = width;
    cam3d.viewportHeight = height;
    cam3d.fieldOfView = 60f;
    cam3d.near = 0.1f;
    cam3d.far = Math.max(width, height) * 20f;
    world3d.syncEngineScene(width, height);
  }

  @Override
  public void setCoordinateSpace(CoordinateSpace space) {
    // World-only; keep method for Visualization.render compatibility.
  }

  @Override
  public void beginFrame() {
    renderThread = Thread.currentThread();
    world2d.endShapes();
    world2d.endSprites();
    pipeline.beginFrame();
    world3d.resetFrameFlags();
    frameStats.reset();
    world3d.syncEngineScene(getWidth(), getHeight());
  }

  @Override
  public boolean isRenderThread() {
    Thread t = renderThread;
    return t != null && Thread.currentThread() == t;
  }

  @Override
  public boolean runOnRenderThreadAndWait(BooleanSupplier action) {
    if (action == null) {
      return false;
    }
    if (isRenderThread() || Gdx.app == null) {
      return action.getAsBoolean();
    }
    AtomicBoolean result = new AtomicBoolean(false);
    CountDownLatch latch = new CountDownLatch(1);
    Gdx.app.postRunnable(
        () -> {
          try {
            result.set(action.getAsBoolean());
          } finally {
            latch.countDown();
          }
        });
    try {
      if (!latch.await(60, TimeUnit.SECONDS)) {
        LOGGER.log(Level.WARNING, "Timed out waiting for render-thread image work");
        return false;
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
    return result.get();
  }

  @Override
  public void clear(float r, float g, float b) {
    world2d.endShapes();
    world2d.endSprites();
    if (pipeline.inWorld3D()) {
      world3d.end3D();
    }
    ScreenUtils.clear(r, g, b, 1f, true);
  }

  @Override
  public void endWorld() {
    if (pipeline.inWorld3D()) {
      world3d.end3D();
    }
    world2d.endShapes();
    world2d.endSprites();
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
    world2d.fillRects(xywh, argb, count);
  }

  @Override
  public void fillCircles(float[] xyd, int[] argb, int count) {
    world2d.fillCircles(xyd, argb, count);
  }

  @Override
  public void strokeLines(float[] xyxy, int[] argb, int count) {
    world2d.strokeLines(xyxy, argb, count);
  }

  @Override
  public void strokeEllipses(float[] xywh, int[] argb, int count) {
    world2d.strokeEllipses(xywh, argb, count);
  }

  @Override
  public void strokeWeight(float weightPx) {
    world2d.strokeWeight(weightPx);
  }

  @Override
  public void drawText(String text, float x, float y, float sizePx) {
    overlay.drawText(text, x, y, sizePx);
  }

  @Override
  public void drawTexts(String[] texts, float x, float[] ys, float sizePx, int count) {
    overlay.drawTexts(texts, x, ys, sizePx, count);
  }

  @Override
  public void drawTexts(String[] texts, float[] xs, float[] ys, float sizePx, int count) {
    overlay.drawTexts(texts, xs, ys, sizePx, count);
  }

  public void setImageRepository(ImageRepository imageRepository) {
    overlay.setImageRepository(imageRepository);
  }

  @Override
  public void drawImageRemap(
      ImageHandle image,
      int[] stripIndices,
      boolean[] stripHighlight,
      int length,
      boolean horizontal,
      int contentRevision) {
    overlay.drawImageRemap(
        image, stripIndices, stripHighlight, length, horizontal, contentRevision);
  }

  @Override
  public void drawImageRemap(
      ImageHandle image,
      int[] stripIndices,
      boolean[] stripHighlight,
      int length,
      boolean horizontal,
      int contentRevision,
      float highlightStrength) {
    overlay.drawImageRemap(
        image,
        stripIndices,
        stripHighlight,
        length,
        horizontal,
        contentRevision,
        highlightStrength);
  }

  @Override
  public void drawArgbPixels(int[] argb, int width, int height) {
    overlay.drawArgbPixels(argb, width, height);
  }

  @Override
  public void drawArgbPixels(int[] argb, int width, int height, int contentRevision) {
    overlay.drawArgbPixels(argb, width, height, contentRevision);
  }

  /** Draws a pre-uploaded Overlay texture (image remap GPU path). */
  public void drawOverlayTexture(Texture texture, float x, float y, float w, float h) {
    overlay.drawOverlayTexture(texture, x, y, w, h);
  }

  @Override
  public void begin3D() {
    world3d.begin3D();
  }

  @Override
  public void end3D() {
    world3d.end3D();
  }

  @Override
  public void drawBoxes(InstanceData data) {
    world3d.drawBoxes(data);
  }

  @Override
  public void drawQuads(InstanceData data) {
    world3d.drawQuads(data);
  }

  @Override
  public void drawSpheres(InstanceData data) {
    world3d.drawSpheres(data);
  }

  @Override
  public void strokeLines3D(float[] xyzxyz, int[] argb, int count) {
    world3d.strokeLines3D(xyzxyz, argb, count);
  }

  @Override
  public void strokeLines3D(float[] xyzxyz, int[] argb, int count, boolean depthTest) {
    world3d.strokeLines3D(xyzxyz, argb, count, depthTest);
  }

  @Override
  public void dispose() {
    endFrame();
    world2d.dispose();
    world3d.dispose();
    overlay.dispose();
  }
}
