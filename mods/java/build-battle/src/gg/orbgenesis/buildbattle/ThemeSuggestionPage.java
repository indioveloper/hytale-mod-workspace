package gg.orbgenesis.buildbattle;

import com.hypixel.hytale.builtin.triggervolumes.EntityTargetType;
import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.ModifyTagsEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.conditions.BlockTypeCondition;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.joml.Vector3d;
import org.joml.Vector3f;

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

    String themeLabel = normalizeThemeLabel(data.theme);
    String themeKey = themeLabel.toLowerCase(Locale.ROOT);
    if (!VALID_THEME.matcher(themeKey).matches()) {
      showValidationError();
      return;
    }

    TriggerVolumeManager manager =
        store.getResource(TriggerVolumesPlugin.get().getManagerResourceType());
    VolumeEntry sourceVolume =
        manager != null && volumeId != null ? manager.getVolume(volumeId) : null;
    if (manager == null || sourceVolume == null) {
      playerRef.sendMessage(
          Message.raw("Build Battle could not find the source Trigger Volume."));
      close();
      return;
    }

    String replacementVolumeId = createUniqueThemeVolumeId(manager, themeLabel);
    VolumeEntry replacement = createThemeListenerVolume(sourceVolume, replacementVolumeId, themeKey);

    manager.unregister(volumeId);
    manager.notifyViewersRemove(volumeId);
    manager.register(replacementVolumeId, replacement);
    manager.markSpatialDirty();
    manager.notifyViewersAdd(replacement);

    playerRef.sendMessage(
        Message.raw(
            "Build Battle created " + replacementVolumeId + "."));
    close();
  }

  private static String normalizeThemeLabel(String raw) {
    if (raw == null) {
      return "";
    }
    String normalized = Normalizer.normalize(raw, Normalizer.Form.NFKC);
    return normalized.trim();
  }

  private static String createUniqueThemeVolumeId(
      @Nonnull TriggerVolumeManager manager,
      @Nonnull String themeLabel) {
    String base = "Theme " + themeLabel;
    if (!manager.hasVolume(base)) {
      return base;
    }

    int suffix = 2;
    String candidate = base + " " + suffix;
    while (manager.hasVolume(candidate)) {
      suffix++;
      candidate = base + " " + suffix;
    }
    return candidate;
  }

  private static VolumeEntry createThemeListenerVolume(
      @Nonnull VolumeEntry sourceVolume,
      @Nonnull String volumeId,
      @Nonnull String themeKey) {
    VolumeEntry replacement =
        new VolumeEntry(
            volumeId,
            sourceVolume.getWorldName(),
            new Vector3d(sourceVolume.getPosition()),
            sourceVolume.getShape().copy(),
            new java.util.ArrayList<>(),
            new HashSet<>(sourceVolume.getTargetTypes()),
            sourceVolume.isEnabled());

    replacement.getConditions().add(createCondition("Cloth_Block_Wool_Black", 0));
    replacement.getConditions().add(createCondition("Cloth_Block_Wool_White", 1));
    replacement.getConditions().add(createCondition("Cloth_Block_Wool_Green", 2));
    replacement.getConditions().add(createCondition("Cloth_Block_Wool_Yellow", 3));
    replacement.getConditions().add(createCondition("Cloth_Block_Wool_Red", 4));
    replacement.getConditions().add(createCondition("Cloth_Block_Wool_Orange", 5));
    replacement.getConditions().add(createCondition("Cloth_Block_Wool_Pink", 6));
    replacement.getConditions().add(createCondition("Cloth_Block_Wool_Blue", 7));

    replacement.getEffects().add(createPointsEffect(themeKey, "101", 0));
    replacement.getEffects().add(createPointsEffect(themeKey, "102", 1));
    replacement.getEffects().add(createPointsEffect(themeKey, "103", 2));
    replacement.getEffects().add(createPointsEffect(themeKey, "104", 3));
    replacement.getEffects().add(createPointsEffect(themeKey, "105", 4));
    replacement.getEffects().add(createPointsEffect(themeKey, "106", 5));
    replacement.getEffects().add(createPointsEffect(themeKey, "107", 6));
    replacement.getEffects().add(createPointsEffect(themeKey, "108", 7));

    Map<String, String> tags = new LinkedHashMap<>();
    tags.put("theme_" + themeKey, "empty");
    tags.put("points", "0");
    replacement.setTags(tags);
    replacement.setKeepLoaded(sourceVolume.isKeepLoaded());
    replacement.setCooldown(sourceVolume.getCooldown());
    replacement.setCooldownMode(sourceVolume.getCooldownMode());
    replacement.setActivationDelay(sourceVolume.getActivationDelay());
    replacement.setCancelDelayedEffectsOnExit(sourceVolume.isCancelDelayedEffectsOnExit());
    replacement.setProjectileSource(sourceVolume.getProjectileSource());
    replacement.setConditionTiming(sourceVolume.getConditionTiming());
    replacement.setRejectionDelayMode(sourceVolume.getRejectionDelayMode());
    replacement.setColor(sourceVolume.getColor() != null ? new Vector3f(sourceVolume.getColor()) : null);
    replacement.setGroupId(sourceVolume.getGroupId());
    return replacement;
  }

  private static BlockTypeCondition createCondition(
      @Nonnull String blockType,
      int entry) {
    BlockTypeCondition condition =
        BlockTypeCondition.create(TriggerEventType.BLOCK_PLACED, blockType);
    if (entry > 0) {
      condition.setEntry(entry);
    }
    return condition;
  }

  private static ModifyTagsEffect createPointsEffect(
      @Nonnull String themeKey,
      @Nonnull String tagValue,
      int entry) {
    ModifyTagsEffect effect =
        ModifyTagsEffect.increment(TriggerEventType.BLOCK_PLACED, "points", tagValue)
            .withMatchTag("theme_" + themeKey, "");
    if (entry > 0) {
      effect.setEntry(entry);
    }
    return effect;
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
