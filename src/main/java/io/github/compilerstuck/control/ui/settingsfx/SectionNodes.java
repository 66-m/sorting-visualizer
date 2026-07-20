package io.github.compilerstuck.control.ui.settingsfx;

import javafx.scene.Node;

/** Prebuilt section content nodes for the one-pager shell. */
public record SectionNodes(
    Node arraySize, Node sorting, Node speed, Node visualization, Node appearance, Node options) {}
