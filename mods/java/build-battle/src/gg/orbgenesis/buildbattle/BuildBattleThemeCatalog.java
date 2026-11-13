package gg.orbgenesis.buildbattle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class BuildBattleThemeCatalog {
  private static final String DEFAULT_JSON =
      """
      [
        "Castle",
        "Dragon",
        "Pirate ship",
        "Tree house",
        "Volcano",
        "Space station",
        "Underwater city",
        "Ancient temple",
        "Robot",
        "Haunted mansion"
      ]
      """;

  private BuildBattleThemeCatalog() {}

  public static List<String> load(Path dataDirectory) throws IOException {
    Files.createDirectories(dataDirectory);
    Path path = dataDirectory.resolve("themes.json");
    if (!Files.isRegularFile(path)) {
      Files.writeString(path, DEFAULT_JSON, StandardCharsets.UTF_8);
    }

    JsonArray root = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8))
        .getAsJsonArray();
    List<String> result = new ArrayList<>();
    Set<String> seen = new LinkedHashSet<>();
    for (JsonElement element : root) {
      if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
        continue;
      }
      String theme = element.getAsString().trim();
      if (theme.isEmpty() || theme.length() > 64) {
        continue;
      }
      if (seen.add(theme.toLowerCase(Locale.ROOT))) {
        result.add(theme);
      }
    }
    if (result.size() < 2) {
      throw new IOException("themes.json must contain at least two unique non-empty themes");
    }
    return List.copyOf(result);
  }
}
