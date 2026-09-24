package io.github._66_m.control;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.GdxRuntimeException;
import org.junit.jupiter.api.Test;

class StartupFailureTest {

  @Test
  void windowCreationFailureExplainsTheOpenGlRequirement() {
    String message =
        StartupFailure.describe(new GdxRuntimeException(StartupFailure.WINDOW_CREATION_FAILED));
    assertTrue(message.contains("OpenGL 3.3"), message);
    assertTrue(message.contains("graphics driver"), message);
  }

  @Test
  void windowCreationFailureIsFoundInTheCauseChain() {
    Throwable wrapped =
        new RuntimeException(
            "outer", new GdxRuntimeException(StartupFailure.WINDOW_CREATION_FAILED));
    assertTrue(StartupFailure.describe(wrapped).contains("OpenGL 3.3"));
  }

  @Test
  void otherErrorsAreShownAsTheyAre() {
    String message = StartupFailure.describe(new IllegalStateException("boom"));
    assertTrue(message.contains("IllegalStateException: boom"), message);
  }
}
