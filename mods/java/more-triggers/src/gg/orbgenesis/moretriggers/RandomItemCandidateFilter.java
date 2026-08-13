package gg.orbgenesis.moretriggers;

import java.util.Set;
import java.util.regex.Pattern;

/** Defines the survival-friendly creative-library pool used by GiveRandomItem. */
final class RandomItemCandidateFilter {
  private static final Set<String> BLOCK_CATEGORIES =
      Set.of(
          "Blocks.Rocks",
          "Blocks.Wood",
          "Blocks.Metal",
          "Blocks.Cloth",
          "Blocks.Soils",
          "Blocks.Deco",
          "Furniture.Benches",
          "Furniture.Containers",
          "Furniture.Furniture",
          "Furniture.Doors",
          "Furniture.Lighting",
          "Furniture.Beds",
          "Furniture.Shelves",
          "Furniture.Signs");

  private static final Set<String> DENIED_CATEGORIES =
      Set.of(
          "Blocks.Plants",
          "Blocks.Ores",
          "Blocks.Fluids",
          "Blocks.Portals",
          "Items.Debug",
          "Tool",
          "Tool.BuilderTool",
          "Tool.BuilderToolSecondPage",
          "Tool.BrushFilters",
          "Tool.PrefabEditing",
          "Tool.Machinima",
          "Tool.TechnicalBlocks");

  private static final Pattern DENIED_ID_TOKEN =
      Pattern.compile(
          "(?:^|_)(?:debug|qa|test|example|template|prototype|filter|editor|technical|portal|spawn|placeholder|ore|trap|explosive)(?:_|$)",
          Pattern.CASE_INSENSITIVE);

  private static final Pattern PLANT_ID_TOKEN =
      Pattern.compile(
          "(?:^|_)(?:plant|crop|seed|sapling|tree|trunk|branch|root|roots|bush|flower|grass|leaf|leaves|vine|mushroom|cactus|coral|kelp|seaweed)(?:_|$)",
          Pattern.CASE_INSENSITIVE);

  private RandomItemCandidateFilter() {}

  static boolean isEligible(
      String itemId,
      String[] categories,
      String subCategory,
      boolean hasBlockType,
      boolean hasWeapon,
      boolean hasTool,
      boolean hasBuilderTool,
      boolean hasBlockSelectorTool,
      boolean variant) {
    if (itemId == null
        || itemId.isBlank()
        || categories == null
        || categories.length == 0
        || variant
        || hasBuilderTool
        || hasBlockSelectorTool
        || DENIED_ID_TOKEN.matcher(itemId).find()
        || PLANT_ID_TOKEN.matcher(itemId).find()
        || "Trees".equalsIgnoreCase(subCategory)) {
      return false;
    }

    boolean allowedBlockCategory = false;
    boolean weaponCategory = false;
    boolean toolCategory = false;
    for (String category : categories) {
      if (category == null || category.isBlank()) {
        continue;
      }
      if (DENIED_CATEGORIES.contains(category) || category.startsWith("Tool.")) {
        return false;
      }
      allowedBlockCategory |= BLOCK_CATEGORIES.contains(category);
      weaponCategory |= "Items.Weapons".equals(category);
      toolCategory |= "Items.Tools".equals(category);
    }

    return (hasBlockType && allowedBlockCategory)
        || (hasWeapon && weaponCategory)
        || (hasTool && toolCategory);
  }
}
