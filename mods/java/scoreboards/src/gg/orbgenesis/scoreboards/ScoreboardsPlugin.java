package gg.orbgenesis.scoreboards;

import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;

public class ScoreboardsPlugin extends JavaPlugin {
  private static ScoreboardsPlugin instance;

  private final Config<ScoreboardConfig> config;
  private final ScoreboardManager manager;

  public ScoreboardsPlugin(JavaPluginInit init) {
    super(init);
    instance = this;
    config = withConfig("scoreboards", ScoreboardConfig.CODEC);
    manager = new ScoreboardManager(config);
  }

  public static ScoreboardsPlugin get() {
    return instance;
  }

  public ScoreboardManager getManager() {
    return manager;
  }

  @Override
  protected void setup() {
    super.setup();
    ObjectivePlugin objectives = ObjectivePlugin.get();
    if (objectives == null) {
      throw new IllegalStateException("Hytale:Objectives is required");
    }
    objectives.registerTask(
        "OrbGenesisManualCount",
        ManualCountObjectiveTaskAsset.class,
        ManualCountObjectiveTaskAsset.CODEC,
        ManualCountObjectiveTask.class,
        ManualCountObjectiveTask.CODEC,
        ManualCountObjectiveTask::new);

    manager.initialize();
    getCommandRegistry().registerCommand(new ScoreboardsCommand(this));
    getCommandRegistry().registerCommand(new OpenScoreboardsCommand(this));
    getEventRegistry().registerGlobal(PlayerReadyEvent.class, this::onPlayerReady);

    TriggerVolumesPlugin triggerVolumes = TriggerVolumesPlugin.get();
    if (triggerVolumes != null) {
      triggerVolumes.registerEffectType(
          "ControlScoreboard", ControlScoreboardEffect.class, ControlScoreboardEffect.CODEC);
      triggerVolumes.registerEffectType(
          "ModifyScoreboardTask",
          ModifyScoreboardTaskEffect.class,
          ModifyScoreboardTaskEffect.CODEC);
      triggerVolumes.registerConditionType(
          "ScoreboardState", ScoreboardStateCondition.class, ScoreboardStateCondition.CODEC);
      triggerVolumes.registerConditionType(
          "ScoreboardTaskValue",
          ScoreboardTaskValueCondition.class,
          ScoreboardTaskValueCondition.CODEC);
    }
  }

  private void onPlayerReady(PlayerReadyEvent event) {
    if (event.getPlayerRef() != null && event.getPlayerRef().isValid()) {
      manager.resyncPlayer(
          event
              .getPlayerRef()
              .getStore()
              .getComponent(event.getPlayerRef(), com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType()));
    }
  }
}
