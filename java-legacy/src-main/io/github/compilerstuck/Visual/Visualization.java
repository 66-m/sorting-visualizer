package io.github.compilerstuck.Visual;

import io.github.compilerstuck.Control.model.ArrayModel;
import io.github.compilerstuck.Control.render.RenderContext;
import io.github.compilerstuck.Sound.Sound;
import io.github.compilerstuck.Visual.Gradient.ColorGradient;

public abstract class Visualization {

    protected ArrayModel arrayController;
    protected ColorGradient colorGradient;
    protected RenderContext proc;
    protected int screenWidth;
    protected int screenHeight;
    protected String name;
    protected Sound sound;

    public String getName() {
        return name;
    }

    public Visualization(ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
        this.arrayController = arrayController;
        this.colorGradient = colorGradient;
        this.proc = proc;
        this.sound = sound;
        screenHeight = proc.getHeight();
        screenWidth = proc.getWidth();
    }

    public void updateColorGradient(ColorGradient colorGradient) {
        this.colorGradient = colorGradient;
    }

    public void update() {
        screenHeight = proc.getHeight();
        screenWidth = proc.getWidth();

        proc.background(15);

        proc.fill(255);
        proc.textSize(25);
        proc.text("CompilerStuck", screenWidth - 175, 20); //Branding
        proc.textSize(20);
    }

}
