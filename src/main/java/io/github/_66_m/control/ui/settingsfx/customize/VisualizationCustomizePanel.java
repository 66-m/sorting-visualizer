package io.github._66_m.control.ui.settingsfx.customize;

import io.github._66_m.control.config.visual.VisualizationSettings;
import javafx.scene.Node;

/** JavaFX form for editing one visualization's settings as a draft. */
public interface VisualizationCustomizePanel {

  Node build();

  void load(VisualizationSettings settings);

  VisualizationSettings toSettings();

  VisualizationSettings defaults();

  /** Invoked when draft controls change (for live preview). Not fired during {@link #load}. */
  default void setOnDraftChanged(Runnable listener) {}

  default boolean isValid() {
    return true;
  }

  default String validationMessage() {
    return "";
  }
}
