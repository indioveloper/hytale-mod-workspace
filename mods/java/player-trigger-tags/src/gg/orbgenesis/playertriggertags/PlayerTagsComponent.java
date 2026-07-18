package gg.orbgenesis.playertriggertags;

import com.hypixel.hytale.codec.KeyedCodec;
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
          .build();

  private Map<String, String> tags = new HashMap<>();

  public Map<String, String> getTags() {
    return tags;
  }

  @Override
  public PlayerTagsComponent clone() {
    PlayerTagsComponent copy = new PlayerTagsComponent();
    copy.tags.putAll(tags);
    return copy;
  }
}
