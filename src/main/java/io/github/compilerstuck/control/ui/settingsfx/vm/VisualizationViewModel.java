package io.github.compilerstuck.control.ui.settingsfx.vm;

import io.github.compilerstuck.control.AppContext;
import io.github.compilerstuck.control.catalog.VisualConstraints;
import io.github.compilerstuck.control.catalog.VisualizationCatalog;
import io.github.compilerstuck.control.catalog.VisualizationDescriptor;
import io.github.compilerstuck.control.config.SettingsDefaults;
import io.github.compilerstuck.control.config.visual.VisualizationSettings;
import io.github.compilerstuck.control.ui.settingsfx.customize.VisualizationCustomizePanels;
import io.github.compilerstuck.visual.ConfigurableVisualization;
import io.github.compilerstuck.visual.ImageSourceVisualization;
import io.github.compilerstuck.visual.Visualization;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

/** Headless visualization view-model with image-path validation (G3). */
public final class VisualizationViewModel {

  public static final String PROP_SELECTED_ID = "selectedId";
  public static final String PROP_NEEDS_IMAGE = "needsImage";
  public static final String PROP_IMAGE_PATH = "imagePath";
  public static final String PROP_IMAGE_ERROR = "imageError";
  public static final String PROP_INPUTS_ENABLED = "inputsEnabled";
  public static final String PROP_CONFIGURABLE = "configurable";

  private final AppContext app;
  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  private final List<VisualizationDescriptor> descriptors;
  private final Map<String, Visualization> visualizationsById = new HashMap<>();

  private String selectedId;
  private boolean needsImage;
  private String imagePath = "";
  private String imageError = "";
  private boolean inputsEnabled = true;
  private boolean configurable;
  private IntConsumer sizeDisplaySync = size -> {};

  public VisualizationViewModel(AppContext app) {
    this.app = app;
    this.descriptors = VisualizationCatalog.all();
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

  /** True when the selected visualization implements {@link ConfigurableVisualization}. */
  public boolean isConfigurable() {
    return configurable;
  }

  public VisualConstraints currentConstraints() {
    int index = indexOfId(selectedId);
    if (index < 0) {
      return VisualConstraints.NONE;
    }
    return descriptors.get(index).constraints();
  }

  public String getImagePath() {
    return imagePath;
  }

  public String getImageError() {
    return imageError;
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
  }

  private int indexOfId(String id) {
    for (int i = 0; i < descriptors.size(); i++) {
      if (descriptors.get(i).id().equals(id)) {
        return i;
      }
    }
    return -1;
  }

  private Visualization getOrCreate(String id) {
    Visualization existing = visualizationsById.get(id);
    if (existing != null) {
      return existing;
    }
    // Adopt the composition-root instance for the initially selected id (avoids a duplicate).
    Visualization fromApp = app.getVisualization();
    if (fromApp != null
        && visualizationsById.isEmpty()
        && id.equals(app.getPreferences().getVisualizationId())) {
      visualizationsById.put(id, fromApp);
      return fromApp;
    }
    VisualizationDescriptor descriptor = VisualizationCatalog.findById(id);
    Visualization viz =
        descriptor
            .factory()
            .create(
                app.getPublishedArray(),
                app.getColorGradient(),
                app.getSound(),
                app.getRenderSystem());
    VisualizationSettings saved = app.getPreferences().getVisualSettingsMap().get(id);
    if (saved != null && viz instanceof ConfigurableVisualization configurableViz) {
      configurableViz.applySettings(saved);
    }
    visualizationsById.put(id, viz);
    return viz;
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
   * Validates and applies an image path for the current image visualization. Returns {@code true}
   * on success.
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

    Visualization viz = getOrCreate(selectedId);
    String absolute = path.toAbsolutePath().toString();
    boolean loaded = false;
    if (viz instanceof ImageSourceVisualization imageViz) {
      loaded = app.loadImageForVisualization(imageViz, absolute);
      if (loaded) {
        app.setVisualization(viz);
      }
    }

    if (!loaded) {
      imageError = "Could not decode image";
      pcs.firePropertyChange(PROP_IMAGE_ERROR, oldError, imageError);
      return false;
    }

    imagePath = absolute;
    imageError = "";
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

  public VisualizationSettings getCurrentCustomization() {
    if (selectedId == null) {
      return null;
    }
    Visualization viz = getOrCreate(selectedId);
    if (viz instanceof ConfigurableVisualization configurableViz) {
      return configurableViz.currentSettings();
    }
    return null;
  }

  /**
   * Applies settings to the live visualization without persisting. Used for customize-dialog draft
   * preview; call {@link #applyCustomization} to save.
   */
  public boolean previewCustomization(VisualizationSettings settings) {
    ConfigurableVisualization configurableViz = configurableVisualizationFor(settings);
    if (configurableViz == null) {
      return false;
    }
    configurableViz.applySettings(settings);
    return true;
  }

  public boolean applyCustomization(VisualizationSettings settings) {
    ConfigurableVisualization configurableViz = configurableVisualizationFor(settings);
    if (configurableViz == null) {
      return false;
    }
    configurableViz.applySettings(settings);
    app.saveVisualizationSettings(settings);
    return true;
  }

  /**
   * Clears persisted custom settings and applies defaults live only to already-created
   * visualizations (and the current selection). Other types keep defaults on next create.
   *
   * @return {@code true} if the reset ran (inputs enabled)
   */
  public boolean resetAllCustomizations() {
    if (!inputsEnabled) {
      return false;
    }
    app.clearAllVisualizationSettings();
    if (selectedId != null) {
      getOrCreate(selectedId);
    }
    for (Map.Entry<String, Visualization> entry : visualizationsById.entrySet()) {
      VisualizationCustomizePanels.defaultsFor(entry.getKey())
          .ifPresent(
              defaults -> {
                if (entry.getValue() instanceof ConfigurableVisualization configurableViz) {
                  configurableViz.applySettings(defaults);
                }
              });
    }
    return true;
  }

  private ConfigurableVisualization configurableVisualizationFor(VisualizationSettings settings) {
    if (!inputsEnabled || settings == null) {
      return null;
    }
    if (!settings.visualizationId().equals(selectedId)) {
      return null;
    }
    Visualization viz = getOrCreate(selectedId);
    if (!(viz instanceof ConfigurableVisualization configurableViz)) {
      return null;
    }
    return configurableViz;
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
    boolean oldConfigurable = configurable;
    selectedId = descriptor.id();
    needsImage = constraints.requiresImage();
    Visualization viz = getOrCreate(selectedId);
    configurable = viz instanceof ConfigurableVisualization;
    app.setVisualization(viz);
    app.setVisualizationId(descriptor.id());

    if (fireEvents) {
      pcs.firePropertyChange(PROP_SELECTED_ID, oldId, selectedId);
      pcs.firePropertyChange(PROP_NEEDS_IMAGE, oldNeeds, needsImage);
      pcs.firePropertyChange(PROP_CONFIGURABLE, oldConfigurable, configurable);
    }
  }
}
