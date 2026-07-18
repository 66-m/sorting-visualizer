package io.github.compilerstuck.control.ui.settingsfx;

/**
 * 8pt spacing grid for the Settings one-pager (see 04-ui-ux-design-mockup §4.6).
 */
public final class SettingsLayout {

  /** Label↔subtitle, tight list rows. */
  public static final double GAP_XS = 4;

  /** Controls within a section, HBox rows. */
  public static final double GAP_SM = 8;

  /** Action-bar controls, related sub-groups. */
  public static final double GAP_MD = 12;

  /** Header bottom, logical group breaks (e.g. Sorting clusters). */
  public static final double GAP_LG = 16;

  /** Root padding, section stack in a column. */
  public static final double GAP_XL = 20;

  /** Left/right column gap. */
  public static final double GAP_COL = 24;

  private SettingsLayout() {}
}
