package gg.orbgenesis.buildbattle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class BuildBattleTeamPlanner {
  private BuildBattleTeamPlanner() {}

  static List<Plan> plan(List<Entry> players, BuildBattleMode mode, int teamSize) {
    List<Plan> result = new ArrayList<>();
    if (mode == BuildBattleMode.SOLO) {
      int number = 1;
      for (Entry player : players) {
        result.add(new Plan("team-" + number++, player.name(), new ArrayList<>(List.of(player.id()))));
      }
      return result;
    }

    Map<String, List<Plan>> named = new LinkedHashMap<>();
    List<Entry> automatic = new ArrayList<>();
    int serial = 1;
    for (Entry player : players) {
      if (player.preferredTeam().isEmpty()) {
        automatic.add(player);
        continue;
      }
      List<Plan> buckets = named.computeIfAbsent(player.preferredTeam(), ignored -> new ArrayList<>());
      Plan target = buckets.stream()
          .filter(team -> team.members().size() < teamSize)
          .findFirst()
          .orElse(null);
      if (target == null) {
        String suffix = buckets.isEmpty() ? "" : " " + (buckets.size() + 1);
        target = new Plan(
            "team-" + serial++, player.preferredTeam() + suffix, new ArrayList<>());
        buckets.add(target);
        result.add(target);
      }
      target.members().add(player.id());
    }

    for (Entry player : automatic) {
      Plan target = result.stream()
          .filter(team -> team.members().size() < teamSize)
          .min(Comparator.comparingInt(team -> team.members().size()))
          .orElse(null);
      if (target == null) {
        int teamNumber = serial++;
        target = new Plan("team-" + teamNumber, "Team " + teamNumber, new ArrayList<>());
        result.add(target);
      }
      target.members().add(player.id());
    }
    return result;
  }

  record Entry(UUID id, String name, String preferredTeam) {}

  record Plan(String id, String displayName, List<UUID> members) {}
}
