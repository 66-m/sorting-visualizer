package io.github.compilerstuck.control.render;

/**
 * A trivial {@link RenderContext} implementation that does nothing. Useful for unit tests or
 * running the application in a headless environment.
 */
public class HeadlessRenderContext implements RenderContext {
  private final int width;
  private final int height;
  private final int[] pixels;

  public HeadlessRenderContext(int width, int height) {
    this.width = width;
    this.height = height;
    this.pixels = width > 0 && height > 0 ? new int[width * height] : new int[0];
  }

  @Override
  public void delay(int ms) {
    // no-op
  }

  @Override
  public void background(int rgb) {
    // no-op
  }

  @Override
  public void fill(int rgb) {
    // no-op
  }

  @Override
  public void fill(int rgb, float alpha) {
    // no-op
  }

  @Override
  public void textSize(int size) {
    // no-op
  }

  @Override
  public void text(String str, float x, float y) {
    // no-op
  }

  @Override
  public void stroke(int rgb) {
    // no-op
  }

  @Override
  public void stroke(int rgb, float alpha) {
    // no-op
  }

  @Override
  public void noStroke() {
    // no-op
  }

  @Override
  public void noFill() {
    // no-op
  }

  @Override
  public void rect(float x, float y, float w, float h) {
    // no-op
  }

  @Override
  public void line(float x1, float y1, float x2, float y2) {
    // no-op
  }

  @Override
  public void line(float x1, float y1, float z1, float x2, float y2, float z2) {
    // no-op
  }

  @Override
  public void ellipse(float x, float y, float w, float h) {
    // no-op
  }

  @Override
  public void circle(float x, float y, float extent) {
    // no-op
  }

  @Override
  public void lights() {
    // no-op
  }

  @Override
  public void pushMatrix() {
    // no-op
  }

  @Override
  public void popMatrix() {
    // no-op
  }

  @Override
  public void translate(float x, float y) {
    // no-op
  }

  @Override
  public void translate(float x, float y, float z) {
    // no-op
  }

  @Override
  public void rotateX(float angle) {
    // no-op
  }

  @Override
  public void rotateY(float angle) {
    // no-op
  }

  @Override
  public void rotateZ(float angle) {
    // no-op
  }

  @Override
  public void box(float size) {
    // no-op
  }

  @Override
  public void box(float w, float h, float d) {
    // no-op
  }

  @Override
  public float frameRate() {
    return 60f;
  }

  @Override
  public void loadPixels() {
    // no-op
  }

  @Override
  public void updatePixels() {
    // no-op
  }

  @Override
  public int[] pixels() {
    return pixels;
  }

  @Override
  public int color(float r, float g, float b) {
    return 0xFF000000 | (((int) r & 0xFF) << 16) | (((int) g & 0xFF) << 8) | ((int) b & 0xFF);
  }

  @Override
  public float red(int argb) {
    return (argb >> 16) & 0xFF;
  }

  @Override
  public float green(int argb) {
    return (argb >> 8) & 0xFF;
  }

  @Override
  public float blue(int argb) {
    return argb & 0xFF;
  }

  @Override
  public LoadedImage loadImage(String path) {
    return new DummyLoadedImage();
  }

  @Override
  public void setResizable(boolean resizable) {
    // no-op
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  private static final class DummyLoadedImage implements LoadedImage {
    private final int[] pixels = new int[1];

    @Override
    public int pixelWidth() {
      return 1;
    }

    @Override
    public int pixelHeight() {
      return 1;
    }

    @Override
    public int[] pixels() {
      return pixels;
    }

    @Override
    public void loadPixels() {
      // no-op
    }

    @Override
    public void resize(int w, int h) {
      // no-op
    }
  }
}
