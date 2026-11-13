package gg.raynormods.holograms;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.server.core.asset.AssetModule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

final class HologramRuntime {
    static final String GENERATED_PACK_NAME = "RaynorMods:RaynorHologramsData";
    private static Path dataDirectory;
    private static Path imageDirectory;
    private static Path generatedPackDirectory;
    private static PluginManifest manifest;

    private HologramRuntime() {}

    static void initialize(Path directory, PluginManifest pluginManifest) {
        dataDirectory = directory.toAbsolutePath().normalize();
        imageDirectory = dataDirectory.resolve("images");
        generatedPackDirectory = dataDirectory.resolve("generated-pack");
        manifest = pluginManifest;
        try {
            Files.createDirectories(imageDirectory);
            Files.createDirectories(generatedPackDirectory.resolve("Common/Particles/RaynorHolograms"));
            Files.createDirectories(generatedPackDirectory.resolve("Server/Particles/RaynorHolograms"));
            writeGeneratedManifest();
            installSampleImage();
            registerGeneratedPack();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create the hologram image directory", exception);
        }
    }

    private static void writeGeneratedManifest() throws IOException {
        Path file = generatedPackDirectory.resolve("manifest.json");
        String contents = """
            {
              "Group": "RaynorMods",
              "Name": "RaynorHologramsData",
              "Version": "%s",
              "Description": "Generated textures and native particles for Raynor Holograms.",
              "Authors": [{ "Name": "Raynor" }],
              "ServerVersion": ">=0.6.2 <0.7.0",
              "Dependencies": {},
              "OptionalDependencies": {},
              "DisabledByDefault": false,
              "IncludesAssetPack": true
            }
            """.formatted(RaynorHologramsPlugin.VERSION);
        if (!Files.isRegularFile(file) || !Files.readString(file, StandardCharsets.UTF_8).equals(contents)) {
            Files.writeString(file, contents, StandardCharsets.UTF_8);
        }
    }

    private static void registerGeneratedPack() {
        AssetModule module = AssetModule.get();
        if (module == null || manifest == null) return;
        if (module.getAssetPack(GENERATED_PACK_NAME) == null) {
            module.registerPack(GENERATED_PACK_NAME, generatedPackDirectory, manifest, AssetPack.PackSource.RUNTIME);
        }
    }

    static synchronized void refreshGeneratedPack() {
        AssetModule module = AssetModule.get();
        if (module == null || manifest == null) return;
        if (module.getAssetPack(GENERATED_PACK_NAME) != null) module.unregisterPack(GENERATED_PACK_NAME);
        module.registerPack(GENERATED_PACK_NAME, generatedPackDirectory, manifest, AssetPack.PackSource.RUNTIME);
    }

    private static void installSampleImage() throws IOException {
        Path sample = imageDirectory.resolve("prueba.gif");
        if (Files.exists(sample)) return;
        try (InputStream input = HologramRuntime.class.getResourceAsStream("/Samples/prueba.gif")) {
            if (input != null) Files.copy(input, sample);
        }
    }

    static Path imageDirectory() {
        if (imageDirectory == null) throw new IllegalStateException("HologramRuntime is not initialized");
        return imageDirectory;
    }

    static Path generatedPackDirectory() {
        if (generatedPackDirectory == null) throw new IllegalStateException("HologramRuntime is not initialized");
        return generatedPackDirectory;
    }

    static void clearCaches() {
        TextRasterRenderer.clearCache();
        ImageAssetRenderer.clearCache();
    }
}
