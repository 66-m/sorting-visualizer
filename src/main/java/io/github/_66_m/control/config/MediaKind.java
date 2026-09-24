package io.github._66_m.control.config;

import java.util.List;
import java.util.Locale;

/**
 * External media file a visualization can be fed from (besides the legacy strip image, which keeps
 * its own {@code imagePath} preference). Each kind remembers its own last path.
 */
public enum MediaKind {
  NONE("", "", ""),
  VIDEO("videoPath", "Video", "mp4,mkv,webm,mov,avi,m4v,gif"),
  MODEL("modelPath", "3D model (.obj)", "obj"),
  TEXTURE("texturePath", "Texture", "png,jpg,jpeg,bmp,gif");

  private final String prefKey;
  private final String label;
  private final String extensions;

  MediaKind(String prefKey, String label, String extensions) {
    this.prefKey = prefKey;
    this.label = label;
    this.extensions = extensions;
  }

  /** Preferences key holding the last path for this kind; empty for {@link #NONE}. */
  public String prefKey() {
    return prefKey;
  }

  /** Field label in the Settings window. */
  public String label() {
    return label;
  }

  /** Lower-case file extensions offered by the file picker. */
  public List<String> extensions() {
    return extensions.isEmpty() ? List.of() : List.of(extensions.split(",", -1));
  }

  /** {@code true} when {@code fileName} ends with one of {@link #extensions()}. */
  public boolean accepts(String fileName) {
    if (fileName == null) {
      return false;
    }
    String lower = fileName.toLowerCase(Locale.ROOT);
    for (String ext : extensions()) {
      if (lower.endsWith("." + ext)) {
        return true;
      }
    }
    return false;
  }
}
