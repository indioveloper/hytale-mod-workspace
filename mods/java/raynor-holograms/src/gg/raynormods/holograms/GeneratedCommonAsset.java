/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.server.core.asset.common.CommonAsset
 *  javax.annotation.Nonnull
 */
package gg.raynormods.holograms;

import com.hypixel.hytale.server.core.asset.common.CommonAsset;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

final class GeneratedCommonAsset
extends CommonAsset {
    private final byte[] bytes;

    GeneratedCommonAsset(String string, byte[] byArray) {
        super(string, byArray);
        this.bytes = (byte[])byArray.clone();
    }

    @Nonnull
    protected CompletableFuture<byte[]> getBlob0() {
        return CompletableFuture.completedFuture(this.bytes);
    }
}

