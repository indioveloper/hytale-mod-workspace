package gg.raynor.triggervolumebrowser;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.builtin.triggervolumes.command.TriggerVolumeArgTypes;
import com.hypixel.hytale.builtin.triggervolumes.ui.TriggerVolumeInspectorPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Locale;
import javax.annotation.Nonnull;

/**
 * Drop-in replacement for vanilla {@code /triggervolume effects}.
 *
 * <p>The Trigger Volume client context binds E directly to that command, bypassing the held
 * item's Use interaction. The bridge keeps the vanilla behavior for the vanilla tool and routes
 * only Raynor's clone to the enhanced inspector.
 */
final class TriggerVolumeEffectsBridgeCommand extends AbstractPlayerCommand {
  static final String CLONE_ITEM_ID = "Raynor_TriggerVolumeBrowserTool";

  private final OptionalArg<String> volumeNameArg =
      withOptionalArg(
          "volumeName",
          "server.commands.triggervolume.effects.volumeName.desc",
          TriggerVolumeArgTypes.VOLUME_NAME);
  private final FlagArg groupFlag =
      withFlagArg("group", "server.commands.triggervolume.effects.group.desc");

  TriggerVolumeEffectsBridgeCommand() {
    super("effects", "server.commands.triggervolume.effects.desc");
    requirePermission("hytale.system.command.triggervolume.effects");
  }

  @Override
  protected void execute(
      @Nonnull CommandContext context,
      @Nonnull Store<EntityStore> store,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull PlayerRef playerRef,
      @Nonnull World world) {
    var plugin = TriggerVolumesPlugin.get();
    var worldName = world.getName().toLowerCase(Locale.ROOT);
    var manager = store.getResource(plugin.getManagerResourceType());
    if (manager == null) {
      return;
    }

    var volumeName = volumeNameArg.get(context);
    if (volumeName == null) {
      volumeName = manager.getPlayerSelection(context.sender().getUuid());
    }

    var player = store.getComponent(ref, Player.getComponentType());
    if (player == null) {
      return;
    }

    var heldItem = InventoryComponent.getItemInHand(store, ref);
    boolean cloneEquipped = heldItem != null && CLONE_ITEM_ID.equals(heldItem.getItemId());

    if (volumeName == null) {
      player
          .getPageManager()
          .openCustomPage(
              ref,
              store,
              new TriggerVolumeInspectorPage(
                  playerRef, worldName, null, TriggerVolumeInspectorPage.InspectorTab.EFFECTS));
      return;
    }

    var entry = manager.getVolume(volumeName);
    if (entry == null) {
      context.sendMessage(
          Message.translation("server.commands.triggervolume.notFound").param("name", volumeName));
      return;
    }

    var groupId = entry.getGroupId();
    if (groupFlag.get(context) && groupId != null) {
      var group = manager.getGroup(groupId);
      if (group != null) {
        player
            .getPageManager()
            .openCustomPage(
                ref,
                store,
                new TriggerVolumeInspectorPage(
                    playerRef,
                    worldName,
                    groupId,
                    true,
                    TriggerVolumeInspectorPage.InspectorTab.EFFECTS));
        return;
      }
    }

    player
        .getPageManager()
        .openCustomPage(
            ref,
            store,
            cloneEquipped
                ? new EffectBrowserInspectorPage(playerRef, worldName, volumeName)
                : new TriggerVolumeInspectorPage(
                    playerRef,
                    worldName,
                    volumeName,
                    TriggerVolumeInspectorPage.InspectorTab.EFFECTS));
  }
}
