package gg.orbgenesis.scoreboards;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

public final class ScoreboardIds {
  private static final String ASSET_PREFIX = "OrbGenesis_Scoreboard_";

  private ScoreboardIds() {}

  public static String cleanDefinitionId(String raw) {
    return clean(raw, "main");
  }

  public static String cleanTaskId(String raw) {
    return clean(raw, "score");
  }

  private static String clean(String raw, String fallback) {
    String value = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
    value = value.replaceAll("[^a-z0-9_-]+", "_");
    value = value.replaceAll("^_+|_+$", "");
    return value.isEmpty() ? fallback : value;
  }

  public static String assetId(String definitionId) {
    return ASSET_PREFIX + cleanDefinitionId(definitionId);
  }

  public static UUID deterministicUuid(String key) {
    return UUID.nameUUIDFromBytes(("orbgenesis:scoreboards:" + key).getBytes(StandardCharsets.UTF_8));
  }
}
