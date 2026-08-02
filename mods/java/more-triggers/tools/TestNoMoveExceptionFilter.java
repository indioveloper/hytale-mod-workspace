package gg.orbgenesis.moretriggers;

public final class TestNoMoveExceptionFilter {
  private TestNoMoveExceptionFilter() {}

  public static void main(String[] args) {
    String[] roles = {"Nexus_Avatar_Sword_Runner", " Skeleton_Warrior ", ""};

    assertValue(true, NoMoveExceptionFilter.isExcluded(true, null, true, roles));
    assertValue(false, NoMoveExceptionFilter.isExcluded(true, null, false, roles));
    assertValue(
        true,
        NoMoveExceptionFilter.isExcluded(
            false, "Nexus_Avatar_Sword_Runner", false, roles));
    assertValue(
        true,
        NoMoveExceptionFilter.isExcluded(false, "skeleton_warrior", false, roles));
    assertValue(false, NoMoveExceptionFilter.isExcluded(false, "Kweebec", false, roles));
    assertValue(false, NoMoveExceptionFilter.isExcluded(false, null, false, roles));
    assertValue(false, NoMoveExceptionFilter.isExcluded(false, "Kweebec", false, null));

    System.out.println("No Move exception filter tests passed.");
  }

  private static void assertValue(boolean expected, boolean actual) {
    if (expected != actual) {
      throw new AssertionError("Expected " + expected + " but got " + actual);
    }
  }
}
