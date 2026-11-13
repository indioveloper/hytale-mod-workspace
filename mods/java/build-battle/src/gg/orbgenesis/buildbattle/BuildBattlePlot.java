package gg.orbgenesis.buildbattle;

import com.hypixel.hytale.math.vector.Rotation3f;
import org.joml.Vector3d;
import org.joml.Vector3i;

public record BuildBattlePlot(
    String id,
    String world,
    Vector3i minimum,
    Vector3i maximum,
    Vector3d spawn,
    Vector3d viewingPosition,
    float yaw,
    String resetPrefab,
    Vector3i resetOrigin) {

  public boolean contains(String worldName, Vector3i position) {
    return world.equalsIgnoreCase(worldName)
        && position.x >= minimum.x
        && position.y >= minimum.y
        && position.z >= minimum.z
        && position.x <= maximum.x
        && position.y <= maximum.y
        && position.z <= maximum.z;
  }

  public Rotation3f rotation() {
    return new Rotation3f(0.0f, yaw, 0.0f);
  }
}
