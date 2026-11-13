package gg.raynormods.holograms;

import java.util.Locale;

enum TextAlignment {
    LEFT,
    CENTER,
    RIGHT;

    static TextAlignment parse(String value) {
        if (value == null) return CENTER;
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return CENTER;
        }
    }
}
