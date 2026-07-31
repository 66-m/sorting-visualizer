package io.github.compilerstuck.control.ui.settingsfx;

import io.github.compilerstuck.control.ui.settingsfx.vm.SoundViewModel;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;

/** Sound effects checkbox bound to {@link SoundViewModel}. */
public final class SoundSection {

  public static final String ROOT_ID = "section-sound";

  private SoundSection() {}

  public static Node build(SoundViewModel vm) {
    CheckBox soundEffects = new CheckBox(SettingsStrings.SOUND_EFFECTS);
    soundEffects.setId(ROOT_ID);
    soundEffects.setTooltip(new Tooltip(SettingsStrings.SOUND_EFFECTS_TOOLTIP));
    soundEffects.setSelected(vm.isSoundEnabled());
    soundEffects
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
                  if (soundEffects.isSelected() != enabled) {
                    soundEffects.setSelected(enabled);
                  }
                });
          }
        });
    VmBindings.bindInputsEnabled(
        soundEffects,
        vm::isInputsEnabled,
        vm::addPropertyChangeListener,
        SoundViewModel.PROP_INPUTS_ENABLED);

    return soundEffects;
  }
}
