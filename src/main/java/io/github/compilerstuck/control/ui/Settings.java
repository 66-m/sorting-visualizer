package io.github.compilerstuck.control.ui;

import io.github.compilerstuck.control.AppContext;
import io.github.compilerstuck.control.config.MainControllerConfig;
import io.github.compilerstuck.control.ui.settings.ActionBar;
import io.github.compilerstuck.control.ui.settings.ArraySizePanel;
import io.github.compilerstuck.control.ui.settings.DisplayPanel;
import io.github.compilerstuck.control.ui.settings.GradientPanel;
import io.github.compilerstuck.control.ui.settings.SettingsUi;
import io.github.compilerstuck.control.ui.settings.SortingPanel;
import io.github.compilerstuck.control.ui.settings.SoundPanel;
import io.github.compilerstuck.control.ui.settings.SpeedPanel;
import io.github.compilerstuck.control.ui.settings.VisualizationPanel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

/**
 * Settings window coordinator. Panel content lives under {@link
 * io.github.compilerstuck.control.ui.settings}.
 */
public class Settings extends JFrame {

  private final AppContext app;
  private final Rectangle launchScreenBounds;

  private ArraySizePanel arraySizePanel;
  private SortingPanel sortingPanel;
  private SpeedPanel speedPanel;
  private VisualizationPanel visualizationPanel;
  private DisplayPanel displayPanel;
  private ActionBar actionBar;

  public Settings(AppContext app, Rectangle launchScreenBounds)
      throws UnsupportedLookAndFeelException,
          ClassNotFoundException,
          InstantiationException,
          IllegalAccessException {
    this.app = app;
    this.launchScreenBounds =
        launchScreenBounds != null
            ? new Rectangle(launchScreenBounds)
            : GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration()
                .getBounds();
    initialize();
  }

  public void initialize() {
    setTitle("Sorting Algorithm Visualizer - Settings");
    AppIcons.applySettingsTo(this);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setResizable(true);

    addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent windowEvent) {
            app.shutdown();
          }
        });

    int targetWidth = Math.max(1, launchScreenBounds.width / 2);
    // Half-screen height clips the form on common displays; allow up to ~90% tall.
    int maxHeight = Math.max(1, (int) Math.round(launchScreenBounds.height * 0.9));

    JComponent mainPanel = createMainUI();
    setContentPane(mainPanel);
    pack();

    // Preferred height from pack() is for pack-width; remeasure at the launch width.
    Insets insets = getInsets();
    int innerWidth = Math.max(1, targetWidth - insets.left - insets.right);
    mainPanel.setSize(innerWidth, Short.MAX_VALUE);
    int neededHeight = mainPanel.getPreferredSize().height + insets.top + insets.bottom;
    int h =
        Math.min(
            maxHeight, Math.max(MainControllerConfig.SETTINGS_MIN_HEIGHT, neededHeight));
    setSize(targetWidth, h);
    setMinimumSize(
        new Dimension(
            Math.min(MainControllerConfig.SETTINGS_MIN_WIDTH, targetWidth),
            Math.min(MainControllerConfig.SETTINGS_MIN_HEIGHT, h)));
    setLocation(
        launchScreenBounds.x + Math.max(0, (launchScreenBounds.width - targetWidth) / 2),
        launchScreenBounds.y + Math.max(0, (launchScreenBounds.height - h) / 2));

    setVisible(true);
  }

  private JComponent createMainUI() {
    sortingPanel = new SortingPanel(app, this);
    visualizationPanel = new VisualizationPanel(app);
    speedPanel = new SpeedPanel(app);
    actionBar =
        new ActionBar(
            app,
            sortingPanel::isRunAllSelected,
            sortingPanel::getAlgorithmList,
            sortingPanel::getSelectedAlgorithmIndex);
    arraySizePanel =
        new ArraySizePanel(
            app, visualizationPanel::currentConstraints, actionBar::setRunEnabled);
    visualizationPanel.setSizeDisplaySync(arraySizePanel::syncDisplayedSize);
    arraySizePanel.syncDisplayedSize(app.getSize());

    GradientPanel gradientPanel = new GradientPanel(app);
    displayPanel =
        new DisplayPanel(app, this, actionBar::isCancelEnabled, actionBar::setCancelEnabled);
    SoundPanel soundPanel = new SoundPanel(app);

    JPanel root = new JPanel(new BorderLayout());
    root.setBackground(UiTheme.BG_PRIMARY);
    root.setBorder(
        BorderFactory.createEmptyBorder(
            UiTheme.SPACING_LG, UiTheme.SPACING_LG, UiTheme.SPACING_LG, UiTheme.SPACING_LG));

    root.add(createHeaderPanel(), BorderLayout.NORTH);

    ViewportWidthPanel columns = new ViewportWidthPanel();
    columns.setLayout(new GridLayout(1, 2, UiTheme.SPACING_LG, 0));
    columns.setBackground(UiTheme.BG_PRIMARY);
    columns.setBorder(BorderFactory.createEmptyBorder(UiTheme.SPACING_MD, 0, 0, 0));
    columns.add(createLeftColumn(arraySizePanel, sortingPanel, speedPanel));
    columns.add(createRightColumn(visualizationPanel, gradientPanel, displayPanel, soundPanel));

    JScrollPane scroll = new JScrollPane(columns);
    scroll.setBorder(null);
    scroll.getViewport().setBackground(UiTheme.BG_PRIMARY);
    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    root.add(scroll, BorderLayout.CENTER);
    root.add(actionBar.getPanel(), BorderLayout.SOUTH);
    return root;
  }

  /**
   * Scrollable panel that always matches the viewport width so child layouts reflow when the frame
   * is resized, while still allowing a vertical scrollbar when content is taller than the window.
   */
  private static final class ViewportWidthPanel extends JPanel implements Scrollable {
    ViewportWidthPanel() {
      super();
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
      return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
      return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
      return Math.max(visibleRect.height * 9 / 10, 1);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
      return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
      return false;
    }
  }

  private JPanel createLeftColumn(
      ArraySizePanel arraySize, SortingPanel sorting, SpeedPanel speed) {
    JPanel col = new JPanel();
    col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
    col.setBackground(UiTheme.BG_PRIMARY);

    StyledCard arraySizeCard = arraySize.getCard();
    StyledCard sortingCard = sorting.getCard();
    StyledCard speedCard = speed.getCard();
    for (StyledCard c : new StyledCard[] {arraySizeCard, sortingCard, speedCard}) {
      prepareCard(c);
    }

    col.add(SettingsUi.createSectionLabel("Array Size"));
    col.add(arraySizeCard);
    col.add(Box.createVerticalStrut(UiTheme.SPACING_LG));
    col.add(SettingsUi.createSectionLabel("Sorting"));
    col.add(sortingCard);
    col.add(Box.createVerticalStrut(UiTheme.SPACING_LG));
    col.add(SettingsUi.createSectionLabel("Speed"));
    col.add(speedCard);
    return col;
  }

  private JPanel createRightColumn(
      VisualizationPanel visualization,
      GradientPanel gradient,
      DisplayPanel display,
      SoundPanel sound) {
    JPanel col = new JPanel();
    col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
    col.setBackground(UiTheme.BG_PRIMARY);

    StyledCard vizCard = visualization.getCard();
    StyledCard gradientCard = gradient.getCard();
    StyledCard displayCard = display.getCard();
    StyledCard soundCard = sound.getCard();
    for (StyledCard c : new StyledCard[] {vizCard, gradientCard, displayCard, soundCard}) {
      prepareCard(c);
    }

    col.add(SettingsUi.createSectionLabel("Visualization"));
    col.add(vizCard);
    col.add(Box.createVerticalStrut(UiTheme.SPACING_LG));
    col.add(SettingsUi.createSectionLabel("Gradient"));
    col.add(gradientCard);
    col.add(Box.createVerticalStrut(UiTheme.SPACING_LG));
    col.add(SettingsUi.createSectionLabel("Display"));
    col.add(displayCard);
    col.add(Box.createVerticalStrut(UiTheme.SPACING_LG));
    col.add(SettingsUi.createSectionLabel("Sound"));
    col.add(soundCard);
    return col;
  }

  private static void prepareCard(StyledCard card) {
    card.setAlignmentX(Component.LEFT_ALIGNMENT);
    // Allow height to grow when the column narrows and controls wrap.
    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
  }

  private JPanel createHeaderPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(UiTheme.BG_PRIMARY);
    panel.setBorder(BorderFactory.createEmptyBorder(0, 0, UiTheme.SPACING_MD, 0));

    JLabel title = new JLabel("Sorting Visualizer");
    title.setFont(UiTheme.FONT_TITLE);
    title.setForeground(UiTheme.TEXT_PRIMARY);
    panel.add(title, BorderLayout.WEST);

    JLabel subtitle = new JLabel("Settings");
    subtitle.setFont(UiTheme.FONT_BODY);
    subtitle.setForeground(UiTheme.TEXT_SECONDARY);
    panel.add(subtitle, BorderLayout.EAST);
    return panel;
  }

  public void setEnableInputs(boolean enabled) {
    arraySizePanel.setInputsEnabled(enabled);
    sortingPanel.setInputsEnabled(enabled);
    actionBar.setRunEnabled(enabled);
    visualizationPanel.setInputsEnabled(enabled);
    speedPanel.setInputsEnabled(enabled);
    if (enabled && displayPanel != null) {
      displayPanel.refreshExportEnabled(app);
    }
  }

  public void setEnableCancelButton(boolean enabled) {
    actionBar.setCancelEnabled(enabled);
  }

  public void setProgressBar(int progress) {
    actionBar.setProgress(progress);
  }
}
