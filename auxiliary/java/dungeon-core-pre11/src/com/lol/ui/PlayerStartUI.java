/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  au.ellie.hyui.builders.PageBuilder
 *  au.ellie.hyui.builders.TextFieldBuilder
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

import au.ellie.hyui.builders.PageBuilder;
import au.ellie.hyui.builders.TextFieldBuilder;
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
import com.lol.components.PlayerStart;
import com.lol.utils.BlockHelper;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class PlayerStartUI
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
        PlayerStart ps = (PlayerStart)store.getComponent(blockRef, PlayerStart.getComponentType());
        if (ps == null) {
            return null;
        }
        PageBuilder page = (PageBuilder)PageBuilder.pageForPlayer((PlayerRef)playerRef).loadHtml("PlayerStart.html");
        page.getById("title", TextFieldBuilder.class).ifPresent(title -> {
            title.withValue(ps.bannerTitle);
            title.addEventListener(CustomUIEventBindingType.ValueChanged, val -> {
                ps.bannerTitle = val;
            });
        });
        page.getById("message", TextFieldBuilder.class).ifPresent(message -> {
            message.withValue(ps.bannerMessage);
            message.addEventListener(CustomUIEventBindingType.ValueChanged, val -> {
                ps.bannerMessage = val;
            });
        });
        return page.open(((EntityStore)componentAccessor.getExternalData()).getStore());
    }
}
