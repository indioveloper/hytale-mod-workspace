package gg.orbgenesis.particleshapevfx;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import java.util.logging.Level;
import org.joml.Vector3d;

public class SpawnParticleShapeEffect extends TriggerEffect {
  public enum Shape {
    CUBE_EDGES,
    SPHERE_SURFACE,
    LINE
  }

  public enum CoordinateMode {
    RELATIVE_TO_VOLUME,
    ABSOLUTE
  }

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private static final int HARD_MAX_POINTS = 4096;

  public static final BuilderCodec<SpawnParticleShapeEffect> CODEC =
      BuilderCodec.builder(
              SpawnParticleShapeEffect.class,
              SpawnParticleShapeEffect::new,
              TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("Shape", new EnumCodec<>(Shape.class), false),
              (effect, value) -> effect.shape = value != null ? value : Shape.CUBE_EDGES,
              effect -> effect.shape)
          .add()
          .append(
              new KeyedCodec<>("ParticleSystem", Codec.STRING, false),
              (effect, value) -> effect.particleSystem = value,
              effect -> effect.particleSystem)
          .add()
          .append(
              new KeyedCodec<>(
                  "CoordinateMode", new EnumCodec<>(CoordinateMode.class), false),
              (effect, value) ->
                  effect.coordinateMode =
                      value != null ? value : CoordinateMode.RELATIVE_TO_VOLUME,
              effect -> effect.coordinateMode)
          .add()
          .append(
              new KeyedCodec<>("Center", Vector3dUtil.CODEC, false),
              (effect, value) -> effect.center = copyOrZero(value),
              effect -> effect.center)
          .add()
          .append(
              new KeyedCodec<>("Start", Vector3dUtil.CODEC, false),
              (effect, value) -> effect.start = copyOrZero(value),
              effect -> effect.start)
          .add()
          .append(
              new KeyedCodec<>("End", Vector3dUtil.CODEC, false),
              (effect, value) -> effect.end = value != null ? new Vector3d(value) : new Vector3d(5, 0, 0),
              effect -> effect.end)
          .add()
          .append(
              new KeyedCodec<>("Size", Codec.DOUBLE, false),
              (effect, value) -> effect.size = value,
              effect -> effect.size)
          .add()
          .append(
              new KeyedCodec<>("Spacing", Codec.DOUBLE, false),
              (effect, value) -> effect.spacing = value,
              effect -> effect.spacing)
          .add()
          .append(
              new KeyedCodec<>("ParticleScale", Codec.FLOAT, false),
              (effect, value) -> effect.particleScale = value,
              effect -> effect.particleScale)
          .add()
          .append(
              new KeyedCodec<>("Duration", Codec.FLOAT, false),
              (effect, value) -> effect.duration = value,
              effect -> effect.duration)
          .add()
          .append(
              new KeyedCodec<>("MaxPoints", Codec.INTEGER, false),
              (effect, value) -> effect.maxPoints = value,
              effect -> effect.maxPoints)
          .add()
          .build();

  private Shape shape = Shape.CUBE_EDGES;
  private String particleSystem = "OrbGenesis_Shape_Point_Red";
  private CoordinateMode coordinateMode = CoordinateMode.RELATIVE_TO_VOLUME;
  private Vector3d center = new Vector3d();
  private Vector3d start = new Vector3d(-2.5D, 0.0D, 0.0D);
  private Vector3d end = new Vector3d(2.5D, 0.0D, 0.0D);
  private double size = 5.0D;
  private double spacing = 0.12D;
  private float particleScale = 1.0F;
  private float duration = 10.0F;
  private int maxPoints = 512;

  @Override
  public void execute(TriggerContext context) {
    if (particleSystem == null || particleSystem.isBlank() || context.getStore() == null) {
      LOGGER.at(Level.WARNING).log("SpawnParticleShape: particle system or store is unavailable");
      return;
    }

    Vector3d origin = resolveOrigin(context);
    if (origin == null) {
      LOGGER.at(Level.WARNING).log("SpawnParticleShape: Trigger Volume center is unavailable");
      return;
    }

    int pointLimit = Math.max(8, Math.min(HARD_MAX_POINTS, maxPoints));
    double resolvedSpacing = clampFinite(Math.abs(spacing), 0.02D, 64.0D, 0.12D);
    double resolvedSize = clampFinite(Math.abs(size), 0.02D, 512.0D, 5.0D);
    List<Vector3d> points = generatePoints(origin, resolvedSize, resolvedSpacing, pointLimit);
    if (points.isEmpty()) {
      return;
    }

    var store = context.getStore();
    SpatialResource<Ref<EntityStore>, EntityStore> playerSpatial =
        store.getResource(EntityModule.get().getPlayerSpatialResourceType());
    if (playerSpatial == null) {
      LOGGER.at(Level.WARNING).log("SpawnParticleShape: player spatial resource is unavailable");
      return;
    }
    List<Ref<EntityStore>> recipients = SpatialResource.getThreadLocalReferenceList();
    recipients.clear();
    Bounds bounds = Bounds.of(points);
    playerSpatial
        .getSpatialStructure()
        .collect(bounds.center, bounds.radius + 75.0D, recipients);
    if (recipients.isEmpty()) {
      return;
    }

    float resolvedScale = (float) clampFinite(Math.abs(particleScale), 0.01D, 100.0D, 1.0D);
    float resolvedDuration = (float) clampFinite(duration, 0.05D, 3600.0D, 10.0D);
    for (Vector3d point : points) {
      ParticleUtil.spawnParticleEffect(
          particleSystem,
          point.x,
          point.y,
          point.z,
          0.0F,
          0.0F,
          0.0F,
          resolvedScale,
          null,
          null,
          recipients,
          store,
          resolvedDuration);
    }
  }

  private List<Vector3d> generatePoints(
      Vector3d origin, double resolvedSize, double resolvedSpacing, int pointLimit) {
    if (shape == Shape.LINE) {
      return ParticleShapePointGenerator.line(
          resolveCoordinate(origin, start),
          resolveCoordinate(origin, end),
          resolvedSpacing,
          pointLimit);
    }

    Vector3d resolvedCenter = resolveCoordinate(origin, center);
    if (shape == Shape.SPHERE_SURFACE) {
      return ParticleShapePointGenerator.sphereSurface(
          resolvedCenter, resolvedSize, resolvedSpacing, pointLimit);
    }
    return ParticleShapePointGenerator.cubeEdges(
        resolvedCenter, resolvedSize, resolvedSpacing, pointLimit);
  }

  private Vector3d resolveOrigin(TriggerContext context) {
    if (coordinateMode == CoordinateMode.ABSOLUTE) {
      return new Vector3d();
    }
    return context.getVolume() == null
        ? null
        : new Vector3d(context.getVolume().getPosition());
  }

  private static Vector3d resolveCoordinate(Vector3d origin, Vector3d configured) {
    return new Vector3d(origin).add(configured == null ? new Vector3d() : configured);
  }

  private static Vector3d copyOrZero(Vector3d value) {
    return value == null ? new Vector3d() : new Vector3d(value);
  }

  private static double clampFinite(
      double value, double minimum, double maximum, double fallback) {
    if (!Double.isFinite(value)) {
      return fallback;
    }
    return Math.max(minimum, Math.min(maximum, value));
  }

  private record Bounds(Vector3d center, double radius) {
    static Bounds of(List<Vector3d> points) {
      double minX = Double.POSITIVE_INFINITY;
      double minY = Double.POSITIVE_INFINITY;
      double minZ = Double.POSITIVE_INFINITY;
      double maxX = Double.NEGATIVE_INFINITY;
      double maxY = Double.NEGATIVE_INFINITY;
      double maxZ = Double.NEGATIVE_INFINITY;
      for (Vector3d point : points) {
        minX = Math.min(minX, point.x);
        minY = Math.min(minY, point.y);
        minZ = Math.min(minZ, point.z);
        maxX = Math.max(maxX, point.x);
        maxY = Math.max(maxY, point.y);
        maxZ = Math.max(maxZ, point.z);
      }
      Vector3d center =
          new Vector3d(
              (minX + maxX) * 0.5D,
              (minY + maxY) * 0.5D,
              (minZ + maxZ) * 0.5D);
      double radius = 0.0D;
      for (Vector3d point : points) {
        radius = Math.max(radius, center.distance(point));
      }
      return new Bounds(center, radius);
    }
  }
}
