package gg.orbgenesis.configurablespawners;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.UUID;
import javax.annotation.Nullable;

public final class ConfigurableSpawnerComponent implements Component<ChunkStore> {
  private static ComponentType<ChunkStore, ConfigurableSpawnerComponent> componentType;

  public static final BuilderCodec<ConfigurableSpawnerComponent> CODEC =
      BuilderCodec.builder(ConfigurableSpawnerComponent.class, ConfigurableSpawnerComponent::new)
          .append(new KeyedCodec<>("SpawnerId", Codec.UUID_BINARY, false), (c, v) -> c.spawnerId = v, c -> c.spawnerId).add()
          .append(new KeyedCodec<>("Tag", Codec.STRING, false), (c, v) -> c.tag = v == null ? "" : v, c -> c.tag).add()
          .append(new KeyedCodec<>("Tags", new ArrayCodec<>(Codec.STRING, String[]::new), false), (c, v) -> c.tags = v == null ? new String[0] : v, c -> c.tags).add()
          .append(new KeyedCodec<>("Enabled", Codec.BOOLEAN, false), (c, v) -> c.enabled = v == null || v, c -> c.enabled).add()
          .append(new KeyedCodec<>("Role", Codec.STRING, false), (c, v) -> c.roleId = v == null ? "" : v, c -> c.roleId).add()
          .append(new KeyedCodec<>("MobName", Codec.STRING, false), (c, v) -> c.mobName = clean(v), c -> c.mobName).add()
          .append(new KeyedCodec<>("HeldItem", Codec.STRING, false), (c, v) -> c.heldItemId = v == null ? "" : v, c -> c.heldItemId).add()
          .append(new KeyedCodec<>("MaxHealth", Codec.DOUBLE, false), (c, v) -> c.maxHealth = v == null ? 0.0 : v, c -> c.maxHealth).add()
          .append(new KeyedCodec<>("MobScale", Codec.DOUBLE, false), (c, v) -> c.mobScale = v == null ? 1.0 : v, c -> c.mobScale).add()
          .append(new KeyedCodec<>("MobSpeed", Codec.DOUBLE, false), (c, v) -> c.mobSpeed = v == null ? 1.0 : v, c -> c.mobSpeed).add()
          .append(new KeyedCodec<>("CustomArmor", Codec.BOOLEAN, false), (c, v) -> c.customArmor = v != null && v, c -> c.customArmor).add()
          .append(new KeyedCodec<>("ArmorHead", Codec.STRING, false), (c, v) -> c.armorHeadId = clean(v), c -> c.armorHeadId).add()
          .append(new KeyedCodec<>("ArmorChest", Codec.STRING, false), (c, v) -> c.armorChestId = clean(v), c -> c.armorChestId).add()
          .append(new KeyedCodec<>("ArmorHands", Codec.STRING, false), (c, v) -> c.armorHandsId = clean(v), c -> c.armorHandsId).add()
          .append(new KeyedCodec<>("ArmorLegs", Codec.STRING, false), (c, v) -> c.armorLegsId = clean(v), c -> c.armorLegsId).add()
          .append(new KeyedCodec<>("Aggression", new EnumCodec<>(AggressionMode.class), false), (c, v) -> c.aggressionMode = v == null ? AggressionMode.ROLE_DEFAULT : v, c -> c.aggressionMode).add()
          .append(new KeyedCodec<>("LootMode", new EnumCodec<>(LootMode.class), false), (c, v) -> c.lootMode = v == null ? LootMode.DEFAULT : v, c -> c.lootMode).add()
          .append(new KeyedCodec<>("Loot", new ArrayCodec<>(SpawnerLootEntry.CODEC, SpawnerLootEntry[]::new), false), (c, v) -> c.lootEntries = copyLoot(v), c -> c.lootEntries).add()
          .append(new KeyedCodec<>("MobProfiles", new ArrayCodec<>(SpawnerMobProfile.CODEC, SpawnerMobProfile[]::new), false), (c, v) -> c.mobProfiles = copyProfiles(v), c -> c.mobProfiles).add()
          .append(new KeyedCodec<>("CadenceMinSeconds", Codec.DOUBLE, false), (c, v) -> c.cadenceMinSeconds = v == null ? 5.0 : v, c -> c.cadenceMinSeconds).add()
          .append(new KeyedCodec<>("CadenceMaxSeconds", Codec.DOUBLE, false), (c, v) -> c.cadenceMaxSeconds = v == null ? 10.0 : v, c -> c.cadenceMaxSeconds).add()
          .append(new KeyedCodec<>("SpawnCountMin", Codec.INTEGER, false), (c, v) -> c.spawnCountMin = v == null ? 1 : v, c -> c.spawnCountMin).add()
          .append(new KeyedCodec<>("SpawnCountMax", Codec.INTEGER, false), (c, v) -> c.spawnCountMax = v == null ? 3 : v, c -> c.spawnCountMax).add()
          .append(new KeyedCodec<>("MaxAlive", Codec.INTEGER, false), (c, v) -> c.maxAlive = v == null ? 6 : v, c -> c.maxAlive).add()
          .append(new KeyedCodec<>("ActivationRadius", Codec.DOUBLE, false), (c, v) -> c.activationRadius = v == null ? 16.0 : v, c -> c.activationRadius).add()
          .append(new KeyedCodec<>("HorizontalRadius", Codec.DOUBLE, false), (c, v) -> c.horizontalRadius = v == null ? 4.0 : v, c -> c.horizontalRadius).add()
          .append(new KeyedCodec<>("VerticalRadius", Codec.INTEGER, false), (c, v) -> c.verticalRadius = v == null ? 2 : v, c -> c.verticalRadius).add()
          .append(new KeyedCodec<>("MinLight", Codec.INTEGER, false), (c, v) -> c.minLight = v == null ? 0 : v, c -> c.minLight).add()
          .append(new KeyedCodec<>("MaxLight", Codec.INTEGER, false), (c, v) -> c.maxLight = v == null ? 15 : v, c -> c.maxLight).add()
          .append(new KeyedCodec<>("SpawnAttempts", Codec.INTEGER, false), (c, v) -> c.spawnAttempts = v == null ? 12 : v, c -> c.spawnAttempts).add()
          .append(new KeyedCodec<>("CooldownSeconds", Codec.DOUBLE, false), (c, v) -> c.cooldownSeconds = v == null ? 5.0 : v, c -> c.cooldownSeconds).add()
          .append(new KeyedCodec<>("TrackedMobs", new ArrayCodec<>(Codec.UUID_BINARY, UUID[]::new), false), (c, v) -> c.trackedMobs = v == null ? new UUID[0] : v, c -> c.trackedMobs).add()
          .build();

  UUID spawnerId;
  String tag = "";
  String[] tags = new String[0];
  boolean enabled = true;
  String roleId = "";
  String mobName = "";
  String heldItemId = "";
  double maxHealth;
  double mobScale = 1.0;
  double mobSpeed = 1.0;
  boolean customArmor;
  String armorHeadId = "";
  String armorChestId = "";
  String armorHandsId = "";
  String armorLegsId = "";
  AggressionMode aggressionMode = AggressionMode.ROLE_DEFAULT;
  LootMode lootMode = LootMode.DEFAULT;
  SpawnerLootEntry[] lootEntries = emptyLoot();
  SpawnerMobProfile[] mobProfiles = new SpawnerMobProfile[0];
  double cadenceMinSeconds = 5.0;
  double cadenceMaxSeconds = 10.0;
  int spawnCountMin = 1;
  int spawnCountMax = 3;
  int maxAlive = 6;
  double activationRadius = 16.0;
  double horizontalRadius = 4.0;
  int verticalRadius = 2;
  int minLight = 0;
  int maxLight = 15;
  int spawnAttempts = 12;
  double cooldownSeconds = 5.0;
  UUID[] trackedMobs = new UUID[0];

  transient double maintenanceSeconds;
  transient boolean invalidRoleLogged;

  public static ComponentType<ChunkStore, ConfigurableSpawnerComponent> getComponentType() {
    return componentType;
  }

  public static void setComponentType(ComponentType<ChunkStore, ConfigurableSpawnerComponent> type) {
    componentType = type;
  }

  public UUID ensureSpawnerId() {
    if (spawnerId == null) {
      spawnerId = UUID.randomUUID();
    }
    return spawnerId;
  }

  public void normalize() {
    // Kept in the codec only so old saves with the former manual toggle remain readable.
    enabled = true;
    tags = normalizeTags(tag, tags);
    tag = tags.length == 0 ? "" : tags[0];
    roleId = roleId == null ? "" : roleId.trim();
    mobName = clean(mobName);
    if (mobName.length() > 64) mobName = mobName.substring(0, 64);
    heldItemId = heldItemId == null ? "" : heldItemId.trim();
    armorHeadId = clean(armorHeadId);
    armorChestId = clean(armorChestId);
    armorHandsId = clean(armorHandsId);
    armorLegsId = clean(armorLegsId);
    maxHealth = clamp(maxHealth, 0.0, 100000.0);
    mobScale = clamp(mobScale, 0.1, 5.0);
    mobSpeed = Math.round(clamp(mobSpeed, 0.0, 3.0) * 10.0) / 10.0;
    aggressionMode = aggressionMode == null ? AggressionMode.ROLE_DEFAULT : aggressionMode;
    lootMode = lootMode == null ? LootMode.DEFAULT : lootMode;
    cadenceMinSeconds = clamp(cadenceMinSeconds, 0.25, 3600.0);
    cadenceMaxSeconds = clamp(cadenceMaxSeconds, 0.25, 3600.0);
    if (cadenceMinSeconds > cadenceMaxSeconds) {
      double swap = cadenceMinSeconds;
      cadenceMinSeconds = cadenceMaxSeconds;
      cadenceMaxSeconds = swap;
    }
    spawnCountMin = clamp(spawnCountMin, 1, 64);
    spawnCountMax = clamp(spawnCountMax, 1, 64);
    spawnCountMax = Math.max(spawnCountMin, spawnCountMax);
    maxAlive = clamp(maxAlive, 1, 256);
    activationRadius = clamp(activationRadius, 1.0, 128.0);
    horizontalRadius = clamp(horizontalRadius, 0.0, 64.0);
    verticalRadius = clamp(verticalRadius, 0, 32);
    // Kept in the codec only so 0.3.0 can read blocks created by older builds.
    minLight = 0;
    maxLight = clamp(maxLight, 0, 15);
    spawnAttempts = clamp(spawnAttempts, 1, 64);
    cooldownSeconds = clamp(cooldownSeconds, 0.0, cadenceMaxSeconds);
    trackedMobs = trackedMobs == null ? new UUID[0] : trackedMobs;
    lootEntries = copyLoot(lootEntries);
    if (mobProfiles == null || mobProfiles.length == 0) {
      mobProfiles = new SpawnerMobProfile[] {profileFromLegacy()};
    } else {
      mobProfiles = copyProfiles(mobProfiles);
    }
    for (SpawnerMobProfile profile : mobProfiles) profile.normalize();
    syncLegacyFromFirstProfile();
  }

  public void copyConfigurationFrom(ConfigurableSpawnerComponent source) {
    tag = source.tag;
    tags = source.tags == null ? new String[0] : Arrays.copyOf(source.tags, source.tags.length);
    enabled = true;
    roleId = source.roleId;
    mobName = source.mobName;
    heldItemId = source.heldItemId;
    maxHealth = source.maxHealth;
    mobScale = source.mobScale;
    mobSpeed = source.mobSpeed;
    customArmor = source.customArmor;
    armorHeadId = source.armorHeadId;
    armorChestId = source.armorChestId;
    armorHandsId = source.armorHandsId;
    armorLegsId = source.armorLegsId;
    aggressionMode = source.aggressionMode;
    lootMode = source.lootMode;
    lootEntries = copyLoot(source.lootEntries);
    mobProfiles = copyProfiles(source.mobProfiles);
    cadenceMinSeconds = source.cadenceMinSeconds;
    cadenceMaxSeconds = source.cadenceMaxSeconds;
    spawnCountMin = source.spawnCountMin;
    spawnCountMax = source.spawnCountMax;
    maxAlive = source.maxAlive;
    activationRadius = source.activationRadius;
    horizontalRadius = source.horizontalRadius;
    verticalRadius = source.verticalRadius;
    minLight = 0;
    maxLight = source.maxLight;
    spawnAttempts = source.spawnAttempts;
    normalize();
  }

  public ConfigurableSpawnerComponent copyConfiguration() {
    ConfigurableSpawnerComponent copy = new ConfigurableSpawnerComponent();
    copy.copyConfigurationFrom(this);
    copy.spawnerId = spawnerId;
    return copy;
  }

  @Override
  @Nullable
  public Component<ChunkStore> clone() {
    ConfigurableSpawnerComponent copy = copyConfiguration();
    copy.cooldownSeconds = cooldownSeconds;
    copy.trackedMobs = Arrays.copyOf(trackedMobs, trackedMobs.length);
    copy.maintenanceSeconds = maintenanceSeconds;
    copy.invalidRoleLogged = invalidRoleLogged;
    return copy;
  }

  private static SpawnerLootEntry[] emptyLoot() {
    SpawnerLootEntry[] entries = new SpawnerLootEntry[SpawnerLootEntry.MAX_ENTRIES];
    for (int i = 0; i < entries.length; i++) {
      entries[i] = new SpawnerLootEntry();
    }
    return entries;
  }

  private static SpawnerLootEntry[] copyLoot(SpawnerLootEntry[] source) {
    SpawnerLootEntry[] entries = emptyLoot();
    if (source == null) {
      return entries;
    }
    for (int i = 0; i < Math.min(source.length, entries.length); i++) {
      entries[i] = source[i] == null ? new SpawnerLootEntry() : source[i].copy();
    }
    return entries;
  }

  SpawnerMobProfile[] usableProfiles() {
    return Arrays.stream(mobProfiles).filter(profile -> !profile.roleId.isBlank())
        .toArray(SpawnerMobProfile[]::new);
  }

  SpawnerMobProfile selectProfile(double roll) {
    SpawnerMobProfile[] usable = usableProfiles();
    if (usable.length == 0) return null;
    double total = Arrays.stream(usable).mapToDouble(profile -> profile.weight).sum();
    double cursor = Math.max(0.0, Math.min(Math.nextDown(1.0), roll)) * total;
    for (SpawnerMobProfile profile : usable) {
      cursor -= profile.weight;
      if (cursor < 0.0) return profile;
    }
    return usable[usable.length - 1];
  }

  void updateFirstProfileFromLegacy() {
    SpawnerMobProfile first = profileFromLegacy();
    if (mobProfiles == null || mobProfiles.length == 0) {
      mobProfiles = new SpawnerMobProfile[] {first};
    } else {
      SpawnerMobProfile[] updated = copyProfiles(mobProfiles);
      double weight = updated[0].weight;
      boolean eliteEnabled = updated[0].eliteEnabled;
      double eliteChance = updated[0].eliteChancePercent;
      String elitePrefix = updated[0].elitePrefix;
      double eliteHealth = updated[0].eliteHealthMultiplier;
      double eliteScale = updated[0].eliteScaleMultiplier;
      double eliteSpeed = updated[0].eliteSpeedMultiplier;
      boolean eliteEquipment = updated[0].eliteOverrideEquipment;
      String eliteHeld = updated[0].eliteHeldItemId;
      String eliteHead = updated[0].eliteArmorHeadId;
      String eliteChest = updated[0].eliteArmorChestId;
      String eliteHands = updated[0].eliteArmorHandsId;
      String eliteLegs = updated[0].eliteArmorLegsId;
      SpawnerLootEntry[] eliteLoot = SpawnerMobProfile.copyLoot(updated[0].eliteLootEntries);
      first.weight = weight;
      first.eliteEnabled = eliteEnabled;
      first.eliteChancePercent = eliteChance;
      first.elitePrefix = elitePrefix;
      first.eliteHealthMultiplier = eliteHealth;
      first.eliteScaleMultiplier = eliteScale;
      first.eliteSpeedMultiplier = eliteSpeed;
      first.eliteOverrideEquipment = eliteEquipment;
      first.eliteHeldItemId = eliteHeld;
      first.eliteArmorHeadId = eliteHead;
      first.eliteArmorChestId = eliteChest;
      first.eliteArmorHandsId = eliteHands;
      first.eliteArmorLegsId = eliteLegs;
      first.eliteLootEntries = eliteLoot;
      updated[0] = first;
      mobProfiles = updated;
    }
  }

  private SpawnerMobProfile profileFromLegacy() {
    SpawnerMobProfile profile = new SpawnerMobProfile();
    profile.roleId = roleId;
    profile.mobName = mobName;
    profile.heldItemId = heldItemId;
    profile.maxHealth = maxHealth;
    profile.mobScale = mobScale;
    profile.mobSpeed = mobSpeed;
    profile.customArmor = customArmor;
    profile.armorHeadId = armorHeadId;
    profile.armorChestId = armorChestId;
    profile.armorHandsId = armorHandsId;
    profile.armorLegsId = armorLegsId;
    profile.aggressionMode = aggressionMode;
    profile.lootMode = lootMode;
    profile.lootEntries = copyLoot(lootEntries);
    profile.normalize();
    return profile;
  }

  private void syncLegacyFromFirstProfile() {
    if (mobProfiles.length == 0) return;
    SpawnerMobProfile first = mobProfiles[0];
    roleId = first.roleId;
    mobName = first.mobName;
    heldItemId = first.heldItemId;
    maxHealth = first.maxHealth;
    mobScale = first.mobScale;
    mobSpeed = first.mobSpeed;
    customArmor = first.customArmor;
    armorHeadId = first.armorHeadId;
    armorChestId = first.armorChestId;
    armorHandsId = first.armorHandsId;
    armorLegsId = first.armorLegsId;
    aggressionMode = first.aggressionMode;
    lootMode = first.lootMode;
    lootEntries = copyLoot(first.lootEntries);
  }

  private static SpawnerMobProfile[] copyProfiles(SpawnerMobProfile[] source) {
    if (source == null || source.length == 0) return new SpawnerMobProfile[0];
    int length = Math.min(source.length, SpawnerMobProfile.MAX_PROFILES);
    SpawnerMobProfile[] copy = new SpawnerMobProfile[length];
    for (int i = 0; i < length; i++) {
      copy[i] = source[i] == null ? new SpawnerMobProfile() : source[i].copy();
    }
    return copy;
  }

  private static int clamp(int value, int minimum, int maximum) {
    return Math.max(minimum, Math.min(maximum, value));
  }

  private static double clamp(double value, double minimum, double maximum) {
    return Math.max(minimum, Math.min(maximum, value));
  }

  private static String clean(String value) {
    return value == null ? "" : value.trim();
  }

  private static String[] normalizeTags(String legacyTag, String[] values) {
    LinkedHashSet<String> normalized = new LinkedHashSet<>();
    if (values != null) {
      for (String value : values) {
        String cleaned = clean(value);
        if (!cleaned.isEmpty()) normalized.add(cleaned);
      }
    }
    if (normalized.isEmpty()) {
      String cleaned = clean(legacyTag);
      if (!cleaned.isEmpty()) normalized.add(cleaned);
    }
    return normalized.toArray(String[]::new);
  }
}
