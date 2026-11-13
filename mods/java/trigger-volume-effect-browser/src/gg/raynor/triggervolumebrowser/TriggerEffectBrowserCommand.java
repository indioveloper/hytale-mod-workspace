package gg.raynor.triggervolumebrowser;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Locale;
import javax.annotation.Nonnull;

final class TriggerEffectBrowserCommand extends AbstractPlayerCommand {
  TriggerEffectBrowserCommand() {
    super("triggerpalette", "server.commands.triggerEffectBrowser.desc");
  }

  @Override
  protected void execute(
      @Nonnull CommandContext context,
      @Nonnull Store<EntityStore> store,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull PlayerRef playerRef,
      @Nonnull World world) {
    var plugin = TriggerVolumesPlugin.get();
    var manager = store.getResource(plugin.getManagerResourceType());
    var player = store.getComponent(ref, Player.getComponentType());
    if (manager == null || player == null) {
      return;
    }

    var selectedVolume = manager.getPlayerSelection(context.sender().getUuid());
    player.getPageManager().clearCustomPageAcknowledgements();
    player
        .getPageManager()
        .openCustomPage(
            ref,
            store,
            new EffectBrowserInspectorPage(
                playerRef, world.getName().toLowerCase(Locale.ROOT), selectedVolume));
  }
}
