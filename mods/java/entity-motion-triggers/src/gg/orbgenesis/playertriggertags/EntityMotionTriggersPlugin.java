package gg.orbgenesis.playertriggertags;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class EntityMotionTriggersPlugin extends JavaPlugin {
  private static EntityMotionTriggersPlugin instance;

  private ComponentType<EntityStore, HorizontalMovingPlatformComponent>
      horizontalMovingPlatformComponentType;
  private ComponentType<EntityStore, PendingSpawnItemCollisionComponent>
      pendingSpawnItemCollisionComponentType;
  private ComponentType<EntityStore, PendingPlatformCollisionComponent>
      pendingPlatformCollisionComponentType;

  public EntityMotionTriggersPlugin(JavaPluginInit init) {
    super(init);
    instance = this;
  }

  public static EntityMotionTriggersPlugin get() {
    return instance;
  }

  public ComponentType<EntityStore, HorizontalMovingPlatformComponent>
      getHorizontalMovingPlatformComponentType() {
    return horizontalMovingPlatformComponentType;
  }

  public ComponentType<EntityStore, PendingSpawnItemCollisionComponent>
      getPendingSpawnItemCollisionComponentType() {
    return pendingSpawnItemCollisionComponentType;
  }

  public ComponentType<EntityStore, PendingPlatformCollisionComponent>
      getPendingPlatformCollisionComponentType() {
    return pendingPlatformCollisionComponentType;
  }

  @Override
  protected void setup() {
    super.setup();

    horizontalMovingPlatformComponentType =
        getEntityStoreRegistry()
            .registerComponent(
                HorizontalMovingPlatformComponent.class,
                "OrbGenesis_HorizontalMovingPlatform",
                HorizontalMovingPlatformComponent.CODEC);
    getEntityStoreRegistry().registerSystem(new HorizontalMovingPlatformSystem());
    pendingSpawnItemCollisionComponentType =
        getEntityStoreRegistry()
            .registerComponent(
                PendingSpawnItemCollisionComponent.class,
                "OrbGenesis_PendingSpawnItemCollision",
                PendingSpawnItemCollisionComponent.CODEC);
    getEntityStoreRegistry().registerSystem(new PendingSpawnItemCollisionSystem());
    pendingPlatformCollisionComponentType =
        getEntityStoreRegistry()
            .registerComponent(
                PendingPlatformCollisionComponent.class,
                "OrbGenesis_PendingPlatformCollision",
                PendingPlatformCollisionComponent.CODEC);
    getEntityStoreRegistry().registerSystem(new PendingPlatformCollisionSystem());

    TriggerVolumesPlugin triggerVolumes = TriggerVolumesPlugin.get();
    triggerVolumes.registerEffectType(
        "ApplyHorizontalPlatformMotion",
        ApplyHorizontalPlatformMotionEffect.class,
        ApplyHorizontalPlatformMotionEffect.CODEC);
    triggerVolumes.registerEffectType(
        "StopHorizontalPlatformMotion",
        StopHorizontalPlatformMotionEffect.class,
        StopHorizontalPlatformMotionEffect.CODEC);
    triggerVolumes.registerEffectType(
        "ApplyPlayerPlatformCollision",
        ApplyPlayerPlatformCollisionEffect.class,
        ApplyPlayerPlatformCollisionEffect.CODEC);
    triggerVolumes.registerEffectType(
        "RemovePlayerPlatformCollision",
        RemovePlayerPlatformCollisionEffect.class,
        RemovePlayerPlatformCollisionEffect.CODEC);
    triggerVolumes.registerEffectType(
        "SpawnItems", SpawnItemsEffect.class, SpawnItemsEffect.CODEC);
    triggerVolumes.registerAssetField("SpawnItems", "Item", "Item");
    triggerVolumes.registerEffectType(
        "ConvertBlocksToEntities",
        ConvertBlocksToEntitiesEffect.class,
        ConvertBlocksToEntitiesEffect.CODEC);
    triggerVolumes.registerAssetField("ConvertBlocksToEntities", "Item", "Item");
    triggerVolumes.registerEffectType(
        "AttachMovingParticles",
        AttachMovingParticlesEffect.class,
        AttachMovingParticlesEffect.CODEC);
    triggerVolumes.registerAssetField(
        "AttachMovingParticles", "ParticleSystem", "ParticleSystem");
  }
}
