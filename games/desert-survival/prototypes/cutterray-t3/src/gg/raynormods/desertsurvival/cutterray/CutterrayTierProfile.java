package gg.raynormods.desertsurvival.cutterray;

import com.hypixel.hytale.server.core.inventory.ItemStack;

/** Provisional shared timing, damage and yield curve for the six cutterray tiers. */
enum CutterrayTierProfile {
  TIER_1(1, 10, 1, 1, 2),
  TIER_2(2, 20, 1, 2, 3),
  TIER_3(3, 30, 2, 2, 4),
  TIER_4(4, 40, 2, 4, 6),
  TIER_5(5, 50, 3, 5, 7),
  TIER_6(6, 60, 4, 6, 8);

  final int tier;
  final int damagePerSecond;
  final int pulseYield;
  final int finalYieldMin;
  final int finalYieldMax;

  CutterrayTierProfile(
      int tier,
      int damagePerSecond,
      int pulseYield,
      int finalYieldMin,
      int finalYieldMax) {
    this.tier = tier;
    this.damagePerSecond = damagePerSecond;
    this.pulseYield = pulseYield;
    this.finalYieldMin = finalYieldMin;
    this.finalYieldMax = finalYieldMax;
  }

  static CutterrayTierProfile fromItem(ItemStack itemStack) {
    String itemId = itemStack == null ? null : itemStack.getItemId();
    if (itemId != null) {
      for (CutterrayTierProfile profile : values()) {
        if (itemId.contains("_T" + profile.tier + "_")) {
          return profile;
        }
      }
    }
    return TIER_1;
  }
}
