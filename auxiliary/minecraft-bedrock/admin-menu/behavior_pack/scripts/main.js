import { EnchantmentTypes, EffectTypes, EntityComponentTypes, EquipmentSlot,
  GameMode, ItemComponentTypes, system, world } from "@minecraft/server";
import { ActionFormData } from "@minecraft/server-ui";
import { ICONS, EFFECTS, BENEFICIAL_EFFECTS, CURSES, INSTANT, shortId, enchantName, enchantIcon, effectName } from "./catalog.js";
import { ITEM_ICONS, EFFECT_ICONS } from "./vanilla-icons.js";

const MENU_ITEM = "raynor:admin_menu";
const SETTINGS = "raynor:menu_effects_v2";
const openPlayers = new Set();
const ARMOR = [[EquipmentSlot.Head, "Cabeza"], [EquipmentSlot.Chest, "Pecho"],
  [EquipmentSlot.Legs, "Piernas"], [EquipmentSlot.Feet, "Pies"], [EquipmentSlot.Offhand, "Mano secundaria"]];

system.beforeEvents.startup.subscribe(({ itemComponentRegistry }) => {
  itemComponentRegistry.registerCustomComponent("raynor:open_admin_menu", {
    onUse({ source }) {
      if (source.typeId === "minecraft:player") system.run(() => runMenu(source));
    },
  });
});
world.afterEvents.playerLeave.subscribe(({ playerId }) => openPlayers.delete(playerId));

// Instant effects have no native duration. Persist only their opted-in repetition.
system.runInterval(() => {
  for (const player of world.getAllPlayers()) {
    const state = readSettings(player);
    for (const id of INSTANT) {
      if (!state[id]?.enabled) continue;
      try { player.addEffect(id, 1, { amplifier: state[id].level - 1, showParticles: false }); }
      catch (error) { console.warn(`[Menu magico] ${id}: ${error}`); }
    }
  }
}, 20);

function readSettings(player) {
  try { return JSON.parse(String(player.getDynamicProperty(SETTINGS) ?? "{}")); }
  catch { return {}; }
}
function effectState(player, id) {
  const saved = readSettings(player)[id];
  const active = player.getEffect(id);
  const enabled = INSTANT.has(id) ? !!saved?.enabled : !!active;
  return { enabled, level: enabled ? (INSTANT.has(id) ? saved.level : active.amplifier + 1) : 0 };
}
function setEffect(player, id, enabled, level) {
  if (!BENEFICIAL_EFFECTS.has(id)) throw new Error("Este efecto no es beneficioso y no esta disponible.");
  level = enabled ? Math.max(0, Math.min(EFFECTS[id]?.[1] ? 256 : 1, level)) : 0;
  enabled = level > 0;
  if (!INSTANT.has(id)) {
    if (enabled) {
      // IDs originate exclusively from EffectTypes, never from user text.
      if (!/^[a-z0-9_]+$/.test(id)) throw new Error("ID de efecto no valido");
      player.removeEffect(id); // Lowering the amplifier must also take effect immediately.
      player.runCommand(`effect @s ${id} infinite ${level - 1} true`);
    } else player.removeEffect(id);
  } else if (!enabled) player.removeEffect(id);
  else player.addEffect(id, 1, { amplifier: level - 1, showParticles: false });
  const settings = readSettings(player);
  settings[id] = { enabled, level };
  player.setDynamicProperty(SETTINGS, JSON.stringify(settings));
}
function clearEffects(player) {
  const settings = readSettings(player);
  for (const state of Object.values(settings)) state.enabled = false;
  player.setDynamicProperty(SETTINGS, JSON.stringify(settings));
  for (const effect of player.getEffects()) player.removeEffect(effect.typeId);
}
function itemIcon(item) {
  const id = shortId(item.typeId);
  const alias = { bow: "bow_standby", crossbow: "crossbow_standby", fishing_rod: "fishing_rod_uncast",
    shield: "empty_armor_slot_shield", enchanted_book: "book_enchanted" };
  const name = alias[id] ?? id.replace(/^wooden_/, "wood_").replace(/^golden_/, "gold_");
  return ITEM_ICONS[name] ?? ICONS.book;
}
function itemName(item) {
  return item.nameTag ? { text: item.nameTag } : { translate: item.localizationKey };
}
function itemLabel(item, prefix = "") {
  return { rawtext: [{ text: prefix }, itemName(item)] };
}
async function choose(player, title, body, buttons) {
  const form = new ActionFormData().title(title).body(body);
  for (const [label, icon] of buttons) form.button(label, icon);
  const response = await form.show(player);
  return response.canceled ? undefined : response.selection;
}

async function runMenu(player) {
  if (openPlayers.has(player.id)) return;
  openPlayers.add(player.id);
  try {
    while (true) {
      const choice = await choose(player, "Menu magico v1.2.0", "Elige una opcion", [
        ["Modo de juego", ICONS.sword], ["Efectos de pocion", ICONS.potion],
        ["Encantar objeto", ICONS.table], ["Quitar todos los efectos", ICONS.milk],
      ]);
      if (choice === undefined) return;
      if (choice === 0 && !(await gameModes(player))) return;
      if (choice === 1 && !(await effectsMenu(player))) return;
      if (choice === 2 && !(await inventoryMenu(player))) return;
      if (choice === 3) { clearEffects(player); player.sendMessage("§aTodos los efectos desactivados."); }
    }
  } catch (error) {
    try { player.sendMessage(`§cNo se pudo completar la accion: ${error.message ?? error}`); } catch {}
    console.warn(`[Menu magico] ${error}`);
  } finally { openPlayers.delete(player.id); }
}
async function gameModes(player) {
  const choice = await choose(player, "Modo de juego", "Selecciona un modo", [
    ["Supervivencia", ICONS.sword], ["Creativo", ICONS.crafting], ["Espectador", ICONS.star], ["Volver", ICONS.back],
  ]);
  if (choice === undefined) return false;
  if (choice < 3) player.setGameMode([GameMode.Survival, GameMode.Creative, GameMode.Spectator][choice]);
  return true;
}
async function effectsMenu(player) {
  const types = EffectTypes.getAll().map(type => shortId(type.getName()))
    .filter(id => BENEFICIAL_EFFECTS.has(id))
    .sort((a, b) => String(effectName(a)).localeCompare(String(effectName(b)), "es"));
  while (true) {
    const buttons = types.map(id => {
      const state = effectState(player, id);
      return [`${effectName(id)}\n${state.enabled ? "§aACTIVO" : "§7INACTIVO"}${EFFECTS[id]?.[1] ? ` - Nivel ${state.level}` : ""}`,
        EFFECT_ICONS[id] ?? EFFECTS[id]?.[2] ?? ICONS.potion];
    });
    buttons.push(["Volver", ICONS.back]);
    const choice = await choose(player, "Efectos beneficiosos", "Solo beneficios. + aplica al instante; nivel 0 lo quita. Sin particulas.", buttons);
    if (choice === undefined) return false;
    if (choice === types.length) return true;
    if (!(await effectEditor(player, types[choice]))) return false;
  }
}
async function effectEditor(player, id) {
  while (true) {
    const state = effectState(player, id);
    const scaled = EFFECTS[id]?.[1] ?? false;
    const description = INSTANT.has(id)
      ? "Se aplica al instante y se repite cada segundo. Nivel 0 lo quita."
      : "Duracion: infinita. Sin particulas. Puede terminar al morir, beber leche o por una mecanica del juego.";
    const body = `${description}\n\nEstado: ${state.enabled ? "ACTIVO" : "INACTIVO"}\nNivel: ${state.level}\nLos cambios se aplican al pulsar. Nivel 0 lo quita.`;
    const buttons = [[scaled ? "+ Aumentar nivel" : "+ Aplicar nivel 1", ICONS.plus],
      [scaled ? "- Reducir nivel" : "- Quitar efecto", ICONS.minus]];
    if (scaled) buttons.push(["Quitar efecto", ICONS.off]);
    buttons.push(["Volver", ICONS.back]);
    const choice = await choose(player, effectName(id), body, buttons);
    if (choice === undefined) return false;
    if (choice === buttons.length - 1) return true;
    const nextLevel = choice === 2 ? 0 : state.level + (choice === 0 ? 1 : -1);
    setEffect(player, id, nextLevel > 0, nextLevel);
  }
}

function inventoryTargets(player) {
  const result = [];
  const inventory = player.getComponent(EntityComponentTypes.Inventory)?.container;
  if (inventory) for (let i = 0; i < inventory.size; i++) {
    const slot = inventory.getSlot(i), item = slot.getItem();
    if (item?.typeId !== MENU_ITEM && item?.getComponent(ItemComponentTypes.Enchantable))
      result.push({ slot, item, label: `Casilla ${i + 1}: ` });
  }
  const equipment = player.getComponent(EntityComponentTypes.Equippable);
  if (equipment) for (const [key, label] of ARMOR) {
    const slot = equipment.getEquipmentSlot(key), item = slot.getItem();
    if (item?.getComponent(ItemComponentTypes.Enchantable)) result.push({ slot, item, label: `${label}: ` });
  }
  return result;
}
function readTarget(target) {
  const current = target.slot.getItem();
  if (!current || itemFingerprint(current) !== itemFingerprint(target.item))
    throw new Error("El objeto ha cambiado. Abre de nuevo el menu para no modificar otro objeto.");
  return current;
}
function itemFingerprint(item) {
  const enchantments = item.getComponent(ItemComponentTypes.Enchantable)?.getEnchantments()
    .map(entry => [entry.type.id, entry.level]).sort() ?? [];
  const properties = item.getDynamicPropertyIds().sort().map(id => [id, item.getDynamicProperty(id)]);
  return JSON.stringify([item.typeId, item.amount, item.nameTag, item.getLore(), enchantments,
    item.getComponent(ItemComponentTypes.Durability)?.damage, properties,
    item.keepOnDeath, item.lockMode, item.getCanDestroy(), item.getCanPlaceOn()]);
}
async function inventoryMenu(player) {
  while (true) {
    const targets = inventoryTargets(player);
    const buttons = targets.map(target => [itemLabel(target.item, target.label), itemIcon(target.item)]);
    buttons.push(["Volver", ICONS.back]);
    const choice = await choose(player, "Objetos encantables", targets.length
      ? "Inventario, armadura equipada y mano secundaria." : "No tienes objetos encantables.", buttons);
    if (choice === undefined) return false;
    if (choice === targets.length) return true;
    if (!(await enchantmentsMenu(player, targets[choice]))) return false;
  }
}
function compatibleEnchantments(item) {
  const component = item.getComponent(ItemComponentTypes.Enchantable);
  return EnchantmentTypes.getAll().filter(type => {
    if (CURSES.has(shortId(type.id))) return false;
    if (component.hasEnchantment(type)) return true;
    try { return component.canAddEnchantment({ type, level: 1 }); } catch { return false; }
  }).sort((a, b) => enchantName(a.id).localeCompare(enchantName(b.id), "es"));
}
async function enchantmentsMenu(player, target) {
  while (true) {
    const item = readTarget(target);
    const component = item.getComponent(ItemComponentTypes.Enchantable);
    const types = compatibleEnchantments(item);
    const buttons = types.map(type => {
      const level = component.getEnchantment(type)?.level ?? 0;
      return [`${enchantName(type.id)}\n${level ? `§aNivel ${level}` : "§7Sin aplicar"}`, enchantIcon(type.id)];
    });
    buttons.push(["Volver", ICONS.back]);
    const choice = await choose(player, "Encantamientos compatibles", itemLabel(item), buttons);
    if (choice === undefined) return false;
    if (choice === types.length) return true;
    if (!(await enchantEditor(player, target, types[choice]))) return false;
  }
}
function changeEnchantment(target, type, level) {
  if (CURSES.has(shortId(type.id))) throw new Error("Las maldiciones no estan disponibles.");
  const item = readTarget(target), component = item.getComponent(ItemComponentTypes.Enchantable);
  level = Math.max(0, Math.min(type.maxLevel, level));
  component.removeEnchantment(type);
  if (level) component.addEnchantment({ type, level });
  target.slot.setItem(item);
  target.item = item.clone(); // Refresh the snapshot after our own successful write.
}
async function enchantEditor(player, target, type) {
  while (true) {
    const item = readTarget(target);
    const level = item.getComponent(ItemComponentTypes.Enchantable).getEnchantment(type)?.level ?? 0;
    const buttons = type.maxLevel > 1
      ? [["+ Aumentar nivel", ICONS.plus], ["- Reducir nivel", ICONS.minus], ["Quitar encantamiento", ICONS.off]]
      : [["+ Aplicar nivel 1", ICONS.plus], ["- Quitar encantamiento", ICONS.minus]];
    buttons.push(["Volver", ICONS.back]);
    const choice = await choose(player, enchantName(type.id),
      `Nivel: ${level} / ${type.maxLevel}\n${level ? "Aplicado" : "Sin aplicar"}\nLos cambios se aplican al pulsar. Nivel 0 lo elimina.`, buttons);
    if (choice === undefined) return false;
    if (choice === buttons.length - 1) return true;
    changeEnchantment(target, type, type.maxLevel === 1 ? (choice === 0 ? 1 : 0)
      : choice === 2 ? 0 : level + (choice === 0 ? 1 : -1));
  }
}
