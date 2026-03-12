package io.github.compilerstuck.Control.ui;

import java.awt.*;

/**
 * UI theme constants for the application.
 * 2025 design system: Tailwind CSS Zinc neutrals + Indigo accent.
 * Inspired by Shadcn UI / Vercel / Linear visual language.
 */
public class UiTheme {

    // ── Backgrounds (Zinc scale) ──────────────────────────────────────────────
    public static final Color BG_PRIMARY   = new Color(250, 250, 250); // zinc-50
    public static final Color BG_SECONDARY = new Color(255, 255, 255); // white – cards
    public static final Color BG_INPUT     = new Color(244, 244, 245); // zinc-100 – input bg
    public static final Color BG_HOVER     = new Color(244, 244, 245); // zinc-100

    // Legacy alias kept for any remaining callers
    public static final Color BG_TERTIARY  = BG_HOVER;

    // ── Borders ───────────────────────────────────────────────────────────────
    public static final Color BORDER_COLOR = new Color(228, 228, 231); // zinc-200
    public static final Color BORDER_FOCUS = new Color(99, 102, 241);  // indigo-500

    // ── Text (Zinc scale) ─────────────────────────────────────────────────────
    public static final Color TEXT_PRIMARY   = new Color(24, 24, 27);   // zinc-900
    public static final Color TEXT_SECONDARY = new Color(113, 113, 122); // zinc-500
    public static final Color TEXT_TERTIARY  = new Color(161, 161, 170); // zinc-400
    public static final Color TEXT_MUTED     = TEXT_TERTIARY;

    // ── Accent: Indigo ────────────────────────────────────────────────────────
    public static final Color ACCENT_PRIMARY   = new Color(99,  102, 241); // indigo-500
    public static final Color ACCENT_HOVER     = new Color(79,  70,  229); // indigo-600
    public static final Color ACCENT_LIGHT     = new Color(238, 242, 255); // indigo-50

    // ── Semantic ──────────────────────────────────────────────────────────────
    public static final Color ACCENT_SUCCESS = new Color(34,  197, 94);  // green-500
    public static final Color ACCENT_WARNING = new Color(245, 158, 11);  // amber-500
    public static final Color ACCENT_ERROR   = new Color(239, 68,  68);  // red-500
    public static final Color ACCENT_INFO    = ACCENT_PRIMARY;

    // ── Buttons ───────────────────────────────────────────────────────────────
    // Primary: dark (Vercel / Linear style — near-black pill button)
    public static final Color BUTTON_PRIMARY       = new Color(24,  24,  27);  // zinc-900
    public static final Color BUTTON_PRIMARY_HOVER = new Color(39,  39,  42);  // zinc-800
    public static final Color BUTTON_PRIMARY_FG    = new Color(250, 250, 250); // zinc-50

    // Secondary: outlined white
    public static final Color BUTTON_SECONDARY       = Color.WHITE;
    public static final Color BUTTON_SECONDARY_HOVER = new Color(244, 244, 245); // zinc-100

    // ── Typography (system font stack) ────────────────────────────────────────
    private static final String[] FONT_CANDIDATES = {
        "Segoe UI", "Ubuntu", "Liberation Sans", "Cantarell", Font.SANS_SERIF
    };
    public static final String FONT_FAMILY;
    static {
        String chosen = Font.SANS_SERIF;
        for (String name : FONT_CANDIDATES) {
            Font probe = new Font(name, Font.PLAIN, 12);
            if (!probe.getFamily().equals("Dialog")) {
                chosen = name;
                break;
            }
        }
        FONT_FAMILY = chosen;
    }

    public static final Font FONT_TITLE   = new Font(FONT_FAMILY, Font.BOLD,  16);
    /** Small, uppercase section header */
    public static final Font FONT_SECTION = new Font(FONT_FAMILY, Font.BOLD,  10);
    public static final Font FONT_SUBTITLE = new Font(FONT_FAMILY, Font.BOLD,  13);
    public static final Font FONT_LABEL   = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FONT_BODY    = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_SMALL   = new Font(FONT_FAMILY, Font.PLAIN, 11);

    // ── Spacing (8-pt grid) ───────────────────────────────────────────────────
    public static final int SPACING_XS = 4;
    public static final int SPACING_SM = 8;
    public static final int SPACING_MD = 12;
    public static final int SPACING_LG = 16;
    public static final int SPACING_XL = 24;

    // ── Component sizes ───────────────────────────────────────────────────────
    public static final int BUTTON_HEIGHT       = 36;
    public static final int BUTTON_HEIGHT_SMALL = 28;
    public static final int INPUT_HEIGHT        = 34;
    public static final int COLOR_SWATCH_SIZE   = 36;

    // ── Card styling ──────────────────────────────────────────────────────────
    public static final int BORDER_RADIUS = 8;
    public static final int BORDER_WIDTH  = 1;
}
