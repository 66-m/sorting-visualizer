package io.github.compilerstuck.control.ui.settings;

import io.github.compilerstuck.control.AppContext;
import io.github.compilerstuck.control.ui.ComponentFactory;
import io.github.compilerstuck.control.ui.UiTheme;
import io.github.compilerstuck.sortingalgorithms.SortingAlgorithm;
import java.awt.*;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.swing.*;

/** Bottom bar: Cancel / RUN buttons and progress strip. */
public final class ActionBar {
  private final JButton runButton;
  private final JButton cancelButton;
  private final JProgressBar progressBarArray;
  private final JPanel panel;

  public ActionBar(
      AppContext app,
      BooleanSupplier runAllSelected,
      Supplier<List<SortingAlgorithm>> algorithmList,
      IntSupplier selectedAlgorithmIndex) {
    panel = new JPanel(new BorderLayout());
    panel.setBackground(UiTheme.BG_PRIMARY);
    panel.setBorder(BorderFactory.createEmptyBorder(UiTheme.SPACING_MD, 0, UiTheme.SPACING_LG, 0));

    cancelButton = ComponentFactory.createSecondaryButton("Cancel");
    cancelButton.setEnabled(false);
    cancelButton.addActionListener(
        e -> {
          app.cancelSorting();
          cancelButton.setEnabled(false);
        });

    runButton = ComponentFactory.createPrimaryButton("RUN");
    runButton.addActionListener(
        e -> {
          if (runAllSelected.getAsBoolean()) {
            app.setAlgorithms(algorithmList.get());
          } else {
            app.setAlgorithm(algorithmList.get().get(selectedAlgorithmIndex.getAsInt()));
          }
          app.setStart(true);
          cancelButton.setEnabled(true);
        });

    JPanel buttonsRow = new JPanel();
    buttonsRow.setLayout(new BoxLayout(buttonsRow, BoxLayout.X_AXIS));
    buttonsRow.setOpaque(false);
    buttonsRow.add(Box.createHorizontalGlue());
    buttonsRow.add(cancelButton);
    buttonsRow.add(Box.createHorizontalStrut(UiTheme.SPACING_SM));
    buttonsRow.add(runButton);

    progressBarArray = ComponentFactory.createProgressBar();
    progressBarArray.setValue(100);

    panel.add(buttonsRow, BorderLayout.CENTER);
    panel.add(progressBarArray, BorderLayout.SOUTH);
  }

  public JPanel getPanel() {
    return panel;
  }

  public void setRunEnabled(boolean enabled) {
    runButton.setEnabled(enabled);
  }

  public void setCancelEnabled(boolean enabled) {
    cancelButton.setEnabled(enabled);
  }

  public boolean isCancelEnabled() {
    return cancelButton.isEnabled();
  }

  public void setProgress(int progress) {
    progressBarArray.setValue(progress);
  }
}
