package io.github._66_m.control.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.prefs.Preferences;
import org.junit.jupiter.api.Test;

class MediaKindTest {

  @Test
  void acceptsItsExtensionsCaseInsensitively() {
    assertTrue(MediaKind.VIDEO.accepts("/x/Bad Apple.MP4"));
    assertTrue(MediaKind.MODEL.accepts("bunny.obj"));
    assertFalse(MediaKind.MODEL.accepts("bunny.fbx"));
    assertTrue(MediaKind.TEXTURE.accepts("earth.jpeg"));
    assertFalse(MediaKind.NONE.accepts("anything.png"));
  }

  @Test
  void mediaPathsRoundTripPerKind() throws Exception {
    Preferences node = Preferences.userRoot().node("sortvis-test-media-" + System.nanoTime());
    try {
      UserPreferences prefs = UserPreferences.load(node);
      assertEquals("", prefs.getMediaPath(MediaKind.VIDEO));
      prefs.setMediaPath(MediaKind.VIDEO, "/v.mp4");
      prefs.setMediaPath(MediaKind.MODEL, "/m.obj");
      prefs.setMediaPath(MediaKind.NONE, "/ignored");
      prefs.save(node);
      UserPreferences again = UserPreferences.load(node);
      assertEquals("/v.mp4", again.getMediaPath(MediaKind.VIDEO));
      assertEquals("/m.obj", again.getMediaPath(MediaKind.MODEL));
      assertEquals("", again.getMediaPath(MediaKind.TEXTURE));
      assertEquals("", again.getMediaPath(MediaKind.NONE));
    } finally {
      node.removeNode();
    }
  }
}
