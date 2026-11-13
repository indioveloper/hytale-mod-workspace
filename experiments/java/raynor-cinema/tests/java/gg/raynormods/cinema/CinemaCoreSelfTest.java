package gg.raynormods.cinema;

import java.util.concurrent.atomic.AtomicLong;

public final class CinemaCoreSelfTest {
    public static void main(String[] args) {
        parsesSupportedYoutubeUrls();
        rejectsNonYoutubeAndInvalidIds();
        keepsAnAuthoritativeMonotonicClock();
        isolatesWorldsAndNormalizesIds();
        System.out.println("Cinema core self-test passed.");
    }

    private static void parsesSupportedYoutubeUrls() {
        String id = "M7lc1UVf-VE";
        requireId("https://www.youtube.com/watch?v=" + id + "&t=30", id);
        requireId("https://youtu.be/" + id, id);
        requireId("https://youtube.com/shorts/" + id, id);
        requireId("https://m.youtube.com/live/" + id, id);
        requireId("https://www.youtube-nocookie.com/embed/" + id, id);
        CinemaSource source = CinemaSource.parse("https://youtu.be/" + id).source();
        require(source.embedUrl(42).contains("start=42"), "embed URL carries synchronized start");
        require(source.thumbnailUrl().contains(id), "thumbnail URL carries ID");
    }

    private static void rejectsNonYoutubeAndInvalidIds() {
        require(!CinemaSource.parse("http://youtu.be/M7lc1UVf-VE").success(), "HTTP rejected");
        require(!CinemaSource.parse("https://example.com/M7lc1UVf-VE").success(), "foreign host rejected");
        require(!CinemaSource.parse("https://youtube.com/watch?v=short").success(), "invalid ID rejected");
    }

    private static void keepsAnAuthoritativeMonotonicClock() {
        AtomicLong now = new AtomicLong(10_000_000_000L);
        CinemaSessionManager manager = new CinemaSessionManager(now::get);
        CinemaSessionManager.CreateResult created = manager.create("world", "main", "https://youtu.be/M7lc1UVf-VE");
        require(created.success(), "session created");
        CinemaSession session = manager.get("world", "main");
        session.seek(30);
        session.play();
        now.addAndGet(2_500_000_000L);
        requireClose(session.snapshot().positionSeconds(), 32.5, "playing clock advances from epoch");
        session.pause();
        now.addAndGet(4_000_000_000L);
        requireClose(session.snapshot().positionSeconds(), 32.5, "paused clock remains fixed");
        session.play();
        now.addAndGet(500_000_000L);
        requireClose(session.snapshot().positionSeconds(), 33.0, "resume continues from paused position");
        session.stop();
        requireClose(session.snapshot().positionSeconds(), 0.0, "stop rewinds");
    }

    private static void isolatesWorldsAndNormalizesIds() {
        CinemaSessionManager manager = new CinemaSessionManager(() -> 0L);
        require(manager.create("A", "ROOM_1", "https://youtu.be/M7lc1UVf-VE").success(), "world A created");
        require(manager.create("B", "room_1", "https://youtu.be/M7lc1UVf-VE").success(), "world B created");
        require(manager.get("a", "room_1") != null, "case normalized");
        require(manager.list("a").size() == 1, "world list isolated");
        require(!manager.create("A", "bad id", "https://youtu.be/M7lc1UVf-VE").success(), "unsafe ID rejected");
    }

    private static void requireId(String url, String expected) {
        CinemaSource.ParseResult result = CinemaSource.parse(url);
        require(result.success(), "URL parsed: " + url);
        require(expected.equals(result.source().mediaId()), "ID normalized: " + url);
    }

    private static void requireClose(double actual, double expected, String message) {
        require(Math.abs(actual - expected) < 0.0001, message + " (expected " + expected + ", got " + actual + ")");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
