package gg.orbgenesis.particleshapevfx;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public class ParticleShapeVfxPlugin extends JavaPlugin {
  public ParticleShapeVfxPlugin(JavaPluginInit init) {
    super(init);
  }

  @Override
  protected void setup() {
    super.setup();
    TriggerVolumesPlugin triggerVolumes = TriggerVolumesPlugin.get();
    triggerVolumes.registerEffectType(
        "SpawnParticleShape", SpawnParticleShapeEffect.class, SpawnParticleShapeEffect.CODEC);
    triggerVolumes.registerAssetField(
        "SpawnParticleShape", "ParticleSystem", "ParticleSystem");
  }
}
