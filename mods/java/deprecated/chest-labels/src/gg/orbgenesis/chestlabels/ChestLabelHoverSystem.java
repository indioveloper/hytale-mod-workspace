package gg.orbgenesis.chestlabels;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChestLabelHoverSystem extends EntityTickingSystem<EntityStore> {
  private final Query<EntityStore> query =
      Query.and(Player.getComponentType(), PlayerRef.getComponentType());

  private final ComponentType<ChunkStore, ChestLabelComponent> chestLabelComponentType;
  private final Map<UUID, HoverState> lastStateByPlayer = new ConcurrentHashMap<>();

  public ChestLabelHoverSystem(ComponentType<ChunkStore, ChestLabelComponent> chestLabelComponentType) {
    this.chestLabelComponentType = chestLabelComponentType;
  }

  @Override
  public Query<EntityStore> getQuery() {
    return query;
  }

  @Override
  public void tick(
      float dt,
      int index,
      ArchetypeChunk<EntityStore> archetypeChunk,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> commandBuffer) {
    Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
    Player player = archetypeChunk.getComponent(index, Player.getComponentType());
    PlayerRef playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
    if (player == null || playerRef == null) {
      return;
    }

    World world = store.getExternalData().getWorld();
    Ref<ChunkStore> targetRef = ChestLabelTargeting.getTargetContainerRef(entityRef, world, store);
    if (targetRef == null) {
      hideIfNeeded(player, playerRef);
      return;
    }

    ChestLabelComponent label = targetRef.getStore().getComponent(targetRef, chestLabelComponentType);
    boolean hasLabel = label != null && !label.isEmpty();

    HoverState next =
        new HoverState(
            ChestLabelTargeting.toKey(targetRef),
            hasLabel ? label.getName() : "",
            hasLabel ? ChestIconRegistry.normalizeKey(label.getIcon()) : ChestIconRegistry.DEFAULT_ICON_KEY,
            hasLabel);
    HoverState previous = lastStateByPlayer.get(playerRef.getUuid());
    if (next.equals(previous)) {
      return;
    }

    ChestLabelHoverHud.getOrCreate(player, playerRef).showForChest(next.name, next.iconKey, next.hasLabel);
    lastStateByPlayer.put(playerRef.getUuid(), next);
  }

  private void hideIfNeeded(Player player, PlayerRef playerRef) {
    HoverState removed = lastStateByPlayer.remove(playerRef.getUuid());
    if (removed == null) {
      return;
    }

    ChestLabelHoverHud.getOrCreate(player, playerRef).hideLabel();
  }

  private static final class HoverState {
    private final String targetKey;
    private final String name;
    private final String iconKey;
    private final boolean hasLabel;

    private HoverState(String targetKey, String name, String iconKey, boolean hasLabel) {
      this.targetKey = targetKey;
      this.name = name;
      this.iconKey = iconKey;
      this.hasLabel = hasLabel;
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof HoverState state)) {
        return false;
      }
      return targetKey.equals(state.targetKey)
          && name.equals(state.name)
          && iconKey.equals(state.iconKey)
          && hasLabel == state.hasLabel;
    }

    @Override
    public int hashCode() {
      int result = targetKey.hashCode();
      result = 31 * result + name.hashCode();
      result = 31 * result + iconKey.hashCode();
      result = 31 * result + Boolean.hashCode(hasLabel);
      return result;
    }
  }
}
