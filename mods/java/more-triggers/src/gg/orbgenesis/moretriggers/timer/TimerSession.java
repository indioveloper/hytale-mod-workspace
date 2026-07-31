package gg.orbgenesis.moretriggers.timer;

final class TimerSession {
  private final long totalNanos;
  private long deadlineNanos;
  private long pausedRemainingNanos;
  private long completedAtNanos = -1L;
  private boolean paused;
  private boolean visible = true;

  TimerSession(int durationSeconds, long nowNanos) {
    totalNanos = Math.max(1L, durationSeconds) * 1_000_000_000L;
    deadlineNanos = nowNanos + totalNanos;
  }

  synchronized long totalNanos() {
    return totalNanos;
  }

  synchronized long remainingNanos(long nowNanos) {
    if (completedAtNanos >= 0L) {
      return 0L;
    }
    if (paused) {
      return pausedRemainingNanos;
    }
    return Math.max(0L, deadlineNanos - nowNanos);
  }

  synchronized boolean updateCompletion(long nowNanos) {
    if (!paused && completedAtNanos < 0L && nowNanos >= deadlineNanos) {
      completedAtNanos = nowNanos;
      return true;
    }
    return false;
  }

  synchronized boolean pause(long nowNanos) {
    if (paused || completedAtNanos >= 0L) {
      return false;
    }
    pausedRemainingNanos = Math.max(0L, deadlineNanos - nowNanos);
    paused = true;
    return true;
  }

  synchronized boolean resume(long nowNanos) {
    if (!paused || completedAtNanos >= 0L) {
      return false;
    }
    deadlineNanos = nowNanos + pausedRemainingNanos;
    paused = false;
    return true;
  }

  synchronized boolean isPaused() {
    return paused;
  }

  synchronized boolean isVisible() {
    return visible;
  }

  synchronized boolean setVisible(boolean nextVisible) {
    if (visible == nextVisible) {
      return false;
    }
    visible = nextVisible;
    return true;
  }

  synchronized boolean shouldExpire(long nowNanos) {
    return completedAtNanos >= 0L && nowNanos - completedAtNanos >= 1_000_000_000L;
  }

  synchronized boolean isComplete() {
    return completedAtNanos >= 0L;
  }
}
