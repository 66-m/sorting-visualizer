package io.github.compilerstuck.control.ui.settingsfx.vm;

import io.github.compilerstuck.control.AppContext;
import io.github.compilerstuck.control.catalog.VisualConstraints;
import io.github.compilerstuck.control.catalog.VisualizationCatalog;
import io.github.compilerstuck.control.catalog.VisualizationDescriptor;
import io.github.compilerstuck.control.config.SettingsDefaults;
import io.github.compilerstuck.visual.ImageHorizontal;
import io.github.compilerstuck.visual.ImageVertical;
import io.github.compilerstuck.visual.Visualization;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntConsumer;

/** Headless visualization view-model with image-path validation (G3). */
public final class VisualizationViewModel {

  public static final String PROP_SELECTED_ID = "selectedId";
  public static final String PROP_NEEDS_IMAGE = "needsImage";
  public static final String PROP_IMAGE_PATH = "imagePath";
  public static final String PROP_IMAGE_ERROR = "imageError";
  public static final String PROP_INPUTS_ENABLED = "inputsEnabled";

  private final AppContext app;
  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  private final List<VisualizationDescriptor> descriptors;
  private final List<Visualization> visualizations;

  private String selectedId;
  private boolean needsImage;
  private String imagePath = "";
  private String imageError = "";
  private boolean inputsEnabled = true;
  private IntConsumer sizeDisplaySync = size -> {};

  public VisualizationViewModel(AppContext app) {
    this.app = app;
    this.descriptors = VisualizationCatalog.all();
    this.visualizations = new ArrayList<>();
    for (VisualizationDescriptor descriptor : descriptors) {
      visualizations.add(
          descriptor
              .factory()
              .create(
                  app.getArrayController(),
                  app.getColorGradient(),
                  app.getSound(),
                  app.getRenderContext()));
    }
    String id = app.getPreferences().getVisualizationId();
    int index = VisualizationCatalog.indexOfId(id);
    applySelection(index, false);
    if (needsImage) {
      String savedPath = app.getPreferences().getImagePath();
      if (savedPath != null && !savedPath.isBlank()) {
        setImagePath(Path.of(savedPath));
      }
    }
  }

  public void setSizeDisplaySync(IntConsumer sizeDisplaySync) {
    this.sizeDisplaySync = sizeDisplaySync != null ? sizeDisplaySync : size -> {};
  }

  public List<VisualizationDescriptor> getDescriptors() {
    return Collections.unmodifiableList(descriptors);
  }

  public String getSelectedId() {
    return selectedId;
  }

  public boolean needsImage() {
    return needsImage;
  }

  public String getImagePath() {
    return imagePath;
  }

  public String getImageError() {
    return imageError;
  }

  public VisualConstraints currentConstraints() {
    int index = indexOfId(selectedId);
    if (index < 0) {
      return VisualConstraints.NONE;
    }
    return descriptors.get(index).constraints();
  }

  public void selectVisualization(String id) {
    if (!inputsEnabled) {
      return;
    }
    int index = indexOfId(id);
    if (index < 0) {
      return;
    }
    applySelection(index, true);
  }

  /**
   * Validates and applies an image path for the current image visualization (G3). Returns {@code
   * true} on success.
   */
  public boolean setImagePath(Path path) {
    if (!inputsEnabled || !needsImage) {
      return false;
    }
    String oldPath = imagePath;
    String oldError = imageError;

    if (path == null || !Files.isRegularFile(path) || !Files.isReadable(path)) {
      imageError = "Image file not found or not readable";
      pcs.firePropertyChange(PROP_IMAGE_ERROR, oldError, imageError);
      return false;
    }

    int index = indexOfId(selectedId);
    Visualization viz = visualizations.get(index);
    boolean loaded = false;
    String absolute = path.toAbsolutePath().toString();
    if (viz instanceof ImageVertical imageVertical) {
      loaded = imageVertical.setImg(absolute);
      if (loaded) {
        app.setVisualization(imageVertical);
      }
    } else if (viz instanceof ImageHorizontal imageHorizontal) {
      loaded = imageHorizontal.setImg(absolute);
      if (loaded) {
        app.setVisualization(imageHorizontal);
      }
    }

    if (!loaded) {
      imageError = "Could not decode image";
      pcs.firePropertyChange(PROP_IMAGE_ERROR, oldError, imageError);
      return false;
    }

    imagePath = absolute;
    imageError = "";
    app.setImagePath(absolute);
    pcs.firePropertyChange(PROP_IMAGE_PATH, oldPath, imagePath);
    pcs.firePropertyChange(PROP_IMAGE_ERROR, oldError, imageError);
    return true;
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

  private void applySelection(int index, boolean fireEvents) {
    VisualizationDescriptor descriptor = descriptors.get(index);
    VisualConstraints constraints = descriptor.constraints();

    if (!constraints.requiresImage()) {
      int fitted =
          constraints.fitSize(
              app.getSize(), SettingsDefaults.ARRAY_SIZE_MIN, SettingsDefaults.ARRAY_SIZE_MAX);
      if (fitted != app.getSize() && !app.isRunning()) {
        app.updateArraySize(fitted);
        sizeDisplaySync.accept(fitted);
      }
    }

    String oldId = selectedId;
    boolean oldNeeds = needsImage;
    selectedId = descriptor.id();
    needsImage = constraints.requiresImage();
    app.setVisualization(visualizations.get(index));
    app.setVisualizationId(descriptor.id());

    if (fireEvents) {
      pcs.firePropertyChange(PROP_SELECTED_ID, oldId, selectedId);
      pcs.firePropertyChange(PROP_NEEDS_IMAGE, oldNeeds, needsImage);
    }
  }

  private int indexOfId(String id) {
    for (int i = 0; i < descriptors.size(); i++) {
      if (descriptors.get(i).id().equals(id)) {
        return i;
      }
    }
    return VisualizationCatalog.indexOfId(id);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
  }
}
