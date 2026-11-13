package gg.raynormods.holograms;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class RichTextParser {
    private static final Map<String, Integer> NAMED_COLORS = Map.ofEntries(
        Map.entry("black", 0x111111), Map.entry("negro", 0x111111),
        Map.entry("white", 0xFFFFFF), Map.entry("blanco", 0xFFFFFF),
        Map.entry("gray", 0xAAAAAA), Map.entry("grey", 0xAAAAAA), Map.entry("gris", 0xAAAAAA),
        Map.entry("red", 0xFF5555), Map.entry("rojo", 0xFF5555),
        Map.entry("orange", 0xFFAA00), Map.entry("naranja", 0xFFAA00),
        Map.entry("yellow", 0xFFFF55), Map.entry("amarillo", 0xFFFF55),
        Map.entry("green", 0x55FF55), Map.entry("verde", 0x55FF55),
        Map.entry("cyan", 0x55FFFF), Map.entry("cian", 0x55FFFF),
        Map.entry("blue", 0x5555FF), Map.entry("azul", 0x5555FF),
        Map.entry("purple", 0xAA00AA), Map.entry("morado", 0xAA00AA), Map.entry("violeta", 0xAA00AA),
        Map.entry("pink", 0xFF55FF), Map.entry("rosa", 0xFF55FF)
    );
    private static final Map<Character, Integer> LEGACY_COLORS = Map.ofEntries(
        Map.entry('0', 0x000000), Map.entry('1', 0x0000AA), Map.entry('2', 0x00AA00),
        Map.entry('3', 0x00AAAA), Map.entry('4', 0xAA0000), Map.entry('5', 0xAA00AA),
        Map.entry('6', 0xFFAA00), Map.entry('7', 0xAAAAAA), Map.entry('8', 0x555555),
        Map.entry('9', 0x5555FF), Map.entry('a', 0x55FF55), Map.entry('b', 0x55FFFF),
        Map.entry('c', 0xFF5555), Map.entry('d', 0xFF55FF), Map.entry('e', 0xFFFF55),
        Map.entry('f', 0xFFFFFF)
    );

    private RichTextParser() {}

    static String plainText(Parsed parsed) {
        StringBuilder result = new StringBuilder(parsed.visibleCharacters() + Math.max(0, parsed.lines().size() - 1));
        for (int lineIndex = 0; lineIndex < parsed.lines().size(); lineIndex++) {
            if (lineIndex > 0) result.append('\n');
            for (Glyph glyph : parsed.lines().get(lineIndex)) result.append(glyph.text());
        }
        return result.toString();
    }

    static boolean hasRichFormatting(Parsed parsed) {
        for (List<Glyph> line : parsed.lines()) {
            for (Glyph glyph : line) {
                if (!glyph.style().equals(Style.DEFAULT)) return true;
            }
        }
        return false;
    }

    static Parsed parse(String source) {
        String input = (source == null ? "" : source).replace("\r\n", "\n").replace('\r', '\n');
        List<List<Glyph>> lines = new ArrayList<>();
        List<Glyph> line = new ArrayList<>();
        lines.add(line);
        Style style = Style.DEFAULT;
        int visible = 0;

        for (int index = 0; index < input.length();) {
            if (input.startsWith("\\n", index)) {
                line = new ArrayList<>();
                lines.add(line);
                index += 2;
                continue;
            }
            char character = input.charAt(index);
            if (character == '\n') {
                line = new ArrayList<>();
                lines.add(line);
                index++;
                continue;
            }
            if (character == '<') {
                int close = input.indexOf('>', index + 1);
                if (close > index) {
                    String token = input.substring(index + 1, close).trim();
                    if (token.equalsIgnoreCase("br")) {
                        line = new ArrayList<>();
                        lines.add(line);
                        index = close + 1;
                        continue;
                    }
                    Style parsed = parseTag(token, style);
                    if (parsed != null) {
                        style = parsed;
                        index = close + 1;
                        continue;
                    }
                }
            }
            if (character == '&' && index + 1 < input.length()) {
                if (input.charAt(index + 1) == '#' && index + 8 <= input.length()) {
                    Integer rgb = parseHex(input.substring(index + 2, index + 8));
                    if (rgb != null) {
                        style = new Style(rgb, rgb, style.effect());
                        index += 8;
                        continue;
                    }
                }
                char code = Character.toLowerCase(input.charAt(index + 1));
                Integer rgb = LEGACY_COLORS.get(code);
                if (rgb != null) {
                    style = new Style(rgb, rgb, Effect.NORMAL);
                    index += 2;
                    continue;
                }
                if (code == 'l') {
                    style = new Style(style.rgb(), style.secondaryRgb(), Effect.BOLD);
                    index += 2;
                    continue;
                }
                if (code == 'r') {
                    style = Style.DEFAULT;
                    index += 2;
                    continue;
                }
            }

            int codePoint = input.codePointAt(index);
            String glyph = new String(Character.toChars(codePoint));
            line.add(new Glyph(glyph, style));
            visible++;
            index += Character.charCount(codePoint);
        }
        return new Parsed(lines.stream().map(List::copyOf).toList(), visible);
    }

    private static Style parseTag(String raw, Style current) {
        if (raw.equals("/") || raw.equalsIgnoreCase("reset")) return Style.DEFAULT;
        String[] parts = raw.toLowerCase(Locale.ROOT).split(":", -1);
        ColorPair colors = colorPair(parts[0].trim());
        Effect effect = parts.length > 1 ? Effect.parse(parts[1]) : current.effect();
        if (colors == null) {
            Effect onlyEffect = Effect.parse(parts[0]);
            if (onlyEffect == null) return null;
            return new Style(current.rgb(), current.secondaryRgb(), onlyEffect);
        }
        return effect == null ? null : new Style(colors.first(), colors.second(), effect);
    }

    private static ColorPair colorPair(String value) {
        int separator = value.indexOf('~');
        if (separator < 0) {
            Integer single = color(value);
            return single == null ? null : new ColorPair(single, single);
        }
        Integer first = color(value.substring(0, separator).trim());
        Integer second = color(value.substring(separator + 1).trim());
        return first == null || second == null ? null : new ColorPair(first, second);
    }

    private static Integer color(String value) {
        if (value.startsWith("#")) return parseHex(value.substring(1));
        return NAMED_COLORS.get(value);
    }

    private static Integer parseHex(String value) {
        if (value.length() != 6) return null;
        try {
            return Integer.parseInt(value, 16);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    record Style(int rgb, int secondaryRgb, Effect effect) {
        static final Style DEFAULT = new Style(0xFFFFFF, 0xFFFFFF, Effect.NORMAL);
        boolean hasGradient() { return rgb != secondaryRgb; }
    }

    private record ColorPair(int first, int second) {}
    record Glyph(String text, Style style) {}
    record Parsed(List<List<Glyph>> lines, int visibleCharacters) {}

    enum Effect {
        NORMAL, GLOW, HIGHLIGHT, BOLD, SHADOW, OUTLINE;

        static Effect parse(String value) {
            return switch (value.trim().toLowerCase(Locale.ROOT)) {
                case "normal" -> NORMAL;
                case "glow", "brillo" -> GLOW;
                case "highlight", "resaltado" -> HIGHLIGHT;
                case "bold", "negrita" -> BOLD;
                case "shadow", "sombra" -> SHADOW;
                case "outline", "stroke", "contorno", "borde" -> OUTLINE;
                case "background", "fondo" -> HIGHLIGHT;
                default -> null;
            };
        }
    }
}
