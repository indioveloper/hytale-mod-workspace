package gg.orbgenesis.configurablespawners;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nullable;

public final class SpawnedBySpawnerComponent implements Component<EntityStore> {
  private static ComponentType<EntityStore, SpawnedBySpawnerComponent> componentType;

  public static final BuilderCodec<SpawnedBySpawnerComponent> CODEC =
      BuilderCodec.builder(SpawnedBySpawnerComponent.class, SpawnedBySpawnerComponent::new)
          .append(new KeyedCodec<>("SpawnerId", Codec.UUID_BINARY), (c, v) -> c.spawnerId = v, c -> c.spawnerId).add()
          .append(new KeyedCodec<>("Aggression", new EnumCodec<>(AggressionMode.class), false), (c, v) -> c.aggressionMode = v == null ? AggressionMode.ROLE_DEFAULT : v, c -> c.aggressionMode).add()
          .append(new KeyedCodec<>("HeldItem", Codec.STRING, false), (c, v) -> c.heldItemId = v == null ? "" : v, c -> c.heldItemId).add()
          .append(new KeyedCodec<>("MaxHealth", Codec.DOUBLE, false), (c, v) -> c.maxHealth = v == null ? 0.0 : v, c -> c.maxHealth).add()
          .append(new KeyedCodec<>("MobScale", Codec.DOUBLE, false), (c, v) -> c.mobScale = v == null ? 1.0 : v, c -> c.mobScale).add()
          .append(new KeyedCodec<>("CustomArmor", Codec.BOOLEAN, false), (c, v) -> c.customArmor = v != null && v, c -> c.customArmor).add()
          .append(new KeyedCodec<>("ArmorHead", Codec.STRING, false), (c, v) -> c.armorHeadId = v == null ? "" : v, c -> c.armorHeadId).add()
          .append(new KeyedCodec<>("ArmorChest", Codec.STRING, false), (c, v) -> c.armorChestId = v == null ? "" : v, c -> c.armorChestId).add()
          .append(new KeyedCodec<>("ArmorHands", Codec.STRING, false), (c, v) -> c.armorHandsId = v == null ? "" : v, c -> c.armorHandsId).add()
          .append(new KeyedCodec<>("ArmorLegs", Codec.STRING, false), (c, v) -> c.armorLegsId = v == null ? "" : v, c -> c.armorLegsId).add()
          .append(new KeyedCodec<>("LootMode", new EnumCodec<>(LootMode.class), false), (c, v) -> c.lootMode = v == null ? LootMode.DEFAULT : v, c -> c.lootMode).add()
          .append(new KeyedCodec<>("Loot", new ArrayCodec<>(SpawnerLootEntry.CODEC, SpawnerLootEntry[]::new), false), (c, v) -> c.lootEntries = copyLoot(v), c -> c.lootEntries).add()
          .append(new KeyedCodec<>("RetaliationTarget", Codec.UUID_BINARY, false), (c, v) -> c.retaliationTarget = v, c -> c.retaliationTarget).add()
          .build();

  UUID spawnerId;
  AggressionMode aggressionMode = AggressionMode.ROLE_DEFAULT;
  String heldItemId = "";
  double maxHealth;
  double mobScale = 1.0;
  boolean customArmor;
  String armorHeadId = "";
  String armorChestId = "";
  String armorHandsId = "";
  String armorLegsId = "";
  LootMode lootMode = LootMode.DEFAULT;
  SpawnerLootEntry[] lootEntries = new SpawnerLootEntry[0];
  UUID retaliationTarget;
  transient boolean equipmentApplied;

  public SpawnedBySpawnerComponent() {}

  public SpawnedBySpawnerComponent(UUID spawnerId, ConfigurableSpawnerComponent config) {
    this.spawnerId = spawnerId;
    this.aggressionMode = config.aggressionMode;
    this.heldItemId = config.heldItemId;
    this.maxHealth = config.maxHealth;
    this.mobScale = config.mobScale;
    this.customArmor = config.customArmor;
    this.armorHeadId = config.armorHeadId;
    this.armorChestId = config.armorChestId;
    this.armorHandsId = config.armorHandsId;
    this.armorLegsId = config.armorLegsId;
    this.lootMode = config.lootMode;
    this.lootEntries = copyLoot(config.lootEntries);
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
    copy.heldItemId = heldItemId;
    copy.maxHealth = maxHealth;
    copy.mobScale = mobScale;
    copy.customArmor = customArmor;
    copy.armorHeadId = armorHeadId;
    copy.armorChestId = armorChestId;
    copy.armorHandsId = armorHandsId;
    copy.armorLegsId = armorLegsId;
    copy.lootMode = lootMode;
    copy.lootEntries = copyLoot(lootEntries);
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
