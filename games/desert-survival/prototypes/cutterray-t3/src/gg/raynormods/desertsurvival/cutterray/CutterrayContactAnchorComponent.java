package gg.raynormods.desertsurvival.cutterray;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/** Keeps the continuous beam sound and contact light alive while use is held. */
public final class CutterrayContactAnchorComponent implements Component<EntityStore> {
  public static final BuilderCodec<CutterrayContactAnchorComponent> CODEC =
      BuilderCodec.builder(
              CutterrayContactAnchorComponent.class,
              CutterrayContactAnchorComponent::new)
          .append(
              new KeyedCodec<>("Ticks", Codec.INTEGER, false),
              (component, value) -> component.ticksRemaining = value,
              component -> component.ticksRemaining)
          .add()
          .build();

  private static ComponentType<EntityStore, CutterrayContactAnchorComponent> componentType;
  private int ticksRemaining;

  public CutterrayContactAnchorComponent() {
  }

  public CutterrayContactAnchorComponent(int ticksRemaining) {
    this.ticksRemaining = ticksRemaining;
  }

  static void setComponentType(
      ComponentType<EntityStore, CutterrayContactAnchorComponent> value) {
    componentType = value;
  }

  public static ComponentType<EntityStore, CutterrayContactAnchorComponent> getComponentType() {
    return componentType;
  }

  boolean tickAndIsExpired() {
    return --ticksRemaining <= 0;
  }

  @Override
  public CutterrayContactAnchorComponent clone() {
    return new CutterrayContactAnchorComponent(ticksRemaining);
  }
}
