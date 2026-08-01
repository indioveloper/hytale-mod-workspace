package gg.orbgenesis.moretriggers;

final class NoMoveExceptionFilter {
  private NoMoveExceptionFilter() {}

  static boolean isExcluded(
      boolean player, String npcRole, boolean excludePlayers, String[] excludedNpcRoles) {
    if (player && excludePlayers) {
      return true;
    }
    if (npcRole == null || npcRole.isBlank() || excludedNpcRoles == null) {
      return false;
    }

    String normalizedRole = npcRole.trim();
    for (String excludedRole : excludedNpcRoles) {
      if (excludedRole != null && normalizedRole.equalsIgnoreCase(excludedRole.trim())) {
        return true;
      }
    }
    return false;
  }
}
