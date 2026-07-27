package gg.orbgenesis.moretriggers;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferUtil;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBuffer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.PrefabUtil;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import org.joml.Vector3d;
import org.joml.Vector3i;

public class PasteRandomPrefabEffect extends TriggerEffect {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

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
              new KeyedCodec<>("ShowParticles", Codec.BOOLEAN, false),
              (effect, value) -> effect.showParticles = value,
              effect -> effect.showParticles)
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
  public boolean showParticles;

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

    PrefabBuffer buffer;
    try {
      buffer = PrefabBufferUtil.loadBuffer(prefabPath);
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
    try {
      PrefabUtil.paste(
          buffer.newAccess(),
          world,
          blockPosition,
          Rotation.None,
          true,
          new Random(),
          flags,
          false,
          false,
          true,
          context.getStore());
    } catch (Exception e) {
      LOGGER.at(Level.WARNING).withCause(e).log("PasteRandomPrefab: Failed to paste prefab '%s'", prefabPath);
    }
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

  static Path resolveDirectPrefabPath(String rawPath) {
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
}
