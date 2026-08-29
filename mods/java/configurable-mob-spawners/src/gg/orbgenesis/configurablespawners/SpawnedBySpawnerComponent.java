package gg.orbgenesis.configurablespawners;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

public final class SpawnedBySpawnerComponent implements Component<EntityStore> {
  private static ComponentType<EntityStore, SpawnedBySpawnerComponent> componentType;

  public static final BuilderCodec<SpawnedBySpawnerComponent> CODEC =
      BuilderCodec.builder(SpawnedBySpawnerComponent.class, SpawnedBySpawnerComponent::new)
          .append(new KeyedCodec<>("SpawnerId", Codec.UUID_BINARY), (c, v) -> c.spawnerId = v, c -> c.spawnerId).add()
          .append(new KeyedCodec<>("Aggression", new EnumCodec<>(AggressionMode.class), false), (c, v) -> c.aggressionMode = v == null ? AggressionMode.ROLE_DEFAULT : v, c -> c.aggressionMode).add()
          .append(new KeyedCodec<>("MobName", Codec.STRING, false), (c, v) -> c.mobName = v == null ? "" : v, c -> c.mobName).add()
          .append(new KeyedCodec<>("HeldItem", Codec.STRING, false), (c, v) -> c.heldItemId = v == null ? "" : v, c -> c.heldItemId).add()
          .append(new KeyedCodec<>("MaxHealth", Codec.DOUBLE, false), (c, v) -> c.maxHealth = v == null ? 0.0 : v, c -> c.maxHealth).add()
          .append(new KeyedCodec<>("MobScale", Codec.DOUBLE, false), (c, v) -> c.mobScale = v == null ? 1.0 : v, c -> c.mobScale).add()
          .append(new KeyedCodec<>("MobSpeed", Codec.DOUBLE, false), (c, v) -> c.mobSpeed = v == null ? 1.0 : v, c -> c.mobSpeed).add()
          .append(new KeyedCodec<>("PhysicalScaleApplied", Codec.BOOLEAN, false), (c, v) -> c.physicalScaleApplied = v != null && v, c -> c.physicalScaleApplied).add()
          .append(new KeyedCodec<>("CustomArmor", Codec.BOOLEAN, false), (c, v) -> c.customArmor = v != null && v, c -> c.customArmor).add()
          .append(new KeyedCodec<>("ArmorHead", Codec.STRING, false), (c, v) -> c.armorHeadId = v == null ? "" : v, c -> c.armorHeadId).add()
          .append(new KeyedCodec<>("ArmorChest", Codec.STRING, false), (c, v) -> c.armorChestId = v == null ? "" : v, c -> c.armorChestId).add()
          .append(new KeyedCodec<>("ArmorHands", Codec.STRING, false), (c, v) -> c.armorHandsId = v == null ? "" : v, c -> c.armorHandsId).add()
          .append(new KeyedCodec<>("ArmorLegs", Codec.STRING, false), (c, v) -> c.armorLegsId = v == null ? "" : v, c -> c.armorLegsId).add()
          .append(new KeyedCodec<>("LootMode", new EnumCodec<>(LootMode.class), false), (c, v) -> c.lootMode = v == null ? LootMode.DEFAULT : v, c -> c.lootMode).add()
          .append(new KeyedCodec<>("Loot", new ArrayCodec<>(SpawnerLootEntry.CODEC, SpawnerLootEntry[]::new), false), (c, v) -> c.lootEntries = copyLoot(v), c -> c.lootEntries).add()
          .append(new KeyedCodec<>("Elite", Codec.BOOLEAN, false), (c, v) -> c.elite = v != null && v, c -> c.elite).add()
          .append(new KeyedCodec<>("BaseMobName", Codec.STRING, false), (c, v) -> c.baseMobName = v == null ? "" : v, c -> c.baseMobName).add()
          .append(new KeyedCodec<>("ElitePrefix", Codec.STRING, false), (c, v) -> c.elitePrefix = v == null ? "Élite" : v, c -> c.elitePrefix).add()
          .append(new KeyedCodec<>("HealthMultiplier", Codec.DOUBLE, false), (c, v) -> c.healthMultiplier = v == null ? 1.0 : v, c -> c.healthMultiplier).add()
          .append(new KeyedCodec<>("EliteLoot", new ArrayCodec<>(SpawnerLootEntry.CODEC, SpawnerLootEntry[]::new), false), (c, v) -> c.eliteLootEntries = copyLoot(v), c -> c.eliteLootEntries).add()
          .append(new KeyedCodec<>("LastPlayerAttacker", Codec.UUID_BINARY, false), (c, v) -> c.lastPlayerAttacker = v, c -> c.lastPlayerAttacker).add()
          .append(new KeyedCodec<>("RetaliationTarget", Codec.UUID_BINARY, false), (c, v) -> c.retaliationTarget = v, c -> c.retaliationTarget).add()
          .build();

  UUID spawnerId;
  AggressionMode aggressionMode = AggressionMode.ROLE_DEFAULT;
  String mobName = "";
  String heldItemId = "";
  double maxHealth;
  double mobScale = 1.0;
  double mobSpeed = 1.0;
  boolean physicalScaleApplied;
  boolean customArmor;
  String armorHeadId = "";
  String armorChestId = "";
  String armorHandsId = "";
  String armorLegsId = "";
  LootMode lootMode = LootMode.DEFAULT;
  SpawnerLootEntry[] lootEntries = new SpawnerLootEntry[0];
  boolean elite;
  String baseMobName = "";
  String elitePrefix = "Élite";
  double healthMultiplier = 1.0;
  SpawnerLootEntry[] eliteLootEntries = new SpawnerLootEntry[0];
  UUID lastPlayerAttacker;
  UUID retaliationTarget;
  transient boolean equipmentApplied;
  transient double presentationCheckSeconds;
  transient Set<UUID> titleViewers = new HashSet<>();

  public SpawnedBySpawnerComponent() {}

  public SpawnedBySpawnerComponent(UUID spawnerId, SpawnerMobProfile profile, boolean elite) {
    this.spawnerId = spawnerId;
    this.elite = elite;
    this.baseMobName = profile.effectiveBaseName();
    this.elitePrefix = profile.elitePrefix;
    this.aggressionMode = elite ? AggressionMode.HOSTILE : profile.aggressionMode;
    this.mobName = elite ? profile.elitePrefix + " " + baseMobName : profile.mobName;
    boolean override = elite && profile.eliteOverrideEquipment;
    this.heldItemId = override ? profile.eliteHeldItemId : profile.heldItemId;
    this.maxHealth = profile.maxHealth;
    this.healthMultiplier = elite ? profile.eliteHealthMultiplier : 1.0;
    this.mobScale = profile.mobScale * (elite ? profile.eliteScaleMultiplier : 1.0);
    this.mobSpeed = profile.mobSpeed * (elite ? profile.eliteSpeedMultiplier : 1.0);
    this.customArmor = override || profile.customArmor;
    this.armorHeadId = override ? profile.eliteArmorHeadId : profile.armorHeadId;
    this.armorChestId = override ? profile.eliteArmorChestId : profile.armorChestId;
    this.armorHandsId = override ? profile.eliteArmorHandsId : profile.armorHandsId;
    this.armorLegsId = override ? profile.eliteArmorLegsId : profile.armorLegsId;
    this.lootMode = profile.lootMode;
    this.lootEntries = copyLoot(profile.lootEntries);
    this.eliteLootEntries = elite ? copyLoot(profile.eliteLootEntries) : new SpawnerLootEntry[0];
  }

  public static ComponentType<EntityStore, SpawnedBySpawnerComponent> getComponentType() {
    return componentType;
  }

  public static void setComponentType(ComponentType<EntityStore, SpawnedBySpawnerComponent> type) {
    componentType = type;
  }

  @Override
  @Nullable
  public Component<EntityStore> clone() {
    SpawnedBySpawnerComponent copy = new SpawnedBySpawnerComponent();
    copy.spawnerId = spawnerId;
    copy.aggressionMode = aggressionMode;
    copy.mobName = mobName;
    copy.heldItemId = heldItemId;
    copy.maxHealth = maxHealth;
    copy.mobScale = mobScale;
    copy.mobSpeed = mobSpeed;
    copy.physicalScaleApplied = physicalScaleApplied;
    copy.customArmor = customArmor;
    copy.armorHeadId = armorHeadId;
    copy.armorChestId = armorChestId;
    copy.armorHandsId = armorHandsId;
    copy.armorLegsId = armorLegsId;
    copy.lootMode = lootMode;
    copy.lootEntries = copyLoot(lootEntries);
    copy.elite = elite;
    copy.baseMobName = baseMobName;
    copy.elitePrefix = elitePrefix;
    copy.healthMultiplier = healthMultiplier;
    copy.eliteLootEntries = copyLoot(eliteLootEntries);
    copy.lastPlayerAttacker = lastPlayerAttacker;
    copy.retaliationTarget = retaliationTarget;
    copy.equipmentApplied = equipmentApplied;
    return copy;
  }

  private static SpawnerLootEntry[] copyLoot(SpawnerLootEntry[] source) {
    if (source == null) {
      return new SpawnerLootEntry[0];
    }
    SpawnerLootEntry[] copy = new SpawnerLootEntry[source.length];
    for (int i = 0; i < source.length; i++) {
      copy[i] = source[i] == null ? new SpawnerLootEntry() : source[i].copy();
    }
    return copy;
  }
}
