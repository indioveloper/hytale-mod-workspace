package gg.orbgenesis.mapselector;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class MapsCommand extends AbstractPlayerCommand {
  private static final Message OPEN_FAILED =
      Message.raw("No se pudo abrir el selector de mapas.");

  private final MapSelectorPlugin plugin;

  public MapsCommand(MapSelectorPlugin plugin) {
    super("mapas", "Abre el selector de mapas del minijuego.");
    this.plugin = plugin;
  }

  @Override
  protected void execute(
      CommandContext context,
      Store<EntityStore> store,
      Ref<EntityStore> playerEntityRef,
      PlayerRef playerRef,
      World world) {
    Player player = store.getComponent(playerEntityRef, Player.getComponentType());
    if (player == null || playerRef == null) {
      context.sendMessage(OPEN_FAILED);
      return;
    }

    MapSelectorPage page =
        new MapSelectorPage(plugin, playerRef, plugin.getSelectedMap(playerRef.getUuid()));
    player.getPageManager().openCustomPage(playerEntityRef, store, page);
    page.showInitialPreview();
  }
}
