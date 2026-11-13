package gg.raynormods.cinema;

import java.util.Objects;
import java.util.function.LongSupplier;

/**
 * Server-authoritative playback clock. Position is derived from an epoch instead of incremented
 * every tick, so lag and tick-rate variation do not accumulate drift.
 */
public final class CinemaSession {
    public enum State {
        STOPPED,
        PAUSED,
        PLAYING
    }

    private final String id;
    private final String world;
    private final CinemaSource source;
    private final LongSupplier nanoClock;
    private State state = State.STOPPED;
    private double anchorPositionSeconds;
    private long anchorNanos;
    private long revision = 1;

    CinemaSession(String id, String world, CinemaSource source, LongSupplier nanoClock) {
        this.id = Objects.requireNonNull(id);
        this.world = Objects.requireNonNull(world);
        this.source = Objects.requireNonNull(source);
        this.nanoClock = Objects.requireNonNull(nanoClock);
        this.anchorNanos = nanoClock.getAsLong();
    }

    public synchronized void play() {
        if (state == State.PLAYING) return;
        anchorNanos = nanoClock.getAsLong();
        state = State.PLAYING;
        revision++;
    }

    public synchronized void pause() {
        if (state != State.PLAYING) return;
        anchorPositionSeconds = positionAt(nanoClock.getAsLong());
        anchorNanos = nanoClock.getAsLong();
        state = State.PAUSED;
        revision++;
    }

    public synchronized void seek(double seconds) {
        if (!Double.isFinite(seconds)) throw new IllegalArgumentException("Seek must be finite");
        anchorPositionSeconds = Math.max(0.0, seconds);
        anchorNanos = nanoClock.getAsLong();
        revision++;
    }

    public synchronized void stop() {
        anchorPositionSeconds = 0.0;
        anchorNanos = nanoClock.getAsLong();
        state = State.STOPPED;
        revision++;
    }

    public synchronized Snapshot snapshot() {
        long now = nanoClock.getAsLong();
        return new Snapshot(id, world, source, state, positionAt(now), now, revision);
    }

    private double positionAt(long nowNanos) {
        if (state != State.PLAYING) return anchorPositionSeconds;
        long elapsed = Math.max(0L, nowNanos - anchorNanos);
        return anchorPositionSeconds + elapsed / 1_000_000_000.0;
    }

    public record Snapshot(
        String id,
        String world,
        CinemaSource source,
        State state,
        double positionSeconds,
        long serverMonotonicNanos,
        long revision
    ) {}
}
