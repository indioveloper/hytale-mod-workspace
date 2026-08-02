package gg.orbgenesis.particleshapevfx;

import com.hypixel.hytale.codec.codecs.EnumCodec;
import java.util.Arrays;
import java.util.List;
import org.joml.Vector3d;

public final class TestParticleShapePointGenerator {
  private TestParticleShapePointGenerator() {}

  public static void main(String[] args) {
    testLine();
    testCube();
    testSphere();
    testLimits();
    testCodecEnumKeys();
    System.out.println("Particle shape geometry tests passed.");
  }

  private static void testCodecEnumKeys() {
    require(
        Arrays.equals(
            new EnumCodec<>(SpawnParticleShapeEffect.Shape.class).getEnumKeys(),
            new String[] {"CubeEdges", "SphereSurface", "Line"}),
        "Shape JSON values must use the EnumCodec camel-case keys");
    require(
        Arrays.equals(
            new EnumCodec<>(SpawnParticleShapeEffect.CoordinateMode.class).getEnumKeys(),
            new String[] {"RelativeToVolume", "Absolute"}),
        "CoordinateMode JSON values must use the EnumCodec camel-case keys");
  }

  private static void testLine() {
    List<Vector3d> points =
        ParticleShapePointGenerator.line(
            new Vector3d(1, 2, 3), new Vector3d(6, 2, 3), 1.0D, 100);
    require(points.size() == 6, "Five-block line with spacing 1 must contain 6 points");
    require(points.get(0).equals(new Vector3d(1, 2, 3)), "Line must include Start");
    require(points.get(5).equals(new Vector3d(6, 2, 3)), "Line must include End");
  }

  private static void testCube() {
    List<Vector3d> points =
        ParticleShapePointGenerator.cubeEdges(new Vector3d(), 2.0D, 1.0D, 100);
    require(points.size() == 20, "Two-block cube with one midpoint per edge must contain 20 points");
    for (Vector3d point : points) {
      require(
          Math.abs(point.x) <= 1.0D && Math.abs(point.y) <= 1.0D && Math.abs(point.z) <= 1.0D,
          "Cube point escaped its bounds: " + point);
      int boundaryAxes =
          (Math.abs(Math.abs(point.x) - 1.0D) < 1.0E-9D ? 1 : 0)
              + (Math.abs(Math.abs(point.y) - 1.0D) < 1.0E-9D ? 1 : 0)
              + (Math.abs(Math.abs(point.z) - 1.0D) < 1.0E-9D ? 1 : 0);
      require(boundaryAxes >= 2, "Cube point is not on an edge: " + point);
    }
  }

  private static void testSphere() {
    Vector3d center = new Vector3d(4, 5, 6);
    List<Vector3d> points =
        ParticleShapePointGenerator.sphereSurface(center, 4.0D, 0.4D, 500);
    require(points.size() <= 500 && points.size() >= 6, "Sphere point count is invalid");
    for (Vector3d point : points) {
      require(Math.abs(point.distance(center) - 2.0D) < 1.0E-8D, "Sphere radius is not exact");
    }
  }

  private static void testLimits() {
    require(
        ParticleShapePointGenerator.line(new Vector3d(), new Vector3d(100, 0, 0), 0.01D, 32)
                .size()
            == 32,
        "Line must honor MaxPoints");
    require(
        ParticleShapePointGenerator.cubeEdges(new Vector3d(), 100.0D, 0.01D, 128).size()
            <= 128,
        "Cube must honor MaxPoints");
    require(
        ParticleShapePointGenerator.sphereSurface(new Vector3d(), 100.0D, 0.01D, 128).size()
            == 128,
        "Sphere must honor MaxPoints");
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }
}
