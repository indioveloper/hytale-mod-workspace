package gg.raynormods.cinema;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

public final class CinemaSessionManager {
    private final Map<String, CinemaSession> sessions = new ConcurrentHashMap<>();
    private final LongSupplier nanoClock;

    public CinemaSessionManager() {
        this(System::nanoTime);
    }

    CinemaSessionManager(LongSupplier nanoClock) {
        this.nanoClock = nanoClock;
    }

    public CreateResult create(String world, String id, String url) {
        String normalizedWorld = normalizeWorld(world);
        String normalizedId = normalizeId(id);
        if (normalizedId == null) {
            return CreateResult.failure("El ID debe contener entre 1 y 32 letras, numeros, _ o -.");
        }
        CinemaSource.ParseResult parsed = CinemaSource.parse(url);
        if (!parsed.success()) return CreateResult.failure(parsed.error());
        CinemaSession session = new CinemaSession(normalizedId, normalizedWorld, parsed.source(), nanoClock);
        if (sessions.putIfAbsent(key(normalizedWorld, normalizedId), session) != null) {
            return CreateResult.failure("Ya existe una sesion con ese ID en este mundo.");
        }
        return CreateResult.success(session.snapshot());
    }

    public CinemaSession get(String world, String id) {
        String normalizedId = normalizeId(id);
        return normalizedId == null ? null : sessions.get(key(normalizeWorld(world), normalizedId));
    }

    public boolean remove(String world, String id) {
        String normalizedId = normalizeId(id);
        return normalizedId != null && sessions.remove(key(normalizeWorld(world), normalizedId)) != null;
    }

    public List<CinemaSession.Snapshot> list(String world) {
        String normalizedWorld = normalizeWorld(world);
        List<CinemaSession.Snapshot> result = new ArrayList<>();
        for (CinemaSession session : sessions.values()) {
            CinemaSession.Snapshot snapshot = session.snapshot();
            if (snapshot.world().equals(normalizedWorld)) result.add(snapshot);
        }
        result.sort(Comparator.comparing(CinemaSession.Snapshot::id));
        return List.copyOf(result);
    }

    private static String key(String world, String id) {
        return world + '\u0000' + id;
    }

    private static String normalizeWorld(String value) {
        return value == null || value.isBlank() ? "default" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeId(String value) {
        if (value == null) return null;
        String id = value.trim().toLowerCase(Locale.ROOT);
        if (id.isEmpty() || id.length() > 32) return null;
        for (int index = 0; index < id.length(); index++) {
            char character = id.charAt(index);
            if (!(character >= 'a' && character <= 'z')
                && !(character >= '0' && character <= '9')
                && character != '_' && character != '-') return null;
        }
        return id;
    }

    public record CreateResult(CinemaSession.Snapshot session, String error) {
        public static CreateResult success(CinemaSession.Snapshot session) {
            return new CreateResult(session, null);
        }

        public static CreateResult failure(String error) {
            return new CreateResult(null, error);
        }

        public boolean success() { return session != null; }
    }
}
