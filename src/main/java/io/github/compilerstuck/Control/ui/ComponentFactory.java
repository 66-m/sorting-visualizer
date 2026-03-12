package io.github.compilerstuck.Control.ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Factory for consistently styled, rounded UI components.
 * All buttons use custom-painted rounded rectangles for a modern 2025 look.
 */
public class ComponentFactory {

    private static final int BUTTON_ARC   = 8;
    private static final int INPUT_ARC    = 6;
    private static final int INPUT_PAD_H  = 10;
    private static final int INPUT_PAD_V  = 6;

    // ── Labels ────────────────────────────────────────────────────────────────

    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UiTheme.FONT_TITLE);
        label.setForeground(UiTheme.TEXT_PRIMARY);
        label.setOpaque(false);
        return label;
    }

    public static JLabel createSubtitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UiTheme.FONT_SUBTITLE);
        label.setForeground(UiTheme.TEXT_PRIMARY);
        label.setOpaque(false);
        return label;
    }

    public static JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UiTheme.FONT_LABEL);
        label.setForeground(UiTheme.TEXT_PRIMARY);
        label.setOpaque(false);
        return label;
    }

    // ── Buttons ───────────────────────────────────────────────────────────────

    /**
     * Primary action button — dark background, white text, rounded corners.
     * Used for the main "RUN" action — more prominence with larger font.
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = buildRoundedButton(text, UiTheme.BUTTON_PRIMARY, UiTheme.BUTTON_PRIMARY_FG,
                null, BUTTON_ARC, UiTheme.BUTTON_HEIGHT);
        btn.setFont(new Font(UiTheme.FONT_FAMILY, Font.BOLD, 14));
        addHoverSmooth(btn, UiTheme.BUTTON_PRIMARY, UiTheme.BUTTON_PRIMARY_HOVER);
        btn.setPreferredSize(new Dimension(120, UiTheme.BUTTON_HEIGHT));
        btn.setMinimumSize(new Dimension(90, UiTheme.BUTTON_HEIGHT));
        btn.setMaximumSize(new Dimension(220, UiTheme.BUTTON_HEIGHT));
        return btn;
    }

    /**
     * Secondary action button — white background, zinc border, dark text.
     * Used for "Cancel", etc.
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = buildRoundedButton(text, UiTheme.BUTTON_SECONDARY, UiTheme.TEXT_PRIMARY,
                UiTheme.BORDER_COLOR, BUTTON_ARC, UiTheme.BUTTON_HEIGHT);
        btn.setFont(UiTheme.FONT_LABEL);
        addHoverSmooth(btn, UiTheme.BUTTON_SECONDARY, UiTheme.BUTTON_SECONDARY_HOVER);
        btn.setPreferredSize(new Dimension(110, UiTheme.BUTTON_HEIGHT));
        btn.setMinimumSize(new Dimension(80, UiTheme.BUTTON_HEIGHT));
        btn.setMaximumSize(new Dimension(200, UiTheme.BUTTON_HEIGHT));
        return btn;
    }

    /**
     * Compact inline button for auxiliary actions (Apply, Configure, Select Image).
     * Same visual style as secondary but smaller height.
     */
    public static JButton createSmallButton(String text) {
        JButton btn = buildRoundedButton(text, UiTheme.BUTTON_SECONDARY, UiTheme.TEXT_PRIMARY,
                UiTheme.BORDER_COLOR, BUTTON_ARC, UiTheme.BUTTON_HEIGHT_SMALL);
        btn.setFont(UiTheme.FONT_SMALL);
        addHoverSmooth(btn, UiTheme.BUTTON_SECONDARY, UiTheme.BUTTON_SECONDARY_HOVER);
        btn.setPreferredSize(new Dimension(90, UiTheme.BUTTON_HEIGHT_SMALL));
        btn.setMinimumSize(new Dimension(60, UiTheme.BUTTON_HEIGHT_SMALL));
        btn.setMaximumSize(new Dimension(200, UiTheme.BUTTON_HEIGHT_SMALL));
        return btn;
    }

    /** Shared builder for all rounded buttons. */
    private static JButton buildRoundedButton(String text, Color bg, Color fg, Color borderCol,
                                              int arc, int height) {
        final Color[] borderRef = {borderCol};
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? getBackground() : new Color(244, 244, 245));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                if (borderRef[0] == null) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? borderRef[0] : new Color(228, 228, 231));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorderPainted(false); // we handle border in paintBorder override
        btn.setFocusPainted(false);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(0, UiTheme.SPACING_MD, 0, UiTheme.SPACING_MD));
        return btn;
    }

    // ── Form inputs ───────────────────────────────────────────────────────────

    public static JCheckBox createCheckBox(String label) {
        JCheckBox cb = new JCheckBox(label);
        cb.setFont(UiTheme.FONT_LABEL);
        cb.setForeground(UiTheme.TEXT_PRIMARY);
        cb.setOpaque(false);
        cb.setFocusPainted(false);
        return cb;
    }

    public static <T> JComboBox<T> createComboBox() {
        JComboBox<T> box = new JComboBox<>();
        box.setFont(UiTheme.FONT_BODY);
        box.setForeground(UiTheme.TEXT_PRIMARY);
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UiTheme.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(2, 4, 2, 4)
        ));
        box.setPreferredSize(new Dimension(150, UiTheme.INPUT_HEIGHT));
        box.setMaximumRowCount(15);
        return box;
    }

    public static JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(UiTheme.FONT_BODY);
        field.setForeground(UiTheme.TEXT_PRIMARY);
        field.setBackground(UiTheme.BG_INPUT);
        field.setOpaque(true);  // paint our own background (slightly off-white)
        field.setCaretColor(UiTheme.ACCENT_PRIMARY);
        field.setSelectionColor(UiTheme.ACCENT_PRIMARY);
        field.setSelectedTextColor(Color.WHITE);
        field.setPreferredSize(new Dimension(120, UiTheme.INPUT_HEIGHT));
        field.setBorder(buildInputBorder(UiTheme.BORDER_COLOR));

        // Blue focus ring
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(buildInputBorder(UiTheme.BORDER_FOCUS));
                field.setForeground(UiTheme.TEXT_PRIMARY);
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(buildInputBorder(UiTheme.BORDER_COLOR));
            }
        });
        return field;
    }

    /** Rounded input border that also fills the white background. */
    private static Border buildInputBorder(Color borderColor) {
        return new Border() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(x, y, w - 1, h - 1, INPUT_ARC, INPUT_ARC);
                g2.dispose();
            }
            @Override public Insets getBorderInsets(Component c) {
                return new Insets(INPUT_PAD_V, INPUT_PAD_H, INPUT_PAD_V, INPUT_PAD_H);
            }
            @Override public boolean isBorderOpaque() { return false; }
        };
    }

    public static JSlider createSlider(int min, int max, int initial) {
        JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, initial);
        slider.setFont(UiTheme.FONT_SMALL);
        slider.setForeground(UiTheme.TEXT_SECONDARY);
        slider.setOpaque(false);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.putClientProperty("JSlider.isFilled", true);
        slider.putClientProperty("Slider.paintThumbArrowShape", false);
        return slider;
    }

    /**
     * Color swatch panel — compact rounded square, click to pick color.
     */
    public static JPanel createColorSwatch(Color color) {
        JPanel swatch = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(UiTheme.BORDER_COLOR);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.dispose();
            }
        };
        int sz = UiTheme.COLOR_SWATCH_SIZE;
        swatch.setBackground(color);
        swatch.setPreferredSize(new Dimension(sz, sz));
        swatch.setMaximumSize(new Dimension(sz, sz));
        swatch.setMinimumSize(new Dimension(sz, sz));
        swatch.setOpaque(false);
        swatch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        swatch.setToolTipText("Click to customize color");
        return swatch;
    }

    /**
     * Thin accent-colored progress bar — used as a status strip.
     */
    public static JProgressBar createProgressBar() {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setForeground(UiTheme.ACCENT_PRIMARY);
        bar.setBackground(UiTheme.BG_INPUT);
        bar.setBorder(null);
        bar.setPreferredSize(new Dimension(400, 4));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
        bar.setStringPainted(false);
        return bar;
    }

    public static JComponent createSeparator() {
        JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
        sep.setForeground(UiTheme.BORDER_COLOR);
        sep.setPreferredSize(new Dimension(Integer.MAX_VALUE, 12));
        return sep;
    }

    public static StyledCard createCard() {
        return new StyledCard();
    }

    // ── Hover helper ─────────────────────────────────────────────────────────

    private static void addHoverSmooth(JButton btn, Color normal, Color hover) {
        btn.addMouseListener(new MouseAdapter() {
            private Timer timer;
            private boolean targetHover = false;
            private Color currentColor = normal;

            @Override public void mouseEntered(MouseEvent e) {
                if (!btn.isEnabled()) return;
                targetHover = true;
                if (timer != null) timer.stop();
                animateColor();
            }

            @Override public void mouseExited(MouseEvent e) {
                targetHover = false;
                if (timer != null) timer.stop();
                animateColor();
            }

            private void animateColor() {
                timer = new Timer(16, evt -> {
                    Color target = targetHover ? hover : normal;
                    if (!currentColor.equals(target)) {
                        currentColor = interpolate(currentColor, target, 0.3f);
                        btn.setBackground(currentColor);
                    } else {
                        ((Timer)evt.getSource()).stop();
                    }
                });
                timer.start();
            }

            private Color interpolate(Color a, Color b, float t) {
                return new Color(
                    (int)(a.getRed() + (b.getRed() - a.getRed()) * t),
                    (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                    (int)(a.getBlue() + (b.getBlue() - a.getBlue()) * t)
                );
            }
        });
    }
}
