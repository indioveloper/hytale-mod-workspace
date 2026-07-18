package gg.orbgenesis.triggersnpcs;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class HorizontalMovingPlatformComponent implements Component<EntityStore> {
  public static final BuilderCodec<HorizontalMovingPlatformComponent> CODEC =
      BuilderCodec.builder(
              HorizontalMovingPlatformComponent.class,
              HorizontalMovingPlatformComponent::new)
          .append(
              new KeyedCodec<>("BaseX", Codec.DOUBLE, false),
              (component, value) -> component.baseX = value,
              component -> component.baseX)
          .add()
          .append(
              new KeyedCodec<>("Amplitude", Codec.DOUBLE, false),
              (component, value) -> component.amplitude = value,
              component -> component.amplitude)
          .add()
          .append(
              new KeyedCodec<>("Speed", Codec.DOUBLE, false),
              (component, value) -> component.speed = value,
              component -> component.speed)
          .add()
          .append(
              new KeyedCodec<>("Phase", Codec.DOUBLE, false),
              (component, value) -> component.phase = value,
              component -> component.phase)
          .add()
          .build();

  private double baseX;
  private double amplitude = 3.0D;
  private double speed = 1.0D;
  private double phase;
  private transient double elapsedTimeSeconds;

  public static ComponentType<EntityStore, HorizontalMovingPlatformComponent>
      getComponentType() {
    return TriggersNpcsPlugin.get().getHorizontalMovingPlatformComponentType();
  }

  public HorizontalMovingPlatformComponent() {}

  public HorizontalMovingPlatformComponent(
      double baseX, double amplitude, double speed, double phase) {
    this.baseX = baseX;
    this.amplitude = amplitude;
    this.speed = speed;
    this.phase = phase;
  }

  public double getBaseX() {
    return baseX;
  }

  public double getAmplitude() {
    return amplitude;
  }

  public double getSpeed() {
    return speed;
  }

  public double getPhase() {
    return phase;
  }

  public double advanceAndGetCurrentX(float dt) {
    elapsedTimeSeconds += dt;
    return baseX + Math.sin(elapsedTimeSeconds * speed + phase) * amplitude;
  }

  @Override
  public HorizontalMovingPlatformComponent clone() {
    return new HorizontalMovingPlatformComponent(baseX, amplitude, speed, phase);
  }
}
