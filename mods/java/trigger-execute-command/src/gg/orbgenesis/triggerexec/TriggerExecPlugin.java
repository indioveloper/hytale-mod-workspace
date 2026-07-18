package gg.orbgenesis.triggerexec;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public class TriggerExecPlugin extends JavaPlugin {
  public TriggerExecPlugin(JavaPluginInit init) {
    super(init);
  }

  @Override
  protected void setup() {
    super.setup();
    TriggerVolumesPlugin.get()
        .registerEffectType("ExecuteCommand", ExecuteCommandEffect.class, ExecuteCommandEffect.CODEC);
  }
}
