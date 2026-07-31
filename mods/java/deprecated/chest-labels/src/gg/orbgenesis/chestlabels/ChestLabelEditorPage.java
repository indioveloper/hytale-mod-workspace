package gg.orbgenesis.chestlabels;

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
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import javax.annotation.Nonnull;
import org.joml.Vector3i;

public class ChestLabelEditorPage
    extends InteractiveCustomUIPage<ChestLabelEditorPage.ChestLabelEditorPageEventData> {
  private static final Map<String, String> ICON_LABELS =
      Map.of(
          "loot", "Loot",
          "star", "Star",
          "key", "Key",
          "check", "Check",
          "warning", "Warning");

  private final ChestLabelsPlugin plugin;
  private final Vector3i blockPos;
  private String draftName;
  private String selectedIconKey;

  public ChestLabelEditorPage(
      ChestLabelsPlugin plugin, PlayerRef playerRef, Vector3i blockPos, String initialName, String initialIconKey) {
    super(playerRef, CustomPageLifetime.CanDismiss, ChestLabelEditorPageEventData.CODEC);
    this.plugin = plugin;
    this.blockPos = new Vector3i(blockPos);
    // Older builds accidentally saved the selector expression as chest text.
    this.draftName = "#NameInput.Value".equals(initialName) ? "" : (initialName == null ? "" : initialName);
    this.selectedIconKey = ChestIconRegistry.normalizeKey(initialIconKey);
  }

  @Override
  public void build(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull UICommandBuilder commandBuilder,
      @Nonnull UIEventBuilder eventBuilder,
      @Nonnull Store<EntityStore> store) {
    commandBuilder.append("Pages/ChestLabelEditorPage.ui");
    commandBuilder.set("#NameInput.Value", draftName);
    commandBuilder.set("#SelectedIconValue.Text", getSelectedIconLabel());

    eventBuilder.addEventBinding(
        CustomUIEventBindingType.ValueChanged,
        "#NameInput",
        EventData.of(ChestLabelEditorPageEventData.KEY_NAME, "#NameInput.Value"),
        false);

    bindIconButton(eventBuilder, "#LootButton", "loot");
    bindIconButton(eventBuilder, "#StarButton", "star");
    bindIconButton(eventBuilder, "#KeyButton", "key");
    bindIconButton(eventBuilder, "#CheckButton", "check");
    bindIconButton(eventBuilder, "#WarningButton", "warning");

    // Keep the save binding in the same event format as the working icon buttons.
    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#SaveButton",
        new EventData().append(ChestLabelEditorPageEventData.KEY_ACTION, ChestLabelEditorPageEventData.ACTION_SAVE),
        false);
    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#ClearButton",
        new EventData().append(ChestLabelEditorPageEventData.KEY_ACTION, ChestLabelEditorPageEventData.ACTION_CLEAR),
        false);
    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#CloseButton",
        EventData.of(
            ChestLabelEditorPageEventData.KEY_ACTION, ChestLabelEditorPageEventData.ACTION_CLOSE),
        false);
  }

  @Override
  public void handleDataEvent(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Store<EntityStore> store,
      @Nonnull ChestLabelEditorPageEventData data) {
    if (data.name != null) {
      draftName = data.name;
    }

    if (ChestLabelEditorPageEventData.ACTION_SELECT_ICON.equals(data.action) && data.icon != null) {
      selectedIconKey = ChestIconRegistry.normalizeKey(data.icon);
      UICommandBuilder commands = new UICommandBuilder();
      commands.set("#SelectedIconValue.Text", getSelectedIconLabel());
      sendUpdate(commands, null, false);
      return;
    }

    if (ChestLabelEditorPageEventData.ACTION_CLOSE.equals(data.action)) {
      close();
      return;
    }

    World world = store.getExternalData().getWorld();
    Ref<ChunkStore> containerRef =
        ChestLabelTargeting.getContainerRefAt(world, blockPos.x, blockPos.y, blockPos.z);
    if (containerRef == null) {
      playerRef.sendMessage(Message.raw("That chest is no longer available."));
      sendUpdate(null, null, true);
      return;
    }

    if (ChestLabelEditorPageEventData.ACTION_CLEAR.equals(data.action)) {
      containerRef.getStore().tryRemoveComponent(containerRef, plugin.getChestLabelComponentType());
      draftName = "";
      selectedIconKey = ChestIconRegistry.DEFAULT_ICON_KEY;

      UICommandBuilder commands = new UICommandBuilder();
      commands.set("#NameInput.Value", "");
      commands.set("#SelectedIconValue.Text", getSelectedIconLabel());
      sendUpdate(commands, null, false);
      playerRef.sendMessage(Message.raw("Chest label cleared."));
      return;
    }

    if (ChestLabelEditorPageEventData.ACTION_SAVE.equals(data.action)) {
      ChestLabelComponent component =
          containerRef.getStore().ensureAndGetComponent(containerRef, plugin.getChestLabelComponentType());
      component.set(draftName, selectedIconKey);
      draftName = component.getName();
      selectedIconKey = component.getIcon();

      playerRef.sendMessage(Message.raw("Chest label updated."));
      close();
    }
  }

  public void clearPage() {
    sendUpdate(null, null, true);
  }

  private void bindIconButton(UIEventBuilder eventBuilder, String selector, String iconKey) {
    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        selector,
        new EventData()
            .append(ChestLabelEditorPageEventData.KEY_ACTION, ChestLabelEditorPageEventData.ACTION_SELECT_ICON)
            .append(ChestLabelEditorPageEventData.KEY_ICON, iconKey),
        false);
  }

  private String getSelectedIconLabel() {
    return ICON_LABELS.getOrDefault(selectedIconKey, "Loot");
  }

  public static final class ChestLabelEditorPageEventData {
    static final String KEY_ACTION = "Action";
    // The @ prefix tells CustomUI to evaluate a selector rather than send it literally.
    static final String KEY_NAME = "@Name";
    static final String KEY_ICON = "Icon";

    static final String ACTION_SAVE = "Save";
    static final String ACTION_CLEAR = "Clear";
    static final String ACTION_SELECT_ICON = "SelectIcon";
    static final String ACTION_CLOSE = "Close";

    static final BuilderCodec<ChestLabelEditorPageEventData> CODEC =
        BuilderCodec.builder(ChestLabelEditorPageEventData.class, ChestLabelEditorPageEventData::new)
            .append(
                new KeyedCodec<>(KEY_ACTION, Codec.STRING),
                (entry, value) -> entry.action = value,
                entry -> entry.action)
            .add()
            .append(
                new KeyedCodec<>(KEY_NAME, Codec.STRING),
                (entry, value) -> entry.name = value,
                entry -> entry.name)
            .add()
            .append(
                new KeyedCodec<>(KEY_ICON, Codec.STRING),
                (entry, value) -> entry.icon = value,
                entry -> entry.icon)
            .add()
            .build();

    private String action;
    private String name;
    private String icon;
  }
}
