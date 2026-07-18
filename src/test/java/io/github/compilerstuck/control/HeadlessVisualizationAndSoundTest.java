package io.github.compilerstuck.control;

import static org.junit.jupiter.api.Assertions.*;

import io.github.compilerstuck.control.model.ArrayController;
import io.github.compilerstuck.control.render.HeadlessRenderContext;
import io.github.compilerstuck.sound.HeadlessSound;
import io.github.compilerstuck.visual.Bars;
import io.github.compilerstuck.visual.Circle;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HeadlessVisualizationAndSoundTest {

  private ArrayController controller;
  private HeadlessRenderContext renderCtx;
  private HeadlessSound sound;

  @BeforeEach
  void setUp() {
    controller = new ArrayController(10);
    renderCtx = new HeadlessRenderContext(200, 100);
    sound = new HeadlessSound(controller);
  }

  @Test
  @DisplayName("Visualization instances update without throwing exceptions")
  void visualizationsUpdateHeadlessly() {
    ColorGradient gradient = new ColorGradient(Color.WHITE, Color.WHITE, Color.BLACK, "test");
    gradient.updateGradient(10); // Initialize gradient colors to match array size
    Bars bars = new Bars(controller, gradient, sound, renderCtx);
    Circle circle = new Circle(controller, gradient, sound, renderCtx);

    assertDoesNotThrow(bars::update);
    assertDoesNotThrow(circle::update);
  }

  @Test
  @DisplayName("HeadlessSound does nothing when playSound is called")
  void headlessSoundNoOp() {
    assertDoesNotThrow(() -> sound.playSound(5));
    assertDoesNotThrow(() -> sound.mute(true));
  }
}
