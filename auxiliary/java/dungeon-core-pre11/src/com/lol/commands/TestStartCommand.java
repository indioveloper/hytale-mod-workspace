/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.hypixel.hytale.component.Ref
 *  com.hypixel.hytale.component.Store
 *  com.hypixel.hytale.server.core.Message
 *  com.hypixel.hytale.server.core.command.system.CommandContext
 *  com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand
 *  com.hypixel.hytale.server.core.universe.PlayerRef
 *  com.hypixel.hytale.server.core.universe.world.World
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  org.checkerframework.checker.nullness.compatqual.NonNullDecl
 */
package com.lol.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lol.resources.DungeonBlocks;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class TestStartCommand
extends AbstractPlayerCommand {
    public TestStartCommand() {
        super("start_dungeon", "duh..");
    }

    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
        World w = ((EntityStore)store.getExternalData()).getWorld();
        Store chunkStore = w.getChunkStore().getStore();
        DungeonBlocks db = (DungeonBlocks)chunkStore.getResource(DungeonBlocks.getResourceType());
        boolean newState = !db.active;
        db.activate(newState, w);
        playerRef.sendMessage(Message.raw((String)("active? " + newState)));
    }
}
