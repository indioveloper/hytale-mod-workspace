package gg.orbgenesis.playertriggers;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class StopFollowingPlayerEffect extends TriggerEffect {
  public static final BuilderCodec<StopFollowingPlayerEffect> CODEC =
      BuilderCodec.builder(
              StopFollowingPlayerEffect.class,
              StopFollowingPlayerEffect::new,
              TriggerEffect.BASE_CODEC)
          .build();

  @Override
  public void execute(TriggerContext context) {
    PlayerRef player = PlayerTagAccess.getPlayer(context);
    VolumeEntry volume = context == null ? null : context.getVolume();
    if (player == null || volume == null) {
      return;
    }

    TriggerVolumeManager manager =
        context
            .getStore()
            .getResource(TriggerVolumesPlugin.get().getManagerResourceType());
    if (manager != null) {
      PlayerBinding.unbind(
          manager, volume, context.getEntityRef(), player.getUuid());
    }
  }
}
