/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.component.CommandBuffer
 *  com.hypixel.hytale.component.system.EcsEvent
 *  com.hypixel.hytale.math.vector.Vector3i
 *  com.hypixel.hytale.server.core.universe.world.storage.ChunkStore
 */
package com.lol.events;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.system.EcsEvent;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.lol.components.SignalSender;

public class SignalEvent
extends EcsEvent {
    public boolean signal;

    public static void sendEvent(boolean signal, SignalSender sender, Vector3i pos, CommandBuffer<ChunkStore> commandBuffer) {
    }
}

