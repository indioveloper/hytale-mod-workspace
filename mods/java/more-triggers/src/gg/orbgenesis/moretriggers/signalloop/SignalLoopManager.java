package gg.orbgenesis.moretriggers.signalloop;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.effect.SignalTag;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.TaggedVolumeEffectUtil;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Vector3d;

public final class SignalLoopManager {
  private static final int MAX_LOOP_ID_LENGTH = 64;

  private final Map<TriggerVolumeManager, Map<String, Session>> sessionsByWorld =
      new ConcurrentHashMap<>();

  public void apply(ControlSignalLoopEffect effect, TriggerContext context) {
    String loopId = normalizeLoopId(effect.loopId());
    if (loopId == null) {
      return;
    }
    Store<EntityStore> store = context.getStore();
    TriggerVolumeManager volumeManager =
        store.getResource(TriggerVolumesPlugin.get().getManagerResourceType());
    if (volumeManager == null) {
      return;
    }
    Map<String, Session> worldSessions =
        sessionsByWorld.computeIfAbsent(volumeManager, ignored -> new ConcurrentHashMap<>());

    switch (effect.action()) {
      case STOP -> remove(worldSessions, loopId, volumeManager);
      case PAUSE -> update(worldSessions, loopId, Session::pause);
      case RESUME -> update(worldSessions, loopId, Session::resume);
      case PULSE_NOW -> pulseNow(worldSessions, loopId, volumeManager);
      case START -> start(effect, context, volumeManager, worldSessions, loopId);
    }
  }

  private void start(
      ControlSignalLoopEffect effect,
      TriggerContext context,
      TriggerVolumeManager volumeManager,
      Map<String, Session> worldSessions,
      String loopId) {
    Session existing = worldSessions.get(loopId);
    if (existing != null) {
      if (effect.startBehavior() == SignalLoopStartBehavior.IGNORE_IF_RUNNING) {
        return;
      }
      if (effect.startBehavior() == SignalLoopStartBehavior.RESTART) {
        existing.restart();
        return;
      }
    }

    Ref<EntityStore> actorRef = context.getEntityRef();
    UUID actorUuid = TriggerVolumeManager.ENVIRONMENT_ACTOR_UUID;
    if (actorRef != null) {
      UUIDComponent uuidComponent =
          context.getStore().getComponent(actorRef, UUIDComponent.getComponentType());
      if (uuidComponent == null) {
        actorRef = null;
      } else {
        actorUuid = uuidComponent.getUuid();
      }
    }

    VolumeEntry source = context.getVolume();
    Vector3d sourcePosition = new Vector3d(source.getPosition());
    Vector3d queryOrigin = resolveCenter(context, effect.center(), sourcePosition);
    Vector3d eventOrigin = context.getEventPosition() == null
        ? new Vector3d(sourcePosition)
        : new Vector3d(context.getEventPosition());
    Session session = new Session(
        loopId,
        source.getId(),
        actorRef,
        actorUuid,
        queryOrigin,
        eventOrigin,
        TaggedVolumeEffectUtil.composeTagFilter(effect.matchKey(), effect.matchValue()),
        Math.max(0.0, effect.radius()),
        SignalTag.zip(effect.signalKeys(), effect.signalValues()),
        trimToNull(effect.continueTagKey()),
        trimToNull(effect.continueTagValue()),
        effect.intervalSeconds(),
        effect.durationSeconds(),
        effect.maxPulses(),
        effect.firstPulse());
    worldSessions.put(loopId, session);
  }

  public void tick(TriggerVolumeManager volumeManager, double deltaSeconds) {
    Map<String, Session> worldSessions = sessionsByWorld.get(volumeManager);
    if (worldSessions == null || worldSessions.isEmpty()) {
      return;
    }
    for (Session session : new ArrayList<>(worldSessions.values())) {
      VolumeEntry source = volumeManager.getVolume(session.sourceVolumeId);
      if (source == null || !source.isEnabled() || !matchesContinueTag(source, session)) {
        worldSessions.remove(session.loopId, session);
        continue;
      }
      if (session.schedule.tick(deltaSeconds)) {
        emit(volumeManager, session, source);
      }
      if (session.schedule.isFinished()) {
        worldSessions.remove(session.loopId, session);
      }
    }
    if (worldSessions.isEmpty()) {
      sessionsByWorld.remove(volumeManager, worldSessions);
    }
  }

  private void pulseNow(
      Map<String, Session> worldSessions,
      String loopId,
      TriggerVolumeManager volumeManager) {
    Session session = worldSessions.get(loopId);
    if (session == null) {
      return;
    }
    VolumeEntry source = volumeManager.getVolume(session.sourceVolumeId);
    if (source == null || !source.isEnabled() || !matchesContinueTag(source, session)) {
      remove(worldSessions, loopId, volumeManager);
      return;
    }
    if (session.schedule.forcePulse()) {
      emit(volumeManager, session, source);
    }
    if (session.schedule.isFinished()) {
      remove(worldSessions, loopId, volumeManager);
    }
  }

  private static void emit(
      TriggerVolumeManager volumeManager,
      Session session,
      VolumeEntry source) {
    Ref<EntityStore> actorRef = session.actorRef;
    UUID actorUuid = session.actorUuid;
    if (actorRef != null && !actorRef.isValid()) {
      actorRef = null;
      actorUuid = TriggerVolumeManager.ENVIRONMENT_ACTOR_UUID;
    }
    List<VolumeEntry> targets;
    if (session.matchTag == null) {
      targets = session.radius <= 0.0
          || source.getPosition().distanceSquared(session.queryOrigin) <= session.radius * session.radius
          ? List.of(source)
          : List.of();
    } else {
      targets = TaggedVolumeEffectUtil.collectTargets(
          volumeManager, session.matchTag, session.radius, session.queryOrigin);
    }
    for (VolumeEntry target : targets) {
      volumeManager.enqueueVolumeEvent(
          TriggerEventType.SIGNAL_RECEIVED,
          actorRef,
          actorUuid,
          target.getId(),
          session.signalTags,
          session.eventOrigin,
          null);
    }
  }

  private static boolean matchesContinueTag(VolumeEntry source, Session session) {
    if (session.continueTagKey == null) {
      return true;
    }
    Map<String, String> tags = source.getRawTags();
    if (!tags.containsKey(session.continueTagKey)) {
      return false;
    }
    if (session.continueTagValue == null) {
      return true;
    }
    String actual = tags.get(session.continueTagKey);
    return TaggedVolumeEffectUtil.normalizeTagValue(session.continueTagValue)
        .equals(TaggedVolumeEffectUtil.normalizeTagValue(actual));
  }

  private static Vector3d resolveCenter(
      TriggerContext context,
      TaggedVolumeEffectUtil.Center center,
      Vector3d sourcePosition) {
    if (center == TaggedVolumeEffectUtil.Center.ENTITY && context.getActorPosition() != null) {
      return new Vector3d(context.getActorPosition());
    }
    if (center == TaggedVolumeEffectUtil.Center.EVENT && context.getEventPosition() != null) {
      return new Vector3d(context.getEventPosition());
    }
    return new Vector3d(sourcePosition);
  }

  private static void update(
      Map<String, Session> worldSessions,
      String loopId,
      java.util.function.Consumer<Session> action) {
    Session session = worldSessions.get(loopId);
    if (session != null) {
      action.accept(session);
    }
  }

  private void remove(
      Map<String, Session> worldSessions, String loopId, TriggerVolumeManager volumeManager) {
    worldSessions.remove(loopId);
    if (worldSessions.isEmpty()) {
      sessionsByWorld.remove(volumeManager, worldSessions);
    }
  }

  public void clear() {
    sessionsByWorld.clear();
  }

  private static String normalizeLoopId(String value) {
    String normalized = trimToNull(value);
    if (normalized == null) {
      return null;
    }
    return normalized.length() <= MAX_LOOP_ID_LENGTH
        ? normalized
        : normalized.substring(0, MAX_LOOP_ID_LENGTH);
  }

  private static String trimToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  private static final class Session {
    private final String loopId;
    private final String sourceVolumeId;
    private final Ref<EntityStore> actorRef;
    private final UUID actorUuid;
    private final Vector3d queryOrigin;
    private final Vector3d eventOrigin;
    private final String matchTag;
    private final double radius;
    private final List<SignalTag> signalTags;
    private final String continueTagKey;
    private final String continueTagValue;
    private final double intervalSeconds;
    private final double durationSeconds;
    private final int maxPulses;
    private final SignalLoopFirstPulse firstPulse;
    private SignalLoopSchedule schedule;

    private Session(
        String loopId,
        String sourceVolumeId,
        Ref<EntityStore> actorRef,
        UUID actorUuid,
        Vector3d queryOrigin,
        Vector3d eventOrigin,
        String matchTag,
        double radius,
        List<SignalTag> signalTags,
        String continueTagKey,
        String continueTagValue,
        double intervalSeconds,
        double durationSeconds,
        int maxPulses,
        SignalLoopFirstPulse firstPulse) {
      this.loopId = loopId;
      this.sourceVolumeId = sourceVolumeId;
      this.actorRef = actorRef;
      this.actorUuid = actorUuid;
      this.queryOrigin = queryOrigin;
      this.eventOrigin = eventOrigin;
      this.matchTag = matchTag;
      this.radius = radius;
      this.signalTags = List.copyOf(signalTags);
      this.continueTagKey = continueTagKey;
      this.continueTagValue = continueTagValue;
      this.intervalSeconds = intervalSeconds;
      this.durationSeconds = durationSeconds;
      this.maxPulses = maxPulses;
      this.firstPulse = firstPulse;
      restart();
    }

    private void restart() {
      schedule = new SignalLoopSchedule(
          intervalSeconds, durationSeconds, maxPulses, firstPulse);
    }

    private void pause() {
      schedule.pause();
    }

    private void resume() {
      schedule.resume();
    }
  }
}
