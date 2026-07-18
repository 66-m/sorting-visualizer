package io.github.compilerstuck.control.ui.settings;

import io.github.compilerstuck.control.AppContext;
import io.github.compilerstuck.control.ui.ComponentFactory;
import io.github.compilerstuck.control.ui.StyledCard;
import io.github.compilerstuck.control.ui.UiTheme;

import javax.swing.*;
import java.awt.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** Display options: measurements and comparison table. */
public final class DisplayPanel {
    private final StyledCard card;

    public DisplayPanel(
            AppContext app,
            BooleanSupplier cancelButtonEnabled,
            Consumer<Boolean> setCancelEnabled
    ) {
        card = ComponentFactory.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JCheckBox showMeasurementsCheckBox = ComponentFactory.createCheckBox("Show measurements");
        showMeasurementsCheckBox.setSelected(true);
        showMeasurementsCheckBox.addActionListener(e ->
                app.setPrintMeasurements(showMeasurementsCheckBox.isSelected()));
        showMeasurementsCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JCheckBox comparisonTableCheckBox = ComponentFactory.createCheckBox("Show comparison table");
        comparisonTableCheckBox.addActionListener(e -> {
            app.setShowComparisonTable(comparisonTableCheckBox.isSelected());
            if (!app.isRunning() && cancelButtonEnabled.getAsBoolean()) {
                setCancelEnabled.accept(false);
            }
        });
        comparisonTableCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(showMeasurementsCheckBox);
        card.add(Box.createVerticalStrut(UiTheme.SPACING_SM));
        card.add(comparisonTableCheckBox);
    }

    public StyledCard getCard() {
        return card;
    }
}
