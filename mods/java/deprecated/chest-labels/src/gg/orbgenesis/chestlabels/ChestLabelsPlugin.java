package gg.orbgenesis.chestlabels;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class ChestLabelsPlugin extends JavaPlugin {
  private static ChestLabelsPlugin instance;

  private ComponentType<ChunkStore, ChestLabelComponent> chestLabelComponentType;

  public ChestLabelsPlugin(JavaPluginInit init) {
    super(init);
    instance = this;
  }

  public static ChestLabelsPlugin get() {
    return instance;
  }

  public ComponentType<ChunkStore, ChestLabelComponent> getChestLabelComponentType() {
    return chestLabelComponentType;
  }

  @Override
  protected void setup() {
    super.setup();

    chestLabelComponentType =
        getChunkStoreRegistry()
            .registerComponent(
                ChestLabelComponent.class,
                "OrbGenesis_ChestLabel",
                ChestLabelComponent.CODEC);

    getCommandRegistry().registerCommand(new ChestLabelCommand(this));
    getEntityStoreRegistry().registerSystem(new ChestLabelHoverSystem(chestLabelComponentType));
  }
}
