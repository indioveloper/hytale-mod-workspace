import { system, world } from "@minecraft/server";
import { ENABLED_PROPERTY, blockUnderFeet, shouldReplace } from "./core.js";

const TOGGLE_TAG = "raynor_diamond_walker_toggle";

system.runInterval(() => {
  for (const player of world.getAllPlayers()) {
    if (player.hasTag(TOGGLE_TAG)) {
      player.removeTag(TOGGLE_TAG);
      toggle(player);
    }
    if (player.getDynamicProperty(ENABLED_PROPERTY) !== true) continue;
    try {
      const block = player.dimension.getBlock(blockUnderFeet(player.location));
      if (shouldReplace(block)) block.setType("minecraft:diamond_block");
    } catch (error) {
      console.warn(`[Camino de diamantes] ${player.name}: ${error}`);
    }
  }
}, 1);

function toggle(player) {
  const enabled = player.getDynamicProperty(ENABLED_PROPERTY) !== true;
  player.setDynamicProperty(ENABLED_PROPERTY, enabled);
  player.sendMessage(enabled
    ? "§aCamino de diamantes activado. Vuelve a usar /function diamantes para detenerlo."
    : "§eCamino de diamantes desactivado.");
}
