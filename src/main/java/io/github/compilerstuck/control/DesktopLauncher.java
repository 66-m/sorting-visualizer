package io.github.compilerstuck.control;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import io.github.compilerstuck.control.config.AppConfig;
import io.github.compilerstuck.control.ui.AppIcons;
import io.github.compilerstuck.control.ui.settingsfx.JavaFxBootstrap;
import java.awt.Dimension;
import java.awt.Rectangle;

/** Desktop entry point: JavaFX Settings toolkit first, then libGDX LWJGL3 visualization window. */
public final class DesktopLauncher {

  private DesktopLauncher() {}

  public static void main(String[] args) {
    LaunchArgs.parse(args);
    AppIcons.installApplicationIcons();
    JavaFxBootstrap.start();

    boolean fullscreen = LaunchArgs.fullscreen();
    boolean portrait = LaunchArgs.portrait();

    Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
    config.setTitle("Sorting Algorithm Visualizer - Visualization");
    config.useVsync(true);
    config.setForegroundFPS(AppConfig.TARGET_FRAME_RATE);
    // GL 3.0 (emulated by desktop OpenGL 3.2) required for mesh hardware instancing.
    config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL30, 3, 2);
    config.setWindowIcon("logo.png");
    config.setWindowSizeLimits(AppConfig.MIN_WINDOW_WIDTH, AppConfig.MIN_WINDOW_HEIGHT, -1, -1);

    if (fullscreen) {
      // Exclusive fullscreen (GLFW monitor) so desktop panels auto-hide on Linux/Windows/macOS.
      config.setFullscreenMode(
          Lwjgl3ApplicationConfiguration.getDisplayMode(
              DisplayBounds.resolveMonitor(LaunchArgs.display())));
    } else if (portrait) {
      Rectangle bounds = DisplayBounds.resolveBounds(LaunchArgs.display());
      Dimension portraitDim = DisplayBounds.portraitSize(bounds);
      config.setWindowedMode(portraitDim.width, portraitDim.height);
      int x = bounds.x + Math.max(0, (bounds.width - portraitDim.width) / 2);
      int y = bounds.y + Math.max(0, (bounds.height - portraitDim.height) / 2);
      config.setWindowPosition(x, y);
    } else {
      Rectangle bounds = DisplayBounds.resolveBounds(LaunchArgs.display());
      config.setWindowedMode(Math.max(1, bounds.width), Math.max(1, bounds.height));
      config.setWindowPosition(bounds.x, bounds.y);
      config.setMaximized(true);
    }

    new Lwjgl3Application(new SortingVisualizerGame(), config);
  }
}
