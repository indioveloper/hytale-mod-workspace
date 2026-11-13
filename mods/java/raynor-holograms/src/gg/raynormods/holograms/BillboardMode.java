package gg.raynormods.holograms;

import java.util.Locale;

enum BillboardMode {
    FIXED,
    BILLBOARD_Y,
    BILLBOARD;

    static BillboardMode parse(String value) {
        if (value == null) return FIXED;
        String normalized = value.trim().replace('-', '_').toUpperCase(Locale.ROOT);
        if (normalized.equals("BILLBOARDY")) normalized = "BILLBOARD_Y";
        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException ignored) {
            return FIXED;
        }
    }
}
