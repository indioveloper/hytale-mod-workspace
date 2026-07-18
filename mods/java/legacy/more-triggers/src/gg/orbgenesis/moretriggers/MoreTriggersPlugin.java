package gg.orbgenesis.moretriggers;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public class MoreTriggersPlugin extends JavaPlugin {
  public MoreTriggersPlugin(JavaPluginInit init) {
    super(init);
  }

  @Override
  public void setup() {
    TriggerVolumesPlugin triggerVolumes = TriggerVolumesPlugin.get();
    triggerVolumes.registerEffectType(
        "RemoveEventTitle", RemoveEventTitleEffect.class, RemoveEventTitleEffect.CODEC);
    triggerVolumes.registerEffectType(
        "RandomTagSelection", RandomTagSelectionEffect.class, RandomTagSelectionEffect.CODEC);
    triggerVolumes.registerEffectType(
        "PasteRandomPrefab", PasteRandomPrefabEffect.class, PasteRandomPrefabEffect.CODEC);
    triggerVolumes.registerAssetField("PasteRandomPrefab", "Prefab1", "Prefab");
    triggerVolumes.registerAssetField("PasteRandomPrefab", "Prefab2", "Prefab");
    triggerVolumes.registerAssetField("PasteRandomPrefab", "Prefabs", "Prefab");
  }
}
