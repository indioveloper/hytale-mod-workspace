# Investigación de progresión de recursos

Revisión de progresión adoptada: 2026-11-17. La tabla antigua de seis escalones
ha sido sustituida por el [plan maestro](DESERT_SURVIVAL_MASTER_PLAN_2026-11-17.md).

## Referencia de Dune: Awakening

La progresión observada de cutterays portátiles es acumulativa:

| Herramienta | Nuevo recurso compatible |
| --- | --- |
| Improvised Cutteray | Cobre, granito y metal recuperado |
| Cutteray Mk1 | Hierro |
| Cutteray Mk2 | Carbono y erythrite |
| Cutteray Mk3 | Basalto y aluminio |
| Industrial Cutteray Mk4 | Jasmio |
| Cutteray Mk5 | Titanio y estravidio |
| Cutteray Mk6 | Mismos recursos finales, mayor rendimiento |

El acero no es un nodo natural. Se refina a partir de lingotes de hierro,
mineral de carbono y agua. El jasmio se combina con aluminio para producir
duraluminio; titanio y estravidio alimentan la progresión final.

Fuentes consultadas:

- https://awakening.wiki/Cutteray
- https://awakening.wiki/Steel
- https://awakening.wiki/Jasmium
- https://db.duneawakening.net/resources/steel-ingot/
- https://duneawakening.com/es/news/dune-awakening-1-1-20-0-patch-notes/

## Progresión adoptada para el mod

El orden de acceso a recursos sigue el de Dune: Awakening. Los nombres y
valores exactos de rendimiento y consumo energético esperan la matriz
`CUTTERRAY_DUNE_PROGRESION_CANONICA.md`, citada en el plan maestro pero no
incluida en el adjunto recibido.

| Tier | Recurso nuevo accesible |
| ---: | --- |
| 0 improvisado | Cobre, granito y metal recuperado |
| 1 | Hierro |
| 2 | Carbono y eritrita |
| 3 | Basalto y aluminio |
| 4 | Jasmio |
| 5 | Titanio y estravidio |
| 6 | Mismos recursos finales, más rendimiento |

Entre T1 y T6 se plantean las variantes únicas T1.5–T6.5: Sim, Olef,
Callie, Sandflies, Tarl y Kynes. Su rendimiento y acceso exacto al siguiente
recurso dependen de la segunda matriz aún no recibida,
`DESERT_SURVIVAL_UNIQUE_SCHEMATICS_AND_CUTTERAYS.md`. El prototipo actual
denominado T3 no implementa por sí solo esta progresión completa.

El acero se representará como lingotes, placas y componentes refinados. No tendrá
nodo de extracción propio.

La correspondencia con los nodos Research existentes queda fijada así, sin
cambiar IDs de investigación ni prerrequisitos:

| Nodo guardado | Nombre visible vigente | Banda de Research | Recurso nuevo canónico | Estado real |
| --- | --- | --- | --- | --- |
| `CN-0.2` | Cutterray improvisado T0 | Chatarra | Chatarra, granito y cobre | Output futuro `Tool_Cutterray_T0_Prototype`; aún sin asset/receta. |
| `CN-1.2` | Cutterray T1 | Cobre | Hierro | Output futuro `Tool_Cutterray_T1_Prototype`; aún sin asset/receta. |
| `CN-2.2` | Cutterray T2 | Hierro | Carbono y eritrita | Output futuro `Tool_Cutterray_T2_Prototype` sin asset/receta; también conserva `Tool_Cutterray_T3_Prototype` como item experimental de cobre/hierro. |

El T3 canónico (basalto y aluminio) necesita un nodo posterior a `CN-2.2` y
su propia receta y **otro ID de item**: `Tool_Cutterray_T3_Prototype` debe
seguir siendo experimental y no recibir una futura receta de T3 canónico,
porque `CN-2.2` ya lo enlaza. El ID del nuevo nodo se asignará al ampliar el
árbol; no reutilizar `CN-2.2` ni renombrar el ID del prototipo para fingir una migración. Los
IDs de output futuros T0/T1/T2 ya corresponden a esos nodos en fuentes.
Los personajes que investigaron antes reciben las recetas nuevas al entrar,
según la sincronización existente de Research; comprobarlo cuando haya assets.
El ID del item experimental T3 permanece intacto. El
[borrador de costes](CRAFTING_MATERIALS_ZONES_AND_BUILDING.md) los presenta
solo como presupuestos de balance.
