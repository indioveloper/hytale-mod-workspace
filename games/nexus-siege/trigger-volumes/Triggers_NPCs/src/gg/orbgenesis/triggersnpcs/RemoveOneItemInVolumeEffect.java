package gg.orbgenesis.triggersnpcs;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RemoveOneItemInVolumeEffect extends TriggerEffect {
  public static final BuilderCodec<RemoveOneItemInVolumeEffect> CODEC =
      BuilderCodec.builder(
              RemoveOneItemInVolumeEffect.class,
              RemoveOneItemInVolumeEffect::new,
              TriggerEffect.BASE_CODEC)
          .append(new KeyedCodec<>("Item", Codec.STRING), (effect, value) -> effect.itemId = value, effect -> effect.itemId)
          .add()
          .build();

  private String itemId;

  @Override
  public void execute(TriggerContext context) {
    if (itemId == null || itemId.isBlank()) {
      return;
    }

    var store = context.getStore();
    var spatial = store.getResource(EntityModule.get().getEntitySpatialResourceType());
    if (spatial == null) {
      return;
    }

    List<Ref<EntityStore>> candidates = new ArrayList<>();
    var results = SpatialResource.<EntityStore>getThreadLocalReferenceList();
    for (var volume : context.getSpatialVolumes()) {
      results.clear();
      var shape = volume.getShape();
      var origin = volume.getPosition();
      spatial.getSpatialStructure().collect(origin, shape.getMaxDistanceFromOrigin(), results);
      for (var candidate : results) {
        if (!candidate.isValid() || candidates.contains(candidate)) {
          continue;
        }

        var itemComponent = store.getComponent(candidate, ItemComponent.getComponentType());
        if (itemComponent == null || itemComponent.getItemStack() == null
            || !itemId.equals(itemComponent.getItemStack().getItemId())) {
          continue;
        }

        var transform = store.getComponent(candidate, TransformComponent.getComponentType());
        if (transform != null && shape.contains(origin, transform.getPosition())) {
          candidates.add(candidate);
        }
      }
    }

    if (!candidates.isEmpty()) {
      store.removeEntity(candidates.get(ThreadLocalRandom.current().nextInt(candidates.size())), RemoveReason.REMOVE);
    }
  }
}
