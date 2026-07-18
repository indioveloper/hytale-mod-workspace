package gg.orbgenesis.triggersnpcs;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class TriggersNpcsPlugin extends JavaPlugin {
  private static TriggersNpcsPlugin instance;

  private ComponentType<EntityStore, HorizontalMovingPlatformComponent>
      horizontalMovingPlatformComponentType;

  public TriggersNpcsPlugin(JavaPluginInit init) {
    super(init);
    instance = this;
  }

  public static TriggersNpcsPlugin get() {
    return instance;
  }

  public ComponentType<EntityStore, HorizontalMovingPlatformComponent>
      getHorizontalMovingPlatformComponentType() {
    return horizontalMovingPlatformComponentType;
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

    TriggerVolumesPlugin triggerVolumes = TriggerVolumesPlugin.get();
    triggerVolumes.registerEffectType(
        "SpawnNpcRandomModel", SpawnNpcRandomModelEffect.class, SpawnNpcRandomModelEffect.CODEC);
    triggerVolumes.registerEffectType("EquipNpcItem", EquipNpcItemEffect.class, EquipNpcItemEffect.CODEC);
    triggerVolumes.registerEffectType(
        "RemoveOneItemInVolume", RemoveOneItemInVolumeEffect.class, RemoveOneItemInVolumeEffect.CODEC);
    triggerVolumes.registerAssetField("SpawnNpcRandomModel", "NpcType", "NpcRole");
    triggerVolumes.registerAssetField("EquipNpcItem", "Item", "Item");
    triggerVolumes.registerAssetField("RemoveOneItemInVolume", "Item", "Item");
  }
}
