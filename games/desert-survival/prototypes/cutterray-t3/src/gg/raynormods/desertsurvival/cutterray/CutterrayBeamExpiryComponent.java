package gg.raynormods.desertsurvival.cutterray;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class CutterrayBeamExpiryComponent implements Component<EntityStore> {
  public static final BuilderCodec<CutterrayBeamExpiryComponent> CODEC =
      BuilderCodec.builder(
              CutterrayBeamExpiryComponent.class,
              CutterrayBeamExpiryComponent::new)
          .append(
              new KeyedCodec<>("Ticks", Codec.INTEGER, false),
              (component, value) -> component.ticksRemaining = value,
              component -> component.ticksRemaining)
          .add()
          .build();

  private static ComponentType<EntityStore, CutterrayBeamExpiryComponent> componentType;
  private int ticksRemaining;

  public CutterrayBeamExpiryComponent() {
  }

  public CutterrayBeamExpiryComponent(int ticksRemaining) {
    this.ticksRemaining = ticksRemaining;
  }

  static void setComponentType(
      ComponentType<EntityStore, CutterrayBeamExpiryComponent> value) {
    componentType = value;
  }

  public static ComponentType<EntityStore, CutterrayBeamExpiryComponent> getComponentType() {
    return componentType;
  }

  boolean tickAndIsExpired() {
    return --ticksRemaining <= 0;
  }

  @Override
  public CutterrayBeamExpiryComponent clone() {
    return new CutterrayBeamExpiryComponent(ticksRemaining);
  }
}
