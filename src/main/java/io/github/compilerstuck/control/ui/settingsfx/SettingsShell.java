package io.github.compilerstuck.control.ui.settingsfx;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Run Deck Settings shell: header + two-column body + sticky action bar. Section content is
 * supplied by {@link SectionNodes}.
 */
public final class SettingsShell {

  public static final String TITLE_ID = "settings-title";
  public static final String RUN_BUTTON_ID = "settings-run";
  public static final String CANCEL_BUTTON_ID = "settings-cancel";
  public static final String PROGRESS_ID = "settings-progress";

  private SettingsShell() {}

  /** Builds the shell with empty placeholder sections (TestFX / inspection). */
  public static ShellResult build() {
    return build(
        new SectionNodes(
            placeholder(),
            placeholder(),
            placeholder(),
            placeholder(),
            placeholder(),
            placeholder()));
  }

  /** Builds the root node and exposes action-bar controls for wiring. */
  public static ShellResult build(SectionNodes sections) {
    BorderPane root = new BorderPane();
    root.getStyleClass().add("settings-root");
    root.setPadding(new Insets(SettingsLayout.GAP_COL));
    root.setTop(buildHeader());
    root.setCenter(buildBody(sections));

    ProgressBar progress = new ProgressBar(0);
    progress.setId(PROGRESS_ID);
    progress.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(progress, Priority.ALWAYS);

    Button cancel = new Button(SettingsStrings.CANCEL);
    cancel.setId(CANCEL_BUTTON_ID);
    cancel.getStyleClass().add(Styles.BUTTON_OUTLINED);
    cancel.setDisable(true);

    Button run = new Button(SettingsStrings.RUN);
    run.setId(RUN_BUTTON_ID);
    run.getStyleClass().add(Styles.ACCENT);
    run.setDefaultButton(true);

    HBox bar = new HBox(SettingsLayout.GAP_MD, progress, cancel, run);
    bar.setAlignment(Pos.CENTER_LEFT);
    bar.getStyleClass().add("settings-action-bar");
    root.setBottom(bar);

    return new ShellResult(root, progress, run, cancel);
  }

  private static Node buildHeader() {
    Label title = new Label(SettingsStrings.TITLE);
    title.setId(TITLE_ID);
    title.getStyleClass().add("settings-title");

    Label hint = new Label(SettingsStrings.WINDOW_HINT);
    hint.getStyleClass().add("settings-window-hint");
    hint.setWrapText(true);

    VBox header = new VBox(SettingsLayout.GAP_SM, title, hint);
    header.getStyleClass().add("settings-header");
    header.setPadding(new Insets(0, 0, SettingsLayout.GAP_LG, 0));
    return header;
  }

  private static Node buildBody(SectionNodes sections) {
    VBox left =
        column(
            section(SettingsStrings.SECTION_ARRAY_SIZE, sections.arraySize()),
            section(SettingsStrings.SECTION_SORTING, sections.sorting()),
            section(SettingsStrings.SECTION_SPEED, sections.speed()));
    VBox right =
        column(
            section(SettingsStrings.SECTION_VISUALIZATION, sections.visualization()),
            section(SettingsStrings.SECTION_APPEARANCE, sections.appearance()),
            section(SettingsStrings.SECTION_OPTIONS, sections.options()));

    HBox columns = new HBox(SettingsLayout.GAP_COL, left, right);
    columns.setAlignment(Pos.TOP_LEFT);
    HBox.setHgrow(left, Priority.ALWAYS);
    HBox.setHgrow(right, Priority.ALWAYS);
    left.setMaxWidth(Double.MAX_VALUE);
    right.setMaxWidth(Double.MAX_VALUE);

    ScrollPane scroll = new ScrollPane(columns);
    scroll.setFitToWidth(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroll.setFocusTraversable(false);
    return scroll;
  }

  private static VBox column(Node... sectionNodes) {
    VBox col = new VBox(SettingsLayout.GAP_XL, sectionNodes);
    col.setFillWidth(true);
    col.setAlignment(Pos.TOP_LEFT);
    return col;
  }

  private static Node section(String labelText, Node content) {
    Label label = new Label(labelText);
    label.getStyleClass().add("settings-section-label");
    VBox block = new VBox(SettingsLayout.GAP_SM, label, content);
    block.getStyleClass().add("settings-section");
    block.setFillWidth(true);
    return block;
  }

  private static Node placeholder() {
    Label empty = new Label("");
    empty.setMinHeight(24);
    return empty;
  }
}
