package gg.orbgenesis.playertriggertags;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.EntityPart;
import com.hypixel.hytale.protocol.ModelParticle;
import com.hypixel.hytale.protocol.packets.entities.SpawnModelParticles;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.joml.Vector3f;

/**
 * Starts a client-side particle emitter attached to every matching entity.
 *
 * <p>The emitter follows the entity automatically through its network id. When
 * {@code detachedFromModel} is enabled, the client leaves the entire particle system at its
 * initial world position.
 */
public class AttachMovingParticlesEffect extends TriggerEffect {
  private static final double DEFAULT_VIEW_RANGE = 75.0D;

  public static final BuilderCodec<AttachMovingParticlesEffect> CODEC =
      BuilderCodec.builder(
              AttachMovingParticlesEffect.class,
              AttachMovingParticlesEffect::new,
              TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("ParticleSystem", Codec.STRING, false),
              (effect, value) -> effect.particleSystem = value,
              effect -> effect.particleSystem)
          .add()
          .append(
              new KeyedCodec<>("OffsetX", Codec.FLOAT, false),
              (effect, value) -> effect.offsetX = value,
              effect -> effect.offsetX)
          .add()
          .append(
              new KeyedCodec<>("OffsetY", Codec.FLOAT, false),
              (effect, value) -> effect.offsetY = value,
              effect -> effect.offsetY)
          .add()
          .append(
              new KeyedCodec<>("OffsetZ", Codec.FLOAT, false),
              (effect, value) -> effect.offsetZ = value,
              effect -> effect.offsetZ)
          .add()
          .append(
              new KeyedCodec<>("RotationX", Codec.FLOAT, false),
              (effect, value) -> effect.rotationX = value,
              effect -> effect.rotationX)
          .add()
          .append(
              new KeyedCodec<>("RotationY", Codec.FLOAT, false),
              (effect, value) -> effect.rotationY = value,
              effect -> effect.rotationY)
          .add()
          .append(
              new KeyedCodec<>("RotationZ", Codec.FLOAT, false),
              (effect, value) -> effect.rotationZ = value,
              effect -> effect.rotationZ)
          .add()
          .append(
              new KeyedCodec<>("Scale", Codec.FLOAT, false),
              (effect, value) -> effect.scale = value,
              effect -> effect.scale)
          .add()
          .append(
              new KeyedCodec<>("TargetNodeName", Codec.STRING, false),
              (effect, value) -> effect.targetNodeName = value,
              effect -> effect.targetNodeName)
          .add()
          .append(
              new KeyedCodec<>("DetachEmittedParticles", Codec.BOOLEAN, false),
              (effect, value) -> effect.detachEmittedParticles = value,
              effect -> effect.detachEmittedParticles)
          .add()
          .append(
              new KeyedCodec<>("ClearOnEntityRemove", Codec.BOOLEAN, false),
              (effect, value) -> effect.clearOnEntityRemove = value,
              effect -> effect.clearOnEntityRemove)
          .add()
          .append(
              new KeyedCodec<>("OnlyFirstMatch", Codec.BOOLEAN, false),
              (effect, value) -> effect.onlyFirstMatch = value,
              effect -> effect.onlyFirstMatch)
          .add()
          .build();

  private String particleSystem;
  private float offsetX;
  private float offsetY;
  private float offsetZ;
  private float rotationX;
  private float rotationY;
  private float rotationZ;
  private float scale = 1.0F;
  private String targetNodeName;
  private boolean detachEmittedParticles;
  private boolean clearOnEntityRemove = true;
  private boolean onlyFirstMatch;

  @Override
  public void execute(TriggerContext context) {
    if (particleSystem == null || particleSystem.isBlank()) {
      log(Level.WARNING, "AttachMovingParticles skipped: ParticleSystem is empty.");
      return;
    }
    if (ParticleSystem.getAssetMap().getAsset(particleSystem) == null) {
      log(
          Level.WARNING,
          "AttachMovingParticles skipped: particle system '%s' does not exist.",
          particleSystem);
      return;
    }

    var store = context.getStore();
    var entitySpatial = store.getResource(EntityModule.get().getEntitySpatialResourceType());
    var playerSpatial = store.getResource(EntityModule.get().getPlayerSpatialResourceType());
    if (entitySpatial == null || playerSpatial == null) {
      log(Level.WARNING, "AttachMovingParticles skipped: spatial resources are unavailable.");
      return;
    }

    String nodeName =
        targetNodeName == null || targetNodeName.isBlank() ? null : targetNodeName.trim();
    Direction rotationOffset =
        rotationX == 0.0F && rotationY == 0.0F && rotationZ == 0.0F
            ? null
            // ModelParticle rotation offsets use asset-style degrees, unlike
            // world SpawnParticleSystem packets, whose Direction uses radians.
            : new Direction(rotationY, rotationX, rotationZ);
    ModelParticle attachedParticle =
        new ModelParticle(
            particleSystem,
            Math.max(scale, 0.01F),
            null,
            EntityPart.Self,
            nodeName,
            new Vector3f(offsetX, offsetY, offsetZ),
            rotationOffset,
            detachEmittedParticles,
            clearOnEntityRemove);

    Set<Ref<EntityStore>> seen = new HashSet<>();
    int matchingEntities = 0;
    int missingNetworkIds = 0;
    int packetsSent = 0;
    Ref<EntityStore> closestEntity = null;
    double closestDistanceSquared = Double.POSITIVE_INFINITY;
    var entityResults = SpatialResource.<EntityStore>getThreadLocalReferenceList();
    for (var volume : context.getSpatialVolumes()) {
      entityResults.clear();
      var shape = volume.getShape();
      var origin = volume.getPosition();
      entitySpatial
          .getSpatialStructure()
          .collect(origin, shape.getMaxDistanceFromOrigin(), entityResults);
      for (var candidate : entityResults) {
        if (!candidate.isValid()
            || !seen.add(candidate)
            || !MotionTargeting.matches(store, candidate, origin, shape)) {
          continue;
        }
        matchingEntities++;

        TransformComponent transform =
            store.getComponent(candidate, TransformComponent.getComponentType());
        if (onlyFirstMatch) {
          double distanceSquared = transform.getPosition().distanceSquared(origin);
          if (distanceSquared < closestDistanceSquared) {
            closestDistanceSquared = distanceSquared;
            closestEntity = candidate;
          }
          continue;
        }

        int sent = sendToNearbyPlayers(store, playerSpatial, candidate, attachedParticle);
        if (sent < 0) {
          missingNetworkIds++;
        } else {
          packetsSent += sent;
        }
      }
    }
    if (onlyFirstMatch && closestEntity != null) {
      int sent = sendToNearbyPlayers(store, playerSpatial, closestEntity, attachedParticle);
      if (sent < 0) {
        missingNetworkIds++;
      } else {
        packetsSent += sent;
      }
    }
    logResult(matchingEntities, missingNetworkIds, packetsSent);
  }

  private static int sendToNearbyPlayers(
      com.hypixel.hytale.component.Store<EntityStore> store,
      SpatialResource<Ref<EntityStore>, EntityStore> playerSpatial,
      Ref<EntityStore> entity,
      ModelParticle attachedParticle) {
    NetworkId networkId = store.getComponent(entity, NetworkId.getComponentType());
    TransformComponent transform =
        store.getComponent(entity, TransformComponent.getComponentType());
    if (networkId == null || transform == null) {
      return -1;
    }

    var nearbyPlayers = new ArrayList<Ref<EntityStore>>();
    playerSpatial
        .getSpatialStructure()
        .collect(transform.getPosition(), DEFAULT_VIEW_RANGE, nearbyPlayers);
    SpawnModelParticles packet =
        new SpawnModelParticles(networkId.getId(), new ModelParticle[] {attachedParticle});
    int packetsSent = 0;
    for (var playerEntity : nearbyPlayers) {
      if (!playerEntity.isValid()) {
        continue;
      }
      PlayerRef playerRef =
          store.getComponent(playerEntity, PlayerRef.getComponentType());
      if (playerRef != null) {
        playerRef.getPacketHandler().write(packet);
        packetsSent++;
      }
    }
    return packetsSent;
  }

  private void logResult(int matchingEntities, int missingNetworkIds, int packetsSent) {
    log(
        packetsSent > 0 ? Level.INFO : Level.WARNING,
        "AttachMovingParticles '%s': matched=%d, missingNetworkId=%d, packets=%d, "
            + "offset=(%.2f, %.2f, %.2f), detached=%s.",
        particleSystem,
        matchingEntities,
        missingNetworkIds,
        packetsSent,
        offsetX,
        offsetY,
        offsetZ,
        detachEmittedParticles);
  }

  private static void log(Level level, String message, Object... arguments) {
    EntityMotionTriggersPlugin.get().getLogger().at(level).logVarargs(message, arguments);
  }
}
