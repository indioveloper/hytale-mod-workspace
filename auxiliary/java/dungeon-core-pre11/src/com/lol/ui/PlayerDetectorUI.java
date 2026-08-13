/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  au.ellie.hyui.builders.DropdownBoxBuilder
 *  au.ellie.hyui.builders.NumberFieldBuilder
 *  au.ellie.hyui.builders.PageBuilder
 *  com.hypixel.hytale.component.ComponentAccessor
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.math.vector.Vector3i
 *  com.hypixel.hytale.protocol.BlockPosition
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

import au.ellie.hyui.builders.DropdownBoxBuilder;
import au.ellie.hyui.builders.NumberFieldBuilder;
import au.ellie.hyui.builders.PageBuilder;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import org.joml.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lol.components.PlayerDetector;
import com.lol.utils.BlockHelper;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class PlayerDetectorUI
implements OpenCustomUIInteraction.CustomPageSupplier {
    @NullableDecl
    public CustomUIPage tryCreate(Ref<EntityStore> ref, ComponentAccessor<EntityStore> componentAccessor, PlayerRef playerRef, InteractionContext interactionContext) {
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
        PlayerDetector pd = (PlayerDetector)store.getComponent(blockRef, PlayerDetector.getComponentType());
        if (pd == null) {
            return null;
        }
        PageBuilder page = (PageBuilder)PageBuilder.pageForPlayer((PlayerRef)playerRef).loadHtml("PlayerDetector.html");
        ((NumberFieldBuilder)page.getById("range", NumberFieldBuilder.class).get()).withValue(pd.range);
        page.addEventListener("range", CustomUIEventBindingType.ValueChanged, (data, ctx) -> {
            if (data instanceof Double) {
                Double num = (Double)data;
                pd.range = num;
            }
        });
        page.getById("mode", DropdownBoxBuilder.class).ifPresent(mode -> {
            PlayerDetector.DetectorMode[] values;
            for (PlayerDetector.DetectorMode option : values = PlayerDetector.DetectorMode.values()) {
                mode.addEntry(option.name(), option.name());
            }
            mode.withValue(pd.mode.name());
            mode.addEventListener(CustomUIEventBindingType.ValueChanged, val -> {
                for (PlayerDetector.DetectorMode option : values) {
                    if (!option.name().equalsIgnoreCase((String)val)) continue;
                    pd.mode = option;
                    break;
                }
            });
        });
        return page.open(((EntityStore)componentAccessor.getExternalData()).getStore());
    }
}
