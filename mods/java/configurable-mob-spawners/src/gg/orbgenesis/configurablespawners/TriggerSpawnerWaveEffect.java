package gg.orbgenesis.configurablespawners;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.universe.world.World;

/** Starts one immediate wave on each loaded spawner bearing the configured tag. */
public final class TriggerSpawnerWaveEffect extends TriggerEffect {
  public static final BuilderCodec<TriggerSpawnerWaveEffect> CODEC =
      BuilderCodec.builder(
              TriggerSpawnerWaveEffect.class, TriggerSpawnerWaveEffect::new, TriggerEffect.BASE_CODEC)
          .append(new KeyedCodec<>("Tag", Codec.STRING), (effect, value) -> effect.tag = value,
              effect -> effect.tag)
          .add()
          .build();

  private String tag = "";

  @Override
  public void execute(TriggerContext context) {
    if (context == null || context.getStore() == null || tag == null || tag.isBlank()) return;
    World world = context.getStore().getExternalData().getWorld();
    ConfigurableMobSpawnersPlugin.getWaveActivationManager().trigger(world, tag.trim());
  }
}
