package io.github.compilerstuck.control.render;

/**
 * Abstraction over the Processing drawing API used by visualizations. This interface exists so that
 * rendering code can be tested or executed in a headless environment without depending on a real
 * {@code PApplet}.
 */
public interface RenderContext extends ProcessingContext {
  void background(int rgb);

  void fill(int rgb);

  void fill(int rgb, float alpha);

  void textSize(int size);

  void text(String str, float x, float y);

  void stroke(int rgb);

  void stroke(int rgb, float alpha);

  void noStroke();

  void noFill();

  void rect(float x, float y, float w, float h);

  // basic primitives used by visuals
  void line(float x1, float y1, float x2, float y2);

  void line(float x1, float y1, float z1, float x2, float y2, float z2);

  void ellipse(float x, float y, float w, float h);

  void circle(float x, float y, float extent);

  void lights();

  void pushMatrix();

  void popMatrix();

  void translate(float x, float y);

  void translate(float x, float y, float z);

  void rotateX(float angle);

  void rotateY(float angle);

  void rotateZ(float angle);

  void box(float size);

  void box(float w, float h, float d);

  float frameRate();

  void loadPixels();

  void updatePixels();

  int[] pixels();

  int color(float r, float g, float b);

  float red(int argb);

  float green(int argb);

  float blue(int argb);

  LoadedImage loadImage(String path);

  void setResizable(boolean resizable);

  int getWidth();

  int getHeight();
}
