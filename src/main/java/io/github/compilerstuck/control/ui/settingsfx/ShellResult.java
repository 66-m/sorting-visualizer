package io.github.compilerstuck.control.ui.settingsfx;

import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;

/** Built Settings shell root plus action-bar controls for wiring. */
public record ShellResult(BorderPane root, ProgressBar progress, Button run, Button cancel) {}
