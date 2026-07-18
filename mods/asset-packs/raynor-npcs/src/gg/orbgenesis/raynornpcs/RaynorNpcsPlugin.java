package gg.orbgenesis.raynornpcs;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public class RaynorNpcsPlugin extends JavaPlugin {
  public RaynorNpcsPlugin(JavaPluginInit init) {
    super(init);
  }

  @Override
  protected void setup() {
    super.setup();

    TriggerVolumesPlugin triggerVolumes = TriggerVolumesPlugin.get();
    triggerVolumes.registerEffectType(
        "SpawnNpcRandomModel", SpawnNpcRandomModelEffect.class, SpawnNpcRandomModelEffect.CODEC);
    triggerVolumes.registerEffectType(
        "EquipNpcItem", EquipNpcItemEffect.class, EquipNpcItemEffect.CODEC);
    triggerVolumes.registerEffectType(
        "RemoveOneItemInVolume",
        RemoveOneItemInVolumeEffect.class,
        RemoveOneItemInVolumeEffect.CODEC);
    triggerVolumes.registerAssetField("SpawnNpcRandomModel", "NpcType", "NpcRole");
    triggerVolumes.registerAssetField("EquipNpcItem", "Item", "Item");
    triggerVolumes.registerAssetField("RemoveOneItemInVolume", "Item", "Item");
  }
}
