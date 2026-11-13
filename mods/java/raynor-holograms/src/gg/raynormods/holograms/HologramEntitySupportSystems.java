package gg.raynormods.holograms;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.entity.Dirty;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;
import javax.annotation.Nonnull;
import java.util.UUID;

public final class HologramEntitySupportSystems {
    private HologramEntitySupportSystems() {}

    private static void ensureCommon(Ref<EntityStore> ref, int index, ArchetypeChunk<EntityStore> chunk,
                                     CommandBuffer<EntityStore> commands, com.hypixel.hytale.server.core.asset.type.model.config.Model model) {
        if (chunk.getComponent(index, MovementStatesComponent.getComponentType()) == null) commands.addComponent(ref, MovementStatesComponent.getComponentType(), new MovementStatesComponent());
        if (chunk.getComponent(index, Intangible.getComponentType()) == null) commands.addComponent(ref, Intangible.getComponentType(), Intangible.INSTANCE);
        if (model != null && chunk.getComponent(index, BoundingBox.getComponentType()) == null) commands.addComponent(ref, BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));
    }

    private static void markDirty(Ref<EntityStore> ref, CommandBuffer<EntityStore> commands) {
        Dirty dirty = commands.getComponent(ref, Dirty.getComponentType());
        if (dirty != null) dirty.markDirty();
    }

    private static void updateBillboard(Ref<EntityStore> ref, int index, ArchetypeChunk<EntityStore> chunk,
                                        CommandBuffer<EntityStore> commands, BillboardMode mode) {
        if (mode == BillboardMode.FIXED) return;
        TransformComponent transform = chunk.getComponent(index, TransformComponent.getComponentType());
        if (transform == null) return;
        World world = commands.getExternalData().getWorld();
        Vector3d origin = transform.getPosition();
        PlayerRef nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (PlayerRef player : world.getPlayerRefs()) {
            if (player.getTransform() == null) continue;
            double distance = player.getTransform().getPosition().distanceSquared(origin);
            if (distance < nearestDistance) { nearest = player; nearestDistance = distance; }
        }
        if (nearest == null) return;
        Vector3d target = new Vector3d(nearest.getTransform().getPosition()).add(0, 1.6, 0);
        Rotation3f facing = Rotation3f.lookAt(origin, target);
        if (mode == BillboardMode.BILLBOARD_Y) facing.setPitch(0);
        facing.setRoll(0);
        TransformComponent changed = transform.clone();
        changed.setRotation(facing);
        commands.putComponent(ref, TransformComponent.getComponentType(), changed);
        if (chunk.getComponent(index, HeadRotation.getComponentType()) != null) commands.putComponent(ref, HeadRotation.getComponentType(), new HeadRotation(facing));
    }

    public static final class TextIdentity extends RefChangeSystem<EntityStore, TextHologramComponent> {
        @Nonnull public Query<EntityStore> getQuery() { return TextHologramComponent.getComponentType(); }
        @Nonnull public ComponentType<EntityStore, TextHologramComponent> componentType() { return TextHologramComponent.getComponentType(); }

        public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull TextHologramComponent component,
                                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commands) {
            UUIDComponent uuid = commands.getComponent(ref, UUIDComponent.getComponentType());
            if (uuid == null) return;
            String entityUuid = uuid.getUuid().toString();
            boolean duplicated = !component.getEntityUuid().isBlank() && !component.getEntityUuid().equals(entityUuid);
            if (component.getId().isBlank() || duplicated) component.setId("text_" + UUID.randomUUID().toString().substring(0, 8));
            component.setEntityUuid(entityUuid);
            component.invalidateRender();
            com.hypixel.hytale.server.core.asset.type.model.config.Model invisible = ParticleHologramAssets.invisibleModel();
            if (invisible != null) {
                commands.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(invisible));
                commands.putComponent(ref, BoundingBox.getComponentType(), new BoundingBox(invisible.getBoundingBox()));
            }
            markDirty(ref, commands);
        }

        public void onComponentSet(@Nonnull Ref<EntityStore> ref, TextHologramComponent oldValue,
                                   @Nonnull TextHologramComponent newValue, @Nonnull Store<EntityStore> store,
                                   @Nonnull CommandBuffer<EntityStore> commands) {
            newValue.invalidateRender();
            com.hypixel.hytale.server.core.asset.type.model.config.Model invisible = ParticleHologramAssets.invisibleModel();
            if (invisible != null) {
                commands.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(invisible));
                commands.putComponent(ref, BoundingBox.getComponentType(), new BoundingBox(invisible.getBoundingBox()));
            }
            markDirty(ref, commands);
        }
        public void onComponentRemoved(@Nonnull Ref<EntityStore> ref, @Nonnull TextHologramComponent component,
                                       @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commands) {}
    }

    public static final class ImageIdentity extends RefChangeSystem<EntityStore, ImageHologramComponent> {
        @Nonnull public Query<EntityStore> getQuery() { return ImageHologramComponent.getComponentType(); }
        @Nonnull public ComponentType<EntityStore, ImageHologramComponent> componentType() { return ImageHologramComponent.getComponentType(); }

        public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull ImageHologramComponent component,
                                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commands) {
            UUIDComponent uuid = commands.getComponent(ref, UUIDComponent.getComponentType());
            if (uuid == null) return;
            String entityUuid = uuid.getUuid().toString();
            boolean duplicated = !component.getEntityUuid().isBlank() && !component.getEntityUuid().equals(entityUuid);
            if (component.getId().isBlank() || duplicated) component.setId("image_" + UUID.randomUUID().toString().substring(0, 8));
            component.setEntityUuid(entityUuid);
            component.invalidateRender();
            markDirty(ref, commands);
        }

        public void onComponentSet(@Nonnull Ref<EntityStore> ref, ImageHologramComponent oldValue,
                                   @Nonnull ImageHologramComponent newValue, @Nonnull Store<EntityStore> store,
                                   @Nonnull CommandBuffer<EntityStore> commands) { newValue.invalidateRender(); markDirty(ref, commands); }
        public void onComponentRemoved(@Nonnull Ref<EntityStore> ref, @Nonnull ImageHologramComponent component,
                                       @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commands) {}
    }

    public static final class TextSupport extends EntityTickingSystem<EntityStore> {
        @Nonnull public Query<EntityStore> getQuery() { return TextHologramComponent.getComponentType(); }

        public void tick(float delta, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                         @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commands) {
            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            TextHologramComponent component = chunk.getComponent(index, TextHologramComponent.getComponentType());
            if (component == null) return;
            if (chunk.getComponent(index, ProjectileComponent.getComponentType()) != null) {
                commands.removeComponent(ref, ProjectileComponent.getComponentType());
            }
            if (component.advanceLifetime(delta)) {
                commands.removeEntity(ref, RemoveReason.REMOVE);
                return;
            }
            int opacityStep = component.opacityStep();
            boolean assetReady = component.advanceAssetWarmup(delta);
            boolean deliveryRefresh = component.shouldRefreshAfterAssetDelivery();
            boolean contentRefresh = component.shouldResolvePlaceholders() || component.getRenderedSignature().isEmpty();
            if (contentRefresh) {
                String resolved = PlaceholderResolver.resolve(component.getText(), store.getExternalData().getWorld());
                if (component.usesLiveText(resolved)) {
                    String plain = RichTextParser.plainText(RichTextParser.parse(resolved));
                    Nameplate current = chunk.getComponent(index, Nameplate.getComponentType());
                    if (current == null) commands.addComponent(ref, Nameplate.getComponentType(), new Nameplate(plain));
                    else if (!current.getText().equals(plain)) commands.putComponent(ref, Nameplate.getComponentType(), new Nameplate(plain));
                    if (!component.getRenderedSignature().startsWith("live:")) {
                        com.hypixel.hytale.server.core.asset.type.model.config.Model invisible = ParticleHologramAssets.invisibleModel();
                        if (invisible != null) {
                            commands.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(invisible));
                            commands.putComponent(ref, BoundingBox.getComponentType(), new BoundingBox(invisible.getBoundingBox()));
                        }
                    }
                    component.markRendered("live:" + plain, 10);
                } else if (component.getRenderedSignature().startsWith("live:")) {
                    if (chunk.getComponent(index, Nameplate.getComponentType()) != null) {
                        commands.removeComponent(ref, Nameplate.getComponentType());
                    }
                    component.invalidateRender();
                }
            }
            if (component.getRenderedSignature().startsWith("live:")) {
                ModelComponent model = chunk.getComponent(index, ModelComponent.getComponentType());
                ensureCommon(ref, index, chunk, commands, model == null ? null : model.getModel());
                return;
            }
            if (chunk.getComponent(index, Nameplate.getComponentType()) != null) commands.removeComponent(ref, Nameplate.getComponentType());
            if (contentRefresh || component.getRenderedOpacityStep() != opacityStep || deliveryRefresh) {
                String resolved = PlaceholderResolver.resolve(component.getText(), store.getExternalData().getWorld());
                TextRasterRenderer.RenderedText rendered = TextRasterRenderer.create(
                    resolved, component.getSize(), component.getAlignment(), opacityStep / 10.0f);
                if (rendered.success()) {
                    boolean changed = !rendered.signature().equals(component.getRenderedSignature());
                    if (assetReady && (changed || deliveryRefresh)) {
                        commands.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(rendered.model()));
                        commands.putComponent(ref, BoundingBox.getComponentType(), new BoundingBox(rendered.model().getBoundingBox()));
                    }
                    if (chunk.getComponent(index, PersistentModel.getComponentType()) == null) {
                        commands.addComponent(ref, PersistentModel.getComponentType(), new PersistentModel(new com.hypixel.hytale.server.core.asset.type.model.config.Model.ModelReference(TextHologramService.SELECTION_MODEL, 1, null, true)));
                    }
                    component.markRendered(rendered.signature(), opacityStep);
                }
            }
            ModelComponent model = chunk.getComponent(index, ModelComponent.getComponentType());
            ensureCommon(ref, index, chunk, commands, model == null ? null : model.getModel());
            if (component.shouldUpdateBillboard()) updateBillboard(ref, index, chunk, commands, component.getBillboardMode());
        }
    }

    public static final class ImageSupport extends EntityTickingSystem<EntityStore> {
        @Nonnull public Query<EntityStore> getQuery() { return ImageHologramComponent.getComponentType(); }

        public void tick(float delta, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                         @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commands) {
            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            ImageHologramComponent component = chunk.getComponent(index, ImageHologramComponent.getComponentType());
            if (component == null) return;
            if (chunk.getComponent(index, ProjectileComponent.getComponentType()) != null) {
                commands.removeComponent(ref, ProjectileComponent.getComponentType());
            }
            boolean externalImage = component.getSource() != null
                && !component.getSource().trim().toLowerCase(java.util.Locale.ROOT).startsWith("builtin:");
            if (component.getParticleAssetId().isEmpty() && externalImage) {
                if (component.getParticlePreparation() == null && component.getParticleError().isEmpty()) {
                    component.beginParticlePreparation();
                    return;
                }
                ParticleHologramAssets.AssetResult particle = component.consumeParticlePreparation();
                if (particle == null) return;
                if (particle.success()) {
                    component.setParticleAsset(particle);
                    com.hypixel.hytale.server.core.asset.type.model.config.Model invisible = ParticleHologramAssets.invisibleModel();
                    if (invisible != null) {
                        commands.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(invisible));
                        commands.putComponent(ref, BoundingBox.getComponentType(), new BoundingBox(invisible.getBoundingBox()));
                    }
                }
                return;
            }

            if (!component.getParticleAssetId().isEmpty()) {
                TransformComponent transform = chunk.getComponent(index, TransformComponent.getComponentType());
                if (transform != null && component.shouldSpawnParticle(delta)) {
                    ParticleUtil.spawnParticleEffect(component.getParticleAssetId(), transform.getPosition(),
                        transform.getRotation().yaw(), transform.getRotation().pitch(), transform.getRotation().roll(),
                        1.0f, component.getParticleLifeSpanSeconds(), commands);
                }
                ModelComponent model = chunk.getComponent(index, ModelComponent.getComponentType());
                ensureCommon(ref, index, chunk, commands, model == null ? null : model.getModel());
                return;
            }

            ImageAssetRenderer.RenderedAnimation animation = component.getAnimation();
            if (animation == null) {
                animation = ImageAssetRenderer.create(component.getSource(), component.getSize());
                if (animation.success()) {
                    component.setAnimation(animation);
                    commands.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(animation.frame(0)));
                    commands.putComponent(ref, BoundingBox.getComponentType(), new BoundingBox(animation.frame(0).getBoundingBox()));
                }
            } else if (component.advanceFrame(delta)) {
                commands.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(animation.frame(component.currentFrame())));
            }
            ModelComponent model = chunk.getComponent(index, ModelComponent.getComponentType());
            ensureCommon(ref, index, chunk, commands, model == null ? null : model.getModel());
            if (component.shouldUpdateBillboard()) updateBillboard(ref, index, chunk, commands, component.getBillboardMode());
        }
    }
}
