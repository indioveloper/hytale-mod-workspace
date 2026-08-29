/*
 * Único punto de edición para versiones, estados y URLs públicas.
 * No inventes enlaces de CurseForge: añade `curseforge` solo cuando la ficha
 * exista en el perfil oficial de Raynor.
 */
window.RAYNOR_PORTAL = {
  links: {
    github: "https://github.com/indioveloper/hytale-mod-workspace",
    releases: "https://github.com/indioveloper/hytale-mod-workspace/releases",
    curseforge: "https://www.curseforge.com/members/raynor_hytale/projects",
    configurator: "https://hytale-mob-spawner-configurator.vercel.app"
  },
  mods: [
    {
      slug: "configurable-mob-spawners",
      name: "Configurable Mob Spawners",
      version: "0.5.3",
      status: "WIP · Update 6",
      maturity: "prototype",
      icon: "assets/configurable-mob-spawners.png",
      accent: "cyan",
      summary: "Un bloque de spawner diseñado para controlar oleadas completas sin programar.",
      features: ["Editor web con código CMS1", "NPC, equipo, loot y comportamiento", "Preview y configuración persistente"],
      source: "mods/java/configurable-mob-spawners",
      extraLink: { label: "Abrir configurador", urlKey: "configurator" }
    },
    {
      slug: "more-triggers",
      name: "More Triggers",
      version: "1.10.5",
      status: "Update 6 · Activo",
      maturity: "stable",
      icon: "assets/more-triggers.png",
      accent: "violet",
      summary: "Una caja de herramientas general para llevar los Trigger Volumes mucho más lejos.",
      features: ["Comandos, mensajes y tags", "Timer circular por jugador", "Objetos aleatorios y regla NoMove"],
      source: "mods/java/more-triggers"
    },
    {
      slug: "entity-motion-triggers",
      name: "Entity Motion Triggers",
      version: "1.3.2",
      status: "Update 6 · Activo",
      maturity: "stable",
      icon: "assets/entity-motion-triggers.png",
      accent: "orange",
      summary: "Crea props, convierte bloques y mueve plataformas desde Trigger Volumes.",
      features: ["Movimiento y rotación", "Colisión para plataformas", "Partículas ancladas a entidades"],
      source: "mods/java/entity-motion-triggers"
    },
    {
      slug: "build-battle",
      name: "Build Battle",
      version: "0.2.4",
      status: "Update 6 · Prototipo",
      maturity: "prototype",
      icon: "assets/build-battle.png",
      accent: "pink",
      summary: "Sugerencias de temas y control seguro de herramientas creativas por parcela.",
      features: ["Temas guardados como tags", "Whitelist de Builder Tools", "Restauración al salir del plot"],
      source: "mods/java/build-battle"
    },
    {
      slug: "particle-shape-vfx",
      name: "Particle Shape VFX",
      version: "0.1.1",
      status: "Update 6 · Prototipo",
      maturity: "prototype",
      icon: "assets/particle-shape-vfx.png",
      accent: "green",
      summary: "Dibuja formas precisas con partículas desde cualquier Trigger Volume.",
      features: ["Cubos, esferas y líneas", "Coordenadas absolutas o relativas", "Densidad y coste controlables"],
      source: "mods/java/particle-shape-vfx"
    },
    {
      slug: "scoreboards",
      name: "Editable Objectives",
      version: "2.0.11",
      status: "Update 6 · Smoke pendiente",
      maturity: "prototype",
      icon: "assets/scoreboards.png",
      accent: "gold",
      summary: "Objectives nativos, editables y persistentes con comandos, interfaz y triggers.",
      features: ["Hasta cinco tareas", "Progreso individual o compartido", "Control completo desde Trigger Volumes"],
      source: "mods/java/scoreboards"
    }
  ],
  packs: [
    {
      slug: "raynor-npcs",
      name: "Raynor NPCs",
      version: "1.1.2",
      marker: "NPC",
      summary: "Roles, marcadores y efectos reutilizables para generar, equipar y limpiar NPCs.",
      status: "Update 6 · Activo",
      source: "mods/asset-packs/raynor-npcs"
    },
    {
      slug: "mechanisms",
      name: "Mechanisms",
      version: "1.1.1",
      marker: "MEC",
      summary: "Palancas, botones de esencia y una piedra que crea zonas NoBuild temporales.",
      status: "Update 6 · Smoke visual pendiente",
      source: "mods/asset-packs/mechanisms"
    },
    {
      slug: "raynor-blocks",
      name: "Raynor Blocks",
      version: "1.0.2",
      marker: "BLK",
      summary: "Barreras técnicas y bloques fantasma para previsualizar zonas y posiciones.",
      status: "Update 6 · Validación estática",
      source: "mods/asset-packs/blocks"
    },
    {
      slug: "nexus-siege-props",
      name: "Nexus Siege Props",
      version: "1.0.1",
      marker: "NS",
      summary: "Props reutilizables del minijuego Nexus Siege, empezando por el soporte de espada.",
      status: "Update 6 · Proyecto interno",
      source: "mods/asset-packs/nexus-siege-props"
    },
    {
      slug: "roguelike-prefabs",
      name: "Roguelike Prefabs",
      version: "1.0.1",
      marker: "RLG",
      summary: "Mapas, salas y conectores usados por las pruebas roguelike existentes.",
      status: "Update 6 · Uso interno",
      source: "mods/asset-packs/roguelike-prefabs"
    }
  ]
};
