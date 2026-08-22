package gg.orbgenesis.configurablespawners;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.concurrent.ThreadLocalRandom;

final class SpawnerMobProfile {
  static final int MAX_PROFILES = 12;

  static final BuilderCodec<SpawnerMobProfile> CODEC =
      BuilderCodec.builder(SpawnerMobProfile.class, SpawnerMobProfile::new)
          .append(new KeyedCodec<>("Role", Codec.STRING, false), (p, v) -> p.roleId = clean(v), p -> p.roleId).add()
          .append(new KeyedCodec<>("MobName", Codec.STRING, false), (p, v) -> p.mobName = clean(v), p -> p.mobName).add()
          .append(new KeyedCodec<>("Weight", Codec.DOUBLE, false), (p, v) -> p.weight = v == null ? 1.0 : v, p -> p.weight).add()
          .append(new KeyedCodec<>("HeldItem", Codec.STRING, false), (p, v) -> p.heldItemId = clean(v), p -> p.heldItemId).add()
          .append(new KeyedCodec<>("MaxHealth", Codec.DOUBLE, false), (p, v) -> p.maxHealth = v == null ? 0.0 : v, p -> p.maxHealth).add()
          .append(new KeyedCodec<>("MobScale", Codec.DOUBLE, false), (p, v) -> p.mobScale = v == null ? 1.0 : v, p -> p.mobScale).add()
          .append(new KeyedCodec<>("MobSpeed", Codec.DOUBLE, false), (p, v) -> p.mobSpeed = v == null ? 1.0 : v, p -> p.mobSpeed).add()
          .append(new KeyedCodec<>("CustomArmor", Codec.BOOLEAN, false), (p, v) -> p.customArmor = v != null && v, p -> p.customArmor).add()
          .append(new KeyedCodec<>("ArmorHead", Codec.STRING, false), (p, v) -> p.armorHeadId = clean(v), p -> p.armorHeadId).add()
          .append(new KeyedCodec<>("ArmorChest", Codec.STRING, false), (p, v) -> p.armorChestId = clean(v), p -> p.armorChestId).add()
          .append(new KeyedCodec<>("ArmorHands", Codec.STRING, false), (p, v) -> p.armorHandsId = clean(v), p -> p.armorHandsId).add()
          .append(new KeyedCodec<>("ArmorLegs", Codec.STRING, false), (p, v) -> p.armorLegsId = clean(v), p -> p.armorLegsId).add()
          .append(new KeyedCodec<>("Aggression", new EnumCodec<>(AggressionMode.class), false), (p, v) -> p.aggressionMode = v == null ? AggressionMode.ROLE_DEFAULT : v, p -> p.aggressionMode).add()
          .append(new KeyedCodec<>("LootMode", new EnumCodec<>(LootMode.class), false), (p, v) -> p.lootMode = v == null ? LootMode.DEFAULT : v, p -> p.lootMode).add()
          .append(new KeyedCodec<>("Loot", new ArrayCodec<>(SpawnerLootEntry.CODEC, SpawnerLootEntry[]::new), false), (p, v) -> p.lootEntries = copyLoot(v), p -> p.lootEntries).add()
          .append(new KeyedCodec<>("EliteEnabled", Codec.BOOLEAN, false), (p, v) -> p.eliteEnabled = v != null && v, p -> p.eliteEnabled).add()
          .append(new KeyedCodec<>("EliteChance", Codec.DOUBLE, false), (p, v) -> p.eliteChancePercent = v == null ? 0.0 : v, p -> p.eliteChancePercent).add()
          .append(new KeyedCodec<>("ElitePrefix", Codec.STRING, false), (p, v) -> p.elitePrefix = clean(v), p -> p.elitePrefix).add()
          .append(new KeyedCodec<>("EliteHealthMultiplier", Codec.DOUBLE, false), (p, v) -> p.eliteHealthMultiplier = v == null ? 2.0 : v, p -> p.eliteHealthMultiplier).add()
          .append(new KeyedCodec<>("EliteScaleMultiplier", Codec.DOUBLE, false), (p, v) -> p.eliteScaleMultiplier = v == null ? 1.25 : v, p -> p.eliteScaleMultiplier).add()
          .append(new KeyedCodec<>("EliteSpeedMultiplier", Codec.DOUBLE, false), (p, v) -> p.eliteSpeedMultiplier = v == null ? 1.15 : v, p -> p.eliteSpeedMultiplier).add()
          .append(new KeyedCodec<>("EliteOverrideEquipment", Codec.BOOLEAN, false), (p, v) -> p.eliteOverrideEquipment = v != null && v, p -> p.eliteOverrideEquipment).add()
          .append(new KeyedCodec<>("EliteHeldItem", Codec.STRING, false), (p, v) -> p.eliteHeldItemId = clean(v), p -> p.eliteHeldItemId).add()
          .append(new KeyedCodec<>("EliteArmorHead", Codec.STRING, false), (p, v) -> p.eliteArmorHeadId = clean(v), p -> p.eliteArmorHeadId).add()
          .append(new KeyedCodec<>("EliteArmorChest", Codec.STRING, false), (p, v) -> p.eliteArmorChestId = clean(v), p -> p.eliteArmorChestId).add()
          .append(new KeyedCodec<>("EliteArmorHands", Codec.STRING, false), (p, v) -> p.eliteArmorHandsId = clean(v), p -> p.eliteArmorHandsId).add()
          .append(new KeyedCodec<>("EliteArmorLegs", Codec.STRING, false), (p, v) -> p.eliteArmorLegsId = clean(v), p -> p.eliteArmorLegsId).add()
          .append(new KeyedCodec<>("EliteLoot", new ArrayCodec<>(SpawnerLootEntry.CODEC, SpawnerLootEntry[]::new), false), (p, v) -> p.eliteLootEntries = copyLoot(v), p -> p.eliteLootEntries).add()
          .build();

  String roleId = "";
  String mobName = "";
  double weight = 1.0;
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
  boolean eliteEnabled;
  double eliteChancePercent;
  String elitePrefix = "Élite";
  double eliteHealthMultiplier = 2.0;
  double eliteScaleMultiplier = 1.25;
  double eliteSpeedMultiplier = 1.15;
  boolean eliteOverrideEquipment;
  String eliteHeldItemId = "";
  String eliteArmorHeadId = "";
  String eliteArmorChestId = "";
  String eliteArmorHandsId = "";
  String eliteArmorLegsId = "";
  SpawnerLootEntry[] eliteLootEntries = emptyLoot();

  void normalize() {
    roleId = clean(roleId);
    mobName = clean(mobName);
    if (mobName.length() > 64) mobName = mobName.substring(0, 64);
    weight = clamp(weight, 0.01, 100000.0);
    heldItemId = clean(heldItemId);
    maxHealth = clamp(maxHealth, 0.0, 100000.0);
    mobScale = clamp(mobScale, 0.1, 5.0);
    mobSpeed = rounded(clamp(mobSpeed, 0.0, 3.0));
    armorHeadId = clean(armorHeadId);
    armorChestId = clean(armorChestId);
    armorHandsId = clean(armorHandsId);
    armorLegsId = clean(armorLegsId);
    aggressionMode = aggressionMode == null ? AggressionMode.ROLE_DEFAULT : aggressionMode;
    lootMode = lootMode == null ? LootMode.DEFAULT : lootMode;
    lootEntries = copyLoot(lootEntries);
    eliteChancePercent = clamp(eliteChancePercent, 0.0, 100.0);
    elitePrefix = clean(elitePrefix);
    if (elitePrefix.isBlank()) elitePrefix = "Élite";
    if (elitePrefix.length() > 32) elitePrefix = elitePrefix.substring(0, 32);
    eliteHealthMultiplier = clamp(eliteHealthMultiplier, 0.1, 100.0);
    eliteScaleMultiplier = clamp(eliteScaleMultiplier, 0.1, 5.0);
    eliteSpeedMultiplier = clamp(eliteSpeedMultiplier, 0.0, 3.0);
    eliteHeldItemId = clean(eliteHeldItemId);
    eliteArmorHeadId = clean(eliteArmorHeadId);
    eliteArmorChestId = clean(eliteArmorChestId);
    eliteArmorHandsId = clean(eliteArmorHandsId);
    eliteArmorLegsId = clean(eliteArmorLegsId);
    eliteLootEntries = copyLoot(eliteLootEntries);
  }

  boolean rollElite(ThreadLocalRandom random) {
    return eliteEnabled && eliteChancePercent > 0.0
        && random.nextDouble(100.0) < eliteChancePercent;
  }

  String effectiveBaseName() {
    if (!mobName.isBlank()) return mobName;
    String value = roleId.replace('_', ' ').trim();
    return value.isBlank() ? "mob" : value;
  }

  SpawnerMobProfile copy() {
    SpawnerMobProfile copy = new SpawnerMobProfile();
    copy.roleId = roleId;
    copy.mobName = mobName;
    copy.weight = weight;
    copy.heldItemId = heldItemId;
    copy.maxHealth = maxHealth;
    copy.mobScale = mobScale;
    copy.mobSpeed = mobSpeed;
    copy.customArmor = customArmor;
    copy.armorHeadId = armorHeadId;
    copy.armorChestId = armorChestId;
    copy.armorHandsId = armorHandsId;
    copy.armorLegsId = armorLegsId;
    copy.aggressionMode = aggressionMode;
    copy.lootMode = lootMode;
    copy.lootEntries = copyLoot(lootEntries);
    copy.eliteEnabled = eliteEnabled;
    copy.eliteChancePercent = eliteChancePercent;
    copy.elitePrefix = elitePrefix;
    copy.eliteHealthMultiplier = eliteHealthMultiplier;
    copy.eliteScaleMultiplier = eliteScaleMultiplier;
    copy.eliteSpeedMultiplier = eliteSpeedMultiplier;
    copy.eliteOverrideEquipment = eliteOverrideEquipment;
    copy.eliteHeldItemId = eliteHeldItemId;
    copy.eliteArmorHeadId = eliteArmorHeadId;
    copy.eliteArmorChestId = eliteArmorChestId;
    copy.eliteArmorHandsId = eliteArmorHandsId;
    copy.eliteArmorLegsId = eliteArmorLegsId;
    copy.eliteLootEntries = copyLoot(eliteLootEntries);
    return copy;
  }

  private static SpawnerLootEntry[] emptyLoot() {
    SpawnerLootEntry[] entries = new SpawnerLootEntry[SpawnerLootEntry.MAX_ENTRIES];
    for (int i = 0; i < entries.length; i++) entries[i] = new SpawnerLootEntry();
    return entries;
  }

  static SpawnerLootEntry[] copyLoot(SpawnerLootEntry[] source) {
    SpawnerLootEntry[] entries = emptyLoot();
    if (source == null) return entries;
    for (int i = 0; i < Math.min(source.length, entries.length); i++) {
      entries[i] = source[i] == null ? new SpawnerLootEntry() : source[i].copy();
    }
    return entries;
  }

  private static double rounded(double value) {
    return Math.round(value * 100.0) / 100.0;
  }

  private static double clamp(double value, double minimum, double maximum) {
    return Math.max(minimum, Math.min(maximum, value));
  }

  private static String clean(String value) {
    return value == null ? "" : value.trim();
  }
}
