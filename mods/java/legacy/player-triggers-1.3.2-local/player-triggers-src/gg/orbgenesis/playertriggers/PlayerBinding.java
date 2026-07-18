package gg.orbgenesis.playertriggers;

import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.UUID;
import org.joml.Vector3d;

final class PlayerBinding {
  static final String PLAYER_KEY = "orbgenesis:following_player";
  static final String OFFSET_X_KEY = "orbgenesis:following_offset_x";
  static final String OFFSET_Y_KEY = "orbgenesis:following_offset_y";
  static final String OFFSET_Z_KEY = "orbgenesis:following_offset_z";

  private PlayerBinding() {}

  static void bind(
      TriggerVolumeManager manager,
      VolumeEntry volume,
      UUID playerId,
      Vector3d offset,
      Ref<EntityStore> source) {
    manager.setTag(volume.getId(), PLAYER_KEY, playerId.toString(), source, playerId);
    manager.setTag(volume.getId(), OFFSET_X_KEY, Double.toString(offset.x), source, playerId);
    manager.setTag(volume.getId(), OFFSET_Y_KEY, Double.toString(offset.y), source, playerId);
    manager.setTag(volume.getId(), OFFSET_Z_KEY, Double.toString(offset.z), source, playerId);
  }

  static void unbind(
      TriggerVolumeManager manager,
      VolumeEntry volume,
      Ref<EntityStore> source,
      UUID playerId) {
    manager.removeTag(volume.getId(), PLAYER_KEY, source, playerId);
    manager.removeTag(volume.getId(), OFFSET_X_KEY, source, playerId);
    manager.removeTag(volume.getId(), OFFSET_Y_KEY, source, playerId);
    manager.removeTag(volume.getId(), OFFSET_Z_KEY, source, playerId);
  }

  static void unbindIfBoundTo(
      TriggerVolumeManager manager,
      VolumeEntry volume,
      Ref<EntityStore> source,
      UUID playerId) {
    if (playerId != null && playerId.equals(getPlayerId(volume))) {
      unbind(manager, volume, source, playerId);
    }
  }

  static UUID getPlayerId(VolumeEntry volume) {
    String raw = get(volume, PLAYER_KEY);
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      return UUID.fromString(raw);
    } catch (IllegalArgumentException ignored) {
      return null;
    }
  }

  static Vector3d getOffset(VolumeEntry volume) {
    return new Vector3d(
        parseDouble(get(volume, OFFSET_X_KEY)),
        parseDouble(get(volume, OFFSET_Y_KEY)),
        parseDouble(get(volume, OFFSET_Z_KEY)));
  }

  private static String get(VolumeEntry volume, String key) {
    Map<String, String> tags = volume.getRawTags();
    return tags == null ? null : tags.get(key);
  }

  private static double parseDouble(String value) {
    if (value == null || value.isBlank()) {
      return 0.0D;
    }
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException ignored) {
      return 0.0D;
    }
  }
}
