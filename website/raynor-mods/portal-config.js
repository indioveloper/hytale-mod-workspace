/*
 * Único punto de edición para versiones, estados y URLs públicas.
 * Los enlaces `curseforge` apuntan a las fichas publicas de cada mod.
 * Las rutas `download` son URLs publicas del portal para descargar cada JAR.
 */
window.RAYNOR_PORTAL = {
  links: {
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
      summaryEn: "A configurable spawner block for building complete waves without programming.",
      statusEn: "WIP · Update 6",
      features: ["Editor web con código CMS1", "NPC, equipo, loot y comportamiento", "Preview y configuración persistente"],
      curseforge: "https://www.curseforge.com/hytale/mods/configurable-mob-spawners/preview",
      download: "downloads/ConfigurableMobSpawners-0.5.3.jar",
      extraLink: { label: "Abrir configurador", labelEn: "Open configurator", urlKey: "configurator" }
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
      summaryEn: "A general-purpose toolbox for taking Trigger Volumes much further.",
      statusEn: "Update 6 · Active",
      features: ["Comandos, mensajes y tags", "Timer circular por jugador", "Objetos aleatorios y regla NoMove"],
      curseforge: "https://www.curseforge.com/hytale/mods/more-triggers/preview",
      download: "downloads/More_Triggers-1_10_5.jar"
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
      summaryEn: "Create props, convert blocks and move platforms from Trigger Volumes.",
      statusEn: "Update 6 · Active",
      features: ["Movimiento y rotación", "Colisión para plataformas", "Partículas ancladas a entidades"],
      curseforge: "https://www.curseforge.com/hytale/mods/entity-motion-triggers/preview",
      download: "downloads/Entity_Motion_Triggers-1_3_2.jar"
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
      summaryEn: "Draw precise particle shapes from any Trigger Volume.",
      statusEn: "Update 6 · Prototype",
      features: ["Cubos, esferas y líneas", "Coordenadas absolutas o relativas", "Densidad y coste controlables"],
      curseforge: "https://www.curseforge.com/hytale/mods/particle-shape-vfx/preview",
      download: "downloads/Particle_Shape_VFX-0_1_1.jar"
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
      summaryEn: "Native, editable and persistent Objectives controlled by commands, UI and triggers.",
      statusEn: "Update 6 · Smoke test pending",
      features: ["Hasta cinco tareas", "Progreso individual o compartido", "Control completo desde Trigger Volumes"],
      curseforge: "https://www.curseforge.com/hytale/mods/editable-objectives-scoreboards/preview",
      download: "downloads/Scoreboards-2_0_11.jar"
    }
  ],
  packs: []
};
