export const ICONS = {
  sword: "textures/items/diamond_sword", potion: "textures/items/potion_bottle_regeneration",
  table: "textures/raynor/enchanting_table", milk: "textures/items/bucket_milk",
  crafting: "textures/ui/crafting_table", star: "textures/items/nether_star",
  back: "textures/ui/arrow_left", plus: "textures/ui/plus", minus: "textures/ui/minus",
  on: "textures/ui/confirm", off: "textures/ui/cancel", book: "textures/items/book_enchanted",
};

// Name, whether amplifier changes gameplay, optional icon override.
export const EFFECTS = {
  speed: ["Velocidad", true], slowness: ["Lentitud", true], haste: ["Prisa", true],
  mining_fatigue: ["Fatiga minera", true], strength: ["Fuerza", true],
  instant_health: ["Curacion instantanea", true, "textures/ui/heart"],
  instant_damage: ["Dano instantaneo", true, "textures/ui/wither_heart"],
  jump_boost: ["Salto mejorado", true], nausea: ["Nausea", false],
  regeneration: ["Regeneracion", true], resistance: ["Resistencia", true],
  fire_resistance: ["Resistencia al fuego", false], water_breathing: ["Respiracion acuatica", false],
  invisibility: ["Invisibilidad", false], blindness: ["Ceguera", false],
  night_vision: ["Vision nocturna", false], hunger: ["Hambre", true], weakness: ["Debilidad", true],
  poison: ["Veneno", true], wither: ["Wither", true], health_boost: ["Salud extra", true],
  absorption: ["Absorcion", true], saturation: ["Saturacion", true, "textures/ui/hunger_effect"],
  levitation: ["Levitacion", true], fatal_poison: ["Veneno mortal", true, "textures/ui/poison_effect"],
  conduit_power: ["Poder del conducto", false], slow_falling: ["Caida lenta", false],
  bad_omen: ["Mal presagio", false], village_hero: ["Heroe de la aldea", true],
  darkness: ["Oscuridad", false], trial_omen: ["Presagio de prueba", false],
  raid_omen: ["Presagio de asalto", false], wind_charged: ["Carga de viento", false],
  weaving: ["Tejido", false], oozing: ["Rezumar", false], infested: ["Infestacion", false],
  breath_of_the_nautilus: ["Aliento del nautilo", false], frozen: ["Congelacion", false],
};
// Explicit allowlist: neutral/risky effects and future unknown effects stay hidden.
export const BENEFICIAL_EFFECTS = new Set([
  "speed", "haste", "strength", "instant_health", "jump_boost", "regeneration",
  "resistance", "fire_resistance", "water_breathing", "invisibility", "night_vision",
  "health_boost", "absorption", "saturation", "conduit_power", "slow_falling",
  "village_hero", "breath_of_the_nautilus",
]);
export const CURSES = new Set(["binding", "vanishing", "binding_curse", "vanishing_curse", "curse_of_binding", "curse_of_vanishing"]);
export const INSTANT = new Set(["instant_health", "saturation"]);

// Existing vanilla symbols, all in the native pixel-art style.
export const ENCHANTMENTS = {
  aqua_affinity: ["Afinidad acuatica", "ui/haste_effect"],
  bane_of_arthropods: ["Perdicion de los artropodos", "items/spider_eye"],
  binding: ["Maldicion de ligamiento", "items/lead"],
  blast_protection: ["Proteccion contra explosiones", "items/gunpowder"],
  breach: ["Brecha", "items/iron_chestplate"], channeling: ["Conductividad", "items/trident"],
  density: ["Densidad", "items/mace"], depth_strider: ["Agilidad acuatica", "ui/water_breathing_effect"],
  efficiency: ["Eficiencia", "items/diamond_pickaxe"], feather_falling: ["Caida de pluma", "items/feather"],
  fire_aspect: ["Aspecto igneo", "items/blaze_powder"], fire_protection: ["Proteccion contra fuego", "ui/fire_resistance_effect"],
  flame: ["Fuego", "items/fireball"], fortune: ["Fortuna", "items/diamond"],
  frost_walker: ["Paso helado", "items/snowball"], impaling: ["Empalamiento", "items/trident"],
  infinity: ["Infinidad", "items/arrow"], knockback: ["Empuje", "ui/arrow_right"],
  looting: ["Saqueo", "items/emerald"], loyalty: ["Lealtad", "ui/undoArrow"],
  luck_of_the_sea: ["Suerte marina", "items/fish_raw"], lure: ["Atraccion", "items/fishing_rod_uncast"],
  mending: ["Reparacion", "items/experience_bottle"], multishot: ["Multidisparo", "items/crossbow_arrow"],
  piercing: ["Perforacion", "items/arrow"], power: ["Poder", "items/bow_standby"],
  projectile_protection: ["Proteccion contra proyectiles", "items/iron_helmet"],
  protection: ["Proteccion", "items/diamond_chestplate"], punch: ["Retroceso", "ui/strength_effect"],
  quick_charge: ["Carga rapida", "items/clock_item"], respiration: ["Respiracion", "ui/water_breathing_effect"],
  riptide: ["Propulsion acuatica", "ui/conduit_power_effect"], sharpness: ["Filo", "items/diamond_sword"],
  silk_touch: ["Toque de seda", "items/string"], smite: ["Castigo", "items/bone"],
  soul_speed: ["Velocidad del alma", "ui/speed_effect"], swift_sneak: ["Sigilo veloz", "items/diamond_boots"],
  thorns: ["Espinas", "items/sweet_berries"], unbreaking: ["Irrompibilidad", "items/netherite_ingot"],
  vanishing: ["Maldicion de desaparicion", "ui/invisibility_effect"], wind_burst: ["Rafaga de viento", "ui/wind_charged_effect"],
  lunge: ["Embestida", "items/spear/diamond_spear"],
};

export const shortId = (id) => id.replace(/^minecraft:/, "");
export const enchantName = (id) => ENCHANTMENTS[shortId(id)]?.[0] ?? shortId(id).replaceAll("_", " ");
export const enchantIcon = (id) => ENCHANTMENTS[shortId(id)] ? `textures/${ENCHANTMENTS[shortId(id)][1]}` : ICONS.book;
export const effectName = (id) => EFFECTS[shortId(id)]?.[0] ?? shortId(id).replaceAll("_", " ");
