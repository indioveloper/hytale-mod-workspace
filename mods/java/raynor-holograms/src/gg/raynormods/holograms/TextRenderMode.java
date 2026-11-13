package gg.raynormods.holograms;

import java.util.Locale;

/** Selects between Hytale's client-rendered nameplate and the rich model renderer. */
enum TextRenderMode {
    AUTO,
    LIVE,
    RICH;

    static TextRenderMode parse(String value) {
        if (value == null || value.isBlank()) return AUTO;
        try {
            String normalized = value.trim().toUpperCase(Locale.ROOT);
            if (normalized.equals("ATLAS")) return RICH;
            return valueOf(normalized);
        } catch (IllegalArgumentException ignored) {
            return AUTO;
        }
    }
}
