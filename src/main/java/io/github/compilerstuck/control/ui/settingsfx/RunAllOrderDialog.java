package io.github.compilerstuck.control.ui.settingsfx;

import atlantafx.base.theme.Styles;
import io.github.compilerstuck.control.ui.settingsfx.vm.AlgorithmEntry;
import io.github.compilerstuck.control.ui.settingsfx.vm.AlgorithmViewModel;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * Modal dialog to include/exclude run-all algorithms and drag-reorder them.
 *
 * <p>Layout: hint → selection summary + bulk actions → numbered reorderable list → Cancel / Apply.
 */
public final class RunAllOrderDialog {

  public static final String DIALOG_ID = "run-all-order-dialog";
  public static final String LIST_ID = "run-all-order-list";

  private RunAllOrderDialog() {}

  public static void show(Window owner, AlgorithmViewModel vm) {
    ObservableList<DraftRow> rows = FXCollections.observableArrayList();
    for (AlgorithmEntry entry : vm.getEntries()) {
      rows.add(new DraftRow(entry.getId(), entry.getName(), entry.isSelected()));
    }

    Label hint = new Label(SettingsStrings.RUN_ALL_ORDER_HINT);
    hint.getStyleClass().add("settings-muted");
    hint.setWrapText(true);

    Label sectionLabel = new Label(SettingsStrings.RUN_ALL_ORDER_SECTION);
    sectionLabel.getStyleClass().add("settings-section-label");

    Label countLabel = new Label();
    countLabel.getStyleClass().addAll("settings-muted", "run-all-order-count");
    countLabel.setMinWidth(Region.USE_PREF_SIZE);

    Label status = new Label();
    status.getStyleClass().add("settings-inline-status");
    status.setWrapText(true);
    status.setVisible(false);
    status.setManaged(false);

    ListView<DraftRow> list = new ListView<>(rows);
    list.setId(LIST_ID);
    list.getStyleClass().add("settings-run-all-order-list");
    list.setPrefHeight(Math.min(440, Math.max(220, 40 * Math.min(rows.size(), 12) + 8)));
    list.setCellFactory(lv -> new OrderCell(rows, () -> refreshChrome(rows, countLabel, status)));
    VBox.setVgrow(list, Priority.ALWAYS);

    Button selectAll = new Button(SettingsStrings.SELECT_ALL);
    selectAll.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.SMALL);
    selectAll.setOnAction(
        e -> {
          for (DraftRow row : rows) {
            row.selected = true;
          }
          list.refresh();
          refreshChrome(rows, countLabel, status);
        });

    Button clear = new Button(SettingsStrings.CLEAR_SELECTION);
    clear.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.SMALL);
    clear.setOnAction(
        e -> {
          for (DraftRow row : rows) {
            row.selected = false;
          }
          list.refresh();
          refreshChrome(rows, countLabel, status);
        });

    Region toolbarSpacer = new Region();
    HBox.setHgrow(toolbarSpacer, Priority.ALWAYS);
    HBox toolbar = new HBox(SettingsLayout.GAP_SM, selectAll, clear, toolbarSpacer, countLabel);
    toolbar.setAlignment(Pos.CENTER_LEFT);
    toolbar.getStyleClass().add("run-all-order-toolbar");

    VBox listBlock = new VBox(SettingsLayout.GAP_SM, sectionLabel, toolbar, list);
    listBlock.getStyleClass().add("run-all-order-section");

    VBox content = new VBox(SettingsLayout.GAP_MD, hint, listBlock, status);
    content.getStyleClass().add("run-all-order-content");
    content.setPadding(
        new Insets(
            SettingsLayout.GAP_SM, SettingsLayout.GAP_SM, SettingsLayout.GAP_XS, SettingsLayout.GAP_SM));
    content.setFillWidth(true);

    refreshChrome(rows, countLabel, status);

    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.initOwner(owner);
    dialog.setTitle(SettingsStrings.RUN_ALL_ORDER_TITLE);
    dialog.getDialogPane().setId(DIALOG_ID);
    dialog.getDialogPane().getStyleClass().add("run-all-order-dialog");
    dialog.getDialogPane().setContent(content);
    dialog.getDialogPane().setPrefWidth(460);
    dialog.getDialogPane().setMinWidth(400);

    ButtonType applyType = new ButtonType(SettingsStrings.APPLY, ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelType =
        new ButtonType(SettingsStrings.CANCEL, ButtonBar.ButtonData.CANCEL_CLOSE);
    dialog.getDialogPane().getButtonTypes().addAll(cancelType, applyType);

    Button applyButton = (Button) dialog.getDialogPane().lookupButton(applyType);
    applyButton.getStyleClass().add(Styles.ACCENT);
    applyButton.addEventFilter(
        javafx.event.ActionEvent.ACTION,
        e -> {
          if (selectedCount(rows) == 0) {
            e.consume();
            showStatus(status, SettingsStrings.RUN_ALL_ORDER_EMPTY, true);
          }
        });

    Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelType);
    cancelButton.getStyleClass().add(Styles.BUTTON_OUTLINED);

    var css = SettingsFxController.class.getResource("/css/settings-app.css");
    if (css != null) {
      dialog.getDialogPane().getStylesheets().add(css.toExternalForm());
    }

    dialog
        .showAndWait()
        .filter(applyType::equals)
        .ifPresent(
            ignored -> {
              Set<String> selected =
                  rows.stream()
                      .filter(r -> r.selected)
                      .map(r -> r.id)
                      .collect(Collectors.toCollection(HashSet::new));
              vm.applyRunAllOrder(rows.stream().map(r -> r.id).toList(), selected);
            });
  }

  private static void refreshChrome(ObservableList<DraftRow> rows, Label countLabel, Label status) {
    int selected = selectedCount(rows);
    countLabel.setText(
        selected == 1
            ? SettingsStrings.RUN_ALL_ORDER_COUNT_ONE
            : String.format(SettingsStrings.RUN_ALL_ORDER_COUNT, selected));
    if (selected > 0) {
      clearStatus(status);
    }
  }

  private static int selectedCount(ObservableList<DraftRow> rows) {
    int n = 0;
    for (DraftRow row : rows) {
      if (row.selected) {
        n++;
      }
    }
    return n;
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

  private static final class DraftRow {
    final String id;
    final String name;
    boolean selected;

    DraftRow(String id, String name, boolean selected) {
      this.id = id;
      this.name = name;
      this.selected = selected;
    }
  }

  private static final class OrderCell extends ListCell<DraftRow> {
    private final ObservableList<DraftRow> rows;
    private final Runnable onChanged;
    private final CheckBox selected = new CheckBox();
    private final Label order = new Label();
    private final Label name = new Label();
    private final Label grip = new Label(SettingsStrings.DRAG_HANDLE);
    private final HBox root;

    OrderCell(ObservableList<DraftRow> rows, Runnable onChanged) {
      this.rows = rows;
      this.onChanged = onChanged;
      grip.getStyleClass().add("settings-drag-handle");
      grip.setTooltip(new Tooltip(SettingsStrings.RUN_ALL_ORDER_DRAG_TOOLTIP));
      order.getStyleClass().add("run-all-order-index");
      order.setMinWidth(28);
      order.setAlignment(Pos.CENTER_RIGHT);
      name.setMaxWidth(Double.MAX_VALUE);
      HBox.setHgrow(name, Priority.ALWAYS);
      root = new HBox(SettingsLayout.GAP_SM, grip, selected, order, name);
      root.setAlignment(Pos.CENTER_LEFT);
      root.getStyleClass().add("settings-run-all-order-row");

      selected.setOnAction(
          e -> {
            DraftRow item = getItem();
            if (item != null) {
              item.selected = selected.isSelected();
              // Refresh all cells so run-order numbers stay in sync.
              getListView().refresh();
              onChanged.run();
            }
          });

      // Drag from the grip only so checkbox / label clicks stay clickable.
      grip.setOnDragDetected(
          e -> {
            if (isEmpty() || getItem() == null) {
              return;
            }
            Dragboard db = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(getIndex()));
            db.setContent(content);
            db.setDragView(root.snapshot(null, null));
            getStyleClass().add("settings-drag-source");
            e.consume();
          });

      setOnDragOver(this::onDragOver);
      setOnDragEntered(
          e -> {
            if (canAccept(e)) {
              getStyleClass().add("settings-drag-over");
            }
          });
      setOnDragExited(e -> getStyleClass().remove("settings-drag-over"));
      setOnDragDropped(this::onDragDropped);
      setOnDragDone(
          e -> {
            getStyleClass().remove("settings-drag-source");
            e.consume();
          });
    }

    private void onDragOver(DragEvent e) {
      if (canAccept(e)) {
        e.acceptTransferModes(TransferMode.MOVE);
      }
      e.consume();
    }

    private void onDragDropped(DragEvent e) {
      Dragboard db = e.getDragboard();
      boolean success = false;
      if (db.hasString() && !isEmpty()) {
        try {
          int from = Integer.parseInt(db.getString());
          int to = getIndex();
          if (from >= 0 && from < rows.size() && to >= 0 && to < rows.size() && from != to) {
            DraftRow moved = rows.remove(from);
            rows.add(to, moved);
            success = true;
            getListView().refresh();
            onChanged.run();
          }
        } catch (NumberFormatException ignored) {
          // Invalid payload — ignore drop.
        }
      }
      e.setDropCompleted(success);
      getStyleClass().remove("settings-drag-over");
      e.consume();
    }

    private boolean canAccept(DragEvent e) {
      return e.getGestureSource() != this
          && e.getDragboard().hasString()
          && !isEmpty()
          && getItem() != null;
    }

    @Override
    protected void updateItem(DraftRow item, boolean empty) {
      super.updateItem(item, empty);
      if (empty || item == null) {
        setText(null);
        setGraphic(null);
        return;
      }
      selected.setSelected(item.selected);
      name.setText(item.name);
      if (item.selected) {
        order.setText(String.valueOf(runOrderIndex(item)));
        order.getStyleClass().remove("run-all-order-index-muted");
        name.getStyleClass().remove("run-all-order-name-muted");
      } else {
        order.setText(SettingsStrings.RUN_ALL_ORDER_SKIPPED);
        if (!order.getStyleClass().contains("run-all-order-index-muted")) {
          order.getStyleClass().add("run-all-order-index-muted");
        }
        if (!name.getStyleClass().contains("run-all-order-name-muted")) {
          name.getStyleClass().add("run-all-order-name-muted");
        }
      }
      setGraphic(root);
    }

    /** 1-based position among selected rows in list order. */
    private int runOrderIndex(DraftRow target) {
      int n = 0;
      for (DraftRow row : rows) {
        if (row.selected) {
          n++;
          if (row == target) {
            return n;
          }
        }
      }
      return 0;
    }
  }
}
