package io.github._66_m.control;

import com.badlogic.gdx.graphics.g2d.freetype.FreeType;
import com.badlogic.gdx.utils.GdxNativesLoader;
import io.github._66_m.control.render.GeometryBatch2D;
import io.github._66_m.control.render.InstanceRenderer3D;
import io.github._66_m.control.render.LineRenderer3D;
import io.github._66_m.control.render.asset.AppAssets;
import io.github._66_m.control.render.asset.ImageRemapRenderer;
import io.github._66_m.control.ui.AppIcons;
import io.github._66_m.control.ui.settingsfx.SettingsStylesheets;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import org.lwjgl.glfw.GLFW;

/**
 * {@code --self-check}: loads everything that differs per platform (libGDX, FreeType, LWJGL/GLFW
 * natives, GLFW initialization, the JavaFX toolkit) plus the bundled resources, then exits without
 * opening a window. Needs a display (e.g. Xvfb on a headless Linux machine). Release CI runs it on
 * every OS so a package that cannot start is never published.
 */
final class SelfCheck {

  /** Classpath resources the app loads at startup, taken from the loaders' own constants. */
  static final List<String> REQUIRED_RESOURCES =
      List.of(
          AppIcons.LOGO_RESOURCE,
          SettingsStylesheets.CSS_PATH,
          "/" + AppAssets.FONT_PATH,
          "/" + GeometryBatch2D.VERT_PATH,
          "/" + GeometryBatch2D.FRAG_PATH,
          "/" + InstanceRenderer3D.VERT_PATH,
          "/" + InstanceRenderer3D.FRAG_PATH,
          "/" + LineRenderer3D.VERT_PATH,
          "/" + LineRenderer3D.FRAG_PATH,
          "/" + ImageRemapRenderer.VERT_PATH,
          "/" + ImageRemapRenderer.FRAG_PATH);

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
      // Same order as the app: JavaFX first, then GLFW while JavaFX is running. On macOS, GLFW
      // init off the process's first thread only works with glfw_async (see DesktopLauncher).
      startJavaFx();
      out.println("  ok  JavaFX toolkit");
      try {
        if (!GLFW.glfwInit()) {
          throw new IllegalStateException("glfwInit failed (no display?)");
        }
        GLFW.glfwTerminate();
        out.println("  ok  GLFW initialization alongside JavaFX");
      } finally {
        Platform.exit();
      }
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

  private static void startJavaFx() throws InterruptedException {
    CountDownLatch started = new CountDownLatch(1);
    Platform.startup(started::countDown);
    if (!started.await(30, TimeUnit.SECONDS)) {
      throw new IllegalStateException("JavaFX toolkit did not start within 30 s");
    }
  }
}
