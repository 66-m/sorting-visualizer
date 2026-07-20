package io.github.compilerstuck.control.ui.settingsfx;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Shared field chrome for the Settings Run Deck. */
public final class SettingsControls {

  private SettingsControls() {}

  public static Label fieldLabel(String text) {
    Label label = new Label(text);
    label.getStyleClass().add("settings-field-label");
    label.setMinWidth(Region.USE_PREF_SIZE);
    return label;
  }

  public static Label valueLabel() {
    Label label = new Label();
    label.getStyleClass().add("settings-value");
    label.setMinWidth(Region.USE_PREF_SIZE);
    label.setAlignment(Pos.CENTER_RIGHT);
    return label;
  }

  public static Label mutedLabel(String text) {
    Label label = new Label(text);
    label.getStyleClass().add("settings-muted");
    label.setWrapText(true);
    return label;
  }

  /** Field label left, live value right — used above sliders. */
  public static HBox labelValueHeader(String fieldText, Label value) {
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox header = new HBox(SettingsLayout.GAP_SM, fieldLabel(fieldText), spacer, value);
    header.setAlignment(Pos.CENTER_LEFT);
    return header;
  }

  /** Label stacked above a full-width control. */
  public static VBox labeledField(String fieldText, Node control) {
    Label label = fieldLabel(fieldText);
    if (control instanceof javafx.scene.control.Control c) {
      label.setLabelFor(c);
    }
    if (control instanceof Region region) {
      region.setMaxWidth(Double.MAX_VALUE);
    }
    return new VBox(SettingsLayout.GAP_XS, label, control);
  }

  /**
   * Primary control grows; secondary action stays fixed on the right (Size+Apply, combo+Customize,
   * path+Browse).
   */
  public static HBox controlWithAction(Node control, Node action) {
    if (control instanceof Region region) {
      region.setMaxWidth(Double.MAX_VALUE);
    }
    HBox row = new HBox(SettingsLayout.GAP_SM, control, action);
    row.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(control, Priority.ALWAYS);
    return row;
  }
}
