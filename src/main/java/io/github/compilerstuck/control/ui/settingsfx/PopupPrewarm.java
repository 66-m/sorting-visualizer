package io.github.compilerstuck.control.ui.settingsfx;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.util.Duration;

/**
 * Forces ComboBox / ColorPicker popup skins to build once. With a live NEWT/OpenGL canvas, the
 * first {@code show()} on these controls is otherwise ~0.5–1s on Linux; later opens are cheap.
 */
final class PopupPrewarm {

  /** Style class toggled on the scene root while popups are opened invisibly during warm-up. */
  static final String PREWARMING_CLASS = "prewarming-popups";

  private PopupPrewarm() {}

  /** Opens+closes every popup under {@code root} synchronously (blocks the FX thread). */
  static void warmControls(Parent root) {
    root.applyCss();
    root.layout();
    for (Node control : collectPopupControls(root)) {
      showHide(control);
    }
  }

  /**
   * Opens+closes popups one control per pulse so the FX thread stays responsive. Adds {@link
   * #PREWARMING_CLASS} for the duration so AtlantaFX popups stay invisible (no flash).
   */
  static void warmControlsAsync(Parent root) {
    root.applyCss();
    root.layout();
    List<Node> controls = collectPopupControls(root);
    if (controls.isEmpty()) {
      return;
    }
    root.getStyleClass().add(PREWARMING_CLASS);
    Iterator<Node> it = controls.iterator();
    PauseTransition pulse = new PauseTransition(Duration.millis(1));
    pulse.setOnFinished(
        e -> {
          if (!it.hasNext()) {
            root.getStyleClass().remove(PREWARMING_CLASS);
            return;
          }
          showHide(it.next());
          pulse.playFromStart();
        });
    pulse.play();
  }

  private static void showHide(Node control) {
    if (control instanceof ComboBox<?> combo) {
      combo.show();
      combo.hide();
    } else if (control instanceof ColorPicker color) {
      color.show();
      color.hide();
    }
  }

  /** ComboBoxes first (algorithm/viz), then ColorPickers. */
  private static List<Node> collectPopupControls(Parent root) {
    List<ComboBox<?>> combos = new ArrayList<>();
    List<ColorPicker> colors = new ArrayList<>();
    collect(root, combos, colors);
    List<Node> out = new ArrayList<>(combos.size() + colors.size());
    out.addAll(combos);
    out.addAll(colors);
    return out;
  }

  private static void collect(Node node, List<ComboBox<?>> combos, List<ColorPicker> colors) {
    if (node instanceof ComboBox<?> combo) {
      combos.add(combo);
    } else if (node instanceof ColorPicker color) {
      colors.add(color);
    }

    // ScrollPane / TabPane / TitledPane keep content outside getChildrenUnmodifiable().
    if (node instanceof ScrollPane scroll && scroll.getContent() != null) {
      collect(scroll.getContent(), combos, colors);
    }
    if (node instanceof TitledPane titled && titled.getContent() != null) {
      collect(titled.getContent(), combos, colors);
    }
    if (node instanceof TabPane tabs) {
      for (Tab tab : tabs.getTabs()) {
        if (tab.getContent() != null) {
          collect(tab.getContent(), combos, colors);
        }
      }
    }

    if (node instanceof Parent parent) {
      for (Node child : parent.getChildrenUnmodifiable()) {
        collect(child, combos, colors);
      }
    }
  }
}
