package io.github.compilerstuck.control.ui.settings;

import io.github.compilerstuck.control.AppContext;
import io.github.compilerstuck.control.ui.ComponentFactory;
import io.github.compilerstuck.control.ui.StyledCard;

import javax.swing.*;
import java.awt.*;

/** Sound enable toggle. */
public final class SoundPanel {
    private final StyledCard card;

    public SoundPanel(AppContext app) {
        card = ComponentFactory.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JCheckBox muteCheckBox = ComponentFactory.createCheckBox("Enable sound effects");
        muteCheckBox.setSelected(true);
        muteCheckBox.addChangeListener(e -> app.getSound().setIsMuted(!muteCheckBox.isSelected()));
        muteCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(muteCheckBox);
    }

    public StyledCard getCard() {
        return card;
    }
}
