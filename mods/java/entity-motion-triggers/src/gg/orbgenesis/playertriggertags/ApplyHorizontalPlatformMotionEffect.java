package gg.orbgenesis.playertriggertags;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.NPCMarkerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PropComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashSet;
import java.util.Set;

public class ApplyHorizontalPlatformMotionEffect extends TriggerEffect {
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
          .append(
              new KeyedCodec<>("OnlyFirstMatch", Codec.BOOLEAN, false),
              (effect, value) -> effect.onlyFirstMatch = value,
              effect -> effect.onlyFirstMatch)
          .add()
          .build();

  private Double targetX;
  private Double targetY;
  private Double targetZ;
  private double speed = 1.0D;
  private double speedY;
  private double speedZ;
  private boolean loopBack;
  private boolean onlyFirstMatch;

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
        if (!matchesTarget(store, candidate, origin, shape)) {
          continue;
        }

        TransformComponent transform =
            store.getComponent(candidate, TransformComponent.getComponentType());
        var start = transform.getPosition();
        double distanceX = targetX - start.x;
        double distanceY = targetY - start.y;
        double distanceZ = targetZ - start.z;
        if (distanceX == 0.0D && distanceY == 0.0D && distanceZ == 0.0D) {
          continue;
        }
        if (isUnreachable(distanceX, speed)
            || isUnreachable(distanceY, speedY)
            || isUnreachable(distanceZ, speedZ)) {
          continue;
        }
        store.putComponent(
            candidate,
            HorizontalMovingPlatformComponent.getComponentType(),
            new HorizontalMovingPlatformComponent(
                start.x,
                start.y,
                start.z,
                distanceX,
                distanceY,
                distanceZ,
                speed,
                speedY,
                speedZ,
                loopBack));

        if (onlyFirstMatch) {
          return;
        }
      }
    }
  }

  private static boolean isUnreachable(double distance, double axisSpeed) {
    return distance != 0.0D && axisSpeed == 0.0D;
  }

  private boolean matchesTarget(
      com.hypixel.hytale.component.Store<EntityStore> store,
      Ref<EntityStore> candidate,
      org.joml.Vector3d origin,
      com.hypixel.hytale.builtin.triggervolumes.shape.TriggerVolumeShape shape) {
    TransformComponent transform =
        store.getComponent(candidate, TransformComponent.getComponentType());
    if (transform == null || !shape.contains(origin, transform.getPosition())) {
      return false;
    }

    if (store.getComponent(candidate, PropComponent.getComponentType()) == null
        && store.getComponent(candidate, NPCMarkerComponent.getComponentType()) == null) {
      return false;
    }
    return true;
  }
}
