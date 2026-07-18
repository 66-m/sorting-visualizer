package io.github.compilerstuck.Control.ui;

import io.github.compilerstuck.Control.AppContext;
import io.github.compilerstuck.Control.catalog.AlgorithmCatalog;
import io.github.compilerstuck.Control.catalog.AlgorithmDescriptor;
import io.github.compilerstuck.Control.catalog.VisualConstraints;
import io.github.compilerstuck.Control.catalog.VisualizationCatalog;
import io.github.compilerstuck.Control.catalog.VisualizationDescriptor;
import io.github.compilerstuck.Control.config.ShuffleType;
import io.github.compilerstuck.SortingAlgorithms.SortingAlgorithm;
import io.github.compilerstuck.Visual.ImageHorizontal;
import io.github.compilerstuck.Visual.ImageVertical;
import io.github.compilerstuck.Visual.Marker;
import io.github.compilerstuck.Visual.Visualization;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;
import io.github.compilerstuck.Visual.Gradient.MultiGradient;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;

/**
 * Modernized Settings UI for the Sorting Algorithm Visualizer.
 * Features:
 * - Tabbed interface for better organization
 * - Modern color scheme and typography
 * - Improved spacing and visual hierarchy
 * - Enhanced component styling for better UX
 * - All original functionality preserved
 */
public class Settings extends JFrame {

    private final AppContext app;

    // UI Components
    private JSlider speedSlider;
    private JSlider arraySizeSlider;
    private JTextField arraySizeTextField;
    private JButton arraySizeOkButton;
    
    private JComboBox<String> algorithmListComboBox;
    private JCheckBox runAllCheckBox;
    private JButton buttonRunAllSettings;
    private JComboBox<String> shuffleListBox;
    
    private JComboBox<String> visualizationListComboBox;
    private JComboBox<String> gradientListComboBox;
    private JPanel colorChoose1;
    private JPanel colorChoose2;
    private JCheckBox showMeasurementsCheckBox;
    private JCheckBox comparisonTableCheckBox;
    private JButton buttonSetImg;
    
    private JCheckBox muteCheckBox;
    
    private JButton runButton;
    private JButton cancelButton;
    private JProgressBar progressBarArray;
    
    // Data
    private List<VisualizationDescriptor> visualizationDescriptors;
    private ArrayList<SortingAlgorithm> algorithmList;
    private ArrayList<ColorGradient> gradientList;
    private ArrayList<ShuffleType> shuffleTypes;
    private ArrayList<Visualization> visualizationList;
    
    // State
    private final Color errorColor = new Color(244, 67, 54);
    private int maxSize = 20000;
    private int previousVisualizationIndex = 0;

    public Settings(AppContext app) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        this.app = app;
        initialize();
    }

    public void initialize() {
        // Configure window
        setTitle("Sorting Algorithm Visualizer - Settings");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);
        setSize(1100, 640);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);
        
        // Add window listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                app.shutdown();
            }
        });

        // Create main UI
        JComponent mainPanel = createMainUI();
        setContentPane(mainPanel);
        setVisible(true);
    }

    /**
     * Create the main one-page two-column interface
     */
    private JComponent createMainUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiTheme.BG_PRIMARY);
        root.setBorder(BorderFactory.createEmptyBorder(
                UiTheme.SPACING_LG, UiTheme.SPACING_LG, 0, UiTheme.SPACING_LG));

        root.add(createHeaderPanel(), BorderLayout.NORTH);

        // Two equal columns separated by a gap
        JPanel columns = new JPanel(new GridLayout(1, 2, UiTheme.SPACING_LG, 0));
        columns.setBackground(UiTheme.BG_PRIMARY);
        columns.setBorder(BorderFactory.createEmptyBorder(UiTheme.SPACING_MD, 0, 0, 0));
        columns.add(createLeftColumn());
        columns.add(createRightColumn());

        JScrollPane scroll = new JScrollPane(columns);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UiTheme.BG_PRIMARY);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        root.add(scroll, BorderLayout.CENTER);
        root.add(createActionPanel(), BorderLayout.SOUTH);
        return root;
    }

    /** Small uppercase section label placed above each card. */
    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text.toUpperCase());
        label.setFont(UiTheme.FONT_SECTION);
        label.setForeground(UiTheme.ACCENT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 2, UiTheme.SPACING_XS, 0));
        return label;
    }

    /** Left column: Array Size → Sorting → Speed */
    private JPanel createLeftColumn() {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(UiTheme.BG_PRIMARY);

        StyledCard arraySizeCard = createArraySizeCard();
        StyledCard sortingCard   = createSortingCard();
        StyledCard speedCard     = createSpeedCard();
        for (StyledCard c : new StyledCard[]{arraySizeCard, sortingCard, speedCard}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1000));
        }

        col.add(createSectionLabel("Array Size"));
        col.add(arraySizeCard);
        col.add(Box.createVerticalStrut(UiTheme.SPACING_LG));
        col.add(createSectionLabel("Sorting"));
        col.add(sortingCard);
        col.add(Box.createVerticalStrut(UiTheme.SPACING_LG));
        col.add(createSectionLabel("Speed"));
        col.add(speedCard);
        col.add(Box.createVerticalGlue());
        return col;
    }

    /** Right column: Visualization → Gradient → Display → Sound */
    private JPanel createRightColumn() {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(UiTheme.BG_PRIMARY);

        StyledCard vizCard     = createVisualizationCard();
        StyledCard gradientCard = createGradientCard();
        StyledCard displayCard = createDisplayCard();
        StyledCard soundCard   = createSoundCard();
        for (StyledCard c : new StyledCard[]{vizCard, gradientCard, displayCard, soundCard}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1000));
        }

        col.add(createSectionLabel("Visualization"));
        col.add(vizCard);
        col.add(Box.createVerticalStrut(UiTheme.SPACING_LG));
        col.add(createSectionLabel("Gradient"));
        col.add(gradientCard);
        col.add(Box.createVerticalStrut(UiTheme.SPACING_LG));
        col.add(createSectionLabel("Display"));
        col.add(displayCard);
        col.add(Box.createVerticalStrut(UiTheme.SPACING_LG));
        col.add(createSectionLabel("Sound"));
        col.add(soundCard);
        col.add(Box.createVerticalGlue());
        return col;
    }

    /** Slim title bar: "Sorting Visualizer" on the left, "Settings" on the right. */
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

    /** Array size: slider on top, then [text-field] [Apply] in one row. */
    private StyledCard createArraySizeCard() {
        StyledCard card = ComponentFactory.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        int arraySize = app.getSize();
        arraySizeSlider = ComponentFactory.createSlider(0, maxSize, arraySize);
        arraySizeSlider.setPaintTicks(true);
        arraySizeSlider.setPaintLabels(true);
        arraySizeSlider.setMinorTickSpacing(maxSize / 8);
        arraySizeSlider.setMajorTickSpacing(maxSize / 4);
        arraySizeSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        arraySizeSlider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

        arraySizeTextField = ComponentFactory.createTextField();
        arraySizeTextField.setText(String.valueOf(arraySize));
        arraySizeTextField.setMaximumSize(new Dimension(90, UiTheme.INPUT_HEIGHT));
        arraySizeTextField.setPreferredSize(new Dimension(90, UiTheme.INPUT_HEIGHT));

        arraySizeOkButton = ComponentFactory.createSmallButton("Apply");
        arraySizeOkButton.setEnabled(false);

        JPanel inputRow = new JPanel();
        inputRow.setLayout(new BoxLayout(inputRow, BoxLayout.X_AXIS));
        inputRow.setOpaque(false);
        inputRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiTheme.INPUT_HEIGHT));
        inputRow.add(arraySizeTextField);
        inputRow.add(Box.createHorizontalStrut(UiTheme.SPACING_SM));
        inputRow.add(arraySizeOkButton);
        inputRow.add(Box.createHorizontalGlue());

        Color normalColor = UiTheme.TEXT_PRIMARY;
        arraySizeSlider.addChangeListener(e -> {
            if (arraySizeSlider.getValue() <= 3) {
                runButton.setEnabled(false);
                arraySizeSlider.setValue(3);
                arraySizeTextField.setText("3");
                arraySizeTextField.setForeground(errorColor);
            } else {
                applyArraySize(arraySizeSlider.getValue());
                arraySizeTextField.setText(String.valueOf(arraySizeSlider.getValue()));
                arraySizeTextField.setForeground(normalColor);
                runButton.setEnabled(true);
            }
            arraySizeOkButton.setEnabled(false);
        });

        arraySizeTextField.addActionListener(e -> {
            validateAndApplySize();
            arraySizeOkButton.setEnabled(false);
        });

        arraySizeTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { updateSizeInputState(); }
            public void removeUpdate(DocumentEvent e)  { updateSizeInputState(); }
            public void insertUpdate(DocumentEvent e)  { updateSizeInputState(); }
            private void updateSizeInputState() {
                String text = arraySizeTextField.getText();
                if (text.equals(text.replaceAll("[^0-9]", ""))) {
                    arraySizeOkButton.setEnabled(true);
                    arraySizeTextField.setForeground(normalColor);
                } else {
                    arraySizeOkButton.setEnabled(false);
                    arraySizeTextField.setForeground(errorColor);
                }
            }
        });

        arraySizeOkButton.addActionListener(e -> validateAndApplySize());

        card.add(arraySizeSlider);
        card.add(Box.createVerticalStrut(UiTheme.SPACING_SM));
        card.add(inputRow);
        return card;
    }

    private void validateAndApplySize() {
        String text = arraySizeTextField.getText();
        if (text.matches("[0-9]+") && text.length() < 6) {
            int value = Integer.parseInt(text);
            if (value > maxSize) {
                arraySizeSlider.setValue(maxSize);
            } else if (value < 3) {
                arraySizeSlider.setValue(3);
            } else {
                arraySizeSlider.setValue(value);
            }
        }
    }

    /**
     * Applies the requested array size, confirming with the user when the
     * currently selected visualization requires a different size (e.g. a
     * perfect square/cube for grid-based visuals).
     */
    private void applyArraySize(int requestedSize) {
        VisualConstraints constraints = currentVisualizationConstraints();
        if (constraints != null && !constraints.requiresImage()) {
            OptionalInt proposed = constraints.proposeSize(requestedSize);
            if (proposed.isPresent()) {
                int choice = JOptionPane.showConfirmDialog(this,
                        "The selected visualization works best with " + proposed.getAsInt()
                                + " elements instead of " + requestedSize + ". Use " + proposed.getAsInt() + "?",
                        "Adjust array size", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    app.updateArraySize(proposed.getAsInt());
                    return;
                }
            }
        }
        app.updateArraySize(requestedSize);
    }

    private VisualConstraints currentVisualizationConstraints() {
        if (visualizationDescriptors == null || visualizationListComboBox == null) {
            return null;
        }
        int index = visualizationListComboBox.getSelectedIndex();
        if (index < 0 || index >= visualizationDescriptors.size()) {
            return null;
        }
        return visualizationDescriptors.get(index).constraints();
    }

    /** Sorting: algorithm combo + run-all row + shuffle combo, all in one card. */
    private StyledCard createSortingCard() {
        StyledCard card = ComponentFactory.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        algorithmList = new ArrayList<>();
        for (AlgorithmDescriptor descriptor : AlgorithmCatalog.all()) {
            SortingAlgorithm alg = descriptor.factory().apply(app.getArrayController(), app.getRenderContext());
            alg.setOperationReporter(app.getStateManager()::setCurrentOperation);
            algorithmList.add(alg);
        }

        algorithmListComboBox = ComponentFactory.createComboBox();
        for (SortingAlgorithm algorithm : algorithmList) {
            algorithmListComboBox.addItem(algorithm.getName());
        }
        algorithmListComboBox.setSelectedIndex(0);
        algorithmListComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        algorithmListComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiTheme.INPUT_HEIGHT));

        final int[] selectedAlgorithmIndex = {0};
        algorithmListComboBox.addActionListener(e -> {
            app.setAlgorithm(algorithmList.get(algorithmListComboBox.getSelectedIndex()));
            selectedAlgorithmIndex[0] = algorithmListComboBox.getSelectedIndex();
        });

        // Run-all row
        JPanel runAllRow = new JPanel();
        runAllRow.setLayout(new BoxLayout(runAllRow, BoxLayout.X_AXIS));
        runAllRow.setOpaque(false);
        runAllRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        runAllRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiTheme.BUTTON_HEIGHT));

        runAllCheckBox = ComponentFactory.createCheckBox("Run all");
        runAllCheckBox.setSelected(false);
        runAllCheckBox.addActionListener(e -> {
            algorithmListComboBox.setEnabled(!runAllCheckBox.isSelected());
            buttonRunAllSettings.setEnabled(runAllCheckBox.isSelected());
        });

        buttonRunAllSettings = ComponentFactory.createSmallButton("Configure");
        buttonRunAllSettings.setEnabled(false);
        buttonRunAllSettings.addActionListener(e -> showRunAllDialog(selectedAlgorithmIndex[0]));

        runAllRow.add(runAllCheckBox);
        runAllRow.add(Box.createHorizontalGlue());
        runAllRow.add(buttonRunAllSettings);

        // Shuffle combo
        shuffleTypes = new ArrayList<>(Arrays.asList(
                ShuffleType.RANDOM, ShuffleType.REVERSE,
                ShuffleType.ALMOST_SORTED, ShuffleType.SORTED
        ));

        shuffleListBox = ComponentFactory.createComboBox();
        for (ShuffleType st : shuffleTypes) {
            shuffleListBox.addItem(st.toString());
        }
        shuffleListBox.setSelectedIndex(0);
        shuffleListBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        shuffleListBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiTheme.INPUT_HEIGHT));
        shuffleListBox.addActionListener(e ->
                app.getArrayController().setShuffleType(shuffleTypes.get(shuffleListBox.getSelectedIndex()))
        );

        card.add(algorithmListComboBox);
        card.add(Box.createVerticalStrut(UiTheme.SPACING_SM));
        card.add(runAllRow);
        card.add(Box.createVerticalStrut(UiTheme.SPACING_SM));
        card.add(shuffleListBox);
        return card;
    }

    /** Speed slider with emoji labels. */
    private StyledCard createSpeedCard() {
        StyledCard card = ComponentFactory.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        final int[] DELAY_TIME = {50, 10, 1, 1, 1};
        final double[] DELAY_FACTOR = {1.0, 1.0, 1.0, 0.12, 0.02};

        speedSlider = ComponentFactory.createSlider(1, 5, 3);
        speedSlider.setSnapToTicks(true);
        speedSlider.setMajorTickSpacing(1);
        speedSlider.setToolTipText("Select animation speed level");
        speedSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        speedSlider.setPreferredSize(new Dimension(400, 55));
        speedSlider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

        java.util.Hashtable<Integer, JLabel> speedLabels = new java.util.Hashtable<>();
        speedLabels.put(1, new JLabel("Very Slow"));
        speedLabels.put(2, new JLabel("Slow"));
        speedLabels.put(3, new JLabel("Normal"));
        speedLabels.put(4, new JLabel("Fast"));
        speedLabels.put(5, new JLabel("Max"));
        speedSlider.setLabelTable(speedLabels);

        speedSlider.addChangeListener(e -> {
            int level = speedSlider.getValue() - 1;
            app.setDelayTime(DELAY_TIME[level]);
            app.setDelayFactor(DELAY_FACTOR[level]);
        });

        card.add(speedSlider);
        return card;
    }

    /** Visualization type picker + conditional image-file button. */
    private StyledCard createVisualizationCard() {
        StyledCard card = ComponentFactory.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        visualizationDescriptors = VisualizationCatalog.all();
        visualizationList = new ArrayList<>();
        for (VisualizationDescriptor descriptor : visualizationDescriptors) {
            visualizationList.add(descriptor.factory().create(
                    app.getArrayController(), app.getColorGradient(), app.getSound(), app.getRenderContext()));
        }

        visualizationListComboBox = ComponentFactory.createComboBox();
        for (Visualization visualization : visualizationList) {
            visualizationListComboBox.addItem(visualization.getName());
        }
        visualizationListComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        visualizationListComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiTheme.INPUT_HEIGHT));
        previousVisualizationIndex = visualizationListComboBox.getSelectedIndex();
        visualizationListComboBox.addActionListener(e -> {
            int index = visualizationListComboBox.getSelectedIndex();
            VisualConstraints constraints = visualizationDescriptors.get(index).constraints();

            if (!constraints.requiresImage()) {
                OptionalInt proposed = constraints.proposeSize(app.getSize());
                if (proposed.isPresent()) {
                    int choice = JOptionPane.showConfirmDialog(this,
                            "This visualization works best with " + proposed.getAsInt()
                                    + " elements instead of " + app.getSize() + ". Use " + proposed.getAsInt() + "?",
                            "Adjust array size", JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {
                        app.updateArraySize(proposed.getAsInt());
                    } else {
                        visualizationListComboBox.setSelectedIndex(previousVisualizationIndex);
                        return;
                    }
                }
            }

            previousVisualizationIndex = index;
            Visualization visualization = visualizationList.get(index);
            app.setVisualization(visualization);
            boolean isImage = constraints.requiresImage();
            buttonSetImg.setVisible(isImage);
            buttonSetImg.setEnabled(isImage);
        });

        buttonSetImg = ComponentFactory.createSmallButton("Select Image");
        buttonSetImg.setVisible(false);
        buttonSetImg.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonSetImg.addActionListener(e -> selectImageFile());

        card.add(visualizationListComboBox);
        card.add(Box.createVerticalStrut(UiTheme.SPACING_SM));
        card.add(buttonSetImg);
        return card;
    }

    /** Gradient picker + two clickable color swatches for custom colors. */
    private StyledCard createGradientCard() {
        StyledCard card = ComponentFactory.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        gradientList = new ArrayList<>(Arrays.asList(
                new ColorGradient(new Color(200, 0, 0), new Color(200, 0, 0), Color.WHITE, "Red"),
                new ColorGradient(new Color(0, 200, 0), new Color(0, 200, 0), Color.WHITE, "Green"),
                new ColorGradient(new Color(0, 0, 200), new Color(0, 0, 200), Color.WHITE, "Blue"),
                new ColorGradient(Color.WHITE, Color.WHITE, Color.RED, "White"),
                new ColorGradient(Color.WHITE, Color.BLACK, Color.WHITE, "White -> Black"),
                new ColorGradient(Color.RED, Color.BLACK, Color.WHITE, "Red -> Black"),
                new ColorGradient(Color.BLUE, Color.RED, Color.WHITE, "Blue -> Red"),
                new ColorGradient(Color.BLACK, Color.WHITE, Color.WHITE, "Black -> White"),
                new ColorGradient(Color.BLACK, Color.RED, Color.WHITE, "Black -> Red"),
                new MultiGradient(Color.WHITE, "Rainbow"),
                new ColorGradient(Color.PINK, Color.BLACK, Color.WHITE, "Custom Gradient")
        ));

        gradientListComboBox = ComponentFactory.createComboBox();
        for (ColorGradient gradient : gradientList) {
            gradientListComboBox.addItem(gradient.getName());
        }
        gradientListComboBox.setSelectedIndex(5);
        gradientListComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        gradientListComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiTheme.INPUT_HEIGHT));

        Color color1 = app.getColorGradient().getMarkerColor(0, Marker.NORMAL);
        colorChoose1 = ComponentFactory.createColorSwatch(color1);

        Color color2 = app.getColorGradient().getMarkerColor(app.getSize() - 1, Marker.NORMAL);
        colorChoose2 = ComponentFactory.createColorSwatch(color2);

        JPanel swatchRow = new JPanel();
        swatchRow.setLayout(new BoxLayout(swatchRow, BoxLayout.X_AXIS));
        swatchRow.setOpaque(false);
        swatchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        swatchRow.add(colorChoose1);
        swatchRow.add(Box.createHorizontalStrut(UiTheme.SPACING_SM));
        swatchRow.add(colorChoose2);
        swatchRow.add(Box.createHorizontalGlue());

        gradientListComboBox.addActionListener(e -> {
            ColorGradient selected = gradientList.get(gradientListComboBox.getSelectedIndex());
            selected.updateGradient(app.getSize());
            app.setColorGradient(selected);
            colorChoose1.setBackground(app.getColorGradient().getMarkerColor(0, Marker.NORMAL));
            colorChoose2.setBackground(app.getColorGradient().getMarkerColor(app.getSize() - 1, Marker.NORMAL));
        });

        MouseListener ml = new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                JPanel jPanel = (JPanel) e.getSource();
                Color initColor = jPanel.getBackground();
                Color selectedColor = JColorChooser.showDialog(null, "Select Color", jPanel.getBackground());
                if (selectedColor != null && !initColor.equals(selectedColor)) {
                    jPanel.setBackground(selectedColor);
                    if (jPanel.getName().equals("colorChoose1")) {
                        gradientList.get(gradientList.size() - 1).setColor1(selectedColor);
                        gradientList.get(gradientList.size() - 1).setColor2(colorChoose2.getBackground());
                    } else {
                        gradientList.get(gradientList.size() - 1).setColor2(selectedColor);
                        gradientList.get(gradientList.size() - 1).setColor1(colorChoose1.getBackground());
                    }
                    gradientListComboBox.setSelectedIndex(gradientList.size() - 1);
                }
            }
        };
        colorChoose1.addMouseListener(ml);
        colorChoose1.setName("colorChoose1");
        colorChoose2.addMouseListener(ml);
        colorChoose2.setName("colorChoose2");

        JLabel swatchHint = new JLabel("Click swatch to customize");
        swatchHint.setFont(UiTheme.FONT_SMALL);
        swatchHint.setForeground(UiTheme.TEXT_SECONDARY);
        swatchHint.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(gradientListComboBox);
        card.add(Box.createVerticalStrut(UiTheme.SPACING_SM));
        card.add(swatchRow);
        card.add(Box.createVerticalStrut(UiTheme.SPACING_XS));
        card.add(swatchHint);
        return card;
    }

    /** Display options: show measurements + comparison table toggles. */
    private StyledCard createDisplayCard() {
        StyledCard card = ComponentFactory.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        showMeasurementsCheckBox = ComponentFactory.createCheckBox("Show measurements");
        showMeasurementsCheckBox.setSelected(true);
        showMeasurementsCheckBox.addActionListener(e ->
                app.setPrintMeasurements(showMeasurementsCheckBox.isSelected()));
        showMeasurementsCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        comparisonTableCheckBox = ComponentFactory.createCheckBox("Show comparison table");
        comparisonTableCheckBox.addActionListener(e -> {
            app.setShowComparisonTable(comparisonTableCheckBox.isSelected());
            if (!app.isRunning() && cancelButton.isEnabled()) cancelButton.setEnabled(false);
        });
        comparisonTableCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(showMeasurementsCheckBox);
        card.add(Box.createVerticalStrut(UiTheme.SPACING_SM));
        card.add(comparisonTableCheckBox);
        return card;
    }

    /** Sound enable toggle. */
    private StyledCard createSoundCard() {
        StyledCard card = ComponentFactory.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        muteCheckBox = ComponentFactory.createCheckBox("Enable sound effects");
        muteCheckBox.setSelected(true);
        muteCheckBox.addChangeListener(e -> app.getSound().setIsMuted(!muteCheckBox.isSelected()));
        muteCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(muteCheckBox);
        return card;
    }

    /** Bottom bar: [Cancel] [RUN] buttons + thin progress strip. */
    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UiTheme.BG_PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(
                UiTheme.SPACING_MD, 0, UiTheme.SPACING_LG, 0));

        runButton = ComponentFactory.createPrimaryButton("RUN");
        runButton.addActionListener(e -> {
            if (runAllCheckBox.isSelected()) {
                app.setAlgorithms(algorithmList);
            } else {
                app.setAlgorithm(algorithmList.get(algorithmListComboBox.getSelectedIndex()));
            }
            app.setStart(true);
            cancelButton.setEnabled(true);
        });

        cancelButton = ComponentFactory.createSecondaryButton("Cancel");
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(e -> {
            app.cancelSorting();
            cancelButton.setEnabled(false);
        });

        JPanel buttonsRow = new JPanel();
        buttonsRow.setLayout(new BoxLayout(buttonsRow, BoxLayout.X_AXIS));
        buttonsRow.setOpaque(false);
        buttonsRow.add(Box.createHorizontalGlue());
        buttonsRow.add(cancelButton);
        buttonsRow.add(Box.createHorizontalStrut(UiTheme.SPACING_SM));
        buttonsRow.add(runButton);

        progressBarArray = ComponentFactory.createProgressBar();
        progressBarArray.setValue(100);

        panel.add(buttonsRow, BorderLayout.CENTER);
        panel.add(progressBarArray, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Show run all algorithms configuration dialog
     */
    private void showRunAllDialog(int selectedIndex) {
        DefaultListModel<JCheckBox> runAllSettings = new DefaultListModel<>();
        JCheckBoxList checkBoxList = new JCheckBoxList(runAllSettings);

        for (SortingAlgorithm alg : algorithmList) {
            JCheckBox algCheckBox = new JCheckBox(alg.getName());
            algCheckBox.setSelected(alg.isSelected());
            algCheckBox.addChangeListener(e -> alg.setSelected(algCheckBox.isSelected()));
            runAllSettings.addElement(algCheckBox);
        }

        final int[] dragFromIndex = {-1};
        final boolean[] wasSelected = {false};

        checkBoxList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragFromIndex[0] = checkBoxList.locationToIndex(e.getPoint());
                wasSelected[0] = !runAllSettings.getElementAt(dragFromIndex[0]).isSelected();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragFromIndex[0] = -1;
                wasSelected[0] = false;
            }
        });

        checkBoxList.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int currentDragIndex = checkBoxList.locationToIndex(e.getPoint());

                if (dragFromIndex[0] != -1 && currentDragIndex != -1 && dragFromIndex[0] != currentDragIndex) {
                    JCheckBox draggedItem = runAllSettings.getElementAt(dragFromIndex[0]);
                    runAllSettings.remove(dragFromIndex[0]);
                    runAllSettings.add(currentDragIndex, draggedItem);

                    SortingAlgorithm temp = algorithmList.get(dragFromIndex[0]);
                    algorithmList.remove(dragFromIndex[0]);
                    algorithmList.add(currentDragIndex, temp);

                    dragFromIndex[0] = currentDragIndex;
                    if (wasSelected[0] != draggedItem.isSelected()) {
                        draggedItem.setSelected(wasSelected[0]);
                    }
                }
            }
        });

        JDialog runAllSettingDialog = new JDialog();
        runAllSettingDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                for (SortingAlgorithm alg : algorithmList) {
                    algorithmListComboBox.addItem(alg.getName());
                    algorithmListComboBox.removeItemAt(0);
                }
                algorithmListComboBox.setSelectedIndex(selectedIndex);
            }
        });

        runAllSettingDialog.setSize(350, 500);
        runAllSettingDialog.setLocationRelativeTo(this);
        runAllSettingDialog.setTitle("Configure Algorithm Execution Order");
        runAllSettingDialog.add(new JScrollPane(checkBoxList));
        runAllSettingDialog.setResizable(false);
        runAllSettingDialog.setModal(true);
        runAllSettingDialog.setVisible(true);
    }

    /**
     * Open file chooser for image selection
     */
    private void selectImageFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an image for visualization");
        fileChooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG and JPG images", "png", "jpg");
        fileChooser.addChoosableFileFilter(filter);
        int retval = fileChooser.showDialog(null, "Select image");
        if (retval == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String imagePath = selectedFile.getAbsolutePath();

            int index = visualizationListComboBox.getSelectedIndex();
            Visualization selectedVisualization = visualizationList.get(index);
            if (selectedVisualization instanceof ImageVertical imageVertical) {
                imageVertical.setImg(imagePath);
                app.setVisualization(imageVertical);
            } else if (selectedVisualization instanceof ImageHorizontal imageHorizontal) {
                imageHorizontal.setImg(imagePath);
                app.setVisualization(imageHorizontal);
            }
        }
    }

    // Public interface methods
    
    public void setEnableInputs(boolean enabled) {
        arraySizeSlider.setEnabled(enabled);
        algorithmListComboBox.setEnabled(enabled);
        runAllCheckBox.setEnabled(enabled);
        shuffleListBox.setEnabled(enabled);
        runButton.setEnabled(enabled);
        arraySizeOkButton.setEnabled(enabled);
        arraySizeTextField.setEnabled(enabled);
        visualizationListComboBox.setEnabled(enabled);
        buttonRunAllSettings.setEnabled(enabled);
        speedSlider.setEnabled(enabled);
    }

    public void setEnableCancelButton(boolean enabled) {
        cancelButton.setEnabled(enabled);
    }

    public void setProgressBar(int progress) {
        progressBarArray.setValue(progress);
    }
}
