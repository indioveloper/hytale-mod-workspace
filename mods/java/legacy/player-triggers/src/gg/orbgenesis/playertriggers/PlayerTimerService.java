package gg.orbgenesis.playertriggers;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerTimerService {
  private static final double MAX_DURATION_SECONDS = 365.0D * 24.0D * 60.0D * 60.0D;
  private final Map<UUID, TimerEntry> timers = new ConcurrentHashMap<>();

  public void start(
      TriggerContext context,
      PlayerRef player,
      double durationSeconds,
      String label,
      String successTag,
      boolean restartIfRunning,
      StartPlayerTimerEffect owner) {
    if (!restartIfRunning && timers.containsKey(player.getUuid())) {
      return;
    }

    TriggerVolumeManager manager =
        context
            .getStore()
            .getResource(TriggerVolumesPlugin.get().getManagerResourceType());
    if (manager == null) {
      return;
    }

    manager.removeTag(
        context.getVolume().getId(),
        successTag,
        context.getEntityRef(),
        player.getUuid());

    long durationNanos =
        Math.max(
            1L,
            (long)
                (Math.min(durationSeconds, MAX_DURATION_SECONDS)
                    * 1_000_000_000L));
    TimerEntry entry =
        new TimerEntry(
            context.getStore(),
            player.getUuid(),
            context.getVolume().getId(),
            successTag,
            label == null || label.isBlank() ? "Tiempo restante" : label.trim(),
            safeDeadline(durationNanos),
            owner);
    timers.put(player.getUuid(), entry);
    show(entry, context.getEntityRef(), player);
  }

  public void cancel(UUID playerId, StartPlayerTimerEffect owner) {
    TimerEntry entry = timers.get(playerId);
    if (entry != null
        && entry.owner == owner
        && timers.remove(playerId, entry)) {
      removeHud(entry);
    }
  }

  void tick(Store<EntityStore> store) {
    long now = System.nanoTime();
    for (TimerEntry entry : timers.values()) {
      if (entry.store != store) {
        continue;
      }

      Ref<EntityStore> playerRef = store.getExternalData().getRefFromUUID(entry.playerId);
      if (playerRef == null || !playerRef.isValid()) {
        timers.remove(entry.playerId, entry);
        continue;
      }

      long remainingNanos = entry.endNanos - now;
      if (remainingNanos <= 0L) {
        if (timers.remove(entry.playerId, entry)) {
          finish(entry, playerRef);
        }
        continue;
      }

      long remainingSeconds =
          Math.max(1L, (remainingNanos + 999_999_999L) / 1_000_000_000L);
      if (remainingSeconds != entry.lastDisplayedSeconds) {
        entry.lastDisplayedSeconds = remainingSeconds;
        updateHud(store, playerRef, entry);
      }
    }
  }

  private void show(
      TimerEntry entry, Ref<EntityStore> entityRef, PlayerRef playerRef) {
    Player player = entry.store.getComponent(entityRef, Player.getComponentType());
    if (player == null) {
      return;
    }
    PlayerTimerHud hud =
        PlayerTimerHud.getOrCreate(player, playerRef, entry.label);
    hud.updateTimer(entry.label, formatSeconds(secondsRemaining(entry)));
  }

  private void updateHud(
      Store<EntityStore> store, Ref<EntityStore> playerRef, TimerEntry entry) {
    Player player = store.getComponent(playerRef, Player.getComponentType());
    PlayerRef networkPlayer = store.getComponent(playerRef, PlayerRef.getComponentType());
    if (player == null || networkPlayer == null) {
      return;
    }
    PlayerTimerHud hud =
        PlayerTimerHud.getOrCreate(player, networkPlayer, entry.label);
    hud.updateTimer(entry.label, formatSeconds(entry.lastDisplayedSeconds));
  }

  private void finish(TimerEntry entry, Ref<EntityStore> playerRef) {
    removeHud(entry);
    TriggerVolumeManager manager =
        entry.store.getResource(TriggerVolumesPlugin.get().getManagerResourceType());
    VolumeEntry volume = manager == null ? null : manager.getVolume(entry.volumeId);
    if (volume != null) {
      manager.removeTag(
          volume.getId(), entry.successTag, playerRef, entry.playerId);
      manager.setTag(
          volume.getId(), entry.successTag, "success", playerRef, entry.playerId);
    }
  }

  private void removeHud(TimerEntry entry) {
    Ref<EntityStore> playerRef =
        entry.store.getExternalData().getRefFromUUID(entry.playerId);
    if (playerRef == null || !playerRef.isValid()) {
      return;
    }
    Player player = entry.store.getComponent(playerRef, Player.getComponentType());
    PlayerRef networkPlayer =
        entry.store.getComponent(playerRef, PlayerRef.getComponentType());
    if (player != null && networkPlayer != null) {
      player.getHudManager().removeCustomHud(networkPlayer, PlayerTimerHud.HUD_KEY);
    }
  }

  private static long secondsRemaining(TimerEntry entry) {
    long nanos = Math.max(0L, entry.endNanos - System.nanoTime());
    return Math.max(1L, (nanos + 999_999_999L) / 1_000_000_000L);
  }

  static String formatSeconds(long totalSeconds) {
    long minutes = totalSeconds / 60L;
    long seconds = totalSeconds % 60L;
    return String.format("%02d:%02d", minutes, seconds);
  }

  private static long safeDeadline(long durationNanos) {
    long now = System.nanoTime();
    try {
      return Math.addExact(now, durationNanos);
    } catch (ArithmeticException ignored) {
      return Long.MAX_VALUE;
    }
  }

  static final class TimerEntry {
    final Store<EntityStore> store;
    final UUID playerId;
    final String volumeId;
    final String successTag;
    final String label;
    final long endNanos;
    final StartPlayerTimerEffect owner;
    volatile long lastDisplayedSeconds = -1L;

    TimerEntry(
        Store<EntityStore> store,
        UUID playerId,
        String volumeId,
        String successTag,
        String label,
        long endNanos,
        StartPlayerTimerEffect owner) {
      this.store = store;
      this.playerId = playerId;
      this.volumeId = volumeId;
      this.successTag = successTag;
      this.label = label;
      this.endNanos = endNanos;
      this.owner = owner;
    }
  }
}
