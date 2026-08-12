package gg.orbgenesis.configurablespawners;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

final class SpawnerConfigString {
  static final String PREFIX = "CMS1:";
  private static final int MAX_ENCODED_LENGTH = 16384;

  private SpawnerConfigString() {}

  static String encode(ConfigurableSpawnerComponent config) {
    config.normalize();
    JsonObject root = new JsonObject();
    root.addProperty("v", 1);
    root.addProperty("enabled", config.enabled);
    root.addProperty("tag", config.tag);
    root.addProperty("role", config.roleId);
    root.addProperty("health", config.maxHealth);
    root.addProperty("scale", config.mobScale);
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
      result.enabled = bool(root, "enabled", true);
      result.tag = string(root, "tag", "");
      result.roleId = string(root, "role", "Skeleton");
      result.maxHealth = number(root, "health", 0.0);
      result.mobScale = number(root, "scale", 1.0);
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
      result.normalize();
      return result;
    } catch (RuntimeException exception) {
      throw new IllegalArgumentException("Invalid configuration string", exception);
    }
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
