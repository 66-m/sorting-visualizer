package io.github.compilerstuck.control.ui.settingsfx;

/** 8pt spacing grid for the Settings one-pager (see 04-ui-ux-design-mockup §4.6). */
public final class SettingsLayout {

  /** Label↔control, tight clusters (slider + precision). */
  public static final double GAP_XS = 4;

  /** Fields within a section, HBox rows. */
  public static final double GAP_SM = 8;

  /** Action-bar controls, related sub-groups inside a section. */
  public static final double GAP_MD = 12;

  /** Header bottom padding, footer top padding. */
  public static final double GAP_LG = 16;

  /** Between sections in a column (~2× field spacing). */
  public static final double GAP_XL = 24;

  /** Left/right column gap; also root content padding. */
  public static final double GAP_COL = 24;

  private SettingsLayout() {}
}
