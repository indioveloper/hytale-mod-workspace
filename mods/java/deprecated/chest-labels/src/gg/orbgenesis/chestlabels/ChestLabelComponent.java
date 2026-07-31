package gg.orbgenesis.chestlabels;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class ChestLabelComponent implements Component<ChunkStore> {
  public static final BuilderCodec<ChestLabelComponent> CODEC =
      BuilderCodec.builder(ChestLabelComponent.class, ChestLabelComponent::new)
          .appendInherited(
              new KeyedCodec<>("Name", Codec.STRING),
              (component, value) -> component.name = value,
              component -> component.name,
              (component, parent) -> component.name = parent.name)
          .add()
          .appendInherited(
              new KeyedCodec<>("Icon", Codec.STRING),
              (component, value) -> component.icon = value,
              component -> component.icon,
              (component, parent) -> component.icon = parent.icon)
          .add()
          .build();

  private String name = "";
  private String icon = ChestIconRegistry.DEFAULT_ICON_KEY;

  public ChestLabelComponent() {}

  public ChestLabelComponent(ChestLabelComponent other) {
    name = other.name;
    icon = other.icon;
  }

  public String getName() {
    return name;
  }

  public String getIcon() {
    return icon;
  }

  public void set(String nextName, String nextIcon) {
    name = sanitizeName(nextName);
    icon = ChestIconRegistry.normalizeKey(nextIcon);
  }

  public boolean isEmpty() {
    return name == null || name.isBlank();
  }

  @Override
  public ChestLabelComponent clone() {
    return new ChestLabelComponent(this);
  }

  private static String sanitizeName(String raw) {
    if (raw == null) {
      return "";
    }

    String cleaned = raw.trim();
    if (cleaned.length() > 48) {
      cleaned = cleaned.substring(0, 48).trim();
    }
    return cleaned;
  }
}
