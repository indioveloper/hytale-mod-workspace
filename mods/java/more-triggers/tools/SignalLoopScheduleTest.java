package gg.orbgenesis.moretriggers.signalloop;

public final class SignalLoopScheduleTest {
  private SignalLoopScheduleTest() {}

  public static void main(String[] args) {
    SignalLoopSchedule immediate =
        new SignalLoopSchedule(5.0, 0.0, 0, SignalLoopFirstPulse.IMMEDIATE);
    require(immediate.tick(0.05), "immediate first pulse");
    require(!immediate.tick(4.9), "wait between pulses");
    require(immediate.tick(0.1), "second interval pulse");

    SignalLoopSchedule delayed =
        new SignalLoopSchedule(2.0, 0.0, 0, SignalLoopFirstPulse.AFTER_INTERVAL);
    require(!delayed.tick(1.9), "delayed first pulse waits");
    require(delayed.tick(0.1), "delayed first pulse");

    SignalLoopSchedule clamped =
        new SignalLoopSchedule(0.0, 0.0, 0, SignalLoopFirstPulse.AFTER_INTERVAL);
    require(!clamped.tick(0.05), "minimum interval first half");
    require(clamped.tick(0.05), "minimum interval clamp");

    SignalLoopSchedule limited =
        new SignalLoopSchedule(1.0, 0.0, 2, SignalLoopFirstPulse.IMMEDIATE);
    require(limited.tick(0.05), "limited first pulse");
    require(limited.tick(1.0), "limited second pulse");
    require(limited.isFinished(), "maximum pulse stop");
    require(!limited.tick(10.0), "finished loop stays stopped");

    SignalLoopSchedule duration =
        new SignalLoopSchedule(2.0, 3.0, 0, SignalLoopFirstPulse.AFTER_INTERVAL);
    require(duration.tick(2.0), "duration allows pulse before end");
    require(!duration.tick(1.0), "duration ends before next pulse");
    require(duration.isFinished(), "duration stop");

    SignalLoopSchedule paused =
        new SignalLoopSchedule(1.0, 0.0, 0, SignalLoopFirstPulse.AFTER_INTERVAL);
    paused.tick(0.4);
    paused.pause();
    require(!paused.tick(10.0), "pause freezes loop");
    require(close(paused.getSecondsUntilPulse(), 0.6), "pause freezes interval");
    paused.resume();
    require(paused.tick(0.6), "resume continues interval");

    SignalLoopSchedule lagged =
        new SignalLoopSchedule(1.0, 0.0, 0, SignalLoopFirstPulse.AFTER_INTERVAL);
    require(lagged.tick(5.0), "lag produces one pulse");
    require(lagged.getPulseCount() == 1, "no catch-up burst");
    require(!lagged.tick(0.5), "fresh interval after lag");

    SignalLoopSchedule forced =
        new SignalLoopSchedule(3.0, 0.0, 0, SignalLoopFirstPulse.AFTER_INTERVAL);
    forced.tick(1.0);
    require(forced.forcePulse(), "manual pulse");
    require(close(forced.getSecondsUntilPulse(), 2.0), "manual pulse preserves schedule");
    require(forced.tick(2.0), "scheduled pulse still occurs");

    System.out.println("Repeating signal schedule tests passed.");
  }

  private static boolean close(double left, double right) {
    return Math.abs(left - right) < 0.000001;
  }

  private static void require(boolean condition, String label) {
    if (!condition) {
      throw new AssertionError(label);
    }
  }
}
