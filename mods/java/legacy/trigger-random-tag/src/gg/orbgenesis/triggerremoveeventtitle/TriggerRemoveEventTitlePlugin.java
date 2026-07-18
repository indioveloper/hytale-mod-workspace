package gg.orbgenesis.triggerremoveeventtitle;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public class TriggerRemoveEventTitlePlugin extends JavaPlugin {
  public TriggerRemoveEventTitlePlugin(JavaPluginInit init) {
    super(init);
  }

  @Override
  public void setup() {
    TriggerVolumesPlugin triggerVolumes = TriggerVolumesPlugin.get();
    triggerVolumes.registerEffectType(
        "RemoveEventTitle", RemoveEventTitleEffect.class, RemoveEventTitleEffect.CODEC);
    triggerVolumes.registerEffectType(
        "RandomTagSelection", RandomTagSelectionEffect.class, RandomTagSelectionEffect.CODEC);
  }
}
