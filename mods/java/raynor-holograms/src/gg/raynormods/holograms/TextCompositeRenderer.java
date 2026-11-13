package gg.raynormods.holograms;

import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.server.core.asset.common.CommonAsset;
import com.hypixel.hytale.server.core.asset.common.CommonAssetModule;
import com.hypixel.hytale.server.core.asset.common.CommonAssetRegistry;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import org.joml.Vector3d;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Renders one high-resolution texture and one quad per rich text hologram. */
final class TextCompositeRenderer {
    private static final String RUNTIME_PACK = "RaynorMods:RaynorHologramsRuntime";
    private static final String GENERATED_ROOT = "NPC/RaynorHolograms/Generated/";
    private static final String BASE_MODEL = "Raynor_Hologram_Text_Selection_Proxy";
    private static final int MAX_TEXTURE_SIDE = 2048;
    private static final double MODEL_PIXELS_PER_BLOCK = 512.0;
    private static final int MAX_MODEL_CACHE = 128;
    private static final Map<RenderKey, Model> MODEL_CACHE = new LinkedHashMap<>(32, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<RenderKey, Model> eldest) {
            return size() > MAX_MODEL_CACHE;
        }
    };

    private TextCompositeRenderer() {}

    static BufferedImage renderPreview(RichTextParser.Parsed parsed, TextAlignment alignment, float opacity) {
        return render(parsed, alignment, opacity).image();
    }

    static Model create(RichTextParser.Parsed parsed, String text, float size,
                        TextAlignment alignment, int opacityStep) {
        RenderKey cacheKey = new RenderKey(text, Float.floatToIntBits(size), alignment, opacityStep);
        synchronized (MODEL_CACHE) {
            Model cached = MODEL_CACHE.get(cacheKey);
            if (cached != null) return cached;
            Model created = createUncached(parsed, size, alignment, opacityStep);
            if (created != null) MODEL_CACHE.put(cacheKey, created);
            return created;
        }
    }

    /** Serializing cache misses also prevents duplicate runtime asset registration. */
    private static Model createUncached(RichTextParser.Parsed parsed, float size,
                                        TextAlignment alignment, int opacityStep) {
        ModelAsset base = ModelAsset.getAssetMap().getAsset(BASE_MODEL);
        CommonAssetModule module = CommonAssetModule.get();
        if (base == null || module == null) return null;
        try {
            RenderedCanvas rendered = render(parsed, alignment, opacityStep / 10.0f);
            BufferedImage edgeSafe = bleedTransparentRgb(rendered.image(), 4);
            LimitedImage limited = limit(edgeSafe);
            BufferedImage texture = RuntimeAssetFactory.entityTexture(limited.image());
            byte[] textureBytes = png(texture);
            String hash = CommonAsset.hash(textureBytes).substring(0, 24);
            String texturePath = GENERATED_ROOT + "Text_" + hash + ".png";
            String modelPath = GENERATED_ROOT + "Text_" + hash + ".blockymodel";

            boolean textureChanged = register(module, new GeneratedCommonAsset(texturePath, textureBytes), false);
            boolean modelChanged = register(module, new GeneratedCommonAsset(modelPath,
                modelJson(texture.getWidth(), texture.getHeight()).getBytes(StandardCharsets.UTF_8)), true);
            if (textureChanged && !modelChanged) {
                CommonAsset current = CommonAssetRegistry.getByName(texturePath);
                if (current != null) module.sendAsset(current, true);
            }

            double effectiveLineHeight = rendered.lineHeight() * limited.factor();
            float scale = (float)(size * MODEL_PIXELS_PER_BLOCK / effectiveLineHeight);
            double width = texture.getWidth() * scale / MODEL_PIXELS_PER_BLOCK;
            double height = texture.getHeight() * scale / MODEL_PIXELS_PER_BLOCK;
            Box bounds = new Box(new Vector3d(-width / 2.0, -height / 2.0, -0.08),
                new Vector3d(width / 2.0, height / 2.0, 0.08));
            Model model = new Model(base.getId(), scale, Map.of(), new ModelAttachment[0], bounds,
                modelPath, texturePath, base.getGradientSet(), base.getGradientId(),
                base.getEyeHeight(), base.getCrouchOffset(), base.getSittingOffset(),
                base.getSleepingOffset(), base.getAnimationSetMap(), base.getCamera(), base.getLight(),
                base.getParticles(), base.getTrails(), base.getPhysicsValues(), base.getDetailBoxes(),
                base.getPhobia(), base.getPhobiaModelAssetId());
            return model;
        } catch (Exception exception) {
            return null;
        }
    }

    private static RenderedCanvas render(RichTextParser.Parsed parsed, TextAlignment alignment, float opacity) {
        int fontSize = adaptiveFontSize(parsed);
        int lineHeight = Math.round(fontSize * 1.28f);
        int padding = Math.max(20, fontSize / 3);
        Font normal = new Font(Font.SANS_SERIF, Font.PLAIN, fontSize);
        Font bold = normal.deriveFont(Font.BOLD);
        BufferedImage metricsImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D metricsGraphics = metricsImage.createGraphics();
        configure(metricsGraphics);
        FontMetrics normalMetrics = metricsGraphics.getFontMetrics(normal);
        FontMetrics boldMetrics = metricsGraphics.getFontMetrics(bold);
        int[] widths = new int[parsed.lines().size()];
        int widest = 1;
        for (int line = 0; line < parsed.lines().size(); line++) {
            int width = 0;
            for (RichTextParser.Glyph glyph : parsed.lines().get(line)) {
                width += font(glyph, normal, bold) == bold
                    ? boldMetrics.stringWidth(glyph.text()) : normalMetrics.stringWidth(glyph.text());
            }
            widths[line] = width;
            widest = Math.max(widest, width);
        }
        metricsGraphics.dispose();

        int height = Math.max(lineHeight, parsed.lines().size() * lineHeight) + padding * 2;
        BufferedImage image = new BufferedImage(widest + padding * 2, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        configure(graphics);
        float alpha = Math.max(0.0f, Math.min(1.0f, opacity));
        BufferedImage glow = renderGlowLayer(parsed, alignment, widths, widest, padding, lineHeight,
            normal, bold, normalMetrics, boldMetrics, alpha, image.getWidth(), image.getHeight(), fontSize);
        if (glow != null) graphics.drawImage(glow, 0, 0, null);
        for (int lineIndex = 0; lineIndex < parsed.lines().size(); lineIndex++) {
            List<RichTextParser.Glyph> line = parsed.lines().get(lineIndex);
            int x = switch (alignment) {
                case LEFT -> padding;
                case CENTER -> padding + (widest - widths[lineIndex]) / 2;
                case RIGHT -> padding + widest - widths[lineIndex];
            };
            int baseline = padding + lineIndex * lineHeight + normalMetrics.getAscent();
            for (RichTextParser.Glyph glyph : line) {
                Font selected = font(glyph, normal, bold);
                graphics.setFont(selected);
                FontMetrics glyphMetrics = selected == bold ? boldMetrics : normalMetrics;
                int advance = glyphMetrics.stringWidth(glyph.text());
                Paint fill = paint(glyph.style(), padding, padding + widest);
                switch (glyph.style().effect()) {
                    case HIGHLIGHT -> {
                        graphics.setComposite(AlphaComposite.SrcOver.derive(alpha * 0.55f));
                        graphics.setPaint(fill);
                        int inset = Math.max(3, fontSize / 20);
                        graphics.fillRoundRect(x - inset, baseline - glyphMetrics.getAscent(), advance + inset * 2,
                            glyphMetrics.getHeight(), fontSize / 8, fontSize / 8);
                        drawGlyph(graphics, glyph.text(), x, baseline, Color.WHITE, alpha);
                    }
                    case SHADOW -> {
                        int offset = Math.max(3, fontSize / 16);
                        drawGlyph(graphics, glyph.text(), x + offset, baseline + offset, Color.BLACK, alpha * 0.72f);
                        drawGlyph(graphics, glyph.text(), x, baseline, fill, alpha);
                    }
                    case GLOW -> drawGlyph(graphics, glyph.text(), x, baseline, fill, alpha);
                    case OUTLINE -> drawOutlinedGlyph(graphics, glyph.text(), x, baseline, selected, fill, alpha, fontSize);
                    default -> drawGlyph(graphics, glyph.text(), x, baseline, fill, alpha);
                }
                x += advance;
            }
        }
        graphics.dispose();
        return new RenderedCanvas(image, lineHeight);
    }

    private static int adaptiveFontSize(RichTextParser.Parsed parsed) {
        int longest = parsed.lines().stream().mapToInt(List::size).max().orElse(1);
        if (longest <= 28 && parsed.lines().size() <= 4) return 128;
        if (longest <= 64 && parsed.lines().size() <= 8) return 108;
        return 88;
    }

    private static Font font(RichTextParser.Glyph glyph, Font normal, Font bold) {
        return glyph.style().effect() == RichTextParser.Effect.BOLD ? bold : normal;
    }

    private static Paint paint(RichTextParser.Style style, int startX, int endX) {
        Color first = new Color(style.rgb());
        if (!style.hasGradient()) return first;
        return new GradientPaint(startX, 0, first, Math.max(startX + 1, endX), 0,
            new Color(style.secondaryRgb()));
    }

    private static void drawGlyph(Graphics2D graphics, String text, int x, int y, Paint paint, float alpha) {
        graphics.setComposite(AlphaComposite.SrcOver.derive(Math.max(0.0f, Math.min(1.0f, alpha))));
        graphics.setPaint(paint);
        graphics.drawString(text, x, y);
    }

    private static void drawOutlinedGlyph(Graphics2D graphics, String text, int x, int y, Font font,
                                          Paint fill, float alpha, int fontSize) {
        Shape glyph = font.createGlyphVector(graphics.getFontRenderContext(), text).getOutline(x, y);
        graphics.setComposite(AlphaComposite.SrcOver.derive(Math.max(0.0f, Math.min(1.0f, alpha * 0.9f))));
        graphics.setPaint(new Color(0, 0, 0));
        graphics.setStroke(new BasicStroke(Math.max(2.5f, fontSize / 24.0f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.draw(glyph);
        graphics.setComposite(AlphaComposite.SrcOver.derive(Math.max(0.0f, Math.min(1.0f, alpha))));
        graphics.setPaint(fill);
        graphics.fill(glyph);
    }

    private static BufferedImage renderGlowLayer(RichTextParser.Parsed parsed, TextAlignment alignment,
                                                  int[] widths, int widest, int padding, int lineHeight,
                                                  Font normal, Font bold, FontMetrics normalMetrics,
                                                  FontMetrics boldMetrics, float alpha, int imageWidth,
                                                  int imageHeight, int fontSize) {
        boolean hasGlow = parsed.lines().stream().flatMap(List::stream)
            .anyMatch(glyph -> glyph.style().effect() == RichTextParser.Effect.GLOW);
        if (!hasGlow) return null;
        BufferedImage layer = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D glow = layer.createGraphics();
        configure(glow);
        for (int lineIndex = 0; lineIndex < parsed.lines().size(); lineIndex++) {
            int x = switch (alignment) {
                case LEFT -> padding;
                case CENTER -> padding + (widest - widths[lineIndex]) / 2;
                case RIGHT -> padding + widest - widths[lineIndex];
            };
            int baseline = padding + lineIndex * lineHeight + normalMetrics.getAscent();
            for (RichTextParser.Glyph glyph : parsed.lines().get(lineIndex)) {
                Font selected = font(glyph, normal, bold);
                glow.setFont(selected);
                FontMetrics metrics = selected == bold ? boldMetrics : normalMetrics;
                if (glyph.style().effect() == RichTextParser.Effect.GLOW) {
                    drawGlyph(glow, glyph.text(), x, baseline, paint(glyph.style(), padding, padding + widest), alpha * 0.9f);
                }
                x += metrics.stringWidth(glyph.text());
            }
        }
        glow.dispose();
        int radius = Math.max(4, fontSize / 18);
        float[] kernel = gaussianKernel(radius);
        BufferedImage horizontal = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        new ConvolveOp(new Kernel(kernel.length, 1, kernel), ConvolveOp.EDGE_NO_OP, null).filter(layer, horizontal);
        BufferedImage blurred = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        new ConvolveOp(new Kernel(1, kernel.length, kernel), ConvolveOp.EDGE_NO_OP, null).filter(horizontal, blurred);
        return blurred;
    }

    private static float[] gaussianKernel(int radius) {
        int length = radius * 2 + 1;
        float sigma = Math.max(1.0f, radius / 2.25f);
        float[] values = new float[length];
        float sum = 0;
        for (int index = 0; index < length; index++) {
            int distance = index - radius;
            float value = (float)Math.exp(-(distance * distance) / (2.0f * sigma * sigma));
            values[index] = value;
            sum += value;
        }
        for (int index = 0; index < length; index++) values[index] /= sum;
        return values;
    }

    private static void configure(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    private static LimitedImage limit(BufferedImage source) {
        int longest = Math.max(source.getWidth(), source.getHeight());
        if (longest <= MAX_TEXTURE_SIDE) return new LimitedImage(source, 1.0);
        double factor = (double)MAX_TEXTURE_SIDE / longest;
        int width = Math.max(1, (int)Math.round(source.getWidth() * factor));
        int height = Math.max(1, (int)Math.round(source.getHeight() * factor));
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resized.createGraphics();
        graphics.setComposite(AlphaComposite.Src);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        return new LimitedImage(resized, factor);
    }

    /** Copies nearby RGB into transparent pixels so atlas mipmaps do not create dark fringes. */
    private static BufferedImage bleedTransparentRgb(BufferedImage source, int radius) {
        BufferedImage current = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D copy = current.createGraphics();
        copy.setComposite(AlphaComposite.Src);
        copy.drawImage(source, 0, 0, null);
        copy.dispose();
        int width = current.getWidth();
        int height = current.getHeight();
        for (int pass = 0; pass < radius; pass++) {
            int[] before = current.getRGB(0, 0, width, height, null, 0, width);
            int[] after = before.clone();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int index = y * width + x;
                    if ((before[index] >>> 24) != 0 || (before[index] & 0x00ffffff) != 0) continue;
                    long red = 0, green = 0, blue = 0;
                    int count = 0;
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dx == 0 && dy == 0) continue;
                            int nx = x + dx, ny = y + dy;
                            if (nx < 0 || ny < 0 || nx >= width || ny >= height) continue;
                            int neighbor = before[ny * width + nx];
                            if ((neighbor >>> 24) == 0 && (neighbor & 0x00ffffff) == 0) continue;
                            red += (neighbor >>> 16) & 0xff;
                            green += (neighbor >>> 8) & 0xff;
                            blue += neighbor & 0xff;
                            count++;
                        }
                    }
                    if (count > 0) after[index] = ((int)(red / count) << 16) | ((int)(green / count) << 8) | (int)(blue / count);
                }
            }
            current.setRGB(0, 0, width, height, after, 0, width);
        }
        return current;
    }

    static void clearCache() {
        synchronized (MODEL_CACHE) { MODEL_CACHE.clear(); }
    }

    static int cachedModelCount() {
        synchronized (MODEL_CACHE) { return MODEL_CACHE.size(); }
    }

    private static boolean register(CommonAssetModule module, GeneratedCommonAsset asset, boolean forceRebuild) {
        CommonAsset existing = CommonAssetRegistry.getByName(asset.getName());
        if (existing != null && existing.getHash().equals(asset.getHash())) return false;
        module.addCommonAsset(RUNTIME_PACK, asset, false);
        module.sendAsset(asset, forceRebuild);
        return true;
    }

    private static byte[] png(BufferedImage image) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "PNG", output)) throw new IllegalStateException("PNG encoder unavailable");
        return output.toByteArray();
    }

    private static String modelJson(int width, int height) {
        return String.format(Locale.ROOT, """
            {
              "nodes": [{
                "id": "text",
                "name": "Raynor Rich Text",
                "children": [],
                "position": { "x": 0, "y": 0, "z": 0 },
                "orientation": { "x": 0, "y": 0, "z": 0, "w": 1 },
                "shape": {
                  "type": "quad",
                  "offset": { "x": 0, "y": 0, "z": 0 },
                  "stretch": { "x": 1, "y": 1, "z": 1 },
                  "settings": { "size": { "x": %d, "y": %d } },
                  "visible": true,
                  "doubleSided": true,
                  "shadingMode": "flat",
                  "unwrapMode": "custom",
                  "textureLayout": {
                    "front": {
                      "offset": { "x": 0, "y": 0 },
                      "mirror": { "x": false, "y": false },
                      "angle": 0
                    }
                  }
                }
              }],
              "lod": "auto"
            }
            """, width, height);
    }

    private record RenderedCanvas(BufferedImage image, int lineHeight) {}
    private record LimitedImage(BufferedImage image, double factor) {}
    private record RenderKey(String text, int sizeBits, TextAlignment alignment, int opacityStep) {}
}
