package gg.orbgenesis.moretriggers;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.joml.Vector3d;

/** Applies every active NoMove rule to entities located inside its volume. */
final class NoMoveRuleSystem extends EntityTickingSystem<EntityStore> {
  private final Query<EntityStore> query =
      Query.and(TransformComponent.getComponentType(), Velocity.getComponentType());

  @Override
  public Query<EntityStore> getQuery() {
    return query;
  }

  @Override
  public void tick(
      float deltaTime,
      int index,
      ArchetypeChunk<EntityStore> archetypeChunk,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> commandBuffer) {
    TransformComponent transform =
        archetypeChunk.getComponent(index, TransformComponent.getComponentType());
    Velocity velocity = archetypeChunk.getComponent(index, Velocity.getComponentType());
    if (transform == null || velocity == null) {
      return;
    }

    TriggerVolumeManager manager =
        store.getResource(TriggerVolumesPlugin.get().getManagerResourceType());
    if (manager == null) {
      return;
    }

    boolean player =
        archetypeChunk.getComponent(index, PlayerRef.getComponentType()) != null;
    NPCEntity npc = archetypeChunk.getComponent(index, NPCEntity.getComponentType());
    String npcRole = npc != null ? npc.getRoleName() : null;

    for (NoMoveRule rule :
        manager.getActiveRules(transform.getPosition(), NoMoveRule.class)) {
      if (!NoMoveExceptionFilter.isExcluded(
          player, npcRole, rule.excludePlayers, rule.excludedNpcRoles)) {
        velocity.addInstruction(new Vector3d(), null, ChangeVelocityType.Set);
        return;
      }
    }
  }
}
