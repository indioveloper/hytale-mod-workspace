package gg.orbgenesis.buildbattle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3d;
import org.joml.Vector3i;

public record BuildBattleConfig(
    int minimumPlayers,
    int teamSize,
    int themeCandidates,
    int themeVoteSeconds,
    int buildSeconds,
    int secondsPerBuildVote,
    int resultsSeconds,
    List<BuildBattlePlot> plots) {

  private static final String DEFAULT_JSON =
      """
      {
        "minimumPlayers": 2,
        "teamSize": 2,
        "themeCandidates": 3,
        "themeVoteSeconds": 30,
        "buildSeconds": 300,
        "secondsPerBuildVote": 20,
        "resultsSeconds": 15,
        "plots": [
          {
            "id": "plot-1",
            "world": "default",
            "min": [-64, 0, -64],
            "max": [-1, 255, -1],
            "spawn": [-32.5, 65, -32.5],
            "view": [-32.5, 72, -72.5],
            "yaw": 0,
            "resetPrefab": "",
            "resetOrigin": [-64, 0, -64]
          },
          {
            "id": "plot-2",
            "world": "default",
            "min": [1, 0, -64],
            "max": [64, 255, -1],
            "spawn": [32.5, 65, -32.5],
            "view": [32.5, 72, -72.5],
            "yaw": 0,
            "resetPrefab": "",
            "resetOrigin": [1, 0, -64]
          },
          {
            "id": "plot-3",
            "world": "default",
            "min": [-64, 0, 1],
            "max": [-1, 255, 64],
            "spawn": [-32.5, 65, 32.5],
            "view": [-32.5, 72, 72.5],
            "yaw": 3.1415927,
            "resetPrefab": "",
            "resetOrigin": [-64, 0, 1]
          },
          {
            "id": "plot-4",
            "world": "default",
            "min": [1, 0, 1],
            "max": [64, 255, 64],
            "spawn": [32.5, 65, 32.5],
            "view": [32.5, 72, 72.5],
            "yaw": 3.1415927,
            "resetPrefab": "",
            "resetOrigin": [1, 0, 1]
          }
        ]
      }
      """;

  public static BuildBattleConfig load(Path dataDirectory) throws IOException {
    Files.createDirectories(dataDirectory);
    Path path = dataDirectory.resolve("build-battle.json");
    if (!Files.isRegularFile(path)) {
      Files.writeString(path, DEFAULT_JSON, StandardCharsets.UTF_8);
    }

    JsonObject root = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8))
        .getAsJsonObject();
    List<BuildBattlePlot> plots = new ArrayList<>();
    JsonArray plotArray = root.has("plots") ? root.getAsJsonArray("plots") : new JsonArray();
    for (JsonElement element : plotArray) {
      if (!element.isJsonObject()) {
        continue;
      }
      BuildBattlePlot plot = decodePlot(element.getAsJsonObject());
      if (plot != null) {
        plots.add(plot);
      }
    }

    return new BuildBattleConfig(
        integer(root, "minimumPlayers", 2, 1, 128),
        integer(root, "teamSize", 2, 2, 32),
        integer(root, "themeCandidates", 3, 2, 5),
        integer(root, "themeVoteSeconds", 30, 5, 600),
        integer(root, "buildSeconds", 300, 10, 86400),
        integer(root, "secondsPerBuildVote", 20, 5, 600),
        integer(root, "resultsSeconds", 15, 5, 600),
        List.copyOf(plots));
  }

  private static BuildBattlePlot decodePlot(JsonObject object) {
    String id = string(object, "id", "").trim();
    String world = string(object, "world", "default").trim();
    Vector3i first = intVector(object.get("min"));
    Vector3i second = intVector(object.get("max"));
    Vector3d spawn = doubleVector(object.get("spawn"));
    Vector3d view = doubleVector(object.get("view"));
    if (id.isEmpty() || world.isEmpty() || first == null || second == null || spawn == null) {
      return null;
    }
    Vector3i minimum = new Vector3i(
        Math.min(first.x, second.x), Math.min(first.y, second.y), Math.min(first.z, second.z));
    Vector3i maximum = new Vector3i(
        Math.max(first.x, second.x), Math.max(first.y, second.y), Math.max(first.z, second.z));
    if (view == null) {
      view = new Vector3d(spawn).add(0.0, 6.0, -12.0);
    }
    float yaw = object.has("yaw") ? object.get("yaw").getAsFloat() : 0.0f;
    String resetPrefab = string(object, "resetPrefab", "").trim();
    Vector3i resetOrigin = intVector(object.get("resetOrigin"));
    if (resetOrigin == null) {
      resetOrigin = new Vector3i(minimum);
    }
    return new BuildBattlePlot(
        id, world, minimum, maximum, spawn, view, yaw, resetPrefab, resetOrigin);
  }

  private static int integer(JsonObject root, String key, int fallback, int min, int max) {
    int value = root.has(key) ? root.get(key).getAsInt() : fallback;
    return Math.max(min, Math.min(max, value));
  }

  private static String string(JsonObject root, String key, String fallback) {
    return root.has(key) ? root.get(key).getAsString() : fallback;
  }

  private static Vector3i intVector(JsonElement element) {
    if (element == null || !element.isJsonArray() || element.getAsJsonArray().size() != 3) {
      return null;
    }
    JsonArray array = element.getAsJsonArray();
    return new Vector3i(array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt());
  }

  private static Vector3d doubleVector(JsonElement element) {
    if (element == null || !element.isJsonArray() || element.getAsJsonArray().size() != 3) {
      return null;
    }
    JsonArray array = element.getAsJsonArray();
    return new Vector3d(
        array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble());
  }
}
