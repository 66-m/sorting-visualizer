package io.github.compilerstuck.control.ui.settings;

import io.github.compilerstuck.control.AppContext;
import io.github.compilerstuck.control.catalog.VisualConstraints;
import io.github.compilerstuck.control.catalog.VisualizationCatalog;
import io.github.compilerstuck.control.catalog.VisualizationDescriptor;
import io.github.compilerstuck.control.ui.ComponentFactory;
import io.github.compilerstuck.control.ui.StyledCard;
import io.github.compilerstuck.control.ui.UiTheme;
import io.github.compilerstuck.visual.ImageHorizontal;
import io.github.compilerstuck.visual.ImageVertical;
import io.github.compilerstuck.visual.Visualization;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Visualization type picker and image-file button. */
public final class VisualizationPanel {
  private static final int MIN_ARRAY_SIZE = 3;
  private static final int MAX_ARRAY_SIZE = 20000;

  private final AppContext app;

  private final List<VisualizationDescriptor> visualizationDescriptors;
  private final ArrayList<Visualization> visualizationList;
  private final JComboBox<String> visualizationListComboBox;
  private final JButton buttonSetImg;
  private final StyledCard card;
  private IntConsumer sizeDisplaySync = size -> {};

  public VisualizationPanel(AppContext app) {
    this.app = app;

    card = ComponentFactory.createCard();
    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

    visualizationDescriptors = VisualizationCatalog.all();
    visualizationList = new ArrayList<>();
    for (VisualizationDescriptor descriptor : visualizationDescriptors) {
      visualizationList.add(
          descriptor
              .factory()
              .create(
                  app.getArrayController(),
                  app.getColorGradient(),
                  app.getSound(),
                  app.getRenderContext()));
    }

    visualizationListComboBox = ComponentFactory.createComboBox();
    for (Visualization visualization : visualizationList) {
      visualizationListComboBox.addItem(visualization.getName());
    }
    visualizationListComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    visualizationListComboBox.setMaximumSize(
        new Dimension(Integer.MAX_VALUE, UiTheme.INPUT_HEIGHT));
    visualizationListComboBox.setSelectedIndex(
        VisualizationCatalog.indexOfId(app.getPreferences().getVisualizationId()));

    buttonSetImg = ComponentFactory.createSmallButton("Select Image");
    buttonSetImg.setVisible(false);
    buttonSetImg.setAlignmentX(Component.LEFT_ALIGNMENT);
    buttonSetImg.addActionListener(e -> selectImageFile());

    visualizationListComboBox.addActionListener(e -> applySelectedVisualization());

    // Sync restored visualization without resizing again if already valid
    applySelectedVisualization();

    card.add(visualizationListComboBox);
    card.add(Box.createVerticalStrut(UiTheme.SPACING_SM));
    card.add(buttonSetImg);
  }

  public StyledCard getCard() {
    return card;
  }

  /** Called when a constrained visualization auto-fits the array size. */
  public void setSizeDisplaySync(IntConsumer sizeDisplaySync) {
    this.sizeDisplaySync = sizeDisplaySync != null ? sizeDisplaySync : size -> {};
  }

  public VisualConstraints currentConstraints() {
    int index = visualizationListComboBox.getSelectedIndex();
    if (index < 0 || index >= visualizationDescriptors.size()) {
      return null;
    }
    return visualizationDescriptors.get(index).constraints();
  }

  public void setInputsEnabled(boolean enabled) {
    visualizationListComboBox.setEnabled(enabled);
  }

  private void applySelectedVisualization() {
    int index = visualizationListComboBox.getSelectedIndex();
    if (index < 0 || index >= visualizationDescriptors.size()) {
      return;
    }

    VisualConstraints constraints = visualizationDescriptors.get(index).constraints();
    if (!constraints.requiresImage()) {
      int fitted = constraints.fitSize(app.getSize(), MIN_ARRAY_SIZE, MAX_ARRAY_SIZE);
      if (fitted != app.getSize()) {
        app.updateArraySize(fitted);
        sizeDisplaySync.accept(fitted);
      }
    }

    Visualization visualization = visualizationList.get(index);
    app.setVisualization(visualization);
    app.setVisualizationId(visualizationDescriptors.get(index).id());
    boolean isImage = constraints.requiresImage();
    buttonSetImg.setVisible(isImage);
    buttonSetImg.setEnabled(isImage);
  }

  private void selectImageFile() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Select an image for visualization");
    fileChooser.setAcceptAllFileFilterUsed(false);
    FileNameExtensionFilter filter =
        new FileNameExtensionFilter("PNG and JPG images", "png", "jpg");
    fileChooser.addChoosableFileFilter(filter);
    int retval = fileChooser.showDialog(null, "Select image");
    if (retval == JFileChooser.APPROVE_OPTION) {
      File selectedFile = fileChooser.getSelectedFile();
      String imagePath = selectedFile.getAbsolutePath();

      int index = visualizationListComboBox.getSelectedIndex();
      Visualization selectedVisualization = visualizationList.get(index);
      if (selectedVisualization instanceof ImageVertical imageVertical) {
        imageVertical.setImg(imagePath);
        app.setVisualization(imageVertical);
      } else if (selectedVisualization instanceof ImageHorizontal imageHorizontal) {
        imageHorizontal.setImg(imagePath);
        app.setVisualization(imageHorizontal);
      }
    }
  }
}
