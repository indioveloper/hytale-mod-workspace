package gg.orbgenesis.configurablespawners;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class SpawnerRetaliationSystem extends DamageEventSystem {
  @Override
  @Nonnull
  public Query<EntityStore> getQuery() {
    return SpawnedBySpawnerComponent.getComponentType();
  }

  @Override
  @Nullable
  public SystemGroup<EntityStore> getGroup() {
    return DamageModule.get().getInspectDamageGroup();
  }

  @Override
  public void handle(
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull Damage damage) {
    SpawnedBySpawnerComponent tracker = archetypeChunk.getComponent(
        index, SpawnedBySpawnerComponent.getComponentType());
    if (tracker == null || tracker.aggressionMode != AggressionMode.RETALIATE) return;
    if (!(damage.getSource() instanceof Damage.EntitySource source) || !source.getRef().isValid()) return;
    if (commandBuffer.getComponent(source.getRef(), Player.getComponentType()) == null) return;
    UUIDComponent uuid = commandBuffer.getComponent(source.getRef(), UUIDComponent.getComponentType());
    if (uuid != null) tracker.retaliationTarget = uuid.getUuid();
  }
}
