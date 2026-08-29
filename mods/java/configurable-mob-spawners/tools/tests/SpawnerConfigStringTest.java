package gg.orbgenesis.configurablespawners;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class SpawnerConfigStringTest {
  public static void main(String[] args) {
    ConfigurableSpawnerComponent fresh = new ConfigurableSpawnerComponent();
    require(fresh.roleId.isBlank(), "new blocks start without a role");
    require(fresh.enabled, "legacy enabled field remains normalized on");

    ConfigurableSpawnerComponent source = new ConfigurableSpawnerComponent();
    source.roleId = "Skeleton_Soldier";
    source.mobName = "Guardián del puente";
    source.tags = new String[] {"arena", "wave.one"};
    source.maxHealth = 42.5;
    source.mobScale = 1.7;
    source.mobSpeed = 2.4;
    source.horizontalRadius = 9.0;
    source.minLight = 1;
    source.maxLight = 3;
    source.customArmor = true;
    source.armorHeadId = "Armor_Iron_Head";
    source.lootMode = LootMode.REPLACE;
    source.lootEntries[0].set("Ingredient_Bone", 2, 5, 37.5);
    source.spawnCountMin = 5;
    source.spawnCountMax = 3;
    source.updateFirstProfileFromLegacy();
    SpawnerMobProfile second = new SpawnerMobProfile();
    second.roleId = "Trork_Warrior";
    second.weight = 3.0;
    second.eliteEnabled = true;
    second.eliteChancePercent = 25.0;
    second.elitePrefix = "Veterano";
    second.eliteHealthMultiplier = 2.5;
    second.eliteScaleMultiplier = 1.4;
    second.eliteSpeedMultiplier = 1.2;
    second.eliteOverrideEquipment = true;
    second.eliteHeldItemId = "Weapon_Club_Adamantite";
    second.eliteLootEntries[0].set("Ingredient_Bone", 4, 8, 50.0);
    source.mobProfiles = new SpawnerMobProfile[] {source.mobProfiles[0], second};

    String encoded = SpawnerConfigString.encode(source);
    if (!encoded.startsWith("CMS1:")) throw new AssertionError("Missing CMS1 prefix");
    ConfigurableSpawnerComponent decoded = SpawnerConfigString.decode(encoded);
    require(decoded.roleId.equals(source.roleId), "role");
    require(decoded.mobName.equals(source.mobName), "mob name");
    require(decoded.tags.length == 2 && decoded.tags[1].equals("wave.one"), "tags");
    require(decoded.maxHealth == source.maxHealth, "health");
    require(decoded.mobScale == source.mobScale, "scale");
    require(decoded.mobSpeed == source.mobSpeed, "speed");
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
    require(decoded.spawnCountMin == 5 && decoded.spawnCountMax == 5, "wave count ordering");
    require(decoded.mobProfiles.length == 2, "profile count");
    require(decoded.mobProfiles[1].roleId.equals("Trork_Warrior"), "second profile role");
    require(decoded.mobProfiles[1].weight == 3.0, "second profile weight");
    require(decoded.mobProfiles[1].eliteEnabled, "elite enabled");
    require(decoded.mobProfiles[1].eliteChancePercent == 25.0, "elite chance");
    require(decoded.mobProfiles[1].elitePrefix.equals("Veterano"), "elite prefix");
    require(decoded.mobProfiles[1].eliteOverrideEquipment, "elite equipment override");
    require(decoded.mobProfiles[1].eliteLootEntries[0].minQuantity == 4, "elite loot");
    require(decoded.selectProfile(0.10).roleId.equals("Skeleton_Soldier"), "weighted first range");
    require(decoded.selectProfile(0.50).roleId.equals("Trork_Warrior"), "weighted second range");

    ConfigurableSpawnerComponent fourMobSource = new ConfigurableSpawnerComponent();
    fourMobSource.mobProfiles = new SpawnerMobProfile[] {
        profile("Zombie_Burnt"), profile("Cactee"), profile("Cow_Calf"), profile("Snake_Cobra")
    };
    fourMobSource.normalize();
    ConfigurableSpawnerComponent fourMobDecoded = SpawnerConfigString.decode(
        SpawnerConfigString.encode(fourMobSource));
    require(fourMobDecoded.mobProfiles.length == 4, "four-profile CMS1 count");
    require(fourMobDecoded.selectProfile(0.10).roleId.equals("Zombie_Burnt"),
        "four-profile first range");
    require(fourMobDecoded.selectProfile(0.30).roleId.equals("Cactee"),
        "four-profile second range");
    require(fourMobDecoded.selectProfile(0.55).roleId.equals("Cow_Calf"),
        "four-profile third range");
    require(fourMobDecoded.selectProfile(0.90).roleId.equals("Snake_Cobra"),
        "four-profile fourth range");

    String malformedLootJson = "{\"v\":1,\"speed\":9,\"lootMode\":\"REPLACE\",\"loot\":["
        + "{\"item\":\"Weapon_Club_Adamantite\",\"min\":12,\"max\":1,\"chance\":100}]}";
    String malformedLoot = SpawnerConfigString.PREFIX + Base64.getUrlEncoder().withoutPadding()
        .encodeToString(malformedLootJson.getBytes(StandardCharsets.UTF_8));
    ConfigurableSpawnerComponent normalizedLoot = SpawnerConfigString.decode(malformedLoot);
    require(normalizedLoot.mobSpeed == 3.0, "speed maximum clamp");
    require(normalizedLoot.lootEntries[0].minQuantity == 12, "imported loot min retained");
    require(normalizedLoot.lootEntries[0].maxQuantity == 12, "imported loot max ordering");

    String legacyJson = "{\"v\":1,\"role\":\"Skeleton\",\"enabled\":false}";
    String legacy = SpawnerConfigString.PREFIX + Base64.getUrlEncoder().withoutPadding()
        .encodeToString(legacyJson.getBytes(StandardCharsets.UTF_8));
    ConfigurableSpawnerComponent legacyConfig = SpawnerConfigString.decode(legacy);
    require(legacyConfig.mobSpeed == 1.0, "legacy speed default");
    require(legacyConfig.enabled, "legacy manual disable is ignored");
    require(legacyConfig.mobProfiles.length == 1
        && legacyConfig.mobProfiles[0].roleId.equals("Skeleton"), "legacy profile migration");
    require(!SpawnerConfigString.encode(legacyConfig).contains("enabled"), "enabled omitted from CMS1");

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

  private static SpawnerMobProfile profile(String roleId) {
    SpawnerMobProfile profile = new SpawnerMobProfile();
    profile.roleId = roleId;
    profile.weight = 1.0;
    return profile;
  }
}
