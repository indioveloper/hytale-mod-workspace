package gg.orbgenesis.playertriggertags;

import com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class PlayerTriggerTagsPlugin extends JavaPlugin {
  private static PlayerTriggerTagsPlugin instance;

  private ComponentType<EntityStore, PlayerTagsComponent> playerTagsComponentType;

  public PlayerTriggerTagsPlugin(JavaPluginInit init) {
    super(init);
    instance = this;
  }

  public static PlayerTriggerTagsPlugin get() {
    return instance;
  }

  public ComponentType<EntityStore, PlayerTagsComponent> getPlayerTagsComponentType() {
    return playerTagsComponentType;
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
    TriggerVolumesPlugin triggerVolumes = TriggerVolumesPlugin.get();
    triggerVolumes.registerEffectType(
        "ModifyPlayerTag", ModifyPlayerTagEffect.class, ModifyPlayerTagEffect.CODEC);
    triggerVolumes.registerConditionType(
        "PlayerTagCondition", PlayerTagCondition.class, PlayerTagCondition.CODEC);
  }
}
