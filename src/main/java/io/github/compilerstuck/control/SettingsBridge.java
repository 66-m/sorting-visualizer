package io.github.compilerstuck.control;

/**
 * Narrow cross-thread Settings UI façade used by the libGDX render thread. Implementations marshal
 * to the JavaFX thread; {@link #NOOP} is safe before Settings is wired.
 */
public interface SettingsBridge {

  void setProgress(int progress);

  void setInputsEnabled(boolean enabled);

  void setCancelEnabled(boolean enabled);

  SettingsBridge NOOP =
      new SettingsBridge() {
        @Override
        public void setProgress(int progress) {}

        @Override
        public void setInputsEnabled(boolean enabled) {}

        @Override
        public void setCancelEnabled(boolean enabled) {}
      };
}
