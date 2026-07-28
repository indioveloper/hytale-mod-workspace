package gg.orbgenesis.buildbattle;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class CreativeToolRestrictionRuleSystem
    extends EntityTickingSystem<EntityStore> {
  private final Query<EntityStore> query =
      Query.and(PlayerRef.getComponentType(), TransformComponent.getComponentType());
  private final CreativeToolRestrictionManager restrictionManager;

  public CreativeToolRestrictionRuleSystem(
      CreativeToolRestrictionManager restrictionManager) {
    this.restrictionManager = restrictionManager;
  }

  @Override
  public Query<EntityStore> getQuery() {
    return query;
  }

  @Override
  public void tick(
      float deltaTime,
      int index,
      ArchetypeChunk<EntityStore> archetypeChunk,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> commandBuffer) {
    Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
    PlayerRef playerRef =
        archetypeChunk.getComponent(index, PlayerRef.getComponentType());
    TransformComponent transform =
        archetypeChunk.getComponent(index, TransformComponent.getComponentType());
    if (playerRef == null || transform == null) {
      return;
    }

    TriggerVolumeManager volumeManager =
        store.getResource(TriggerVolumesPlugin.get().getManagerResourceType());
    boolean isRestricted =
        volumeManager != null
            && volumeManager.hasActiveRule(
                transform.getPosition(), RestrictBuildBattleCreativeToolsRule.class);

    if (isRestricted) {
      restrictionManager.refresh(playerRef, entityRef, store);
    } else {
      restrictionManager.restore(playerRef.getUuid());
    }
  }
}
