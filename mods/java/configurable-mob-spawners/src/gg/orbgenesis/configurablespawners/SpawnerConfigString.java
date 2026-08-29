package gg.orbgenesis.configurablespawners;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

final class SpawnerConfigString {
  static final String PREFIX = "CMS1:";
  // Compound spawners can carry several equipment and loot tables in one portable string.
  private static final int MAX_ENCODED_LENGTH = 65536;

  private SpawnerConfigString() {}

  static String encode(ConfigurableSpawnerComponent config) {
    config.normalize();
    JsonObject root = new JsonObject();
    root.addProperty("v", 1);
    root.addProperty("tag", config.tag);
    JsonArray tags = new JsonArray();
    for (String tag : config.tags) tags.add(tag);
    root.add("tags", tags);
    root.addProperty("role", config.roleId);
    root.addProperty("name", config.mobName);
    root.addProperty("health", config.maxHealth);
    root.addProperty("scale", config.mobScale);
    root.addProperty("speed", config.mobSpeed);
    root.addProperty("held", config.heldItemId);
    root.addProperty("aggression", config.aggressionMode.name());
    root.addProperty("cadenceMin", config.cadenceMinSeconds);
    root.addProperty("cadenceMax", config.cadenceMaxSeconds);
    root.addProperty("countMin", config.spawnCountMin);
    root.addProperty("countMax", config.spawnCountMax);
    root.addProperty("maxAlive", config.maxAlive);
    root.addProperty("activation", config.activationRadius);
    root.addProperty("radius", config.horizontalRadius);
    root.addProperty("lightMax", config.maxLight);
    root.addProperty("lootMode", config.lootMode.name());

    JsonObject armor = new JsonObject();
    armor.addProperty("enabled", config.customArmor);
    armor.addProperty("head", config.armorHeadId);
    armor.addProperty("chest", config.armorChestId);
    armor.addProperty("hands", config.armorHandsId);
    armor.addProperty("legs", config.armorLegsId);
    root.add("armor", armor);

    JsonArray loot = new JsonArray();
    for (SpawnerLootEntry entry : config.lootEntries) {
      JsonObject item = new JsonObject();
      item.addProperty("item", entry.itemId);
      item.addProperty("min", entry.minQuantity);
      item.addProperty("max", entry.maxQuantity);
      item.addProperty("chance", entry.chancePercent);
      loot.add(item);
    }
    root.add("loot", loot);
    JsonArray mobs = new JsonArray();
    for (SpawnerMobProfile profile : config.mobProfiles) mobs.add(encodeProfile(profile));
    root.add("mobs", mobs);
    return PREFIX + Base64.getUrlEncoder().withoutPadding()
        .encodeToString(root.toString().getBytes(StandardCharsets.UTF_8));
  }

  static ConfigurableSpawnerComponent decode(String encoded) {
    if (encoded == null) throw new IllegalArgumentException("Missing configuration");
    String value = encoded.trim();
    if (!value.startsWith(PREFIX) || value.length() > MAX_ENCODED_LENGTH) {
      throw new IllegalArgumentException("Unsupported configuration string");
    }
    try {
      String json = new String(
          Base64.getUrlDecoder().decode(value.substring(PREFIX.length())), StandardCharsets.UTF_8);
      JsonObject root = JsonParser.parseString(json).getAsJsonObject();
      if (integer(root, "v", 0) != 1) throw new IllegalArgumentException("Unsupported version");
      ConfigurableSpawnerComponent result = new ConfigurableSpawnerComponent();
      // CMS1 strings created before 0.4.6 may contain "enabled". It is intentionally ignored:
      // spawning is controlled by player proximity, light and the local living-mob limit.
      result.enabled = true;
      result.tag = string(root, "tag", "");
      if (root.has("tags") && root.get("tags").isJsonArray()) {
        JsonArray tags = root.getAsJsonArray("tags");
        result.tags = new String[tags.size()];
        for (int i = 0; i < tags.size(); i++) result.tags[i] = tags.get(i).getAsString();
      }
      result.roleId = string(root, "role", "Skeleton");
      result.mobName = string(root, "name", "");
      result.maxHealth = number(root, "health", 0.0);
      result.mobScale = number(root, "scale", 1.0);
      result.mobSpeed = number(root, "speed", 1.0);
      result.heldItemId = string(root, "held", "");
      result.aggressionMode = AggressionMode.valueOf(string(root, "aggression", "ROLE_DEFAULT"));
      result.cadenceMinSeconds = number(root, "cadenceMin", 5.0);
      result.cadenceMaxSeconds = number(root, "cadenceMax", 10.0);
      result.spawnCountMin = integer(root, "countMin", 1);
      result.spawnCountMax = integer(root, "countMax", 3);
      result.maxAlive = integer(root, "maxAlive", 6);
      result.activationRadius = number(root, "activation", 16.0);
      result.horizontalRadius = number(root, "radius", 4.0);
      result.minLight = 0;
      result.maxLight = integer(root, "lightMax", 15);
      result.lootMode = LootMode.valueOf(string(root, "lootMode", "DEFAULT"));
      if (root.has("armor") && root.get("armor").isJsonObject()) {
        JsonObject armor = root.getAsJsonObject("armor");
        result.customArmor = bool(armor, "enabled", false);
        result.armorHeadId = string(armor, "head", "");
        result.armorChestId = string(armor, "chest", "");
        result.armorHandsId = string(armor, "hands", "");
        result.armorLegsId = string(armor, "legs", "");
      }
      if (root.has("loot") && root.get("loot").isJsonArray()) {
        JsonArray loot = root.getAsJsonArray("loot");
        for (int i = 0; i < Math.min(loot.size(), SpawnerLootEntry.MAX_ENTRIES); i++) {
          JsonObject item = loot.get(i).getAsJsonObject();
          result.lootEntries[i].set(
              string(item, "item", ""), integer(item, "min", 1), integer(item, "max", 1),
              number(item, "chance", 100.0));
        }
      }
      if (root.has("mobs") && root.get("mobs").isJsonArray()) {
        JsonArray mobs = root.getAsJsonArray("mobs");
        int length = Math.min(mobs.size(), SpawnerMobProfile.MAX_PROFILES);
        result.mobProfiles = new SpawnerMobProfile[length];
        for (int i = 0; i < length; i++) {
          result.mobProfiles[i] = decodeProfile(mobs.get(i).getAsJsonObject());
        }
      }
      result.normalize();
      return result;
    } catch (RuntimeException exception) {
      throw new IllegalArgumentException("Invalid configuration string", exception);
    }
  }

  private static JsonObject encodeProfile(SpawnerMobProfile profile) {
    profile.normalize();
    JsonObject object = new JsonObject();
    object.addProperty("role", profile.roleId);
    object.addProperty("name", profile.mobName);
    object.addProperty("weight", profile.weight);
    object.addProperty("health", profile.maxHealth);
    object.addProperty("scale", profile.mobScale);
    object.addProperty("speed", profile.mobSpeed);
    object.addProperty("held", profile.heldItemId);
    object.addProperty("aggression", profile.aggressionMode.name());
    object.addProperty("lootMode", profile.lootMode.name());
    object.add("armor", encodeArmor(profile.customArmor, profile.armorHeadId,
        profile.armorChestId, profile.armorHandsId, profile.armorLegsId));
    object.add("loot", encodeLoot(profile.lootEntries));
    JsonObject elite = new JsonObject();
    elite.addProperty("enabled", profile.eliteEnabled);
    elite.addProperty("chance", profile.eliteChancePercent);
    elite.addProperty("prefix", profile.elitePrefix);
    elite.addProperty("healthMultiplier", profile.eliteHealthMultiplier);
    elite.addProperty("scaleMultiplier", profile.eliteScaleMultiplier);
    elite.addProperty("speedMultiplier", profile.eliteSpeedMultiplier);
    elite.addProperty("overrideEquipment", profile.eliteOverrideEquipment);
    elite.addProperty("held", profile.eliteHeldItemId);
    elite.add("armor", encodeArmor(profile.eliteOverrideEquipment,
        profile.eliteArmorHeadId, profile.eliteArmorChestId,
        profile.eliteArmorHandsId, profile.eliteArmorLegsId));
    elite.add("loot", encodeLoot(profile.eliteLootEntries));
    object.add("elite", elite);
    return object;
  }

  private static SpawnerMobProfile decodeProfile(JsonObject object) {
    SpawnerMobProfile profile = new SpawnerMobProfile();
    profile.roleId = string(object, "role", "");
    profile.mobName = string(object, "name", "");
    profile.weight = number(object, "weight", 1.0);
    profile.maxHealth = number(object, "health", 0.0);
    profile.mobScale = number(object, "scale", 1.0);
    profile.mobSpeed = number(object, "speed", 1.0);
    profile.heldItemId = string(object, "held", "");
    profile.aggressionMode = AggressionMode.valueOf(string(object, "aggression", "ROLE_DEFAULT"));
    profile.lootMode = LootMode.valueOf(string(object, "lootMode", "DEFAULT"));
    if (object.has("armor") && object.get("armor").isJsonObject()) {
      JsonObject armor = object.getAsJsonObject("armor");
      profile.customArmor = bool(armor, "enabled", false);
      profile.armorHeadId = string(armor, "head", "");
      profile.armorChestId = string(armor, "chest", "");
      profile.armorHandsId = string(armor, "hands", "");
      profile.armorLegsId = string(armor, "legs", "");
    }
    profile.lootEntries = decodeLoot(object.get("loot"));
    if (object.has("elite") && object.get("elite").isJsonObject()) {
      JsonObject elite = object.getAsJsonObject("elite");
      profile.eliteEnabled = bool(elite, "enabled", false);
      profile.eliteChancePercent = number(elite, "chance", 0.0);
      profile.elitePrefix = string(elite, "prefix", "Élite");
      profile.eliteHealthMultiplier = number(elite, "healthMultiplier", 2.0);
      profile.eliteScaleMultiplier = number(elite, "scaleMultiplier", 1.25);
      profile.eliteSpeedMultiplier = number(elite, "speedMultiplier", 1.15);
      profile.eliteOverrideEquipment = bool(elite, "overrideEquipment", false);
      profile.eliteHeldItemId = string(elite, "held", "");
      if (elite.has("armor") && elite.get("armor").isJsonObject()) {
        JsonObject armor = elite.getAsJsonObject("armor");
        profile.eliteArmorHeadId = string(armor, "head", "");
        profile.eliteArmorChestId = string(armor, "chest", "");
        profile.eliteArmorHandsId = string(armor, "hands", "");
        profile.eliteArmorLegsId = string(armor, "legs", "");
      }
      profile.eliteLootEntries = decodeLoot(elite.get("loot"));
    }
    profile.normalize();
    return profile;
  }

  private static JsonObject encodeArmor(
      boolean enabled, String head, String chest, String hands, String legs) {
    JsonObject armor = new JsonObject();
    armor.addProperty("enabled", enabled);
    armor.addProperty("head", head);
    armor.addProperty("chest", chest);
    armor.addProperty("hands", hands);
    armor.addProperty("legs", legs);
    return armor;
  }

  private static JsonArray encodeLoot(SpawnerLootEntry[] entries) {
    JsonArray loot = new JsonArray();
    for (SpawnerLootEntry entry : entries) {
      JsonObject item = new JsonObject();
      item.addProperty("item", entry.itemId);
      item.addProperty("min", entry.minQuantity);
      item.addProperty("max", entry.maxQuantity);
      item.addProperty("chance", entry.chancePercent);
      loot.add(item);
    }
    return loot;
  }

  private static SpawnerLootEntry[] decodeLoot(com.google.gson.JsonElement element) {
    SpawnerLootEntry[] entries = SpawnerMobProfile.copyLoot(null);
    if (element == null || !element.isJsonArray()) return entries;
    JsonArray loot = element.getAsJsonArray();
    for (int i = 0; i < Math.min(loot.size(), entries.length); i++) {
      JsonObject item = loot.get(i).getAsJsonObject();
      entries[i].set(string(item, "item", ""), integer(item, "min", 1),
          integer(item, "max", 1), number(item, "chance", 100.0));
    }
    return entries;
  }

  private static String string(JsonObject object, String key, String fallback) {
    return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : fallback;
  }

  private static boolean bool(JsonObject object, String key, boolean fallback) {
    return object.has(key) ? object.get(key).getAsBoolean() : fallback;
  }

  private static int integer(JsonObject object, String key, int fallback) {
    return object.has(key) ? object.get(key).getAsInt() : fallback;
  }

  private static double number(JsonObject object, String key, double fallback) {
    return object.has(key) ? object.get(key).getAsDouble() : fallback;
  }
}
