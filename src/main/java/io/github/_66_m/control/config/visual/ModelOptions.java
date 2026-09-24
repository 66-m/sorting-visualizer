package io.github._66_m.control.config.visual;

/** Enums shared by the 3D model visualizations' settings. */
public final class ModelOptions {

  private ModelOptions() {}

  /**
   * Order in which the model's pieces are assigned to array indices; decides what a half-sorted
   * array looks like.
   */
  public enum AssemblyOrder {
    /** Bottom-up. */
    HEIGHT,
    /** Core outward (onion layers). */
    RADIAL,
    /** Around the vertical axis (orange peel). */
    ANGLE,
    /** Height bands swept by angle (helix build). */
    SPIRAL
  }

  /** Vertical axis of the model file. */
  public enum UpAxis {
    Y_UP,
    Z_UP
  }

  /** Where element colors come from. */
  public enum ColorSource {
    /** Texture / material colors (falls back to the gradient when the model has none). */
    MODEL,
    /** The app's color gradient by value. */
    GRADIENT
  }
}
