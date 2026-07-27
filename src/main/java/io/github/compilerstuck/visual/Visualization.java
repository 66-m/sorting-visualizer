package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.CoordinateSpace;
import io.github.compilerstuck.control.render.RenderSystem;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;

public abstract class Visualization {

  protected ArrayModel arrayModel;
  protected ColorGradient colorGradient;
  protected RenderSystem rs;
  protected int screenWidth;
  protected int screenHeight;
  protected String name;
  protected Sound sound;

  public String getName() {
    return name;
  }

  public Visualization(
      ArrayModel arrayModel, ColorGradient colorGradient, Sound sound, RenderSystem rs) {
    this.arrayModel = arrayModel;
    this.colorGradient = colorGradient;
    this.rs = rs;
    this.sound = sound;
    screenHeight = rs.getHeight();
    screenWidth = rs.getWidth();
  }

  public void updateColorGradient(ColorGradient colorGradient) {
    this.colorGradient = colorGradient;
  }

  protected static boolean contentChanged(long cachedRevision, ArrayModel model) {
    return cachedRevision != model.getVisualRevision();
  }

  /**
   * Authoring space for world 2D/3D draws. Defaults to {@link CoordinateSpace#WORLD_YUP}. Overlay
   * text/pixels ignore this.
   */
  protected CoordinateSpace coordinateSpace() {
    return CoordinateSpace.WORLD_YUP;
  }

  /** Preferred entry: render with delta time (seconds). */
  public void render(float delta) {
    screenHeight = rs.getHeight();
    screenWidth = rs.getWidth();
    rs.setCoordinateSpace(coordinateSpace());
    update(delta);
  }

  /**
   * Draw the visualization for this frame. Background is cleared by the render pipeline before this
   * is called.
   */
  public abstract void update(float delta);

  /** Convert World2D Y-up bar-top (or point) Y into Overlay top-left Y-down screen Y. */
  protected float worldYToOverlayY(float worldY) {
    return screenHeight - worldY;
  }

  /** Processing pixel X → World3D X (scene centered). */
  protected float toWorldX(float px) {
    return px - screenWidth * 0.5f;
  }

  /** Processing pixel Y (top-left Y-down) → World3D Y. */
  protected float toWorldY(float py) {
    return -(py - screenHeight * 0.5f);
  }
}
