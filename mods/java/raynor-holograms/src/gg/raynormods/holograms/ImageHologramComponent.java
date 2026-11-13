package gg.raynormods.holograms;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public final class ImageHologramComponent implements Component<EntityStore> {
    public static final BuilderCodec<ImageHologramComponent> CODEC = BuilderCodec
        .builder(ImageHologramComponent.class, ImageHologramComponent::new)
        .append(new KeyedCodec<>("Id", Codec.STRING), (component, value) -> component.id = value, component -> component.id).add()
        .append(new KeyedCodec<>("Image", Codec.STRING), (component, value) -> component.source = migrateSource(value), component -> component.source).add()
        .append(new KeyedCodec<>("Size", Codec.FLOAT), (component, value) -> component.size = value, component -> component.size).add()
        .append(new KeyedCodec<>("Billboard", Codec.STRING, false), (component, value) -> component.billboard = value, component -> component.billboard).add()
        .append(new KeyedCodec<>("EntityUuid", Codec.STRING, false), (component, value) -> component.entityUuid = value, component -> component.entityUuid).add()
        .build();

    private static ComponentType<EntityStore, ImageHologramComponent> componentType;
    private String id = "";
    private String source = "builtin:orb";
    private float size = 2.0f;
    private String billboard = BillboardMode.BILLBOARD_Y.name();
    private String entityUuid = "";
    private transient ImageAssetRenderer.RenderedAnimation animation;
    private transient int frameIndex;
    private transient float frameElapsedMs;
    private transient int billboardTicks;
    private transient String particleAssetId = "";
    private transient float particleLifeSeconds;
    private transient float particleElapsedSeconds;
    private transient CompletableFuture<ParticleHologramAssets.AssetResult> particlePreparation;
    private transient String particleError = "";

    public ImageHologramComponent() {}

    ImageHologramComponent(String id, String source, float size, BillboardMode billboard) {
        this.id = id;
        this.source = migrateSource(source);
        this.size = size;
        this.billboard = billboard.name();
    }

    private static String migrateSource(String value) {
        if (value == null || value.isBlank()) return "builtin:orb";
        String trimmed = value.trim();
        return ImageCatalog.find(trimmed) == null || trimmed.contains(":") ? trimmed : "builtin:" + trimmed;
    }

    public static void setComponentType(ComponentType<EntityStore, ImageHologramComponent> type) { componentType = type; }

    @Nonnull
    public static ComponentType<EntityStore, ImageHologramComponent> getComponentType() {
        if (componentType == null) throw new IllegalStateException("ImageHologramComponent has not been registered");
        return componentType;
    }

    public String getId() { return id; }
    void setId(String value) { id = value; }
    public String getSource() { return source; }
    public String getImageKey() { return source; }
    void setSource(String value) { source = migrateSource(value); invalidateRender(); }
    public float getSize() { return Float.isFinite(size) && size > 0 ? size : 2.0f; }
    void setSize(float value) { size = value; invalidateRender(); }
    BillboardMode getBillboardMode() { return BillboardMode.parse(billboard); }
    void setBillboardMode(BillboardMode value) { billboard = value.name(); invalidateRender(); }
    String getEntityUuid() { return entityUuid; }
    void setEntityUuid(String value) { entityUuid = value == null ? "" : value; }
    ImageAssetRenderer.RenderedAnimation getAnimation() { return animation; }
    void setAnimation(ImageAssetRenderer.RenderedAnimation value) { animation = value; frameIndex = 0; frameElapsedMs = 0; }
    void invalidateRender() {
        animation = null;
        frameIndex = 0;
        frameElapsedMs = 0;
        particleAssetId = "";
        particleLifeSeconds = 0;
        particleElapsedSeconds = 0;
        particlePreparation = null;
        particleError = "";
    }
    int currentFrame() { return frameIndex; }
    boolean shouldUpdateBillboard() { return billboardTicks++ % 4 == 0; }

    String getParticleAssetId() { return particleAssetId; }
    float getParticleLifeSpanSeconds() { return Math.max(0.05f, particleLifeSeconds); }
    CompletableFuture<ParticleHologramAssets.AssetResult> getParticlePreparation() { return particlePreparation; }
    void beginParticlePreparation() {
        if (particlePreparation == null && particleError.isEmpty()) {
            particlePreparation = ParticleHologramAssets.prepareAsync(getSource(), getSize(), getBillboardMode());
        }
    }
    ParticleHologramAssets.AssetResult consumeParticlePreparation() {
        CompletableFuture<ParticleHologramAssets.AssetResult> preparation = particlePreparation;
        if (preparation == null || !preparation.isDone()) return null;
        particlePreparation = null;
        try {
            ParticleHologramAssets.AssetResult result = preparation.join();
            if (!result.success()) particleError = result.error();
            return result;
        } catch (RuntimeException exception) {
            Throwable cause = exception.getCause();
            particleError = cause == null ? exception.getMessage() : cause.getMessage();
            return ParticleHologramAssets.AssetResult.failure(particleError);
        }
    }
    String getParticleError() { return particleError; }

    void setParticleAsset(ParticleHologramAssets.AssetResult result) {
        particleAssetId = result.assetId();
        particleLifeSeconds = result.lifeSpanSeconds();
        // Give the Common/Server asset update a short delivery window before the
        // first spawn. Later spawns retain the exact GIF loop duration.
        particleElapsedSeconds = Math.max(0.0f, result.lifeSpanSeconds() - 0.75f);
    }

    boolean shouldSpawnParticle(float deltaSeconds) {
        if (particleAssetId.isEmpty()) return false;
        particleElapsedSeconds += Math.max(0, deltaSeconds);
        if (particleElapsedSeconds < particleLifeSeconds) return false;
        particleElapsedSeconds %= Math.max(0.05f, particleLifeSeconds);
        return true;
    }

    boolean advanceFrame(float deltaSeconds) {
        if (animation == null || animation.frameCount() <= 1) return false;
        frameElapsedMs += Math.max(0, deltaSeconds) * 1000.0f;
        boolean changed = false;
        int guard = animation.frameCount();
        while (frameElapsedMs >= animation.delay(frameIndex) && guard-- > 0) {
            frameElapsedMs -= animation.delay(frameIndex);
            frameIndex = (frameIndex + 1) % animation.frameCount();
            changed = true;
        }
        return changed;
    }

    ImageHologramComponent copy() {
        ImageHologramComponent copy = new ImageHologramComponent(id, source, size, getBillboardMode());
        copy.entityUuid = entityUuid;
        return copy;
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() { return copy(); }
}
