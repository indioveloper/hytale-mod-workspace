export const BUCKET_ITEM = "raynor:mob_bucket";
export const MOB_PROPERTY = "raynor:mob_type";

const FORBIDDEN = new Set([
  "minecraft:player", "minecraft:armor_stand", "minecraft:npc", "minecraft:agent",
  "minecraft:ender_dragon", "minecraft:wither", "minecraft:warden"
]);

export function mobLabel(typeId) {
  return typeId.replace(/^minecraft:/, "").split("_")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1)).join(" ");
}

export function captureProblem(target) {
  if (FORBIDDEN.has(target.typeId)) return "Esta criatura no se puede guardar en el cubo.";
  if (!target.getComponent("minecraft:health")) return "Solo se pueden capturar criaturas vivas.";
  if (target.nameTag) return "No se capturan criaturas con nombre para no perder sus datos.";
  const tameable = target.getComponent("minecraft:tameable");
  if (tameable?.isTamed) return "Las mascotas domesticadas no se pueden capturar.";
  const inventory = target.getComponent("minecraft:inventory");
  if (inventory?.container?.size > 0) return "Las criaturas con inventario no se pueden capturar.";
  return undefined;
}

export function isFilled(item) {
  return typeof item?.getDynamicProperty(MOB_PROPERTY) === "string";
}
