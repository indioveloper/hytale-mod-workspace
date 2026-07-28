package gg.orbgenesis.buildbattle;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

public class ThemeSuggestionPage
    extends InteractiveCustomUIPage<ThemeSuggestionPage.ThemeSuggestionEventData> {
  private static final Pattern VALID_THEME =
      Pattern.compile("[\\p{L}\\p{N}][\\p{L}\\p{N}_-]{0,31}");

  private final String volumeId;

  public ThemeSuggestionPage(PlayerRef playerRef, String volumeId) {
    super(playerRef, CustomPageLifetime.CanDismiss, ThemeSuggestionEventData.CODEC);
    this.volumeId = volumeId;
  }

  @Override
  public void build(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull UICommandBuilder commandBuilder,
      @Nonnull UIEventBuilder eventBuilder,
      @Nonnull Store<EntityStore> store) {
    commandBuilder.append("Pages/ThemeSuggestionPage.ui");

    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#SuggestButton",
        new EventData()
            .append(ThemeSuggestionEventData.KEY_ACTION, ThemeSuggestionEventData.ACTION_SUGGEST)
            .append(ThemeSuggestionEventData.KEY_THEME, "#ThemeInput.Value"),
        false);
    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#CancelButton",
        EventData.of(
            ThemeSuggestionEventData.KEY_ACTION,
            ThemeSuggestionEventData.ACTION_CANCEL),
        false);
  }

  @Override
  public void handleDataEvent(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Store<EntityStore> store,
      @Nonnull ThemeSuggestionEventData data) {
    if (ThemeSuggestionEventData.ACTION_CANCEL.equals(data.action)) {
      close();
      return;
    }

    if (!ThemeSuggestionEventData.ACTION_SUGGEST.equals(data.action)) {
      return;
    }

    String theme = normalizeTheme(data.theme);
    if (!VALID_THEME.matcher(theme).matches()) {
      showValidationError();
      return;
    }

    TriggerVolumeManager manager =
        store.getResource(TriggerVolumesPlugin.get().getManagerResourceType());
    if (manager == null || volumeId == null || manager.getVolume(volumeId) == null) {
      playerRef.sendMessage(
          Message.raw("Build Battle could not find the source Trigger Volume."));
      close();
      return;
    }

    manager.setTag(
        volumeId,
        "theme_" + theme,
        "empty",
        ref,
        playerRef.getUuid());
    manager.setTag(
        volumeId,
        "points",
        "0",
        ref,
        playerRef.getUuid());

    playerRef.sendMessage(
        Message.raw(
            "Build Battle volume: theme_" + theme + "=empty, points=0"));
    close();
  }

  private static String normalizeTheme(String raw) {
    if (raw == null) {
      return "";
    }
    String normalized = Normalizer.normalize(raw, Normalizer.Form.NFKC);
    return normalized.trim().toLowerCase(Locale.ROOT);
  }

  private void showValidationError() {
    UICommandBuilder commands = new UICommandBuilder();
    commands.set("#ErrorMessage.Visible", true);
    sendUpdate(commands, null, false);
  }

  public static final class ThemeSuggestionEventData {
    static final String KEY_ACTION = "Action";
    static final String KEY_THEME = "@Theme";

    static final String ACTION_SUGGEST = "Suggest";
    static final String ACTION_CANCEL = "Cancel";

    static final BuilderCodec<ThemeSuggestionEventData> CODEC =
        BuilderCodec.builder(
                ThemeSuggestionEventData.class,
                ThemeSuggestionEventData::new)
            .append(
                new KeyedCodec<>(KEY_ACTION, Codec.STRING),
                (entry, value) -> entry.action = value,
                entry -> entry.action)
            .add()
            .append(
                new KeyedCodec<>(KEY_THEME, Codec.STRING),
                (entry, value) -> entry.theme = value,
                entry -> entry.theme)
            .add()
            .build();

    private String action;
    private String theme;
  }
}
