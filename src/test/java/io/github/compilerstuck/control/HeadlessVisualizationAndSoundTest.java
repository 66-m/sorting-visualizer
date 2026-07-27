package io.github.compilerstuck.control;

import static org.junit.jupiter.api.Assertions.*;

import io.github.compilerstuck.control.model.ArrayController;
import io.github.compilerstuck.control.render.FakeRenderSystem;
import io.github.compilerstuck.sound.HeadlessSound;
import io.github.compilerstuck.visual.Bars;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HeadlessVisualizationAndSoundTest {

  private ArrayController controller;
  private FakeRenderSystem rs;
  private HeadlessSound sound;

  @BeforeEach
  void setUp() {
    controller = new ArrayController(10);
    rs = new FakeRenderSystem(200, 100);
    sound = new HeadlessSound(controller);
  }

  @Test
  @DisplayName("Bars updates without throwing exceptions")
  void visualizationsUpdateHeadlessly() {
    ColorGradient gradient = new ColorGradient(Color.WHITE, Color.WHITE, Color.BLACK, "test");
    gradient.updateGradient(10);
    Bars bars = new Bars(controller, gradient, sound, rs);

    assertDoesNotThrow(() -> bars.render(1f / 60f));
  }

  @Test
  @DisplayName("HeadlessSound does nothing when playSound is called")
  void headlessSoundNoOp() {
    assertDoesNotThrow(() -> sound.playSound(5));
    assertDoesNotThrow(() -> sound.mute(true));
  }
}
