package gg.raynormods.holograms;

import com.hypixel.hytale.server.core.asset.common.CommonAsset;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;

import java.nio.charset.StandardCharsets;
import java.util.List;

/** Keeps the text API stable while rendering one high-resolution texture per styled phrase. */
final class TextRasterRenderer {
    static final int MAX_SOURCE_LENGTH = 4096;
    static final int MAX_VISIBLE_GLYPHS = 1024;
    static final int MAX_LINES = 32;
    static final int MAX_COLUMNS = 160;

    private TextRasterRenderer() {}

    static RenderedText create(String resolvedText, float size, TextAlignment alignment) {
        return create(resolvedText, size, alignment, 1.0f);
    }

    static RenderedText create(String resolvedText, float size, TextAlignment alignment, float opacity) {
        RichTextParser.Parsed parsed = RichTextParser.parse(resolvedText);
        String error = validationError(parsed, resolvedText);
        if (error != null) return new RenderedText(null, "", error);
        int opacityStep = Math.max(0, Math.min(10, Math.round(opacity * 10.0f)));
        Model model = TextCompositeRenderer.create(parsed, resolvedText, size, alignment, opacityStep);
        if (model == null) return new RenderedText(null, "", "No se pudo registrar el modelo de texto.");
        String signature = CommonAsset.hash((alignment + "\n" + opacityStep + "\n" + resolvedText)
            .getBytes(StandardCharsets.UTF_8));
        return new RenderedText(model, signature, "");
    }

    static String validationError(RichTextParser.Parsed parsed, String source) {
        if (source != null && source.length() > MAX_SOURCE_LENGTH) return "El texto y sus etiquetas no pueden superar 4096 caracteres.";
        if (parsed.visibleCharacters() > MAX_VISIBLE_GLYPHS) return "El holograma no puede superar 1024 caracteres visibles.";
        if (parsed.lines().size() > MAX_LINES) return "El holograma no puede superar 32 lineas.";
        for (List<RichTextParser.Glyph> line : parsed.lines()) {
            if (line.size() > MAX_COLUMNS) return "Cada linea puede tener un maximo de 160 caracteres.";
        }
        return null;
    }

    static void clearCache() { TextCompositeRenderer.clearCache(); }

    record RenderedText(Model model, String signature, String error) {
        boolean success() { return model != null; }
    }
}
