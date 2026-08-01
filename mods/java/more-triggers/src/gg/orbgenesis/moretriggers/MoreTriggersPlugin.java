package gg.orbgenesis.moretriggers;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import gg.orbgenesis.moretriggers.timer.ControlTimerEffect;
import gg.orbgenesis.moretriggers.timer.TimerCommand;
import gg.orbgenesis.moretriggers.timer.TimerManager;
import gg.orbgenesis.moretriggers.timer.TimerTickingSystem;

public class MoreTriggersPlugin extends JavaPlugin {
  private static MoreTriggersPlugin instance;

  private final TimerManager timerManager = new TimerManager();

  public MoreTriggersPlugin(JavaPluginInit init) {
    super(init);
    instance = this;
  }

  public static MoreTriggersPlugin get() {
    return instance;
  }

  public TimerManager getTimerManager() {
    return timerManager;
  }

  @Override
  public void setup() {
    super.setup();
    TriggerVolumesPlugin triggerVolumes = TriggerVolumesPlugin.get();
    triggerVolumes.registerEffectType(
        "GiveRandomItem", GiveRandomItemEffect.class, GiveRandomItemEffect.CODEC);
    triggerVolumes.registerEffectType(
        "PasteRandomPrefab", PasteRandomPrefabEffect.class, PasteRandomPrefabEffect.CODEC);
    triggerVolumes.registerEffectType(
        "SendTagMessage", SendTagMessageEffect.class, SendTagMessageEffect.CODEC);
    triggerVolumes.registerEffectType(
        "ShowTagEventTitle", ShowTagEventTitleEffect.class, ShowTagEventTitleEffect.CODEC);
    triggerVolumes.registerEffectType(
        "ControlTimer", ControlTimerEffect.class, ControlTimerEffect.CODEC);
    triggerVolumes.registerEffectType(
        "ExecuteCommand", ExecuteCommandEffect.class, ExecuteCommandEffect.CODEC);
    triggerVolumes.registerRuleType("NoMove", NoMoveRule.class, NoMoveRule.CODEC);
    triggerVolumes.registerAssetField("PasteRandomPrefab", "Prefab1", "Prefab");
    triggerVolumes.registerAssetField("PasteRandomPrefab", "Prefab2", "Prefab");
    triggerVolumes.registerAssetField("PasteRandomPrefab", "Prefabs", "Prefab");
    triggerVolumes.registerAssetField("NoMove", "ExcludedNpcRoles", "NpcRole");
    getCommandRegistry().registerCommand(new TimerCommand(this));
    getEntityStoreRegistry().registerSystem(new TimerTickingSystem(timerManager));
    getEntityStoreRegistry().registerSystem(new NoMoveRuleSystem());
  }

  @Override
  protected void shutdown() {
    timerManager.clear();
    instance = null;
    super.shutdown();
  }
}
