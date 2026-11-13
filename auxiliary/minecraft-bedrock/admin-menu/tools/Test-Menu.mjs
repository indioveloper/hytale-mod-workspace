import assert from 'node:assert/strict';
import { readFileSync, existsSync } from 'node:fs';
import vm from 'node:vm';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import * as catalog from '../behavior_pack/scripts/catalog.js';
import * as icons from '../behavior_pack/scripts/vanilla-icons.js';
const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const source = readFileSync(resolve(root, 'behavior_pack/scripts/main.js'), 'utf8');
assert(!source.includes('ModalFormData') && !source.includes('.slider('));
const queue = [], forms = [], intervals = [];
class Form {
  constructor() { this.buttons = []; }
  title(value) { this.titleText = value; return this; }
  body(value) { this.bodyText = value; return this; }
  button(label, icon) { assert(icon, 'Every button must have an icon'); this.buttons.push([label, icon]); return this; }
  async show() { forms.push(this); const selection = queue.shift(); return { canceled: selection === undefined, selection }; }
}
const sharp = { id: 'sharpness', maxLevel: 5 }, silk = { id: 'silk_touch', maxLevel: 1 };
class Item {
  constructor(id = 'minecraft:diamond_sword') { this.typeId = id; this.amount = 1; this.levels = {}; this.damage = 0; this.nameTag = ''; this.localizationKey = 'item.diamond_sword.name'; }
  clone() { const copy = new Item(this.typeId); Object.assign(copy, this, { levels: { ...this.levels } }); return copy; }
  getLore() { return []; } getDynamicPropertyIds() { return []; }
  getCanDestroy() { return []; } getCanPlaceOn() { return []; }
  getComponent(id) {
    if (id === 'durability') return { damage: this.damage };
    if (id !== 'enchantable' || this.typeId === 'minecraft:dirt') return undefined;
    return {
      getEnchantments: () => Object.entries(this.levels).map(([id, level]) => ({ type: { id }, level })),
      hasEnchantment: type => !!this.levels[type.id],
      getEnchantment: type => this.levels[type.id] ? { type, level: this.levels[type.id] } : undefined,
      canAddEnchantment: ({ type }) => type.id === 'sharpness' && !this.levels[type.id],
      removeEnchantment: type => { delete this.levels[type.id]; },
      addEnchantment: ({ type, level }) => { assert(level > 0 && level <= type.maxLevel); this.levels[type.id] = level; },
    };
  }
}
class Slot {
  constructor(item) { this.item = item; }
  getItem() { return this.item?.clone(); }
  setItem(item) { this.item = item.clone(); }
}
const inventory = [new Slot(new Item()), new Slot(new Item('minecraft:dirt'))];
const equipped = { Head: new Slot(new Item('minecraft:diamond_helmet')), Offhand: new Slot(new Item('minecraft:shield')) };
const effects = new Map(), properties = new Map(), commands = [], applied = [];
const player = {
  id: 'test', getDynamicProperty: id => properties.get(id), setDynamicProperty: (id, value) => properties.set(id, value),
  getEffect: id => effects.get(catalog.shortId(id)), getEffects: () => [...effects].map(([typeId, effect]) => ({ typeId, ...effect })),
  removeEffect: id => effects.delete(catalog.shortId(id)),
  addEffect: (id, duration, options) => { applied.push({ id, duration, options }); },
  runCommand: command => { commands.push(command); const tokens = command.split(' '); effects.set(tokens[2], { amplifier: +tokens[4] }); },
  sendMessage() {}, setGameMode(mode) { this.mode = mode; },
  getComponent: id => id === 'inventory' ? { container: { size: inventory.length, getSlot: i => inventory[i] } }
    : id === 'equippable' ? { getEquipmentSlot: key => equipped[key] ?? new Slot() } : undefined,
};
const context = vm.createContext({ ...catalog, ...icons, console, ActionFormData: Form,
  EnchantmentTypes: { getAll: () => [sharp, silk, { id: 'binding', maxLevel: 1 }, { id: 'minecraft:vanishing', maxLevel: 1 }] },
  EffectTypes: { getAll: () => Object.keys(catalog.EFFECTS).map(id => ({ getName: () => id })) },
  EntityComponentTypes: { Inventory: 'inventory', Equippable: 'equippable' },
  ItemComponentTypes: { Enchantable: 'enchantable', Durability: 'durability' },
  EquipmentSlot: Object.fromEntries(['Head','Chest','Legs','Feet','Offhand'].map(x => [x,x])),
  GameMode: { Survival: 'survival', Creative: 'creative', Spectator: 'spectator' },
  system: { beforeEvents: { startup: { subscribe() {} } }, runInterval: fn => intervals.push(fn) },
  world: { afterEvents: { playerLeave: { subscribe() {} } }, getAllPlayers: () => [player] },
});
vm.runInContext(source.replace(/import[\s\S]*?from\s+"[^"]+";/g, '') +
  '\nglobalThis.api = {setEffect, effectState, clearEffects, effectEditor, effectsMenu, inventoryTargets, readTarget, changeEnchantment, compatibleEnchantments, enchantEditor, gameModes, itemIcon};', context);
const api = context.api;
api.setEffect(player, 'speed', true, 3);
assert.equal(commands.at(-1), 'effect @s speed infinite 2 true');
api.setEffect(player, 'speed', true, 2);
assert.equal(api.effectState(player, 'speed').level, 2);
api.setEffect(player, 'speed', false, 2);
assert.equal(api.effectState(player, 'speed').enabled, false);
assert.equal(api.effectState(player, 'speed').level, 0);
queue.push(0, 3); // First + applies immediately, with no activation step.
await api.effectEditor(player, 'speed');
assert.equal(api.effectState(player, 'speed').level, 1);
assert.equal(api.effectState(player, 'speed').enabled, true);
queue.push(0, 3);
await api.effectEditor(player, 'speed');
assert.equal(api.effectState(player, 'speed').level, 2);
queue.push(1, 1, 3);
await api.effectEditor(player, 'speed');
assert.equal(api.effectState(player, 'speed').level, 0);
assert.equal(api.effectState(player, 'speed').enabled, false);
queue.push(0, 2);
await api.effectEditor(player, 'night_vision');
assert.equal(api.effectState(player, 'night_vision').level, 1);
queue.push(1, 2);
await api.effectEditor(player, 'night_vision');
assert.equal(api.effectState(player, 'night_vision').level, 0);
for (const id of ['poison', 'instant_damage', 'slowness', 'levitation', 'bad_omen', 'infested']) {
  assert.throws(() => api.setEffect(player, id, true, 1), /no es beneficioso/);
}
api.setEffect(player, 'instant_health', true, 2);
assert.equal(applied.at(-1).id, 'instant_health', 'Instant healing applies without waiting for interval');
properties.set('raynor:menu_effects_v2', JSON.stringify({ instant_health: { enabled: true, level: 2 }, instant_damage: { enabled: true, level: 8 } }));
intervals[0]();
assert(!applied.some(x => x.id === 'instant_damage'), 'Legacy damage repetition must never run');
assert.equal(applied.at(-1).id, 'instant_health');
assert.equal(applied.at(-1).options.showParticles, false);
api.clearEffects(player);
const count = applied.length;
intervals[0]();
assert.equal(applied.length, count);
assert.equal(effects.size, 0);
queue.push(catalog.BENEFICIAL_EFFECTS.size);
await api.effectsMenu(player);
assert.equal(forms.at(-1).buttons.length, catalog.BENEFICIAL_EFFECTS.size + 1);
for (const [id, [name]] of Object.entries(catalog.EFFECTS)) {
  if (!catalog.BENEFICIAL_EFFECTS.has(id)) assert(!forms.at(-1).buttons.some(([label]) => label.startsWith(name + '\n')));
}
const targets = api.inventoryTargets(player);
assert.equal(targets.length, 3); // Inventory sword, head armor, offhand shield. Dirt excluded.
assert(targets.some(x => x.label.includes('Cabeza')));
assert(targets.some(x => x.label.includes('secundaria')));
const target = targets[0];
const cursed = new Item(); cursed.levels.binding = 1;
assert(!api.compatibleEnchantments(cursed).some(type => catalog.CURSES.has(catalog.shortId(type.id))));
assert.throws(() => api.changeEnchantment(target, { id: 'binding', maxLevel: 1 }, 1), /maldiciones/);
queue.push(0, 3);
await api.enchantEditor(player, target, sharp);
assert.equal(inventory[0].item.levels.sharpness, 1, 'First + enchants immediately');
queue.push(0, 2);
await api.enchantEditor(player, target, silk);
assert.equal(inventory[0].item.levels.silk_touch, 1);
queue.push(1, 2);
await api.enchantEditor(player, target, silk);
assert.equal(inventory[0].item.levels.silk_touch, undefined);
api.changeEnchantment(target, sharp, 4);
assert.equal(inventory[0].item.levels.sharpness, 4);
assert(api.compatibleEnchantments(api.readTarget(target)).some(type => type.id === 'sharpness'));
queue.push(1, 3); // Minus, back.
await api.enchantEditor(player, target, sharp);
assert.equal(inventory[0].item.levels.sharpness, 3);
api.changeEnchantment(target, sharp, 100);
assert.equal(inventory[0].item.levels.sharpness, 5);
api.changeEnchantment(target, sharp, 0);
assert.equal(inventory[0].item.levels.sharpness, undefined);
inventory[0].item.damage++;
assert.throws(() => api.readTarget(target), /cambiado/);
queue.push(1);
await api.gameModes(player);
assert.equal(player.mode, 'creative');
for (const id of ['diamond_sword', 'golden_helmet', 'wooden_pickaxe', 'leather_boots', 'bow', 'fishing_rod', 'shield', 'diamond_spear']) {
  assert.notEqual(api.itemIcon(new Item(`minecraft:${id}`)), catalog.ICONS.book, `Missing icon: ${id}`);
}
const bp = JSON.parse(readFileSync(resolve(root, 'behavior_pack/manifest.json')));
const rp = JSON.parse(readFileSync(resolve(root, 'resource_pack/manifest.json')));
assert(bp.dependencies.some(dep => dep.uuid === rp.header.uuid && String(dep.version) === String(rp.header.version)));
const itemTexture = JSON.parse(readFileSync(resolve(root, 'behavior_pack/items/admin_menu.json')))['minecraft:item'].components['minecraft:icon'];
assert.equal(itemTexture, 'clock_item');
const atlasPath = resolve(root, '.build/reference/item_texture.json');
if (existsSync(atlasPath)) {
  const atlas = JSON.parse(readFileSync(atlasPath, 'utf8').replace(/^\/\/.*$/gm, ''));
  assert(atlas.texture_data[itemTexture], 'Item icon must be registered in the vanilla atlas');
}
const reference = resolve(root, '.build/reference/tree.json');
if (existsSync(reference)) {
  const paths = new Set(JSON.parse(readFileSync(reference)).tree.map(x => x.path));
  const allIcons = [...Object.values(catalog.ICONS), ...Object.values(catalog.ENCHANTMENTS).map(x => `textures/${x[1]}`),
    ...Object.values(catalog.EFFECTS).map(x => x[2]).filter(Boolean), ...Object.values(icons.EFFECT_ICONS)];
  for (const icon of allIcons) assert(paths.has(`resource_pack/${icon}.png`) || paths.has(`resource_pack/${icon}.tga`) || existsSync(resolve(root, `resource_pack/${icon}.png`)), `Missing texture: ${icon}`);
}
assert(forms.every(form => form.buttons.every(([label]) => typeof label !== 'string' || !/^(Activar|Desactivar)$/.test(label))));
assert(bp.header.name.endsWith('v' + bp.header.version.join('.')));
assert(rp.header.name.endsWith('v' + rp.header.version.join('.')));
for (const pack of ['behavior_pack', 'resource_pack']) assert(existsSync(resolve(root, pack, 'pack_icon.png')));
console.log('OK: beneficial-only, no curses (including existing), immediate +, zero removes, single-level controls, instant healing and legacy damage stopped; prior inventory/icon/snapshot tests pass.');
