package io.github.compilerstuck.control.ui.settingsfx.vm;

import io.github.compilerstuck.control.AppContext;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.file.Path;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Headless display / metrics view-model. CSV export has no Swing dialogs. */
public final class DisplayViewModel {

  private static final Logger LOGGER = Logger.getLogger(DisplayViewModel.class.getName());

  public static final String PROP_PRINT_MEASUREMENTS = "printMeasurements";
  public static final String PROP_SHOW_COMPARISON_TABLE = "showComparisonTable";
  public static final String PROP_CAN_EXPORT = "canExport";
  public static final String PROP_INPUTS_ENABLED = "inputsEnabled";

  private final AppContext app;
  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  private boolean printMeasurements;
  private boolean showComparisonTable;
  private boolean inputsEnabled = true;

  public DisplayViewModel(AppContext app) {
    this.app = app;
    this.printMeasurements = app.getStateManager().shouldPrintMeasurements();
    this.showComparisonTable = app.getStateManager().shouldShowComparisonTable();
  }

  public boolean isPrintMeasurements() {
    return printMeasurements;
  }

  public void setPrintMeasurements(boolean print) {
    if (!inputsEnabled || printMeasurements == print) {
      return;
    }
    boolean old = printMeasurements;
    printMeasurements = print;
    app.setPrintMeasurements(print);
    pcs.firePropertyChange(PROP_PRINT_MEASUREMENTS, old, print);
  }

  public boolean isShowComparisonTable() {
    return showComparisonTable;
  }

  public void setShowComparisonTable(boolean show) {
    if (!inputsEnabled || showComparisonTable == show) {
      return;
    }
    boolean old = showComparisonTable;
    showComparisonTable = show;
    app.setShowComparisonTable(show);
    pcs.firePropertyChange(PROP_SHOW_COMPARISON_TABLE, old, show);
    pcs.firePropertyChange(PROP_CAN_EXPORT, null, canExport());
  }

  public boolean canExport() {
    return app.getSessionManager().hasResults();
  }

  /** Re-evaluates export enablement after a sorting session finishes. */
  public void refreshCanExport() {
    pcs.firePropertyChange(PROP_CAN_EXPORT, null, canExport());
  }

  /**
   * Exports comparison CSV to {@code path}. Returns {@code true} on success, {@code false} if there
   * are no results or export fails.
   */
  public boolean exportCsv(Path path) {
    if (!app.getSessionManager().hasResults()) {
      return false;
    }
    try {
      Path target = path;
      if (target != null && !target.toString().toLowerCase(Locale.ROOT).endsWith(".csv")) {
        target = Path.of(target + ".csv");
      }
      app.getSessionManager().exportCsv(target, app.getAlgorithms());
      return true;
    } catch (Exception ex) {
      LOGGER.log(Level.WARNING, "Failed to export CSV", ex);
      return false;
    }
  }

  public boolean isInputsEnabled() {
    return inputsEnabled;
  }

  public void setInputsEnabled(boolean enabled) {
    if (inputsEnabled == enabled) {
      return;
    }
    boolean old = inputsEnabled;
    inputsEnabled = enabled;
    pcs.firePropertyChange(PROP_INPUTS_ENABLED, old, enabled);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
  }
}
