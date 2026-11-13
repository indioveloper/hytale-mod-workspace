package gg.raynormods.desertsurvival.vitals;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.EnvironmentSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import org.joml.Vector3d;

final class SurvivalVitalsSystem extends EntityTickingSystem<EntityStore> {
  static final String STILLSUIT_CHEST_ID = "Armor_Stillsuit_Prototype";
  static final String STILLSUIT_HEAD_ID = "Armor_Stillsuit_T1_Head_Prototype";
  static final String STILLSUIT_HANDS_ID = "Armor_Stillsuit_T1_Hands_Prototype";
  static final String STILLSUIT_LEGS_ID = "Armor_Stillsuit_T1_Legs_Prototype";

  private static final Set<String> T1_PIECES = Set.of(
      STILLSUIT_CHEST_ID, STILLSUIT_HEAD_ID, STILLSUIT_HANDS_ID, STILLSUIT_LEGS_ID);
  private static final double T1_RESISTANCE_PER_PIECE = 5.0;
  private static final double SUN_MULTIPLIER = 1.25;
  private static final double BURNING_HEAT = 6.0;
  private static final double RESISTANCE_DIVISOR = 100.0;
  private static final String HEALTH_MODIFIER = "Raynor_Hydration_MaxHealth";

  private final Query<EntityStore> query = Query.and(
      Player.getComponentType(),
      PlayerRef.getComponentType(),
      TransformComponent.getComponentType(),
      EntityStatMap.getComponentType(),
      SurvivalVitalsComponent.getComponentType());

  @Override
  public Query<EntityStore> getQuery() {
    return query;
  }

  @Override
  public Set<Dependency<EntityStore>> getDependencies() {
    return Set.of(new SystemDependency<>(
        Order.AFTER, EntityStatsModule.PlayerRegenerateStatsSystem.class));
  }

  @Override
  public boolean isParallel(int archetypeChunkSize, int taskCount) {
    return false;
  }

  @Override
  public void tick(
      float deltaSeconds,
      int index,
      ArchetypeChunk<EntityStore> chunk,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> commands) {
    SurvivalVitalsComponent vitals = chunk.getComponent(
        index, SurvivalVitalsComponent.getComponentType());
    EntityStatMap stats = chunk.getComponent(index, EntityStatMap.getComponentType());
    if (vitals == null || stats == null) {
      return;
    }

    // Health regeneration and other native healing run every server tick. Keep
    // the dehydration ceiling in the same cadence so healing cannot bypass it
    // between the one-second hydration simulation steps.
    enforceDehydrationHealthCap(stats, vitals);
    if (!vitals.advance(deltaSeconds)) {
      return;
    }

    Player player = chunk.getComponent(index, Player.getComponentType());
    PlayerRef playerRef = chunk.getComponent(index, PlayerRef.getComponentType());
    TransformComponent transform = chunk.getComponent(index, TransformComponent.getComponentType());
    if (player == null || playerRef == null || transform == null) {
      return;
    }

    Ref<EntityStore> playerEntity = chunk.getReferenceTo(index);
    vitals.migrateLegacyScale();
    Exposure exposure = sampleExposure(store, transform.getPosition());
    EquipmentProfile equipment = equipmentProfile(playerEntity, store);
    vitals.updateSunExposure(exposure.exposed());

    double heatReceived = exposure.heatTier().biomeHeat()
        * (exposure.exposed() ? SUN_MULTIPLIER : 1.0);
    if (vitals.isBurning()) {
      heatReceived += BURNING_HEAT;
    }
    double waterLossMl = Math.max(
        0.0, heatReceived - (equipment.heatResistance() / RESISTANCE_DIVISOR));
    vitals.loseWater(
        waterLossMl, equipment.recoveryRate(), equipment.hasRecoveryPocket());

    applyHydrationEffects(playerEntity, stats, vitals, commands);
    commands.putComponent(
        playerEntity, SurvivalVitalsComponent.getComponentType(), vitals.clone());
    SurvivalVitalsHud.getOrCreate(player, playerRef).render(
        vitals,
        exposure.exposed(),
        exposure.heatTier(),
        equipment,
        waterLossMl);
  }

  static EquipmentProfile equipmentProfile(Ref<EntityStore> player, Store<EntityStore> store) {
    InventoryComponent.Armor armor = store.getComponent(
        player, InventoryComponent.Armor.getComponentType());
    if (armor == null) {
      return new EquipmentProfile(0.0, 0.0, false);
    }
    double[] resistance = {0.0};
    boolean[] chest = {false};
    armor.getInventory().forEach((slot, stack) -> {
      if (stack == null) {
        return;
      }
      String itemId = stack.getItemId();
      if (T1_PIECES.contains(itemId)) {
        resistance[0] += T1_RESISTANCE_PER_PIECE;
      }
      if (STILLSUIT_CHEST_ID.equals(itemId)) {
        chest[0] = true;
      }
    });
    return new EquipmentProfile(
        resistance[0],
        chest[0] ? SurvivalVitalsComponent.STILLSUIT_T1_RECOVERY_RATE : 0.0,
        chest[0]);
  }

  static boolean isWearingStillsuitChest(Ref<EntityStore> player, Store<EntityStore> store) {
    return equipmentProfile(player, store).hasRecoveryPocket();
  }

  private static void applyHydrationEffects(
      Ref<EntityStore> player,
      EntityStatMap stats,
      SurvivalVitalsComponent vitals,
      CommandBuffer<EntityStore> commands) {
    int healthIndex = EntityStatType.getAssetMap().getIndex("Health");
    if (healthIndex >= 0 && stats.get(healthIndex) != null) {
      double hydration = vitals.getHydrationMl();
      if (hydration <= 0.0) {
        // Keep the native maximum on its normal scale so 50 HP always occupies
        // the same amount of the bar. The dehydration cap is enforced on the
        // current value and visualized by the HUD marker instead of rescaling.
        stats.removeModifier(healthIndex, HEALTH_MODIFIER);
        enforceDehydrationHealthCap(stats, vitals);
      } else if (hydration > SurvivalVitalsComponent.HIGH_HYDRATION_ML) {
        stats.putModifier(
            healthIndex,
            HEALTH_MODIFIER,
            new StaticModifier(
                Modifier.ModifierTarget.MAX,
                StaticModifier.CalculationType.MULTIPLICATIVE,
                1.2f));
      } else {
        stats.removeModifier(healthIndex, HEALTH_MODIFIER);
      }

      stats.minStatValue(healthIndex, stats.get(healthIndex).getMax());
      float regeneration = hydration > SurvivalVitalsComponent.HIGH_HYDRATION_ML
          ? 2.0f
          : hydration >= SurvivalVitalsComponent.LOW_HYDRATION_ML ? 1.0f : 0.0f;
      if (regeneration > 0.0f) {
        stats.addStatValue(healthIndex, regeneration);
      }
      if (vitals.tickDehydrationDamage()) {
        commands.invoke(
            player,
            new Damage(
                new Damage.EnvironmentSource("dehydration"),
                DamageCause.ENVIRONMENT,
                2.0f));
      }
    }

    int staminaIndex = EntityStatType.getAssetMap().getIndex("Stamina");
    if (staminaIndex >= 0 && stats.get(staminaIndex) != null) {
      float current = stats.get(staminaIndex).get();
      float limited = vitals.limitStaminaRegeneration(current);
      if (limited < current) {
        stats.setStatValue(staminaIndex, limited);
      }
    }
  }

  private static void enforceDehydrationHealthCap(
      EntityStatMap stats, SurvivalVitalsComponent vitals) {
    if (vitals.getHydrationMl() > 0.0) {
      return;
    }
    int healthIndex = EntityStatType.getAssetMap().getIndex("Health");
    if (healthIndex < 0 || stats.get(healthIndex) == null) {
      return;
    }
    float dehydrationCap = stats.get(healthIndex).getMax() * 0.5f;
    stats.minStatValue(healthIndex, dehydrationCap);
  }

  private static Exposure sampleExposure(Store<EntityStore> store, Vector3d position) {
    var world = store.getExternalData().getWorld();
    ChunkStore chunkStore = world.getChunkStore();
    int x = (int) Math.floor(position.x);
    int y = (int) Math.floor(position.y + 1.6);
    int z = (int) Math.floor(position.z);
    Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReferenceAtBlock(x, y, z);
    if (sectionRef == null || !sectionRef.isValid()) {
      return new Exposure(false, HeatTier.fromEnvironment(""));
    }

    var sectionStore = chunkStore.getStore();
    BlockSection blockSection = sectionStore.getComponent(
        sectionRef, BlockSection.getComponentType());
    EnvironmentSection environmentSection = sectionStore.getComponent(
        sectionRef, EnvironmentSection.getComponentType());
    String environmentId = "";
    if (environmentSection != null) {
      Environment environment = Environment.getAssetMap().getAsset(
          environmentSection.get(x, y, z));
      if (environment != null) {
        environmentId = environment.getId();
      }
    }

    double sunlightFactor = store.getResource(
        WorldTimeResource.getResourceType()).getSunlightFactor();
    int skyLight = blockSection != null && blockSection.hasGlobalLight()
        ? blockSection.getGlobalLight().getSkyLight(x, y, z)
        : 0;
    boolean exposed = sunlightFactor >= 0.15 && skyLight >= 14;
    return new Exposure(exposed, HeatTier.fromEnvironment(environmentId));
  }

  record EquipmentProfile(
      double heatResistance, double recoveryRate, boolean hasRecoveryPocket) {
  }

  private record Exposure(boolean exposed, HeatTier heatTier) {
  }
}
