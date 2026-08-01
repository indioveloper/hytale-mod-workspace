package gg.orbgenesis.moretriggers;

import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.MovementForceRotationType;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Vector2f;

/** Archived controller from the More Triggers 1.8.x experiment. */
final class GravityViewController {
  private static final float HALF_TURN_RADIANS = (float) Math.PI;
  private static final float THIRD_PERSON_DISTANCE = 4.0f;

  private final Map<UUID, State> states = new ConcurrentHashMap<>();

  void apply(
      PlayerRef player,
      SetPlayerGravityViewEffect.Perspective perspective,
      float cameraLerpSpeed,
      float invertedEyeHeight,
      boolean allowPerspectiveToggle,
      boolean compensateLookControls,
      boolean compensateMovementControls) {
    State state =
        new State(
            perspective != null
                ? perspective
                : SetPlayerGravityViewEffect.Perspective.FIRST_PERSON,
            cameraLerpSpeed,
            invertedEyeHeight,
            allowPerspectiveToggle,
            compensateLookControls,
            compensateMovementControls);
    states.put(player.getUuid(), state);
    send(player, state);
  }

  SetPlayerGravityViewEffect.Perspective toggle(PlayerRef player) {
    State current = states.get(player.getUuid());
    if (current == null) {
      return null;
    }

    SetPlayerGravityViewEffect.Perspective nextPerspective =
        current.perspective == SetPlayerGravityViewEffect.Perspective.FIRST_PERSON
            ? SetPlayerGravityViewEffect.Perspective.THIRD_PERSON
            : SetPlayerGravityViewEffect.Perspective.FIRST_PERSON;
    State next =
        new State(
            nextPerspective,
            current.cameraLerpSpeed,
            current.invertedEyeHeight,
            current.allowPerspectiveToggle,
            current.compensateLookControls,
            current.compensateMovementControls);
    states.put(player.getUuid(), next);
    send(player, next);
    return nextPerspective;
  }

  void reset(PlayerRef player) {
    states.remove(player.getUuid());
    player
        .getPacketHandler()
        .writeNoCache(new SetServerCamera(ClientCameraView.Custom, false, null));
  }

  void clear() {
    states.clear();
  }

  boolean shouldCompensateMovement(UUID playerId) {
    State state = states.get(playerId);
    return state != null && state.compensateMovementControls;
  }

  private void send(PlayerRef player, State state) {
    ServerCameraSettings settings = new ServerCameraSettings();
    settings.rotationLerpSpeed = state.cameraLerpSpeed;
    settings.distance = THIRD_PERSON_DISTANCE;
    settings.eyeOffset = false;
    settings.positionOffset = new Position(0.0, -state.invertedEyeHeight, 0.0);
    settings.displayReticle = true;
    settings.movementForceRotationType = MovementForceRotationType.CameraRotation;
    settings.isFirstPerson =
        state.perspective == SetPlayerGravityViewEffect.Perspective.FIRST_PERSON;
    settings.rotationOffset = new Direction(0.0f, 0.0f, HALF_TURN_RADIANS);
    if (state.compensateLookControls) {
      settings.lookMultiplier = new Vector2f(-1.0f, -1.0f);
    }
    player
        .getPacketHandler()
        .writeNoCache(
            new SetServerCamera(
                ClientCameraView.Custom, !state.allowPerspectiveToggle, settings));
  }

  private record State(
      SetPlayerGravityViewEffect.Perspective perspective,
      float cameraLerpSpeed,
      float invertedEyeHeight,
      boolean allowPerspectiveToggle,
      boolean compensateLookControls,
      boolean compensateMovementControls) {}
}
