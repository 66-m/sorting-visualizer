package io.github.compilerstuck.Control;

/**
 * Abstraction over the minimal subset of Processing's drawing API used by
 * visualizations.  This interface exists so that rendering code can be tested
 * or executed in a headless environment without depending on a real
 * {@code PApplet}.
 */
public interface RenderContext extends ProcessingContext {
    void background(int rgb);
    void fill(int rgb);
    void textSize(int size);
    void text(String str, float x, float y);
    void stroke(int rgb);
    void rect(float x, float y, float w, float h);

    // basic primitives used by visuals
    void line(float x1, float y1, float x2, float y2);
    void ellipse(float x, float y, float w, float h);


    int getWidth();
    int getHeight();
}
