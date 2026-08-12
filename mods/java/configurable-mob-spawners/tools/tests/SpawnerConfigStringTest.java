package gg.orbgenesis.configurablespawners;

public final class SpawnerConfigStringTest {
  public static void main(String[] args) {
    ConfigurableSpawnerComponent source = new ConfigurableSpawnerComponent();
    source.roleId = "Skeleton_Soldier";
    source.maxHealth = 42.5;
    source.mobScale = 1.7;
    source.horizontalRadius = 9.0;
    source.minLight = 1;
    source.maxLight = 3;
    source.customArmor = true;
    source.armorHeadId = "Armor_Iron_Head";
    source.lootMode = LootMode.REPLACE;
    source.lootEntries[0].set("Ingredient_Bone", 2, 5, 37.5);

    String encoded = SpawnerConfigString.encode(source);
    if (!encoded.startsWith("CMS1:")) throw new AssertionError("Missing CMS1 prefix");
    ConfigurableSpawnerComponent decoded = SpawnerConfigString.decode(encoded);
    require(decoded.roleId.equals(source.roleId), "role");
    require(decoded.maxHealth == source.maxHealth, "health");
    require(decoded.mobScale == source.mobScale, "scale");
    require(decoded.horizontalRadius == source.horizontalRadius, "radius");
    require(decoded.maxLight == 3, "light");
    require(decoded.minLight == 0, "legacy minimum light ignored");
    require(decoded.customArmor, "armor enabled");
    require(decoded.armorHeadId.equals(source.armorHeadId), "armor item");
    require(decoded.lootMode == LootMode.REPLACE, "loot mode");
    require(decoded.lootEntries[0].itemId.equals("Ingredient_Bone"), "loot item");
    require(decoded.lootEntries[0].minQuantity == 2, "loot min");
    require(decoded.lootEntries[0].maxQuantity == 5, "loot max");
    require(decoded.lootEntries[0].chancePercent == 37.5, "loot chance");

    boolean rejected = false;
    try {
      SpawnerConfigString.decode("CMS2:invalid");
    } catch (IllegalArgumentException expected) {
      rejected = true;
    }
    require(rejected, "invalid version rejection");
    System.out.println("Configurable Mob Spawners CMS1 round-trip checks passed.");
  }

  private static void require(boolean condition, String field) {
    if (!condition) throw new AssertionError("Round-trip failed for " + field);
  }
}
