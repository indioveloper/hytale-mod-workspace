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
          .build();

  private Map<String, String> tags = new HashMap<>();
  private boolean mobKillListenerEnabled;
  private String mobKillCounterTag = "mob_kills";
  private String mobKillPoints = "1";
  private String mobKillFilter = "";

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

  @Override
  public PlayerTagsComponent clone() {
    PlayerTagsComponent copy = new PlayerTagsComponent();
    copy.tags.putAll(tags);
    copy.mobKillListenerEnabled = mobKillListenerEnabled;
    copy.mobKillCounterTag = mobKillCounterTag;
    copy.mobKillPoints = mobKillPoints;
    copy.mobKillFilter = mobKillFilter;
    return copy;
  }
}
