package io.github.compilerstuck.control.ui.settingsfx.customize;

import atlantafx.base.theme.Styles;
import io.github.compilerstuck.control.config.visual.CubeSettings;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.ui.settingsfx.SettingsLayout;
import io.github.compilerstuck.control.ui.settingsfx.SettingsStrings;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Draft editor for {@link CubeSettings}.
 *
 * <p>Layout: sectioned form on a 4-column grid (label | control | value | reset) so every row
 * shares the same alignment and density.
 */
public final class CubeCustomizePanel implements VisualizationCustomizePanel {

  private static final double LABEL_WIDTH = 118;
  private static final double VALUE_WIDTH = 92;
  private static final double RESET_WIDTH = 56;

  private final Slider rotationSpeed =
      new Slider(
          CubeSettings.ROTATION_SPEED_MIN,
          CubeSettings.ROTATION_SPEED_MAX,
          CubeSettings.DEFAULT_ROTATION_SPEED);
  private final Slider fillOpacity =
      new Slider(
          CubeSettings.FILL_OPACITY_MIN,
          CubeSettings.FILL_OPACITY_MAX,
          CubeSettings.DEFAULT_FILL_OPACITY);
  private final Slider sceneScale =
      new Slider(
          CubeSettings.SCENE_SCALE_DIVISOR_MIN,
          CubeSettings.SCENE_SCALE_DIVISOR_MAX,
          CubeSettings.DEFAULT_SCENE_SCALE_DIVISOR);
  private final CheckBox wireframe = new CheckBox();
  private final Label rotationValue = valueLabel();
  private final Label fillOpacityValue = valueLabel();
  private final Label sceneScaleValue = valueLabel();
  private Runnable onDraftChanged = () -> {};
  private boolean loading;

  @Override
  public Node build() {
    configureSlider(rotationSpeed, false);
    configureSlider(fillOpacity, true);
    configureSlider(sceneScale, false);

    bindValueLabel(rotationSpeed, rotationValue, v -> String.format("%.2f rad/s", v));
    bindValueLabel(fillOpacity, fillOpacityValue, v -> String.format("%d", Math.round(v)));
    bindValueLabel(sceneScale, sceneScaleValue, v -> String.format("%.2f", v));
    bindDraftChanges();

    VBox motion =
        section(
            SettingsStrings.CUBE_SECTION_MOTION,
            sliderRow(
                SettingsStrings.CUBE_ROTATION_SPEED,
                rotationSpeed,
                rotationValue,
                CubeSettings.DEFAULT_ROTATION_SPEED));

    VBox appearance =
        section(
            SettingsStrings.CUBE_SECTION_APPEARANCE,
            sliderRow(
                SettingsStrings.CUBE_FILL_OPACITY,
                fillOpacity,
                fillOpacityValue,
                CubeSettings.DEFAULT_FILL_OPACITY),
            checkboxRow(SettingsStrings.CUBE_WIREFRAME, wireframe, CubeSettings.DEFAULT_WIREFRAME));

    VBox frame =
        section(
            SettingsStrings.CUBE_SECTION_FRAME,
            sliderRow(
                SettingsStrings.CUBE_SCENE_SCALE,
                sceneScale,
                sceneScaleValue,
                CubeSettings.DEFAULT_SCENE_SCALE_DIVISOR));

    VBox root = new VBox(SettingsLayout.GAP_MD, frame, motion, appearance);
    root.getStyleClass().add("customize-panel");
    root.setFillWidth(true);
    return root;
  }

  @Override
  public void load(VisualizationSettings settings) {
    CubeSettings cube = settings instanceof CubeSettings c ? c : CubeSettings.defaults();
    loading = true;
    try {
      rotationSpeed.setValue(cube.rotationSpeedRadPerSec());
      fillOpacity.setValue(cube.fillOpacity());
      sceneScale.setValue(cube.sceneScaleDivisor());
      wireframe.setSelected(cube.wireframeEnabled());
    } finally {
      loading = false;
    }
  }

  @Override
  public VisualizationSettings toSettings() {
    return new CubeSettings(
        rotationSpeed.getValue(),
        (int) Math.round(fillOpacity.getValue()),
        wireframe.isSelected(),
        sceneScale.getValue());
  }

  @Override
  public VisualizationSettings defaults() {
    return CubeSettings.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    onDraftChanged = listener != null ? listener : () -> {};
  }

  private void bindDraftChanges() {
    rotationSpeed.valueProperty().addListener((obs, old, value) -> fireDraftChanged());
    fillOpacity.valueProperty().addListener((obs, old, value) -> fireDraftChanged());
    sceneScale.valueProperty().addListener((obs, old, value) -> fireDraftChanged());
    wireframe.selectedProperty().addListener((obs, old, value) -> fireDraftChanged());
  }

  private void fireDraftChanged() {
    if (!loading) {
      onDraftChanged.run();
    }
  }

  private static VBox section(String title, FieldRow... rows) {
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

  private static ColumnConstraints[] columnConstraints() {
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

  private static FieldRow sliderRow(
      String labelText, Slider slider, Label value, double defaultValue) {
    Label label = fieldLabel(labelText);
    label.setLabelFor(slider);
    Button reset = resetButton(() -> slider.setValue(defaultValue));
    return new FieldRow(label, slider, value, reset);
  }

  private static FieldRow checkboxRow(String labelText, CheckBox checkBox, boolean defaultValue) {
    Label label = fieldLabel(labelText);
    label.setLabelFor(checkBox);
    checkBox.setText("");
    Button reset = resetButton(() -> checkBox.setSelected(defaultValue));
    // Keep the value column for column alignment (empty placeholder).
    Region valuePlaceholder = new Region();
    valuePlaceholder.setMinWidth(VALUE_WIDTH);
    valuePlaceholder.setPrefWidth(VALUE_WIDTH);
    valuePlaceholder.setMaxWidth(VALUE_WIDTH);
    return new FieldRow(label, checkBox, valuePlaceholder, reset);
  }

  private static Label fieldLabel(String text) {
    Label label = new Label(text);
    label.getStyleClass().add("customize-field-label");
    label.setMinWidth(Region.USE_PREF_SIZE);
    return label;
  }

  private static void configureSlider(Slider slider, boolean snapIntegers) {
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

  private static Label valueLabel() {
    Label label = new Label();
    label.getStyleClass().addAll("settings-muted", "customize-value");
    label.setMinWidth(VALUE_WIDTH);
    label.setPrefWidth(VALUE_WIDTH);
    label.setMaxWidth(VALUE_WIDTH);
    label.setAlignment(Pos.CENTER_RIGHT);
    label.setPadding(new Insets(0, SettingsLayout.GAP_XS, 0, 0));
    return label;
  }

  private static Button resetButton(Runnable action) {
    Button button = new Button(SettingsStrings.RESET_SETTING);
    button.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.SMALL, "customize-reset");
    button.setTooltip(new Tooltip(SettingsStrings.RESET_SETTING_TOOLTIP));
    button.setOnAction(e -> action.run());
    button.setMinWidth(RESET_WIDTH);
    button.setPrefWidth(RESET_WIDTH);
    button.setMaxWidth(RESET_WIDTH);
    return button;
  }

  private static void bindValueLabel(
      Slider slider, Label label, java.util.function.DoubleFunction<String> format) {
    Runnable update = () -> label.setText(format.apply(slider.getValue()));
    update.run();
    slider.valueProperty().addListener((obs, old, value) -> update.run());
  }

  private record FieldRow(Node label, Node control, Node value, Node reset) {}
}
