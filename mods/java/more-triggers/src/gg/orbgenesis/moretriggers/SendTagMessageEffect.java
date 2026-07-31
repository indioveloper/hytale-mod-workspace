package gg.orbgenesis.moretriggers;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerVolumeEntityQuery;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialData;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3d;

public class SendTagMessageEffect extends TriggerEffect {
  public enum Recipient {
    TRIGGERING_PLAYER,
    NEAREST_PLAYER,
    PLAYERS_IN_VOLUME,
    ALL_PLAYERS
  }

  public static final BuilderCodec<SendTagMessageEffect> CODEC =
      BuilderCodec.builder(
              SendTagMessageEffect.class, SendTagMessageEffect::new, TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("Message", Codec.STRING),
              (effect, value) -> effect.message = value,
              effect -> effect.message)
          .add()
          .append(
              new KeyedCodec<>("Recipient", new EnumCodec<>(Recipient.class), false),
              (effect, value) ->
                  effect.recipient = value != null ? value : Recipient.TRIGGERING_PLAYER,
              effect -> effect.recipient)
          .add()
          .append(
              new KeyedCodec<>("TagSource", new EnumCodec<>(TagSource.class), false),
              (effect, value) ->
                  effect.tagSource = value != null ? value : TagSource.SELF,
              effect -> effect.tagSource)
          .add()
          .append(
              new KeyedCodec<>("TagRadius", Codec.DOUBLE, false),
              (effect, value) -> effect.tagRadius = value,
              effect -> effect.tagRadius)
          .add()
          .build();

  public String message = "";
  public Recipient recipient = Recipient.TRIGGERING_PLAYER;
  public TagSource tagSource = TagSource.SELF;
  public double tagRadius = 50.0D;

  @Override
  public void execute(TriggerContext context) {
    if (context == null || context.getStore() == null || message == null) {
      return;
    }

    Message resolvedMessage =
        Message.raw(TagTemplateResolver.resolve(context, message, tagSource, tagRadius));
    for (PlayerRef player : resolveRecipients(context)) {
      player.sendMessage(resolvedMessage);
    }
  }

  private List<PlayerRef> resolveRecipients(TriggerContext context) {
    Store<EntityStore> store = context.getStore();
    return switch (recipient) {
      case TRIGGERING_PLAYER -> {
        PlayerRef player = playerRefOf(store, context.getEntityRef());
        yield player != null ? List.of(player) : List.of();
      }
      case NEAREST_PLAYER -> nearestPlayer(store, context.getVolume().getPosition());
      case PLAYERS_IN_VOLUME -> playersInVolumes(store, context.getSpatialVolumes());
      case ALL_PLAYERS -> allPlayers(store);
    };
  }

  private static List<PlayerRef> nearestPlayer(Store<EntityStore> store, Vector3d origin) {
    SpatialResource<Ref<EntityStore>, EntityStore> spatial =
        store.getResource(EntityModule.get().getPlayerSpatialResourceType());
    if (spatial == null) {
      return List.of();
    }

    SpatialData<Ref<EntityStore>> data = spatial.getSpatialData();
    PlayerRef nearest = null;
    double nearestDistance = Double.MAX_VALUE;
    for (int i = 0; i < data.size(); i++) {
      double distance = data.getVector(i).distanceSquared(origin);
      if (distance < nearestDistance) {
        PlayerRef candidate = playerRefOf(store, data.getData(i));
        if (candidate != null) {
          nearest = candidate;
          nearestDistance = distance;
        }
      }
    }
    return nearest != null ? List.of(nearest) : List.of();
  }

  private static List<PlayerRef> playersInVolumes(
      Store<EntityStore> store,
      List<com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry> volumes) {
    List<Ref<EntityStore>> refs =
        TriggerVolumeEntityQuery.collectTargets(store, volumes, false, true, "");
    List<PlayerRef> players = new ArrayList<>(refs.size());
    for (Ref<EntityStore> ref : refs) {
      PlayerRef player = playerRefOf(store, ref);
      if (player != null) {
        players.add(player);
      }
    }
    return players;
  }

  private static List<PlayerRef> allPlayers(Store<EntityStore> store) {
    SpatialResource<Ref<EntityStore>, EntityStore> spatial =
        store.getResource(EntityModule.get().getPlayerSpatialResourceType());
    if (spatial == null) {
      return List.of();
    }

    SpatialData<Ref<EntityStore>> data = spatial.getSpatialData();
    List<PlayerRef> players = new ArrayList<>(data.size());
    for (int i = 0; i < data.size(); i++) {
      PlayerRef player = playerRefOf(store, data.getData(i));
      if (player != null) {
        players.add(player);
      }
    }
    return players;
  }

  private static PlayerRef playerRefOf(Store<EntityStore> store, Ref<EntityStore> ref) {
    if (ref == null || !ref.isValid()) {
      return null;
    }
    return store.getComponent(ref, PlayerRef.getComponentType());
  }
}
