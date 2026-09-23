package io.github._66_m.control;

import com.badlogic.gdx.graphics.g2d.freetype.FreeType;
import com.badlogic.gdx.utils.GdxNativesLoader;
import io.github._66_m.control.ui.AppIcons;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import org.lwjgl.glfw.GLFW;

/**
 * {@code --self-check}: loads everything that differs per platform (libGDX, FreeType, LWJGL/GLFW
 * natives, the JavaFX toolkit) plus the bundled resources, then exits without opening a window.
 * Release CI runs it on every OS so a package that cannot start is never published.
 */
final class SelfCheck {

  static final List<String> REQUIRED_RESOURCES =
      List.of(
          AppIcons.LOGO_RESOURCE,
          "/css/settings-app.css",
          "/fonts/LiberationSans-Regular.ttf",
          "/shaders/geo2d.vert",
          "/shaders/geo2d.frag",
          "/shaders/image_remap.vert",
          "/shaders/image_remap.frag",
          "/shaders/instance_lit.vert",
          "/shaders/instance_lit.frag",
          "/shaders/line3d.vert",
          "/shaders/line3d.frag");

  private SelfCheck() {}

  /** Runs every check, reporting to {@code out}; returns the process exit code. */
  static int run(PrintStream out) {
    String version = DesktopLauncher.class.getPackage().getImplementationVersion();
    out.println(
        "sorting-visualizer self-check "
            + (version != null ? version : "(dev)")
            + " on "
            + System.getProperty("os.name")
            + "/"
            + System.getProperty("os.arch")
            + ", Java "
            + Runtime.version());
    try {
      checkResources(out);
      GdxNativesLoader.load();
      out.println("  ok  libGDX natives");
      FreeType.initFreeType().dispose();
      out.println("  ok  FreeType natives");
      out.println("  ok  LWJGL/GLFW natives (GLFW " + GLFW.glfwGetVersionString() + ")");
      startAndStopJavaFx();
      out.println("  ok  JavaFX toolkit");
      out.println("self-check passed");
      return 0;
    } catch (Throwable t) {
      out.println("self-check FAILED: " + t);
      t.printStackTrace(out);
      return 1;
    }
  }

  private static void checkResources(PrintStream out) {
    for (String resource : REQUIRED_RESOURCES) {
      if (SelfCheck.class.getResource(resource) == null) {
        throw new IllegalStateException("missing bundled resource " + resource);
      }
    }
    out.println("  ok  " + REQUIRED_RESOURCES.size() + " bundled resources");
  }

  private static void startAndStopJavaFx() throws InterruptedException {
    CountDownLatch started = new CountDownLatch(1);
    Platform.startup(started::countDown);
    if (!started.await(30, TimeUnit.SECONDS)) {
      throw new IllegalStateException("JavaFX toolkit did not start within 30 s");
    }
    Platform.exit();
  }
}
