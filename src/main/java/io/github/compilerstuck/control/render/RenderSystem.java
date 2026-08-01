package io.github.compilerstuck.control.render;

import io.github.compilerstuck.control.render.asset.ImageHandle;
import io.github.compilerstuck.control.render.asset.ImageStripRemap;

/**
 * Idiomatic libGDX draw surface for visualizations: batched 2D + instanced-style 3D. Owned by the
 * app frame pipeline: visuals submit geometry; they do not manage cameras, batches, or HUD.
 *
 * <p>World2D: bottom-left Y-up. World3D: center Y-up. Overlay ({@link #drawText}, {@link
 * #drawArgbPixels}): top-left Y-down screen pixels.
 */
public interface RenderSystem {

  void resize(int width, int height);

  void beginFrame();

  void clear(float r, float g, float b);

  /** Ends world (2D/3D) batches before HUD. */
  void endWorld();

  /** Ends HUD / remaining 2D batches for the frame. */
  void endFrame();

  void dispose();

  int getWidth();

  int getHeight();

  float deltaTime();

  /** Recent frames-per-second estimate, or {@code 0} if unknown / unavailable. */
  default int framesPerSecond() {
    return 0;
  }

  /**
   * Active authoring space for subsequent world 2D/3D draws. Overlay text/pixels ignore this and
   * always use screen Y-down.
   */
  void setCoordinateSpace(CoordinateSpace space);

  // --- World2D (bottom-left, Y-up) ---

  /** Packed rects: {@code xywh} = [x,y,w,h] × count; {@code argb} length ≥ count. */
  void fillRects(float[] xywh, int[] argb, int count);

  /** Packed circles: {@code xyd} = [x,y,diameter] × count. */
  void fillCircles(float[] xyd, int[] argb, int count);

  /** Packed lines: {@code xyxy} = [x1,y1,x2,y2] × count. */
  void strokeLines(float[] xyxy, int[] argb, int count);

  /** Packed ellipses: {@code xywh} = [cx,cy,w,h] × count (center + size). */
  void strokeEllipses(float[] xywh, int[] argb, int count);

  void strokeWeight(float weightPx);

  /** Overlay: top-left Y-down screen pixels. */
  void drawText(String text, float x, float y, float sizePx);

  /**
   * Overlay: gray channel (0–255) for subsequent {@link #drawText} / {@link #drawTexts} calls.
   * Default implementations ignore the value (white text).
   */
  default void setOverlayTextGray(int gray0to255) {}

  /**
   * Overlay: advance width of {@code text} at {@code sizePx}. Returns {@code 0} when unknown (e.g.
   * null/empty text).
   */
  default float measureTextWidth(String text, float sizePx) {
    return 0f;
  }

  /**
   * Overlay: draw multiple strings in one SpriteBatch session. {@code ys[i]} is top-left Y (same
   * convention as {@link #drawText}).
   */
  void drawTexts(String[] texts, float x, float[] ys, float sizePx, int count);

  /**
   * Overlay: like {@link #drawTexts(String[], float, float[], float, int)} but each string has its
   * own X (NumberPlot).
   */
  default void drawTexts(String[] texts, float[] xs, float[] ys, float sizePx, int count) {
    if (texts == null || xs == null || ys == null || count <= 0) {
      return;
    }
    int n = Math.min(count, Math.min(texts.length, Math.min(xs.length, ys.length)));
    for (int i = 0; i < n; i++) {
      if (texts[i] != null) {
        drawText(texts[i], xs[i], ys[i], sizePx);
      }
    }
  }

  /** Overlay: full-frame ARGB blit, origin top-left. */
  void drawArgbPixels(int[] argb, int width, int height);

  /**
   * Overlay blit with a content revision. Implementations may skip GPU upload when revision and
   * size match the last draw (idle image visuals).
   */
  default void drawArgbPixels(int[] argb, int width, int height, int contentRevision) {
    drawArgbPixels(argb, width, height);
  }

  /**
   * Overlay image strip remap. {@code stripIndices[i]} is the source strip for destination strip
   * {@code i}. Implementations may cache by {@code contentRevision} (idle frames skip CPU/GPU
   * upload work).
   */
  default void drawImageRemap(
      ImageHandle image,
      int[] stripIndices,
      boolean[] stripHighlight,
      int length,
      boolean horizontal,
      int contentRevision) {
    drawImageRemap(image, stripIndices, stripHighlight, length, horizontal, contentRevision, 1f);
  }

  default void drawImageRemap(
      ImageHandle image,
      int[] stripIndices,
      boolean[] stripHighlight,
      int length,
      boolean horizontal,
      int contentRevision,
      float highlightStrength) {
    if (image == null || stripIndices == null || length <= 0) {
      return;
    }
    int w = image.width();
    int h = image.height();
    int[] src = image.argb();
    int[] dst = new int[w * h];
    ImageStripRemap.remap(
        src, dst, w, h, stripIndices, stripHighlight, length, horizontal, highlightStrength);
    drawArgbPixels(dst, w, h, contentRevision);
  }

  // --- World3D (center, Y-up) ---

  void begin3D();

  void end3D();

  /** Draw unit boxes transformed by instance data (Y-up world positions/euler). */
  void drawBoxes(InstanceData data);

  /** Flat quads in local XY (for planes / pyramid tiles), transformed by instance data. */
  void drawQuads(InstanceData data);

  /** Small spheres / blobs at instance positions (scale.x used as radius). */
  void drawSpheres(InstanceData data);

  /** 3D line segments: {@code xyzxyz} = [x1,y1,z1,x2,y2,z2] × count in world units. */
  void strokeLines3D(float[] xyzxyz, int[] argb, int count);

  /**
   * 3D line segments with optional depth testing. {@code depthTest == false} draws as an overlay
   * (Cube wireframe stays readable over opaque/translucent fills).
   */
  default void strokeLines3D(float[] xyzxyz, int[] argb, int count, boolean depthTest) {
    strokeLines3D(xyzxyz, argb, count);
  }

  /** {@code true} when the caller is already on the thread that owns GL / image GPU resources. */
  default boolean isRenderThread() {
    return true;
  }

  /**
   * Runs {@code action} on the render thread and waits for completion. Headless/fake
   * implementations run immediately on the caller thread.
   */
  default boolean runOnRenderThreadAndWait(java.util.function.BooleanSupplier action) {
    return action != null && action.getAsBoolean();
  }
}
