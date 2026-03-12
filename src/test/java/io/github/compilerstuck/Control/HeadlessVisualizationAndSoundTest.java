package io.github.compilerstuck.Control;

import io.github.compilerstuck.Sound.HeadlessSound;
import io.github.compilerstuck.Visual.Bars;
import io.github.compilerstuck.Visual.Circle;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;
import java.awt.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeadlessVisualizationAndSoundTest {

    private ArrayController controller;
    private HeadlessRenderContext renderCtx;
    private HeadlessSound sound;

    @BeforeEach
    void setUp() {
        MainController.setSize(10);
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
        sound.playSound(5);
        // no exception and nothing to verify - simply ensure method exists
        assertTrue(true);
    }
}
