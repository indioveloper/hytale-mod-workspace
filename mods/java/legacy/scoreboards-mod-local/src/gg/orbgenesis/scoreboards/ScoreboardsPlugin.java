package gg.orbgenesis.scoreboards;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public class ScoreboardsPlugin extends JavaPlugin {
  public ScoreboardsPlugin(JavaPluginInit init) {
    super(init);
  }

  @Override
  protected void setup() {
    super.setup();
    getCommandRegistry().registerCommand(new ScoreboardsCommand());
    getCommandRegistry().registerCommand(new AliasCommand("scoreboardui"));
    getCommandRegistry().registerCommand(new AliasCommand("scoreui"));
  }
}
