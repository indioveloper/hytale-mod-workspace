package gg.raynormods.holograms;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.PropComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class ImageHologramService {
    private static final Pattern KEY = Pattern.compile("[a-z0-9_-]{1,32}");

    private ImageHologramService() {}

    public static SpawnResult spawn(Store<EntityStore> store, Ref<EntityStore> player, ImageCatalog.Entry entry, float size) {
        if (entry == null) return SpawnResult.failure("Imagen desconocida.");
        return spawn(store, player, nextKey(store), "builtin:" + entry.key(), size);
    }

    public static SpawnResult spawn(Store<EntityStore> store, Ref<EntityStore> player, String key, String source, float size) {
        TextHologramService.Placement placement = TextHologramService.placement(store, player, 1.5);
        if (placement == null) return SpawnResult.failure("No se pudo obtener la posicion del jugador.");
        return spawnAt(store, key, source, size, placement.position(), placement.yaw(), BillboardMode.BILLBOARD_Y);
    }

    public static SpawnResult spawnAt(Store<EntityStore> store, ImageCatalog.Entry entry, float size, Vector3d position, float yaw) {
        if (entry == null) return SpawnResult.failure("Imagen desconocida.");
        return spawnAt(store, nextKey(store), "builtin:" + entry.key(), size, position, yaw, BillboardMode.FIXED);
    }

    public static SpawnResult spawnAt(Store<EntityStore> store, String key, String source, float size, Vector3d position,
                                      float yaw, BillboardMode billboard) {
        String normalized = TextHologramService.normalizeKey(key);
        String validation = validate(normalized, source, size, position);
        if (validation != null) return SpawnResult.failure(validation);
        if (HologramLookup.exists(store, normalized)) return SpawnResult.failure("Ya existe un holograma con la clave " + normalized + ".");

        ImageHologramComponent component = new ImageHologramComponent(normalized, source, size, billboard);
        ImageAssetRenderer.RenderedAnimation animation = null;
        com.hypixel.hytale.server.core.asset.type.model.config.Model visibleModel = null;
        boolean externalImage = source != null && !source.trim().toLowerCase(Locale.ROOT).startsWith("builtin:");
        if (externalImage) visibleModel = ParticleHologramAssets.invisibleModel();
        if (!externalImage || visibleModel == null) {
            animation = ImageAssetRenderer.create(source, size);
            if (!animation.success()) return SpawnResult.failure(animation.error());
            component.setAnimation(animation);
            visibleModel = animation.frame(0);
        }
        Rotation3f rotation = new Rotation3f(0, yaw, 0);
        Holder<EntityStore> holder = store.getRegistry().newHolder();
        holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(position, rotation));
        holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
        holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(visibleModel));
        holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(new com.hypixel.hytale.server.core.asset.type.model.config.Model.ModelReference(TextHologramService.SELECTION_MODEL, 1.0f, null, true)));
        holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(visibleModel.getBoundingBox()));
        holder.addComponent(MovementStatesComponent.getComponentType(), new MovementStatesComponent());
        holder.addComponent(Intangible.getComponentType(), Intangible.INSTANCE);
        holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(rotation));
        holder.addComponent(PropComponent.getComponentType(), PropComponent.get());
        holder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(1.0f));
        holder.addComponent(ImageHologramComponent.getComponentType(), component);
        holder.ensureComponent(UUIDComponent.getComponentType());
        Ref<EntityStore> created = store.addEntity(holder, AddReason.SPAWN);
        return created == null || !created.isValid() ? SpawnResult.failure("Hytale no pudo crear la entidad del holograma.") : SpawnResult.success(normalized);
    }

    public static List<LoadedHologram> list(Store<EntityStore> store) {
        List<LoadedHologram> result = new ArrayList<>();
        store.forEachChunk(ImageHologramComponent.getComponentType(), (chunk, commands) -> {
            for (int index = 0; index < chunk.size(); index++) {
                ImageHologramComponent component = chunk.getComponent(index, ImageHologramComponent.getComponentType());
                TransformComponent transform = chunk.getComponent(index, TransformComponent.getComponentType());
                if (component == null || transform == null) continue;
                Vector3d position = transform.getPosition();
                result.add(new LoadedHologram(component.getId(), component.getSource(), component.getSize(),
                    component.getBillboardMode(), position.x, position.y, position.z));
            }
        });
        return result;
    }

    public static LoadedHologram find(Store<EntityStore> store, String key) {
        String normalized = TextHologramService.normalizeKey(key);
        for (LoadedHologram hologram : list(store)) if (hologram.id().equals(normalized)) return hologram;
        return null;
    }

    public static String setScale(Store<EntityStore> store, String key, float size) {
        if (!Float.isFinite(size) || size < 0.1f || size > 32.0f) return "La escala debe estar entre 0.1 y 32.";
        return update(store, key, component -> component.setSize(size));
    }

    public static String setBillboard(Store<EntityStore> store, String key, BillboardMode mode) {
        return update(store, key, component -> component.setBillboardMode(mode));
    }

    public static String setSource(Store<EntityStore> store, String key, String source) {
        String sourceError = ImageAssetRenderer.validateSource(source);
        if (sourceError != null) return sourceError;
        return update(store, key, component -> component.setSource(source));
    }

    public static String moveHere(Store<EntityStore> store, Ref<EntityStore> player, String key) {
        TextHologramService.Placement placement = TextHologramService.placement(store, player, 1.5);
        if (placement == null) return "No se pudo obtener la posicion del jugador.";
        return HologramLookup.move(store, key, placement.position(), placement.yaw(), ImageHologramComponent.getComponentType(), ImageHologramComponent::getId);
    }

    public static int remove(Store<EntityStore> store, String key) {
        String normalized = TextHologramService.normalizeKey(key);
        int[] removed = {0};
        store.forEachChunk(ImageHologramComponent.getComponentType(), (chunk, commands) -> {
            for (int index = 0; index < chunk.size(); index++) {
                ImageHologramComponent component = chunk.getComponent(index, ImageHologramComponent.getComponentType());
                if (component != null && component.getId().equalsIgnoreCase(normalized)) {
                    commands.removeEntity(chunk.getReferenceTo(index), RemoveReason.REMOVE);
                    removed[0]++;
                }
            }
        });
        return removed[0];
    }

    private static String update(Store<EntityStore> store, String key, Consumer<ImageHologramComponent> edit) {
        String normalized = TextHologramService.normalizeKey(key);
        String[] error = {"No existe un holograma de imagen con la clave " + normalized + "."};
        store.forEachChunk(ImageHologramComponent.getComponentType(), (chunk, commands) -> {
            for (int index = 0; index < chunk.size(); index++) {
                ImageHologramComponent component = chunk.getComponent(index, ImageHologramComponent.getComponentType());
                if (component == null || !component.getId().equalsIgnoreCase(normalized)) continue;
                ImageHologramComponent copy = component.copy();
                edit.accept(copy);
                commands.putComponent(chunk.getReferenceTo(index), ImageHologramComponent.getComponentType(), copy);
                error[0] = null;
            }
        });
        return error[0];
    }

    private static String validate(String key, String source, float size, Vector3d position) {
        if (!KEY.matcher(key).matches()) return "La clave debe tener 1-32 caracteres: a-z, 0-9, _ o -.";
        if (source == null || source.isBlank()) return "La fuente de imagen no puede estar vacia.";
        if (!Float.isFinite(size) || size < 0.1f || size > 32.0f) return "El tamano debe estar entre 0.1 y 32 bloques.";
        if (position == null || !Double.isFinite(position.x) || !Double.isFinite(position.y) || !Double.isFinite(position.z)) return "Las coordenadas no son validas.";
        return null;
    }

    private static String nextKey(Store<EntityStore> store) {
        for (int index = 1; index < 10000; index++) {
            String key = "image" + index;
            if (!HologramLookup.exists(store, key)) return key;
        }
        return "image" + System.currentTimeMillis();
    }

    public record SpawnResult(boolean success, String id, String error) {
        static SpawnResult success(String id) { return new SpawnResult(true, id, ""); }
        static SpawnResult failure(String error) { return new SpawnResult(false, "", error); }
    }

    public record LoadedHologram(String id, String source, float size, BillboardMode billboard,
                                 double x, double y, double z) {
        public String imageKey() { return source; }
    }
}
