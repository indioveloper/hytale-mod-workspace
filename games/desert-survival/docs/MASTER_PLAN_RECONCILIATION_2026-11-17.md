# Conciliación del plan maestro con el workspace

Fecha: 2026-11-17. Fuente de decisiones de diseño:
[plan maestro](DESERT_SURVIVAL_MASTER_PLAN_2026-11-17.md). Fuente de versiones,
instalaciones y smoke tests: [STATUS](../STATUS.md). Esta revisión actualiza
documentos, **no** código, assets ni JAR instalados. Las dos matrices citadas
por el plan (`CUTTERRAY_DUNE_PROGRESION_CANONICA.md` y
`DESERT_SURVIVAL_UNIQUE_SCHEMATICS_AND_CUTTERAYS.md`) no están en el workspace:
no se han inferido sus cifras.

| Archivo de la sección 7 | Conciliación aplicada | Límite o decisión pendiente |
| --- | --- | --- |
| `STATUS.md` | Se conserva la separación fuentes/instalado/probado y se corrige Research 0.2.8 en el handoff. | Probar 0.2.8, Build Selector 0.6.2, WorldGen 0.4.1 y Enemy Base 0.3.2. |
| `DOCS_INDEX.md` | Enlaza plan, esta conciliación y los cinco nuevos documentos de agenda. | Mantenerlo al día tras cada decisión. |
| `RESOURCE_PROGRESSION_RESEARCH.md` | T0–T6 y variantes T1.5–T6.5; enlaza el desajuste con los nombres Research. | Matrices de rendimiento/energía ausentes. |
| `RESOURCE_NODE_ARCHITECTURE.md` | Se declara híbrido bloque+entidad como propuesta; el prototipo real es bloque modelado. | Validar si el híbrido justifica su coste de red y persistencia. |
| `RESEARCH_TREE_MVP.md` | Separa rueda fija de 48 nodos del Códice futuro de siete pestañas y permisos únicos. | Definir ampliación de tiers y migración de IDs sin romper saves. |
| `RESEARCH_UNLOCKS_AND_ITEMS.md` | Corrige integración parcial de recetas 0.2.8 y marca nombres de cutterray heredados. | Resolver correspondencia de los nodos actuales con T0–T3. |
| `LOOT_AND_BLUEPRINTS_DRAFT.md` | Marca la antigua exclusión de Research del MVP como obsoleta; añade vínculo al contrato de únicos. | Integrar BP físico, permisos y pools; «plano» actual no los implementa. |
| `CRAFTING_MATERIALS_ZONES_AND_BUILDING.md` | Separa costes sugeridos de recetas reales, corrige distribución actual y corta el salto a aluminio. | Catálogo de materiales intermedios, recetas y estaciones reales. |
| `WORLDGEN_DESERT_PLAN.md` | Actualiza pack 0.4.1, props `Column`, zona 4 de hierro y frontera `Void`; anillos exteriores futuros. | Transición de saves y frontera jugable. |
| `prototypes/cutterray-t3/README.md` | Aclara que `T3` del asset no valida el T3 canónico. | Migrar perfiles tras recibir matrices. |
| `prototypes/research-tree/README.md` | Distingue Research 0.2.8 de Códice/Únicos futuros. | Smoke test de recetas. |
| `prototypes/build-selector/README.md` | Distingue generador/purificador locales de futura red y pentaescudos. | Smoke test 0.6.2 y diseño de prioridades. |
| `prototypes/desert-worldgen/README.md` | Distingue cuatro zonas finitas de futuros anillos infinitos y tiers de cutterray. | Smoke test 0.4.1, llaves, nuevos chunks. |
| `prototypes/enemy-base/README.md` | Distingue loot/llaves/cadáveres prototipo de blueprints únicas. | Smoke test 0.3.2 y persistencia de cadáveres. |

Nuevas agendas de diseño: [esquemas únicos](UNIQUE_SCHEMATICS_CATALOG.md),
[red de energía](BASE_POWER_GRID.md), [desierto profundo](DEEP_DESERT_WORLDGEN.md),
[combate y Power Packs](CHARACTER_COMBAT_AND_POWER.md) y
[misiones y lore](PROGRESSION_QUESTS_AND_LORE.md).

## Fricciones que necesitan decisión antes de implementar

1. Los nodos Research actuales llaman tier 1/2/3 a cutterrays que el nuevo
   diseño ordena T0/T1/T2/T3. No cambiar IDs persistidos solo para renombrar;
   diseñar tabla de migración, receta y acceso a materiales. El prototipo T3
   extrae cobre/hierro y no prueba basalto/aluminio.
2. Las 48 investigaciones y la rueda actual cubren bandas 0–2. El Códice de
   siete pestañas y tiers 0–6 es un objetivo, no una UI ya disponible.
3. Los cofres especiales contienen un «plano» provisional, no el esquema único
   con descubrimiento y permiso personal. La llave garantizada y el prompt
   del cadáver todavía deben comprobarse en juego.
4. El mapa actual acaba en `Void`; ampliar a anillos infinitos exige decidir
   migración de chunks/partidas existentes antes de tocar el generador.
5. El agua como ingrediente de objetos únicos y los valores de energía,
   rendimiento y materiales intermedios siguen abiertos. No convertirlos en
   recetas o pruebas que afirmen un balance definitivo.
