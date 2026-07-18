package io.github.compilerstuck.control.ui.settings;

import io.github.compilerstuck.control.AppContext;
import io.github.compilerstuck.control.ui.ComponentFactory;
import io.github.compilerstuck.control.ui.StyledCard;
import io.github.compilerstuck.control.ui.UiTheme;
import io.github.compilerstuck.sortingalgorithms.SortingAlgorithm;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Display options: measurements, comparison table, CSV export. */
public final class DisplayPanel {
  private static final Logger LOGGER = Logger.getLogger(DisplayPanel.class.getName());

  private final StyledCard card;
  private final JButton exportCsvButton;

  public DisplayPanel(
      AppContext app,
      Component dialogParent,
      BooleanSupplier cancelButtonEnabled,
      Consumer<Boolean> setCancelEnabled) {
    card = ComponentFactory.createCard();
    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

    JCheckBox showMeasurementsCheckBox = ComponentFactory.createCheckBox("Show measurements");
    showMeasurementsCheckBox.setSelected(true);
    showMeasurementsCheckBox.addActionListener(
        e -> app.setPrintMeasurements(showMeasurementsCheckBox.isSelected()));
    showMeasurementsCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);

    JCheckBox comparisonTableCheckBox = ComponentFactory.createCheckBox("Show comparison table");
    comparisonTableCheckBox.addActionListener(
        e -> {
          app.setShowComparisonTable(comparisonTableCheckBox.isSelected());
          if (!app.isRunning() && cancelButtonEnabled.getAsBoolean()) {
            setCancelEnabled.accept(false);
          }
          refreshExportEnabled(app);
        });
    comparisonTableCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);

    exportCsvButton = ComponentFactory.createSmallButton("Export CSV…");
    exportCsvButton.setAlignmentX(Component.LEFT_ALIGNMENT);
    exportCsvButton.setEnabled(false);
    exportCsvButton.addActionListener(e -> exportCsv(app, dialogParent));

    card.add(showMeasurementsCheckBox);
    card.add(Box.createVerticalStrut(UiTheme.SPACING_SM));
    card.add(comparisonTableCheckBox);
    card.add(Box.createVerticalStrut(UiTheme.SPACING_SM));
    card.add(exportCsvButton);
  }

  public StyledCard getCard() {
    return card;
  }

  public void refreshExportEnabled(AppContext app) {
    exportCsvButton.setEnabled(app.getSessionManager().hasResults());
  }

  private void exportCsv(AppContext app, Component dialogParent) {
    if (!app.getSessionManager().hasResults()) {
      JOptionPane.showMessageDialog(
          dialogParent,
          "No comparison results to export yet. Run algorithms with the comparison table enabled.",
          "Export CSV",
          JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Export comparison results");
    chooser.setSelectedFile(new File("sorting-comparison.csv"));
    chooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
    if (chooser.showSaveDialog(dialogParent) != JFileChooser.APPROVE_OPTION) {
      return;
    }

    Path path = chooser.getSelectedFile().toPath();
    if (!path.toString().toLowerCase().endsWith(".csv")) {
      path = Path.of(path + ".csv");
    }

    try {
      List<SortingAlgorithm> algorithms = app.getAlgorithms();
      app.getSessionManager().exportCsv(path, algorithms);
      System.out.println("Comparison CSV written to: " + path.toAbsolutePath());
      JOptionPane.showMessageDialog(
          dialogParent,
          "Saved to:\n" + path.toAbsolutePath(),
          "Export CSV",
          JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception ex) {
      LOGGER.log(Level.WARNING, "Failed to export CSV", ex);
      JOptionPane.showMessageDialog(
          dialogParent,
          "Failed to export CSV: " + ex.getMessage(),
          "Export CSV",
          JOptionPane.ERROR_MESSAGE);
    }
  }
}
