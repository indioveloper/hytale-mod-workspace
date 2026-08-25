package gg.orbgenesis.moretriggers;

public final class RoomOccupancyGeometryTest {
  private RoomOccupancyGeometryTest() {}

  public static void main(String[] args) {
    var room = bounds(0, 0, 0, 19, 8, 19);
    assert RoomOccupancyGeometry.overlaps(room, bounds(18, 0, 0, 37, 8, 19));
    assert !RoomOccupancyGeometry.overlaps(room, bounds(19, 0, 0, 38, 8, 19));
    assert !RoomOccupancyGeometry.overlaps(room, bounds(0, 8, 0, 19, 16, 19));
    assert RoomOccupancyGeometry.overlaps(room, bounds(0, 7.5, 0, 19, 15.5, 19));
    assert RoomOccupancyGeometry.overlaps(room, bounds(0, 0, 18, 19, 8, 37));
    System.out.println("Room occupancy geometry tests passed.");
  }

  private static RoomOccupancyGeometry.Bounds bounds(
      double minX,
      double minY,
      double minZ,
      double maxX,
      double maxY,
      double maxZ) {
    return new RoomOccupancyGeometry.Bounds(minX, minY, minZ, maxX, maxY, maxZ);
  }
}
