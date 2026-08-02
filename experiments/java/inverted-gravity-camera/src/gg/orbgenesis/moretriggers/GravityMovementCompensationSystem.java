package gg.orbgenesis.moretriggers;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerInput;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Vector3d;

/** Archived server-side movement reflection experiment. */
final class GravityMovementCompensationSystem extends EntityTickingSystem<EntityStore> {
  private static final double MIN_DIRECTION_LENGTH_SQUARED = 1.0e-8;
  private static final double TELEPORT_DISTANCE_SQUARED = 100.0;

  private final GravityViewController controller;
  private final Map<UUID, Vector3d> rawClientPositions = new ConcurrentHashMap<>();
  private final Query<EntityStore> query =
      Query.and(
          PlayerInput.getComponentType(),
          PlayerRef.getComponentType(),
          HeadRotation.getComponentType(),
          TransformComponent.getComponentType());

  GravityMovementCompensationSystem(GravityViewController controller) {
    this.controller = controller;
  }

  @Override
  public Query<EntityStore> getQuery() {
    return query;
  }

  @Override
  public Set<Dependency<EntityStore>> getDependencies() {
    return Set.of(new SystemDependency<>(Order.BEFORE, PlayerSystems.ProcessPlayerInput.class));
  }

  @Override
  public void tick(
      float dt,
      int index,
      ArchetypeChunk<EntityStore> archetypeChunk,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> commandBuffer) {
    PlayerRef player = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
    if (player == null) {
      return;
    }

    UUID playerId = player.getUuid();
    if (!controller.shouldCompensateMovement(playerId)) {
      rawClientPositions.remove(playerId);
      return;
    }

    PlayerInput input = archetypeChunk.getComponent(index, PlayerInput.getComponentType());
    HeadRotation headRotation =
        archetypeChunk.getComponent(index, HeadRotation.getComponentType());
    TransformComponent transform =
        archetypeChunk.getComponent(index, TransformComponent.getComponentType());
    if (input == null || headRotation == null || transform == null) {
      return;
    }

    Vector3d serverPosition = new Vector3d(transform.getPosition());
    Vector3d rawPosition =
        rawClientPositions.computeIfAbsent(playerId, ignored -> new Vector3d(serverPosition));
    Vector3d forward = headRotation.getDirection();
    normalizeHorizontal(forward);

    for (PlayerInput.InputUpdate update : input.getMovementUpdateQueue()) {
      if (update instanceof PlayerInput.SetHead setHead) {
        setHorizontalDirection(forward, setHead.direction());
      } else if (update instanceof PlayerInput.SetClientVelocity setVelocity) {
        reflectHorizontal(setVelocity.getVelocity(), forward.x, forward.z);
      } else if (update instanceof PlayerInput.WishMovement wishMovement) {
        Vector3d reflected =
            reflectHorizontal(
                new Vector3d(
                    wishMovement.getX(), wishMovement.getY(), wishMovement.getZ()),
                forward.x,
                forward.z);
        wishMovement.setX(reflected.x);
        wishMovement.setZ(reflected.z);
      } else if (update instanceof PlayerInput.RelativeMovement relativeMovement) {
        Vector3d rawDelta =
            new Vector3d(
                relativeMovement.getX(), relativeMovement.getY(), relativeMovement.getZ());
        rawPosition.add(rawDelta);
        reflectHorizontal(rawDelta, forward.x, forward.z);
        relativeMovement.setX(rawDelta.x);
        relativeMovement.setZ(rawDelta.z);
        serverPosition.add(rawDelta);
      } else if (update instanceof PlayerInput.AbsoluteMovement absoluteMovement) {
        Vector3d nextRaw =
            new Vector3d(
                absoluteMovement.getX(), absoluteMovement.getY(), absoluteMovement.getZ());
        Vector3d rawDelta = nextRaw.sub(rawPosition, new Vector3d());
        rawPosition.set(nextRaw);
        if (rawDelta.lengthSquared() > TELEPORT_DISTANCE_SQUARED) {
          serverPosition.set(nextRaw);
        } else {
          reflectHorizontal(rawDelta, forward.x, forward.z);
          serverPosition.add(rawDelta);
          absoluteMovement.setX(serverPosition.x);
          absoluteMovement.setZ(serverPosition.z);
        }
      }
    }
  }

  private static void setHorizontalDirection(Vector3d target, Direction direction) {
    target.set(-Math.sin(direction.yaw), 0.0, -Math.cos(direction.yaw));
    normalizeHorizontal(target);
  }

  private static void normalizeHorizontal(Vector3d direction) {
    direction.y = 0.0;
    double lengthSquared = direction.x * direction.x + direction.z * direction.z;
    if (lengthSquared < MIN_DIRECTION_LENGTH_SQUARED) {
      direction.set(1.0, 0.0, 0.0);
      return;
    }
    double inverseLength = 1.0 / Math.sqrt(lengthSquared);
    direction.x *= inverseLength;
    direction.z *= inverseLength;
  }

  static Vector3d reflectHorizontal(Vector3d movement, double forwardX, double forwardZ) {
    double projection = movement.x * forwardX + movement.z * forwardZ;
    movement.x = 2.0 * projection * forwardX - movement.x;
    movement.z = 2.0 * projection * forwardZ - movement.z;
    return movement;
  }
}
