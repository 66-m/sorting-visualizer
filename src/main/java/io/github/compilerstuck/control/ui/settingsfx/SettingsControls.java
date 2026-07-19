package io.github.compilerstuck.control.ui.settingsfx;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

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
}
