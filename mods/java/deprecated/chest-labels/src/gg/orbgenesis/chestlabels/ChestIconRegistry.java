package gg.orbgenesis.chestlabels;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

public final class ChestIconRegistry {
  public static final String DEFAULT_ICON_KEY = "loot";

  private static final Map<String, String> ICON_SELECTORS = new LinkedHashMap<>();

  static {
    ICON_SELECTORS.put("loot", "#IconLoot");
    ICON_SELECTORS.put("star", "#IconStar");
    ICON_SELECTORS.put("key", "#IconKey");
    ICON_SELECTORS.put("check", "#IconCheck");
    ICON_SELECTORS.put("warning", "#IconWarning");
  }

  private ChestIconRegistry() {}

  public static String normalizeKey(String raw) {
    if (raw == null || raw.isBlank()) {
      return DEFAULT_ICON_KEY;
    }

    String key = raw.trim().toLowerCase(Locale.ROOT);
    return ICON_SELECTORS.containsKey(key) ? key : DEFAULT_ICON_KEY;
  }

  public static String getSelector(String raw) {
    return ICON_SELECTORS.get(normalizeKey(raw));
  }

  public static Set<String> getKeys() {
    return ICON_SELECTORS.keySet();
  }

  public static String listIcons() {
    StringJoiner joiner = new StringJoiner(", ");
    for (String key : ICON_SELECTORS.keySet()) {
      joiner.add(key);
    }
    return joiner.toString();
  }
}
