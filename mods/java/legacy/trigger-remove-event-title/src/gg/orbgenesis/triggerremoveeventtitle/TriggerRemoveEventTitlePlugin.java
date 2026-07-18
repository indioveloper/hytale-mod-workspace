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
    TriggerVolumesPlugin.get()
        .registerEffectType(
            "RemoveEventTitle", RemoveEventTitleEffect.class, RemoveEventTitleEffect.CODEC);
  }
}
