package io.github.compilerstuck.control.ui.settingsfx.customize;

import atlantafx.base.theme.Styles;
import io.github.compilerstuck.control.ui.settingsfx.SettingsLayout;
import io.github.compilerstuck.control.ui.settingsfx.SettingsStrings;
import java.util.function.DoubleFunction;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Shared layout helpers for visualization customize panels (label | control | value | reset). */
public final class CustomizePanelSupport {

  public static final double LABEL_WIDTH = 118;
  public static final double VALUE_WIDTH = 92;
  public static final double RESET_WIDTH = 56;

  private CustomizePanelSupport() {}

  public static VBox section(String title, FieldRow... rows) {
    Label heading = new Label(title);
    heading.getStyleClass().add("settings-section-label");

    GridPane body = new GridPane();
    body.getStyleClass().add("customize-field-grid");
    body.setHgap(SettingsLayout.GAP_SM);
    body.setVgap(SettingsLayout.GAP_SM);
    body.getColumnConstraints().addAll(columnConstraints());

    for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
      FieldRow fieldRow = rows[rowIndex];
      body.add(fieldRow.label(), 0, rowIndex);
      body.add(fieldRow.control(), 1, rowIndex);
      body.add(fieldRow.value(), 2, rowIndex);
      body.add(fieldRow.reset(), 3, rowIndex);
      GridPane.setHalignment(fieldRow.value(), HPos.RIGHT);
      GridPane.setHalignment(fieldRow.reset(), HPos.RIGHT);
      GridPane.setValignment(fieldRow.label(), VPos.CENTER);
      GridPane.setValignment(fieldRow.control(), VPos.CENTER);
      GridPane.setValignment(fieldRow.value(), VPos.CENTER);
      GridPane.setValignment(fieldRow.reset(), VPos.CENTER);
      GridPane.setHgrow(fieldRow.control(), Priority.ALWAYS);
    }

    VBox section = new VBox(SettingsLayout.GAP_SM, heading, body);
    section.getStyleClass().add("customize-section");
    return section;
  }

  public static ColumnConstraints[] columnConstraints() {
    ColumnConstraints labelCol = new ColumnConstraints();
    labelCol.setMinWidth(LABEL_WIDTH);
    labelCol.setPrefWidth(LABEL_WIDTH);
    labelCol.setHgrow(Priority.NEVER);

    ColumnConstraints controlCol = new ColumnConstraints();
    controlCol.setHgrow(Priority.ALWAYS);
    controlCol.setFillWidth(true);
    controlCol.setMinWidth(120);

    ColumnConstraints valueCol = new ColumnConstraints();
    valueCol.setMinWidth(VALUE_WIDTH);
    valueCol.setPrefWidth(VALUE_WIDTH);
    valueCol.setHalignment(HPos.RIGHT);

    ColumnConstraints resetCol = new ColumnConstraints();
    resetCol.setMinWidth(RESET_WIDTH);
    resetCol.setPrefWidth(RESET_WIDTH);
    resetCol.setHalignment(HPos.RIGHT);

    return new ColumnConstraints[] {labelCol, controlCol, valueCol, resetCol};
  }

  public static FieldRow sliderRow(
      String labelText, Slider slider, Label value, double defaultValue) {
    Label label = fieldLabel(labelText);
    label.setLabelFor(slider);
    Button reset = resetButton(() -> slider.setValue(defaultValue));
    return new FieldRow(label, slider, value, reset);
  }

  public static FieldRow checkboxRow(String labelText, CheckBox checkBox, boolean defaultValue) {
    Label label = fieldLabel(labelText);
    label.setLabelFor(checkBox);
    checkBox.setText("");
    Button reset = resetButton(() -> checkBox.setSelected(defaultValue));
    Region valuePlaceholder = new Region();
    valuePlaceholder.setMinWidth(VALUE_WIDTH);
    valuePlaceholder.setPrefWidth(VALUE_WIDTH);
    valuePlaceholder.setMaxWidth(VALUE_WIDTH);
    return new FieldRow(label, checkBox, valuePlaceholder, reset);
  }

  public static <T> FieldRow comboRow(String labelText, ComboBox<T> combo, T defaultValue) {
    Label label = fieldLabel(labelText);
    label.setLabelFor(combo);
    combo.setMaxWidth(Double.MAX_VALUE);
    Button reset = resetButton(() -> combo.getSelectionModel().select(defaultValue));
    Region valuePlaceholder = new Region();
    valuePlaceholder.setMinWidth(VALUE_WIDTH);
    valuePlaceholder.setPrefWidth(VALUE_WIDTH);
    valuePlaceholder.setMaxWidth(VALUE_WIDTH);
    return new FieldRow(label, combo, valuePlaceholder, reset);
  }

  public static Label fieldLabel(String text) {
    Label label = new Label(text);
    label.getStyleClass().add("customize-field-label");
    label.setMinWidth(Region.USE_PREF_SIZE);
    return label;
  }

  public static void configureSlider(Slider slider, boolean snapIntegers) {
    slider.setMaxWidth(Double.MAX_VALUE);
    slider.setShowTickMarks(false);
    slider.setShowTickLabels(false);
    slider.getStyleClass().add("customize-slider");
    if (snapIntegers) {
      slider.setMajorTickUnit(1);
      slider.setMinorTickCount(0);
      slider.setSnapToTicks(true);
      slider.setBlockIncrement(1);
    }
  }

  public static Label valueLabel() {
    Label label = new Label();
    label.getStyleClass().addAll("settings-muted", "customize-value");
    label.setMinWidth(VALUE_WIDTH);
    label.setPrefWidth(VALUE_WIDTH);
    label.setMaxWidth(VALUE_WIDTH);
    label.setAlignment(Pos.CENTER_RIGHT);
    label.setPadding(new Insets(0, SettingsLayout.GAP_XS, 0, 0));
    return label;
  }

  public static Button resetButton(Runnable action) {
    Button button = new Button(SettingsStrings.RESET_SETTING);
    button.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.SMALL, "customize-reset");
    button.setTooltip(new Tooltip(SettingsStrings.RESET_SETTING_TOOLTIP));
    button.setOnAction(e -> action.run());
    button.setMinWidth(RESET_WIDTH);
    button.setPrefWidth(RESET_WIDTH);
    button.setMaxWidth(RESET_WIDTH);
    return button;
  }

  public static void bindValueLabel(Slider slider, Label label, DoubleFunction<String> format) {
    Runnable update = () -> label.setText(format.apply(slider.getValue()));
    update.run();
    slider.valueProperty().addListener((obs, old, value) -> update.run());
  }

  public record FieldRow(Node label, Node control, Node value, Node reset) {}
}
