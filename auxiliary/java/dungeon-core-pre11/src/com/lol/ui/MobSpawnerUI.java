/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  au.ellie.hyui.builders.NumberFieldBuilder
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

import au.ellie.hyui.builders.NumberFieldBuilder;
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
import com.lol.components.MobSpawner;
import com.lol.utils.BlockHelper;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class MobSpawnerUI
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
        MobSpawner ms = (MobSpawner)store.getComponent(blockRef, MobSpawner.getComponentType());
        if (ms == null) {
            return null;
        }
        PageBuilder page = (PageBuilder)PageBuilder.pageForPlayer((PlayerRef)playerRef).loadHtml("MobSpawner.html");
        page.getById("role", TextFieldBuilder.class).ifPresent(role -> {
            role.withValue(ms.role);
            role.addEventListener(CustomUIEventBindingType.ValueChanged, val -> {
                ms.role = val;
            });
        });
        page.getById("range", NumberFieldBuilder.class).ifPresent(range -> {
            range.withValue(ms.range);
            range.addEventListener(CustomUIEventBindingType.ValueChanged, val -> {
                ms.range = val;
            });
        });
        page.getById("count", NumberFieldBuilder.class).ifPresent(count -> {
            count.withValue((double)ms.count);
            count.addEventListener(CustomUIEventBindingType.ValueChanged, val -> {
                ms.count = val.intValue();
            });
        });
        page.getById("health", NumberFieldBuilder.class).ifPresent(health -> {
            health.withValue((double)ms.health);
            health.addEventListener(CustomUIEventBindingType.ValueChanged, val -> {
                ms.health = val.intValue();
            });
        });
        return page.open(((EntityStore)componentAccessor.getExternalData()).getStore());
    }
}
