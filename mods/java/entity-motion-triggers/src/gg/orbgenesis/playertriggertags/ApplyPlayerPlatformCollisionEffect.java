package gg.orbgenesis.playertriggertags;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollision;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollisionConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashSet;
import java.util.Set;

/** Makes the entities inside the executing volume solid to players. */
public class ApplyPlayerPlatformCollisionEffect extends TriggerEffect {
  public static final BuilderCodec<ApplyPlayerPlatformCollisionEffect> CODEC =
      BuilderCodec.builder(
              ApplyPlayerPlatformCollisionEffect.class,
              ApplyPlayerPlatformCollisionEffect::new,
              TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("CollisionConfig", Codec.STRING, false),
              (effect, value) -> effect.collisionConfig = value,
              effect -> effect.collisionConfig)
          .add()
          .append(
              new KeyedCodec<>("OnlyFirstMatch", Codec.BOOLEAN, false),
              (effect, value) -> effect.onlyFirstMatch = value,
              effect -> effect.onlyFirstMatch)
          .add()
          .build();

  private String collisionConfig = "HardCollision";
  private boolean onlyFirstMatch;

  @Override
  public void execute(TriggerContext context) {
    HitboxCollisionConfig config = HitboxCollisionConfig.getAssetMap().getAsset(collisionConfig);
    if (config == null) {
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
        if (!candidate.isValid() || !seen.add(candidate) || !MotionTargeting.matches(store, candidate, origin, shape)) {
          continue;
        }

        HitboxCollision collision = store.getComponent(candidate, HitboxCollision.getComponentType());
        if (collision == null) {
          store.putComponent(candidate, HitboxCollision.getComponentType(), new HitboxCollision(config));
        } else {
          collision.setHitboxCollisionConfig(config);
        }
        if (onlyFirstMatch) {
          return;
        }
      }
    }
  }

}
