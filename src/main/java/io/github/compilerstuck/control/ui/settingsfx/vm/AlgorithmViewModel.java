package io.github.compilerstuck.control.ui.settingsfx.vm;

import io.github.compilerstuck.control.AppContext;
import io.github.compilerstuck.control.catalog.AlgorithmCatalog;
import io.github.compilerstuck.control.catalog.AlgorithmDescriptor;
import io.github.compilerstuck.control.config.RunAllEntryPref;
import io.github.compilerstuck.control.config.ShuffleType;
import io.github.compilerstuck.control.config.UserPreferences;
import io.github.compilerstuck.sortingalgorithms.SortingAlgorithm;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Headless algorithm / run-all / shuffle view-model. Run-all order and selection persist via {@link
 * UserPreferences}.
 */
public final class AlgorithmViewModel {

  public static final String PROP_SELECTED_ID = "selectedId";
  public static final String PROP_RUN_ALL = "runAll";
  public static final String PROP_SHUFFLE_TYPE = "shuffleType";
  public static final String PROP_ENTRIES = "entries";
  public static final String PROP_CAN_START = "canStart";
  public static final String PROP_INPUTS_ENABLED = "inputsEnabled";

  private static final List<ShuffleType> SHUFFLE_TYPES =
      List.of(
          ShuffleType.RANDOM, ShuffleType.REVERSE, ShuffleType.ALMOST_SORTED, ShuffleType.SORTED);

  private final AppContext app;
  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  private final List<AlgorithmDescriptor> descriptors = new ArrayList<>();
  private final List<SortingAlgorithm> algorithms = new ArrayList<>();
  private final List<AlgorithmEntry> entries = new ArrayList<>();

  private String selectedId;
  private boolean runAll;
  private ShuffleType shuffleType = ShuffleType.RANDOM;
  private boolean inputsEnabled = true;

  public AlgorithmViewModel(AppContext app) {
    this.app = app;
    for (AlgorithmDescriptor descriptor : AlgorithmCatalog.all()) {
      SortingAlgorithm alg =
          descriptor.factory().apply(app.getArrayController(), app.getDelayContext());
      alg.setOperationReporter(app.getStateManager()::setCurrentOperation);
      descriptors.add(descriptor);
      algorithms.add(alg);
      entries.add(new AlgorithmEntry(descriptor.id(), alg.getName(), alg.isSelected()));
    }
    restoreRunAllFromPreferences(app.getPreferences());
    int index = AlgorithmCatalog.indexOfId(app.getPreferences().getAlgorithmId());
    // After reorder, resolve by id rather than catalog index.
    int selectedIndex = indexOfId(app.getPreferences().getAlgorithmId());
    if (selectedIndex < 0) {
      selectedIndex = Math.min(Math.max(index, 0), descriptors.size() - 1);
    }
    selectedId = descriptors.get(selectedIndex).id();
    app.setAlgorithm(algorithms.get(selectedIndex));
    shuffleType = app.getArrayController().getShuffleType();
    runAll = app.getPreferences().isRunAll();
  }

  private void restoreRunAllFromPreferences(UserPreferences prefs) {
    List<RunAllEntryPref> saved = prefs.getRunAllEntriesList();
    if (saved.isEmpty()) {
      return;
    }
    Map<String, Integer> currentIndex = new HashMap<>();
    for (int i = 0; i < descriptors.size(); i++) {
      currentIndex.put(descriptors.get(i).id(), i);
    }

    List<AlgorithmDescriptor> newDescriptors = new ArrayList<>();
    List<SortingAlgorithm> newAlgorithms = new ArrayList<>();
    List<AlgorithmEntry> newEntries = new ArrayList<>();
    Set<String> seen = new HashSet<>();

    for (RunAllEntryPref pref : saved) {
      Integer idx = currentIndex.get(pref.id());
      if (idx == null || !seen.add(pref.id())) {
        continue;
      }
      AlgorithmDescriptor descriptor = descriptors.get(idx);
      SortingAlgorithm alg = algorithms.get(idx);
      alg.setSelected(pref.selected());
      newDescriptors.add(descriptor);
      newAlgorithms.add(alg);
      newEntries.add(new AlgorithmEntry(descriptor.id(), alg.getName(), pref.selected()));
    }
    for (int i = 0; i < descriptors.size(); i++) {
      String id = descriptors.get(i).id();
      if (seen.add(id)) {
        newDescriptors.add(descriptors.get(i));
        newAlgorithms.add(algorithms.get(i));
        newEntries.add(entries.get(i));
      }
    }
    descriptors.clear();
    descriptors.addAll(newDescriptors);
    algorithms.clear();
    algorithms.addAll(newAlgorithms);
    entries.clear();
    entries.addAll(newEntries);
  }

  public List<AlgorithmDescriptor> getDescriptors() {
    return Collections.unmodifiableList(descriptors);
  }

  public List<AlgorithmEntry> getEntries() {
    return Collections.unmodifiableList(entries);
  }

  public List<ShuffleType> getShuffleTypes() {
    return SHUFFLE_TYPES;
  }

  public String getSelectedId() {
    return selectedId;
  }

  public boolean isRunAll() {
    return runAll;
  }

  public ShuffleType getShuffleType() {
    return shuffleType;
  }

  public void selectAlgorithm(String id) {
    if (!inputsEnabled || runAll) {
      return;
    }
    int index = indexOfId(id);
    if (index < 0 || id.equals(selectedId)) {
      return;
    }
    String old = selectedId;
    selectedId = id;
    app.setAlgorithm(algorithms.get(index));
    app.setAlgorithmId(descriptors.get(index).id());
    pcs.firePropertyChange(PROP_SELECTED_ID, old, selectedId);
  }

  public void setRunAll(boolean value) {
    if (!inputsEnabled || runAll == value) {
      return;
    }
    boolean old = runAll;
    boolean oldCanStart = canStart();
    runAll = value;
    persistRunAll();
    pcs.firePropertyChange(PROP_RUN_ALL, old, value);
    pcs.firePropertyChange(PROP_CAN_START, oldCanStart, canStart());
  }

  /** True when a single algorithm is selected, or run-all has at least one entry checked. */
  public boolean canStart() {
    if (!runAll) {
      return selectedId != null;
    }
    for (AlgorithmEntry entry : entries) {
      if (entry.isSelected()) {
        return true;
      }
    }
    return false;
  }

  public void setShuffleType(ShuffleType type) {
    if (!inputsEnabled || type == null || type == shuffleType) {
      return;
    }
    ShuffleType old = shuffleType;
    shuffleType = type;
    app.setShuffleType(type);
    pcs.firePropertyChange(PROP_SHUFFLE_TYPE, old, type);
  }

  public void setEntrySelected(int index, boolean selected) {
    if (!inputsEnabled || index < 0 || index >= entries.size()) {
      return;
    }
    AlgorithmEntry entry = entries.get(index);
    if (entry.isSelected() == selected) {
      return;
    }
    boolean oldCanStart = canStart();
    entry.setSelected(selected);
    algorithms.get(index).setSelected(selected);
    persistRunAll();
    pcs.firePropertyChange(PROP_ENTRIES, null, getEntries());
    pcs.firePropertyChange(PROP_CAN_START, oldCanStart, canStart());
  }

  public void moveEntry(int fromIndex, int toIndex) {
    if (!inputsEnabled
        || fromIndex < 0
        || toIndex < 0
        || fromIndex >= entries.size()
        || toIndex >= entries.size()
        || fromIndex == toIndex) {
      return;
    }
    entries.add(toIndex, entries.remove(fromIndex));
    algorithms.add(toIndex, algorithms.remove(fromIndex));
    descriptors.add(toIndex, descriptors.remove(fromIndex));
    persistRunAll();
    pcs.firePropertyChange(PROP_ENTRIES, null, getEntries());
  }

  /**
   * Replaces run-all order and selection from a complete snapshot (e.g. after drag-reorder in the
   * configure-order dialog). {@code orderedIds} must be a permutation of the current entry ids.
   */
  public void applyRunAllOrder(List<String> orderedIds, Set<String> selectedIds) {
    if (!inputsEnabled || orderedIds == null || selectedIds == null) {
      return;
    }
    if (orderedIds.size() != entries.size()) {
      return;
    }
    Map<String, Integer> currentIndex = new HashMap<>();
    for (int i = 0; i < entries.size(); i++) {
      currentIndex.put(entries.get(i).getId(), i);
    }
    if (orderedIds.size() != new HashSet<>(orderedIds).size()) {
      return;
    }
    for (String id : orderedIds) {
      if (!currentIndex.containsKey(id)) {
        return;
      }
    }

    List<AlgorithmDescriptor> newDescriptors = new ArrayList<>(orderedIds.size());
    List<SortingAlgorithm> newAlgorithms = new ArrayList<>(orderedIds.size());
    List<AlgorithmEntry> newEntries = new ArrayList<>(orderedIds.size());
    boolean oldCanStart = canStart();
    for (String id : orderedIds) {
      int idx = currentIndex.get(id);
      boolean selected = selectedIds.contains(id);
      AlgorithmDescriptor descriptor = descriptors.get(idx);
      SortingAlgorithm alg = algorithms.get(idx);
      alg.setSelected(selected);
      newDescriptors.add(descriptor);
      newAlgorithms.add(alg);
      newEntries.add(new AlgorithmEntry(id, entries.get(idx).getName(), selected));
    }
    descriptors.clear();
    descriptors.addAll(newDescriptors);
    algorithms.clear();
    algorithms.addAll(newAlgorithms);
    entries.clear();
    entries.addAll(newEntries);
    persistRunAll();
    pcs.firePropertyChange(PROP_ENTRIES, null, getEntries());
    pcs.firePropertyChange(PROP_CAN_START, oldCanStart, canStart());
  }

  /**
   * Applies the current selection to AppContext for a Run action: either the single selected
   * algorithm or all selected run-all entries in order.
   */
  public void applySelectionToAppContext() {
    if (runAll) {
      for (int i = 0; i < algorithms.size(); i++) {
        algorithms.get(i).setSelected(entries.get(i).isSelected());
      }
      app.setAlgorithms(algorithms);
    } else {
      int index = indexOfId(selectedId);
      if (index >= 0) {
        app.setAlgorithm(algorithms.get(index));
      }
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

  private void persistRunAll() {
    List<RunAllEntryPref> prefs = new ArrayList<>(entries.size());
    for (AlgorithmEntry entry : entries) {
      prefs.add(new RunAllEntryPref(entry.getId(), entry.isSelected()));
    }
    app.persistRunAll(runAll, prefs);
  }

  private int indexOfId(String id) {
    for (int i = 0; i < descriptors.size(); i++) {
      if (descriptors.get(i).id().equals(id)) {
        return i;
      }
    }
    return -1;
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
  }
}
