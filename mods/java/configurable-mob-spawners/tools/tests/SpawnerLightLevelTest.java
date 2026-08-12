package gg.orbgenesis.configurablespawners;

public final class SpawnerLightLevelTest {
  public static void main(String[] args) {
    expect(15, SpawnerLightLevel.calculate(0, 0, 63, 64, 1.0), "open sky at noon");
    expect(3, SpawnerLightLevel.calculate(0, 0, 63, 64, 0.25), "open sky at low sun");
    expect(0, SpawnerLightLevel.calculate(0, 0, 70, 64, 1.0), "covered darkness");
    expect(10, SpawnerLightLevel.calculate(10, 0, 70, 64, 1.0), "covered block light");
    expect(7, SpawnerLightLevel.calculate(0, 7, 70, 64, 1.0), "propagated skylight");
    expect(15, SpawnerLightLevel.calculate(30, 0, 70, 64, 1.0), "clamped block light");
    System.out.println("Configurable Mob Spawners light-level checks passed.");
  }

  private static void expect(int expected, int actual, String caseName) {
    if (expected != actual) {
      throw new AssertionError(caseName + ": expected " + expected + ", got " + actual);
    }
  }
}
