package gg.raynormods.desertsurvival.research;

import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import javax.annotation.Nonnull;

public final class ResearchPrototypePlugin extends JavaPlugin {
  public ResearchPrototypePlugin(@Nonnull JavaPluginInit init) {
    super(init);
  }

  @Override
  protected void setup() {
    // Interaction codecs must exist before the asset phase resolves RootInteraction references.
    getCodecRegistry(Interaction.CODEC).register(
        "RaynorCollectIntel", CollectIntelInteraction.class, CollectIntelInteraction.CODEC);
    ResearchProgressComponent.setComponentType(
        getEntityStoreRegistry().registerComponent(
            ResearchProgressComponent.class,
            "Raynor_DesertResearchProgress",
            ResearchProgressComponent.CODEC));
    getCommandRegistry().registerCommand(new ResearchCommand());
    getEntityStoreRegistry().registerSystem(new IntelInventoryConversionSystem());
    getEventRegistry().registerGlobal(PlayerReadyEvent.class, this::onPlayerReady);
  }

  private void onPlayerReady(PlayerReadyEvent event) {
    var ref = event.getPlayerRef();
    if (ref == null || !ref.isValid()) {
      return;
    }
    if (ref != null && ref.isValid()
        && ref.getStore().getComponent(
            ref, ResearchProgressComponent.getComponentType()) == null) {
      ref.getStore().addComponent(
          ref,
          ResearchProgressComponent.getComponentType(),
          ResearchProgressComponent.fresh());
    }
    IntelInventoryConversionSystem.convertExisting(ref, ref.getStore());
  }
}
