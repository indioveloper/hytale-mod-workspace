package gg.raynormods.desertsurvival.vitals;

import java.util.Locale;

record HeatTier(int level, String label, double biomeHeat) {
  static HeatTier fromEnvironment(String environmentId) {
    String id = environmentId == null ? "" : environmentId.toLowerCase(Locale.ROOT);
    if (containsZone(id, 4)) {
      return new HeatTier(3, "ZONA 4", 3.0);
    }
    if (containsZone(id, 3)) {
      return new HeatTier(0, "ZONA 3", 0.0);
    }
    if (containsZone(id, 2)) {
      return new HeatTier(2, "ZONA 2", 2.0);
    }
    if (containsZone(id, 1)) {
      return new HeatTier(1, "ZONA 1", 1.0);
    }
    return new HeatTier(1, "ZONA 1 (FALLBACK)", 1.0);
  }

  private static boolean containsZone(String value, int zone) {
    return value.contains("zone" + zone)
        || value.contains("zone_" + zone)
        || value.contains("zone-" + zone)
        || value.contains("zone " + zone);
  }
}
