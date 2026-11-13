package gg.raynormods.holograms;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class TextHologramComponent implements Component<EntityStore> {
    public static final BuilderCodec<TextHologramComponent> CODEC = BuilderCodec
        .builder(TextHologramComponent.class, TextHologramComponent::new)
        .append(new KeyedCodec<>("Id", Codec.STRING), (component, value) -> component.id = value, component -> component.id).add()
        .append(new KeyedCodec<>("Text", Codec.STRING), (component, value) -> component.text = value, component -> component.text).add()
        .append(new KeyedCodec<>("Size", Codec.FLOAT, false), (component, value) -> component.size = value, component -> component.size).add()
        .append(new KeyedCodec<>("Billboard", Codec.STRING, false), (component, value) -> component.billboard = value, component -> component.billboard).add()
        .append(new KeyedCodec<>("Alignment", Codec.STRING, false), (component, value) -> component.alignment = value, component -> component.alignment).add()
        .append(new KeyedCodec<>("RenderMode", Codec.STRING, false), (component, value) -> component.renderMode = value, component -> component.renderMode).add()
        .append(new KeyedCodec<>("Lifetime", Codec.FLOAT, false), (component, value) -> component.lifetime = value, component -> component.lifetime).add()
        .append(new KeyedCodec<>("FadeDuration", Codec.FLOAT, false), (component, value) -> component.fadeDuration = value, component -> component.fadeDuration).add()
        .append(new KeyedCodec<>("EntityUuid", Codec.STRING, false), (component, value) -> component.entityUuid = value, component -> component.entityUuid).add()
        .build();

    private static ComponentType<EntityStore, TextHologramComponent> componentType;
    private String id = "";
    private String text = "";
    private float size = 1.0f;
    private String billboard = BillboardMode.BILLBOARD_Y.name();
    private String alignment = TextAlignment.CENTER.name();
    private String renderMode = TextRenderMode.AUTO.name();
    private float lifetime;
    private float fadeDuration;
    private String entityUuid = "";
    private transient float age;
    private transient String renderedSignature = "";
    private transient int renderedOpacityStep = -1;
    private transient int placeholderTicks;
    private transient int billboardTicks;
    private transient float assetWarmupAge;
    private transient int assetDeliveryRefreshes;

    public TextHologramComponent() {}

    TextHologramComponent(String id, String text, float size, BillboardMode billboard, TextAlignment alignment) {
        this(id, text, size, billboard, alignment, 0.0f, 0.0f);
    }

    TextHologramComponent(String id, String text, float size, BillboardMode billboard, TextAlignment alignment,
                          float lifetime, float fadeDuration) {
        this.id = id;
        this.text = text;
        this.size = size;
        this.billboard = billboard.name();
        this.alignment = alignment.name();
        this.lifetime = lifetime;
        this.fadeDuration = fadeDuration;
    }

    public static void setComponentType(ComponentType<EntityStore, TextHologramComponent> type) { componentType = type; }

    @Nonnull
    public static ComponentType<EntityStore, TextHologramComponent> getComponentType() {
        if (componentType == null) throw new IllegalStateException("TextHologramComponent has not been registered");
        return componentType;
    }

    public String getId() { return id; }
    void setId(String value) { id = value; }
    public String getText() { return text; }
    void setText(String value) { text = value == null ? "" : value; invalidateRender(); }
    public float getSize() { return Float.isFinite(size) && size > 0 ? size : 1.0f; }
    void setSize(float value) { size = value; invalidateRender(); }
    BillboardMode getBillboardMode() { return BillboardMode.parse(billboard); }
    void setBillboardMode(BillboardMode value) { billboard = value.name(); }
    TextAlignment getAlignment() { return TextAlignment.parse(alignment); }
    void setAlignment(TextAlignment value) { alignment = value.name(); invalidateRender(); }
    TextRenderMode getRenderMode() { return TextRenderMode.parse(renderMode); }
    void setRenderMode(TextRenderMode value) { renderMode = value.name(); invalidateRender(); }
    boolean usesLiveText(String resolvedText) {
        if (isTemporary() || getRenderMode() == TextRenderMode.RICH) return false;
        if (getRenderMode() == TextRenderMode.LIVE) return true;
        RichTextParser.Parsed parsed = RichTextParser.parse(resolvedText);
        return !RichTextParser.hasRichFormatting(parsed)
            && Math.abs(getSize() - 1.0f) < 0.001f
            && getAlignment() == TextAlignment.CENTER
            && getBillboardMode() != BillboardMode.FIXED;
    }
    String getEntityUuid() { return entityUuid; }
    void setEntityUuid(String value) { entityUuid = value == null ? "" : value; }
    String getRenderedSignature() { return renderedSignature; }
    int getRenderedOpacityStep() { return renderedOpacityStep; }
    void markRendered(String signature, int opacityStep) { renderedSignature = signature; renderedOpacityStep = opacityStep; }
    void invalidateRender() {
        renderedSignature = "";
        renderedOpacityStep = -1;
        placeholderTicks = 0;
        assetWarmupAge = 0;
        assetDeliveryRefreshes = 0;
    }
    boolean shouldResolvePlaceholders() { return placeholderTicks++ % 20 == 0; }
    boolean shouldUpdateBillboard() { return billboardTicks++ % 4 == 0; }
    boolean advanceAssetWarmup(float delta) {
        if (Float.isFinite(delta) && delta > 0) assetWarmupAge += delta;
        return assetWarmupAge >= 0.25f;
    }
    boolean shouldRefreshAfterAssetDelivery() {
        float threshold = switch (assetDeliveryRefreshes) {
            case 0 -> 0.25f;
            case 1 -> 0.75f;
            case 2 -> 1.50f;
            default -> Float.POSITIVE_INFINITY;
        };
        if (assetWarmupAge < threshold) return false;
        assetDeliveryRefreshes++;
        return true;
    }
    boolean advanceLifetime(float delta) {
        if (lifetime <= 0.0f) return false;
        if (Float.isFinite(delta) && delta > 0.0f) age += delta;
        return age >= lifetime;
    }
    int opacityStep() {
        if (lifetime <= 0.0f || fadeDuration <= 0.0f) return 10;
        float remaining = lifetime - age;
        if (remaining >= fadeDuration) return 10;
        int level = Math.max(0, Math.min(4, (int)Math.ceil(remaining / fadeDuration * 4.0f)));
        return switch (level) {
            case 0 -> 0;
            case 1 -> 3;
            case 2 -> 5;
            case 3 -> 8;
            default -> 10;
        };
    }
    boolean isTemporary() { return lifetime > 0.0f; }

    TextHologramComponent copy() {
        TextHologramComponent copy = new TextHologramComponent(id, text, size, getBillboardMode(), getAlignment(), lifetime, fadeDuration);
        copy.renderMode = renderMode;
        copy.entityUuid = entityUuid;
        copy.age = age;
        return copy;
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() { return copy(); }
}
