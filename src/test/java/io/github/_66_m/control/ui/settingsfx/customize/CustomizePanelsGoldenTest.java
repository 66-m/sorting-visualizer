package io.github._66_m.control.ui.settingsfx.customize;

import io.github._66_m.control.config.visual.VisualizationSettings;
import io.github._66_m.control.config.visual.VisualizationSettingsCodec;
import io.github._66_m.control.config.visual.VisualizationSettingsGolden;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Pins what every visualization's customize panel shows and does: layout, labels, slider ranges,
 * value formatting, load/save round-trips, reset buttons and draft-change events.
 */
class CustomizePanelsGoldenTest {

  @BeforeAll
  static void startToolkit() {
    try {
      Platform.startup(() -> {});
    } catch (IllegalStateException alreadyStarted) {
      // Another test already started the JavaFX toolkit.
    }
  }

  @Test
  void panelsMatchGoldenFile() throws Exception {
    StringBuilder out = new StringBuilder();
    for (Class<? extends VisualizationSettings> type :
        VisualizationSettingsGolden.settingsTypes()) {
      String id = VisualizationSettingsGolden.idOf(type);
      out.append("== ").append(id).append('\n');
      Supplier<VisualizationCustomizePanel> factory =
          VisualizationCustomizePanels.forId(id).orElse(null);
      if (factory == null) {
        out.append("(no panel)\n");
        continue;
      }
      out.append(onFxThread(() -> describePanel(type, factory)));
    }
    VisualizationSettingsGolden.assertGolden("customize-panels.txt", out.toString());
  }

  private static String describePanel(
      Class<? extends VisualizationSettings> type, Supplier<VisualizationCustomizePanel> factory) {
    StringBuilder out = new StringBuilder();
    VisualizationSettings defaults = VisualizationSettingsGolden.defaultsOf(type);
    VisualizationSettings nonDefault = VisualizationSettingsGolden.nonDefault(type);

    VisualizationCustomizePanel panel = factory.get();
    AtomicInteger draftEvents = new AtomicInteger();
    Node root = panel.build();
    panel.setOnDraftChanged(draftEvents::incrementAndGet);
    out.append("panel.defaults ").append(encode(panel.defaults())).append('\n');

    panel.load(defaults);
    dump(root, 0, out);
    out.append("afterLoadDefaults ").append(encode(panel.toSettings())).append('\n');

    panel.load(nonDefault);
    out.append("afterLoadNonDefault ").append(encode(panel.toSettings())).append('\n');
    out.append("valueLabels ");
    appendValueLabels(root, out);
    out.append('\n');
    out.append("draftEventsDuringLoad ").append(draftEvents.get()).append('\n');

    int resets = fireResets(root);
    out.append("resetButtons ").append(resets).append('\n');
    out.append("afterResets ").append(encode(panel.toSettings())).append('\n');
    out.append("draftEventsFromResets>0 ").append(draftEvents.get() > 0).append('\n');
    out.append("valid ")
        .append(panel.isValid())
        .append(" '")
        .append(panel.validationMessage())
        .append("'\n");
    return out.toString();
  }

  private static String encode(VisualizationSettings settings) {
    return VisualizationSettingsCodec.encodeEnvelope(settings);
  }

  private static void dump(Node node, int depth, StringBuilder out) {
    out.append("  ".repeat(depth)).append(describe(node));
    if (node.getParent() instanceof GridPane) {
      out.append(" @")
          .append(GridPane.getRowIndex(node))
          .append(',')
          .append(GridPane.getColumnIndex(node));
    }
    out.append('\n');
    if (node instanceof Parent parent && !(node instanceof javafx.scene.control.Control)) {
      for (Node child : parent.getChildrenUnmodifiable()) {
        dump(child, depth + 1, out);
      }
    }
  }

  private static String describe(Node node) {
    String styles = node.getStyleClass().isEmpty() ? "" : " " + node.getStyleClass();
    if (node instanceof Slider s) {
      return "Slider min="
          + s.getMin()
          + " max="
          + s.getMax()
          + " value="
          + s.getValue()
          + " snap="
          + s.isSnapToTicks()
          + " major="
          + s.getMajorTickUnit()
          + " minor="
          + s.getMinorTickCount()
          + " block="
          + s.getBlockIncrement()
          + styles;
    }
    if (node instanceof CheckBox c) {
      return "CheckBox selected=" + c.isSelected() + " text='" + c.getText() + "'" + styles;
    }
    if (node instanceof ComboBox<?> c) {
      return "ComboBox items="
          + c.getItems()
          + " selected="
          + c.getSelectionModel().getSelectedItem()
          + styles;
    }
    if (node instanceof Button b) {
      return "Button '"
          + b.getText()
          + "' tooltip='"
          + (b.getTooltip() == null ? "" : b.getTooltip().getText())
          + "'"
          + styles;
    }
    if (node instanceof Label l) {
      return "Label '"
          + l.getText()
          + "'"
          + (l.getLabelFor() == null ? "" : " for=" + l.getLabelFor().getClass().getSimpleName())
          + styles;
    }
    return node.getClass().getSimpleName() + styles;
  }

  private static void appendValueLabels(Node node, StringBuilder out) {
    if (node instanceof Label l && l.getStyleClass().contains("customize-value")) {
      out.append('[').append(l.getText()).append(']');
    }
    if (node instanceof Parent parent) {
      for (Node child : parent.getChildrenUnmodifiable()) {
        appendValueLabels(child, out);
      }
    }
  }

  private static int fireResets(Node node) {
    int count = 0;
    if (node instanceof Button b && b.getStyleClass().contains("customize-icon-reset")) {
      b.fire();
      count++;
    }
    if (node instanceof Parent parent && !(node instanceof Button)) {
      for (Node child : parent.getChildrenUnmodifiable()) {
        count += fireResets(child);
      }
    }
    return count;
  }

  private static <T> T onFxThread(Supplier<T> action) throws Exception {
    CompletableFuture<T> result = new CompletableFuture<>();
    Platform.runLater(
        () -> {
          try {
            result.complete(action.get());
          } catch (Throwable t) {
            result.completeExceptionally(t);
          }
        });
    return result.get(30, TimeUnit.SECONDS);
  }
}
