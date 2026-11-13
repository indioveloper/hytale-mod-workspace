package gg.raynormods.desertsurvival.research;

import java.util.List;

public record ResearchNode(
    String id,
    ResearchSector sector,
    int tier,
    String name,
    String description,
    int intelCost,
    List<String> prerequisites) {

  public ResearchNode {
    if (intelCost < 0 || intelCost > 1) {
      throw new IllegalArgumentException("MVP research costs must be 0 or 1 Intel: " + id);
    }
    prerequisites = List.copyOf(prerequisites);
  }
}
