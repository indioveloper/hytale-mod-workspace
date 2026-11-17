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
