package io.github.compilerstuck.Control.render;

/**
 * A trivial {@link RenderContext} implementation that does nothing.  Useful for
 * unit tests or running the application in a headless environment.
 */
public class HeadlessRenderContext implements RenderContext {
    private final int width;
    private final int height;

    public HeadlessRenderContext(int width, int height) {
        this.width = width;
        this.height = height;
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
    public void rect(float x, float y, float w, float h) {
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

    @Override
    public void line(float x1, float y1, float x2, float y2) {
        // no-op
    }

    @Override
    public void ellipse(float x, float y, float w, float h) {
        // no-op
    }
}
