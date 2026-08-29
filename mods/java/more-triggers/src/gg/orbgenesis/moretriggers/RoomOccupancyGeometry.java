package gg.orbgenesis.moretriggers;

final class RoomOccupancyGeometry {
  private static final double EPSILON = 1.0E-6D;

  private RoomOccupancyGeometry() {}

  static boolean overlaps(Bounds first, Bounds second) {
    return first.minX < second.maxX - EPSILON
        && first.maxX > second.minX + EPSILON
        && first.minY < second.maxY - EPSILON
        && first.maxY > second.minY + EPSILON
        && first.minZ < second.maxZ - EPSILON
        && first.maxZ > second.minZ + EPSILON;
  }

  record Bounds(
      double minX,
      double minY,
      double minZ,
      double maxX,
      double maxY,
      double maxZ) {}
}
