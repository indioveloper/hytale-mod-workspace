package gg.orbgenesis.moretriggers;

public final class TestRandomItemCandidateFilter {
  private TestRandomItemCandidateFilter() {}

  public static void main(String[] args) {
    assertEligible("Rock_Stone_Bricks", categories("Blocks.Rocks"), null, true, false, false);
    assertEligible("Wood_Oak_Planks", categories("Blocks.Wood"), "BlockSets", true, false, false);
    assertEligible("Bench_WorkBench", categories("Furniture.Benches"), null, true, false, false);
    assertEligible("Furniture_Crude_Chair", categories("Furniture.Furniture"), null, true, false, false);
    assertEligible("Weapon_Iron_Sword", categories("Items.Weapons"), null, false, true, false);
    assertEligible("Tool_Iron_Pickaxe", categories("Items.Tools"), null, false, false, true);

    assertRejected("Plant_Fern", categories("Blocks.Plants"), null, true, false, false);
    assertRejected("Plant_Fern_Trunk", categories("Blocks.Wood"), "Trees", true, false, false);
    assertRejected("Soil_Grass", categories("Blocks.Soils"), null, true, false, false);
    assertRejected("Ore_Adamantite", categories("Blocks.Ores"), null, true, false, false);
    assertRejected("Ore_Cobalt_Slate_Cracked", categories("Blocks.Rocks"), null, true, false, false);
    assertRejected("Portal_Return", categories("Blocks.Portals"), null, true, false, false);
    assertRejected("Build_Black_Cube", categories("Tool.TechnicalBlocks"), null, true, false, false);
    assertRejected("Debug_Stick", categories("Items.Weapons"), null, false, true, false);
    assertRejected("Weapon_Test_Zoom_Rifle", categories("Items.Weapons"), null, false, true, false);
    assertRejected("Spawn_Portal", categories("Blocks.Deco"), null, true, false, false);
    assertRejected("Prototype_Rock_Slab", categories("Blocks.Rocks"), null, true, false, false);
    assertRejected("Trap_Ancient_Platform", categories("Furniture.Shelves"), null, true, false, false);
    assertRejected("Weapon_Debug", categories("Items.Weapons", "Items.Debug"), null, false, true, false);
    assertRejected("EditorTool_Paint", categories("Tool.BuilderTool"), null, false, false, false, true, false, false);
    assertRejected("Selector_Block", categories("Blocks.Rocks"), null, true, false, false, false, true, false);
    assertRejected("Rock_State_Variant", categories("Blocks.Rocks"), null, true, false, false, false, false, true);
    assertRejected("Food_Bread", categories("Items.Foods"), null, false, false, false);
    assertRejected("Weapon_Without_Config", categories("Items.Weapons"), null, false, false, false);
    assertRejected("Uncategorised_Block", null, null, true, false, false);

    System.out.println("Random item candidate filter tests passed.");
  }

  private static String[] categories(String... values) {
    return values;
  }

  private static void assertEligible(
      String id,
      String[] categories,
      String subCategory,
      boolean block,
      boolean weapon,
      boolean tool) {
    assertValue(true, id, categories, subCategory, block, weapon, tool, false, false, false);
  }

  private static void assertRejected(
      String id,
      String[] categories,
      String subCategory,
      boolean block,
      boolean weapon,
      boolean tool) {
    assertValue(false, id, categories, subCategory, block, weapon, tool, false, false, false);
  }

  private static void assertRejected(
      String id,
      String[] categories,
      String subCategory,
      boolean block,
      boolean weapon,
      boolean tool,
      boolean builderTool,
      boolean selectorTool,
      boolean variant) {
    assertValue(
        false,
        id,
        categories,
        subCategory,
        block,
        weapon,
        tool,
        builderTool,
        selectorTool,
        variant);
  }

  private static void assertValue(
      boolean expected,
      String id,
      String[] categories,
      String subCategory,
      boolean block,
      boolean weapon,
      boolean tool,
      boolean builderTool,
      boolean selectorTool,
      boolean variant) {
    boolean actual =
        RandomItemCandidateFilter.isEligible(
            id,
            categories,
            subCategory,
            block,
            weapon,
            tool,
            builderTool,
            selectorTool,
            variant);
    if (actual != expected) {
      throw new AssertionError("Expected " + expected + " for " + id + " but got " + actual);
    }
  }
}
