package gg.orbgenesis.buildbattle;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public class BuildBattlePlugin extends JavaPlugin {
  private static BuildBattlePlugin instance;

  private CreativeToolRestrictionManager creativeToolRestrictionManager;

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

  @Override
  protected void setup() {
    super.setup();
    creativeToolRestrictionManager =
        new CreativeToolRestrictionManager(getDataDirectory(), getLogger());
    creativeToolRestrictionManager.recoverStaleRestrictions();
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
    if (creativeToolRestrictionManager != null) {
      creativeToolRestrictionManager.restoreAll();
    }
    instance = null;
    super.shutdown();
  }
}
