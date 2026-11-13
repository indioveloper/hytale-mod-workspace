import { EntityComponentTypes, EquipmentSlot, ItemStack, system, world } from "@minecraft/server";
import { BUCKET_ITEM, MOB_PROPERTY, captureProblem, isFilled, mobLabel } from "./core.js";

const recentlyCaptured = new Map();

system.beforeEvents.startup.subscribe(({ itemComponentRegistry }) => {
  itemComponentRegistry.registerCustomComponent("raynor:release_mob", {
    onUse({ source }) {
      if (source.typeId !== "minecraft:player") return;
      if ((recentlyCaptured.get(source.id) ?? -10) >= system.currentTick - 1) return;
      system.run(() => releaseMob(source));
    },
  });
});

world.beforeEvents.playerInteractWithEntity.subscribe((event) => {
  if (event.itemStack?.typeId !== BUCKET_ITEM) return;
  event.cancel = true;
  const { player, target } = event;
  system.run(() => captureMob(player, target));
});

world.afterEvents.playerLeave.subscribe(({ playerId }) => recentlyCaptured.delete(playerId));

function mainhand(player) {
  return player.getComponent(EntityComponentTypes.Equippable)?.getEquipmentSlot(EquipmentSlot.Mainhand);
}

function captureMob(player, target) {
  try {
    const slot = mainhand(player);
    const emptyBucket = slot?.getItem();
    if (!emptyBucket || emptyBucket.typeId !== BUCKET_ITEM) return;
    if (isFilled(emptyBucket)) {
      player.sendMessage("§eEste cubo ya contiene una criatura.");
      return;
    }

    const problem = captureProblem(target);
    if (problem) {
      player.sendMessage(`§c${problem}`);
      return;
    }

    const typeId = target.typeId;
    const filledBucket = emptyBucket.clone();
    filledBucket.setDynamicProperty(MOB_PROPERTY, typeId);
    filledBucket.setLore([
      `§7Contiene: §f${mobLabel(typeId)}`,
      "§eUsa el cubo para liberar la criatura"
    ]);

    slot.setItem(filledBucket);
    try {
      target.remove();
    } catch (error) {
      slot.setItem(emptyBucket);
      throw error;
    }
    recentlyCaptured.set(player.id, system.currentTick);
    player.sendMessage(`§a${mobLabel(typeId)} guardado en el cubo.`);
  } catch (error) {
    console.warn(`[Cubos para mobs] No se pudo capturar: ${error}`);
    player.sendMessage("§cNo se ha podido capturar esta criatura.");
  }
}

function releaseMob(player) {
  let spawned;
  try {
    const slot = mainhand(player);
    const filledBucket = slot?.getItem();
    if (!filledBucket || filledBucket.typeId !== BUCKET_ITEM) return;
    const typeId = filledBucket.getDynamicProperty(MOB_PROPERTY);
    if (typeof typeId !== "string") {
      player.sendMessage("§7El cubo esta vacio. Usalo sobre una criatura.");
      return;
    }

    const view = player.getViewDirection();
    const origin = player.getHeadLocation();
    const location = {
      x: origin.x + view.x * 2,
      y: Math.max(player.location.y + 0.1, origin.y + view.y * 2 - 0.7),
      z: origin.z + view.z * 2,
    };
    spawned = player.dimension.spawnEntity(typeId, location);
    slot.setItem(new ItemStack(BUCKET_ITEM, 1));
    player.sendMessage(`§a${mobLabel(typeId)} liberado.`);
  } catch (error) {
    try { spawned?.remove(); } catch {}
    console.warn(`[Cubos para mobs] No se pudo liberar: ${error}`);
    player.sendMessage("§cNo hay espacio seguro para liberar la criatura aqui.");
  }
}
