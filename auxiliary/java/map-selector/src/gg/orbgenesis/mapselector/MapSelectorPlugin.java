package gg.orbgenesis.mapselector;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MapSelectorPlugin extends JavaPlugin {
  private static MapSelectorPlugin instance;

  private final Map<UUID, MapDefinition> selectedMaps = new ConcurrentHashMap<>();

  public MapSelectorPlugin(JavaPluginInit init) {
    super(init);
    instance = this;
  }

  public static MapSelectorPlugin get() {
    return instance;
  }

  @Override
  protected void setup() {
    super.setup();
    getCommandRegistry().registerCommand(new MapsCommand(this));
  }

  public MapDefinition getSelectedMap(UUID playerId) {
    return selectedMaps.getOrDefault(playerId, MapDefinition.MAP_1);
  }

  public void selectMap(UUID playerId, MapDefinition map) {
    selectedMaps.put(playerId, map);
  }
}
