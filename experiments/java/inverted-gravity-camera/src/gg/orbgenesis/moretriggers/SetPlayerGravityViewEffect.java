package gg.orbgenesis.moretriggers;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.TimeUnit;

/** Archived prototype: applies or removes an upside-down camera. */
public class SetPlayerGravityViewEffect extends TriggerEffect {
  private static final float DEFAULT_LERP_SPEED = 0.2f;
  private static final float MIN_LERP_SPEED = 0.01f;
  private static final float MAX_LERP_SPEED = 10.0f;
  private static final float DEFAULT_INVERTED_EYE_HEIGHT = 1.6f;
  private static final float MIN_INVERTED_EYE_HEIGHT = 0.0f;
  private static final float MAX_INVERTED_EYE_HEIGHT = 3.0f;
  private static final long FLIP_DURATION_MILLIS = 1_100L;
  private static final String FLIP_ANIMATION_SET = "Daggers";
  private static final String FLIP_ANIMATION = "Backflip";

  public enum Orientation {
    UPSIDE_DOWN,
    NORMAL
  }

  public enum Perspective {
    FIRST_PERSON,
    THIRD_PERSON
  }

  public static final BuilderCodec<SetPlayerGravityViewEffect> CODEC =
      BuilderCodec.builder(
              SetPlayerGravityViewEffect.class,
              SetPlayerGravityViewEffect::new,
              TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("Orientation", new EnumCodec<>(Orientation.class), false),
              (effect, value) ->
                  effect.orientation = value != null ? value : Orientation.UPSIDE_DOWN,
              effect -> effect.orientation)
          .add()
          .append(
              new KeyedCodec<>("PlayFlipAnimation", Codec.BOOLEAN, false),
              (effect, value) -> effect.playFlipAnimation = value,
              effect -> effect.playFlipAnimation)
          .add()
          .append(
              new KeyedCodec<>("CameraLerpSpeed", Codec.FLOAT, false),
              (effect, value) -> effect.cameraLerpSpeed = value,
              effect -> effect.cameraLerpSpeed)
          .add()
          .append(
              new KeyedCodec<>("AllowPerspectiveToggle", Codec.BOOLEAN, false),
              (effect, value) -> effect.allowPerspectiveToggle = value,
              effect -> effect.allowPerspectiveToggle)
          .add()
          .append(
              new KeyedCodec<>("InitialPerspective", new EnumCodec<>(Perspective.class), false),
              (effect, value) ->
                  effect.initialPerspective =
                      value != null ? value : Perspective.FIRST_PERSON,
              effect -> effect.initialPerspective)
          .add()
          .append(
              new KeyedCodec<>("CompensateLookControls", Codec.BOOLEAN, false),
              (effect, value) -> effect.compensateLookControls = value,
              effect -> effect.compensateLookControls)
          .add()
          .append(
              new KeyedCodec<>("CompensateMovementControls", Codec.BOOLEAN, false),
              (effect, value) -> effect.compensateMovementControls = value,
              effect -> effect.compensateMovementControls)
          .add()
          .append(
              new KeyedCodec<>("InvertedEyeHeight", Codec.FLOAT, false),
              (effect, value) -> effect.invertedEyeHeight = value,
              effect -> effect.invertedEyeHeight)
          .add()
          .build();

  public Orientation orientation = Orientation.UPSIDE_DOWN;
  public boolean playFlipAnimation = true;
  public float cameraLerpSpeed = DEFAULT_LERP_SPEED;
  public boolean allowPerspectiveToggle = true;
  public Perspective initialPerspective = Perspective.FIRST_PERSON;
  public boolean compensateLookControls = true;
  public boolean compensateMovementControls = true;
  public float invertedEyeHeight = DEFAULT_INVERTED_EYE_HEIGHT;

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
    PlayerRef player = store.getComponent(entityRef, PlayerRef.getComponentType());
    if (player == null) {
      return;
    }

    if (playFlipAnimation) {
      playFlipAnimation(entityRef, store);
    }

    Orientation target = orientation != null ? orientation : Orientation.UPSIDE_DOWN;
    if (target == Orientation.NORMAL) {
      MoreTriggersPlugin.get().getGravityViewController().reset(player);
      return;
    }

    MoreTriggersPlugin.get()
        .getGravityViewController()
        .apply(
            player,
            initialPerspective,
            clampCameraLerpSpeed(cameraLerpSpeed),
            clampInvertedEyeHeight(invertedEyeHeight),
            allowPerspectiveToggle,
            compensateLookControls,
            compensateMovementControls);
  }

  private void playFlipAnimation(Ref<EntityStore> entityRef, Store<EntityStore> store) {
    AnimationUtils.playAnimation(
        entityRef,
        AnimationSlot.ServerAction,
        FLIP_ANIMATION_SET,
        FLIP_ANIMATION,
        true,
        store);

    World world = store.getExternalData().getWorld();
    world.scheduleAfter(
        () -> {
          if (entityRef.isValid()) {
            AnimationUtils.stopAnimation(entityRef, AnimationSlot.ServerAction, true, store);
          }
        },
        FLIP_DURATION_MILLIS,
        TimeUnit.MILLISECONDS);
  }

  static float clampCameraLerpSpeed(float value) {
    if (!Float.isFinite(value)) {
      return DEFAULT_LERP_SPEED;
    }
    return Math.max(MIN_LERP_SPEED, Math.min(MAX_LERP_SPEED, value));
  }

  static float clampInvertedEyeHeight(float value) {
    if (!Float.isFinite(value)) {
      return DEFAULT_INVERTED_EYE_HEIGHT;
    }
    return Math.max(MIN_INVERTED_EYE_HEIGHT, Math.min(MAX_INVERTED_EYE_HEIGHT, value));
  }
}
