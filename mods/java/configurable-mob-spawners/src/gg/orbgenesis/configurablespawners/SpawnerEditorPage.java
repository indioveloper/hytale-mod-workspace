package gg.orbgenesis.configurablespawners;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.NonSerialized;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.OpenChatWithCommand;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.spawning.ISpawnableWithModel;
import com.hypixel.hytale.server.spawning.SpawningContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public final class SpawnerEditorPage extends InteractiveCustomUIPage<SpawnerEditorPage.PageData> {
  private static final Pattern TAG_PATTERN = Pattern.compile("^[A-Za-z0-9_.:-]{0,64}$");
  private final BlockModule.BlockStateInfo blockInfo;
  private final ConfigurableSpawnerComponent state;
  private final ConfigurableSpawnerComponent draft;
  private String roleSearch = "";
  private boolean configurationVisible;
  private Ref<EntityStore> preview;

  public SpawnerEditorPage(
      @Nonnull PlayerRef playerRef,
      @Nonnull BlockModule.BlockStateInfo blockInfo,
      @Nonnull ConfigurableSpawnerComponent state,
      @Nonnull CustomPageLifetime lifetime) {
    super(playerRef, lifetime, PageData.CODEC);
    this.blockInfo = blockInfo;
    this.state = state;
    this.draft = state.copyConfiguration();
  }

  @Override
  public void build(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull UICommandBuilder cmd,
      @Nonnull UIEventBuilder events,
      @Nonnull Store<EntityStore> store) {
    draft.normalize();
    if (!configurationVisible) {
      buildLanding(cmd, events);
      return;
    }
    cmd.append("Pages/ConfigurableSpawners/SpawnerEditor.ui");
    populate(cmd);
    bindSimple(events, CustomUIEventBindingType.ValueChanged, "#RoleSearchInput", "SEARCH", PageData.SEARCH, "#RoleSearchInput.Value");
    bindSimple(events, CustomUIEventBindingType.ValueChanged, "#RoleDropdown", "ROLE", PageData.ROLE, "#RoleDropdown.Value");
    bindSimple(events, CustomUIEventBindingType.ValueChanged, "#ScaleSlider", "SCALE", PageData.SCALE, "#ScaleSlider.Value");
    bindSimple(events, CustomUIEventBindingType.ValueChanged, "#SpeedSlider", "SPEED", PageData.SPEED, "#SpeedSlider.Value");
    bindSimple(events, CustomUIEventBindingType.Activating, "#ImportButton", "IMPORT", PageData.CONFIG, "#ConfigStringInput.Value");
    bindConfigurationActions(events);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#CancelButton",
        new EventData().append(PageData.ACTION, "CANCEL"), false);
    updatePreview(ref, store, draft.roleId);
  }

  private void buildLanding(UICommandBuilder cmd, UIEventBuilder events) {
    cmd.append("Pages/ConfigurableSpawners/SpawnerLanding.ui");
    cmd.set("#ConfiguratorUrl.Value", ConfigurableMobSpawnersPlugin.CONFIGURATOR_URL);
    cmd.set("#QuickConfigStringInput.Value", "");
    bindSimple(events, CustomUIEventBindingType.Activating, "#QuickImportButton",
        "QUICK_IMPORT", PageData.CONFIG, "#QuickConfigStringInput.Value");
    events.addEventBinding(CustomUIEventBindingType.Activating, "#ConfigureButton",
        new EventData().append(PageData.ACTION, "CONFIGURE"), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#CopyUrlButton",
        new EventData().append(PageData.ACTION, "COPY_URL"), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#CancelButton",
        new EventData().append(PageData.ACTION, "CANCEL"), false);
  }

  private void populate(UICommandBuilder cmd) {
    cmd.set("#TagInput.Value", String.join(", ", draft.tags));
    cmd.set("#RoleSearchInput.Value", roleSearch);
    cmd.set("#RoleDropdown.Entries", roleEntries(roleSearch, draft.roleId));
    cmd.set("#RoleDropdown.Value", draft.roleId);
    cmd.set("#MobNameInput.Value", draft.mobName);
    cmd.set("#CadenceMinInput.Value", Double.toString(draft.cadenceMinSeconds));
    cmd.set("#CadenceMaxInput.Value", Double.toString(draft.cadenceMaxSeconds));
    cmd.set("#CountMinInput.Value", Integer.toString(draft.spawnCountMin));
    cmd.set("#CountMaxInput.Value", Integer.toString(draft.spawnCountMax));
    cmd.set("#MaxAliveInput.Value", Integer.toString(draft.maxAlive));
    cmd.set("#ActivationInput.Value", Double.toString(draft.activationRadius));
    cmd.set("#MaxHealthInput.Value", Double.toString(draft.maxHealth));
    cmd.set("#ScaleSlider.Value", (float) draft.mobScale);
    cmd.set("#ScaleValue.Text", String.format(Locale.ROOT, "%.1fx", draft.mobScale));
    cmd.set("#SpeedSlider.Value", (float) draft.mobSpeed);
    cmd.set("#SpeedValue.Text", String.format(Locale.ROOT, "%.1fx", draft.mobSpeed));
    cmd.set("#HorizontalInput.Value", Double.toString(draft.horizontalRadius));
    cmd.set("#MaxLightInput.Value", Integer.toString(draft.maxLight));
    cmd.set("#HeldItemInput.Value", draft.heldItemId);
    cmd.set("#AggressionDropdown.Value", draft.aggressionMode.name());
    cmd.set("#LootModeDropdown.Value", draft.lootMode.name());
    cmd.set("#ArmorHeadInput.Value", draft.armorHeadId);
    cmd.set("#ArmorChestInput.Value", draft.armorChestId);
    cmd.set("#ArmorHandsInput.Value", draft.armorHandsId);
    cmd.set("#ArmorLegsInput.Value", draft.armorLegsId);
    for (int i = 0; i < SpawnerLootEntry.MAX_ENTRIES; i++) {
      SpawnerLootEntry loot = draft.lootEntries[i];
      cmd.set("#LootItem" + i + ".Value", loot.itemId);
      cmd.set("#LootMin" + i + ".Value", Integer.toString(loot.minQuantity));
      cmd.set("#LootMax" + i + ".Value", Integer.toString(loot.maxQuantity));
      cmd.set("#LootChance" + i + ".Value", Double.toString(loot.chancePercent));
    }
    cmd.set("#ConfigStringInput.Value", SpawnerConfigString.encode(draft));
  }

  private static List<DropdownEntryInfo> roleEntries(String query, String selected) {
    String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
    ArrayList<DropdownEntryInfo> entries = new ArrayList<>();
    if (selected == null || selected.isBlank()) {
      entries.add(new DropdownEntryInfo(
          LocalizableString.fromMessageId(
              "server.customUI.configurableSpawners.role.unconfigured"), ""));
    }
    entries.addAll(NPCPlugin.get().getRoleTemplateNames(true).stream()
        .filter(id -> needle.isEmpty() || id.toLowerCase(Locale.ROOT).contains(needle) || id.equals(selected))
        .sorted(Comparator.comparing(String::toLowerCase))
        .map(id -> new DropdownEntryInfo(LocalizableString.fromString(id), id))
        .toList());
    return entries;
  }

  private static void bindSimple(
      UIEventBuilder events, CustomUIEventBindingType type, String selector, String action,
      String key, String valueSelector) {
    events.addEventBinding(type, selector,
        new EventData().append(PageData.ACTION, action).append(key, valueSelector), false);
  }

  private static void bindConfigurationActions(UIEventBuilder events) {
    events.addEventBinding(CustomUIEventBindingType.Activating, "#SaveButton",
        configurationData("SAVE"), false);
    events.addEventBinding(CustomUIEventBindingType.Activating, "#ExportButton",
        configurationData("EXPORT"), false);
  }

  private static EventData configurationData(String action) {
    EventData data = new EventData()
        .append(PageData.ACTION, action)
        .append(PageData.TAG, "#TagInput.Value")
        .append(PageData.ROLE, "#RoleDropdown.Value")
        .append(PageData.MOB_NAME, "#MobNameInput.Value")
        .append(PageData.CADENCE_MIN, "#CadenceMinInput.Value")
        .append(PageData.CADENCE_MAX, "#CadenceMaxInput.Value")
        .append(PageData.COUNT_MIN, "#CountMinInput.Value")
        .append(PageData.COUNT_MAX, "#CountMaxInput.Value")
        .append(PageData.MAX_ALIVE, "#MaxAliveInput.Value")
        .append(PageData.ACTIVATION, "#ActivationInput.Value")
        .append(PageData.MAX_HEALTH, "#MaxHealthInput.Value")
        .append(PageData.SCALE, "#ScaleSlider.Value")
        .append(PageData.SPEED, "#SpeedSlider.Value")
        .append(PageData.HORIZONTAL, "#HorizontalInput.Value")
        .append(PageData.MAX_LIGHT, "#MaxLightInput.Value")
        .append(PageData.HELD_ITEM, "#HeldItemInput.Value")
        .append(PageData.AGGRESSION, "#AggressionDropdown.Value")
        .append(PageData.LOOT_MODE, "#LootModeDropdown.Value")
        .append(PageData.ARMOR_HEAD, "#ArmorHeadInput.Value")
        .append(PageData.ARMOR_CHEST, "#ArmorChestInput.Value")
        .append(PageData.ARMOR_HANDS, "#ArmorHandsInput.Value")
        .append(PageData.ARMOR_LEGS, "#ArmorLegsInput.Value");
    for (int i = 0; i < SpawnerLootEntry.MAX_ENTRIES; i++) {
      data.append(PageData.lootItemKey(i), "#LootItem" + i + ".Value")
          .append(PageData.lootMinKey(i), "#LootMin" + i + ".Value")
          .append(PageData.lootMaxKey(i), "#LootMax" + i + ".Value")
          .append(PageData.lootChanceKey(i), "#LootChance" + i + ".Value");
    }
    return data;
  }

  @Override
  public void handleDataEvent(
      @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PageData data) {
    if (data.action == null) return;
    switch (data.action) {
      case "CANCEL" -> close();
      case "CONFIGURE" -> {
        configurationVisible = true;
        rebuild();
      }
      case "QUICK_IMPORT" -> importAndSave(data.config);
      case "COPY_URL" -> prepareConfiguratorUrl();
      case "EXPORT" -> exportConfiguration(data);
      case "SEARCH" -> {
        roleSearch = text(data.search);
        UICommandBuilder cmd = new UICommandBuilder();
        cmd.set("#RoleDropdown.Entries", roleEntries(roleSearch, draft.roleId));
        cmd.set("#RoleDropdown.Value", draft.roleId);
        sendUpdate(cmd, false);
      }
      case "ROLE" -> {
        String role = text(data.role);
        if (!role.isBlank() && NPCPlugin.get().getRoleTemplateNames(true).contains(role)) {
          draft.roleId = role;
          updatePreview(ref, store, role);
        }
      }
      case "SCALE" -> {
        if (data.scale != null) {
          draft.mobScale = Math.max(0.1, Math.min(5.0, Math.round(data.scale * 10.0f) / 10.0));
          UICommandBuilder cmd = new UICommandBuilder();
          cmd.set("#ScaleSlider.Value", (float) draft.mobScale);
          cmd.set("#ScaleValue.Text", String.format(Locale.ROOT, "%.1fx", draft.mobScale));
          sendUpdate(cmd, false);
          updatePreview(ref, store, draft.roleId);
        }
      }
      case "SPEED" -> {
        if (data.speed != null) {
          draft.mobSpeed = Math.max(0.0, Math.min(3.0, Math.round(data.speed * 10.0f) / 10.0));
          UICommandBuilder cmd = new UICommandBuilder();
          cmd.set("#SpeedSlider.Value", (float) draft.mobSpeed);
          cmd.set("#SpeedValue.Text", String.format(Locale.ROOT, "%.1fx", draft.mobSpeed));
          sendUpdate(cmd, false);
        }
      }
      case "IMPORT" -> importConfiguration(ref, store, data.config);
      case "SAVE" -> save(data);
      default -> {}
    }
  }

  private void prepareConfiguratorUrl() {
    playerRef.getPacketHandler().write(
        new OpenChatWithCommand(ConfigurableMobSpawnersPlugin.CONFIGURATOR_URL));
    playerRef.sendMessage(Message.join(
        Message.translation("server.customUI.configurableSpawners.copyUrlChat"),
        Message.raw(" "),
        Message.raw(ConfigurableMobSpawnersPlugin.CONFIGURATOR_URL)
            .link(ConfigurableMobSpawnersPlugin.CONFIGURATOR_URL)));
    showStatus("server.customUI.configurableSpawners.copyUrlReady");
  }

  private void importAndSave(String value) {
    try {
      ConfigurableSpawnerComponent imported = SpawnerConfigString.decode(value);
      draft.copyConfigurationFrom(imported);
      roleSearch = "";
      String validation = validateDraft();
      if (validation != null) {
        showStatus(validation);
        return;
      }
      state.copyConfigurationFrom(draft);
      state.ensureSpawnerId();
      blockInfo.markNeedsSaving();
      playerRef.sendMessage(
          Message.translation("server.customUI.configurableSpawners.importedSaved"));
      close();
    } catch (RuntimeException exception) {
      showStatus("server.customUI.configurableSpawners.error.import");
    }
  }

  private void exportConfiguration(PageData data) {
    String error = readDraft(data);
    if (error != null) {
      showStatus(error);
      return;
    }
    String validation = validateDraft();
    if (validation != null) {
      showStatus(validation);
      return;
    }
    UICommandBuilder cmd = new UICommandBuilder();
    cmd.set("#ConfigStringInput.Value", SpawnerConfigString.encode(draft));
    cmd.set("#Status.Text",
        Message.translation("server.customUI.configurableSpawners.exportReady"));
    sendUpdate(cmd, false);
  }

  private void importConfiguration(Ref<EntityStore> ref, Store<EntityStore> store, String value) {
    try {
      ConfigurableSpawnerComponent imported = SpawnerConfigString.decode(value);
      draft.copyConfigurationFrom(imported);
      roleSearch = "";
      String validation = validateDraft();
      if (validation != null) {
        showStatus(validation);
        return;
      }
      UICommandBuilder cmd = new UICommandBuilder();
      populate(cmd);
      cmd.set("#Status.Text", Message.translation("server.customUI.configurableSpawners.imported"));
      sendUpdate(cmd, false);
      updatePreview(ref, store, draft.roleId);
    } catch (RuntimeException exception) {
      showStatus("server.customUI.configurableSpawners.error.import");
    }
  }

  private void save(PageData data) {
    String error = readDraft(data);
    if (error != null) {
      showStatus(error);
      return;
    }
    String validation = validateDraft();
    if (validation != null) {
      showStatus(validation);
      return;
    }
    state.copyConfigurationFrom(draft);
    state.ensureSpawnerId();
    blockInfo.markNeedsSaving();
    playerRef.sendMessage(Message.translation("server.customUI.configurableSpawners.saved"));
    close();
  }

  private String readDraft(PageData data) {
    try {
      draft.tag = text(data.tag);
      draft.tags = splitTags(draft.tag);
      draft.roleId = text(data.role);
      draft.mobName = text(data.mobName);
      draft.cadenceMinSeconds = number(data.cadenceMin);
      draft.cadenceMaxSeconds = number(data.cadenceMax);
      draft.spawnCountMin = integer(data.countMin);
      draft.spawnCountMax = integer(data.countMax);
      draft.maxAlive = integer(data.maxAlive);
      draft.activationRadius = number(data.activation);
      draft.maxHealth = number(data.maxHealth);
      draft.mobScale = data.scale == null ? 1.0 : data.scale;
      draft.mobSpeed = data.speed == null ? 1.0 : data.speed;
      draft.horizontalRadius = number(data.horizontal);
      draft.minLight = 0;
      draft.maxLight = integer(data.maxLight);
      draft.heldItemId = text(data.heldItem);
      draft.aggressionMode = AggressionMode.valueOf(text(data.aggression));
      draft.lootMode = LootMode.valueOf(text(data.lootMode));
      draft.armorHeadId = text(data.armorHead);
      draft.armorChestId = text(data.armorChest);
      draft.armorHandsId = text(data.armorHands);
      draft.armorLegsId = text(data.armorLegs);
      draft.customArmor = !draft.armorHeadId.isBlank() || !draft.armorChestId.isBlank()
          || !draft.armorHandsId.isBlank() || !draft.armorLegsId.isBlank();
      for (int i = 0; i < SpawnerLootEntry.MAX_ENTRIES; i++) {
        draft.lootEntries[i].set(text(data.lootItem[i]), integer(data.lootMin[i]),
            integer(data.lootMax[i]), number(data.lootChance[i]));
      }
      // The in-game editor intentionally edits Mob 1 only. Preserve the remaining web profiles
      // and the elite settings attached to Mob 1 while synchronizing its basic fields.
      draft.updateFirstProfileFromLegacy();
      draft.normalize();
      return null;
    } catch (RuntimeException exception) {
      return "server.customUI.configurableSpawners.error.number";
    }
  }

  private String validateDraft() {
    for (String tag : draft.tags) {
      if (!TAG_PATTERN.matcher(tag).matches()) return "server.customUI.configurableSpawners.error.tag";
    }
    if (!NPCPlugin.get().getRoleTemplateNames(true).contains(draft.roleId)) return "server.customUI.configurableSpawners.error.role";
    if (!validItem(draft.heldItemId)) return "server.customUI.configurableSpawners.error.item";
    if (draft.customArmor) {
      for (String itemId : List.of(draft.armorHeadId, draft.armorChestId, draft.armorHandsId, draft.armorLegsId)) {
        if (!itemId.isBlank()) {
          Item item = Item.getAssetMap().getAsset(itemId);
          if (item == null || item.getArmor() == null) return "server.customUI.configurableSpawners.error.armor";
        }
      }
    }
    for (SpawnerLootEntry entry : draft.lootEntries) {
      if (!validItem(entry.itemId)) return "server.customUI.configurableSpawners.error.loot";
    }
    return null;
  }

  private static boolean validItem(String itemId) {
    return itemId.isBlank() || Item.getAssetMap().getAsset(itemId) != null;
  }

  private void updatePreview(Ref<EntityStore> player, Store<EntityStore> store, String roleId) {
    try {
      Model model = getNpcModel(roleId);
      if (model == null) return;
      model = SpawnerMobScale.scaledCopy(model, draft.mobScale);
      if (model == null) return;
      TransformComponent transform = store.getComponent(player, TransformComponent.getComponentType());
      HeadRotation head = store.getComponent(player, HeadRotation.getComponentType());
      if (transform == null || head == null) return;
      Vector3d direction = Transform.getDirection(head.getRotation().pitch(), head.getRotation().yaw());
      Vector3d position = new Vector3d(transform.getPosition()).add(direction.mul(4.0));
      position.y = transform.getPosition().y;
      Vector3d facing = new Vector3d(transform.getPosition()).sub(position);
      facing.y = 0.0;
      Rotation3f rotation = Rotation3f.lookAt(facing);
      clearPreview(store);
      var holder = store.getRegistry().newHolder();
      holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
      holder.addComponent(EntityStore.REGISTRY.getNonSerializedComponentType(), NonSerialized.get());
      holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(position, rotation));
      holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
      holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(rotation));
      preview = store.addEntity(holder, AddReason.SPAWN);
    } catch (RuntimeException ignored) {
      clearPreview(store);
    }
  }

  @Nullable
  private static Model getNpcModel(String roleId) {
    NPCPlugin plugin = NPCPlugin.get();
    int index = plugin.getIndex(roleId);
    if (index < 0) return null;
    plugin.forceValidation(index);
    var info = plugin.getRoleBuilderInfo(index);
    if (info == null || !plugin.testAndValidateRole(info)) return null;
    var builder = plugin.tryGetCachedValidRole(index);
    if (!(builder instanceof ISpawnableWithModel spawnable) || !builder.isSpawnable()) return null;
    SpawningContext context = new SpawningContext();
    return context.setSpawnable(spawnable) ? context.getModel() : null;
  }

  private void clearPreview(Store<EntityStore> store) {
    if (preview != null && preview.isValid()) store.removeEntity(preview, RemoveReason.REMOVE);
    preview = null;
  }

  @Override
  public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
    clearPreview(store);
  }

  private void showStatus(String key) {
    UICommandBuilder cmd = new UICommandBuilder();
    cmd.set("#Status.Text", Message.translation(key));
    sendUpdate(cmd, false);
  }

  private static String text(String value) { return value == null ? "" : value.trim(); }
  private static String[] splitTags(String value) {
    return Arrays.stream(text(value).split(","))
        .map(String::trim).filter(tag -> !tag.isEmpty()).distinct().toArray(String[]::new);
  }
  private static double number(String value) { return Double.parseDouble(text(value)); }
  private static int integer(String value) { return Integer.parseInt(text(value)); }

  public static final class PageData {
    static final String ACTION = "Action";
    static final String SEARCH = "@Search";
    static final String CONFIG = "@Config";
    static final String TAG = "@Tag";
    static final String ROLE = "@Role";
    static final String MOB_NAME = "@MobName";
    static final String CADENCE_MIN = "@CadenceMin";
    static final String CADENCE_MAX = "@CadenceMax";
    static final String COUNT_MIN = "@CountMin";
    static final String COUNT_MAX = "@CountMax";
    static final String MAX_ALIVE = "@MaxAlive";
    static final String ACTIVATION = "@Activation";
    static final String MAX_HEALTH = "@MaxHealth";
    static final String SCALE = "@Scale";
    static final String SPEED = "@Speed";
    static final String HORIZONTAL = "@Horizontal";
    static final String MAX_LIGHT = "@MaxLight";
    static final String HELD_ITEM = "@HeldItem";
    static final String AGGRESSION = "@Aggression";
    static final String LOOT_MODE = "@LootMode";
    static final String ARMOR_HEAD = "@ArmorHead";
    static final String ARMOR_CHEST = "@ArmorChest";
    static final String ARMOR_HANDS = "@ArmorHands";
    static final String ARMOR_LEGS = "@ArmorLegs";

    static String lootItemKey(int i) { return "@LootItem" + i; }
    static String lootMinKey(int i) { return "@LootMin" + i; }
    static String lootMaxKey(int i) { return "@LootMax" + i; }
    static String lootChanceKey(int i) { return "@LootChance" + i; }

    static final BuilderCodec<PageData> CODEC = BuilderCodec.builder(PageData.class, PageData::new)
        .append(new KeyedCodec<>(ACTION, Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
        .append(new KeyedCodec<>(SEARCH, Codec.STRING, false), (d, v) -> d.search = v, d -> d.search).add()
        .append(new KeyedCodec<>(CONFIG, Codec.STRING, false), (d, v) -> d.config = v, d -> d.config).add()
        .append(new KeyedCodec<>(TAG, Codec.STRING, false), (d, v) -> d.tag = v, d -> d.tag).add()
        .append(new KeyedCodec<>(ROLE, Codec.STRING, false), (d, v) -> d.role = v, d -> d.role).add()
        .append(new KeyedCodec<>(MOB_NAME, Codec.STRING, false), (d, v) -> d.mobName = v, d -> d.mobName).add()
        .append(new KeyedCodec<>(CADENCE_MIN, Codec.STRING, false), (d, v) -> d.cadenceMin = v, d -> d.cadenceMin).add()
        .append(new KeyedCodec<>(CADENCE_MAX, Codec.STRING, false), (d, v) -> d.cadenceMax = v, d -> d.cadenceMax).add()
        .append(new KeyedCodec<>(COUNT_MIN, Codec.STRING, false), (d, v) -> d.countMin = v, d -> d.countMin).add()
        .append(new KeyedCodec<>(COUNT_MAX, Codec.STRING, false), (d, v) -> d.countMax = v, d -> d.countMax).add()
        .append(new KeyedCodec<>(MAX_ALIVE, Codec.STRING, false), (d, v) -> d.maxAlive = v, d -> d.maxAlive).add()
        .append(new KeyedCodec<>(ACTIVATION, Codec.STRING, false), (d, v) -> d.activation = v, d -> d.activation).add()
        .append(new KeyedCodec<>(MAX_HEALTH, Codec.STRING, false), (d, v) -> d.maxHealth = v, d -> d.maxHealth).add()
        .append(new KeyedCodec<>(SCALE, Codec.FLOAT, false), (d, v) -> d.scale = v, d -> d.scale).add()
        .append(new KeyedCodec<>(SPEED, Codec.FLOAT, false), (d, v) -> d.speed = v, d -> d.speed).add()
        .append(new KeyedCodec<>(HORIZONTAL, Codec.STRING, false), (d, v) -> d.horizontal = v, d -> d.horizontal).add()
        .append(new KeyedCodec<>(MAX_LIGHT, Codec.STRING, false), (d, v) -> d.maxLight = v, d -> d.maxLight).add()
        .append(new KeyedCodec<>(HELD_ITEM, Codec.STRING, false), (d, v) -> d.heldItem = v, d -> d.heldItem).add()
        .append(new KeyedCodec<>(AGGRESSION, Codec.STRING, false), (d, v) -> d.aggression = v, d -> d.aggression).add()
        .append(new KeyedCodec<>(LOOT_MODE, Codec.STRING, false), (d, v) -> d.lootMode = v, d -> d.lootMode).add()
        .append(new KeyedCodec<>(ARMOR_HEAD, Codec.STRING, false), (d, v) -> d.armorHead = v, d -> d.armorHead).add()
        .append(new KeyedCodec<>(ARMOR_CHEST, Codec.STRING, false), (d, v) -> d.armorChest = v, d -> d.armorChest).add()
        .append(new KeyedCodec<>(ARMOR_HANDS, Codec.STRING, false), (d, v) -> d.armorHands = v, d -> d.armorHands).add()
        .append(new KeyedCodec<>(ARMOR_LEGS, Codec.STRING, false), (d, v) -> d.armorLegs = v, d -> d.armorLegs).add()
        .append(new KeyedCodec<>(lootItemKey(0), Codec.STRING, false), (d, v) -> d.lootItem[0] = v, d -> d.lootItem[0]).add()
        .append(new KeyedCodec<>(lootMinKey(0), Codec.STRING, false), (d, v) -> d.lootMin[0] = v, d -> d.lootMin[0]).add()
        .append(new KeyedCodec<>(lootMaxKey(0), Codec.STRING, false), (d, v) -> d.lootMax[0] = v, d -> d.lootMax[0]).add()
        .append(new KeyedCodec<>(lootChanceKey(0), Codec.STRING, false), (d, v) -> d.lootChance[0] = v, d -> d.lootChance[0]).add()
        .append(new KeyedCodec<>(lootItemKey(1), Codec.STRING, false), (d, v) -> d.lootItem[1] = v, d -> d.lootItem[1]).add()
        .append(new KeyedCodec<>(lootMinKey(1), Codec.STRING, false), (d, v) -> d.lootMin[1] = v, d -> d.lootMin[1]).add()
        .append(new KeyedCodec<>(lootMaxKey(1), Codec.STRING, false), (d, v) -> d.lootMax[1] = v, d -> d.lootMax[1]).add()
        .append(new KeyedCodec<>(lootChanceKey(1), Codec.STRING, false), (d, v) -> d.lootChance[1] = v, d -> d.lootChance[1]).add()
        .append(new KeyedCodec<>(lootItemKey(2), Codec.STRING, false), (d, v) -> d.lootItem[2] = v, d -> d.lootItem[2]).add()
        .append(new KeyedCodec<>(lootMinKey(2), Codec.STRING, false), (d, v) -> d.lootMin[2] = v, d -> d.lootMin[2]).add()
        .append(new KeyedCodec<>(lootMaxKey(2), Codec.STRING, false), (d, v) -> d.lootMax[2] = v, d -> d.lootMax[2]).add()
        .append(new KeyedCodec<>(lootChanceKey(2), Codec.STRING, false), (d, v) -> d.lootChance[2] = v, d -> d.lootChance[2]).add()
        .append(new KeyedCodec<>(lootItemKey(3), Codec.STRING, false), (d, v) -> d.lootItem[3] = v, d -> d.lootItem[3]).add()
        .append(new KeyedCodec<>(lootMinKey(3), Codec.STRING, false), (d, v) -> d.lootMin[3] = v, d -> d.lootMin[3]).add()
        .append(new KeyedCodec<>(lootMaxKey(3), Codec.STRING, false), (d, v) -> d.lootMax[3] = v, d -> d.lootMax[3]).add()
        .append(new KeyedCodec<>(lootChanceKey(3), Codec.STRING, false), (d, v) -> d.lootChance[3] = v, d -> d.lootChance[3]).add()
        .append(new KeyedCodec<>(lootItemKey(4), Codec.STRING, false), (d, v) -> d.lootItem[4] = v, d -> d.lootItem[4]).add()
        .append(new KeyedCodec<>(lootMinKey(4), Codec.STRING, false), (d, v) -> d.lootMin[4] = v, d -> d.lootMin[4]).add()
        .append(new KeyedCodec<>(lootMaxKey(4), Codec.STRING, false), (d, v) -> d.lootMax[4] = v, d -> d.lootMax[4]).add()
        .append(new KeyedCodec<>(lootChanceKey(4), Codec.STRING, false), (d, v) -> d.lootChance[4] = v, d -> d.lootChance[4]).add()
        .build();

    String action, search, config, tag, role, mobName, cadenceMin, cadenceMax, countMin, countMax;
    String maxAlive, activation, maxHealth, horizontal, maxLight, heldItem;
    Float scale, speed;
    String aggression, lootMode, armorHead, armorChest, armorHands, armorLegs;
    final String[] lootItem = new String[SpawnerLootEntry.MAX_ENTRIES];
    final String[] lootMin = new String[SpawnerLootEntry.MAX_ENTRIES];
    final String[] lootMax = new String[SpawnerLootEntry.MAX_ENTRIES];
    final String[] lootChance = new String[SpawnerLootEntry.MAX_ENTRIES];
  }
}
