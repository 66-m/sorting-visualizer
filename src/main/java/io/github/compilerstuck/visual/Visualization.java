package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.config.Brand;
import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;

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

  public Visualization(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
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
    proc.text(Brand.WATERMARK, screenWidth - 175, 20);
    proc.textSize(20);
  }
}
