package io.github.sorting_visualizer.Control;

import java.util.Locale;

public enum ShuffleType {
    RANDOM,
    REVERSE,
    ALMOST_SORTED,
    SORTED;

    @Override
    public String toString() {
        return super.toString().toLowerCase(Locale.ROOT).replace('_', ' ');
    }
}

