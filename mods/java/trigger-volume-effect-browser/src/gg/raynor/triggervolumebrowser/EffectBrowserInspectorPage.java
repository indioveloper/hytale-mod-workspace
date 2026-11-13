package gg.raynor.triggervolumebrowser;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerRule;
import com.hypixel.hytale.builtin.triggervolumes.ui.TriggerVolumeInspectorPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class EffectBrowserInspectorPage extends TriggerVolumeInspectorPage {
  private static final String PANEL =
      "Pages/TriggerVolumeEffectBrowser/EffectBrowserPanel.ui";
  private static final String ROW =
      "Pages/TriggerVolumeEffectBrowser/EffectBrowserRow.ui";
  private static final String SECTION =
      "Pages/TriggerVolumeEffectBrowser/EffectBrowserSection.ui";
  private static final String FILTER_BUTTON =
      "Pages/TriggerVolumeEffectBrowser/EffectBrowserFilterButton.ui";
  private static final String HEADER =
      "Pages/TriggerVolumeEffectBrowser/EffectBrowserHeader.ui";
  private static final String BACK_BAR =
      "Pages/TriggerVolumeEffectBrowser/EffectBrowserBackBar.ui";
  private static final String SEARCH_MARKER = "__effect_browser_search__";
  private static final String BACK_MARKER = "__effect_browser_back__";
  private static final String ADD_MARKER = "__effect_browser_add__:";
  private static final String FILTER_MARKER = "__effect_browser_filter__:";

  private String searchText = "";
  private String targetKind = "EFFECT";
  private boolean paletteVisible = true;
  private BrowserMode browserMode = BrowserMode.ALL;
  private String activeGroup = "ALL";

  EffectBrowserInspectorPage(
      @Nonnull PlayerRef playerRef,
      @Nonnull String selectedWorld,
      @Nullable String preSelectedVolumeId) {
    super(playerRef, selectedWorld, preSelectedVolumeId, InspectorTab.EFFECTS);
  }

  @Override
  public void build(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull UICommandBuilder cmd,
      @Nonnull UIEventBuilder evt,
      @Nonnull Store<EntityStore> store) {
    super.build(ref, cmd, evt, store);
    cmd.set("#AddEffectRow.Visible", false);
    cmd.insertBefore("#EffectDetailPanel", BACK_BAR);
    cmd.insertBefore("#EffectDetailPanel", PANEL);
    buildPalette(cmd, evt);
    bindPaletteStaticEvents(evt);
    applyPaletteVisibility(cmd);
  }

  @Override
  public void handleDataEvent(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Store<EntityStore> store,
      @Nonnull PageData data) {
    if (data.action == Action.FilterChanged && SEARCH_MARKER.equals(data.id)) {
      searchText = data.filterText == null ? "" : data.filterText.trim().toLowerCase(Locale.ROOT);
      var cmd = new UICommandBuilder();
      var evt = new UIEventBuilder();
      buildPalette(cmd, evt);
      bindPaletteStaticEvents(evt);
      applyPaletteVisibility(cmd);
      sendUpdate(cmd, evt, false);
      return;
    }

    if (data.action == Action.ChangeTab && BACK_MARKER.equals(data.id)) {
      paletteVisible = true;
      var cmd = new UICommandBuilder();
      var evt = new UIEventBuilder();
      buildPalette(cmd, evt);
      bindPaletteStaticEvents(evt);
      applyPaletteVisibility(cmd);
      sendUpdate(cmd, evt, false);
      return;
    }

    if (data.action == Action.FilterChanged
        && data.id != null
        && data.id.startsWith(FILTER_MARKER)) {
      applyBrowserFilter(data.id.substring(FILTER_MARKER.length()));
      var cmd = new UICommandBuilder();
      var evt = new UIEventBuilder();
      buildPalette(cmd, evt);
      bindPaletteStaticEvents(evt);
      applyPaletteVisibility(cmd);
      sendUpdate(cmd, evt, false);
      return;
    }

    if (data.action == Action.UpdateAddTarget && data.addTargetKind != null) {
      targetKind = normalizedTarget(data.addTargetKind);
      paletteVisible = true;
    } else if (data.action == Action.UpdateAddEventType
        && "ALWAYS_ACTIVE".equals(data.addEventType)) {
      targetKind = "RULE";
      paletteVisible = true;
    } else if (data.action == Action.AddEffect
        && data.id != null
        && data.id.startsWith(ADD_MARKER)) {
      data.effectType = data.id.substring(ADD_MARKER.length());
      targetKind = normalizedTarget(data.addTargetKind);
      paletteVisible = false;
    } else if (data.action == Action.SelectEffect) {
      if (data.effectListKind != null) {
        targetKind = normalizedTarget(data.effectListKind);
      }
      paletteVisible = false;
    } else if (data.action == Action.RemoveEffect) {
      paletteVisible = true;
    } else if (data.action == Action.Select) {
      paletteVisible = true;
      searchText = "";
      browserMode = BrowserMode.ALL;
      activeGroup = "ALL";
    } else if (data.action == Action.ChangeTab && "EFFECTS".equals(data.tab)) {
      paletteVisible = true;
    }

    super.handleDataEvent(ref, store, data);

    var cmd = new UICommandBuilder();
    var evt = new UIEventBuilder();
    cmd.set("#AddEffectRow.Visible", false);
    if (paletteVisible) {
      buildPalette(cmd, evt);
      bindPaletteStaticEvents(evt);
    }
    applyPaletteVisibility(cmd);
    sendUpdate(cmd, evt, false);
  }

  private void buildPalette(UICommandBuilder cmd, UIEventBuilder evt) {
    buildPaletteHeader(cmd);
    cmd.clear("#EffectBrowserList");
    cmd.clear("#EffectBrowserSubFilters");
    cmd.set("#EffectBrowserSearch.Value", searchText);
    cmd.set(
        "#EffectBrowserKind.Text",
        Message.translation("server.customUI.triggerEffectBrowser.kind." + targetKind.toLowerCase(Locale.ROOT)));

    cmd.set("#EffectBrowserModeAll.Disabled", browserMode == BrowserMode.ALL);
    cmd.set("#EffectBrowserModeCategory.Disabled", browserMode == BrowserMode.CATEGORY);
    cmd.set("#EffectBrowserModeMod.Disabled", browserMode == BrowserMode.MOD);

    var types = new ArrayList<TypeInfo>();
    for (String id : registeredIdsForTarget()) {
      types.add(new TypeInfo(id, categoryFor(id), originFor(id)));
    }
    types.sort(Comparator.comparing(type -> humanize(type.id())));
    var visibleTypes = types.stream().filter(type -> matchesSearch(type.id())).toList();

    buildSubFilters(cmd, evt, visibleTypes);
    var filteredTypes = visibleTypes.stream().filter(this::matchesActiveGroup).toList();

    cmd.set("#EffectBrowserEmpty.Visible", filteredTypes.isEmpty());
    if (browserMode == BrowserMode.ALL) {
      appendRows(cmd, evt, filteredTypes, 0);
      return;
    }

    Map<String, List<TypeInfo>> groups = new LinkedHashMap<>();
    if (browserMode == BrowserMode.CATEGORY) {
      for (Category category : Category.values()) {
        groups.put(category.name(), new ArrayList<>());
      }
      for (TypeInfo type : filteredTypes) {
        groups.get(type.category().name()).add(type);
      }
    } else {
      for (Origin origin : Origin.values()) {
        groups.put(origin.name(), new ArrayList<>());
      }
      for (TypeInfo type : filteredTypes) {
        groups.get(type.origin().name()).add(type);
      }
    }

    int childIndex = 0;
    for (var group : groups.entrySet()) {
      if (group.getValue().isEmpty()) {
        continue;
      }
      cmd.append("#EffectBrowserList", SECTION);
      String section = "#EffectBrowserList[" + childIndex++ + "]";
      cmd.set(section + " #SectionTitle.Text", groupLabel(group.getKey()));
      childIndex = appendRows(cmd, evt, group.getValue(), childIndex);
    }
  }

  private void buildPaletteHeader(UICommandBuilder cmd) {
    cmd.clear("#TitleTagChips");
    cmd.clear("#TitleTagChipsOverflow");
    cmd.append("#TitleTagChips", HEADER);
  }

  private int appendRows(
      UICommandBuilder cmd, UIEventBuilder evt, List<TypeInfo> types, int childIndex) {
    for (TypeInfo type : types) {
      cmd.append("#EffectBrowserList", ROW);
      String row = "#EffectBrowserList[" + childIndex++ + "]";
      cmd.set(row + " #EffectName.Text", humanize(type.id()));
      cmd.set(row + " #EffectId.Text", descriptionFor(type));
      evt.addEventBinding(
          CustomUIEventBindingType.Activating,
          row + " #AddButton",
          new EventData()
              .append("Action", Action.AddEffect.name())
              .append("Id", ADD_MARKER + type.id())
              .append("@AddTargetKind", "#AddTargetDropdown.Value")
              .append("@AddEventType", "#AddEventTypeDropdown.Value")
              .append("@AddEntry", "#AddEntryDropdown.Value"));
    }
    return childIndex;
  }

  private void buildSubFilters(
      UICommandBuilder cmd, UIEventBuilder evt, List<TypeInfo> visibleTypes) {
    cmd.set("#EffectBrowserSubFilters.Visible", browserMode != BrowserMode.ALL);
    if (browserMode == BrowserMode.ALL) {
      return;
    }

    int index = 0;
    index = appendFilterButton(cmd, evt, index, "ALL", Message.translation(
        browserMode == BrowserMode.CATEGORY
            ? "server.customUI.triggerEffectBrowser.category.all"
            : "server.customUI.triggerEffectBrowser.mod.all"));

    if (browserMode == BrowserMode.CATEGORY) {
      for (Category category : Category.values()) {
        if (visibleTypes.stream().anyMatch(type -> type.category() == category)) {
          index = appendFilterButton(cmd, evt, index, category.name(), category.label());
        }
      }
    } else {
      for (Origin origin : Origin.values()) {
        if (visibleTypes.stream().anyMatch(type -> type.origin() == origin)) {
          index = appendFilterButton(cmd, evt, index, origin.name(), origin.label());
        }
      }
    }
  }

  private int appendFilterButton(
      UICommandBuilder cmd, UIEventBuilder evt, int index, String key, Message label) {
    cmd.append("#EffectBrowserSubFilters", FILTER_BUTTON);
    String selector = "#EffectBrowserSubFilters[" + index + "]";
    // EffectBrowserFilterButton.ui has the button as its root element. Once appended,
    // the indexed selector already points at #FilterButton; looking for a nested child
    // disconnects the client because that element does not exist.
    cmd.set(selector + ".Text", label);
    cmd.set(selector + ".Disabled", key.equals(activeGroup));
    evt.addEventBinding(
        CustomUIEventBindingType.Activating,
        selector,
        new EventData()
            .append("Action", Action.FilterChanged.name())
            .append("Id", FILTER_MARKER + "GROUP:" + key),
        false);
    return index + 1;
  }

  private void bindPaletteStaticEvents(UIEventBuilder evt) {
    evt.addEventBinding(
        CustomUIEventBindingType.ValueChanged,
        "#EffectBrowserSearch",
        new EventData()
            .append("Action", Action.FilterChanged.name())
            .append("Id", SEARCH_MARKER)
            .append("@FilterText", "#EffectBrowserSearch.Value"),
        false);
    evt.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#EffectBrowserBackButton",
        new EventData()
            .append("Action", Action.ChangeTab.name())
            .append("Id", BACK_MARKER)
            .append("Tab", "EFFECTS"),
        false);
    bindModeButton(evt, "#EffectBrowserModeAll", BrowserMode.ALL);
    bindModeButton(evt, "#EffectBrowserModeCategory", BrowserMode.CATEGORY);
    bindModeButton(evt, "#EffectBrowserModeMod", BrowserMode.MOD);
  }

  private void bindModeButton(UIEventBuilder evt, String selector, BrowserMode mode) {
    evt.addEventBinding(
        CustomUIEventBindingType.Activating,
        selector,
        new EventData()
            .append("Action", Action.FilterChanged.name())
            .append("Id", FILTER_MARKER + "MODE:" + mode.name()),
        false);
  }

  private void applyPaletteVisibility(UICommandBuilder cmd) {
    cmd.set("#EffectBrowserPanel.Visible", paletteVisible);
    cmd.set("#EffectBrowserBackBar.Visible", !paletteVisible);
    if (paletteVisible) {
      cmd.set("#NoEffectSelectionLabel.Visible", false);
      cmd.set("#EffectDetailPanel.Visible", false);
    }
  }

  private Collection<String> registeredIdsForTarget() {
    return switch (targetKind) {
      case "CONDITION" -> TriggerCondition.CODEC.getRegisteredIds();
      case "RULE" -> TriggerRule.CODEC.getRegisteredIds();
      case "EFFECT", "REJECTION_EFFECT" -> TriggerEffect.CODEC.getRegisteredIds();
      default -> List.of();
    };
  }

  private void applyBrowserFilter(String value) {
    if (value.startsWith("MODE:")) {
      try {
        browserMode = BrowserMode.valueOf(value.substring("MODE:".length()));
      } catch (IllegalArgumentException ignored) {
        browserMode = BrowserMode.ALL;
      }
      activeGroup = "ALL";
    } else if (value.startsWith("GROUP:")) {
      activeGroup = value.substring("GROUP:".length());
    }
  }

  private boolean matchesActiveGroup(TypeInfo type) {
    if ("ALL".equals(activeGroup) || browserMode == BrowserMode.ALL) {
      return true;
    }
    return browserMode == BrowserMode.CATEGORY
        ? type.category().name().equals(activeGroup)
        : type.origin().name().equals(activeGroup);
  }

  private Message groupLabel(String key) {
    if (browserMode == BrowserMode.CATEGORY) {
      return Category.valueOf(key).label();
    }
    return Origin.valueOf(key).label();
  }

  private Category categoryFor(String id) {
    String value = id.toLowerCase(Locale.ROOT);
    if (containsAny(value, "scoreboard", "objective")) {
      return Category.OBJECTIVES;
    }
    if (containsAny(value, "particle", "vfx", "sound", "animation", "message", "title")) {
      return Category.AUDIOVISUAL;
    }
    if (containsAny(value, "block", "prefab", "world", "volume", "build", "structure", "spawnpoint")) {
      return Category.WORLD;
    }
    if (containsAny(value, "entity", "npc", "motion", "collision", "teleport", "model", "velocity", "move")) {
      return Category.ENTITIES;
    }
    if (containsAny(value, "item", "inventory", "equipment", "damage", "heal", "status", "stat", "player", "game mode", "gamemode")) {
      return Category.PLAYERS;
    }
    if (containsAny(value, "signal", "command", "tag", "timer", "enable", "disable", "delete", "cooldown", "random", "trigger", "rule")) {
      return Category.LOGIC;
    }
    return Category.GENERAL;
  }

  private Origin originFor(String id) {
    Class<?> typeClass = switch (targetKind) {
      case "CONDITION" -> TriggerCondition.CODEC.getClassFor(id);
      case "RULE" -> TriggerRule.CODEC.getClassFor(id);
      case "EFFECT", "REJECTION_EFFECT" -> TriggerEffect.CODEC.getClassFor(id);
      default -> null;
    };
    String className = typeClass == null ? "" : typeClass.getName();
    if (className.startsWith("gg.orbgenesis.moretriggers")) {
      return Origin.MORE_TRIGGERS;
    }
    if (className.startsWith("gg.orbgenesis.playertriggertags")) {
      return Origin.ENTITY_MOTION;
    }
    if (className.startsWith("gg.orbgenesis.particleshapevfx")) {
      return Origin.PARTICLE_SHAPE;
    }
    if (className.startsWith("gg.orbgenesis.scoreboards")) {
      return Origin.OBJECTIVES;
    }
    if (className.startsWith("gg.orbgenesis.buildbattle")) {
      return Origin.BUILD_BATTLE;
    }
    if (className.startsWith("com.hypixel.hytale")) {
      return Origin.HYTALE;
    }
    return Origin.OTHER;
  }

  private static boolean containsAny(String value, String... needles) {
    for (String needle : needles) {
      if (value.contains(needle)) {
        return true;
      }
    }
    return false;
  }

  private boolean matchesSearch(String id) {
    return searchText.isBlank()
        || id.toLowerCase(Locale.ROOT).contains(searchText)
        || humanize(id).toLowerCase(Locale.ROOT).contains(searchText);
  }

  private static String normalizedTarget(String value) {
    if (value == null) {
      return "EFFECT";
    }
    return switch (value) {
      case "CONDITION", "EFFECT", "REJECTION_EFFECT", "RULE" -> value;
      default -> "EFFECT";
    };
  }

  private Message descriptionFor(TypeInfo type) {
    if (KNOWN_DESCRIPTION_IDS.contains(type.id())) {
      return Message.translation(
          "server.customUI.triggerEffectBrowser.description." + type.id());
    }
    return Message.translation(
        "server.customUI.triggerEffectBrowser.description.fallback." + type.category().key);
  }

  private static final java.util.Set<String> KNOWN_DESCRIPTION_IDS = java.util.Set.of(
      "PermissionCondition", "CooldownCondition", "GameModeCondition", "ItemCondition",
      "RandomChanceCondition", "PlayerCountCondition", "TagCondition", "BlockTypeCondition",
      "EntityCountCondition", "TimeOfDay", "ScoreboardState", "ScoreboardTaskValue",
      "Teleport", "SendMessage", "PlaySound", "SetVelocity", "EntityEffect",
      "TriggerNpcMarkers", "PlayVfx", "SetWeather", "Time", "ShowEventTitle",
      "PastePrefab", "ControlDoors", "EnableVolume", "DisableVolume", "DeleteVolume",
      "DamageEntity", "RemoveEntities", "CancelParticles", "RunRootInteraction", "SetMusic",
      "GiveItem", "SetGameMode", "ModifyTags", "SendSignal", "PlaceBlock",
      "ReplaceBlockType", "SpawnNpc", "PlayAnimation", "ModifyRules", "GiveRandomItem",
      "PasteRandomPrefab", "SendTagMessage", "ShowTagEventTitle", "ControlTimer",
      "ControlSignalLoop", "ExecuteCommand", "ApplyHorizontalPlatformMotion",
      "StopHorizontalPlatformMotion", "ApplyPlayerPlatformCollision",
      "RemovePlayerPlatformCollision", "SpawnItems", "ConvertBlocksToEntities",
      "AttachMovingParticles", "SpawnParticleShape", "ControlScoreboard",
      "ModifyScoreboardTask", "SuggestBuildTheme", "NoBuild", "NoDestroy", "NoHarvest",
      "NoDamage", "NoDoorOpen", "NoUse", "NoHeal", "DamageMultiplier", "Fly",
      "CreativePlacement", "NoTick", "NoMove", "RestrictBuildBattleCreativeTools");

  private static String humanize(String id) {
    if (id == null || id.isBlank()) {
      return "";
    }
    String separated = id.replace('_', ' ').replace('-', ' ');
    separated = separated.replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ");
    StringBuilder result = new StringBuilder();
    for (String part : separated.trim().split("\\s+")) {
      if (part.isEmpty()) {
        continue;
      }
      if (!result.isEmpty()) {
        result.append(' ');
      }
      result.append(Character.toUpperCase(part.charAt(0)));
      if (part.length() > 1) {
        result.append(part.substring(1));
      }
    }
    return result.toString();
  }

  private enum BrowserMode {
    ALL,
    CATEGORY,
    MOD
  }

  private enum Category {
    LOGIC("logic"),
    PLAYERS("players"),
    ENTITIES("entities"),
    WORLD("world"),
    AUDIOVISUAL("audiovisual"),
    OBJECTIVES("objectives"),
    GENERAL("general");

    private final String key;

    Category(String key) {
      this.key = key;
    }

    private Message label() {
      return Message.translation("server.customUI.triggerEffectBrowser.category." + key);
    }
  }

  private enum Origin {
    HYTALE("hytale"),
    MORE_TRIGGERS("moreTriggers"),
    ENTITY_MOTION("entityMotion"),
    PARTICLE_SHAPE("particleShape"),
    OBJECTIVES("objectives"),
    BUILD_BATTLE("buildBattle"),
    OTHER("other");

    private final String key;

    Origin(String key) {
      this.key = key;
    }

    private Message label() {
      return Message.translation("server.customUI.triggerEffectBrowser.mod." + key);
    }
  }

  private record TypeInfo(String id, Category category, Origin origin) {}
}
