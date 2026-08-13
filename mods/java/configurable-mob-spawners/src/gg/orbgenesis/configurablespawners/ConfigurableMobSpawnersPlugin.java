package gg.orbgenesis.configurablespawners;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.protocol.GameMode;
import javax.annotation.Nonnull;

public final class ConfigurableMobSpawnersPlugin extends JavaPlugin {
  public ConfigurableMobSpawnersPlugin(@Nonnull JavaPluginInit init) {
    super(init);
  }

  @Override
  protected void setup() {
    ConfigurableSpawnerComponent.setComponentType(
        getChunkStoreRegistry().registerComponent(
            ConfigurableSpawnerComponent.class,
            "OrbGenesis_ConfigurableMobSpawner",
            ConfigurableSpawnerComponent.CODEC));
    SpawnedBySpawnerComponent.setComponentType(
        getEntityStoreRegistry().registerComponent(
            SpawnedBySpawnerComponent.class,
            "OrbGenesis_SpawnedBySpawner",
            SpawnedBySpawnerComponent.CODEC));

    getChunkStoreRegistry().registerSystem(new SpawnerTickSystem());
    getEntityStoreRegistry().registerSystem(new SpawnerMobSetupSystem());
    getEntityStoreRegistry().registerSystem(new SpawnerAttitudeSystem());
    getEntityStoreRegistry().registerSystem(new SpawnerRetaliationSystem());
    getEntityStoreRegistry().registerSystem(new SpawnerLootSystem());

    OpenCustomUIInteraction.registerBlockEntityCustomPage(
        this,
        SpawnerEditorPage.class,
        "OrbGenesis_ConfigurableMobSpawner",
        (playerRef, blockRef) -> createEditorPage(playerRef.getReference(), blockRef));
  }

  private SpawnerEditorPage createEditorPage(
      Ref<EntityStore> playerRef, Ref<ChunkStore> blockRef) {
    var playerStore = playerRef.getStore();
    Player player = playerStore.getComponent(playerRef, Player.getComponentType());
    var playerRefComponent = playerStore.getComponent(
        playerRef,
        com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType());
    if (player == null || playerRefComponent == null) {
      return null;
    }
    GameMode effectiveGameMode = player.getGameMode();
    if (effectiveGameMode == null) {
      effectiveGameMode = blockStoreWorld(blockRef).getWorldConfig().getGameMode();
    }
    if (effectiveGameMode != GameMode.Creative) {
      playerRefComponent.sendMessage(
          Message.translation("server.customUI.configurableSpawners.creativeOnly"));
      return null;
    }

    var blockStore = blockRef.getStore();
    var info = blockStore.getComponent(
        blockRef,
        com.hypixel.hytale.server.core.modules.block.BlockModule.BlockStateInfo.getComponentType());
    var state = blockStore.getComponent(blockRef, ConfigurableSpawnerComponent.getComponentType());
    if (info == null || state == null) {
      return null;
    }
    return new SpawnerEditorPage(
        playerRefComponent,
        info,
        state,
        CustomPageLifetime.CanDismissOrCloseThroughInteraction);
  }

  private static com.hypixel.hytale.server.core.universe.world.World blockStoreWorld(
      Ref<ChunkStore> blockRef) {
    return blockRef.getStore().getExternalData().getWorld();
  }
}
