package io.github.compilerstuck.control.config;

/**
 * Distinct product identities — do not collapse these into one string.
 *
 * <ul>
 *   <li>{@link #WATERMARK} — in-app visualizer branding
 *   <li>{@link #COPYRIGHT_HOLDER} — legal copyright
 *   <li>{@link #GITHUB_ORG} — GitHub org / Maven groupId coordinate
 * </ul>
 */
public final class Brand {
  private Brand() {}

  /** In-app canvas watermark. */
  public static final String WATERMARK = "CompilerStuck";

  /** Copyright holder for LICENSE / README. */
  public static final String COPYRIGHT_HOLDER = "Marcel Mauel";

  /** GitHub organization and Maven {@code groupId} fragment. */
  public static final String GITHUB_ORG = "66-m";
}
