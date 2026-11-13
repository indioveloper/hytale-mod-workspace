package gg.raynormods.cinema;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public final class RaynorCinemaPlugin extends JavaPlugin {
    public static final String VERSION = "0.1.0";

    private final CinemaSessionManager sessions = new CinemaSessionManager();

    public RaynorCinemaPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        getCommandRegistry().registerCommand((AbstractCommand) new CinemaCommand(sessions));
        getLogger().at(Level.INFO).log(
            "Raynor Cinema %s loaded: synchronized session spike; YouTube playback requires the official player.",
            VERSION);
    }
}
