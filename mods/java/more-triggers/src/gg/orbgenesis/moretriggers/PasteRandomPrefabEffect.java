package gg.orbgenesis.moretriggers;

import com.hypixel.hytale.builtin.triggervolumes.YawRotation;
import com.hypixel.hytale.builtin.triggervolumes.EntityTargetType;
import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.effect.EffectOrigin;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.PastePrefabEffect;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.builtin.triggervolumes.shape.BoxShape;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferSelectionConverter;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferUtil;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBuffer;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.PrefabUtil;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import org.joml.Vector3d;
import org.joml.Vector3i;

public class PasteRandomPrefabEffect extends PastePrefabEffect {
  private static final String OCCUPANCY_TAG = "procedural_room";
  private static final String DEFAULT_OCCUPANCY_GROUP = "default";
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private static final ConcurrentMap<RotatedPrefabKey, PrefabBuffer> ROTATED_PREFABS =
      new ConcurrentHashMap<>();

  public static final BuilderCodec<PasteRandomPrefabEffect> CODEC =
      BuilderCodec.builder(
              PasteRandomPrefabEffect.class,
              PasteRandomPrefabEffect::new,
              TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("Prefab1", Codec.STRING, false),
              (effect, value) -> effect.prefab1 = value,
              effect -> effect.prefab1)
          .add()
          .append(
              new KeyedCodec<>("Weight1", Codec.STRING, false),
              (effect, value) -> effect.weight1 = value,
              effect -> effect.weight1)
          .add()
          .append(
              new KeyedCodec<>("Prefab2", Codec.STRING, false),
              (effect, value) -> effect.prefab2 = value,
              effect -> effect.prefab2)
          .add()
          .append(
              new KeyedCodec<>("Weight2", Codec.STRING, false),
              (effect, value) -> effect.weight2 = value,
              effect -> effect.weight2)
          .add()
          .append(
              new KeyedCodec<>("Prefabs", Codec.STRING),
              (effect, value) -> effect.prefabs = value,
              effect -> effect.prefabs)
          .add()
          .append(
              new KeyedCodec<>("UseWeights", Codec.BOOLEAN, false),
              (effect, value) -> effect.useWeights = value,
              effect -> effect.useWeights)
          .add()
          .append(
              new KeyedCodec<>("Position", Vector3dUtil.CODEC, false),
              (effect, value) -> effect.position = value,
              effect -> effect.position)
          .add()
          .append(
              new KeyedCodec<>("AtVolumeOrigin", Codec.BOOLEAN, false),
              (effect, value) -> effect.atVolumeOrigin = value,
              effect -> effect.atVolumeOrigin)
          .add()
          .append(
              new KeyedCodec<>("Yaw", Rotation.CODEC, false),
              (effect, value) -> effect.yaw = value,
              effect -> effect.yaw)
          .add()
          .append(
              new KeyedCodec<>("ShowParticles", Codec.BOOLEAN, false),
              (effect, value) -> effect.showParticles = value,
              effect -> effect.showParticles)
          .add()
          .append(
              new KeyedCodec<>("PreventOverlap", Codec.BOOLEAN, false),
              (effect, value) -> effect.preventOverlap = value,
              effect -> effect.preventOverlap)
          .add()
          .append(
              new KeyedCodec<>("OccupancyGroup", Codec.STRING, false),
              (effect, value) -> effect.occupancyGroup = value,
              effect -> effect.occupancyGroup)
          .add()
          .build();

  public String prefab1 = "";
  public String weight1 = "";
  public String prefab2 = "";
  public String weight2 = "";
  public String prefabs = "";
  public boolean useWeights;
  public Vector3d position;
  public boolean atVolumeOrigin = true;
  public Rotation yaw = Rotation.None;
  public boolean showParticles;
  public boolean preventOverlap;
  public String occupancyGroup = DEFAULT_OCCUPANCY_GROUP;

  /**
   * The vanilla inspector enables its Show/Hide Preview control for PastePrefabEffect instances.
   * Expose the first resolvable random choice as a stable editor preview; execution remains random.
   */
  @Override
  public String getPrefabRelPath() {
    List<PrefabChoice> choices = collectChoices();
    for (PrefabChoice choice : choices) {
      if (resolveDirectPrefabPath(choice.path) != null) {
        return choice.path;
      }
    }
    return choices.isEmpty() ? null : choices.get(0).path;
  }

  @Override
  public String getPrefabListId() {
    return null;
  }

  @Override
  public Vector3d getPosition() {
    return position == null ? null : new Vector3d(position);
  }

  @Override
  public EffectOrigin getOrigin() {
    return atVolumeOrigin ? EffectOrigin.VOLUME_ORIGIN : EffectOrigin.WORLD_ABSOLUTE;
  }

  @Override
  public Rotation getRotation() {
    return yaw == null ? Rotation.None : yaw;
  }

  @Override
  public void rotateInPlace(float yawRadians, Vector3d volumeOrigin) {
    if (!atVolumeOrigin) {
      return;
    }
    if (position != null) {
      YawRotation.rotate(position, yawRadians);
    }
    int quarterTurns = YawRotation.quarterTurns(yawRadians);
    if (quarterTurns > 0) {
      yaw = YawRotation.rotate(getRotation(), quarterTurns);
    }
  }

  @Override
  public void execute(TriggerContext context) {
    PrefabChoice choice = choosePrefab(collectChoices(), useWeights);
    if (choice == null) {
      return;
    }

    Path prefabPath = resolveDirectPrefabPath(choice.path);
    if (prefabPath == null) {
      LOGGER.at(Level.WARNING).log("PasteRandomPrefab: Prefab '%s' not found", choice.path);
      return;
    }

    Rotation requestedYaw = yaw == null ? Rotation.None : yaw;
    PrefabBuffer buffer;
    try {
      buffer = loadPasteBuffer(prefabPath, requestedYaw);
    } catch (Exception e) {
      LOGGER.at(Level.WARNING).withCause(e).log("PasteRandomPrefab: Failed to load prefab '%s'", prefabPath);
      return;
    }
    if (buffer == null || context.getStore() == null) {
      return;
    }

    EntityStore entityStore = context.getStore().getExternalData();
    World world = entityStore == null ? null : entityStore.getWorld();
    if (world == null || context.getVolume() == null) {
      return;
    }

    Vector3d pastePosition = resolvePastePosition(context);
    Vector3i blockPosition =
        new Vector3i(
            (int) Math.floor(pastePosition.x()),
            (int) Math.floor(pastePosition.y()),
            (int) Math.floor(pastePosition.z()));

    int flags = showParticles ? 0 : 4;
    TriggerVolumeManager volumeManager = null;
    String reservationId = null;
    if (preventOverlap) {
      volumeManager =
          context
              .getStore()
              .getResource(TriggerVolumesPlugin.get().getManagerResourceType());
      if (volumeManager == null) {
        LOGGER.at(Level.WARNING).log(
            "PasteRandomPrefab: overlap protection requested but TriggerVolumeManager is unavailable");
        return;
      }

      String group = normalizedOccupancyGroup();
      RoomOccupancyGeometry.Bounds candidate = calculateBounds(buffer, blockPosition);
      synchronized (volumeManager) {
        if (overlapsReservedRoom(volumeManager, candidate, group)) {
          LOGGER.at(Level.FINE).log(
              "PasteRandomPrefab: skipped occupied room area for group '%s'", group);
          return;
        }
        reservationId = reserveRoom(volumeManager, world, candidate, group, choice.path);
      }
    }

    try {
      PrefabUtil.paste(
          buffer.newAccess(),
          world,
          blockPosition,
          Rotation.None,
          new Random(),
          1,
          flags,
          context.getStore());
    } catch (Exception e) {
      if (volumeManager != null && reservationId != null) {
        volumeManager.unregister(reservationId);
        volumeManager.notifyViewersRemove(reservationId);
      }
      LOGGER.at(Level.WARNING).withCause(e).log("PasteRandomPrefab: Failed to paste prefab '%s'", prefabPath);
    }
  }

  private String normalizedOccupancyGroup() {
    if (occupancyGroup == null || occupancyGroup.isBlank()) {
      return DEFAULT_OCCUPANCY_GROUP;
    }
    return occupancyGroup.trim();
  }

  private static RoomOccupancyGeometry.Bounds calculateBounds(
      PrefabBuffer buffer, Vector3i blockPosition) {
    var access = buffer.newAccess();
    return new RoomOccupancyGeometry.Bounds(
        blockPosition.x() + access.getMinX(),
        blockPosition.y() + access.getMinY(),
        blockPosition.z() + access.getMinZ(),
        blockPosition.x() + access.getMaxX() + 1.0D,
        blockPosition.y() + access.getMaxY() + 1.0D,
        blockPosition.z() + access.getMaxZ() + 1.0D);
  }

  private static boolean overlapsReservedRoom(
      TriggerVolumeManager manager,
      RoomOccupancyGeometry.Bounds candidate,
      String occupancyGroup) {
    Vector3d existingMin = new Vector3d();
    Vector3d existingMax = new Vector3d();
    for (VolumeEntry volume : manager.getVolumes()) {
      if (volume.isPendingDestroy()
          || !occupancyGroup.equals(volume.getRawTags().get(OCCUPANCY_TAG))) {
        continue;
      }
      volume.getShape().getWorldAABB(volume.getPosition(), existingMin, existingMax);
      var existing =
          new RoomOccupancyGeometry.Bounds(
              existingMin.x(),
              existingMin.y(),
              existingMin.z(),
              existingMax.x(),
              existingMax.y(),
              existingMax.z());
      if (RoomOccupancyGeometry.overlaps(candidate, existing)) {
        return true;
      }
    }
    return false;
  }

  private static String reserveRoom(
      TriggerVolumeManager manager,
      World world,
      RoomOccupancyGeometry.Bounds bounds,
      String occupancyGroup,
      String prefabId) {
    String id = manager.generateUniqueVolumeId();
    Vector3d origin = new Vector3d(bounds.minX(), bounds.minY(), bounds.minZ());
    BoxShape shape =
        new BoxShape(
            new Vector3d(),
            new Vector3d(
                bounds.maxX() - bounds.minX(),
                bounds.maxY() - bounds.minY(),
                bounds.maxZ() - bounds.minZ()));
    VolumeEntry reservation =
        new VolumeEntry(
            id,
            world.getName().toLowerCase(java.util.Locale.ROOT),
            origin,
            shape,
            Collections.emptyList(),
            EnumSet.of(EntityTargetType.PLAYER),
            false);
    reservation.setName("__procedural_room_" + id.substring(0, 8));
    reservation.setTags(Map.of(OCCUPANCY_TAG, occupancyGroup, "room_prefab", prefabId));
    manager.register(id, reservation);
    manager.notifyViewersAdd(reservation);
    return id;
  }

  /**
   * Pre-rotates the complete selection instead of asking {@link PrefabUtil} to rotate while pasting.
   * In pre.13.1 the latter rotates block coordinates correctly, but rotates prefab entities around
   * a block corner and does not compose their transform yaw. Trigger Volumes are prefab entities,
   * so 90/270 degree pastes otherwise shift them by one cell and leave their shapes/effects facing
   * the original direction. BlockSelection uses cell-centred entity rotation and composes yaw.
   */
  private static PrefabBuffer loadPasteBuffer(Path prefabPath, Rotation requestedYaw) {
    if (requestedYaw == Rotation.None) {
      return PrefabBufferUtil.loadBuffer(prefabPath);
    }

    Path normalizedPath = prefabPath.toAbsolutePath().normalize();
    return ROTATED_PREFABS.computeIfAbsent(
        new RotatedPrefabKey(normalizedPath, requestedYaw),
        key -> {
          BlockSelection source = PrefabStore.get().getPrefab(key.path);
          BlockSelection rotated = source.rotate(Axis.Y, key.yaw.getDegrees());
          return PrefabBufferSelectionConverter.toPrefabBuffer(key.path, rotated);
        });
  }

  private List<PrefabChoice> collectChoices() {
    List<PrefabChoice> choices = new ArrayList<>();
    addChoice(choices, prefab1, weight1);
    addChoice(choices, prefab2, weight2);
    choices.addAll(parseChoices(prefabs));
    return choices;
  }

  private static void addChoice(List<PrefabChoice> choices, String path, String weight) {
    if (path == null || path.isBlank()) {
      return;
    }
    choices.add(new PrefabChoice(path.trim(), parseProbability(weight)));
  }

  private Vector3d resolvePastePosition(TriggerContext context) {
    if (atVolumeOrigin) {
      Vector3d resolved = new Vector3d(context.getVolume().getPosition());
      if (position != null) {
        resolved.add(position);
      }
      return resolved;
    }
    return position == null ? new Vector3d() : new Vector3d(position);
  }

  public static Path resolveDirectPrefabPath(String rawPath) {
    if (rawPath == null || rawPath.isBlank()) {
      return null;
    }

    String normalized = rawPath.replace('\\', '/').trim();
    PrefabStore store = PrefabStore.get();
    Path path = store.findAssetPrefabPath(normalized);
    if (path != null) {
      return path;
    }
    if (!normalized.endsWith(".prefab.json")) {
      path = store.findAssetPrefabPath(normalized + ".prefab.json");
    }
    return path;
  }

  static List<PrefabChoice> parseChoices(String rawChoices) {
    List<PrefabChoice> choices = new ArrayList<>();
    if (rawChoices == null || rawChoices.isBlank()) {
      return choices;
    }

    for (String rawEntry : rawChoices.split("[,;|\\n\\r]+")) {
      String entry = rawEntry.trim();
      if (entry.isEmpty()) {
        continue;
      }

      int separator = findWeightSeparator(entry);
      if (separator < 0) {
        choices.add(new PrefabChoice(entry, 1.0D));
        continue;
      }

      String path = entry.substring(0, separator).trim();
      String rawWeight = entry.substring(separator + 1).trim();
      if (path.isEmpty()) {
        continue;
      }

      double weight = 1.0D;
      try {
        weight = Double.parseDouble(rawWeight);
      } catch (NumberFormatException ignored) {
        weight = 1.0D;
      }
      choices.add(new PrefabChoice(path, clampProbability(weight)));
    }
    return choices;
  }

  private static int findWeightSeparator(String entry) {
    int equals = entry.lastIndexOf('=');
    int colon = entry.lastIndexOf(':');
    return Math.max(equals, colon);
  }

  static PrefabChoice choosePrefab(List<PrefabChoice> choices, boolean weighted) {
    if (choices.isEmpty()) {
      return null;
    }
    if (!weighted) {
      return choices.get(ThreadLocalRandom.current().nextInt(choices.size()));
    }

    double totalWeight = 0.0D;
    for (PrefabChoice choice : choices) {
      totalWeight += clampProbability(choice.weight);
    }
    if (totalWeight <= 0.0D) {
      return choices.get(ThreadLocalRandom.current().nextInt(choices.size()));
    }

    double roll = ThreadLocalRandom.current().nextDouble(totalWeight);
    for (PrefabChoice choice : choices) {
      roll -= clampProbability(choice.weight);
      if (roll <= 0.0D) {
        return choice;
      }
    }
    return choices.get(choices.size() - 1);
  }

  private static double clampProbability(double weight) {
    if (Double.isNaN(weight) || Double.isInfinite(weight)) {
      return 0.0D;
    }
    return Math.max(0.0D, Math.min(1.0D, weight));
  }

  private static double parseProbability(String rawWeight) {
    if (rawWeight == null || rawWeight.isBlank()) {
      return 1.0D;
    }
    try {
      return clampProbability(Double.parseDouble(rawWeight.trim()));
    } catch (NumberFormatException ignored) {
      return 1.0D;
    }
  }

  static final class PrefabChoice {
    final String path;
    final double weight;

    PrefabChoice(String path, double weight) {
      this.path = path;
      this.weight = weight;
    }
  }

  private record RotatedPrefabKey(Path path, Rotation yaw) {}
}
