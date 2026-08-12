package gg.orbgenesis.configurablespawners;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.RoleUtils;
import com.hypixel.hytale.server.npc.systems.BalancingInitialisationSystem;
import java.util.Set;
import javax.annotation.Nonnull;

final class SpawnerMobSetupSystem extends EntityTickingSystem<EntityStore> {
  private static final String HEALTH_ID = "Health";
  private static final String HEALTH_MODIFIER = "OrbGenesis_Spawner_Max";
  private final Query<EntityStore> query = Query.and(
      SpawnedBySpawnerComponent.getComponentType(), NPCEntity.getComponentType());

  @Override
  @Nonnull
  public Query<EntityStore> getQuery() {
    return query;
  }

  @Override
  @Nonnull
  public Set<Dependency<EntityStore>> getDependencies() {
    return Set.of(new SystemDependency<>(Order.AFTER, BalancingInitialisationSystem.class));
  }

  @Override
  public void tick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer) {
    SpawnedBySpawnerComponent tracker = archetypeChunk.getComponent(
        index, SpawnedBySpawnerComponent.getComponentType());
    NPCEntity npc = archetypeChunk.getComponent(index, NPCEntity.getComponentType());
    if (tracker == null || npc == null || tracker.equipmentApplied || npc.getRole() == null) return;
    if (!tracker.heldItemId.isBlank() && Item.getAssetMap().getAsset(tracker.heldItemId) != null) {
      RoleUtils.setItemInHand(archetypeChunk.getReferenceTo(index), npc, tracker.heldItemId, store);
    }
    if (tracker.maxHealth > 0.0) {
      EntityStatMap stats = archetypeChunk.getComponent(index, EntityStatMap.getComponentType());
      int healthIndex = EntityStatType.getAssetMap().getIndex(HEALTH_ID);
      if (stats != null && healthIndex >= 0 && stats.get(healthIndex) != null) {
        float difference = (float) tracker.maxHealth - stats.get(healthIndex).getMax();
        stats.putModifier(healthIndex, HEALTH_MODIFIER,
            new StaticModifier(Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE, difference));
        stats.maximizeStatValue(healthIndex);
      }
    }
    var ref = archetypeChunk.getReferenceTo(index);
    EntityScaleComponent scale = store.getComponent(ref, EntityScaleComponent.getComponentType());
    if (scale == null) {
      store.putComponent(ref, EntityScaleComponent.getComponentType(),
          new EntityScaleComponent((float) tracker.mobScale));
    } else {
      scale.setScale((float) tracker.mobScale);
    }
    if (tracker.customArmor) {
      applyArmor(archetypeChunk.getReferenceTo(index), npc, tracker.armorHeadId, store);
      applyArmor(archetypeChunk.getReferenceTo(index), npc, tracker.armorChestId, store);
      applyArmor(archetypeChunk.getReferenceTo(index), npc, tracker.armorHandsId, store);
      applyArmor(archetypeChunk.getReferenceTo(index), npc, tracker.armorLegsId, store);
    }
    tracker.equipmentApplied = true;
  }

  private static void applyArmor(
      com.hypixel.hytale.component.Ref<EntityStore> ref,
      NPCEntity npc,
      String itemId,
      Store<EntityStore> store) {
    if (itemId.isBlank()) return;
    Item item = Item.getAssetMap().getAsset(itemId);
    if (item != null && item.getArmor() != null) {
      RoleUtils.setArmor(ref, npc, itemId, store);
    }
  }
}
