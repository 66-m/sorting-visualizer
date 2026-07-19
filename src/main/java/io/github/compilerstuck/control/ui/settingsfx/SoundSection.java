package io.github.compilerstuck.control.ui.settingsfx;

import atlantafx.base.controls.ToggleSwitch;
import io.github.compilerstuck.control.ui.settingsfx.vm.SoundViewModel;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/** Sound effects toggle bound to {@link SoundViewModel}. */
public final class SoundSection {

  public static final String ROOT_ID = "section-sound";

  private SoundSection() {}

  public static Node build(SoundViewModel vm) {
    ToggleSwitch toggle = new ToggleSwitch(SettingsStrings.SOUND_EFFECTS);
    toggle.setSelected(vm.isSoundEnabled());
    toggle
        .selectedProperty()
        .addListener(
            (obs, old, selected) -> {
              if (selected != vm.isSoundEnabled()) {
                vm.setSoundEnabled(selected);
              }
            });
    vm.addPropertyChangeListener(
        evt -> {
          if (SoundViewModel.PROP_SOUND_ENABLED.equals(evt.getPropertyName())) {
            boolean enabled = Boolean.TRUE.equals(evt.getNewValue());
            VmBindings.runFx(
                () -> {
                  if (toggle.isSelected() != enabled) {
                    toggle.setSelected(enabled);
                  }
                });
          }
        });
    VmBindings.bindInputsEnabled(
        toggle,
        vm::isInputsEnabled,
        vm::addPropertyChangeListener,
        SoundViewModel.PROP_INPUTS_ENABLED);

    VBox root = new VBox(toggle);
    root.setId(ROOT_ID);
    return root;
  }
}
