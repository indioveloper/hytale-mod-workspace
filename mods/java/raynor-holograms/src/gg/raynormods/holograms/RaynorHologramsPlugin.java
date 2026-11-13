/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hypixel.hytale.component.ComponentType
 *  com.hypixel.hytale.component.system.ISystem
 *  com.hypixel.hytale.server.core.command.system.AbstractCommand
 *  com.hypixel.hytale.server.core.plugin.JavaPlugin
 *  com.hypixel.hytale.server.core.plugin.JavaPluginInit
 *  com.hypixel.hytale.server.core.universe.world.storage.EntityStore
 *  javax.annotation.Nonnull
 */
package gg.raynormods.holograms;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import gg.raynormods.holograms.HologramCommand;
import gg.raynormods.holograms.HologramEntitySupportSystems;
import gg.raynormods.holograms.ImageCatalog;
import gg.raynormods.holograms.ImageHologramComponent;
import gg.raynormods.holograms.TextHologramComponent;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public final class RaynorHologramsPlugin
extends JavaPlugin {
    public static final String VERSION = "0.8.3";

    public RaynorHologramsPlugin(@Nonnull JavaPluginInit javaPluginInit) {
        super(javaPluginInit);
    }

    protected void setup() {
        HologramRuntime.initialize(this.getDataDirectory(), this.getManifest());
        ImageHologramComponent.setComponentType((ComponentType<EntityStore, ImageHologramComponent>)this.getEntityStoreRegistry().registerComponent(ImageHologramComponent.class, "Raynor_ImageHologram", ImageHologramComponent.CODEC));
        TextHologramComponent.setComponentType((ComponentType<EntityStore, TextHologramComponent>)this.getEntityStoreRegistry().registerComponent(TextHologramComponent.class, "Raynor_TextHologram", TextHologramComponent.CODEC));
        this.getEntityStoreRegistry().registerSystem(new HologramEntitySupportSystems.ImageSupport());
        this.getEntityStoreRegistry().registerSystem(new HologramEntitySupportSystems.TextSupport());
        this.getEntityStoreRegistry().registerSystem(new HologramEntitySupportSystems.ImageIdentity());
        this.getEntityStoreRegistry().registerSystem(new HologramEntitySupportSystems.TextIdentity());
        this.getCommandRegistry().registerCommand((AbstractCommand)new HologramCommand());
        this.getLogger().at(Level.INFO).log("Raynor Holograms %s loaded: adaptive rich text, remote/local images, GIF animation, placeholders, billboard modes and %d built-in images.", VERSION, ImageCatalog.entries().size());
    }
}
