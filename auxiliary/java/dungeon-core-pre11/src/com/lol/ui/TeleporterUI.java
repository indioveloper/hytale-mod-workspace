/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  au.ellie.hyui.builders.CheckBoxBuilder
 *  au.ellie.hyui.builders.DropdownBoxBuilder
 *  au.ellie.hyui.builders.PageBuilder
 *  com.hypixel.hytale.component.ComponentAccessor
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.math.vector.Vector3i
 *  com.hypixel.hytale.protocol.BlockPosition
 *  com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime
 *  com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType
 *  com.hypixel.hytale.server.core.entity.InteractionContext
 *  com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage
 *  com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction$CustomPageSupplier
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  org.checkerframework.checker.nullness.compatqual.NullableDecl
 */
package com.lol.ui;

import au.ellie.hyui.builders.CheckBoxBuilder;
import au.ellie.hyui.builders.DropdownBoxBuilder;
import au.ellie.hyui.builders.PageBuilder;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import org.joml.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lol.DungeonPlugin;
import com.lol.components.TeleporterBlock;
import com.lol.utils.BlockHelper;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class TeleporterUI
implements OpenCustomUIInteraction.CustomPageSupplier {
    @NullableDecl
    public CustomUIPage tryCreate(Ref<EntityStore> ref, ComponentAccessor<EntityStore> componentAccessor, PlayerRef playerRef, InteractionContext interactionContext) {
        DungeonPlugin.logger.info("opening tp page!");
        BlockPosition pos = interactionContext.getTargetBlock();
        if (pos == null) {
            return null;
        }
        Vector3i blockPos = new Vector3i(pos.x, pos.y, pos.z);
        World w = ((EntityStore)componentAccessor.getExternalData()).getWorld();
        Ref<ChunkStore> blockRef = BlockHelper.getBlockRef(w, blockPos);
        if (blockRef == null) {
            return null;
        }
        Store store = w.getChunkStore().getStore();
        TeleporterBlock tp = (TeleporterBlock)store.getComponent(blockRef, TeleporterBlock.getComponentType());
        if (tp == null) {
            return null;
        }
        PageBuilder page = ((PageBuilder)PageBuilder.pageForPlayer((PlayerRef)playerRef).loadHtml("Teleporter.html")).withLifetime(CustomPageLifetime.CanDismiss);
        page.getById("relative", CheckBoxBuilder.class).ifPresent(checkbox -> {
            checkbox.withValue(tp.relative);
            checkbox.addEventListener(CustomUIEventBindingType.ValueChanged, enabled -> {
                tp.relative = enabled;
            });
        });
        page.getById("recv-settings", DropdownBoxBuilder.class).ifPresent(builder -> {
            if (tp.recvMode == null) {
                tp.recvMode = TeleporterBlock.RecvMode.Random;
            }
            builder.withValue(tp.recvMode.name());
            for (TeleporterBlock.RecvMode val2 : TeleporterBlock.RecvMode.values()) {
                builder.addEntry(val2.name(), val2.name());
            }
            builder.addEventListener(CustomUIEventBindingType.ValueChanged, (val, ctx) -> {
                tp.recvMode = TeleporterBlock.RecvMode.valueOf(val);
            });
        });
        page.getById("send-settings", DropdownBoxBuilder.class).ifPresent(builder -> {
            if (tp.sendMode == null) {
                tp.sendMode = TeleporterBlock.SendMode.Random;
            }
            builder.withValue(tp.sendMode.name());
            for (TeleporterBlock.SendMode val2 : TeleporterBlock.SendMode.values()) {
                builder.addEntry(val2.name(), val2.name());
            }
            builder.addEventListener(CustomUIEventBindingType.ValueChanged, (val, ctx) -> {
                tp.sendMode = TeleporterBlock.SendMode.valueOf(val);
            });
        });
        page.getById("mode", DropdownBoxBuilder.class).ifPresent(mode -> {
            if (tp.tpMode == null) {
                tp.tpMode = TeleporterBlock.TeleporterMode.SEND;
            }
            mode.withValue(tp.tpMode.name());
            for (TeleporterBlock.TeleporterMode val2 : TeleporterBlock.TeleporterMode.values()) {
                mode.addEntry(val2.name(), val2.name());
            }
            mode.addEventListener(CustomUIEventBindingType.ValueChanged, (val, ctx) -> {
                tp.tpMode = TeleporterBlock.TeleporterMode.valueOf(val);
            });
        });
        return page.open(((EntityStore)componentAccessor.getExternalData()).getStore());
    }

    record Entry(String name) {
    }
}
