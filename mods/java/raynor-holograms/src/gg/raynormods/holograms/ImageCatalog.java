/*
 * Decompiled with CFR 0.152.
 */
package gg.raynormods.holograms;

import java.util.List;
import java.util.Locale;

public final class ImageCatalog {
    private static final List<Entry> ENTRIES = List.of(new Entry("orb", "Raynor_Hologram_Placeholder_Orb", "Square cyan orb with transparency"), new Entry("banner", "Raynor_Hologram_Placeholder_Banner", "Wide opaque navigation banner"), new Entry("rune", "Raynor_Hologram_Placeholder_Rune", "Tall amber rune with transparency"));

    private ImageCatalog() {
    }

    public static List<Entry> entries() {
        return ENTRIES;
    }

    public static Entry find(String string) {
        if (string == null) {
            return null;
        }
        String string2 = string.trim().toLowerCase(Locale.ROOT);
        for (Entry entry : ENTRIES) {
            if (!entry.key().equals(string2)) continue;
            return entry;
        }
        return null;
    }

    public record Entry(String key, String modelAssetId, String description) {
    }
}

