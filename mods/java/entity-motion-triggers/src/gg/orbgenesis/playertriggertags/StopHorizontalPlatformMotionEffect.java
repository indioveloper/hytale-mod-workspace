package gg.orbgenesis.playertriggertags;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashSet;
import java.util.Set;

public class StopHorizontalPlatformMotionEffect extends TriggerEffect {
  public static final BuilderCodec<StopHorizontalPlatformMotionEffect> CODEC =
      BuilderCodec.builder(
              StopHorizontalPlatformMotionEffect.class,
              StopHorizontalPlatformMotionEffect::new,
              TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("OnlyFirstMatch", Codec.BOOLEAN, false),
              (effect, value) -> effect.onlyFirstMatch = value,
              effect -> effect.onlyFirstMatch)
          .add()
          .build();

  private boolean onlyFirstMatch;

  @Override
  public void execute(TriggerContext context) {
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

        store.tryRemoveComponent(candidate, HorizontalMovingPlatformComponent.getComponentType());
        if (onlyFirstMatch) {
          return;
        }
      }
    }
  }

}
