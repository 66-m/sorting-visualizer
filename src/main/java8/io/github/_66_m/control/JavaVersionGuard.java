package io.github._66_m.control;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JOptionPane;

/**
 * The JAR's entry point. It is compiled for Java 8 (see {@code src/main/java8} in {@code pom.xml})
 * so that an older Java can still run it and explain the problem, instead of failing with an {@code
 * UnsupportedClassVersionError} that a double-clicked JAR never shows. On Windows, {@code java} or
 * the {@code .jar} file association often still points to an old Java even after a newer JDK was
 * installed.
 */
public final class JavaVersionGuard {

  /** Keep in sync with {@code maven.compiler.release}; a unit test checks it. */
  static final int REQUIRED_JAVA = 25;

  private static final String MAIN_CLASS = "io.github._66_m.control.DesktopLauncher";

  private JavaVersionGuard() {}

  public static void main(String[] args) throws Throwable {
    int feature = featureVersion(System.getProperty("java.specification.version"));
    if (feature < REQUIRED_JAVA) {
      reportTooOld(message(feature, System.getProperty("java.home")));
      System.exit(1);
      return;
    }
    try {
      Class.forName(MAIN_CLASS).getMethod("main", String[].class).invoke(null, (Object) args);
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  /** Parses {@code java.specification.version}: {@code "1.8"} is 8, {@code "25"} is 25. */
  static int featureVersion(String specVersion) {
    if (specVersion == null) {
      return 0;
    }
    String version = specVersion.startsWith("1.") ? specVersion.substring(2) : specVersion;
    int end = 0;
    while (end < version.length() && Character.isDigit(version.charAt(end))) {
      end++;
    }
    return end == 0 ? 0 : Integer.parseInt(version.substring(0, end));
  }

  static String message(int feature, String javaHome) {
    return "Sorting Visualizer needs Java "
        + REQUIRED_JAVA
        + " or newer, but it was started with Java "
        + feature
        + ":\n"
        + javaHome
        + "\n\nIf you already installed a newer Java, run the JAR with that one\n"
        + "(<its folder>/bin/java -jar sorting-visualizer.jar), or put its bin folder\n"
        + "first in your PATH. Check with: java -version\n\n"
        + "Or download the installer for your system, which includes Java:\n"
        + "https://github.com/66-m/sorting-visualizer/releases/latest";
  }

  private static void reportTooOld(String message) {
    System.err.println(message);
    if (GraphicsEnvironment.isHeadless()) {
      return;
    }
    try {
      JOptionPane.showMessageDialog(
          null, message, "Sorting Visualizer: Java too old", JOptionPane.ERROR_MESSAGE);
    } catch (RuntimeException | Error e) {
      // Already printed above.
    }
  }
}
