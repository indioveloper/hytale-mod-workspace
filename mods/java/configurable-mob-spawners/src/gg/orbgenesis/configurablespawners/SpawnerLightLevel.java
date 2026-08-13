package gg.orbgenesis.configurablespawners;

final class SpawnerLightLevel {
  private SpawnerLightLevel() {}

  static int calculate(
      int blockLight,
      int propagatedSkyLight,
      int highestOpaqueY,
      int sampleY,
      double sunlightFactor) {
    int directSkyLight = sampleY > highestOpaqueY ? 15 : 0;
    int skyLight = Math.max(clamp(propagatedSkyLight, 0, 15), directSkyLight);
    int effectiveSunlight = (int) (skyLight * clamp(sunlightFactor, 0.0, 1.0));
    return Math.max(clamp(blockLight, 0, 15), effectiveSunlight);
  }

  private static int clamp(int value, int minimum, int maximum) {
    return Math.max(minimum, Math.min(maximum, value));
  }

  private static double clamp(double value, double minimum, double maximum) {
    return Math.max(minimum, Math.min(maximum, value));
  }
}
