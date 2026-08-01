package gg.orbgenesis.moretriggers;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.joml.Vector3d;

/** Retired effect registration; Always Active accepts TriggerRule implementations instead. */
public class NoMoveEffect extends TriggerEffect {
  public static final BuilderCodec<NoMoveEffect> CODEC =
      BuilderCodec.builder(NoMoveEffect.class, NoMoveEffect::new, TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("ExcludePlayers", Codec.BOOLEAN, false),
              (effect, value) -> effect.excludePlayers = value,
              effect -> effect.excludePlayers)
          .add()
          .append(
              new KeyedCodec<>("ExcludedNpcRoles", Codec.STRING_ARRAY, false),
              (effect, value) ->
                  effect.excludedNpcRoles = value != null ? value : new String[0],
              effect -> effect.excludedNpcRoles)
          .add()
          .build();

  public boolean excludePlayers;
  public String[] excludedNpcRoles = new String[0];

  @Override
  public void execute(TriggerContext context) {
    if (context == null || context.getStore() == null) {
      return;
    }

    Ref<EntityStore> entityRef = context.getEntityRef();
    if (entityRef == null || !entityRef.isValid()) {
      return;
    }

    Store<EntityStore> store = context.getStore();
    boolean player = store.getComponent(entityRef, PlayerRef.getComponentType()) != null;
    NPCEntity npc = store.getComponent(entityRef, NPCEntity.getComponentType());
    String npcRole = npc != null ? npc.getRoleName() : null;
    if (NoMoveExceptionFilter.isExcluded(
        player, npcRole, excludePlayers, excludedNpcRoles)) {
      return;
    }

    Velocity velocity = store.getComponent(entityRef, Velocity.getComponentType());
    if (velocity != null) {
      velocity.addInstruction(new Vector3d(), null, ChangeVelocityType.Set);
    }
  }
}
