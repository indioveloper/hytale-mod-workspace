package gg.raynormods.holograms;

import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.protocol.packets.setup.RequestCommonAssetsRebuild;
import com.hypixel.hytale.server.core.asset.common.CommonAsset;
import com.hypixel.hytale.server.core.asset.common.CommonAssetModule;
import com.hypixel.hytale.server.core.asset.common.CommonAssetRegistry;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.universe.Universe;
import org.joml.Vector3d;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

final class RuntimeAssetFactory {
    private static final String RUNTIME_PACK = "RaynorMods:RaynorHolograms";
    private static final String GENERATED_ROOT = "NPC/RaynorHolograms/Generated/";
    private static final String ATLAS_TEXTURE = "NPC/RaynorHolograms/MediaAtlas.png";
    private static final String BASE_MODEL = "Raynor_Hologram_Media_Selection_Proxy";
    private static final double MODEL_PIXELS_PER_BLOCK = 512.0;
    private static final int ATLAS_SIZE = 2048;
    private static final int ATLAS_GAP = 2;
    private static final BufferedImage ATLAS = new BufferedImage(ATLAS_SIZE, ATLAS_SIZE, BufferedImage.TYPE_INT_ARGB);
    private static final Map<String, AtlasRegion> REGIONS = new HashMap<>();
    private static int atlasX;
    private static int atlasY;
    private static int atlasRowHeight;
    private static String publishedAtlasHash = "";

    private RuntimeAssetFactory() {}

    static synchronized RenderedModel create(String prefix, BufferedImage image, float desiredUnits, ScaleBasis basis) {
        return create(prefix, image, desiredUnits, basis, true);
    }

    static synchronized RenderedModel create(String prefix, BufferedImage image, float desiredUnits,
                                             ScaleBasis basis, boolean publishAtlas) {
        if (image == null || image.getWidth() < 1 || image.getHeight() < 1) return null;
        ModelAsset base = ModelAsset.getAssetMap().getAsset(BASE_MODEL);
        CommonAssetModule module = CommonAssetModule.get();
        if (base == null || module == null) return null;
        try {
            BufferedImage texture = entityTexture(image);
            String hash = hash(png(texture)).substring(0, 24);
            AtlasRegion region = REGIONS.get(hash);
            if (region == null) {
                region = allocate(texture.getWidth(), texture.getHeight());
                if (region == null) return null;
                Graphics2D atlasGraphics = ATLAS.createGraphics();
                atlasGraphics.drawImage(texture, region.x(), region.y(), null);
                atlasGraphics.dispose();
                REGIONS.put(hash, region);
            }
            String stem = GENERATED_ROOT + prefix + "_" + hash;
            String modelPath = stem + ".blockymodel";
            register(module, new GeneratedCommonAsset(modelPath,
                modelJson(texture.getWidth(), texture.getHeight(), region.x(), region.y()).getBytes(StandardCharsets.UTF_8)));
            if (publishAtlas) publishAtlas(module);

            int basisPixels = basis == ScaleBasis.HEIGHT ? texture.getHeight() : Math.max(texture.getWidth(), texture.getHeight());
            float scale = desiredUnits * (float)MODEL_PIXELS_PER_BLOCK / Math.max(1, basisPixels);
            double width = texture.getWidth() * scale / MODEL_PIXELS_PER_BLOCK;
            double height = texture.getHeight() * scale / MODEL_PIXELS_PER_BLOCK;
            Box box = new Box(new Vector3d(-width / 2.0, -height / 2.0, -0.08), new Vector3d(width / 2.0, height / 2.0, 0.08));
            Model model = new Model(base.getId(), scale, Map.of(), new ModelAttachment[0], box,
                modelPath, ATLAS_TEXTURE, base.getGradientSet(), base.getGradientId(), base.getEyeHeight(),
                base.getCrouchOffset(), base.getSittingOffset(), base.getSleepingOffset(),
                base.getAnimationSetMap(), base.getCamera(), base.getLight(), base.getParticles(),
                base.getTrails(), base.getPhysicsValues(), base.getDetailBoxes(), base.getPhobia(),
                base.getPhobiaModelAssetId());
            return new RenderedModel(model, hash, texture.getWidth(), texture.getHeight());
        } catch (Exception ignored) {
            return null;
        }
    }

    static synchronized boolean publishAtlas() {
        CommonAssetModule module = CommonAssetModule.get();
        return module != null && publishAtlas(module);
    }

    private static boolean publishAtlas(CommonAssetModule module) {
        try {
            byte[] bytes = png(ATLAS);
            String atlasHash = hash(bytes);
            if (atlasHash.equals(publishedAtlasHash)) return true;
            module.addCommonAsset(RUNTIME_PACK, new GeneratedCommonAsset(ATLAS_TEXTURE, bytes), false);
            // addCommonAsset sends runtime updates without rebuilding the client's entity
            // texture atlas. The bytes arrive and are cached, but the model keeps sampling
            // the transparent atlas prepared at login unless we request a rebuild.
            Universe.get().broadcastPacketNoCache(new RequestCommonAssetsRebuild());
            publishedAtlasHash = atlasHash;
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static AtlasRegion allocate(int width, int height) {
        if (width > ATLAS_SIZE || height > ATLAS_SIZE) return null;
        if (atlasX + width > ATLAS_SIZE) {
            atlasX = 0;
            atlasY += atlasRowHeight + ATLAS_GAP;
            atlasRowHeight = 0;
        }
        if (atlasY + height > ATLAS_SIZE) return null;
        AtlasRegion region = new AtlasRegion(atlasX, atlasY, width, height);
        atlasX += width + ATLAS_GAP;
        atlasRowHeight = Math.max(atlasRowHeight, height);
        return region;
    }

    private static void register(CommonAssetModule module, GeneratedCommonAsset asset) {
        CommonAsset existing = CommonAssetRegistry.getByName(asset.getName());
        if (existing != null && existing.getHash().equals(asset.getHash())) return;
        module.addCommonAsset(RUNTIME_PACK, asset, false);
    }

    private static byte[] png(BufferedImage image) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "PNG", output)) throw new IllegalStateException("PNG encoder unavailable");
        return output.toByteArray();
    }

    static BufferedImage entityTexture(BufferedImage source) {
        int width = Math.max(32, ((source.getWidth() + 31) / 32) * 32);
        int height = Math.max(32, ((source.getHeight() + 31) / 32) * 32);
        if (width == source.getWidth() && height == source.getHeight()
            && source.getType() == BufferedImage.TYPE_INT_ARGB) return source;
        BufferedImage padded = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = padded.createGraphics();
        int x = (width - source.getWidth()) / 2;
        int y = (height - source.getHeight()) / 2;
        graphics.drawImage(source, x, y, null);
        graphics.dispose();
        return padded;
    }

    private static String hash(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    private static String modelJson(int width, int height, int textureX, int textureY) {
        return String.format(Locale.ROOT, """
            {
              "nodes": [
                {
                  "id": "root",
                  "name": "Raynor Runtime Hologram",
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
                        "offset": { "x": %d, "y": %d },
                        "mirror": { "x": false, "y": false },
                        "angle": 0
                      }
                    }
                  }
                }
              ],
              "lod": "auto"
            }
            """, width, height, textureX, textureY);
    }

    enum ScaleBasis { HEIGHT, LONGEST_SIDE }
    record RenderedModel(Model model, String signature, int width, int height) {}
    private record AtlasRegion(int x, int y, int width, int height) {}
}
