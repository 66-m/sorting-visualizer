package io.github._66_m.control.ui.settingsfx.customize;

import io.github._66_m.control.config.visual.SettingsSchema;
import io.github._66_m.control.config.visual.SettingsSchema.BoolParam;
import io.github._66_m.control.config.visual.SettingsSchema.DoubleParam;
import io.github._66_m.control.config.visual.SettingsSchema.EnumParam;
import io.github._66_m.control.config.visual.SettingsSchema.IntParam;
import io.github._66_m.control.config.visual.SettingsSchema.Param;
import io.github._66_m.control.config.visual.VisualizationSettings;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

/**
 * Draft editor generated from a {@link SettingsSchema}: one row per param (slider, checkbox or
 * combo box) grouped into the schema's sections, on the shared 4-column customize grid.
 */
public final class SchemaCustomizePanel<S extends VisualizationSettings>
    implements VisualizationCustomizePanel {

  private final SettingsSchema<S> schema;
  private final Map<Param, Control> controls = new LinkedHashMap<>();
  private final CustomizePanelSupport.DraftSession draft = new CustomizePanelSupport.DraftSession();

  public SchemaCustomizePanel(SettingsSchema<S> schema) {
    this.schema = schema;
    // Controls exist before build() so load()/toSettings() work on an unbuilt panel.
    for (Param param : schema.params()) {
      controls.put(param, createControl(param));
    }
  }

  private static Control createControl(Param param) {
    return switch (param) {
      case DoubleParam d -> new Slider(d.min(), d.max(), d.defaultValue());
      case IntParam i -> new Slider(i.min(), i.max(), i.defaultValue());
      case BoolParam b -> new CheckBox();
      case EnumParam e -> {
        ComboBox<Enum<?>> combo = new ComboBox<>();
        combo.getItems().setAll(e.constants());
        yield combo;
      }
    };
  }

  @Override
  public Node build() {
    Map<String, List<CustomizePanelSupport.FieldRow>> rowsBySection = new LinkedHashMap<>();
    for (String section : schema.sections()) {
      rowsBySection.put(section, new ArrayList<>());
    }
    for (Param param : schema.params()) {
      rowsBySection.get(param.section()).add(buildRow(param));
    }

    Node[] sections =
        rowsBySection.entrySet().stream()
            .map(
                e ->
                    CustomizePanelSupport.section(
                        e.getKey(), e.getValue().toArray(CustomizePanelSupport.FieldRow[]::new)))
            .toArray(Node[]::new);
    return CustomizePanelSupport.panelRoot(sections);
  }

  @SuppressWarnings("unchecked")
  private CustomizePanelSupport.FieldRow buildRow(Param param) {
    Control control = controls.get(param);
    return switch (param) {
      case DoubleParam d -> {
        Slider slider = (Slider) control;
        Label value = CustomizePanelSupport.valueLabel();
        CustomizePanelSupport.configureSlider(slider, false);
        CustomizePanelSupport.bindValueLabel(slider, value, v -> String.format(d.format(), v));
        draft.bind(slider.valueProperty());
        yield CustomizePanelSupport.sliderRow(d.label(), slider, value, d.defaultValue());
      }
      case IntParam i -> {
        Slider slider = (Slider) control;
        Label value = CustomizePanelSupport.valueLabel();
        CustomizePanelSupport.configureSlider(slider, true);
        CustomizePanelSupport.bindValueLabel(
            slider, value, v -> String.format("%d", Math.round(v)));
        draft.bind(slider.valueProperty());
        yield CustomizePanelSupport.sliderRow(i.label(), slider, value, i.defaultValue());
      }
      case BoolParam b -> {
        CheckBox checkBox = (CheckBox) control;
        draft.bind(checkBox.selectedProperty());
        yield CustomizePanelSupport.checkboxRow(b.label(), checkBox, b.defaultValue());
      }
      case EnumParam e -> {
        ComboBox<Enum<?>> combo = (ComboBox<Enum<?>>) control;
        combo.getSelectionModel().select(e.defaultValue());
        draft.bind(combo.getSelectionModel().selectedItemProperty());
        yield CustomizePanelSupport.comboRow(e.label(), combo, e.defaultValue());
      }
    };
  }

  @Override
  public void load(VisualizationSettings settings) {
    S typed = CustomizePanelSupport.castOrDefaults(settings, schema.type(), schema::defaults);
    CustomizePanelSupport.whileLoading(
        draft,
        () -> {
          for (Map.Entry<Param, Control> e : controls.entrySet()) {
            setControl(e.getKey(), e.getValue(), schema.get(typed, e.getKey()));
          }
        });
  }

  @SuppressWarnings("unchecked")
  private static void setControl(Param param, Control control, Object value) {
    switch (param) {
      case DoubleParam d -> ((Slider) control).setValue((Double) value);
      case IntParam i -> ((Slider) control).setValue((Integer) value);
      case BoolParam b -> ((CheckBox) control).setSelected((Boolean) value);
      case EnumParam e -> ((ComboBox<Enum<?>>) control).getSelectionModel().select((Enum<?>) value);
    }
  }

  @Override
  public VisualizationSettings toSettings() {
    return schema.create(param -> readControl(param, controls.get(param)));
  }

  private static Object readControl(Param param, Control control) {
    return switch (param) {
      case DoubleParam d -> ((Slider) control).getValue();
      case IntParam i -> (int) Math.round(((Slider) control).getValue());
      case BoolParam b -> ((CheckBox) control).isSelected();
      case EnumParam e -> ((ComboBox<?>) control).getSelectionModel().getSelectedItem();
    };
  }

  @Override
  public VisualizationSettings defaults() {
    return schema.defaults();
  }

  @Override
  public void setOnDraftChanged(Runnable listener) {
    draft.setListener(listener);
  }
}
