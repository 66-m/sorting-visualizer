package io.github.compilerstuck.control.ui.settings;

import io.github.compilerstuck.control.AppContext;
import io.github.compilerstuck.control.ui.ComponentFactory;
import io.github.compilerstuck.control.ui.StyledCard;
import io.github.compilerstuck.control.ui.UiTheme;
import io.github.compilerstuck.visual.Marker;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import io.github.compilerstuck.visual.gradient.MultiGradient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;

/** Gradient picker and custom color swatches. */
public final class GradientPanel {
    private final StyledCard card;

    public GradientPanel(AppContext app) {
        card = ComponentFactory.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        ArrayList<ColorGradient> gradientList = new ArrayList<>(Arrays.asList(
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

        JComboBox<String> gradientListComboBox = ComponentFactory.createComboBox();
        for (ColorGradient gradient : gradientList) {
            gradientListComboBox.addItem(gradient.getName());
        }
        gradientListComboBox.setSelectedIndex(5);
        gradientListComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        gradientListComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiTheme.INPUT_HEIGHT));

        Color color1 = app.getColorGradient().getMarkerColor(0, Marker.NORMAL);
        JPanel colorChoose1 = ComponentFactory.createColorSwatch(color1);

        Color color2 = app.getColorGradient().getMarkerColor(app.getSize() - 1, Marker.NORMAL);
        JPanel colorChoose2 = ComponentFactory.createColorSwatch(color2);

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
    }

    public StyledCard getCard() {
        return card;
    }
}
