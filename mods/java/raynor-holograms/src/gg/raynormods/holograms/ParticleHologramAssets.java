package gg.raynormods.holograms;

import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.HytaleServer;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/** Builds a small runtime asset pack containing full-colour particle holograms. */
final class ParticleHologramAssets {
    private static final String INVISIBLE_MODEL = "Raynor_Hologram_Invisible_Anchor";
    private static final float STATIC_LIFETIME_SECONDS = 4.25f;

    private ParticleHologramAssets() {}

    static CompletableFuture<AssetResult> prepareAsync(String source, float size, BillboardMode billboard) {
        return CompletableFuture.supplyAsync(
            () -> ensure(source, size, billboard),
            HytaleServer.SCHEDULED_EXECUTOR);
    }

    static synchronized AssetResult ensure(String source, float size, BillboardMode billboard) {
        if (source == null || source.toLowerCase(Locale.ROOT).startsWith("builtin:")) {
            return AssetResult.failure("Los recursos incluidos usan su modelo original.");
        }
        try {
            ImageAssetRenderer.DecodedImage decoded = ImageAssetRenderer.decoded(source);
            if (decoded.frames().isEmpty()) return AssetResult.failure("La imagen no contiene fotogramas.");

            BufferedImage first = decoded.frames().getFirst();
            int cellWidth = multipleOf32(first.getWidth());
            int cellHeight = multipleOf32(first.getHeight());
            int count = decoded.frames().size();
            int columns = count == 1 ? 1 : (int)Math.ceil(Math.sqrt(count));
            int rows = (count + columns - 1) / columns;
            BufferedImage sheet = new BufferedImage(cellWidth * columns, cellHeight * rows, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = sheet.createGraphics();
            graphics.setComposite(AlphaComposite.Src);
            for (int index = 0; index < count; index++) {
                BufferedImage frame = decoded.frames().get(index);
                int column = index % columns;
                int row = index / columns;
                int x = column * cellWidth + (cellWidth - frame.getWidth()) / 2;
                int y = row * cellHeight + (cellHeight - frame.getHeight()) / 2;
                graphics.drawImage(frame, x, y, null);
            }
            graphics.dispose();

            byte[] textureBytes = png(sheet);
            String textureHash = hash(textureBytes).substring(0, 24);
            String mode = switch (billboard) {
                case FIXED -> "None";
                case BILLBOARD_Y -> "BillboardY";
                case BILLBOARD -> "Billboard";
            };
            String identity = textureHash + ':' + size + ':' + mode + ':' + java.util.Arrays.hashCode(decoded.delaysMs());
            String assetHash = hash(identity.getBytes(StandardCharsets.UTF_8)).substring(0, 20);
            String stem = "Raynor_Holo_" + assetHash;

            Path root = HologramRuntime.generatedPackDirectory();
            Path texture = root.resolve("Common/Particles/RaynorHolograms/" + textureHash + ".png");
            Path spawner = root.resolve("Server/Particles/RaynorHolograms/" + stem + ".particlespawner");
            Path system = root.resolve("Server/Particles/RaynorHolograms/" + stem + ".particlesystem");

            float lifeSpan = count == 1 ? STATIC_LIFETIME_SECONDS : animationSeconds(decoded.delaysMs());
            int longest = Math.max(first.getWidth(), first.getHeight());
            float visibleWidth = size * first.getWidth() / longest;
            float visibleHeight = size * first.getHeight() / longest;
            float quadWidth = visibleWidth * cellWidth / first.getWidth();
            float quadHeight = visibleHeight * cellHeight / first.getHeight();

            boolean created = false;
            if (!Files.isRegularFile(texture)) {
                Files.write(texture, textureBytes);
                created = true;
            }
            if (!Files.isRegularFile(spawner)) {
                Files.writeString(spawner, spawnerJson(textureHash, cellWidth, cellHeight, quadWidth, quadHeight,
                    mode, billboard == BillboardMode.FIXED, lifeSpan, decoded.delaysMs()), StandardCharsets.UTF_8);
                created = true;
            }
            if (!Files.isRegularFile(system)) {
                Files.writeString(system, systemJson(stem, Math.max(80.0f, size * 16.0f), size), StandardCharsets.UTF_8);
                created = true;
            }
            if (created) HologramRuntime.refreshGeneratedPack();
            return AssetResult.success(stem, lifeSpan);
        } catch (Exception exception) {
            String message = exception.getMessage();
            return AssetResult.failure(message == null || message.isBlank() ? "No se pudo generar la particula de imagen." : message);
        }
    }

    static Model invisibleModel() {
        ModelAsset asset = ModelAsset.getAssetMap().getAsset(INVISIBLE_MODEL);
        return asset == null ? null : Model.createStaticScaledModel(asset, 1.0f);
    }

    private static String spawnerJson(String textureHash, int frameWidth, int frameHeight,
                                      float width, float height, String mode, boolean rotateWithSpawner,
                                      float lifeSpan, int[] delays) {
        return String.format(Locale.ROOT, """
            {
              "RenderMode": "BlendLinear",
              "ParticleRotationInfluence": "%s",
              "ParticleRotateWithSpawner": %s,
              "LinearFiltering": true,
              "LightInfluence": 0.0,
              "MaxConcurrentParticles": 1,
              "ParticleLifeSpan": { "Min": %.4f, "Max": %.4f },
              "TotalParticles": { "Min": 1, "Max": 1 },
              "SpawnBurst": true,
              "SpawnRate": { "Min": 1.0, "Max": 1.0 },
              "InitialVelocity": {
                "Speed": { "Min": 0.0, "Max": 0.0 },
                "Yaw": { "Min": 0.0, "Max": 0.0 },
                "Pitch": { "Min": 0.0, "Max": 0.0 }
              },
              "Particle": {
                "Texture": "Particles/RaynorHolograms/%s.png",
                "FrameSize": { "Width": %d, "Height": %d },
                "ScaleRatioConstraint": "None",
                "SoftParticles": "Disable",
                "UseSpriteBlending": false,
                "InitialAnimationFrame": {
                  "FrameIndex": { "Min": 0, "Max": 0 },
                  "Scale": {
                    "X": { "Min": %.5f, "Max": %.5f },
                    "Y": { "Min": %.5f, "Max": %.5f }
                  },
                  "Opacity": 1.0,
                  "Color": "#ffffff"
                },
                "Animation": %s
              }
            }
            """, mode, rotateWithSpawner, lifeSpan, lifeSpan, textureHash, frameWidth, frameHeight,
            width, width, height, height, animationJson(delays));
    }

    private static String animationJson(int[] delays) {
        StringBuilder json = new StringBuilder("{\n");
        int count = Math.max(1, delays.length);
        long total = 0;
        for (int delay : delays) total += Math.max(50, delay);
        long elapsed = 0;
        int previousPercent = -1;
        for (int index = 0; index < count; index++) {
            int percent = count == 1 ? 0 : (int)Math.round(elapsed * 100.0 / Math.max(1, total));
            percent = Math.min(99, Math.max(previousPercent + 1, percent));
            if (index > 0) json.append(",\n");
            json.append(String.format(Locale.ROOT,
                "  \"%d\": { \"FrameIndex\": { \"Min\": %d, \"Max\": %d }, \"Opacity\": 1.0 }",
                percent, index, index));
            previousPercent = percent;
            elapsed += Math.max(50, delays[Math.min(index, delays.length - 1)]);
        }
        json.append("\n}");
        return json.toString();
    }

    private static String systemJson(String stem, float distance, float radius) {
        return String.format(Locale.ROOT, """
            {
              "Spawners": [{ "SpawnerId": "%s" }],
              "CullDistance": %.2f,
              "BoundingRadius": %.2f,
              "IsImportant": true
            }
            """, stem, distance, Math.max(1.0f, radius));
    }

    private static float animationSeconds(int[] delays) {
        long total = 0;
        for (int delay : delays) total += Math.max(50, delay);
        return Math.max(0.05f, total / 1000.0f);
    }

    private static int multipleOf32(int value) { return Math.max(32, ((value + 31) / 32) * 32); }

    private static byte[] png(BufferedImage image) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "PNG", output)) throw new IllegalStateException("PNG encoder unavailable");
        return output.toByteArray();
    }

    private static String hash(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    record AssetResult(boolean success, String assetId, float lifeSpanSeconds, String error) {
        static AssetResult success(String id, float seconds) { return new AssetResult(true, id, seconds, ""); }
        static AssetResult failure(String error) { return new AssetResult(false, "", 0, error); }
    }
}
