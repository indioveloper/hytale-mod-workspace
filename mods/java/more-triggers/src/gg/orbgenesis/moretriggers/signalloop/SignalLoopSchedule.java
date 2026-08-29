package gg.orbgenesis.moretriggers.signalloop;

public final class SignalLoopSchedule {
  public static final double MIN_INTERVAL_SECONDS = 0.1;
  private static final double DUE_EPSILON = 0.000000001;

  private final double intervalSeconds;
  private final double durationSeconds;
  private final int maxPulses;
  private double secondsUntilPulse;
  private double elapsedSeconds;
  private int pulseCount;
  private boolean paused;

  public SignalLoopSchedule(
      double intervalSeconds,
      double durationSeconds,
      int maxPulses,
      SignalLoopFirstPulse firstPulse) {
    this.intervalSeconds = Math.max(MIN_INTERVAL_SECONDS, intervalSeconds);
    this.durationSeconds = Math.max(0.0, durationSeconds);
    this.maxPulses = Math.max(0, maxPulses);
    this.secondsUntilPulse =
        firstPulse == SignalLoopFirstPulse.AFTER_INTERVAL ? this.intervalSeconds : 0.0;
  }

  public boolean tick(double deltaSeconds) {
    if (paused || isFinished() || deltaSeconds <= 0.0) {
      return false;
    }
    boolean pulseWasAlreadyDue = secondsUntilPulse <= DUE_EPSILON;
    double remainingDuration =
        durationSeconds > 0.0 ? Math.max(0.0, durationSeconds - elapsedSeconds) : deltaSeconds;
    double appliedDelta = Math.min(deltaSeconds, remainingDuration);
    elapsedSeconds += appliedDelta;
    secondsUntilPulse -= appliedDelta;
    if (!pulseWasAlreadyDue && secondsUntilPulse > DUE_EPSILON) {
      return false;
    }
    pulseCount++;
    // Do not replay a burst after lag. The next pulse starts a fresh interval from this tick.
    secondsUntilPulse = intervalSeconds;
    return true;
  }

  public boolean forcePulse() {
    if (isFinished()) {
      return false;
    }
    pulseCount++;
    return true;
  }

  public boolean isFinished() {
    return (maxPulses > 0 && pulseCount >= maxPulses)
        || (durationSeconds > 0.0 && elapsedSeconds >= durationSeconds);
  }

  public void pause() {
    paused = true;
  }

  public void resume() {
    paused = false;
  }

  public boolean isPaused() {
    return paused;
  }

  public int getPulseCount() {
    return pulseCount;
  }

  public double getSecondsUntilPulse() {
    return secondsUntilPulse;
  }
}
