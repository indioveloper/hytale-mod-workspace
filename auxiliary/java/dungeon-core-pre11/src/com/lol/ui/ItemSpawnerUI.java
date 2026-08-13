/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  au.ellie.hyui.builders.CheckBoxBuilder
 *  au.ellie.hyui.builders.ItemGridBuilder
 *  au.ellie.hyui.builders.NumberFieldBuilder
 *  au.ellie.hyui.builders.PageBuilder
 *  au.ellie.hyui.events.DroppedEventData
 *  com.hypixel.hytale.component.ComponentAccessor
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.math.vector.Vector3i
 *  com.hypixel.hytale.protocol.BlockPosition
 *  com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType
 *  com.hypixel.hytale.server.core.entity.InteractionContext
 *  com.hypixel.hytale.server.core.entity.entities.Player
 *  com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage
 *  com.hypixel.hytale.server.core.inventory.ItemStack
 *  com.hypixel.hytale.server.core.inventory.container.ItemContainer
 *  com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction$CustomPageSupplier
 *  com.hypixel.hytale.server.core.ui.ItemGridSlot
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  org.checkerframework.checker.nullness.compatqual.NullableDecl
 */
package com.lol.ui;

import au.ellie.hyui.builders.CheckBoxBuilder;
import au.ellie.hyui.builders.ItemGridBuilder;
import au.ellie.hyui.builders.NumberFieldBuilder;
import au.ellie.hyui.builders.PageBuilder;
import au.ellie.hyui.events.DroppedEventData;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import org.joml.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.ui.ItemGridSlot;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lol.components.ItemSpawner;
import com.lol.utils.BlockHelper;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class ItemSpawnerUI
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
        ItemSpawner ms = (ItemSpawner)store.getComponent(blockRef, ItemSpawner.getComponentType());
        if (ms == null) {
            return null;
        }
        PageBuilder page = (PageBuilder)PageBuilder.pageForPlayer((PlayerRef)playerRef).loadHtml("ItemSpawner.html");
        page.getById("despawn", CheckBoxBuilder.class).ifPresent(checkbox -> {
            checkbox.withValue(ms.despawn);
            checkbox.addEventListener(CustomUIEventBindingType.ValueChanged, checked -> {
                ms.despawn = checked;
            });
        });
        page.getById("item", ItemGridBuilder.class).ifPresent(grid -> {
            if (ms.item.isEmpty()) {
                grid.addSlot(new ItemGridSlot(null));
            } else {
                grid.addSlot(new ItemGridSlot(ms.item));
            }
        });
        page.addEventListener("item", CustomUIEventBindingType.Dropped, DroppedEventData.class, (val, ctx) -> page.getById("item", ItemGridBuilder.class).ifPresent(grid -> {
            String id = val.getItemStackId();
            int quantity = val.getItemStackQuantity();
            ItemStack stack = new ItemStack(id, quantity);
            grid.updateSlot(new ItemGridSlot(stack), Integer.valueOf(0));
            ctx.updatePage(false);
            ms.item = stack;
        }));
        page.getById("inv", ItemGridBuilder.class).ifPresent(inv -> {
            Player pl = (Player)w.getEntityStore().getStore().getComponent(playerRef.getReference(), Player.getComponentType());
            if (pl == null) {
                return;
            }
            ItemContainer hotbar = pl.getInventory().getHotbar();
            for (short idx = 0; idx < hotbar.getCapacity(); idx = (short)(idx + 1)) {
                ItemStack item = hotbar.getItemStack(idx);
                if (item == null || item.isEmpty()) {
                    inv.addSlot(new ItemGridSlot(null));
                    continue;
                }
                inv.addSlot(new ItemGridSlot(new ItemStack(item.getItemId(), item.getQuantity())));
            }
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
        return page.open(((EntityStore)componentAccessor.getExternalData()).getStore());
    }
}
