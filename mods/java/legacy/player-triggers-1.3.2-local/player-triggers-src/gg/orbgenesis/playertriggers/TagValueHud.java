package gg.orbgenesis.playertriggers;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class TagValueHud extends CustomUIHud {
  public static final String HUD_KEY = "OrbGenesis_TagValue";

  private String label;
  private String value;

  public TagValueHud(PlayerRef playerRef, String label, String value) {
    super(playerRef, HUD_KEY, 13);
    this.label = label;
    this.value = value;
  }

  public static TagValueHud getOrCreate(
      Player player, PlayerRef playerRef, String label) {
    CustomUIHud existing = player.getHudManager().getCustomHud(HUD_KEY);
    if (existing instanceof TagValueHud tagValueHud) {
      return tagValueHud;
    }

    TagValueHud hud = new TagValueHud(playerRef, label, "0");
    player.getHudManager().addCustomHud(playerRef, hud);
    return hud;
  }

  public static void remove(Player player, PlayerRef playerRef) {
    if (player != null && playerRef != null) {
      player.getHudManager().removeCustomHud(playerRef, HUD_KEY);
    }
  }

  public static void refreshIfWatching(
      TriggerContext context, PlayerTagsComponent component) {
    if (context == null || component == null) {
      return;
    }

    Store<EntityStore> store = context.getStore();
    Ref<EntityStore> ref = context.getEntityRef();
    if (store == null || ref == null || !ref.isValid()) {
      return;
    }

    refreshIfWatching(store, ref, component);
  }

  public static void refreshIfWatching(
      Store<EntityStore> store, Ref<EntityStore> ref, PlayerTagsComponent component) {
    if (store == null || ref == null || component == null) {
      return;
    }

    Player player = store.getComponent(ref, Player.getComponentType());
    PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
    if (player == null || playerRef == null) {
      return;
    }

    if (!component.isTagValueHudEnabled()) {
      remove(player, playerRef);
      return;
    }

    String tag = PlayerTagAccess.normalizeKey(component.getTagValueHudTag());
    if (tag.isEmpty()) {
      remove(player, playerRef);
      return;
    }

    getOrCreate(player, playerRef, component.getTagValueHudLabel())
        .updateValue(
            component.getTagValueHudLabel(),
            component.getTags().getOrDefault(tag, "0"));
  }

  public void updateValue(String nextLabel, String nextValue) {
    label = nextLabel == null || nextLabel.isBlank() ? "Tag" : nextLabel;
    value = nextValue == null || nextValue.isBlank() ? "0" : nextValue;
    UICommandBuilder commands = new UICommandBuilder();
    commands.set("#TagValueLabel.Text", label);
    commands.set("#TagValue.Text", value);
    update(false, commands);
  }

  @Override
  protected void build(UICommandBuilder commands) {
    commands.append("HUD/PlayerTriggersTagValue.ui");
    commands.set("#TagValueLabel.Text", label);
    commands.set("#TagValue.Text", value);
  }
}
