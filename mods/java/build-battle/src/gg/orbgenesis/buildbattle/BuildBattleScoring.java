package gg.orbgenesis.buildbattle;

import java.util.Collection;
import java.util.Map;

public final class BuildBattleScoring {
  private BuildBattleScoring() {}

  public static double average(Collection<Integer> votes) {
    if (votes == null || votes.isEmpty()) {
      return 0.0;
    }
    long total = 0;
    int count = 0;
    for (Integer vote : votes) {
      if (vote != null && vote >= 1 && vote <= 5) {
        total += vote;
        count++;
      }
    }
    return count == 0 ? 0.0 : (double) total / count;
  }

  public static String winningTheme(Collection<String> candidates, Map<String, Integer> votes) {
    String winner = null;
    int best = Integer.MIN_VALUE;
    for (String candidate : candidates) {
      int score = votes.getOrDefault(candidate, 0);
      if (winner == null || score > best) {
        winner = candidate;
        best = score;
      }
    }
    return winner;
  }
}
