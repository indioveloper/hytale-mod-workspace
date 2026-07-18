package gg.orbgenesis.playertriggers;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Vector3d;

public class FollowTriggeringPlayerEffect extends TriggerEffect {
  public static final BuilderCodec<FollowTriggeringPlayerEffect> CODEC =
      BuilderCodec.builder(
              FollowTriggeringPlayerEffect.class,
              FollowTriggeringPlayerEffect::new,
              TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("OffsetX", Codec.DOUBLE, false),
              (effect, value) -> effect.offsetX = value,
              effect -> effect.offsetX)
          .add()
          .append(
              new KeyedCodec<>("OffsetY", Codec.DOUBLE, false),
              (effect, value) -> effect.offsetY = value,
              effect -> effect.offsetY)
          .add()
          .append(
              new KeyedCodec<>("OffsetZ", Codec.DOUBLE, false),
              (effect, value) -> effect.offsetZ = value,
              effect -> effect.offsetZ)
          .add()
          .append(
              new KeyedCodec<>("StopFollowingOnExit", Codec.BOOLEAN, false),
              (effect, value) -> effect.stopFollowingOnExit = value,
              effect -> effect.stopFollowingOnExit)
          .add()
          .build();

  private double offsetX;
  private double offsetY;
  private double offsetZ;
  private boolean stopFollowingOnExit;
  private final transient Map<UUID, BindingState> bindings = new ConcurrentHashMap<>();

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
    if (manager == null) {
      return;
    }

    Vector3d offset = new Vector3d(offsetX, offsetY, offsetZ);
    PlayerBinding.bind(manager, volume, player.getUuid(), offset, context.getEntityRef());
    if (stopFollowingOnExit) {
      bindings.put(
          player.getUuid(),
          new BindingState(manager, volume, context.getEntityRef()));
    } else {
      bindings.remove(player.getUuid());
    }

    TransformComponent transform =
        context
            .getStore()
            .getComponent(context.getEntityRef(), TransformComponent.getComponentType());
    if (transform != null) {
      volume.setPosition(new Vector3d(transform.getPosition()).add(offset));
      manager.markSpatialDirty();
      manager.notifyViewers();
    }
  }

  @Override
  public void onEntityExit(UUID playerId) {
    if (!stopFollowingOnExit || playerId == null) {
      return;
    }

    BindingState binding = bindings.remove(playerId);
    if (binding != null) {
      PlayerBinding.unbindIfBoundTo(
          binding.manager, binding.volume, binding.source, playerId);
    }
  }

  private static final class BindingState {
    private final TriggerVolumeManager manager;
    private final VolumeEntry volume;
    private final Ref<EntityStore> source;

    private BindingState(
        TriggerVolumeManager manager,
        VolumeEntry volume,
        Ref<EntityStore> source) {
      this.manager = manager;
      this.volume = volume;
      this.source = source;
    }
  }
}
