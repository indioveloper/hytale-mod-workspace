package gg.raynormods.holograms;

import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import com.hypixel.hytale.common.plugin.PluginManifest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public final class RaynorHologramsSelfTest {
    public static void main(String[] args) {
        versionMetadataStaysInSync();
        parsesLegacyAndHexColors();
        parsesLinesEffectsAndUnicode();
        parsesGradientsOutlinesAndBackgrounds();
        advancesGifByElapsedTimeNotClientFrames();
        validatesBillboardAliases();
        validatesCommandShapes();
        padsEntityTexturesAndFadesTemporaryText();
        generatesNativeGifParticleAssets();
        rendersRichTextWithoutGlyphAtlas();
        selectsNativeTextIntelligently();
        delaysTextRevealUntilRuntimeModelArrives();
        System.out.println("RaynorHologramsSelfTest: OK");
    }

    private static void versionMetadataStaysInSync() {
        try (InputStream stream = RaynorHologramsSelfTest.class.getResourceAsStream("/manifest.json")) {
            require(stream != null, "manifest resource");
            String manifest = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            require(manifest.contains("\"Version\": \"" + RaynorHologramsPlugin.VERSION + "\""),
                "Java version and manifest version must stay in sync");
        } catch (IOException exception) {
            throw new AssertionError("could not read manifest resource", exception);
        }
    }

    private static void parsesLegacyAndHexColors() {
        RichTextParser.Parsed parsed = RichTextParser.parse("&dRosa &#55ffffCian");
        require(parsed.lines().size() == 1, "expected one line");
        require(parsed.lines().getFirst().getFirst().style().rgb() == 0xFF55FF, "legacy pink color");
        RichTextParser.Glyph cyan = parsed.lines().getFirst().stream().filter(glyph -> glyph.text().equals("C")).findFirst().orElseThrow();
        require(cyan.style().rgb() == 0x55FFFF, "hex cyan color");
    }

    private static void parsesLinesEffectsAndUnicode() {
        RichTextParser.Parsed parsed = RichTextParser.parse("<#ff8844:glow>Hola ♥</>\\n<yellow:bold>Mundo</>");
        require(parsed.lines().size() == 2, "escaped newline");
        require(parsed.lines().getFirst().getFirst().style().effect() == RichTextParser.Effect.GLOW, "glow effect");
        require(parsed.lines().get(1).getFirst().style().effect() == RichTextParser.Effect.BOLD, "bold effect");
        require(parsed.visibleCharacters() == 11, "visible unicode glyph count");
    }

    private static void parsesGradientsOutlinesAndBackgrounds() {
        RichTextParser.Parsed parsed = RichTextParser.parse(
            "<#ff55ff~#55ffff:outline>Borde</> <#223344:background>Fondo</>");
        RichTextParser.Style outline = parsed.lines().getFirst().getFirst().style();
        require(outline.rgb() == 0xFF55FF && outline.secondaryRgb() == 0x55FFFF,
            "gradient keeps both endpoint colors");
        require(outline.hasGradient(), "gradient style is detected");
        require(outline.effect() == RichTextParser.Effect.OUTLINE, "outline effect");
        RichTextParser.Glyph background = parsed.lines().getFirst().stream()
            .filter(glyph -> glyph.text().equals("F")).findFirst().orElseThrow();
        require(background.style().effect() == RichTextParser.Effect.HIGHLIGHT,
            "background is an alias for highlight");
    }

    private static void advancesGifByElapsedTimeNotClientFrames() {
        ImageAssetRenderer.RenderedAnimation animation = ImageAssetRenderer.RenderedAnimation.success(
            Arrays.asList((Model)null, (Model)null, (Model)null), new int[]{100, 200, 300}, "test");
        ImageHologramComponent component = new ImageHologramComponent("gif", "local:test.gif", 2, BillboardMode.FIXED);
        component.setAnimation(animation);
        require(!component.advanceFrame(0.099f), "must not advance before GIF delay");
        require(component.advanceFrame(0.001f), "must advance at GIF delay");
        require(component.currentFrame() == 1, "second GIF frame");
        require(!component.advanceFrame(0.199f), "second frame keeps its own delay");
        require(component.advanceFrame(0.001f), "second frame advances at 200 ms");
        require(component.currentFrame() == 2, "third GIF frame");
    }

    private static void validatesBillboardAliases() {
        require(BillboardMode.parse("billboardY") == BillboardMode.BILLBOARD_Y, "billboardY alias");
        require(BillboardMode.parse("fixed") == BillboardMode.FIXED, "fixed mode");
    }

    private static void validatesCommandShapes() {
        HologramCommand command = new HologramCommand();
        var createText = command.getSubCommand("create");
        require(createText != null, "create text command");
        require(createText.getRequiredArguments().get(1).getArgumentType().isGreedyString(),
            "create text must consume the remaining words");

        var createImage = command.getSubCommand("createimage");
        require(createImage != null, "create image command");
        require(createImage.getVariantByArgCount(3) != null,
            "create image must accept a positional size");
        require(command.getSubCommand("gif") == createImage, "gif command alias");
        require(command.getVariantByArgCount(3) != null,
            "root command must accept /holo <key> createimage <source>");
        require(command.getVariantByArgCount(4) != null,
            "root command must accept Fancy syntax with an explicit size");
        require(command.getVariantByArgCount(2) != null,
            "root command must accept /holo <key> billboard");
        require(command.getSubCommand("show").getRequiredArguments().get(1).getArgumentType().isGreedyString(),
            "temporary text must consume the remaining words");
    }

    private static void padsEntityTexturesAndFadesTemporaryText() {
        BufferedImage padded = RuntimeAssetFactory.entityTexture(
            new BufferedImage(341, 73, BufferedImage.TYPE_INT_ARGB));
        require(padded.getWidth() == 352 && padded.getHeight() == 96,
            "entity textures must be padded to multiples of 32");

        TextHologramComponent temporary = new TextHologramComponent(
            "temp", "Hola", 1.0f, BillboardMode.FIXED, TextAlignment.CENTER, 4.0f, 1.0f);
        require(!temporary.advanceLifetime(3.25f), "temporary hologram stays during its lifetime");
        require(temporary.opacityStep() < 10, "temporary hologram fades near expiry");
        require(temporary.advanceLifetime(0.75f), "temporary hologram expires on time");
    }

    private static void generatesNativeGifParticleAssets() {
        try {
            Path root = Path.of("build", "particle-test").toAbsolutePath().normalize();
            HologramRuntime.initialize(root, new PluginManifest());
            ParticleHologramAssets.AssetResult result = ParticleHologramAssets.ensure(
                "local:prueba.gif", 3.0f, BillboardMode.BILLBOARD);
            require(result.success(), "sample GIF particle assets must be generated: " + result.error());
            ParticleHologramAssets.AssetResult resultY = ParticleHologramAssets.ensure(
                "local:prueba.gif", 3.0f, BillboardMode.BILLBOARD_Y);
            require(resultY.success(), "sample GIF BillboardY assets must be generated: " + resultY.error());
            Path server = HologramRuntime.generatedPackDirectory().resolve("Server/Particles/RaynorHolograms");
            Path common = HologramRuntime.generatedPackDirectory().resolve("Common/Particles/RaynorHolograms");
            require(Files.isRegularFile(server.resolve(result.assetId() + ".particlesystem")),
                "generated particle system");
            require(Files.isRegularFile(server.resolve(result.assetId() + ".particlespawner")),
                "generated particle spawner");
            require(Files.list(common).anyMatch(path -> path.getFileName().toString().endsWith(".png")),
                "generated particle texture");
            String spawner = Files.readString(server.resolve(result.assetId() + ".particlespawner"));
            require(spawner.contains("\"ParticleRotationInfluence\": \"Billboard\""),
                "native billboard mode");
            require(spawner.contains("\"FrameIndex\""), "GIF frames are time-keyed in the particle");
            String animation = spawner.substring(spawner.indexOf("\"Animation\""));
            require(!animation.contains("\"Scale\""), "GIF keyframes must not multiply initial scale");
            String spawnerY = Files.readString(server.resolve(resultY.assetId() + ".particlespawner"));
            require(spawnerY.contains("\"ParticleRotationInfluence\": \"BillboardY\""),
                "native horizontal billboard mode");
        } catch (Exception exception) {
            throw new AssertionError("native particle asset generation", exception);
        }
    }

    private static void delaysTextRevealUntilRuntimeModelArrives() {
        TextHologramComponent component = new TextHologramComponent(
            "warmup", "Hola", 1, BillboardMode.BILLBOARD_Y, TextAlignment.CENTER);
        component.invalidateRender();
        require(!component.advanceAssetWarmup(0.24f), "runtime model stays invisible while delivered");
        require(component.advanceAssetWarmup(0.01f), "runtime model reveals after delivery warmup");
        require(component.shouldRefreshAfterAssetDelivery(), "first delayed model resend");
        require(!component.shouldRefreshAfterAssetDelivery(), "delivery resend is not repeated every tick");
        component.advanceAssetWarmup(0.50f);
        require(component.shouldRefreshAfterAssetDelivery(), "second delayed model resend");
    }

    private static void rendersRichTextWithoutGlyphAtlas() {
        BufferedImage image = TextCompositeRenderer.renderPreview(
            RichTextParser.parse("<#ff55ff~#55ffff:glow>Raynor Holograms</>\n"
                + "<#ffb347~#ff4d8d:outline>Contorno</> <#203248:background>Fondo</>"),
            TextAlignment.CENTER, 1.0f);
        require(image.getWidth() > 600 && image.getHeight() >= 200,
            "rich text is rasterized once at high resolution");
        boolean visible = false;
        for (int y = 0; y < image.getHeight() && !visible; y += 4) {
            for (int x = 0; x < image.getWidth(); x += 4) {
                if ((image.getRGB(x, y) >>> 24) != 0) { visible = true; break; }
            }
        }
        require(visible, "rich text texture contains visible antialiased pixels");
        try {
            Path preview = Path.of("build", "rich-text-preview.png").toAbsolutePath().normalize();
            Files.createDirectories(preview.getParent());
            ImageIO.write(image, "PNG", preview.toFile());
        } catch (Exception exception) {
            throw new AssertionError("write rich text preview", exception);
        }
    }

    private static void selectsNativeTextIntelligently() {
        TextHologramComponent plain = new TextHologramComponent(
            "plain", "Hola", 1.0f, BillboardMode.BILLBOARD_Y, TextAlignment.CENTER);
        require(plain.usesLiveText("Hola"), "plain default text uses Hytale nameplates");
        require(!plain.usesLiveText("<#ff55ff:glow>Hola</>"), "rich text keeps the effects renderer in AUTO");
        plain.setRenderMode(TextRenderMode.LIVE);
        require(plain.usesLiveText("<#ff55ff:glow>Hola</>"), "LIVE explicitly forces native text");
        plain.setRenderMode(TextRenderMode.RICH);
        require(!plain.usesLiveText("Hola"), "RICH explicitly forces the styled quad renderer");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
