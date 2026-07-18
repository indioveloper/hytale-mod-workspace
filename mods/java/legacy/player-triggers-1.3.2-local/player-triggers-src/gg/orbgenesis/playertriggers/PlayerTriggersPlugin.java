package gg.orbgenesis.playertriggers;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class PlayerTriggersPlugin extends JavaPlugin {
  private static PlayerTriggersPlugin instance;

  private ComponentType<EntityStore, PlayerTagsComponent> playerTagsComponentType;
  private final PlayerTimerService timerService = new PlayerTimerService();

  public PlayerTriggersPlugin(JavaPluginInit init) {
    super(init);
    instance = this;
  }

  public static PlayerTriggersPlugin get() {
    return instance;
  }

  public ComponentType<EntityStore, PlayerTagsComponent> getPlayerTagsComponentType() {
    return playerTagsComponentType;
  }

  public PlayerTimerService getTimerService() {
    return timerService;
  }

  @Override
  protected void setup() {
    super.setup();

    playerTagsComponentType =
        getEntityStoreRegistry()
            .registerComponent(
                PlayerTagsComponent.class,
                "OrbGenesis_PlayerTriggerTags",
                PlayerTagsComponent.CODEC);
    getEntityStoreRegistry().registerSystem(new PlayerBoundVolumeSystem());
    getEntityStoreRegistry().registerSystem(new PlayerKillCounterHudSystem());
    getEntityStoreRegistry().registerSystem(new PlayerCountTagSystem());
    getEntityStoreRegistry().registerSystem(new MobKillListenerSystem());
    getEntityStoreRegistry().registerSystem(new PlayerTimerSystem(timerService));

    TriggerVolumesPlugin triggerVolumes = TriggerVolumesPlugin.get();
    triggerVolumes.registerEffectType(
        "FollowTriggeringPlayer", FollowTriggeringPlayerEffect.class, FollowTriggeringPlayerEffect.CODEC);
    triggerVolumes.registerEffectType(
        "StopFollowingPlayer", StopFollowingPlayerEffect.class, StopFollowingPlayerEffect.CODEC);
    triggerVolumes.registerEffectType(
        "ModifyPlayerTag", ModifyPlayerTagEffect.class, ModifyPlayerTagEffect.CODEC);
    triggerVolumes.registerEffectType(
        "MobKillListener", MobKillListenerEffect.class, MobKillListenerEffect.CODEC);
    triggerVolumes.registerEffectType(
        "StartPlayerTimer", StartPlayerTimerEffect.class, StartPlayerTimerEffect.CODEC);
    triggerVolumes.registerEffectType(
        "PlayerCountHud", PlayerCountHudEffect.class, PlayerCountHudEffect.CODEC);
    triggerVolumes.registerEffectType(
        "KillCounterHud", KillCounterHudEffect.class, KillCounterHudEffect.CODEC);
    triggerVolumes.registerEffectType(
        "TagValueHud", TagValueHudEffect.class, TagValueHudEffect.CODEC);
    triggerVolumes.registerEffectType(
        "PlayerPermission", PlayerPermissionEffect.class, PlayerPermissionEffect.CODEC);
    triggerVolumes.registerConditionType(
        "PlayerTag", PlayerTagCondition.class, PlayerTagCondition.CODEC);
  }
}
