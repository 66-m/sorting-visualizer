package io.github._66_m.control;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class SelfCheckTest {

  @Test
  void everyResourceTheSelfCheckRequiresIsBundled() {
    for (String resource : SelfCheck.REQUIRED_RESOURCES) {
      assertNotNull(SelfCheck.class.getResource(resource), resource);
    }
  }
}
