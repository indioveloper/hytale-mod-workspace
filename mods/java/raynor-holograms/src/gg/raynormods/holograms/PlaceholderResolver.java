package gg.raynormods.holograms;

import com.hypixel.hytale.server.core.universe.world.World;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

final class PlaceholderResolver {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private PlaceholderResolver() {}

    static String resolve(String source, World world) {
        String text = source == null ? "" : source;
        ZonedDateTime now = ZonedDateTime.now();
        String online = Integer.toString(world == null ? 0 : world.getPlayerCount());
        String worldName = world == null ? "unknown" : world.getName();
        return text
            .replace("{online}", online).replace("%online%", online)
            .replace("{players}", online).replace("%players%", online)
            .replace("{world}", worldName).replace("%world%", worldName)
            .replace("{time}", TIME.format(now)).replace("%time%", TIME.format(now))
            .replace("{date}", DATE.format(now)).replace("%date%", DATE.format(now));
    }
}
