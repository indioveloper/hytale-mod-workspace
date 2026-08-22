package gg.orbgenesis.configurablespawners;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import javax.annotation.Nullable;

final class SpawnerMobScale {
  private SpawnerMobScale() {}

  @Nullable
  static Model scaledCopy(Model source, double relativeScale) {
    if (source == null) return null;
    ModelAsset asset = ModelAsset.getAssetMap().getAsset(source.getModelAssetId());
    if (asset == null) return null;
    float finalScale = source.getScale() * (float) relativeScale;
    return Model.createScaledModel(asset, finalScale, source.getRandomAttachmentIds());
  }

  static boolean apply(
      Ref<EntityStore> ref,
      NPCEntity npc,
      double relativeScale,
      CommandBuffer<EntityStore> commandBuffer) {
    ModelComponent currentComponent = commandBuffer.getComponent(ref, ModelComponent.getComponentType());
    if (currentComponent == null || currentComponent.getModel() == null || npc.getRole() == null) {
      return false;
    }
    Model scaled = scaledCopy(currentComponent.getModel(), relativeScale);
    if (scaled == null) return false;

    npc.setInitialModelScale(scaled.getScale());
    commandBuffer.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(scaled));
    npc.getRole().updateMotionControllers(ref, scaled, scaled.getBoundingBox(), commandBuffer);

    // Older builds used this component as an extra visual-only scale. Keep it neutral
    // so upgrading an already spawned mob does not scale its rendered model twice.
    EntityScaleComponent legacyScale = commandBuffer.getComponent(
        ref, EntityScaleComponent.getComponentType());
    if (legacyScale != null) legacyScale.setScale(1.0f);
    return true;
  }
}
