package io.github.compilerstuck.control;

/**
 * Narrow cross-thread Settings UI façade used by the libGDX render thread. Implementations marshal
 * to the JavaFX thread; {@link #NOOP} is safe before Settings is wired.
 */
public interface SettingsBridge {

  void setProgress(int progress);

  void setInputsEnabled(boolean enabled);

  void setCancelEnabled(boolean enabled);

  /** Bring the Settings window to the front (no-op if Settings is not open yet). */
  void focusSettings();

  SettingsBridge NOOP =
      new SettingsBridge() {
        @Override
        public void setProgress(int progress) {}

        @Override
        public void setInputsEnabled(boolean enabled) {}

        @Override
        public void setCancelEnabled(boolean enabled) {}

        @Override
        public void focusSettings() {}
      };
}
