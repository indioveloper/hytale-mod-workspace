package gg.orbgenesis.scoreboards;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class ScoreboardsCommand extends AbstractPlayerCommand {
  private final ScoreboardTracker tracker;

  public ScoreboardsCommand() {
    this(new ScoreboardTracker());
  }

  public ScoreboardsCommand(ScoreboardTracker tracker) {
    super("scoreboard", "Open the scoreboard editor UI.");
    this.tracker = tracker;
  }

  public ScoreboardsCommand(String name, String description) {
    super(name, description);
    this.tracker = new ScoreboardTracker();
  }

  public ScoreboardsCommand(String name, String description, ScoreboardTracker tracker) {
    super(name, description);
    this.tracker = tracker;
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

  protected void executeScoreboard(
      Store<EntityStore> store,
      Ref<EntityStore> playerEntityRef,
      PlayerRef playerRef) {
    Player player = store.getComponent(playerEntityRef, Player.getComponentType());
    if (player == null || playerRef == null) {
      return;
    }

    player
        .getPageManager()
        .openCustomPage(playerEntityRef, store, new ScoreboardEditorPage(playerRef, tracker));
  }
}
