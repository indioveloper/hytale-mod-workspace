package gg.orbgenesis.scoreboards;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class OpenScoreboardsCommand extends AbstractPlayerCommand {
  private final ScoreboardsPlugin plugin;

  public OpenScoreboardsCommand(ScoreboardsPlugin plugin) {
    super("scoreboard", "Open the editable Objectives interface.");
    this.plugin = plugin;
  }

  @Override
  protected void execute(
      CommandContext context,
      Store<EntityStore> store,
      Ref<EntityStore> ref,
      PlayerRef playerRef,
      World world) {
    Player player = store.getComponent(ref, Player.getComponentType());
    if (player != null) {
      player.getPageManager().clearCustomPageAcknowledgements();
      player
          .getPageManager()
          .openCustomPage(ref, store, new ScoreboardListPage(playerRef, plugin));
    }
  }
}
