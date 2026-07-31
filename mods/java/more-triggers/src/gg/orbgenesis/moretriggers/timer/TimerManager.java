package gg.orbgenesis.moretriggers.timer;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TimerManager {
  private final Map<UUID, TimerSession> sessions = new ConcurrentHashMap<>();

  public int apply(TimerAction action, int durationSeconds, List<PlayerRef> players) {
    TimerAction effective = action == null ? TimerAction.START : action;
    int changed = 0;
    for (PlayerRef playerRef : players == null ? List.<PlayerRef>of() : players) {
      if (playerRef == null || !playerRef.isValid()) {
        continue;
      }
      boolean didChange =
          switch (effective) {
            case START -> start(playerRef, durationSeconds);
            case PAUSE -> pause(playerRef);
            case RESUME -> resume(playerRef);
            case SHOW -> show(playerRef);
            case HIDE -> hide(playerRef);
            case CANCEL -> cancel(playerRef);
          };
      if (didChange) {
        changed++;
      }
    }
    return changed;
  }

  public boolean start(PlayerRef playerRef, int durationSeconds) {
    if (durationSeconds <= 0 || durationSeconds > 359999) {
      return false;
    }
    sessions.put(playerRef.getUuid(), new TimerSession(durationSeconds, System.nanoTime()));
    return true;
  }

  public boolean pause(PlayerRef playerRef) {
    TimerSession session = sessions.get(playerRef.getUuid());
    return session != null && session.pause(System.nanoTime());
  }

  public boolean resume(PlayerRef playerRef) {
    TimerSession session = sessions.get(playerRef.getUuid());
    return session != null && session.resume(System.nanoTime());
  }

  public boolean show(PlayerRef playerRef) {
    TimerSession session = sessions.get(playerRef.getUuid());
    return session != null && session.setVisible(true);
  }

  public boolean hide(PlayerRef playerRef) {
    TimerSession session = sessions.get(playerRef.getUuid());
    if (session == null) {
      return false;
    }
    boolean changed = session.setVisible(false);
    hideHud(playerRef);
    return changed;
  }

  public boolean cancel(PlayerRef playerRef) {
    TimerSession removed = sessions.remove(playerRef.getUuid());
    if (removed == null) {
      return false;
    }
    hideHud(playerRef);
    return true;
  }

  public TimerSnapshot snapshot(PlayerRef playerRef) {
    TimerSession session = sessions.get(playerRef.getUuid());
    if (session == null) {
      return null;
    }
    long now = System.nanoTime();
    session.updateCompletion(now);
    return new TimerSnapshot(
        formatRemaining(session.remainingNanos(now)),
        session.isPaused(),
        session.isVisible(),
        session.isComplete());
  }

  void tick(Player player, PlayerRef playerRef, long nowNanos) {
    TimerSession session = sessions.get(playerRef.getUuid());
    if (session == null) {
      return;
    }
    session.updateCompletion(nowNanos);
    if (session.isVisible()) {
      long remaining = session.remainingNanos(nowNanos);
      CircularTimerHud.getOrCreate(player, playerRef)
          .render(formatRemaining(remaining), frameFor(remaining, session.totalNanos()));
    } else {
      hideHud(player, playerRef);
    }
    if (session.shouldExpire(nowNanos)
        && sessions.remove(playerRef.getUuid(), session)) {
      hideHud(player, playerRef);
    }
  }

  public void clear() {
    sessions.clear();
  }

  static int frameFor(long remainingNanos, long totalNanos) {
    if (remainingNanos <= 0L || totalNanos <= 0L) {
      return 0;
    }
    double ratio = Math.min(1.0, remainingNanos / (double) totalNanos);
    return Math.max(1, (int) Math.ceil(ratio * CircularTimerHud.FRAME_COUNT));
  }

  static String formatRemaining(long remainingNanos) {
    long seconds = remainingNanos <= 0L ? 0L : (remainingNanos + 999_999_999L) / 1_000_000_000L;
    long hours = seconds / 3600L;
    long minutes = (seconds % 3600L) / 60L;
    long remainder = seconds % 60L;
    return hours > 0L
        ? String.format("%02d:%02d:%02d", hours, minutes, remainder)
        : String.format("%02d:%02d", minutes, remainder);
  }

  private void hideHud(PlayerRef playerRef) {
    Ref<EntityStore> ref = playerRef.getReference();
    if (ref == null || !ref.isValid()) {
      return;
    }
    Player player = ref.getStore().getComponent(ref, Player.getComponentType());
    if (player != null) {
      hideHud(player, playerRef);
    }
  }

  private void hideHud(Player player, PlayerRef playerRef) {
    com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud existing =
        player.getHudManager().getCustomHud(CircularTimerHud.HUD_KEY);
    if (existing instanceof CircularTimerHud hud) {
      hud.hideTimer();
    }
  }

  public record TimerSnapshot(String remaining, boolean paused, boolean visible, boolean complete) {}
}
