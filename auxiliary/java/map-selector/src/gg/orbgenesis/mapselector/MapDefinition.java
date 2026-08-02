package gg.orbgenesis.mapselector;

public enum MapDefinition {
  MAP_1("mapa 1", "mapa 1.prefab.json", 100.0, 100.0, 100.0),
  MAP_2("mapa 2", "mapa 2.prefab.json", 200.0, 100.0, 200.0);

  private final String displayName;
  private final String prefabPath;
  private final double destinationX;
  private final double destinationY;
  private final double destinationZ;

  MapDefinition(
      String displayName,
      String prefabPath,
      double destinationX,
      double destinationY,
      double destinationZ) {
    this.displayName = displayName;
    this.prefabPath = prefabPath;
    this.destinationX = destinationX;
    this.destinationY = destinationY;
    this.destinationZ = destinationZ;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getPrefabPath() {
    return prefabPath;
  }

  public double getDestinationX() {
    return destinationX;
  }

  public double getDestinationY() {
    return destinationY;
  }

  public double getDestinationZ() {
    return destinationZ;
  }

  public static MapDefinition fromEventValue(String value) {
    if (value == null) {
      return null;
    }

    for (MapDefinition map : values()) {
      if (map.name().equals(value)) {
        return map;
      }
    }
    return null;
  }
}
