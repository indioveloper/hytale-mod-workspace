package gg.raynormods.desertsurvival.research;

import java.util.HashSet;
import java.util.Set;

public final class ResearchCatalogTest {
  private ResearchCatalogTest() {}

  public static void main(String[] args) {
    require(ResearchCatalog.all().size() == 48, "catalog must contain 48 nodes");
    Set<String> ids = new HashSet<>();
    for (ResearchNode node : ResearchCatalog.all()) {
      require(ids.add(node.id()), "duplicate ID " + node.id());
      require(node.intelCost() == 0 || node.intelCost() == 1, "invalid cost " + node.id());
      require(node.tier() >= 0 && node.tier() <= 2, "invalid tier " + node.id());
      for (String prerequisite : node.prerequisites()) {
        require(ResearchCatalog.get(prerequisite) != null,
            "missing prerequisite " + prerequisite + " for " + node.id());
      }
    }
    require(ResearchCatalog.forSector(ResearchSector.WATER).size() == 10, "water count");
    require(ResearchCatalog.forSector(ResearchSector.COMBAT).size() == 14, "combat count");
    require(ResearchCatalog.forSector(ResearchSector.CONSTRUCTION).size() == 12, "construction count");
    require(ResearchCatalog.forSector(ResearchSector.EXPLORATION).size() == 5, "exploration count");
    require(ResearchCatalog.forSector(ResearchSector.VEHICLES).size() == 7, "vehicle count");

    ResearchProgressComponent progress = ResearchProgressComponent.fresh();
    progress.addIntel(2);
    ResearchNode freeRoot = ResearchCatalog.get("AG-0.1");
    ResearchNode paidChild = ResearchCatalog.get("AG-0.2");
    require(progress.spendAndUnlock(freeRoot), "free root should unlock");
    require(progress.getAvailableIntel() == 2, "free root charged Intel");
    require(progress.hasPrerequisites(paidChild), "paid child prerequisite not satisfied");
    require(progress.spendAndUnlock(paidChild), "paid child should unlock");
    require(progress.getAvailableIntel() == 1, "paid child did not charge one Intel");
    require(!progress.spendAndUnlock(paidChild), "node charged twice");
    require(progress.getAvailableIntel() == 1, "duplicate research changed balance");

    ResearchProgressComponent materialIndependent = ResearchProgressComponent.fresh();
    materialIndependent.addIntel(2);
    ResearchNode armor0 = ResearchCatalog.get("CO-0.1");
    ResearchNode armor1 = ResearchCatalog.get("CO-1.1");
    ResearchNode armor2 = ResearchCatalog.get("CO-2.1");
    require(materialIndependent.spendAndUnlock(armor0), "tier 0 armor prerequisite");
    require(ResearchService.validate(materialIndependent, armor1) == null,
        "material tier must not block tier 1 research");
    require(materialIndependent.spendAndUnlock(armor1), "tier 1 armor unlock");
    require(ResearchService.validate(materialIndependent, armor2) == null,
        "material tier must not block tier 2 research");

    System.out.println(
        "Research prototype tests passed: 48 nodes, Intel spending and material-independent research.");
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }
}
