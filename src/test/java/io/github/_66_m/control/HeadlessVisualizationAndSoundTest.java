package io.github._66_m.control;

import static org.junit.jupiter.api.Assertions.*;

import io.github._66_m.control.model.ArrayController;
import io.github._66_m.control.render.FakeRenderSystem;
import io.github._66_m.sound.HeadlessSound;
import io.github._66_m.visual.Bars;
import io.github._66_m.visual.gradient.ColorGradient;
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
