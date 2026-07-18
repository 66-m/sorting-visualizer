package io.github.compilerstuck.visual;

import static java.lang.Math.floor;
import static java.lang.Math.min;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;
import java.awt.*;
import processing.core.PApplet;

public class Sphere extends Visualization {

  int radius;
  float squareRoot;
  static float aa = 0;

  private int[] colorsRgb;
  private float[] xCords, yCords, zCords;
  private int bufferCapacity;

  public Sphere(
      ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
    super(arrayController, colorGradient, sound, proc);
    name = "3D - Sphere";
  }

  private void ensureBuffers(int n) {
    if (colorsRgb != null && bufferCapacity >= n) return;
    bufferCapacity = n;
    colorsRgb = new int[n];
    xCords = new float[n];
    yCords = new float[n];
    zCords = new float[n];
  }

  @Override
  public void update() {
    super.update();

    proc.lights();

    int nextN = (int) (floor(Math.pow(arrayController.getLength(), 1 / 2.) + 0.1));
    squareRoot = nextN;
    int drawCount = Math.min(arrayController.getLength(), nextN * nextN);

    aa -= PApplet.PI / (10 * proc.frameRate());

    float m = 0;
    float n = 0;

    ensureBuffers(drawCount);

    for (int i = 0; i < drawCount; i++) {

      if (arrayController.getMarker(arrayController.get(i)) == Marker.SET) {
        sound.playSound(arrayController.get(i));
      }

      arrayController.setMarker(arrayController.get(i), Marker.NORMAL);

      float barHeight =
          (((float) 100000
              / arrayController.getLength()
              * (arrayController.getLength()
                  - 2
                      * Math.min(
                          Math.min(
                              Math.abs(i - arrayController.get(i)),
                              Math.abs(i - arrayController.getLength() - arrayController.get(i))),
                          Math.abs(i + arrayController.getLength() - arrayController.get(i))))));

      radius =
          (int) PApplet.map(barHeight, 0, 100000, 0, (int) (min(screenHeight, screenWidth) / 2.3));

      float u = (float) ((m / squareRoot) * 2 * Math.PI);
      float v = (float) ((n / squareRoot) * Math.PI);

      float zSphere = (float) (Math.cos(u) * Math.sin(v));
      float xSphere = (float) (Math.sin(u) * Math.sin(v));
      float ySphere = (float) Math.cos(v);

      float zMapped = PApplet.map(zSphere, -1, 1, -radius, radius); // to front
      float yMapped = PApplet.map(ySphere, -1, 1, -radius, radius); // to side
      float xMapped = PApplet.map(xSphere, -1, 1, -radius, radius); // height

      // rotate z and x
      float zb = (float) (Math.sin(aa) * xMapped + Math.cos(aa) * zMapped);
      float x = (float) ((float) Math.cos(aa) * xMapped - Math.sin(aa) * zMapped);

      // change perspective
      float y = (float) (Math.cos(aa) * yMapped - Math.sin(aa) * zb);
      // float y = (float) (screenHeight * 0.5 - 20 + Math.cos(-10) * yMapped - Math.sin(-10) * zb);

      // calc sircle distance from viewpoint
      float z = (float) (Math.sin(aa) * yMapped + Math.cos(aa) * zb);
      // float z = (float) (Math.sin(-10) * yMapped + Math.cos(-10) * zb);

      // calc circle sizes
      // float size = PApplet.map(z, -radius, radius, 20, 35);

      // size = PApplet.map(barHeight, 0, arrayController.getLength(), 0, size);

      // float x = xMapped;
      // float y = yMapped;
      // float z = zb;

      // sort for size
      Color color =
          colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

      zCords[i] = z;
      colorsRgb[i] = color.getRGB();
      xCords[i] = x;
      yCords[i] = y;

      if (m == squareRoot - 1) {
        if (n == squareRoot - 1) {
          n = 0;
        } else {
          n++;
        }
        m = 0;
      } else {
        m++;
      }
    }

    for (int i = 0; i < drawCount; i++) {
      proc.noStroke();
      proc.fill(colorsRgb[i], (float) (255.));

      proc.pushMatrix();

      // set screen center
      proc.translate(
          (float) screenWidth / 2,
          (float) screenHeight / 2,
          -(int) (min(screenHeight, screenWidth) / 10));

      // set circle position
      proc.translate(xCords[i], yCords[i], zCords[i]);
      proc.circle(0, 0, 3);

      proc.popMatrix();
    }
  }
}
