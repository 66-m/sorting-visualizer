package io.github.compilerstuck.control.ui.settings;

import io.github.compilerstuck.control.AppContext;
import io.github.compilerstuck.control.ui.ComponentFactory;
import io.github.compilerstuck.control.ui.StyledCard;
import io.github.compilerstuck.control.ui.UiTheme;

import javax.swing.*;
import java.awt.*;

/** Speed slider and step-engine toggle. */
public final class SpeedPanel {
    private final JSlider speedSlider;
    private final StyledCard card;

    public SpeedPanel(AppContext app) {
        card = ComponentFactory.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        speedSlider = ComponentFactory.createSlider(1, 5, app.getSpeedLevel());
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

        speedSlider.addChangeListener(e -> app.setSpeedLevel(speedSlider.getValue()));

        JCheckBox stepEngineCheckBox = ComponentFactory.createCheckBox("Step engine (experimental)");
        stepEngineCheckBox.setSelected(app.isUseStepEngine());
        stepEngineCheckBox.setToolTipText("Advance the sort via draw-frame credits instead of Thread.sleep");
        stepEngineCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        stepEngineCheckBox.addActionListener(e -> app.setUseStepEngine(stepEngineCheckBox.isSelected()));

        card.add(speedSlider);
        card.add(Box.createVerticalStrut(UiTheme.SPACING_SM));
        card.add(stepEngineCheckBox);
    }

    public StyledCard getCard() {
        return card;
    }

    public void setInputsEnabled(boolean enabled) {
        speedSlider.setEnabled(enabled);
    }
}
