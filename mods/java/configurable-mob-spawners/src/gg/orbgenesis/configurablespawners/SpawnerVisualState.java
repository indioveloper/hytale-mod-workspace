package gg.orbgenesis.configurablespawners;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import org.joml.Vector3i;

final class SpawnerVisualState {
  private static final String ENABLED_STATE = "On";
  private static final String DISABLED_STATE = "Off";

  private SpawnerVisualState() {}

  static boolean synchronize(World world, Vector3i position, boolean enabled) {
    BlockType current = world.getBlockType(position);
    if (current == null) return false;

    String desired = enabled ? ENABLED_STATE : DISABLED_STATE;
    if (desired.equals(current.getCurrentInteractionState())) return false;

    world.setBlockInteractionState(position, current, desired);
    return true;
  }
}
