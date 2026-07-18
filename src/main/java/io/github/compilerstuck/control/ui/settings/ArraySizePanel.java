package io.github.compilerstuck.control.ui.settings;

import io.github.compilerstuck.control.AppContext;
import io.github.compilerstuck.control.catalog.VisualConstraints;
import io.github.compilerstuck.control.ui.ComponentFactory;
import io.github.compilerstuck.control.ui.StyledCard;
import io.github.compilerstuck.control.ui.UiTheme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Array size slider, text field, and apply button. */
public final class ArraySizePanel {
    private final AppContext app;
    private final Component dialogParent;
    private final Supplier<VisualConstraints> constraintsSupplier;
    private final Consumer<Boolean> setRunEnabled;

    private final JSlider arraySizeSlider;
    private final JTextField arraySizeTextField;
    private final JButton arraySizeOkButton;
    private final StyledCard card;
    private final int maxSize = 20000;
    private final Color errorColor = new Color(244, 67, 54);

    public ArraySizePanel(
            AppContext app,
            Component dialogParent,
            Supplier<VisualConstraints> constraintsSupplier,
            Consumer<Boolean> setRunEnabled
    ) {
        this.app = app;
        this.dialogParent = dialogParent;
        this.constraintsSupplier = constraintsSupplier;
        this.setRunEnabled = setRunEnabled;

        card = ComponentFactory.createCard();
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
                setRunEnabled.accept(false);
                arraySizeSlider.setValue(3);
                arraySizeTextField.setText("3");
                arraySizeTextField.setForeground(errorColor);
            } else {
                applyArraySize(arraySizeSlider.getValue());
                arraySizeTextField.setText(String.valueOf(arraySizeSlider.getValue()));
                arraySizeTextField.setForeground(normalColor);
                setRunEnabled.accept(true);
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
    }

    public StyledCard getCard() {
        return card;
    }

    public void setInputsEnabled(boolean enabled) {
        arraySizeSlider.setEnabled(enabled);
        arraySizeOkButton.setEnabled(enabled);
        arraySizeTextField.setEnabled(enabled);
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

    private void applyArraySize(int requestedSize) {
        VisualConstraints constraints = constraintsSupplier.get();
        if (constraints != null && !constraints.requiresImage()) {
            OptionalInt proposed = constraints.proposeSize(requestedSize);
            if (proposed.isPresent()) {
                int choice = JOptionPane.showConfirmDialog(dialogParent,
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
}
