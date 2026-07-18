package io.github.compilerstuck.control.ui.settingsfx;

import java.beans.PropertyChangeListener;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.scene.Node;

/** Helpers for binding Phase 2 view-models (PropertyChangeSupport) to JavaFX nodes. */
public final class VmBindings {

  private VmBindings() {}

  /** Disables {@code node} when {@code inputsEnabled} is false. */
  public static void bindInputsEnabled(
      Node node,
      BooleanSupplier isEnabled,
      Consumer<PropertyChangeListener> addListener,
      String propertyName) {
    node.setDisable(!isEnabled.getAsBoolean());
    addListener.accept(
        evt -> {
          if (propertyName.equals(evt.getPropertyName())) {
            boolean enabled = Boolean.TRUE.equals(evt.getNewValue());
            runFx(() -> node.setDisable(!enabled));
          }
        });
  }

  public static void runFx(Runnable action) {
    if (Platform.isFxApplicationThread()) {
      action.run();
    } else {
      Platform.runLater(action);
    }
  }
}
