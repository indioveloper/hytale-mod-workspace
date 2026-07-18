package gg.orbgenesis.playertriggers;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.Objective;
import com.hypixel.hytale.protocol.ObjectiveTask;
import com.hypixel.hytale.protocol.packets.assets.TrackOrUpdateObjective;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

final class TeamScoreboardService {
  static final String DEFAULT_TITLE = "Equipos";
  static final String DEFAULT_LINE_ID = "lobby_teams";
  static final String DEFAULT_TEAM_TAG = "team";
  static final String DEFAULT_TEAM_VALUES = "red,blue,green,yellow";
  static final String DEFAULT_TEAM_LABELS = "Rojo,Azul,Verde,Amarillo";
  static final int DEFAULT_TEAM_CAPACITY = 8;

  private static final UUID DEFAULT_OBJECTIVE_UUID =
      UUID.nameUUIDFromBytes("orbgenesis-lobby-team-counts".getBytes());

  private TeamScoreboardService() {}

  static void refreshDefault(Store<EntityStore> store) {
    refresh(
        store,
        DEFAULT_TITLE,
        DEFAULT_LINE_ID,
        DEFAULT_TEAM_TAG,
        DEFAULT_TEAM_VALUES,
        DEFAULT_TEAM_LABELS,
        false,
        DEFAULT_TEAM_CAPACITY);
  }

  static void refresh(
      Store<EntityStore> store,
      String title,
      String lineId,
      String teamTag,
      String teamValuesCsv,
      String teamLabelsCsv,
      boolean showUnassigned,
      int teamCapacity) {
    if (store == null || store.getExternalData() == null) {
      return;
    }

    World world = store.getExternalData().getWorld();
    if (world == null) {
      return;
    }

    String normalizedTag = PlayerTagAccess.normalizeKey(defaultIfBlank(teamTag, DEFAULT_TEAM_TAG));
    List<String> values = parseCsv(defaultIfBlank(teamValuesCsv, DEFAULT_TEAM_VALUES));
    List<String> labels = parseCsv(defaultIfBlank(teamLabelsCsv, DEFAULT_TEAM_LABELS));
    if (values.isEmpty()) {
      return;
    }

    int[] counts = new int[values.size()];
    int unassigned = 0;
    for (PlayerRef playerRef : world.getPlayerRefs()) {
      Ref<EntityStore> ref = playerRef.getReference();
      if (ref == null || !ref.isValid()) {
        continue;
      }

      PlayerTagsComponent component =
          store.getComponent(ref, PlayerTriggersPlugin.get().getPlayerTagsComponentType());
      String playerTeam =
          component == null ? "" : component.getTags().getOrDefault(normalizedTag, "");
      int index = indexOfIgnoreCase(values, playerTeam);
      if (index >= 0) {
        counts[index]++;
      } else {
        unassigned++;
      }
    }

    ObjectiveTask[] tasks =
        buildTasks(values, labels, counts, showUnassigned, unassigned, Math.max(1, teamCapacity));
    Objective objective =
        new Objective(
            DEFAULT_OBJECTIVE_UUID,
            raw(defaultIfBlank(title, DEFAULT_TITLE)),
            raw(""),
            cleanLineId(defaultIfBlank(lineId, DEFAULT_LINE_ID)),
            tasks);
    TrackOrUpdateObjective packet = new TrackOrUpdateObjective(objective);
    for (PlayerRef playerRef : world.getPlayerRefs()) {
      playerRef.getPacketHandler().write(packet);
    }
  }

  private static ObjectiveTask[] buildTasks(
      List<String> values,
      List<String> labels,
      int[] counts,
      boolean showUnassigned,
      int unassigned,
      int teamCapacity) {
    List<ObjectiveTask> tasks = new ArrayList<>();
    for (int i = 0; i < values.size(); i++) {
      String label = i < labels.size() ? labels.get(i) : values.get(i);
      tasks.add(new ObjectiveTask(raw(label), counts[i], teamCapacity));
    }
    if (showUnassigned) {
      tasks.add(new ObjectiveTask(raw("Sin equipo"), unassigned, Math.max(1, unassigned)));
    }
    return tasks.toArray(ObjectiveTask[]::new);
  }

  private static List<String> parseCsv(String csv) {
    List<String> values = new ArrayList<>();
    for (String raw : csv.split(",")) {
      String value = raw.trim();
      if (!value.isEmpty()) {
        values.add(value);
      }
    }
    return values;
  }

  private static int indexOfIgnoreCase(List<String> values, String candidate) {
    if (candidate == null) {
      return -1;
    }

    String normalizedCandidate = candidate.trim().toLowerCase(Locale.ROOT);
    for (int i = 0; i < values.size(); i++) {
      if (values.get(i).trim().toLowerCase(Locale.ROOT).equals(normalizedCandidate)) {
        return i;
      }
    }
    return -1;
  }

  private static String defaultIfBlank(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value.trim();
  }

  private static String cleanLineId(String value) {
    String cleaned = value.trim().replaceAll("[^A-Za-z0-9_]", "_");
    return cleaned.isEmpty() ? DEFAULT_LINE_ID : cleaned;
  }

  private static com.hypixel.hytale.protocol.FormattedMessage raw(String text) {
    return Message.raw(text == null ? "" : text).getFormattedMessage();
  }
}
