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
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.RoleUtils;
import com.hypixel.hytale.server.npc.role.support.DisplayNameSupport;
import com.hypixel.hytale.server.npc.systems.BalancingInitialisationSystem;
import java.util.Set;
import javax.annotation.Nonnull;

final class SpawnerMobSetupSystem extends EntityTickingSystem<EntityStore> {
  private static final String HEALTH_ID = "Health";
  private static final String HEALTH_MODIFIER = "OrbGenesis_Spawner_Max";
  private static final String ELITE_PRESENTATION_EFFECT = "OrbGenesis_Spawner_Elite";
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
    if (!tracker.mobName.isBlank()) {
      DisplayNameSupport.setDisplayName(
          archetypeChunk.getReferenceTo(index), tracker.mobName, true, commandBuffer);
    }
    if (!tracker.heldItemId.isBlank() && Item.getAssetMap().getAsset(tracker.heldItemId) != null) {
      RoleUtils.setItemInHand(
          archetypeChunk.getReferenceTo(index), npc, tracker.heldItemId, commandBuffer);
    }
    if (tracker.maxHealth > 0.0 || tracker.healthMultiplier != 1.0) {
      EntityStatMap stats = archetypeChunk.getComponent(index, EntityStatMap.getComponentType());
      int healthIndex = EntityStatType.getAssetMap().getIndex(HEALTH_ID);
      if (stats != null && healthIndex >= 0 && stats.get(healthIndex) != null) {
        float naturalMaximum = stats.get(healthIndex).getMax();
        double baseMaximum = tracker.maxHealth > 0.0 ? tracker.maxHealth : naturalMaximum;
        double desiredMaximum = Math.min(100000.0, baseMaximum * tracker.healthMultiplier);
        float difference = (float) desiredMaximum - naturalMaximum;
        stats.putModifier(healthIndex, HEALTH_MODIFIER,
            new StaticModifier(Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.ADDITIVE, difference));
        stats.maximizeStatValue(healthIndex);
      }
    }
    var ref = archetypeChunk.getReferenceTo(index);
    if (!tracker.physicalScaleApplied
        && SpawnerMobScale.apply(ref, npc, tracker.mobScale, commandBuffer)) {
      tracker.physicalScaleApplied = true;
    }
    EffectControllerComponent effectController = null;
    if (speedEffectRequired(tracker.mobSpeed) || tracker.elite) {
      effectController = commandBuffer.getComponent(
          ref, EffectControllerComponent.getComponentType());
      if (effectController == null) {
        effectController = new EffectControllerComponent();
        commandBuffer.putComponent(
            ref, EffectControllerComponent.getComponentType(), effectController);
      }
    }
    applySpeed(ref, npc, tracker.mobSpeed, effectController, commandBuffer);
    if (tracker.elite) {
      applyInfiniteEffect(ref, ELITE_PRESENTATION_EFFECT, effectController, commandBuffer);
    }
    if (tracker.customArmor) {
      applyArmor(archetypeChunk.getReferenceTo(index), npc, tracker.armorHeadId, commandBuffer);
      applyArmor(archetypeChunk.getReferenceTo(index), npc, tracker.armorChestId, commandBuffer);
      applyArmor(archetypeChunk.getReferenceTo(index), npc, tracker.armorHandsId, commandBuffer);
      applyArmor(archetypeChunk.getReferenceTo(index), npc, tracker.armorLegsId, commandBuffer);
    }
    tracker.equipmentApplied = true;
  }

  private static void applySpeed(
      com.hypixel.hytale.component.Ref<EntityStore> ref,
      NPCEntity npc,
      double multiplier,
      EffectControllerComponent controller,
      CommandBuffer<EntityStore> commandBuffer) {
    int tenths = Math.max(0, Math.min(30, (int) Math.round(multiplier * 10.0)));
    if (tenths == 10) return;
    String effectId = String.format(java.util.Locale.ROOT,
        "OrbGenesis_SpawnerSpeed_%03d", tenths);
    if (applyInfiniteEffect(ref, effectId, controller, commandBuffer)) {
      npc.invalidateCachedHorizontalSpeedMultiplier();
    }
  }

  private static boolean speedEffectRequired(double multiplier) {
    return Math.max(0, Math.min(30, (int) Math.round(multiplier * 10.0))) != 10;
  }

  private static boolean applyInfiniteEffect(
      com.hypixel.hytale.component.Ref<EntityStore> ref,
      String effectId,
      EffectControllerComponent controller,
      CommandBuffer<EntityStore> commandBuffer) {
    int effectIndex = EntityEffect.getAssetMap().getIndex(effectId);
    EntityEffect effect = effectIndex < 0 ? null : EntityEffect.getAssetMap().getAsset(effectIndex);
    if (effect == null || controller == null) return false;
    controller.addInfiniteEffect(ref, effectIndex, effect, commandBuffer);
    return true;
  }

  private static void applyArmor(
      com.hypixel.hytale.component.Ref<EntityStore> ref,
      NPCEntity npc,
      String itemId,
      CommandBuffer<EntityStore> commandBuffer) {
    if (itemId.isBlank()) return;
    Item item = Item.getAssetMap().getAsset(itemId);
    if (item != null && item.getArmor() != null) {
      RoleUtils.setArmor(ref, npc, itemId, commandBuffer);
    }
  }
}
