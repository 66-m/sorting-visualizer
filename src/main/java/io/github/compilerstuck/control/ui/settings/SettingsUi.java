package io.github.compilerstuck.control.ui.settings;

import io.github.compilerstuck.control.ui.UiTheme;

import javax.swing.*;
import java.awt.*;

/** Shared helpers for settings section labels. */
public final class SettingsUi {
    private SettingsUi() {
    }

    public static JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text.toUpperCase());
        label.setFont(UiTheme.FONT_SECTION);
        label.setForeground(UiTheme.ACCENT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 2, UiTheme.SPACING_XS, 0));
        return label;
    }
}
