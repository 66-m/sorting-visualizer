package io.github.compilerstuck.visual;

import io.github.compilerstuck.control.model.ArrayModel;
import io.github.compilerstuck.control.render.RenderContext;
import processing.core.PApplet;
import io.github.compilerstuck.sound.Sound;
import io.github.compilerstuck.visual.gradient.ColorGradient;

import java.awt.*;

import static java.lang.Math.floor;
import static java.lang.Math.min;

public class Cube extends Visualization {

    int radius;
    static float aa = 0;

    private int[] colorsRgb;
    private float[] xCords, yCords, zCords;
    private float[] sizes;
    private int bufferCapacity;

    public Cube(ArrayModel arrayController, ColorGradient colorGradient, Sound sound, RenderContext proc) {
        super(arrayController, colorGradient, sound, proc);
        name = "3D - Cube";

    }

    private void ensureBuffers(int n) {
        if (colorsRgb != null && bufferCapacity >= n) return;
        bufferCapacity = n;
        colorsRgb = new int[n];
        xCords = new float[n];
        yCords = new float[n];
        zCords = new float[n];
        sizes = new float[n];
    }

    @Override
    public void update() {
        super.update();

        proc.lights();
        
        radius = (int) (min(screenHeight, screenWidth) / 3.5);

        aa -= PApplet.PI / (10 * proc.frameRate());

        int xSize = (int) (floor(Math.pow(arrayController.getLength(), 1 / 3f) + 0.1));
        if (xSize < 1) {
            xSize = 1;
        }
        int drawCount = Math.min(arrayController.getLength(), xSize * xSize * xSize);
        int xCnt = 0;
        int yCnt = 0;
        int zCnt = 0;

        ensureBuffers(drawCount);

        for (int i = 0; i < drawCount; i++) {

            Color color = colorGradient.getMarkerColor(arrayController.get(i), arrayController.getMarker(i));

            if (arrayController.getMarker(arrayController.get(i)) == Marker.SET) {
                sound.playSound(arrayController.get(i));
            }

            arrayController.setMarker(arrayController.get(i), Marker.NORMAL);

            float barHeight = ((arrayController.getLength() / arrayController.getLength() * (arrayController.getLength() - 2f * Math.min(Math.min(Math.abs(i - arrayController.get(i)), Math.abs(i - arrayController.getLength() - arrayController.get(i))), Math.abs(i + arrayController.getLength() - arrayController.get(i))))));

            float xa = PApplet.map(xCnt, 0, xSize, -radius, radius);
            float ya = PApplet.map(yCnt, 0, xSize, -radius, radius);
            float za = PApplet.map(zCnt, 0, xSize, -radius, radius);

            float zb = (float) (Math.sin(aa) * xa + Math.cos(aa) * za);
            float x = (float) ((float) Math.cos(aa) * xa - Math.sin(aa) * za);

            float z = (float) (Math.sin(-10) * ya + Math.cos(-10) * zb);
            float y = (float) (Math.cos(-10) * ya - Math.sin(-10) * zb);

            float size = PApplet.map(barHeight, 0, arrayController.getLength(), 0, radius*2/xSize);

            zCords[i] = z;
            colorsRgb[i] = color.getRGB();
            xCords[i] = x;
            yCords[i] = y;
            sizes[i] = size;

            zCnt++;
            if (zCnt == xSize) {
                if (xCnt == xSize - 1) {
                    yCnt += 1;
                    zCnt = 0;
                    xCnt = 0;
                } else {
                    xCnt += 1;
                    zCnt = 0;
                }
            }
        }

        for (int i = 0; i < drawCount; i++) {
            proc.stroke(colorsRgb[i], 255f);
            //proc.noStroke();
            proc.fill(colorsRgb[i], 120f);


            proc.pushMatrix();
            //set screen center
            proc.translate((float) screenWidth / 2, (float) screenHeight / 2 -(int) (min(screenHeight, screenWidth) / 10), -(int) (min(screenHeight, screenWidth) / 10));
            //set circle position
            proc.translate(xCords[i], yCords[i], zCords[i]);

            //proc.ellipse(0, 0, sizes.get(i), sizes.get(i));
            proc.rotateX(45);
            proc.rotateY(0);
            proc.rotateZ(-aa);
            proc.box(sizes[i], sizes[i], sizes[i]);
            proc.popMatrix();
        }
    }

}
