package io.github.compilerstuck.control.render;

import io.github.compilerstuck.control.render.asset.ImageHandle;
import io.github.compilerstuck.control.render.asset.ImageStripRemap;

/** No-op {@link RenderSystem} for headless tests, with submission counters for smoke asserts. */
public final class FakeRenderSystem implements RenderSystem {
  private final int width;
  private final int height;

  private int rectCount;
  private int circleCount;
  private int lineCount;
  private int ellipseCount;
  private int textCount;
  private int pixelUploadCount;
  private int imageRemapCount;
  private int begin3DCount;
  private int boxInstances;
  private int quadInstances;
  private int sphereInstances;
  private int line3DCount;

  private int[] remapScratch;
  private int lastArgbRevision = Integer.MIN_VALUE;
  private int lastRemapRevision = Integer.MIN_VALUE;

  public FakeRenderSystem(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public void resetCounts() {
    rectCount = 0;
    circleCount = 0;
    lineCount = 0;
    ellipseCount = 0;
    textCount = 0;
    pixelUploadCount = 0;
    imageRemapCount = 0;
    begin3DCount = 0;
    boxInstances = 0;
    quadInstances = 0;
    sphereInstances = 0;
    line3DCount = 0;
    lastArgbRevision = Integer.MIN_VALUE;
    lastRemapRevision = Integer.MIN_VALUE;
  }

  public int rectCount() {
    return rectCount;
  }

  public int circleCount() {
    return circleCount;
  }

  public int lineCount() {
    return lineCount;
  }

  public int ellipseCount() {
    return ellipseCount;
  }

  public int textCount() {
    return textCount;
  }

  public int pixelUploadCount() {
    return pixelUploadCount;
  }

  public int imageRemapCount() {
    return imageRemapCount;
  }

  public int begin3DCount() {
    return begin3DCount;
  }

  public int boxInstances() {
    return boxInstances;
  }

  public int quadInstances() {
    return quadInstances;
  }

  public int sphereInstances() {
    return sphereInstances;
  }

  public int line3DCount() {
    return line3DCount;
  }

  public int total3DPrimitives() {
    return boxInstances + quadInstances + sphereInstances + line3DCount;
  }

  @Override
  public void resize(int width, int height) {}

  @Override
  public void beginFrame() {}

  @Override
  public void clear(float r, float g, float b) {}

  @Override
  public void endWorld() {}

  @Override
  public void endFrame() {}

  @Override
  public void dispose() {}

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public float deltaTime() {
    return 1f / 60f;
  }

  @Override
  public void fillRects(float[] xywh, int[] argb, int count) {
    if (count > 0) {
      rectCount += count;
    }
  }

  @Override
  public void fillCircles(float[] xyd, int[] argb, int count) {
    if (count > 0) {
      circleCount += count;
    }
  }

  @Override
  public void strokeLines(float[] xyxy, int[] argb, int count) {
    if (count > 0) {
      lineCount += count;
    }
  }

  @Override
  public void strokeEllipses(float[] xywh, int[] argb, int count) {
    if (count > 0) {
      ellipseCount += count;
    }
  }

  @Override
  public void strokeWeight(float weightPx) {}

  @Override
  public void drawText(String text, float x, float y, float sizePx) {
    if (text != null) {
      textCount++;
    }
  }

  @Override
  public void drawTexts(String[] texts, float x, float[] ys, float sizePx, int count) {
    if (texts == null || count <= 0) {
      return;
    }
    int n = Math.min(count, texts.length);
    for (int i = 0; i < n; i++) {
      if (texts[i] != null) {
        textCount++;
      }
    }
  }

  @Override
  public void drawTexts(String[] texts, float[] xs, float[] ys, float sizePx, int count) {
    if (texts == null || count <= 0) {
      return;
    }
    int n = Math.min(count, texts.length);
    for (int i = 0; i < n; i++) {
      if (texts[i] != null) {
        textCount++;
      }
    }
  }

  @Override
  public void drawArgbPixels(int[] argb, int width, int height) {
    drawArgbPixels(argb, width, height, System.identityHashCode(argb));
  }

  @Override
  public void drawArgbPixels(int[] argb, int width, int height, int contentRevision) {
    if (argb == null || width <= 0 || height <= 0 || argb.length < width * height) {
      return;
    }
    if (lastArgbRevision != contentRevision) {
      lastArgbRevision = contentRevision;
      pixelUploadCount++;
    }
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
    imageRemapCount++;
    int w = image.width();
    int h = image.height();
    int need = w * h;
    if (remapScratch == null || remapScratch.length < need) {
      remapScratch = new int[need];
    }
    if (lastRemapRevision != contentRevision) {
      ImageStripRemap.remap(
          image.argb(), remapScratch, w, h, stripIndices, stripHighlight, length, horizontal);
      lastRemapRevision = contentRevision;
      pixelUploadCount++;
    }
  }

  @Override
  public void begin3D() {
    begin3DCount++;
  }

  @Override
  public void end3D() {}

  @Override
  public void drawBoxes(InstanceData data) {
    if (data != null && data.count > 0) {
      boxInstances += data.count;
    }
  }

  @Override
  public void drawQuads(InstanceData data) {
    if (data != null && data.count > 0) {
      quadInstances += data.count;
    }
  }

  @Override
  public void drawSpheres(InstanceData data) {
    if (data != null && data.count > 0) {
      sphereInstances += data.count;
    }
  }

  @Override
  public void strokeLines3D(float[] xyzxyz, int[] argb, int count) {
    if (count > 0) {
      line3DCount += count;
    }
  }

  @Override
  public void setCoordinateSpace(CoordinateSpace space) {}
}
