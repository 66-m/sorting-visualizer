package io.github.compilerstuck.control.ui.settingsfx.vm;

/** In-memory run-all list row (selection and order persisted via preferences). */
public final class AlgorithmEntry {

  private final String id;
  private final String name;
  private boolean selected;

  public AlgorithmEntry(String id, String name, boolean selected) {
    this.id = id;
    this.name = name;
    this.selected = selected;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }
}
