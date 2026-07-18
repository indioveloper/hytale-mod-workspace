package gg.orbgenesis.scoreboards;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class AliasCommand extends ScoreboardsCommand {
  public AliasCommand(String name) {
    super(name, "Open the scoreboard editor UI.");
  }

  public AliasCommand(String name, ScoreboardTracker tracker) {
    super(name, "Open the scoreboard editor UI.", tracker);
  }

  @Override
  protected void execute(
      CommandContext context,
      Store<EntityStore> store,
      Ref<EntityStore> playerEntityRef,
      PlayerRef playerRef,
      World world) {
    executeScoreboard(store, playerEntityRef, playerRef);
  }
}
