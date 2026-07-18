package gg.orbgenesis.playertriggertags;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/** Delays collision until a newly spawned prop has finished its network setup. */
public class PendingPlatformCollisionComponent implements Component<EntityStore> {
  public static final BuilderCodec<PendingPlatformCollisionComponent> CODEC =
      BuilderCodec.builder(
              PendingPlatformCollisionComponent.class,
              PendingPlatformCollisionComponent::new)
          .append(
              new KeyedCodec<>("Config", Codec.STRING),
              (component, value) -> component.configId = value,
              component -> component.configId)
          .add()
          .append(
              new KeyedCodec<>("Ticks", Codec.INTEGER, false),
              (component, value) -> component.ticksRemaining = value,
              component -> component.ticksRemaining)
          .add()
          .build();

  private String configId = "HardCollision";
  private int ticksRemaining = 2;

  public PendingPlatformCollisionComponent() {}

  public PendingPlatformCollisionComponent(String configId, int ticksRemaining) {
    this.configId = configId;
    this.ticksRemaining = ticksRemaining;
  }

  public static ComponentType<EntityStore, PendingPlatformCollisionComponent> getComponentType() {
    return PlayerTriggerTagsPlugin.get().getPendingPlatformCollisionComponentType();
  }

  public String getConfigId() {
    return configId;
  }

  public boolean tickAndIsReady() {
    return --ticksRemaining <= 0;
  }

  @Override
  public PendingPlatformCollisionComponent clone() {
    return new PendingPlatformCollisionComponent(configId, ticksRemaining);
  }
}
