package io.github._66_m.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class JavaVersionGuardTest {

  @Test
  void parsesSpecificationVersions() {
    assertEquals(8, JavaVersionGuard.featureVersion("1.8"));
    assertEquals(11, JavaVersionGuard.featureVersion("11"));
    assertEquals(25, JavaVersionGuard.featureVersion("25"));
    assertEquals(26, JavaVersionGuard.featureVersion("26-ea"));
    assertEquals(0, JavaVersionGuard.featureVersion(null));
    assertEquals(0, JavaVersionGuard.featureVersion(""));
  }

  @Test
  void guardRunsOnJava8() throws IOException {
    assertEquals(8, classFileJavaVersion(JavaVersionGuard.class));
  }

  @Test
  void requiredJavaMatchesTheAppsClassFiles() throws IOException {
    assertEquals(JavaVersionGuard.REQUIRED_JAVA, classFileJavaVersion(DesktopLauncher.class));
  }

  @Test
  void messageNamesTheJavaInUseAndTheRequiredOne() {
    String message = JavaVersionGuard.message(8, "C:\\Program Files\\Java\\jre1.8.0_461");
    assertTrue(message.contains("Java 25 or newer"), message);
    assertTrue(message.contains("started with Java 8"), message);
    assertTrue(message.contains("jre1.8.0_461"), message);
  }

  /** Class file major version 52 is Java 8, 69 is Java 25. */
  private static int classFileJavaVersion(Class<?> type) throws IOException {
    try (InputStream in = type.getResourceAsStream(type.getSimpleName() + ".class");
        DataInputStream data = new DataInputStream(in)) {
      data.readInt(); // magic
      data.readUnsignedShort(); // minor
      return data.readUnsignedShort() - 44;
    }
  }
}
