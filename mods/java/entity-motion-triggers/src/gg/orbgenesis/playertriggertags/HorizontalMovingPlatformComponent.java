package gg.orbgenesis.playertriggertags;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Rotation3f;
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
          .append(new KeyedCodec<>("Rotate", Codec.BOOLEAN, false), (c, v) -> c.rotate = v, c -> c.rotate)
          .add()
          .append(new KeyedCodec<>("StartRotationX", Codec.FLOAT, false), (c, v) -> c.startRotationX = v, c -> c.startRotationX)
          .add()
          .append(new KeyedCodec<>("StartRotationY", Codec.FLOAT, false), (c, v) -> c.startRotationY = v, c -> c.startRotationY)
          .add()
          .append(new KeyedCodec<>("StartRotationZ", Codec.FLOAT, false), (c, v) -> c.startRotationZ = v, c -> c.startRotationZ)
          .add()
          .append(new KeyedCodec<>("TargetRotationX", Codec.FLOAT, false), (c, v) -> c.targetRotationX = v, c -> c.targetRotationX)
          .add()
          .append(new KeyedCodec<>("TargetRotationY", Codec.FLOAT, false), (c, v) -> c.targetRotationY = v, c -> c.targetRotationY)
          .add()
          .append(new KeyedCodec<>("TargetRotationZ", Codec.FLOAT, false), (c, v) -> c.targetRotationZ = v, c -> c.targetRotationZ)
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
  private boolean rotate;
  private float startRotationX;
  private float startRotationY;
  private float startRotationZ;
  private float targetRotationX;
  private float targetRotationY;
  private float targetRotationZ;
  private transient double elapsedTimeSeconds;
  private transient boolean initialized;

  public static ComponentType<EntityStore, HorizontalMovingPlatformComponent> getComponentType() {
    return EntityMotionTriggersPlugin.get().getHorizontalMovingPlatformComponentType();
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
      boolean loopBack,
      Rotation3f startRotation,
      Rotation3f targetRotation,
      boolean rotate) {
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
    this.rotate = rotate;
    startRotationX = startRotation.x();
    startRotationY = startRotation.y();
    startRotationZ = startRotation.z();
    targetRotationX = targetRotation.x();
    targetRotationY = targetRotation.y();
    targetRotationZ = targetRotation.z();
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

  public double advanceAndGetProgress(float dt) {
    elapsedTimeSeconds += dt;
    double durationX = duration(amplitude, speed);
    double durationY = duration(amplitudeY, speedY);
    double durationZ = duration(amplitudeZ, speedZ);
    // One shared progress value keeps the movement on the straight line to the target.
    double forwardDuration = Math.max(durationX, Math.max(durationY, durationZ));
    if (!loopBack || forwardDuration == 0.0D) {
      return Math.min(elapsedTimeSeconds / forwardDuration, 1.0D);
    }

    double cycleTime = elapsedTimeSeconds % (forwardDuration * 2.0D);
    double progress = cycleTime <= forwardDuration
        ? cycleTime / forwardDuration
        : 2.0D - cycleTime / forwardDuration;
    return progress;
  }

  public Vector3d positionAt(double progress) {
    return new Vector3d(
        baseX + amplitude * progress,
        baseY + amplitudeY * progress,
        baseZ + amplitudeZ * progress);
  }

  public boolean rotates() {
    return rotate;
  }

  public Rotation3f rotationAt(double progress) {
    return Rotation3f.lerpAngle(
        new Rotation3f(startRotationX, startRotationY, startRotationZ),
        new Rotation3f(targetRotationX, targetRotationY, targetRotationZ),
        (float) progress);
  }

  private static double duration(double distance, double axisSpeed) {
    return distance == 0.0D ? 0.0D : Math.abs(distance / axisSpeed);
  }

  @Override
  public HorizontalMovingPlatformComponent clone() {
    return new HorizontalMovingPlatformComponent(
        baseX,
        baseY,
        baseZ,
        amplitude,
        amplitudeY,
        amplitudeZ,
        speed,
        speedY,
        speedZ,
        loopBack,
        new Rotation3f(startRotationX, startRotationY, startRotationZ),
        new Rotation3f(targetRotationX, targetRotationY, targetRotationZ),
        rotate);
  }
}
