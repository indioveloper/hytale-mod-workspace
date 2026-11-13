package gg.raynormods.desertsurvival.cutterray;

import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import javax.annotation.Nonnull;

public final class CutterrayPrototypePlugin extends JavaPlugin {
  public CutterrayPrototypePlugin(@Nonnull JavaPluginInit init) {
    super(init);
  }

  @Override
  protected void setup() {
    CutterrayBeamExpiryComponent.setComponentType(
        getEntityStoreRegistry().registerComponent(
            CutterrayBeamExpiryComponent.class,
            "Raynor_CutterrayBeamExpiry",
            CutterrayBeamExpiryComponent.CODEC));
    getEntityStoreRegistry().registerSystem(new CutterrayBeamExpirySystem());
    CutterrayContactAnchorComponent.setComponentType(
        getEntityStoreRegistry().registerComponent(
            CutterrayContactAnchorComponent.class,
            "Raynor_CutterrayContactAnchor",
            CutterrayContactAnchorComponent.CODEC));
    getEntityStoreRegistry().registerSystem(new CutterrayContactAnchorSystem());

    getCodecRegistry(Interaction.CODEC)
        .register(
            "CutterrayBeamOn",
            CutterrayBeamOnInteraction.class,
            CutterrayBeamOnInteraction.CODEC)
        .register(
            "CutterrayBeamOff",
            CutterrayBeamOffInteraction.class,
            CutterrayBeamOffInteraction.CODEC);
  }
}
