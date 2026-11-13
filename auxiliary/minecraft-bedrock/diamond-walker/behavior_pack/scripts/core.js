export const ENABLED_PROPERTY = "raynor:diamond_walker_enabled";

export function blockUnderFeet(location) {
  return {
    x: Math.floor(location.x),
    y: Math.floor(location.y - 0.1),
    z: Math.floor(location.z),
  };
}

export function shouldReplace(block) {
  return !!block && !block.isAir && block.typeId !== "minecraft:diamond_block";
}
