package gg.raynormods.cinema;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

/** A normalized source descriptor. This prototype deliberately does not fetch video bytes. */
public record CinemaSource(Type type, String originalUrl, String mediaId, String canonicalUrl) {
    private static final Pattern YOUTUBE_ID = Pattern.compile("[A-Za-z0-9_-]{11}");

    public enum Type {
        YOUTUBE
    }

    public static ParseResult parse(String value) {
        if (value == null || value.isBlank()) return ParseResult.failure("La URL esta vacia.");
        String trimmed = value.trim();
        try {
            URI uri = new URI(trimmed);
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                return ParseResult.failure("La URL de YouTube debe usar HTTPS.");
            }
            String host = normalizeHost(uri.getHost());
            String id = switch (host) {
                case "youtu.be" -> firstPathSegment(uri.getPath());
                case "youtube.com", "youtube-nocookie.com" -> youtubeId(uri);
                default -> null;
            };
            if (!isValidId(id)) {
                return ParseResult.failure("No se encontro un ID de video de YouTube valido.");
            }
            String canonical = "https://www.youtube.com/watch?v=" + id;
            return ParseResult.success(new CinemaSource(Type.YOUTUBE, trimmed, id, canonical));
        } catch (URISyntaxException exception) {
            return ParseResult.failure("La URL no es valida.");
        }
    }

    public String embedUrl(long startSeconds) {
        long safeStart = Math.max(0, startSeconds);
        return "https://www.youtube.com/embed/" + mediaId
            + "?enablejsapi=1&playsinline=1&start=" + safeStart;
    }

    public String thumbnailUrl() {
        return "https://i.ytimg.com/vi/" + mediaId + "/hqdefault.jpg";
    }

    private static String normalizeHost(String host) {
        if (host == null) return "";
        String normalized = host.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("www.")) normalized = normalized.substring(4);
        if (normalized.startsWith("m.")) normalized = normalized.substring(2);
        if (normalized.startsWith("music.")) normalized = normalized.substring(6);
        return normalized;
    }

    private static String youtubeId(URI uri) {
        String first = firstPathSegment(uri.getPath());
        if ("watch".equals(first)) return queryParameter(uri.getRawQuery(), "v");
        if ("shorts".equals(first) || "live".equals(first) || "embed".equals(first)) {
            return pathSegment(uri.getPath(), 1);
        }
        return null;
    }

    private static String queryParameter(String rawQuery, String key) {
        if (rawQuery == null) return null;
        for (String pair : rawQuery.split("&")) {
            int separator = pair.indexOf('=');
            String rawKey = separator < 0 ? pair : pair.substring(0, separator);
            if (!key.equals(URLDecoder.decode(rawKey, StandardCharsets.UTF_8))) continue;
            String rawValue = separator < 0 ? "" : pair.substring(separator + 1);
            return URLDecoder.decode(rawValue, StandardCharsets.UTF_8);
        }
        return null;
    }

    private static String firstPathSegment(String path) {
        return pathSegment(path, 0);
    }

    private static String pathSegment(String path, int index) {
        if (path == null) return null;
        String[] parts = path.split("/");
        int found = 0;
        for (String part : parts) {
            if (part.isBlank()) continue;
            if (found++ == index) return part;
        }
        return null;
    }

    private static boolean isValidId(String id) {
        return id != null && YOUTUBE_ID.matcher(id).matches();
    }

    public record ParseResult(CinemaSource source, String error) {
        public static ParseResult success(CinemaSource source) { return new ParseResult(source, null); }
        public static ParseResult failure(String error) { return new ParseResult(null, error); }
        public boolean success() { return source != null; }
    }
}
