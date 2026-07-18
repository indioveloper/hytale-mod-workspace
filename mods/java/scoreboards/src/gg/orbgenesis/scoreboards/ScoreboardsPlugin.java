package gg.orbgenesis.scoreboards;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public class ScoreboardsPlugin extends JavaPlugin {
  private final ScoreboardTracker tracker = new ScoreboardTracker();

  public ScoreboardsPlugin(JavaPluginInit init) {
    super(init);
  }

  @Override
  protected void setup() {
    super.setup();
    getCommandRegistry().registerCommand(new ScoreboardsCommand(tracker));
    getCommandRegistry().registerCommand(new AliasCommand("scoreboards", tracker));
    getEntityStoreRegistry().registerSystem(new ScoreboardDeathSystem(tracker));
  }
}
