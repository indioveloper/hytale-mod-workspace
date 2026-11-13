package gg.raynormods.desertsurvival.vitals;

import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import javax.annotation.Nonnull;

public final class SurvivalVitalsPlugin extends JavaPlugin {
  public SurvivalVitalsPlugin(@Nonnull JavaPluginInit init) {
    super(init);
  }

  @Override
  protected void setup() {
    SurvivalVitalsComponent.setComponentType(
        getEntityStoreRegistry().registerComponent(
            SurvivalVitalsComponent.class,
            "Raynor_DesertSurvivalVitals",
            SurvivalVitalsComponent.CODEC));
    getEntityStoreRegistry().registerSystem(new SurvivalVitalsSystem());
    getCodecRegistry(Interaction.CODEC).register(
        "RaynorHydrate", HydrateInteraction.class, HydrateInteraction.CODEC);
    getCommandRegistry().registerCommand(new DrinkStillsuitCommand());
    getEventRegistry().registerGlobal(PlayerReadyEvent.class, this::onPlayerReady);
  }

  private void onPlayerReady(PlayerReadyEvent event) {
    var ref = event.getPlayerRef();
    if (ref != null && ref.isValid()
        && ref.getStore().getComponent(ref, SurvivalVitalsComponent.getComponentType()) == null) {
      ref.getStore().addComponent(
          ref,
          SurvivalVitalsComponent.getComponentType(),
          SurvivalVitalsComponent.fresh());
    }
  }
}
