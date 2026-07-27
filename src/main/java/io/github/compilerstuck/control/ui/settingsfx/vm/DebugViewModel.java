package io.github.compilerstuck.control.ui.settingsfx.vm;

import io.github.compilerstuck.control.AppContext;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/** Headless debug toggles (render perf stats). Not gated by inputsEnabled. */
public final class DebugViewModel {

  public static final String PROP_PERF_STATS = "perfStats";

  private final AppContext app;
  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  private boolean perfStats;

  public DebugViewModel(AppContext app) {
    this.app = app;
    this.perfStats = app.isPerfStatsEnabled();
  }

  public boolean isPerfStats() {
    return perfStats;
  }

  public void setPerfStats(boolean enabled) {
    if (perfStats == enabled) {
      return;
    }
    boolean old = perfStats;
    perfStats = enabled;
    app.setPerfStatsEnabled(enabled);
    pcs.firePropertyChange(PROP_PERF_STATS, old, enabled);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
  }
}
