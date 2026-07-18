package gg.orbgenesis.playertriggers;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashMap;
import java.util.Map;

public class PlayerTagsComponent implements Component<EntityStore> {
  public static final BuilderCodec<PlayerTagsComponent> CODEC =
      BuilderCodec.builder(PlayerTagsComponent.class, PlayerTagsComponent::new)
          .append(
              new KeyedCodec<>("Tags", MapCodec.STRING_HASH_MAP_CODEC, false),
              (component, value) -> component.tags = new HashMap<>(value),
              component -> component.tags)
          .add()
          .append(
              new KeyedCodec<>("MobKillListenerEnabled", Codec.BOOLEAN, false),
              (component, value) -> component.mobKillListenerEnabled = value,
              component -> component.mobKillListenerEnabled)
          .add()
          .append(
              new KeyedCodec<>("MobKillCounterTag", Codec.STRING, false),
              (component, value) -> component.mobKillCounterTag = value,
              component -> component.mobKillCounterTag)
          .add()
          .append(
              new KeyedCodec<>("MobKillPoints", Codec.STRING, false),
              (component, value) -> component.mobKillPoints = value,
              component -> component.mobKillPoints)
          .add()
          .append(
              new KeyedCodec<>("MobKillFilter", Codec.STRING, false),
              (component, value) -> component.mobKillFilter = value,
              component -> component.mobKillFilter)
          .add()
          .append(
              new KeyedCodec<>("PlayerCountHudEnabled", Codec.BOOLEAN, false),
              (component, value) -> component.playerCountHudEnabled = value,
              component -> component.playerCountHudEnabled)
          .add()
          .append(
              new KeyedCodec<>("KillCounterHudEnabled", Codec.BOOLEAN, false),
              (component, value) -> component.killCounterHudEnabled = value,
              component -> component.killCounterHudEnabled)
          .add()
          .append(
              new KeyedCodec<>("TagValueHudEnabled", Codec.BOOLEAN, false),
              (component, value) -> component.tagValueHudEnabled = value,
              component -> component.tagValueHudEnabled)
          .add()
          .append(
              new KeyedCodec<>("TagValueHudTag", Codec.STRING, false),
              (component, value) -> component.tagValueHudTag = value,
              component -> component.tagValueHudTag)
          .add()
          .append(
              new KeyedCodec<>("TagValueHudLabel", Codec.STRING, false),
              (component, value) -> component.tagValueHudLabel = value,
              component -> component.tagValueHudLabel)
          .add()
          .build();

  private Map<String, String> tags = new HashMap<>();
  private boolean mobKillListenerEnabled;
  private String mobKillCounterTag = "mob_kills";
  private String mobKillPoints = "1";
  private String mobKillFilter = "";
  private boolean playerCountHudEnabled;
  private boolean killCounterHudEnabled = true;
  private boolean tagValueHudEnabled;
  private String tagValueHudTag = "";
  private String tagValueHudLabel = "Tag";

  public Map<String, String> getTags() {
    return tags;
  }

  public boolean isMobKillListenerEnabled() {
    return mobKillListenerEnabled;
  }

  public void configureMobKillListener(
      boolean enabled, String counterTag, String points, String filter) {
    mobKillListenerEnabled = enabled;
    mobKillCounterTag = PlayerTagAccess.normalizeKey(counterTag);
    mobKillPoints = points == null ? "1" : points.trim();
    mobKillFilter = filter == null ? "" : filter.trim();
  }

  public String getMobKillCounterTag() {
    return mobKillCounterTag;
  }

  public String getMobKillPoints() {
    return mobKillPoints;
  }

  public String getMobKillFilter() {
    return mobKillFilter;
  }

  public boolean isPlayerCountHudEnabled() {
    return playerCountHudEnabled;
  }

  public void setPlayerCountHudEnabled(boolean enabled) {
    playerCountHudEnabled = enabled;
  }

  public boolean isKillCounterHudEnabled() {
    return killCounterHudEnabled;
  }

  public void setKillCounterHudEnabled(boolean enabled) {
    killCounterHudEnabled = enabled;
  }

  public boolean isTagValueHudEnabled() {
    return tagValueHudEnabled;
  }

  public String getTagValueHudTag() {
    return tagValueHudTag;
  }

  public String getTagValueHudLabel() {
    return tagValueHudLabel == null || tagValueHudLabel.isBlank()
        ? tagValueHudTag
        : tagValueHudLabel;
  }

  public void configureTagValueHud(boolean enabled, String tag, String label) {
    tagValueHudEnabled = enabled;
    tagValueHudTag = PlayerTagAccess.normalizeKey(tag);
    tagValueHudLabel = label == null || label.isBlank() ? tagValueHudTag : label;
  }

  @Override
  public PlayerTagsComponent clone() {
    PlayerTagsComponent copy = new PlayerTagsComponent();
    copy.tags.putAll(tags);
    copy.mobKillListenerEnabled = mobKillListenerEnabled;
    copy.mobKillCounterTag = mobKillCounterTag;
    copy.mobKillPoints = mobKillPoints;
    copy.mobKillFilter = mobKillFilter;
    copy.playerCountHudEnabled = playerCountHudEnabled;
    copy.killCounterHudEnabled = killCounterHudEnabled;
    copy.tagValueHudEnabled = tagValueHudEnabled;
    copy.tagValueHudTag = tagValueHudTag;
    copy.tagValueHudLabel = tagValueHudLabel;
    return copy;
  }
}
