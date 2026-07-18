package gg.orbgenesis.playertriggertags;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class PlayerTriggerTagsPlugin extends JavaPlugin {
  private static PlayerTriggerTagsPlugin instance;

  private ComponentType<EntityStore, PlayerTagsComponent> playerTagsComponentType;
  private ComponentType<EntityStore, HorizontalMovingPlatformComponent>
      horizontalMovingPlatformComponentType;
  private ComponentType<EntityStore, PendingPlatformCollisionComponent>
      pendingPlatformCollisionComponentType;

  public PlayerTriggerTagsPlugin(JavaPluginInit init) {
    super(init);
    instance = this;
  }

  public static PlayerTriggerTagsPlugin get() {
    return instance;
  }

  public ComponentType<EntityStore, PlayerTagsComponent> getPlayerTagsComponentType() {
    return playerTagsComponentType;
  }

  public ComponentType<EntityStore, HorizontalMovingPlatformComponent>
      getHorizontalMovingPlatformComponentType() {
    return horizontalMovingPlatformComponentType;
  }

  public ComponentType<EntityStore, PendingPlatformCollisionComponent>
      getPendingPlatformCollisionComponentType() {
    return pendingPlatformCollisionComponentType;
  }

  @Override
  protected void setup() {
    super.setup();

    playerTagsComponentType =
        getEntityStoreRegistry()
            .registerComponent(
                PlayerTagsComponent.class,
                "OrbGenesis_PlayerTriggerTags",
                PlayerTagsComponent.CODEC);
    horizontalMovingPlatformComponentType =
        getEntityStoreRegistry()
            .registerComponent(
                HorizontalMovingPlatformComponent.class,
                "OrbGenesis_HorizontalMovingPlatform",
                HorizontalMovingPlatformComponent.CODEC);
    pendingPlatformCollisionComponentType =
        getEntityStoreRegistry()
            .registerComponent(
                PendingPlatformCollisionComponent.class,
                "OrbGenesis_PendingPlatformCollision",
                PendingPlatformCollisionComponent.CODEC);
    getEntityStoreRegistry().registerSystem(new HorizontalMovingPlatformSystem());
    getEntityStoreRegistry().registerSystem(new PendingPlatformCollisionSystem());

    TriggerVolumesPlugin triggerVolumes = TriggerVolumesPlugin.get();
    triggerVolumes.registerEffectType(
        "ModifyPlayerTag", ModifyPlayerTagEffect.class, ModifyPlayerTagEffect.CODEC);
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
    triggerVolumes.registerAssetField("ConvertBlocksToEntities", "Item", "Item");
    triggerVolumes.registerEffectType(
        "ConvertBlocksToEntities",
        ConvertBlocksToEntitiesEffect.class,
        ConvertBlocksToEntitiesEffect.CODEC);
    triggerVolumes.registerConditionType(
        "PlayerTagCondition", PlayerTagCondition.class, PlayerTagCondition.CODEC);
  }
}
