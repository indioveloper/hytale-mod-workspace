/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.component.system.EcsEvent
 */
package com.lol.events;

import com.hypixel.hytale.component.system.EcsEvent;

public class ResetEvent
extends EcsEvent {
    public boolean running;

    public ResetEvent(boolean run) {
        this.running = run;
    }
}

