package io.github.compilerstuck.control.ui.settingsfx;

import atlantafx.base.theme.Styles;
import io.github.compilerstuck.control.config.audio.AudioSettings;
import io.github.compilerstuck.control.config.audio.AudioSettingsCodec;
import io.github.compilerstuck.control.config.audio.GeneralMidiInstruments;
import io.github.compilerstuck.control.ui.settingsfx.customize.CustomizePanelSupport;
import io.github.compilerstuck.control.ui.settingsfx.vm.AudioSettingsViewModel;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.StringConverter;

/**
 * Modal dialog for customizing the MIDI sound engine: instrument/voice, levels, pitch mapping, and
 * advanced GM2 sound controllers. Includes live "Play test tone" / "Simulate shuffle" previews and
 * clipboard export/import, mirroring {@link VisualizationCustomizeDialog}'s chrome.
 */
public final class AudioSettingsDialog {

  public static final String DIALOG_ID = "audio-settings-dialog";
  public static final String IMPORT_DIALOG_ID = "audio-settings-import-dialog";
  public static final String IMPORT_TEXT_ID = "audio-settings-import-text";
  public static final String UNSAVED_CONFIRM_ID = "audio-settings-unsaved-confirm";

  private static final int SHUFFLE_PREVIEW_SIMULATED_LENGTH = 5000;
  private static final long SHUFFLE_PREVIEW_DURATION_MS = 500L;

  private AudioSettingsDialog() {}

  public static void show(Window owner, AudioSettingsViewModel vm) {
    AudioSettings initial = vm.getSettings();

    ComboBox<Integer> instrument = new ComboBox<>();
    for (int program = 0; program < GeneralMidiInstruments.NAMES.length; program++) {
      instrument.getItems().add(program);
    }
    instrument.setConverter(
        new StringConverter<>() {
          @Override
          public String toString(Integer program) {
            return program == null ? "" : GeneralMidiInstruments.nameFor(program);
          }

          @Override
          public Integer fromString(String string) {
            return instrument.getValue();
          }
        });

    Slider volume = midiSlider(initial.volume());
    Slider velocity =
        new Slider(AudioSettings.VELOCITY_MIN, AudioSettings.MIDI_MAX, initial.velocity());
    Slider pan = midiSlider(initial.pan());
    Slider lowNote = midiSlider(initial.lowNote());
    Slider highNote = midiSlider(initial.highNote());
    Slider reverb = midiSlider(initial.reverb());
    Slider chorus = midiSlider(initial.chorus());
    Slider attackTime = midiSlider(initial.attackTime());
    Slider releaseTime = midiSlider(initial.releaseTime());
    Slider brightness = midiSlider(initial.brightness());
    CustomizePanelSupport.configureSlider(velocity, true);
    for (Slider s :
        new Slider[] {
          volume, pan, lowNote, highNote, reverb, chorus, attackTime, releaseTime, brightness
        }) {
      CustomizePanelSupport.configureSlider(s, true);
    }

    Label volumeValue = CustomizePanelSupport.valueLabel();
    Label velocityValue = CustomizePanelSupport.valueLabel();
    Label panValue = CustomizePanelSupport.valueLabel();
    Label lowNoteValue = CustomizePanelSupport.valueLabel();
    Label highNoteValue = CustomizePanelSupport.valueLabel();
    Label reverbValue = CustomizePanelSupport.valueLabel();
    Label chorusValue = CustomizePanelSupport.valueLabel();
    Label attackTimeValue = CustomizePanelSupport.valueLabel();
    Label releaseTimeValue = CustomizePanelSupport.valueLabel();
    Label brightnessValue = CustomizePanelSupport.valueLabel();

    CustomizePanelSupport.bindValueLabel(volume, volumeValue, v -> String.valueOf((int) v));
    CustomizePanelSupport.bindValueLabel(velocity, velocityValue, v -> String.valueOf((int) v));
    CustomizePanelSupport.bindValueLabel(pan, panValue, v -> panLabel((int) v));
    CustomizePanelSupport.bindValueLabel(lowNote, lowNoteValue, v -> noteLabel((int) v));
    CustomizePanelSupport.bindValueLabel(highNote, highNoteValue, v -> noteLabel((int) v));
    CustomizePanelSupport.bindValueLabel(reverb, reverbValue, v -> String.valueOf((int) v));
    CustomizePanelSupport.bindValueLabel(chorus, chorusValue, v -> String.valueOf((int) v));
    CustomizePanelSupport.bindValueLabel(attackTime, attackTimeValue, v -> String.valueOf((int) v));
    CustomizePanelSupport.bindValueLabel(
        releaseTime, releaseTimeValue, v -> String.valueOf((int) v));
    CustomizePanelSupport.bindValueLabel(brightness, brightnessValue, v -> String.valueOf((int) v));

    // Keep highNote >= lowNote while dragging (record clamps on Apply, but the live slider should
    // not visually cross itself).
    lowNote
        .valueProperty()
        .addListener(
            (obs, old, value) -> {
              if (value.doubleValue() > highNote.getValue()) {
                highNote.setValue(value.doubleValue());
              }
            });
    highNote
        .valueProperty()
        .addListener(
            (obs, old, value) -> {
              if (value.doubleValue() < lowNote.getValue()) {
                lowNote.setValue(value.doubleValue());
              }
            });

    Button instrumentUp = new Button("▲");
    Button instrumentDown = new Button("▼");
    for (Button stepper : new Button[] {instrumentUp, instrumentDown}) {
      stepper.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.SMALL);
      stepper.setMinWidth(28);
      stepper.setPrefWidth(28);
      stepper.setMaxWidth(28);
    }
    instrumentUp.setTooltip(new Tooltip(SettingsStrings.AUDIO_STEPPER_UP_TOOLTIP));
    instrumentDown.setTooltip(new Tooltip(SettingsStrings.AUDIO_STEPPER_DOWN_TOOLTIP));
    instrumentUp.setOnAction(e -> stepCombo(instrument, -1));
    instrumentDown.setOnAction(e -> stepCombo(instrument, 1));
    VBox instrumentStepper = new VBox(0, instrumentUp, instrumentDown);
    instrumentStepper.getStyleClass().add("audio-instrument-stepper");
    HBox instrumentControl = new HBox(2, instrument, instrumentStepper);
    HBox.setHgrow(instrument, Priority.ALWAYS);
    instrument.setMaxWidth(Double.MAX_VALUE);

    Region instrumentValuePlaceholder = new Region();
    instrumentValuePlaceholder.setMinWidth(CustomizePanelSupport.VALUE_WIDTH);
    instrumentValuePlaceholder.setPrefWidth(CustomizePanelSupport.VALUE_WIDTH);
    instrumentValuePlaceholder.setMaxWidth(CustomizePanelSupport.VALUE_WIDTH);

    CustomizePanelSupport.FieldRow instrumentRow =
        new CustomizePanelSupport.FieldRow(
            CustomizePanelSupport.fieldLabel(SettingsStrings.AUDIO_INSTRUMENT),
            instrumentControl,
            instrumentValuePlaceholder,
            CustomizePanelSupport.iconResetButton(
                () ->
                    instrument
                        .getSelectionModel()
                        .select(Integer.valueOf(AudioSettings.DEFAULT_INSTRUMENT_PROGRAM))));

    VBox voiceSection =
        CustomizePanelSupport.section(SettingsStrings.AUDIO_SECTION_VOICE, instrumentRow);

    VBox levelsSection =
        CustomizePanelSupport.section(
            SettingsStrings.AUDIO_SECTION_LEVELS,
            CustomizePanelSupport.sliderRow(
                SettingsStrings.AUDIO_VOLUME, volume, volumeValue, AudioSettings.DEFAULT_VOLUME),
            CustomizePanelSupport.sliderRow(
                SettingsStrings.AUDIO_VELOCITY,
                velocity,
                velocityValue,
                AudioSettings.DEFAULT_VELOCITY),
            CustomizePanelSupport.sliderRow(
                SettingsStrings.AUDIO_PAN, pan, panValue, AudioSettings.DEFAULT_PAN));

    VBox pitchSection =
        CustomizePanelSupport.section(
            SettingsStrings.AUDIO_SECTION_PITCH,
            CustomizePanelSupport.sliderRow(
                SettingsStrings.AUDIO_LOW_NOTE,
                lowNote,
                lowNoteValue,
                AudioSettings.DEFAULT_LOW_NOTE),
            CustomizePanelSupport.sliderRow(
                SettingsStrings.AUDIO_HIGH_NOTE,
                highNote,
                highNoteValue,
                AudioSettings.DEFAULT_HIGH_NOTE));

    Label advancedHint = SettingsControls.mutedLabel(SettingsStrings.AUDIO_ADVANCED_HINT);
    VBox advancedBody =
        new VBox(
            SettingsLayout.GAP_SM,
            advancedHint,
            CustomizePanelSupport.sectionBody(
                CustomizePanelSupport.sliderRow(
                    SettingsStrings.AUDIO_REVERB,
                    reverb,
                    reverbValue,
                    AudioSettings.DEFAULT_REVERB),
                CustomizePanelSupport.sliderRow(
                    SettingsStrings.AUDIO_CHORUS,
                    chorus,
                    chorusValue,
                    AudioSettings.DEFAULT_CHORUS),
                CustomizePanelSupport.sliderRow(
                    SettingsStrings.AUDIO_ATTACK_TIME,
                    attackTime,
                    attackTimeValue,
                    AudioSettings.DEFAULT_ATTACK_TIME),
                CustomizePanelSupport.sliderRow(
                    SettingsStrings.AUDIO_RELEASE_TIME,
                    releaseTime,
                    releaseTimeValue,
                    AudioSettings.DEFAULT_RELEASE_TIME),
                CustomizePanelSupport.sliderRow(
                    SettingsStrings.AUDIO_BRIGHTNESS,
                    brightness,
                    brightnessValue,
                    AudioSettings.DEFAULT_BRIGHTNESS)));
    advancedBody.getStyleClass().add("customize-section");
    TitledPane advancedPane = new TitledPane(SettingsStrings.AUDIO_SECTION_ADVANCED, advancedBody);
    advancedPane.setExpanded(false);
    advancedPane.setAnimated(false);

    Button playTestTone = new Button(SettingsStrings.AUDIO_PLAY_TEST_TONE);
    playTestTone.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.SMALL);
    Button simulateShuffle = new Button(SettingsStrings.AUDIO_SIMULATE_SHUFFLE);
    simulateShuffle.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.SMALL);
    Button simulatePitchSweep = new Button(SettingsStrings.AUDIO_SIMULATE_PITCH_SWEEP);
    simulatePitchSweep.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.SMALL);

    Label status = new Label();
    status.getStyleClass().add("settings-inline-status");
    status.setWrapText(true);
    status.setVisible(false);
    status.setManaged(false);

    HBox previewRow =
        new HBox(SettingsLayout.GAP_SM, playTestTone, simulateShuffle, simulatePitchSweep);
    previewRow.setAlignment(Pos.CENTER_LEFT);

    Label autoplayLabel = SettingsControls.mutedLabel(SettingsStrings.AUDIO_AUTOPLAY_LABEL);
    ComboBox<String> autoplay = new ComboBox<>();
    autoplay
        .getItems()
        .addAll(
            SettingsStrings.AUDIO_AUTOPLAY_OFF,
            SettingsStrings.AUDIO_AUTOPLAY_TEST_TONE,
            SettingsStrings.AUDIO_AUTOPLAY_SHUFFLE,
            SettingsStrings.AUDIO_AUTOPLAY_PITCH_SWEEP);
    autoplay.getSelectionModel().select(SettingsStrings.AUDIO_AUTOPLAY_OFF);
    autoplay.setTooltip(new Tooltip(SettingsStrings.AUDIO_AUTOPLAY_TOOLTIP));
    HBox autoplayRow = new HBox(SettingsLayout.GAP_SM, autoplayLabel, autoplay);
    autoplayRow.setAlignment(Pos.CENTER_LEFT);

    Label previewHeading = new Label(SettingsStrings.AUDIO_SECTION_PREVIEW);
    previewHeading.getStyleClass().add("settings-section-label");
    VBox previewCard = new VBox(SettingsLayout.GAP_SM, previewHeading, autoplayRow, previewRow);
    previewCard.getStyleClass().addAll("customize-section", "settings-form-card");

    VBox topStack = new VBox(SettingsLayout.GAP_LG, voiceSection, levelsSection, pitchSection);
    topStack.getStyleClass().add("settings-form-stack");

    VBox content = new VBox(SettingsLayout.GAP_LG, topStack, advancedPane, previewCard, status);
    content.getStyleClass().add("customize-dialog-content");
    content.setPadding(
        new Insets(
            SettingsLayout.GAP_SM,
            SettingsLayout.GAP_SM,
            SettingsLayout.GAP_XS,
            SettingsLayout.GAP_SM));

    Supplier<AudioSettings> draft =
        () ->
            new AudioSettings(
                instrument.getValue() != null
                    ? instrument.getValue()
                    : AudioSettings.DEFAULT_INSTRUMENT_PROGRAM,
                (int) volume.getValue(),
                (int) velocity.getValue(),
                (int) pan.getValue(),
                (int) lowNote.getValue(),
                (int) highNote.getValue(),
                (int) reverb.getValue(),
                (int) chorus.getValue(),
                (int) attackTime.getValue(),
                (int) releaseTime.getValue(),
                (int) brightness.getValue());

    instrument.getSelectionModel().select(Integer.valueOf(initial.instrumentProgram()));

    // Baseline for the dirty check on close: only a successful Apply moves it forward.
    AtomicReference<AudioSettings> appliedBaseline = new AtomicReference<>(initial);

    Runnable enablePreviewButtons =
        () ->
            Platform.runLater(
                () -> {
                  playTestTone.setDisable(false);
                  simulateShuffle.setDisable(false);
                  simulatePitchSweep.setDisable(false);
                });

    Runnable runShufflePreview =
        () -> {
          playTestTone.setDisable(true);
          simulateShuffle.setDisable(true);
          simulatePitchSweep.setDisable(true);
          vm.previewShuffle(
              draft.get(),
              SHUFFLE_PREVIEW_SIMULATED_LENGTH,
              SHUFFLE_PREVIEW_DURATION_MS,
              enablePreviewButtons);
        };

    Runnable runPitchSweepPreview =
        () -> {
          playTestTone.setDisable(true);
          simulateShuffle.setDisable(true);
          simulatePitchSweep.setDisable(true);
          vm.previewPitchSweep(
              draft.get(),
              SHUFFLE_PREVIEW_SIMULATED_LENGTH,
              SHUFFLE_PREVIEW_DURATION_MS,
              enablePreviewButtons);
        };

    playTestTone.setOnAction(
        e -> {
          clearStatus(status);
          vm.previewTestTone(draft.get());
        });

    simulateShuffle.setOnAction(
        e -> {
          clearStatus(status);
          runShufflePreview.run();
        });

    simulatePitchSweep.setOnAction(
        e -> {
          clearStatus(status);
          runPitchSweepPreview.run();
        });

    // Autoplay: after any control settles for a moment, preview the selected sound so the user
    // can hear the effect of the change without pressing a preview button each time.
    PauseTransition autoplayDebounce = new PauseTransition(Duration.millis(250));
    autoplayDebounce.setOnFinished(
        e -> {
          String mode = autoplay.getValue();
          if (SettingsStrings.AUDIO_AUTOPLAY_TEST_TONE.equals(mode)) {
            vm.previewTestTone(draft.get());
          } else if (SettingsStrings.AUDIO_AUTOPLAY_SHUFFLE.equals(mode)
              && !simulateShuffle.isDisable()) {
            runShufflePreview.run();
          } else if (SettingsStrings.AUDIO_AUTOPLAY_PITCH_SWEEP.equals(mode)
              && !simulatePitchSweep.isDisable()) {
            runPitchSweepPreview.run();
          }
        });
    Runnable scheduleAutoplay =
        () -> {
          if (SettingsStrings.AUDIO_AUTOPLAY_OFF.equals(autoplay.getValue())) {
            return;
          }
          autoplayDebounce.stop();
          autoplayDebounce.playFromStart();
        };
    for (Slider s :
        new Slider[] {
          volume,
          velocity,
          pan,
          lowNote,
          highNote,
          reverb,
          chorus,
          attackTime,
          releaseTime,
          brightness
        }) {
      s.valueProperty().addListener((obs, old, value) -> scheduleAutoplay.run());
    }
    instrument
        .getSelectionModel()
        .selectedItemProperty()
        .addListener((obs, old, value) -> scheduleAutoplay.run());

    ScrollPane scroll = new ScrollPane(content);
    scroll.setFitToWidth(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroll.getStyleClass().add("edge-to-edge");

    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.initOwner(owner);
    dialog.setTitle(SettingsStrings.AUDIO_SETTINGS_TITLE);
    dialog.getDialogPane().setId(DIALOG_ID);
    dialog.getDialogPane().getStyleClass().add("customize-dialog");
    dialog.getDialogPane().setContent(scroll);
    dialog.getDialogPane().setPrefSize(600, 640);
    dialog.getDialogPane().setMinSize(480, 360);
    dialog.setResizable(true);

    ButtonType closeType = new ButtonType(SettingsStrings.CLOSE, ButtonBar.ButtonData.CANCEL_CLOSE);
    ButtonType resetType =
        new ButtonType(SettingsStrings.AUDIO_RESET_ALL, ButtonBar.ButtonData.LEFT);
    ButtonType applyType = new ButtonType(SettingsStrings.APPLY, ButtonBar.ButtonData.APPLY);
    dialog.getDialogPane().getButtonTypes().addAll(closeType, resetType, applyType);

    MenuItem exportItem = new MenuItem(SettingsStrings.EXPORT);
    MenuItem importItem = new MenuItem(SettingsStrings.IMPORT);
    MenuButton exportImportMenu =
        new MenuButton(SettingsStrings.EXPORT_IMPORT_MENU, null, exportItem, importItem);
    exportImportMenu.getStyleClass().add(Styles.BUTTON_OUTLINED);
    ButtonBar.setButtonData(exportImportMenu, ButtonBar.ButtonData.LEFT);

    exportItem.setOnAction(
        e -> {
          String json = AudioSettingsCodec.encodeEnvelope(draft.get());
          ClipboardContent clip = new ClipboardContent();
          clip.putString(json);
          Clipboard.getSystemClipboard().setContent(clip);
          showStatus(status, SettingsStrings.AUDIO_EXPORT_COPIED, false);
        });

    importItem.setOnAction(
        e -> {
          Optional<AudioSettingsCodec.DecodeResult> imported =
              showImportDialog(dialog.getDialogPane().getScene().getWindow());
          if (imported.isEmpty()) {
            return;
          }
          loadIntoControls(
              imported.get().settings(),
              instrument,
              volume,
              velocity,
              pan,
              lowNote,
              highNote,
              reverb,
              chorus,
              attackTime,
              releaseTime,
              brightness);
          if (imported.get().valuesWereClamped()) {
            showStatus(status, SettingsStrings.AUDIO_IMPORT_CLAMPED, true);
          } else {
            showStatus(status, SettingsStrings.AUDIO_IMPORT_SUCCESS, false);
          }
        });

    dialog.setOnShown(
        e -> {
          Window window = dialog.getDialogPane().getScene().getWindow();
          if (window instanceof Stage stage) {
            stage.setResizable(true);
            stage.setMinWidth(480);
            stage.setMinHeight(400);
          }
          if (window != null) {
            window.setOnCloseRequest(
                closeEvent -> {
                  if (!confirmCloseIfDirty(vm, draft, appliedBaseline, dialog)) {
                    closeEvent.consume();
                  }
                });
          }
          Node barNode = dialog.getDialogPane().lookup(".button-bar");
          if (barNode instanceof ButtonBar bar) {
            bar.setButtonOrder("L+CA");
            if (!bar.getButtons().contains(exportImportMenu)) {
              bar.getButtons().add(exportImportMenu);
            }
          }
        });

    Button applyButton = (Button) dialog.getDialogPane().lookupButton(applyType);
    applyButton.getStyleClass().add(Styles.ACCENT);
    applyButton.addEventFilter(
        javafx.event.ActionEvent.ACTION,
        e -> {
          e.consume();
          vm.applySettings(draft.get());
          appliedBaseline.set(draft.get());
          showStatus(status, SettingsStrings.AUDIO_APPLY_SUCCESS, false);
        });

    Button resetButton = (Button) dialog.getDialogPane().lookupButton(resetType);
    resetButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
    resetButton.addEventFilter(
        javafx.event.ActionEvent.ACTION,
        e -> {
          e.consume();
          loadIntoControls(
              AudioSettings.defaults(),
              instrument,
              volume,
              velocity,
              pan,
              lowNote,
              highNote,
              reverb,
              chorus,
              attackTime,
              releaseTime,
              brightness);
          clearStatus(status);
        });

    Button closeButton = (Button) dialog.getDialogPane().lookupButton(closeType);
    closeButton.addEventFilter(
        javafx.event.ActionEvent.ACTION,
        e -> {
          if (!confirmCloseIfDirty(vm, draft, appliedBaseline, dialog)) {
            e.consume();
          }
        });

    var css = SettingsStylesheets.cssUrl();
    if (css != null) {
      dialog.getDialogPane().getStylesheets().add(css.toExternalForm());
    }

    dialog.setOnHidden(
        e -> {
          autoplayDebounce.stop();
          vm.stopPreview();
        });

    dialog.showAndWait();
  }

  /**
   * @return {@code true} if the dialog may close (clean, saved, or explicitly discarded)
   */
  private static boolean confirmCloseIfDirty(
      AudioSettingsViewModel vm,
      Supplier<AudioSettings> draft,
      AtomicReference<AudioSettings> appliedBaseline,
      Dialog<?> dialog) {
    if (appliedBaseline.get().equals(draft.get())) {
      return true;
    }
    UnsavedChangesDialog.Choice choice =
        UnsavedChangesDialog.ask(
            dialog.getDialogPane().getScene().getWindow(),
            UNSAVED_CONFIRM_ID,
            SettingsStrings.CUSTOMIZE_UNSAVED_MESSAGE);
    return switch (choice) {
      case SAVE -> {
        AudioSettings toApply = draft.get();
        vm.applySettings(toApply);
        appliedBaseline.set(toApply);
        yield true;
      }
      case DISCARD -> true;
      case CANCEL -> false;
    };
  }

  private static Slider midiSlider(int initial) {
    return new Slider(AudioSettings.MIDI_MIN, AudioSettings.MIDI_MAX, initial);
  }

  private static void stepCombo(ComboBox<Integer> combo, int delta) {
    int size = combo.getItems().size();
    if (size == 0) {
      return;
    }
    int current = combo.getSelectionModel().getSelectedIndex();
    if (current < 0) {
      current = 0;
    }
    int next = Math.max(0, Math.min(size - 1, current + delta));
    combo.getSelectionModel().select(next);
  }

  private static void loadIntoControls(
      AudioSettings s,
      ComboBox<Integer> instrument,
      Slider volume,
      Slider velocity,
      Slider pan,
      Slider lowNote,
      Slider highNote,
      Slider reverb,
      Slider chorus,
      Slider attackTime,
      Slider releaseTime,
      Slider brightness) {
    instrument.getSelectionModel().select(Integer.valueOf(s.instrumentProgram()));
    volume.setValue(s.volume());
    velocity.setValue(s.velocity());
    pan.setValue(s.pan());
    lowNote.setValue(s.lowNote());
    highNote.setValue(s.highNote());
    reverb.setValue(s.reverb());
    chorus.setValue(s.chorus());
    attackTime.setValue(s.attackTime());
    releaseTime.setValue(s.releaseTime());
    brightness.setValue(s.brightness());
  }

  private static String noteLabel(int note) {
    String[] names = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    int octave = note / 12 - 1;
    return names[note % 12] + octave + " (" + note + ")";
  }

  private static String panLabel(int pan) {
    if (pan == AudioSettings.DEFAULT_PAN) {
      return SettingsStrings.AUDIO_PAN_CENTER;
    }
    int center = AudioSettings.DEFAULT_PAN;
    if (pan < center) {
      return String.format(SettingsStrings.AUDIO_PAN_LEFT_FORMAT, center - pan);
    }
    return String.format(SettingsStrings.AUDIO_PAN_RIGHT_FORMAT, pan - center);
  }

  /** Paste dialog for importing an exported audio config. */
  private static Optional<AudioSettingsCodec.DecodeResult> showImportDialog(Window owner) {
    AtomicReference<AudioSettingsCodec.DecodeResult> accepted = new AtomicReference<>();

    TextArea text = new TextArea();
    text.setId(IMPORT_TEXT_ID);
    text.getStyleClass().add("customize-import-text");
    text.setPromptText(SettingsStrings.AUDIO_IMPORT_PLACEHOLDER);
    text.setWrapText(true);
    text.setPrefRowCount(8);
    text.setTextFormatter(
        new TextFormatter<>(
            change -> change.getControlNewText().length() <= 64_000 ? change : null));
    VBox.setVgrow(text, Priority.ALWAYS);

    String clip = Clipboard.getSystemClipboard().getString();
    if (clip != null && !clip.isBlank()) {
      text.setText(clip.strip());
      text.selectAll();
    }

    Label hint = new Label(SettingsStrings.AUDIO_IMPORT_HINT);
    hint.getStyleClass().add("settings-muted");
    hint.setWrapText(true);

    Label error = new Label();
    error.getStyleClass().addAll("settings-inline-status", "settings-inline-error");
    error.setWrapText(true);
    error.setVisible(false);
    error.setManaged(false);

    Button paste = new Button(SettingsStrings.AUDIO_IMPORT_PASTE);
    paste.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.SMALL);
    paste.setOnAction(
        e -> {
          String next = Clipboard.getSystemClipboard().getString();
          if (next == null || next.isBlank()) {
            showStatus(error, SettingsStrings.AUDIO_IMPORT_EMPTY, true);
            return;
          }
          text.setText(next.strip());
          text.requestFocus();
          text.selectAll();
          clearStatus(error);
        });

    VBox body = new VBox(SettingsLayout.GAP_SM, hint, text, paste, error);
    body.setPadding(new Insets(SettingsLayout.GAP_SM));
    body.setFillWidth(true);

    Dialog<ButtonType> importDialog = new Dialog<>();
    importDialog.initOwner(owner);
    importDialog.setTitle(SettingsStrings.AUDIO_IMPORT_TITLE);
    importDialog.getDialogPane().setId(IMPORT_DIALOG_ID);
    importDialog.getDialogPane().getStyleClass().add("customize-import-dialog");
    importDialog.getDialogPane().setContent(body);
    importDialog.getDialogPane().setPrefWidth(480);
    importDialog.getDialogPane().setPrefHeight(320);

    ButtonType loadType =
        new ButtonType(SettingsStrings.AUDIO_IMPORT_LOAD, ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelType =
        new ButtonType(SettingsStrings.CANCEL, ButtonBar.ButtonData.CANCEL_CLOSE);
    importDialog.getDialogPane().getButtonTypes().addAll(cancelType, loadType);

    Button loadButton = (Button) importDialog.getDialogPane().lookupButton(loadType);
    loadButton.getStyleClass().add(Styles.ACCENT);
    loadButton.addEventFilter(
        javafx.event.ActionEvent.ACTION,
        e -> {
          String raw = text.getText() == null ? "" : text.getText().strip();
          if (raw.isEmpty()) {
            e.consume();
            showStatus(error, SettingsStrings.AUDIO_IMPORT_EMPTY, true);
            return;
          }
          Optional<AudioSettingsCodec.DecodeResult> decoded =
              AudioSettingsCodec.decodeEnvelope(raw);
          if (decoded.isEmpty()) {
            e.consume();
            showStatus(error, SettingsStrings.AUDIO_IMPORT_INVALID, true);
            return;
          }
          accepted.set(decoded.get());
        });

    var css = SettingsStylesheets.cssUrl();
    if (css != null) {
      importDialog.getDialogPane().getStylesheets().add(css.toExternalForm());
    }

    importDialog.setOnShown(e -> text.requestFocus());
    importDialog.showAndWait();
    return Optional.ofNullable(accepted.get());
  }

  private static void showStatus(Label status, String message, boolean error) {
    status.setText(message);
    status.getStyleClass().removeAll("settings-inline-error", "settings-inline-success");
    status.getStyleClass().add(error ? "settings-inline-error" : "settings-inline-success");
    status.setVisible(true);
    status.setManaged(true);
  }

  private static void clearStatus(Label status) {
    status.setText("");
    status.getStyleClass().removeAll("settings-inline-error", "settings-inline-success");
    status.setVisible(false);
    status.setManaged(false);
  }
}
