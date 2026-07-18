package gg.orbgenesis.playertriggertags;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/** Executes one linear movement order, optionally returning to its starting point forever. */
public class HorizontalMovingPlatformComponent implements Component<EntityStore> {
  public static final BuilderCodec<HorizontalMovingPlatformComponent> CODEC =
      BuilderCodec.builder(
              HorizontalMovingPlatformComponent.class,
              HorizontalMovingPlatformComponent::new)
          .append(new KeyedCodec<>("BaseX", Codec.DOUBLE, false), (c, v) -> c.baseX = v, c -> c.baseX)
          .add()
          .append(new KeyedCodec<>("BaseY", Codec.DOUBLE, false), (c, v) -> c.baseY = v, c -> c.baseY)
          .add()
          .append(new KeyedCodec<>("BaseZ", Codec.DOUBLE, false), (c, v) -> c.baseZ = v, c -> c.baseZ)
          .add()
          .append(new KeyedCodec<>("Amplitude", Codec.DOUBLE, false), (c, v) -> c.amplitude = v, c -> c.amplitude)
          .add()
          .append(new KeyedCodec<>("AmplitudeY", Codec.DOUBLE, false), (c, v) -> c.amplitudeY = v, c -> c.amplitudeY)
          .add()
          .append(new KeyedCodec<>("AmplitudeZ", Codec.DOUBLE, false), (c, v) -> c.amplitudeZ = v, c -> c.amplitudeZ)
          .add()
          .append(new KeyedCodec<>("Speed", Codec.DOUBLE, false), (c, v) -> c.speed = v, c -> c.speed)
          .add()
          .append(new KeyedCodec<>("SpeedY", Codec.DOUBLE, false), (c, v) -> c.speedY = v, c -> c.speedY)
          .add()
          .append(new KeyedCodec<>("SpeedZ", Codec.DOUBLE, false), (c, v) -> c.speedZ = v, c -> c.speedZ)
          .add()
          .append(new KeyedCodec<>("LoopBack", Codec.BOOLEAN, false), (c, v) -> c.loopBack = v, c -> c.loopBack)
          .add()
          .build();

  private double baseX;
  private double baseY;
  private double baseZ;
  private double amplitude = 3.0D;
  private double amplitudeY;
  private double amplitudeZ;
  private double speed = 1.0D;
  private double speedY;
  private double speedZ;
  private boolean loopBack;
  private transient double elapsedTimeSeconds;
  private transient boolean initialized;

  public static ComponentType<EntityStore, HorizontalMovingPlatformComponent> getComponentType() {
    return PlayerTriggerTagsPlugin.get().getHorizontalMovingPlatformComponentType();
  }

  public HorizontalMovingPlatformComponent() {}

  public HorizontalMovingPlatformComponent(
      double baseX,
      double baseY,
      double baseZ,
      double amplitude,
      double amplitudeY,
      double amplitudeZ,
      double speed,
      double speedY,
      double speedZ,
      boolean loopBack) {
    this.baseX = baseX;
    this.baseY = baseY;
    this.baseZ = baseZ;
    this.amplitude = amplitude;
    this.amplitudeY = amplitudeY;
    this.amplitudeZ = amplitudeZ;
    this.speed = speed;
    this.speedY = speedY;
    this.speedZ = speedZ;
    this.loopBack = loopBack;
    initialized = true;
  }

  public void initializeMissingBases(Vector3d current) {
    if (!initialized) {
      // Components saved by older versions only stored BaseX.
      baseY = current.y;
      baseZ = current.z;
      initialized = true;
    }
  }

  public Vector3d advanceAndGetCurrentPosition(float dt) {
    elapsedTimeSeconds += dt;
    double durationX = duration(amplitude, speed);
    double durationY = duration(amplitudeY, speedY);
    double durationZ = duration(amplitudeZ, speedZ);
    // One shared progress value keeps the movement on the straight line to the target.
    double forwardDuration = Math.max(durationX, Math.max(durationY, durationZ));
    if (!loopBack || forwardDuration == 0.0D) {
      return positionAt(Math.min(elapsedTimeSeconds / forwardDuration, 1.0D));
    }

    double cycleTime = elapsedTimeSeconds % (forwardDuration * 2.0D);
    double progress = cycleTime <= forwardDuration
        ? cycleTime / forwardDuration
        : 2.0D - cycleTime / forwardDuration;
    return positionAt(progress);
  }

  private Vector3d positionAt(double progress) {
    return new Vector3d(
        baseX + amplitude * progress,
        baseY + amplitudeY * progress,
        baseZ + amplitudeZ * progress);
  }

  private static double duration(double distance, double axisSpeed) {
    return distance == 0.0D ? 0.0D : Math.abs(distance / axisSpeed);
  }

  @Override
  public HorizontalMovingPlatformComponent clone() {
    return new HorizontalMovingPlatformComponent(
        baseX, baseY, baseZ, amplitude, amplitudeY, amplitudeZ, speed, speedY, speedZ, loopBack);
  }
}
