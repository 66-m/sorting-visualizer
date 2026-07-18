package io.github.compilerstuck.control.config;

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

