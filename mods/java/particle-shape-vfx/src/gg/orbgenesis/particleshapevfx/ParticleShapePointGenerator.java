package gg.orbgenesis.particleshapevfx;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector3d;

final class ParticleShapePointGenerator {
  private static final double MIN_SPACING = 0.02D;
  private static final double KEY_SCALE = 1_000_000.0D;

  private ParticleShapePointGenerator() {}

  static List<Vector3d> line(Vector3d start, Vector3d end, double spacing, int maxPoints) {
    int limit = Math.max(2, maxPoints);
    Vector3d delta = new Vector3d(end).sub(start);
    double length = delta.length();
    if (length < 1.0E-9D) {
      return List.of(new Vector3d(start));
    }

    int segments = Math.max(1, (int) Math.ceil(length / sanitizeSpacing(spacing)));
    segments = Math.min(segments, limit - 1);
    List<Vector3d> points = new ArrayList<>(segments + 1);
    for (int index = 0; index <= segments; index++) {
      double factor = (double) index / segments;
      points.add(new Vector3d(start).fma(factor, delta));
    }
    return points;
  }

  static List<Vector3d> cubeEdges(
      Vector3d center, double size, double spacing, int maxPoints) {
    double resolvedSize = Math.max(MIN_SPACING, Math.abs(size));
    int limit = Math.max(8, maxPoints);
    int desiredSegments =
        Math.max(1, (int) Math.ceil(resolvedSize / sanitizeSpacing(spacing)));
    int maximumSegments = Math.max(1, (limit + 4) / 12);
    int segments = Math.min(desiredSegments, maximumSegments);
    double half = resolvedSize * 0.5D;
    double minX = center.x - half;
    double maxX = center.x + half;
    double minY = center.y - half;
    double maxY = center.y + half;
    double minZ = center.z - half;
    double maxZ = center.z + half;

    Map<PointKey, Vector3d> points = new LinkedHashMap<>();
    for (double y : new double[] {minY, maxY}) {
      for (double z : new double[] {minZ, maxZ}) {
        addSegment(points, new Vector3d(minX, y, z), new Vector3d(maxX, y, z), segments);
      }
    }
    for (double x : new double[] {minX, maxX}) {
      for (double z : new double[] {minZ, maxZ}) {
        addSegment(points, new Vector3d(x, minY, z), new Vector3d(x, maxY, z), segments);
      }
    }
    for (double x : new double[] {minX, maxX}) {
      for (double y : new double[] {minY, maxY}) {
        addSegment(points, new Vector3d(x, y, minZ), new Vector3d(x, y, maxZ), segments);
      }
    }
    return new ArrayList<>(points.values());
  }

  static List<Vector3d> sphereSurface(
      Vector3d center, double diameter, double spacing, int maxPoints) {
    double radius = Math.max(MIN_SPACING, Math.abs(diameter) * 0.5D);
    int limit = Math.max(6, maxPoints);
    double surfaceArea = 4.0D * Math.PI * radius * radius;
    int desiredCount =
        Math.max(6, (int) Math.ceil(surfaceArea / Math.pow(sanitizeSpacing(spacing), 2.0D)));
    int count = Math.min(desiredCount, limit);
    double offset = 2.0D / count;
    double increment = Math.PI * (3.0D - Math.sqrt(5.0D));
    List<Vector3d> points = new ArrayList<>(count);
    for (int index = 0; index < count; index++) {
      double y = index * offset - 1.0D + offset * 0.5D;
      double ringRadius = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
      double angle = index * increment;
      points.add(
          new Vector3d(
              center.x + Math.cos(angle) * ringRadius * radius,
              center.y + y * radius,
              center.z + Math.sin(angle) * ringRadius * radius));
    }
    return points;
  }

  private static void addSegment(
      Map<PointKey, Vector3d> points, Vector3d start, Vector3d end, int segments) {
    Vector3d delta = new Vector3d(end).sub(start);
    for (int index = 0; index <= segments; index++) {
      double factor = (double) index / segments;
      Vector3d point = new Vector3d(start).fma(factor, delta);
      points.putIfAbsent(PointKey.from(point), point);
    }
  }

  private static double sanitizeSpacing(double spacing) {
    if (!Double.isFinite(spacing)) {
      return 0.2D;
    }
    return Math.max(MIN_SPACING, Math.abs(spacing));
  }

  private record PointKey(long x, long y, long z) {
    static PointKey from(Vector3d point) {
      return new PointKey(
          Math.round(point.x * KEY_SCALE),
          Math.round(point.y * KEY_SCALE),
          Math.round(point.z * KEY_SCALE));
    }
  }
}
