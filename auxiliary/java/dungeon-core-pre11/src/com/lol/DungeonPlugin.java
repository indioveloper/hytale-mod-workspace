/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.hypixel.hytale.codec.Codec
 *  com.hypixel.hytale.codec.builder.BuilderCodec
 *  com.hypixel.hytale.codec.lookup.StringCodecMapCodec
 *  com.hypixel.hytale.common.plugin.PluginIdentifier
 *  com.hypixel.hytale.component.ComponentRegistryProxy
 *  com.hypixel.hytale.component.ComponentType
 *  com.hypixel.hytale.component.ResourceType
 *  com.hypixel.hytale.component.system.ISystem
 *  com.hypixel.hytale.server.core.command.system.AbstractCommand
 *  com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction
 *  com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction
 *  com.hypixel.hytale.server.core.plugin.JavaPlugin
 *  com.hypixel.hytale.server.core.plugin.JavaPluginInit
 *  com.hypixel.hytale.server.core.plugin.PluginBase
 *  com.hypixel.hytale.server.core.plugin.PluginManager
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  org.checkerframework.checker.nullness.compatqual.NonNullDecl
 */
package com.lol;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.StringCodecMapCodec;
import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lol.commands.DungeonDebugCommand;
import com.lol.commands.TestStartCommand;
import com.lol.components.DungeonBlock;
import com.lol.components.DungeonChunkData;
import com.lol.components.EntityCountRef;
import com.lol.components.Flopper;
import com.lol.components.GhostBlock;
import com.lol.components.Inverter;
import com.lol.components.ItemSpawner;
import com.lol.components.MimicBlockComponent;
import com.lol.components.MimicPlaceMode;
import com.lol.components.MobSpawner;
import com.lol.components.PlayerDetector;
import com.lol.components.PlayerStart;
import com.lol.components.RelayComponent;
import com.lol.components.SignalReceiver;
import com.lol.components.SignalSender;
import com.lol.components.TeleporterBlock;
import com.lol.interactions.LinkItemInteraction;
import com.lol.interactions.MakeMimicInteraction;
import com.lol.interactions.MimicInteractInteraction;
import com.lol.interactions.TeleporterLinkInteraction;
import com.lol.interactions.ViewLinksInteraction;
import com.lol.interactions.ViewMimicsInteraction;
import com.lol.resources.DungeonBlocks;
import com.lol.resources.EntityCountResource;
import com.lol.resources.SignalRouter;
import com.lol.resources.TimeoutAction;
import com.lol.systems.DungeonBlockSystem;
import com.lol.systems.EntityCountSystem;
import com.lol.systems.FlopperSystem;
import com.lol.systems.GhostBlockSystems;
import com.lol.systems.InverterSystem;
import com.lol.systems.ItemSpawnerSystem;
import com.lol.systems.MimicPlaceSystems;
import com.lol.systems.MobSpawnerSystem;
import com.lol.systems.PlayerDetecorSystem;
import com.lol.systems.PlayerStartSystem;
import com.lol.systems.RelaySystem;
import com.lol.systems.SignalRecoverSystem;
import com.lol.systems.SignalSystems;
import com.lol.systems.SignalUpdateSystem;
import com.lol.systems.TeleporterSystem;
import com.lol.systems.TimeoutSystem;
import com.lol.ui.FlopperUI;
import com.lol.ui.ItemSpawnerUI;
import com.lol.ui.MobSpawnerUI;
import com.lol.ui.PlayerDetectorUI;
import com.lol.ui.PlayerStartUI;
import com.lol.ui.RelayPage;
import com.lol.ui.TeleporterUI;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class DungeonPlugin
extends JavaPlugin {
    public static Logger logger = Logger.getLogger("DungeonCore");

    public DungeonPlugin(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    protected void setup() {
        PlayerDetector.setComponentType((ComponentType<ChunkStore, PlayerDetector>)this.getChunkStoreRegistry().registerComponent(PlayerDetector.class, "PlayerDetector", PlayerDetector.CODEC));
        SignalSender.setComponentType((ComponentType<ChunkStore, SignalSender>)this.getChunkStoreRegistry().registerComponent(SignalSender.class, "SignalSender", SignalSender.CODEC));
        SignalReceiver.setComponentType((ComponentType<ChunkStore, SignalReceiver>)this.getChunkStoreRegistry().registerComponent(SignalReceiver.class, "SignalReceiver", SignalReceiver.CODEC));
        TeleporterBlock.setComponentType((ComponentType<ChunkStore, TeleporterBlock>)this.getChunkStoreRegistry().registerComponent(TeleporterBlock.class, "TeleporterBlock", TeleporterBlock.CODEC));
        MobSpawner.setComponentType((ComponentType<ChunkStore, MobSpawner>)this.getChunkStoreRegistry().registerComponent(MobSpawner.class, "MobSpawner", MobSpawner.CODEC));
        ItemSpawner.setComponentType((ComponentType<ChunkStore, ItemSpawner>)this.getChunkStoreRegistry().registerComponent(ItemSpawner.class, "ItemSpawner", ItemSpawner.CODEC));
        GhostBlock.setComponentType((ComponentType<ChunkStore, GhostBlock>)this.getChunkStoreRegistry().registerComponent(GhostBlock.class, "GhostBlock", GhostBlock.CODEC));
        PlayerStart.setComponentType((ComponentType<ChunkStore, PlayerStart>)this.getChunkStoreRegistry().registerComponent(PlayerStart.class, "PlayerStart", PlayerStart.CODEC));
        DungeonBlock.setComponentType((ComponentType<ChunkStore, DungeonBlock>)this.getChunkStoreRegistry().registerComponent(DungeonBlock.class, "DungeonBlock", DungeonBlock.CODEC));
        RelayComponent.setComponentType((ComponentType<ChunkStore, RelayComponent>)this.getChunkStoreRegistry().registerComponent(RelayComponent.class, "RelayComponent", RelayComponent.CODEC));
        Flopper.setComponentType((ComponentType<ChunkStore, Flopper>)this.getChunkStoreRegistry().registerComponent(Flopper.class, "Flopper", Flopper.CODEC));
        Inverter.setComponentType((ComponentType<ChunkStore, Inverter>)this.getChunkStoreRegistry().registerComponent(Inverter.class, "Inverter", Inverter.CODEC));
        MimicBlockComponent.setComponentType((ComponentType<ChunkStore, MimicBlockComponent>)this.getChunkStoreRegistry().registerComponent(MimicBlockComponent.class, "MimicBlockComponent", MimicBlockComponent.CODEC));
        MimicPlaceMode.setComponentType((ComponentType<EntityStore, MimicPlaceMode>)this.getEntityStoreRegistry().registerComponent(MimicPlaceMode.class, "MimicPlaceMode", MimicPlaceMode.CODEC));
        EntityCountRef.setComponentType((ComponentType<EntityStore, EntityCountRef>)this.getEntityStoreRegistry().registerComponent(EntityCountRef.class, "EntityCountRef", EntityCountRef.CODEC));
        DungeonBlocks.setResourceType((ResourceType<ChunkStore, DungeonBlocks>)this.getChunkStoreRegistry().registerResource(DungeonBlocks.class, "DungeonBlocks", DungeonBlocks.CODEC));
        TimeoutAction.setResourceType((ResourceType<ChunkStore, TimeoutAction>)this.getChunkStoreRegistry().registerResource(TimeoutAction.class, "TimeoutAction", TimeoutAction.CODEC));
        SignalRouter.setResourceType((ResourceType<ChunkStore, SignalRouter>)this.getChunkStoreRegistry().registerResource(SignalRouter.class, "SignalRouter", SignalRouter.CODEC));
        EntityCountResource.setResourceType((ResourceType<EntityStore, EntityCountResource>)this.getEntityStoreRegistry().registerResource(EntityCountResource.class, "EntityCountResource", EntityCountResource.CODEC));
        this.getChunkStoreRegistry().registerSystem((ISystem)new PlayerDetecorSystem());
        this.getChunkStoreRegistry().registerSystem((ISystem)new MobSpawnerSystem());
        this.getChunkStoreRegistry().registerSystem((ISystem)new ItemSpawnerSystem());
        this.getChunkStoreRegistry().registerSystem((ISystem)new PlayerStartSystem());
        this.getChunkStoreRegistry().registerSystem((ISystem)new DungeonBlockSystem());
        this.getChunkStoreRegistry().registerSystem((ISystem)new TimeoutSystem());
        this.getChunkStoreRegistry().registerSystem((ISystem)new SignalUpdateSystem());
        this.getChunkStoreRegistry().registerSystem((ISystem)new SignalRecoverSystem());
        this.getChunkStoreRegistry().registerSystem((ISystem)new RelaySystem());
        this.getChunkStoreRegistry().registerSystem((ISystem)new InverterSystem());
        this.getChunkStoreRegistry().registerSystem((ISystem)new TeleporterSystem());
        this.getEntityStoreRegistry().registerSystem((ISystem)new EntityCountSystem());
        MimicPlaceSystems.register((ComponentRegistryProxy<EntityStore>)this.getEntityStoreRegistry());
        GhostBlockSystems.register((ComponentRegistryProxy<ChunkStore>)this.getChunkStoreRegistry());
        FlopperSystem.register((ComponentRegistryProxy<ChunkStore>)this.getChunkStoreRegistry());
        SignalSystems.register((ComponentRegistryProxy<ChunkStore>)this.getChunkStoreRegistry());
        this.getCodecRegistry(Interaction.CODEC).register("LinkItem", LinkItemInteraction.class, LinkItemInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("TeleporterLinkItem", TeleporterLinkInteraction.class, TeleporterLinkInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("ViewLinks", ViewLinksInteraction.class, ViewLinksInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("MakeMimic", MakeMimicInteraction.class, MakeMimicInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("MimicInteract", MimicInteractInteraction.class, MimicInteractInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("ViewMimic", ViewMimicsInteraction.class, ViewMimicsInteraction.CODEC);
        PluginBase uiPlug = PluginManager.get().getPlugin(PluginIdentifier.fromString((String)"Ellie:HyUI"));
        if (uiPlug != null && uiPlug.isEnabled()) {
            this.getCodecRegistry((StringCodecMapCodec)OpenCustomUIInteraction.PAGE_CODEC).register("PlayerDetector", PlayerDetectorUI.class, (Codec)BuilderCodec.builder(PlayerDetectorUI.class, PlayerDetectorUI::new).build());
            this.getCodecRegistry((StringCodecMapCodec)OpenCustomUIInteraction.PAGE_CODEC).register("PlayerStart", PlayerStartUI.class, (Codec)BuilderCodec.builder(PlayerStartUI.class, PlayerStartUI::new).build());
            this.getCodecRegistry((StringCodecMapCodec)OpenCustomUIInteraction.PAGE_CODEC).register("MobSpawner", MobSpawnerUI.class, (Codec)BuilderCodec.builder(MobSpawnerUI.class, MobSpawnerUI::new).build());
            this.getCodecRegistry((StringCodecMapCodec)OpenCustomUIInteraction.PAGE_CODEC).register("ItemSpawner", ItemSpawnerUI.class, (Codec)BuilderCodec.builder(ItemSpawnerUI.class, ItemSpawnerUI::new).build());
            this.getCodecRegistry((StringCodecMapCodec)OpenCustomUIInteraction.PAGE_CODEC).register("RelayBlock", RelayPage.class, (Codec)BuilderCodec.builder(RelayPage.class, RelayPage::new).build());
            this.getCodecRegistry((StringCodecMapCodec)OpenCustomUIInteraction.PAGE_CODEC).register("Flopper", FlopperUI.class, (Codec)BuilderCodec.builder(FlopperUI.class, FlopperUI::new).build());
            this.getCodecRegistry((StringCodecMapCodec)OpenCustomUIInteraction.PAGE_CODEC).register("Custom_Teleporter", TeleporterUI.class, (Codec)BuilderCodec.builder(TeleporterUI.class, TeleporterUI::new).build());
        }
        this.getCommandRegistry().registerCommand((AbstractCommand)new TestStartCommand());
        DungeonChunkData.setComponentType((ComponentType<ChunkStore, DungeonChunkData>)this.getChunkStoreRegistry().registerComponent(DungeonChunkData.class, "DungeonChunkData", DungeonChunkData.CODEC));
        this.getChunkStoreRegistry().registerSystem((ISystem)new DungeonChunkData.DungeonChunkDataLoadingSystem());
        this.getChunkStoreRegistry().registerSystem((ISystem)new DungeonChunkData.LoadBlockComponentPacketSystem(DungeonChunkData.getComponentType()));
        this.getChunkStoreRegistry().registerSystem((ISystem)new DungeonChunkData.UnloadBlockComponentPacketSystem(DungeonChunkData.getComponentType()));
        this.getCommandRegistry().registerCommand((AbstractCommand)new DungeonDebugCommand());
        logger.info("Plugin Setup!");
    }
}
