package gg.orbgenesis.buildbattle;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.joml.Vector3i;

public final class BuildBattleCoreTest {
  public static void main(String[] args) throws Exception {
    Path directory = Files.createTempDirectory("build-battle-core-test");
    try {
      BuildBattleConfig config = BuildBattleConfig.load(directory);
      require(config.minimumPlayers() == 2, "default minimum players");
      require(config.teamSize() == 2, "default team size");
      require(config.plots().size() == 4, "default plots");
      require(
          config.plots().get(0).contains("DEFAULT", new Vector3i(-32, 65, -32)),
          "plot containment and case-insensitive world");
      require(
          !config.plots().get(0).contains("default", new Vector3i(0, 65, 0)),
          "plot boundary exclusion");
      require(config.plots().get(0).resetPrefab().isEmpty(), "optional reset prefab");
      require(
          config.plots().get(0).resetOrigin().equals(new Vector3i(-64, 0, -64)),
          "reset origin");

      List<String> defaults = BuildBattleThemeCatalog.load(directory);
      require(defaults.size() >= 3, "default theme catalog");
      Files.writeString(
          directory.resolve("themes.json"),
          "[\"Castle\", \" castle \", \"Dragon\", \"\", 4]",
          StandardCharsets.UTF_8);
      List<String> deduplicated = BuildBattleThemeCatalog.load(directory);
      require(deduplicated.equals(List.of("Castle", "Dragon")), "theme normalization");

      require(BuildBattleScoring.average(List.of(5, 4, 1)) == (10.0 / 3.0), "average");
      require(BuildBattleScoring.average(List.of()) == 0.0, "empty average");
      require(
          "Dragon".equals(
              BuildBattleScoring.winningTheme(
                  List.of("Dragon", "Castle"), Map.of("Dragon", 2, "Castle", 2))),
          "ties preserve randomized candidate order");

      UUID first = UUID.randomUUID();
      UUID second = UUID.randomUUID();
      UUID third = UUID.randomUUID();
      UUID fourth = UUID.randomUUID();
      var entries = List.of(
          new BuildBattleTeamPlanner.Entry(first, "One", "red"),
          new BuildBattleTeamPlanner.Entry(second, "Two", "red"),
          new BuildBattleTeamPlanner.Entry(third, "Three", "red"),
          new BuildBattleTeamPlanner.Entry(fourth, "Four", ""));
      var teamPlans = BuildBattleTeamPlanner.plan(entries, BuildBattleMode.TEAMS, 2);
      require(teamPlans.size() == 2, "full preferred teams split into capped buckets");
      require(teamPlans.get(0).members().equals(List.of(first, second)), "preferred team members");
      require(teamPlans.get(1).members().equals(List.of(third, fourth)), "automatic balancing");
      require(teamPlans.get(1).displayName().equals("red 2"), "split team display name");
      require(
          BuildBattleTeamPlanner.plan(entries, BuildBattleMode.SOLO, 2).size() == 4,
          "solo assigns one plot per player");
      System.out.println("Build Battle core tests passed.");
    } finally {
      deleteTree(directory);
    }
  }

  private static void deleteTree(Path root) throws Exception {
    try (var paths = Files.walk(root)) {
      for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) {
        Files.deleteIfExists(path);
      }
    }
  }

  private static void require(boolean condition, String label) {
    if (!condition) {
      throw new AssertionError(label);
    }
  }
}
