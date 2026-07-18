package io.github.compilerstuck.control.ui;

import io.github.compilerstuck.control.AppContext;
import io.github.compilerstuck.control.ui.settings.ActionBar;
import io.github.compilerstuck.control.ui.settings.ArraySizePanel;
import io.github.compilerstuck.control.ui.settings.DisplayPanel;
import io.github.compilerstuck.control.ui.settings.GradientPanel;
import io.github.compilerstuck.control.ui.settings.SettingsUi;
import io.github.compilerstuck.control.ui.settings.SortingPanel;
import io.github.compilerstuck.control.ui.settings.SoundPanel;
import io.github.compilerstuck.control.ui.settings.SpeedPanel;
import io.github.compilerstuck.control.ui.settings.VisualizationPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Settings window coordinator. Panel content lives under
 * {@link io.github.compilerstuck.control.ui.settings}.
 */
public class Settings extends JFrame {

    private final AppContext app;

    private ArraySizePanel arraySizePanel;
    private SortingPanel sortingPanel;
    private SpeedPanel speedPanel;
    private VisualizationPanel visualizationPanel;
    private ActionBar actionBar;

    public Settings(AppContext app) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        this.app = app;
        initialize();
    }

    public void initialize() {
        setTitle("Sorting Algorithm Visualizer - Settings");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);
        setSize(1100, 640);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                app.shutdown();
            }
        });

        JComponent mainPanel = createMainUI();
        setContentPane(mainPanel);
        setVisible(true);
    }

    private JComponent createMainUI() {
        sortingPanel = new SortingPanel(app, this);
        visualizationPanel = new VisualizationPanel(app, this);
        speedPanel = new SpeedPanel(app);
        actionBar = new ActionBar(
                app,
                sortingPanel::isRunAllSelected,
                sortingPanel::getAlgorithmList,
                sortingPanel::getSelectedAlgorithmIndex
        );
        arraySizePanel = new ArraySizePanel(
                app,
                this,
                visualizationPanel::currentConstraints,
                actionBar::setRunEnabled
        );

        GradientPanel gradientPanel = new GradientPanel(app);
        DisplayPanel displayPanel = new DisplayPanel(
                app,
                actionBar::isCancelEnabled,
                actionBar::setCancelEnabled
        );
        SoundPanel soundPanel = new SoundPanel(app);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiTheme.BG_PRIMARY);
        root.setBorder(BorderFactory.createEmptyBorder(
                UiTheme.SPACING_LG, UiTheme.SPACING_LG, 0, UiTheme.SPACING_LG));

        root.add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel columns = new JPanel(new GridLayout(1, 2, UiTheme.SPACING_LG, 0));
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

    private JPanel createLeftColumn(ArraySizePanel arraySize, SortingPanel sorting, SpeedPanel speed) {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(UiTheme.BG_PRIMARY);

        StyledCard arraySizeCard = arraySize.getCard();
        StyledCard sortingCard = sorting.getCard();
        StyledCard speedCard = speed.getCard();
        for (StyledCard c : new StyledCard[]{arraySizeCard, sortingCard, speedCard}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1000));
        }

        col.add(SettingsUi.createSectionLabel("Array Size"));
        col.add(arraySizeCard);
        col.add(Box.createVerticalStrut(UiTheme.SPACING_LG));
        col.add(SettingsUi.createSectionLabel("Sorting"));
        col.add(sortingCard);
        col.add(Box.createVerticalStrut(UiTheme.SPACING_LG));
        col.add(SettingsUi.createSectionLabel("Speed"));
        col.add(speedCard);
        col.add(Box.createVerticalGlue());
        return col;
    }

    private JPanel createRightColumn(
            VisualizationPanel visualization,
            GradientPanel gradient,
            DisplayPanel display,
            SoundPanel sound
    ) {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(UiTheme.BG_PRIMARY);

        StyledCard vizCard = visualization.getCard();
        StyledCard gradientCard = gradient.getCard();
        StyledCard displayCard = display.getCard();
        StyledCard soundCard = sound.getCard();
        for (StyledCard c : new StyledCard[]{vizCard, gradientCard, displayCard, soundCard}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1000));
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
        col.add(Box.createVerticalGlue());
        return col;
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
    }

    public void setEnableCancelButton(boolean enabled) {
        actionBar.setCancelEnabled(enabled);
    }

    public void setProgressBar(int progress) {
        actionBar.setProgress(progress);
    }
}
