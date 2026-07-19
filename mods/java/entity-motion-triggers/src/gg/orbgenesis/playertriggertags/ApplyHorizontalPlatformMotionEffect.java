package gg.orbgenesis.playertriggertags;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashSet;
import java.util.Set;

public class ApplyHorizontalPlatformMotionEffect extends TriggerEffect {
  public enum CoordinateMode {
    ABSOLUTE,
    RELATIVE
  }

  public enum TurnDirection {
    NONE,
    RIGHT,
    LEFT
  }

  public static final BuilderCodec<ApplyHorizontalPlatformMotionEffect> CODEC =
      BuilderCodec.builder(
              ApplyHorizontalPlatformMotionEffect.class,
              ApplyHorizontalPlatformMotionEffect::new,
              TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("TargetX", Codec.DOUBLE, false),
              (effect, value) -> effect.targetX = value,
              effect -> effect.targetX)
          .add()
          .append(
              new KeyedCodec<>("TargetY", Codec.DOUBLE, false),
              (effect, value) -> effect.targetY = value,
              effect -> effect.targetY)
          .add()
          .append(
              new KeyedCodec<>("TargetZ", Codec.DOUBLE, false),
              (effect, value) -> effect.targetZ = value,
              effect -> effect.targetZ)
          .add()
          .append(
              new KeyedCodec<>("CoordinateMode", new EnumCodec<>(CoordinateMode.class), false),
              (effect, value) -> effect.coordinateMode = value != null ? value : CoordinateMode.ABSOLUTE,
              effect -> effect.coordinateMode)
          .add()
          .append(
              new KeyedCodec<>("TurnDirection", new EnumCodec<>(TurnDirection.class), false),
              (effect, value) -> effect.turnDirection = value != null ? value : TurnDirection.NONE,
              effect -> effect.turnDirection)
          .add()
          .append(
              new KeyedCodec<>("TurnAngle", Codec.FLOAT, false),
              (effect, value) -> effect.turnAngle = value,
              effect -> effect.turnAngle)
          .add()
          .append(
              new KeyedCodec<>("Speed", Codec.DOUBLE, false),
              (effect, value) -> effect.speed = value,
              effect -> effect.speed)
          .add()
          .append(
              new KeyedCodec<>("SpeedY", Codec.DOUBLE, false),
              (effect, value) -> effect.speedY = value,
              effect -> effect.speedY)
          .add()
          .append(
              new KeyedCodec<>("SpeedZ", Codec.DOUBLE, false),
              (effect, value) -> effect.speedZ = value,
              effect -> effect.speedZ)
          .add()
          .append(
              new KeyedCodec<>("LoopBack", Codec.BOOLEAN, false),
              (effect, value) -> effect.loopBack = value,
              effect -> effect.loopBack)
          .add()
          .build();

  private Double targetX;
  private Double targetY;
  private Double targetZ;
  private CoordinateMode coordinateMode = CoordinateMode.ABSOLUTE;
  private TurnDirection turnDirection = TurnDirection.NONE;
  private float turnAngle;
  private double speed = 1.0D;
  private double speedY;
  private double speedZ;
  private boolean loopBack;

  @Override
  public void execute(TriggerContext context) {
    if (targetX == null || targetY == null || targetZ == null) {
      return;
    }

    var store = context.getStore();
    var spatial = store.getResource(EntityModule.get().getEntitySpatialResourceType());
    if (spatial == null) {
      return;
    }

    Set<Ref<EntityStore>> seen = new HashSet<>();
    var results = SpatialResource.<EntityStore>getThreadLocalReferenceList();

    for (var volume : context.getSpatialVolumes()) {
      results.clear();
      var shape = volume.getShape();
      var origin = volume.getPosition();
      spatial.getSpatialStructure().collect(origin, shape.getMaxDistanceFromOrigin(), results);
      for (var candidate : results) {
        if (!candidate.isValid() || !seen.add(candidate)) {
          continue;
        }
        if (!MotionTargeting.matches(store, candidate, origin, shape)) {
          continue;
        }

        TransformComponent transform =
            store.getComponent(candidate, TransformComponent.getComponentType());
        var start = transform.getPosition();
        double distanceX = coordinateMode == CoordinateMode.RELATIVE ? targetX : targetX - start.x;
        double distanceY = coordinateMode == CoordinateMode.RELATIVE ? targetY : targetY - start.y;
        double distanceZ = coordinateMode == CoordinateMode.RELATIVE ? targetZ : targetZ - start.z;
        // A zero speed means that axis is intentionally held at its current position.
        double movementX = speed == 0.0D ? 0.0D : distanceX;
        double movementY = speedY == 0.0D ? 0.0D : distanceY;
        double movementZ = speedZ == 0.0D ? 0.0D : distanceZ;
        if (movementX == 0.0D && movementY == 0.0D && movementZ == 0.0D) {
          continue;
        }
        float yawTurn = switch (turnDirection) {
          case RIGHT -> Math.min(Math.max(turnAngle, 0.0F), 180.0F);
          case LEFT -> -Math.min(Math.max(turnAngle, 0.0F), 180.0F);
          case NONE -> 0.0F;
        };
        Rotation3f startRotation = new Rotation3f(transform.getRotation());
        Rotation3f targetRotation = new Rotation3f(startRotation);
        targetRotation.addYaw(yawTurn);
        store.putComponent(
            candidate,
            HorizontalMovingPlatformComponent.getComponentType(),
            new HorizontalMovingPlatformComponent(
                start.x,
                start.y,
                start.z,
                movementX,
                movementY,
                movementZ,
                speed,
                speedY,
                speedZ,
                loopBack,
                startRotation,
                targetRotation,
                yawTurn != 0.0F));
      }
    }
  }

}
