package io.github.sorting_visualizer.Visual;

import io.github.sorting_visualizer.Control.ArrayController;
import io.github.sorting_visualizer.Control.MainController;
import io.github.sorting_visualizer.Visual.Gradient.ColorGradient;
import io.github.sorting_visualizer.Sound.Sound;
import processing.core.PApplet;

public abstract class Visualization {

    protected ArrayController arrayController;
    protected ColorGradient colorGradient;
    protected PApplet proc;
    protected int screenWidth;
    protected int screenHeight;
    protected String name;
    protected Sound sound;

    public String getName() {
        return name;
    }

    public Visualization(ArrayController arrayController, ColorGradient colorGradient, Sound sound) {
        this.arrayController = arrayController;
        this.colorGradient = colorGradient;
        proc = MainController.processing;
        this.sound = sound;
        screenHeight = proc.height;
        screenWidth = proc.width;
    }

    public void updateColorGradient(ColorGradient colorGradient){
        this.colorGradient = colorGradient;
    }

    public void update(){
        screenHeight = proc.height;
        screenWidth = proc.width;

        proc.background(15);

        proc.fill(255);
        proc.textSize(25);
        proc.text("CompilerStuck", screenWidth-175, 20); //Branding
        proc.textSize(20);
    }

}
