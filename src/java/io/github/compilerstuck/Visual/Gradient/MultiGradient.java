package io.github.compilerstuck.Visual.Gradient;

import java.awt.*;

public class MultiGradient extends ColorGradient {

    public MultiGradient(Color markerSetColor, String name) {
        super(Color.BLACK, Color.BLACK, markerSetColor, name);
    }

    @Override
    protected Color[] getColorGradient(int size) {

        Color[] colorGradient = new Color[size];

        for (int i = 0; i < size; i++) {
            colorGradient[i] = Color.getHSBColor(((float)i/(size-1)), 0.9f, 0.9f);
        }
        return colorGradient;
    }
}
