package gg.raynormods.holograms;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.function.Function;

final class HologramLookup {
    private HologramLookup() {}

    static boolean exists(Store<EntityStore> store, String key) {
        String normalized = TextHologramService.normalizeKey(key);
        for (TextHologramService.LoadedHologram hologram : TextHologramService.list(store)) {
            if (hologram.id().equalsIgnoreCase(normalized)) return true;
        }
        for (ImageHologramService.LoadedHologram hologram : ImageHologramService.list(store)) {
            if (hologram.id().equalsIgnoreCase(normalized)) return true;
        }
        return false;
    }

    static <T extends Component<EntityStore>> String move(Store<EntityStore> store, String key, Vector3d position,
                                                            float yaw, ComponentType<EntityStore, T> type,
                                                            Function<T, String> id) {
        String normalized = TextHologramService.normalizeKey(key);
        boolean[] found = {false};
        store.forEachChunk(type, (chunk, commands) -> {
            for (int index = 0; index < chunk.size(); index++) {
                T component = chunk.getComponent(index, type);
                if (component == null || !id.apply(component).equalsIgnoreCase(normalized)) continue;
                TransformComponent current = chunk.getComponent(index, TransformComponent.getComponentType());
                if (current == null) continue;
                TransformComponent moved = current.clone();
                moved.teleportPosition(position);
                moved.teleportRotation(new Rotation3f(0, yaw, 0));
                commands.putComponent(chunk.getReferenceTo(index), TransformComponent.getComponentType(), moved);
                HeadRotation head = chunk.getComponent(index, HeadRotation.getComponentType());
                if (head != null) commands.putComponent(chunk.getReferenceTo(index), HeadRotation.getComponentType(), new HeadRotation(new Rotation3f(0, yaw, 0)));
                found[0] = true;
            }
        });
        return found[0] ? null : "No existe un holograma con la clave " + normalized + ".";
    }
}
