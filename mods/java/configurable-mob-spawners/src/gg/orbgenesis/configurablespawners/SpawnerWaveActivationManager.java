package gg.orbgenesis.configurablespawners;

import com.hypixel.hytale.server.core.universe.world.World;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** Delivers one Trigger Volume wave request to every loaded spawner with the requested tag. */
final class SpawnerWaveActivationManager {
  private static final long REQUEST_LIFETIME_NANOS = 2_000_000_000L;
  private final Map<World, List<Request>> requestsByWorld = new HashMap<>();
  private long nextSequence;

  synchronized void trigger(World world, String tag) {
    if (world == null || tag == null || tag.isBlank()) return;
    prune();
    requestsByWorld.computeIfAbsent(world, ignored -> new ArrayList<>())
        .add(new Request(++nextSequence, tag, System.nanoTime() + REQUEST_LIFETIME_NANOS));
  }

  synchronized boolean consumeNext(World world, ConfigurableSpawnerComponent config) {
    if (world == null || config == null || config.tags.length == 0) return false;
    prune();
    List<Request> requests = requestsByWorld.get(world);
    if (requests == null) return false;
    for (Request request : requests) {
      if (request.sequence > config.lastSignalSequence && matches(config.tags, request.tag)) {
        config.lastSignalSequence = request.sequence;
        return true;
      }
    }
    return false;
  }

  synchronized void clear() {
    requestsByWorld.clear();
  }

  static boolean matches(String[] tags, String target) {
    if (tags == null || target == null) return false;
    for (String tag : tags) {
      if (target.equals(tag)) return true;
    }
    return false;
  }

  private void prune() {
    long now = System.nanoTime();
    Iterator<Map.Entry<World, List<Request>>> worlds = requestsByWorld.entrySet().iterator();
    while (worlds.hasNext()) {
      List<Request> requests = worlds.next().getValue();
      requests.removeIf(request -> request.expiresAtNanos < now);
      if (requests.isEmpty()) worlds.remove();
    }
  }

  private record Request(long sequence, String tag, long expiresAtNanos) {}
}
