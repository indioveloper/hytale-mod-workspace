package gg.orbgenesis.raynornpcs;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.common.util.RandomUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.modules.entity.player.ApplyRandomSkinPersistedComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.flock.FlockPlugin;
import com.hypixel.hytale.server.flock.config.FlockAsset;
import com.hypixel.hytale.server.npc.NPCPlugin;
import org.joml.Vector3d;

public class SpawnNpcRandomModelEffect extends TriggerEffect {
  private static final int MAX_COUNT = 64;

  public enum Origin {
    VOLUME_ORIGIN,
    ENTITY,
    WORLD_ABSOLUTE
  }

  public static final BuilderCodec<SpawnNpcRandomModelEffect> CODEC =
      BuilderCodec.builder(
              SpawnNpcRandomModelEffect.class, SpawnNpcRandomModelEffect::new, TriggerEffect.BASE_CODEC)
          .append(new KeyedCodec<>("NpcType", Codec.STRING), (effect, value) -> effect.npcType = value, effect -> effect.npcType)
          .add()
          .append(new KeyedCodec<>("GroupType", Codec.STRING, false), (effect, value) -> effect.groupType = value, effect -> effect.groupType)
          .add()
          .append(new KeyedCodec<>("Origin", new EnumCodec<>(Origin.class), false), (effect, value) -> effect.origin = value, effect -> effect.origin)
          .add()
          .append(new KeyedCodec<>("Offset", Vector3dUtil.CODEC, false), (effect, value) -> effect.offset = value, effect -> effect.offset)
          .add()
          .append(new KeyedCodec<>("Count", Codec.INTEGER, false), (effect, value) -> effect.count = value, effect -> effect.count)
          .add()
          .append(new KeyedCodec<>("Yaw", Codec.FLOAT, false), (effect, value) -> effect.yaw = value, effect -> effect.yaw)
          .add()
          .build();

  private String npcType;
  private String groupType;
  private Origin origin = Origin.VOLUME_ORIGIN;
  private Vector3d offset = new Vector3d();
  private int count = 1;
  private float yaw;

  @Override
  public void execute(TriggerContext context) {
    if (npcType == null || npcType.isBlank()) {
      return;
    }

    var store = context.getStore();
    var npcPlugin = NPCPlugin.get();
    int roleIndex = npcPlugin.getIndex(npcType);
    if (roleIndex < 0) {
      return;
    }

    var position = resolvePosition(context);
    var rotation = new Rotation3f(0f, (float) Math.toRadians(yaw), 0f);
    var flockDefinition = groupType == null || groupType.isBlank() ? null : FlockAsset.getAssetMap().getAsset(groupType);

    for (int spawned = 0; spawned < Math.min(Math.max(count, 1), MAX_COUNT); spawned++) {
      var skin = CosmeticsModule.get().generateRandomSkin(RandomUtil.getSecureRandom());
      Model model = CosmeticsModule.get().createModel(skin);
      var spawnedNpc = npcPlugin.spawnEntity(
          store,
          roleIndex,
          position,
          rotation,
          model,
          (npc, ref, entityStore) -> {
            entityStore.putComponent(ref, PlayerSkinComponent.getComponentType(), new PlayerSkinComponent(skin));
            entityStore.putComponent(
                ref,
                ApplyRandomSkinPersistedComponent.getComponentType(),
                ApplyRandomSkinPersistedComponent.INSTANCE);
          });
      if (spawnedNpc == null) {
        continue;
      }

      FlockPlugin.trySpawnFlock(
          spawnedNpc.first(),
          spawnedNpc.second(),
          store,
          roleIndex,
          position,
          rotation,
          flockDefinition,
          null);
    }
  }

  private Vector3d resolvePosition(TriggerContext context) {
    return switch (origin) {
      case WORLD_ABSOLUTE -> new Vector3d(offset);
      case ENTITY -> {
        var actorPosition = context.getActorPosition();
        var base = actorPosition != null ? actorPosition : new Vector3d(context.getVolume().getPosition());
        yield base.add(offset);
      }
      case VOLUME_ORIGIN -> new Vector3d(context.getVolume().getPosition()).add(offset);
    };
  }
}
