package io.github.sorting_visualizer.Visual.Gradient;

import io.github.sorting_visualizer.Control.MainController;
import io.github.sorting_visualizer.Visual.Marker;

import java.awt.*;

public class ColorGradient {

    private Color color1;
    private Color color2;
    private final Color markerSetColor;
    private final String name;
    protected Color[] colorGradient;

    public ColorGradient(Color color1, Color color2, Color markerSetColor, String name) {
        this.color1 = color1;
        this.color2 = color2;
        this.markerSetColor = markerSetColor;
        this.name = name;
        colorGradient = getColorGradient(MainController.getSize());
    }

    protected Color[] getColorGradient(int size) {
        Color[] colorGradient = new Color[size];
        for (int i = 0; i < size; i++) {
            double scalingFactor;

            scalingFactor = (double) i / (size);
            int r = (int) (color1.getRed() + (color2.getRed() - color1.getRed()) * scalingFactor);
            int g = (int) (color1.getGreen() + (color2.getGreen() - color1.getGreen()) * scalingFactor);
            int b = (int) (color1.getBlue() + (color2.getBlue() - color1.getBlue()) * scalingFactor);

            colorGradient[i] = new Color(r, g, b);
        }

        return colorGradient;
    }

    public void updateGradient(int size) {
        colorGradient = getColorGradient(size);
    }

    public Color getMarkerColor(int index, Marker m) {
        if (m == Marker.NORMAL) return colorGradient[index];
        else if (m == Marker.SET) return Color.WHITE;
        else return Color.BLACK;
    }


    public void setColor1(Color color1) {
        this.color1 = color1;
    }

    public Color getColor1() {
        return color1;
    }

    public void setColor2(Color color2) {
        this.color2 = color2;
    }

    public Color getColor2() {
        return color2;
    }

    public String getName() {
        return name;
    }
}
