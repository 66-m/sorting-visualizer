package io.github.compilerstuck.control.ui.settings;

import io.github.compilerstuck.control.AppContext;
import io.github.compilerstuck.control.catalog.VisualConstraints;
import io.github.compilerstuck.control.ui.ComponentFactory;
import io.github.compilerstuck.control.ui.StyledCard;
import io.github.compilerstuck.control.ui.UiTheme;
import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Array size slider, text field, and apply button. */
public final class ArraySizePanel {
  private final AppContext app;
  private final Supplier<VisualConstraints> constraintsSupplier;
  private final Consumer<Boolean> setRunEnabled;

  private final JSlider arraySizeSlider;
  private final JTextField arraySizeTextField;
  private final JButton arraySizeOkButton;
  private final StyledCard card;
  private final int minSize = 3;
  private final int maxSize = 20000;
  private final Color errorColor = new Color(244, 67, 54);
  private boolean syncingDisplay;

  public ArraySizePanel(
      AppContext app,
      Supplier<VisualConstraints> constraintsSupplier,
      Consumer<Boolean> setRunEnabled) {
    this.app = app;
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
    arraySizeSlider.setPreferredSize(new Dimension(0, 55));
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
    arraySizeSlider.addChangeListener(
        e -> {
          if (syncingDisplay) {
            return;
          }
          int fitted = fit(arraySizeSlider.getValue());
          applyArraySize(fitted);
          syncDisplayedSize(fitted);
          arraySizeTextField.setForeground(fitted >= minSize ? normalColor : errorColor);
          setRunEnabled.accept(fitted > minSize);
          arraySizeOkButton.setEnabled(false);
        });

    arraySizeTextField.addActionListener(
        e -> {
          validateAndApplySize();
          arraySizeOkButton.setEnabled(false);
        });

    arraySizeTextField
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              public void changedUpdate(DocumentEvent e) {
                updateSizeInputState();
              }

              public void removeUpdate(DocumentEvent e) {
                updateSizeInputState();
              }

              public void insertUpdate(DocumentEvent e) {
                updateSizeInputState();
              }

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

  /** Updates slider and text field without re-applying size (e.g. after a visualization change). */
  public void syncDisplayedSize(int size) {
    syncingDisplay = true;
    try {
      arraySizeSlider.setValue(size);
      arraySizeTextField.setText(String.valueOf(size));
    } finally {
      syncingDisplay = false;
    }
  }

  private void validateAndApplySize() {
    String text = arraySizeTextField.getText();
    if (text.matches("[0-9]+") && text.length() < 6) {
      int value = Integer.parseInt(text);
      int fitted = fit(value);
      syncDisplayedSize(fitted);
      applyArraySize(fitted);
    }
  }

  private void applyArraySize(int size) {
    app.updateArraySize(size);
  }

  private int fit(int requestedSize) {
    VisualConstraints constraints = constraintsSupplier.get();
    if (constraints == null) {
      return Math.max(minSize, Math.min(maxSize, requestedSize));
    }
    return constraints.fitSize(requestedSize, minSize, maxSize);
  }
}
