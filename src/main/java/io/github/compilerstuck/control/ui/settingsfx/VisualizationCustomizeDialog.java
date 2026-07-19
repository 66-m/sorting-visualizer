package io.github.compilerstuck.control.ui.settingsfx;

import atlantafx.base.theme.Styles;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettingsCodec;
import io.github.compilerstuck.control.ui.settingsfx.customize.VisualizationCustomizePanel;
import io.github.compilerstuck.control.ui.settingsfx.customize.VisualizationCustomizePanels;
import io.github.compilerstuck.control.ui.settingsfx.vm.VisualizationViewModel;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * Modal draft dialog for per-visualization customization.
 *
 * <p>Draft edits are previewed live on the visualization. Apply persists them; closing with unsaved
 * changes asks to discard and restores the last applied baseline.
 */
public final class VisualizationCustomizeDialog {

  public static final String DIALOG_ID = "visualization-customize-dialog";
  public static final String DISCARD_CONFIRM_ID = "visualization-customize-discard-confirm";
  public static final String IMPORT_DIALOG_ID = "visualization-customize-import-dialog";
  public static final String IMPORT_TEXT_ID = "visualization-customize-import-text";

  private VisualizationCustomizeDialog() {}

  public static void show(Window owner, VisualizationViewModel vm) {
    Optional<Supplier<VisualizationCustomizePanel>> factory =
        VisualizationCustomizePanels.forId(vm.getSelectedId());
    if (factory.isEmpty()) {
      return;
    }

    VisualizationCustomizePanel panel = factory.get().get();
    VisualizationSettings seed = vm.getCurrentCustomization();
    if (seed == null) {
      seed = panel.defaults();
    }
    // Baseline for dirty check / discard restore: open state, updated after each successful Apply.
    AtomicReference<VisualizationSettings> appliedBaseline = new AtomicReference<>(seed);

    Label status = new Label();
    status.getStyleClass().add("settings-inline-status");
    status.setWrapText(true);
    status.setVisible(false);
    status.setManaged(false);

    VBox content = new VBox(SettingsLayout.GAP_MD, panel.build(), status);
    content.getStyleClass().add("customize-dialog-content");
    content.setPadding(
        new Insets(
            SettingsLayout.GAP_SM, SettingsLayout.GAP_SM, SettingsLayout.GAP_XS, SettingsLayout.GAP_SM));
    VBox.setVgrow(content, Priority.ALWAYS);
    panel.setOnDraftChanged(() -> previewDraft(vm, panel));
    panel.load(seed);

    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.initOwner(owner);
    dialog.setTitle(SettingsStrings.CUSTOMIZE_TITLE);
    dialog.getDialogPane().setId(DIALOG_ID);
    dialog.getDialogPane().getStyleClass().add("customize-dialog");
    dialog.getDialogPane().setContent(content);
    dialog.getDialogPane().setPrefWidth(520);
    dialog.getDialogPane().setMinWidth(480);

    // APPLY (not OK_DONE) so the button does not close the dialog by default.
    ButtonType applyType = new ButtonType(SettingsStrings.APPLY, ButtonBar.ButtonData.APPLY);
    ButtonType closeType = new ButtonType(SettingsStrings.CLOSE, ButtonBar.ButtonData.CANCEL_CLOSE);
    ButtonType resetType = new ButtonType(SettingsStrings.RESET_ALL, ButtonBar.ButtonData.LEFT);
    ButtonType importType = new ButtonType(SettingsStrings.IMPORT, ButtonBar.ButtonData.LEFT);
    ButtonType exportType = new ButtonType(SettingsStrings.EXPORT, ButtonBar.ButtonData.LEFT);
    dialog
        .getDialogPane()
        .getButtonTypes()
        .addAll(closeType, resetType, exportType, importType, applyType);

    Button applyButton = (Button) dialog.getDialogPane().lookupButton(applyType);
    applyButton.getStyleClass().add(Styles.ACCENT);
    applyButton.addEventFilter(
        javafx.event.ActionEvent.ACTION,
        e -> {
          e.consume();
          if (!panel.isValid()) {
            return;
          }
          VisualizationSettings next = panel.toSettings();
          if (vm.applyCustomization(next)) {
            appliedBaseline.set(next);
            clearStatus(status);
          }
        });

    Button resetButton = (Button) dialog.getDialogPane().lookupButton(resetType);
    resetButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
    resetButton.addEventFilter(
        javafx.event.ActionEvent.ACTION,
        e -> {
          e.consume();
          panel.load(panel.defaults());
          previewDraft(vm, panel);
          clearStatus(status);
        });

    Button importButton = (Button) dialog.getDialogPane().lookupButton(importType);
    importButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
    importButton.addEventFilter(
        javafx.event.ActionEvent.ACTION,
        e -> {
          e.consume();
          Optional<VisualizationSettingsCodec.DecodeResult> imported =
              showImportDialog(dialog.getDialogPane().getScene().getWindow(), vm.getSelectedId());
          if (imported.isEmpty()) {
            return;
          }
          panel.load(imported.get().settings());
          previewDraft(vm, panel);
          if (imported.get().valuesWereClamped()) {
            showStatus(status, SettingsStrings.CUSTOMIZE_IMPORT_CLAMPED, true);
          } else {
            showStatus(status, SettingsStrings.CUSTOMIZE_IMPORT_SUCCESS, false);
          }
        });

    Button exportButton = (Button) dialog.getDialogPane().lookupButton(exportType);
    exportButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
    exportButton.addEventFilter(
        javafx.event.ActionEvent.ACTION,
        e -> {
          e.consume();
          String json = VisualizationSettingsCodec.encodeEnvelope(panel.toSettings());
          ClipboardContent contentClip = new ClipboardContent();
          contentClip.putString(json);
          Clipboard.getSystemClipboard().setContent(contentClip);
          showStatus(status, SettingsStrings.CUSTOMIZE_EXPORT_COPIED, false);
        });

    var css = SettingsFxController.class.getResource("/css/settings-app.css");
    if (css != null) {
      dialog.getDialogPane().getStylesheets().add(css.toExternalForm());
    }

    // Window close (X): confirm when the draft differs from the last applied / open baseline.
    dialog.setOnShown(
        e -> {
          Window window = dialog.getDialogPane().getScene().getWindow();
          if (window == null) {
            return;
          }
          window.setOnCloseRequest(
              closeEvent -> {
                if (!confirmCloseIfDirty(vm, panel, appliedBaseline.get(), dialog)) {
                  closeEvent.consume();
                }
              });
        });

    // Close: same dirty confirmation as the window X.
    Button closeButton = (Button) dialog.getDialogPane().lookupButton(closeType);
    closeButton.addEventFilter(
        javafx.event.ActionEvent.ACTION,
        e -> {
          if (!confirmCloseIfDirty(vm, panel, appliedBaseline.get(), dialog)) {
            e.consume();
          }
        });

    dialog.showAndWait();
  }

  private static void previewDraft(VisualizationViewModel vm, VisualizationCustomizePanel panel) {
    if (!panel.isValid()) {
      return;
    }
    vm.previewCustomization(panel.toSettings());
  }

  /**
   * @return {@code true} if the dialog may close (clean, or user discarded and live preview was
   *     restored)
   */
  private static boolean confirmCloseIfDirty(
      VisualizationViewModel vm,
      VisualizationCustomizePanel panel,
      VisualizationSettings baseline,
      Dialog<?> dialog) {
    if (!isDirty(panel, baseline)) {
      return true;
    }
    if (!confirmDiscardUnsaved(dialog.getDialogPane().getScene().getWindow())) {
      return false;
    }
    vm.previewCustomization(baseline);
    return true;
  }

  /**
   * Paste dialog for importing an exported config. Returns empty if cancelled or dismissed without
   * a valid import.
   */
  private static Optional<VisualizationSettingsCodec.DecodeResult> showImportDialog(
      Window owner, String expectedVisualizationId) {
    AtomicReference<VisualizationSettingsCodec.DecodeResult> accepted = new AtomicReference<>();

    TextArea text = new TextArea();
    text.setId(IMPORT_TEXT_ID);
    text.getStyleClass().add("customize-import-text");
    text.setPromptText(SettingsStrings.CUSTOMIZE_IMPORT_PLACEHOLDER);
    text.setWrapText(true);
    text.setPrefRowCount(8);
    // Avoid accidental huge pastes locking the UI.
    text.setTextFormatter(
        new TextFormatter<>(
            change -> change.getControlNewText().length() <= 64_000 ? change : null));
    VBox.setVgrow(text, Priority.ALWAYS);

    String clip = Clipboard.getSystemClipboard().getString();
    if (clip != null && !clip.isBlank()) {
      text.setText(clip.strip());
      text.selectAll();
    }

    Label hint = new Label(SettingsStrings.CUSTOMIZE_IMPORT_HINT);
    hint.getStyleClass().add("settings-muted");
    hint.setWrapText(true);

    Label error = new Label();
    error.getStyleClass().addAll("settings-inline-status", "settings-inline-error");
    error.setWrapText(true);
    error.setVisible(false);
    error.setManaged(false);

    Button paste = new Button(SettingsStrings.CUSTOMIZE_IMPORT_PASTE);
    paste.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.SMALL);
    paste.setOnAction(
        e -> {
          String next = Clipboard.getSystemClipboard().getString();
          if (next == null || next.isBlank()) {
            showStatus(error, SettingsStrings.CUSTOMIZE_IMPORT_EMPTY, true);
            return;
          }
          text.setText(next.strip());
          text.requestFocus();
          text.selectAll();
          clearStatus(error);
        });

    VBox body = new VBox(SettingsLayout.GAP_SM, hint, text, paste, error);
    body.setPadding(new Insets(SettingsLayout.GAP_SM));
    body.setFillWidth(true);

    Dialog<ButtonType> importDialog = new Dialog<>();
    importDialog.initOwner(owner);
    importDialog.setTitle(SettingsStrings.CUSTOMIZE_IMPORT_TITLE);
    importDialog.getDialogPane().setId(IMPORT_DIALOG_ID);
    importDialog.getDialogPane().getStyleClass().add("customize-import-dialog");
    importDialog.getDialogPane().setContent(body);
    importDialog.getDialogPane().setPrefWidth(480);
    importDialog.getDialogPane().setPrefHeight(320);

    ButtonType loadType =
        new ButtonType(SettingsStrings.CUSTOMIZE_IMPORT_LOAD, ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelType =
        new ButtonType(SettingsStrings.CANCEL, ButtonBar.ButtonData.CANCEL_CLOSE);
    importDialog.getDialogPane().getButtonTypes().addAll(cancelType, loadType);

    Button loadButton = (Button) importDialog.getDialogPane().lookupButton(loadType);
    loadButton.getStyleClass().add(Styles.ACCENT);
    loadButton.addEventFilter(
        javafx.event.ActionEvent.ACTION,
        e -> {
          String raw = text.getText() == null ? "" : text.getText().strip();
          if (raw.isEmpty()) {
            e.consume();
            showStatus(error, SettingsStrings.CUSTOMIZE_IMPORT_EMPTY, true);
            return;
          }
          Optional<VisualizationSettingsCodec.DecodeResult> decoded =
              VisualizationSettingsCodec.decodeEnvelope(raw);
          if (decoded.isEmpty()) {
            e.consume();
            showStatus(error, SettingsStrings.CUSTOMIZE_IMPORT_INVALID, true);
            return;
          }
          if (!decoded.get().settings().visualizationId().equals(expectedVisualizationId)) {
            e.consume();
            showStatus(error, SettingsStrings.CUSTOMIZE_IMPORT_WRONG_VIZ, true);
            return;
          }
          accepted.set(decoded.get());
        });

    var css = SettingsFxController.class.getResource("/css/settings-app.css");
    if (css != null) {
      importDialog.getDialogPane().getStylesheets().add(css.toExternalForm());
    }

    importDialog.setOnShown(e -> text.requestFocus());
    importDialog.showAndWait();
    return Optional.ofNullable(accepted.get());
  }

  private static boolean isDirty(VisualizationCustomizePanel panel, VisualizationSettings baseline) {
    VisualizationSettings current = panel.toSettings();
    if (baseline == null) {
      return current != null;
    }
    return !baseline.equals(current);
  }

  /** @return {@code true} if the user chose to discard and close */
  private static boolean confirmDiscardUnsaved(Window owner) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.initOwner(owner);
    alert.setTitle(SettingsStrings.CUSTOMIZE_DISCARD_TITLE);
    alert.setHeaderText(null);
    alert.setContentText(SettingsStrings.CUSTOMIZE_DISCARD_MESSAGE);
    alert.getDialogPane().setId(DISCARD_CONFIRM_ID);

    ButtonType discard =
        new ButtonType(SettingsStrings.CUSTOMIZE_DISCARD, ButtonBar.ButtonData.OK_DONE);
    ButtonType keepEditing =
        new ButtonType(SettingsStrings.CUSTOMIZE_KEEP_EDITING, ButtonBar.ButtonData.CANCEL_CLOSE);
    alert.getButtonTypes().setAll(keepEditing, discard);

    var css = SettingsFxController.class.getResource("/css/settings-app.css");
    if (css != null) {
      alert.getDialogPane().getStylesheets().add(css.toExternalForm());
    }

    return alert.showAndWait().filter(discard::equals).isPresent();
  }

  private static void showStatus(Label status, String message, boolean error) {
    status.setText(message);
    status.getStyleClass().removeAll("settings-inline-error", "settings-inline-success");
    status.getStyleClass().add(error ? "settings-inline-error" : "settings-inline-success");
    status.setVisible(true);
    status.setManaged(true);
  }

  private static void clearStatus(Label status) {
    status.setText("");
    status.getStyleClass().removeAll("settings-inline-error", "settings-inline-success");
    status.setVisible(false);
    status.setManaged(false);
  }
}
