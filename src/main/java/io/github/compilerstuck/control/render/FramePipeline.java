package io.github.compilerstuck.control.render;

/**
 * Lightweight pass state machine. Callers apply GL / batch side effects when enter methods return
 * {@code true} (newly entered that pass).
 */
public final class FramePipeline {
  private RenderPass current = RenderPass.NONE;

  public RenderPass current() {
    return current;
  }

  public void beginFrame() {
    current = RenderPass.NONE;
  }

  /**
   * @return true if this call newly entered {@link RenderPass#WORLD_2D}
   */
  public boolean enterWorld2D() {
    if (current == RenderPass.WORLD_2D) {
      return false;
    }
    current = RenderPass.WORLD_2D;
    return true;
  }

  /**
   * @return true if this call newly entered {@link RenderPass#WORLD_3D}
   */
  public boolean enterWorld3D() {
    if (current == RenderPass.WORLD_3D) {
      return false;
    }
    current = RenderPass.WORLD_3D;
    return true;
  }

  /**
   * @return true if this call newly entered {@link RenderPass#OVERLAY}
   */
  public boolean enterOverlay() {
    if (current == RenderPass.OVERLAY) {
      return false;
    }
    current = RenderPass.OVERLAY;
    return true;
  }

  /** Leaves world / overlay passes (after caller has ended batches and GL state). */
  public void endWorld() {
    current = RenderPass.NONE;
  }

  public boolean inWorld3D() {
    return current == RenderPass.WORLD_3D;
  }
}
