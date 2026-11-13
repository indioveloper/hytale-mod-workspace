package gg.raynormods.holograms;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
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

public final class TextHologramService {
    static final String SELECTION_MODEL = "Raynor_Hologram_Invisible_Anchor";
    private static final Pattern KEY = Pattern.compile("[a-z0-9_-]{1,32}");

    private TextHologramService() {}

    public static SpawnResult spawn(Store<EntityStore> store, Ref<EntityStore> player, String text) {
        String key = nextKey(store, "text");
        Placement placement = placement(store, player, 2.0);
        if (placement == null) return SpawnResult.failure("No se pudo obtener la posicion del jugador.");
        return spawnAt(store, key, text, 1.0f, placement.position(), placement.yaw(), BillboardMode.BILLBOARD_Y, TextAlignment.CENTER);
    }

    public static SpawnResult spawn(Store<EntityStore> store, Ref<EntityStore> player, String key, String text) {
        Placement placement = placement(store, player, 2.0);
        if (placement == null) return SpawnResult.failure("No se pudo obtener la posicion del jugador.");
        return spawnAt(store, key, text, 1.0f, placement.position(), placement.yaw(), BillboardMode.BILLBOARD_Y, TextAlignment.CENTER);
    }

    public static SpawnResult spawnTemporary(Store<EntityStore> store, Ref<EntityStore> player, float duration, String text) {
        if (!Float.isFinite(duration) || duration < 1.0f || duration > 300.0f) {
            return SpawnResult.failure("La duracion debe estar entre 1 y 300 segundos.");
        }
        Placement placement = placement(store, player, 2.0);
        if (placement == null) return SpawnResult.failure("No se pudo obtener la posicion del jugador.");
        float fadeDuration = Math.min(1.5f, Math.max(0.5f, duration * 0.35f));
        return spawnAt(store, nextKey(store, "temp"), text, 1.0f, placement.position(), placement.yaw(),
            BillboardMode.BILLBOARD_Y, TextAlignment.CENTER, duration, fadeDuration);
    }

    /** Creates a short-lived rich preview at an editor-selected position. */
    static SpawnResult spawnPreview(Store<EntityStore> store, String text, float size, Vector3d position,
                                    float yaw, BillboardMode billboard, TextAlignment alignment) {
        float duration = 12.0f;
        return spawnAt(store, nextKey(store, "preview"), text, size, position, yaw, billboard, alignment,
            TextRenderMode.RICH, duration, 1.25f);
    }

    public static SpawnResult spawnAt(Store<EntityStore> store, String text, Vector3d position) {
        return spawnAt(store, nextKey(store, "text"), text, 1.0f, position, 0.0f, BillboardMode.FIXED, TextAlignment.CENTER);
    }

    public static SpawnResult spawnAt(Store<EntityStore> store, String text, float size, Vector3d position, float yaw) {
        return spawnAt(store, nextKey(store, "text"), text, size, position, yaw, BillboardMode.BILLBOARD_Y, TextAlignment.CENTER);
    }

    public static SpawnResult spawnAt(Store<EntityStore> store, String key, String text, float size, Vector3d position,
                                      float yaw, BillboardMode billboard, TextAlignment alignment) {
        return spawnAt(store, key, text, size, position, yaw, billboard, alignment, TextRenderMode.AUTO);
    }

    public static SpawnResult spawnAt(Store<EntityStore> store, String key, String text, float size, Vector3d position,
                                      float yaw, BillboardMode billboard, TextAlignment alignment, TextRenderMode renderMode) {
        return spawnAt(store, key, text, size, position, yaw, billboard, alignment, renderMode, 0.0f, 0.0f);
    }

    private static SpawnResult spawnAt(Store<EntityStore> store, String key, String text, float size, Vector3d position,
                                       float yaw, BillboardMode billboard, TextAlignment alignment,
                                       float lifetime, float fadeDuration) {
        return spawnAt(store, key, text, size, position, yaw, billboard, alignment,
            TextRenderMode.AUTO, lifetime, fadeDuration);
    }

    private static SpawnResult spawnAt(Store<EntityStore> store, String key, String text, float size, Vector3d position,
                                       float yaw, BillboardMode billboard, TextAlignment alignment, TextRenderMode renderMode,
                                       float lifetime, float fadeDuration) {
        String normalizedKey = normalizeKey(key);
        String validation = validate(normalizedKey, text, size, position);
        if (validation != null) return SpawnResult.failure(validation);
        if (HologramLookup.exists(store, normalizedKey)) return SpawnResult.failure("Ya existe un holograma con la clave " + normalizedKey + ".");

        String resolved = PlaceholderResolver.resolve(text, store.getExternalData().getWorld());
        TextHologramComponent component = new TextHologramComponent(
            normalizedKey, text.trim(), size, billboard, alignment, lifetime, fadeDuration);
        component.setRenderMode(renderMode == null ? TextRenderMode.AUTO : renderMode);
        TextRasterRenderer.RenderedText rendered = null;
        if (!component.usesLiveText(resolved)) {
            rendered = TextRasterRenderer.create(resolved, size, alignment);
            if (!rendered.success()) return SpawnResult.failure(rendered.error());
            component.markRendered(rendered.signature(), 10);
        }
        Rotation3f rotation = new Rotation3f(0.0f, yaw, 0.0f);
        Holder<EntityStore> holder = store.getRegistry().newHolder();
        holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(position, rotation));
        holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
        com.hypixel.hytale.server.core.asset.type.model.config.Model invisible = ParticleHologramAssets.invisibleModel();
        com.hypixel.hytale.server.core.asset.type.model.config.Model initialModel = invisible == null && rendered != null ? rendered.model() : invisible;
        if (initialModel == null) return SpawnResult.failure("No se encontro el modelo invisible del holograma.");
        holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(initialModel));
        holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(new com.hypixel.hytale.server.core.asset.type.model.config.Model.ModelReference(SELECTION_MODEL, 1.0f, null, true)));
        holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(initialModel.getBoundingBox()));
        holder.addComponent(MovementStatesComponent.getComponentType(), new MovementStatesComponent());
        holder.addComponent(Intangible.getComponentType(), Intangible.INSTANCE);
        holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(rotation));
        holder.addComponent(PropComponent.getComponentType(), PropComponent.get());
        holder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(1.0f));
        holder.addComponent(TextHologramComponent.getComponentType(), component);
        if (component.usesLiveText(resolved)) {
            String plain = RichTextParser.plainText(RichTextParser.parse(resolved));
            holder.addComponent(Nameplate.getComponentType(), new Nameplate(plain));
            component.markRendered("live:" + plain, 10);
        }
        holder.ensureComponent(UUIDComponent.getComponentType());
        Ref<EntityStore> created = store.addEntity(holder, AddReason.SPAWN);
        return created == null || !created.isValid() ? SpawnResult.failure("Hytale no pudo crear la entidad del holograma.") : SpawnResult.success(normalizedKey);
    }

    public static List<LoadedHologram> list(Store<EntityStore> store) {
        List<LoadedHologram> result = new ArrayList<>();
        store.forEachChunk(TextHologramComponent.getComponentType(), (chunk, commands) -> {
            for (int index = 0; index < chunk.size(); index++) {
                TextHologramComponent component = chunk.getComponent(index, TextHologramComponent.getComponentType());
                TransformComponent transform = chunk.getComponent(index, TransformComponent.getComponentType());
                if (component == null || transform == null) continue;
                Vector3d position = transform.getPosition();
                result.add(new LoadedHologram(component.getId(), component.getText(), component.getSize(),
                    component.getBillboardMode(), component.getAlignment(), component.getRenderMode(),
                    position.x, position.y, position.z));
            }
        });
        return result;
    }

    public static LoadedHologram find(Store<EntityStore> store, String key) {
        String normalized = normalizeKey(key);
        for (LoadedHologram hologram : list(store)) if (hologram.id().equals(normalized)) return hologram;
        return null;
    }

    public static String addLine(Store<EntityStore> store, String key, String line) {
        return update(store, key, component -> component.setText(component.getText() + "\n" + line));
    }

    public static String setLine(Store<EntityStore> store, String key, int lineIndex, String value) {
        return update(store, key, component -> {
            List<String> lines = new ArrayList<>(List.of(component.getText().split("\\R", -1)));
            if (lineIndex < 0 || lineIndex >= lines.size()) throw new EditException("Indice de linea fuera de rango.");
            lines.set(lineIndex, value);
            component.setText(String.join("\n", lines));
        });
    }

    public static String removeLine(Store<EntityStore> store, String key, int lineIndex) {
        return update(store, key, component -> {
            List<String> lines = new ArrayList<>(List.of(component.getText().split("\\R", -1)));
            if (lineIndex < 0 || lineIndex >= lines.size()) throw new EditException("Indice de linea fuera de rango.");
            lines.remove(lineIndex);
            if (lines.isEmpty()) lines.add("");
            component.setText(String.join("\n", lines));
        });
    }

    public static String setScale(Store<EntityStore> store, String key, float size) {
        if (!Float.isFinite(size) || size < 0.1f || size > 16.0f) return "La escala debe estar entre 0.1 y 16.";
        return update(store, key, component -> component.setSize(size));
    }

    public static String setBillboard(Store<EntityStore> store, String key, BillboardMode mode) {
        return update(store, key, component -> component.setBillboardMode(mode));
    }

    public static String setAlignment(Store<EntityStore> store, String key, TextAlignment alignment) {
        return update(store, key, component -> component.setAlignment(alignment));
    }

    public static String setRenderMode(Store<EntityStore> store, String key, TextRenderMode mode) {
        return update(store, key, component -> component.setRenderMode(mode));
    }

    public static String moveHere(Store<EntityStore> store, Ref<EntityStore> player, String key) {
        Placement placement = placement(store, player, 1.5);
        if (placement == null) return "No se pudo obtener la posicion del jugador.";
        return HologramLookup.move(store, key, placement.position(), placement.yaw(), TextHologramComponent.getComponentType(), TextHologramComponent::getId);
    }

    public static int remove(Store<EntityStore> store, String key) {
        String normalized = normalizeKey(key);
        int[] removed = {0};
        store.forEachChunk(TextHologramComponent.getComponentType(), (chunk, commands) -> {
            for (int index = 0; index < chunk.size(); index++) {
                TextHologramComponent component = chunk.getComponent(index, TextHologramComponent.getComponentType());
                if (component != null && component.getId().equalsIgnoreCase(normalized)) {
                    commands.removeEntity(chunk.getReferenceTo(index), RemoveReason.REMOVE);
                    removed[0]++;
                }
            }
        });
        return removed[0];
    }

    private static String update(Store<EntityStore> store, String key, Consumer<TextHologramComponent> edit) {
        String normalized = normalizeKey(key);
        String[] error = {"No existe un holograma de texto con la clave " + normalized + "."};
        store.forEachChunk(TextHologramComponent.getComponentType(), (chunk, commands) -> {
            for (int index = 0; index < chunk.size(); index++) {
                TextHologramComponent component = chunk.getComponent(index, TextHologramComponent.getComponentType());
                if (component == null || !component.getId().equalsIgnoreCase(normalized)) continue;
                TextHologramComponent copy = component.copy();
                try {
                    edit.accept(copy);
                    String validation = validate(normalized, copy.getText(), copy.getSize(), new Vector3d());
                    if (validation != null) { error[0] = validation; return; }
                    commands.putComponent(chunk.getReferenceTo(index), TextHologramComponent.getComponentType(), copy);
                    error[0] = null;
                } catch (EditException exception) {
                    error[0] = exception.getMessage();
                }
            }
        });
        return error[0];
    }

    static Placement placement(Store<EntityStore> store, Ref<EntityStore> player, double verticalOffset) {
        TransformComponent transform = store.getComponent(player, TransformComponent.getComponentType());
        if (transform == null) return null;
        HeadRotation head = store.getComponent(player, HeadRotation.getComponentType());
        Vector3d direction = head != null ? head.getDirection() : Transform.getDirection(transform.getRotation().pitch(), transform.getRotation().yaw());
        Vector3d position = new Vector3d(transform.getPosition()).add(new Vector3d(direction).mul(3.0)).add(0, verticalOffset, 0);
        float yaw = head != null ? head.getRotation().yaw() : transform.getRotation().yaw();
        return new Placement(position, yaw);
    }

    static String normalizeKey(String key) { return key == null ? "" : key.trim().toLowerCase(Locale.ROOT); }

    private static String validate(String key, String text, float size, Vector3d position) {
        if (!KEY.matcher(key).matches()) return "La clave debe tener 1-32 caracteres: a-z, 0-9, _ o -.";
        String normalizedText = text == null ? "" : text.trim();
        if (normalizedText.isEmpty()) return "El texto no puede estar vacio.";
        String textError = TextRasterRenderer.validationError(RichTextParser.parse(normalizedText), normalizedText);
        if (textError != null) return textError;
        if (!Float.isFinite(size) || size < 0.1f || size > 16.0f) return "El tamano del texto debe estar entre 0.1 y 16 bloques.";
        if (position == null || !Double.isFinite(position.x) || !Double.isFinite(position.y) || !Double.isFinite(position.z)) return "Las coordenadas no son validas.";
        return null;
    }

    private static String nextKey(Store<EntityStore> store, String prefix) {
        for (int index = 1; index < 10000; index++) {
            String key = prefix + index;
            if (!HologramLookup.exists(store, key)) return key;
        }
        return prefix + System.currentTimeMillis();
    }

    public record SpawnResult(boolean success, String id, String error) {
        static SpawnResult success(String id) { return new SpawnResult(true, id, ""); }
        static SpawnResult failure(String error) { return new SpawnResult(false, "", error); }
    }

    public record LoadedHologram(String id, String text, float size, BillboardMode billboard, TextAlignment alignment,
                                 TextRenderMode renderMode,
                                 double x, double y, double z) {}
    record Placement(Vector3d position, float yaw) {}
    private static final class EditException extends RuntimeException { EditException(String message) { super(message); } }
}
