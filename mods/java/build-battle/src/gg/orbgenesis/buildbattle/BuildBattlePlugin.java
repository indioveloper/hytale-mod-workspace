package gg.orbgenesis.buildbattle;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public class BuildBattlePlugin extends JavaPlugin {
  private static BuildBattlePlugin instance;

  private CreativeToolRestrictionManager creativeToolRestrictionManager;
  private BuildBattleManager buildBattleManager;

  public BuildBattlePlugin(JavaPluginInit init) {
    super(init);
    instance = this;
  }

  public static BuildBattlePlugin get() {
    return instance;
  }

  public CreativeToolRestrictionManager getCreativeToolRestrictionManager() {
    return creativeToolRestrictionManager;
  }

  public BuildBattleManager getBuildBattleManager() {
    return buildBattleManager;
  }

  @Override
  protected void setup() {
    super.setup();
    creativeToolRestrictionManager =
        new CreativeToolRestrictionManager(getDataDirectory(), getLogger());
    creativeToolRestrictionManager.recoverStaleRestrictions();
    buildBattleManager =
        new BuildBattleManager(getDataDirectory(), getLogger(), creativeToolRestrictionManager);
    buildBattleManager.initialize();

    getCommandRegistry().registerCommand(new BuildBattleCommand(buildBattleManager));
    getEventRegistry()
        .registerGlobal(
            PlayerDisconnectEvent.class,
            event -> buildBattleManager.leave(event.getPlayerRef().getUuid()));

    getEntityStoreRegistry().registerSystem(new BuildBattleTickSystem(buildBattleManager));
    getEntityStoreRegistry().registerSystem(new BuildBattleBlockSystems.Place(buildBattleManager));
    getEntityStoreRegistry().registerSystem(new BuildBattleBlockSystems.Break(buildBattleManager));
    getEntityStoreRegistry()
        .registerSystem(new CreativeToolRestrictionSystem(creativeToolRestrictionManager));
    getEntityStoreRegistry()
        .registerSystem(new CreativeToolRestrictionRuleSystem(creativeToolRestrictionManager));

    TriggerVolumesPlugin.get()
        .registerEffectType(
            "SuggestBuildTheme",
            SuggestBuildThemeEffect.class,
            SuggestBuildThemeEffect.CODEC);
    TriggerVolumesPlugin.get()
        .registerRuleType(
            "RestrictBuildBattleCreativeTools",
            RestrictBuildBattleCreativeToolsRule.class,
            RestrictBuildBattleCreativeToolsRule.CODEC);
  }

  @Override
  protected void shutdown() {
    if (buildBattleManager != null) {
      buildBattleManager.shutdown();
    }
    if (creativeToolRestrictionManager != null) {
      creativeToolRestrictionManager.restoreAll();
    }
    instance = null;
    super.shutdown();
  }
}
