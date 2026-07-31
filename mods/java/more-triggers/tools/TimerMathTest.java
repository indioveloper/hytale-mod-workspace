package gg.orbgenesis.moretriggers.timer;

public final class TimerMathTest {
  private TimerMathTest() {}

  public static void main(String[] args) {
    require("00:00".equals(TimerManager.formatRemaining(0L)), "zero format");
    require("00:01".equals(TimerManager.formatRemaining(1L)), "ceil partial second");
    require("01:12".equals(TimerManager.formatRemaining(72_000_000_000L)), "minute format");
    require("01:01:01".equals(TimerManager.formatRemaining(3661_000_000_000L)), "hour format");
    require(TimerManager.frameFor(60L, 60L) == 60, "full frame");
    require(TimerManager.frameFor(45L, 60L) == 45, "three-quarter frame");
    require(TimerManager.frameFor(1L, 60L) == 1, "last visible frame");
    require(TimerManager.frameFor(0L, 60L) == 0, "empty frame");

    long start = 1_000_000_000L;
    TimerSession session = new TimerSession(10, start);
    require(session.remainingNanos(start + 2_000_000_000L) == 8_000_000_000L, "countdown");
    require(session.pause(start + 2_000_000_000L), "pause");
    require(session.remainingNanos(start + 8_000_000_000L) == 8_000_000_000L, "paused value");
    require(session.resume(start + 8_000_000_000L), "resume");
    require(session.remainingNanos(start + 10_000_000_000L) == 6_000_000_000L, "resumed value");
    require(session.updateCompletion(start + 16_000_000_000L), "completion");
    require(session.remainingNanos(start + 16_000_000_000L) == 0L, "completed value");
    require(!session.shouldExpire(start + 16_999_999_999L), "completion grace period");
    require(session.shouldExpire(start + 17_000_000_000L), "completion expiry");
    System.out.println("Circular Timer math tests passed.");
  }

  private static void require(boolean condition, String label) {
    if (!condition) {
      throw new AssertionError(label);
    }
  }
}
