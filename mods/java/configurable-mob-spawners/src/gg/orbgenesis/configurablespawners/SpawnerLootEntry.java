package gg.orbgenesis.configurablespawners;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public final class SpawnerLootEntry {
  public static final int MAX_ENTRIES = 5;

  public static final BuilderCodec<SpawnerLootEntry> CODEC =
      BuilderCodec.builder(SpawnerLootEntry.class, SpawnerLootEntry::new)
          .append(
              new KeyedCodec<>("Item", Codec.STRING, false),
              (entry, value) -> entry.itemId = value == null ? "" : value,
              entry -> entry.itemId)
          .add()
          .append(
              new KeyedCodec<>("Min", Codec.INTEGER, false),
              (entry, value) -> entry.minQuantity = value == null ? 1 : value,
              entry -> entry.minQuantity)
          .add()
          .append(
              new KeyedCodec<>("Max", Codec.INTEGER, false),
              (entry, value) -> entry.maxQuantity = value == null ? 1 : value,
              entry -> entry.maxQuantity)
          .add()
          .append(
              new KeyedCodec<>("Chance", Codec.DOUBLE, false),
              (entry, value) -> entry.chancePercent = value == null ? 100.0 : value,
              entry -> entry.chancePercent)
          .add()
          .build();

  String itemId = "";
  int minQuantity = 1;
  int maxQuantity = 1;
  double chancePercent = 100.0;

  public SpawnerLootEntry() {}

  public SpawnerLootEntry(String itemId, int minQuantity, int maxQuantity, double chancePercent) {
    this.itemId = itemId == null ? "" : itemId;
    this.minQuantity = minQuantity;
    this.maxQuantity = maxQuantity;
    this.chancePercent = chancePercent;
    normalize();
  }

  public String getItemId() {
    return itemId;
  }

  public int getMinQuantity() {
    return minQuantity;
  }

  public int getMaxQuantity() {
    return maxQuantity;
  }

  public double getChancePercent() {
    return chancePercent;
  }

  public void set(String itemId, int minimum, int maximum, double chance) {
    this.itemId = itemId == null ? "" : itemId.trim();
    this.minQuantity = minimum;
    this.maxQuantity = maximum;
    this.chancePercent = chance;
    normalize();
  }

  public void normalize() {
    itemId = itemId == null ? "" : itemId.trim();
    minQuantity = Math.max(1, Math.min(999, minQuantity));
    maxQuantity = Math.max(1, Math.min(999, maxQuantity));
    maxQuantity = Math.max(minQuantity, maxQuantity);
    chancePercent = Math.max(0.0, Math.min(100.0, chancePercent));
  }

  public SpawnerLootEntry copy() {
    return new SpawnerLootEntry(itemId, minQuantity, maxQuantity, chancePercent);
  }
}
