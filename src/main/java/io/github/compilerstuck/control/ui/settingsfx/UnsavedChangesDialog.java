package io.github.compilerstuck.control.ui.settingsfx;

import atlantafx.base.theme.Styles;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

/**
 * Shared "Save and close / Discard changes / Keep editing" confirmation for draft dialogs that want
 * to warn before closing with unapplied changes ({@link VisualizationCustomizeDialog}, {@link
 * AudioSettingsDialog}, {@link RunAllOrderDialog}). Only the alert chrome is shared; each caller
 * keeps its own dirty check and apply-on-save logic.
 */
public final class UnsavedChangesDialog {

  /** Outcome of the confirmation; callers map SAVE to their own apply/persist logic. */
  public enum Choice {
    SAVE,
    DISCARD,
    CANCEL
  }

  private UnsavedChangesDialog() {}

  /**
   * Shows the confirmation and returns the user's choice ({@code Choice.CANCEL} if dismissed).
   *
   * @param dialogPaneId id set on the alert's dialog pane, for test lookups
   * @param message the body text explaining what will be discarded
   */
  public static Choice ask(Window owner, String dialogPaneId, String message) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.initOwner(owner);
    alert.setTitle(SettingsStrings.CUSTOMIZE_UNSAVED_TITLE);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.getDialogPane().setId(dialogPaneId);

    // LEFT = far left; CANCEL_CLOSE = Escape; OK_DONE = Enter / default.
    ButtonType discard =
        new ButtonType(SettingsStrings.CUSTOMIZE_DISCARD, ButtonBar.ButtonData.LEFT);
    ButtonType keepEditing =
        new ButtonType(SettingsStrings.CUSTOMIZE_KEEP_EDITING, ButtonBar.ButtonData.CANCEL_CLOSE);
    ButtonType saveAndClose =
        new ButtonType(SettingsStrings.CUSTOMIZE_SAVE_AND_CLOSE, ButtonBar.ButtonData.OK_DONE);
    alert.getButtonTypes().setAll(discard, keepEditing, saveAndClose);

    var css = SettingsStylesheets.cssUrl();
    if (css != null) {
      alert.getDialogPane().getStylesheets().add(css.toExternalForm());
    }

    alert.setOnShown(
        e -> {
          Node barNode = alert.getDialogPane().lookup(".button-bar");
          if (barNode instanceof ButtonBar bar) {
            // L = LEFT, C = CANCEL_CLOSE, O = OK_DONE; + = flexible gap.
            bar.setButtonOrder("L+CO");
          }
          Button saveButton = (Button) alert.getDialogPane().lookupButton(saveAndClose);
          if (saveButton != null) {
            saveButton.getStyleClass().add(Styles.ACCENT);
          }
          Button discardButton = (Button) alert.getDialogPane().lookupButton(discard);
          if (discardButton != null) {
            discardButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
          }
        });

    return alert
        .showAndWait()
        .map(
            type -> {
              if (saveAndClose.equals(type)) {
                return Choice.SAVE;
              }
              if (discard.equals(type)) {
                return Choice.DISCARD;
              }
              return Choice.CANCEL;
            })
        .orElse(Choice.CANCEL);
  }
}
