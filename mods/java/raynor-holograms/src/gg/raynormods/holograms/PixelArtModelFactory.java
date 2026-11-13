package gg.raynormods.holograms;

import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.server.core.asset.common.CommonAsset;
import com.hypixel.hytale.server.core.asset.common.CommonAssetModule;
import com.hypixel.hytale.server.core.asset.common.CommonAssetRegistry;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import org.joml.Vector3d;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Converts arbitrary images to rectangles sampled from a static 3-3-2 RGB palette.
 * Update 6 accepts the generated model because its texture exists before PrepareEntitiesAtlas.
 */
final class PixelArtModelFactory {
    private static final String RUNTIME_PACK = "RaynorMods:RaynorHologramsRuntime";
    private static final String GENERATED_ROOT = "NPC/RaynorHolograms/Generated/PixelImage_";
    private static final String BASE_MODEL = "Raynor_Hologram_Pixel_Selection_Proxy";
    private static final String COLOR_ATLAS = "NPC/RaynorHolograms/ColorAtlas.png";
    private static final int ATLAS_COLUMNS = 16;
    private static final int CELL_SIZE = 128;
    private static final int MAX_RECTANGLES = 2400;
    private static final int MIN_ADAPTIVE_SIDE = 24;
    private static final double MODEL_PIXELS_PER_BLOCK = 512.0;

    private PixelArtModelFactory() {}

    static synchronized Model create(BufferedImage source, float desiredUnits) {
        if (source == null || source.getWidth() < 1 || source.getHeight() < 1) return null;
        ModelAsset base = ModelAsset.getAssetMap().getAsset(BASE_MODEL);
        CommonAssetModule module = CommonAssetModule.get();
        if (base == null || module == null) return null;
        try {
            Prepared prepared = prepare(source);
            byte[] modelBytes = modelJson(prepared).getBytes(StandardCharsets.UTF_8);
            String hash = CommonAsset.hash(modelBytes).substring(0, 24);
            String modelPath = GENERATED_ROOT + hash + ".blockymodel";
            register(module, new GeneratedCommonAsset(modelPath, modelBytes));

            int longest = Math.max(prepared.width(), prepared.height());
            float scale = desiredUnits * (float)MODEL_PIXELS_PER_BLOCK / Math.max(1, longest);
            double width = prepared.width() * scale / MODEL_PIXELS_PER_BLOCK;
            double height = prepared.height() * scale / MODEL_PIXELS_PER_BLOCK;
            Box bounds = new Box(new Vector3d(-width / 2.0, -height / 2.0, -0.08),
                new Vector3d(width / 2.0, height / 2.0, 0.08));
            return new Model(base.getId(), scale, Map.of(), new ModelAttachment[0], bounds,
                modelPath, COLOR_ATLAS, base.getGradientSet(), base.getGradientId(), base.getEyeHeight(),
                base.getCrouchOffset(), base.getSittingOffset(), base.getSleepingOffset(),
                base.getAnimationSetMap(), base.getCamera(), base.getLight(), base.getParticles(),
                base.getTrails(), base.getPhysicsValues(), base.getDetailBoxes(), base.getPhobia(),
                base.getPhobiaModelAssetId());
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static Prepared prepare(BufferedImage original) {
        BufferedImage image = toArgb(original);
        while (true) {
            int[][] palette = quantize(image);
            List<ColorRectangle> rectangles = rectangles(palette, image.getWidth(), image.getHeight());
            int longest = Math.max(image.getWidth(), image.getHeight());
            if (rectangles.size() <= MAX_RECTANGLES || longest <= MIN_ADAPTIVE_SIDE) {
                return new Prepared(image.getWidth(), image.getHeight(), List.copyOf(rectangles));
            }
            double factor = Math.max(0.70, Math.sqrt((double)MAX_RECTANGLES / rectangles.size()) * 0.94);
            int width = Math.max(MIN_ADAPTIVE_SIDE, (int)Math.round(image.getWidth() * factor));
            int height = Math.max(MIN_ADAPTIVE_SIDE, (int)Math.round(image.getHeight() * factor));
            if (width == image.getWidth() && height == image.getHeight()) {
                return new Prepared(image.getWidth(), image.getHeight(), List.copyOf(rectangles));
            }
            image = resize(image, width, height);
        }
    }

    private static int[][] quantize(BufferedImage image) {
        int[][] result = new int[image.getHeight()][image.getWidth()];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                if (((argb >>> 24) & 0xff) < 64) {
                    result[y][x] = -1;
                    continue;
                }
                int red = (argb >>> 16) & 0xff;
                int green = (argb >>> 8) & 0xff;
                int blue = argb & 0xff;
                int r3 = Math.min(7, (red * 7 + 127) / 255);
                int g3 = Math.min(7, (green * 7 + 127) / 255);
                int b2 = Math.min(3, (blue * 3 + 127) / 255);
                result[y][x] = (r3 << 5) | (g3 << 2) | b2;
            }
        }
        return result;
    }

    private static List<ColorRectangle> rectangles(int[][] colors, int width, int height) {
        boolean[][] used = new boolean[height][width];
        List<ColorRectangle> rectangles = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = colors[y][x];
                if (color < 0 || used[y][x]) continue;
                int rectangleWidth = 1;
                while (x + rectangleWidth < width && !used[y][x + rectangleWidth]
                    && colors[y][x + rectangleWidth] == color) rectangleWidth++;
                int rectangleHeight = 1;
                heightLoop:
                while (y + rectangleHeight < height) {
                    for (int dx = 0; dx < rectangleWidth; dx++) {
                        if (used[y + rectangleHeight][x + dx]
                            || colors[y + rectangleHeight][x + dx] != color) break heightLoop;
                    }
                    rectangleHeight++;
                }
                for (int dy = 0; dy < rectangleHeight; dy++) {
                    for (int dx = 0; dx < rectangleWidth; dx++) used[y + dy][x + dx] = true;
                }
                rectangles.add(new ColorRectangle(x, y, rectangleWidth, rectangleHeight, color));
            }
        }
        return rectangles;
    }

    private static BufferedImage resize(BufferedImage source, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resized.createGraphics();
        graphics.setComposite(AlphaComposite.Src);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        return resized;
    }

    private static BufferedImage toArgb(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_INT_ARGB) return source;
        BufferedImage image = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setComposite(AlphaComposite.Src);
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return image;
    }

    private static void register(CommonAssetModule module, GeneratedCommonAsset asset) {
        CommonAsset existing = CommonAssetRegistry.getByName(asset.getName());
        if (existing != null && existing.getHash().equals(asset.getHash())) return;
        module.addCommonAsset(RUNTIME_PACK, asset, false);
    }

    private static String modelJson(Prepared prepared) {
        StringBuilder json = new StringBuilder(512 + prepared.rectangles().size() * 600);
        json.append("{\n  \"nodes\": [\n");
        for (int index = 0; index < prepared.rectangles().size(); index++) {
            ColorRectangle rectangle = prepared.rectangles().get(index);
            int atlasX = (rectangle.paletteIndex() % ATLAS_COLUMNS) * CELL_SIZE;
            int atlasY = (rectangle.paletteIndex() / ATLAS_COLUMNS) * CELL_SIZE;
            double centerX = rectangle.x() + rectangle.width() / 2.0 - prepared.width() / 2.0;
            double centerY = prepared.height() / 2.0 - rectangle.y() - rectangle.height() / 2.0;
            if (index > 0) json.append(",\n");
            json.append(String.format(Locale.ROOT, """
                {
                  "id": "pixel_%d",
                  "name": "Raynor Image Region",
                  "children": [],
                  "position": { "x": 0, "y": 0, "z": 0 },
                  "orientation": { "x": 0, "y": 0, "z": 0, "w": 1 },
                  "shape": {
                    "type": "quad",
                    "offset": { "x": %.3f, "y": %.3f, "z": 0 },
                    "stretch": { "x": 1.0, "y": 1.0, "z": 1.0 },
                    "settings": { "size": { "x": %.3f, "y": %.3f } },
                    "visible": true,
                    "doubleSided": true,
                    "shadingMode": "flat",
                    "unwrapMode": "custom",
                    "textureLayout": {
                      "front": {
                        "offset": { "x": %d, "y": %d },
                        "mirror": { "x": false, "y": false },
                        "angle": 0
                      }
                    }
                  }
                }
                """, index, centerX, centerY, (double)rectangle.width(),
                (double)rectangle.height(), atlasX, atlasY));
        }
        json.append("\n  ],\n  \"lod\": \"auto\"\n}\n");
        return json.toString();
    }

    private record ColorRectangle(int x, int y, int width, int height, int paletteIndex) {}
    private record Prepared(int width, int height, List<ColorRectangle> rectangles) {}
}
