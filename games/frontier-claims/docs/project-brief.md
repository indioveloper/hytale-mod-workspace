# Frontier Claims — Project Brief

Última actualización: 2026-09-05.

## 1. Premisa

Un evento temporal tipo "juego de mesa de conquista" superpuesto a un mundo
Hytale. Hasta 10 jugadores por instancia compiten para reclamar el mayor
número posible de territorios en un mapa de 80 puntos de interés durante 7
días. Cada territorio da un beneficio distinto (minas, mobs, construcción,
mini-dungeons). Al terminar la ventana, la instancia se cierra y solo queda un
registro de "poder acumulado" en un ranking histórico — no es supervivencia ni
un minijuego de partidas cortas: es una razón para conectarse cada día durante
una semana.

Decisiones ya cerradas con el usuario (no reabrir sin motivo):

- **Escala**: instancias paralelas de hasta 10 jugadores, no un único mundo
  compartido por todo el servidor.
- **Duración**: 7 días por instancia (el boceto manda sobre el "2 semanas" del
  texto original).
- **Racha diaria**: conectar días consecutivos aumenta los tokens de conquista
  del día, no el acceso a tiers.
- **Reset**: al cerrar la instancia todo el progreso territorial desaparece;
  solo sobrevive un ranking/histórico cosmético fuera de la instancia.
- **Monetización**: fuera de alcance de implementación. Se deja un hook de
  datos para "buffs premium" aplicables externamente, sin backend de pago.
- **Mapa**: generación procedural completa (terreno y POIs), no colocación a
  mano. El boceto del usuario es la guía conceptual de zonas y tipos de POI,
  no una geometría fija a reproducir. Ver §3 y `technical-architecture.md`
  §3 para el mecanismo exacto y la regla de semilla por temporada.
- **Disputa de territorio**: solo se reclaman POIs libres; no hay combate para
  arrebatar un territorio ya reclamado por otro jugador (ver §4).
- **Formación de instancia**: cola automática (matchmaking simple), no grupos
  formados a mano ni instancias por gremio (ver §2.1).
- **Racha diaria**: curva lineal suave con tope (ver §7).
- **Poder acumulado**: se basa solo en recursos totales producidos durante la
  temporada, no en territorios poseídos al cierre (ver §11).

## 2. Bucle diario (jugador)

1. Conectar → si es un día nuevo respecto a su última conexión registrada en
   esta instancia, se incrementa su racha (o se resetea a 1 si hubo un hueco).
2. Recibe automáticamente `1 + bonus_por_racha` tokens de conquista de hoy
   (acumulables entre días, sin caducar dentro de la temporada).
3. Va al mapa, elige un POI libre o disputable, paga token(s) + coste en
   oro/gemas según su tier, lo reclama.
4. Gestiona sus territorios ya reclamados: recoge buffers de minas, mejora
   eficiencia/almacenaje, contrata NPCs, construye en sus parcelas, entra a
   sus mini-dungeons (máx. 1 vez/día cada una).
5. Se desconecta. Las minas siguen produciendo (hasta llenar su buffer) y los
   NPCs contratados siguen trabajando — ver §7 sobre por qué esto es posible
   sin que el jugador esté online.

### 2.1 Cómo se entra a una instancia

Cola automática: el jugador pulsa "unirse" (comando o interacción en el HUB
del server) y el sistema lo mete en la primera instancia de Frontier Claims
con hueco (< 10 jugadores) que aún no haya cerrado por timeout; si no hay
ninguna abierta, crea una nueva llamando a `spawnInstance` (ver
`technical-architecture.md`). No requiere coordinar amigos de antemano ni un
sistema de gremios — más sencillo de implementar y evita depender de una
feature social que no existe todavía en el server. Efecto secundario a
aceptar: un jugador puede acabar en una instancia con desconocidos en vez de
con su grupo de amigos; si eso resulta un problema en playtesting, añadir
"unirse con invitación" es una extensión de esta cola, no un rediseño.

## 3. El mapa y los 80 POIs

Conteos objetivo, basados en el boceto del usuario (aproximados, no
garantizados al 100% por ser generación procedural — ver
`technical-architecture.md` §3.2):

| Tipo de POI | Cantidad objetivo | Notas |
| --- | --- | --- |
| Mina de oro | ~15 | Producción autónoma con buffer limitado |
| Mina de gemas/cristal | ~15 | Igual que oro, recurso distinto |
| Spawner de mobs | ~15 | Dropean recursos de crafteo, no oro/gemas |
| Recompensa inmediata | ~15 | Un solo cofre/beneficio al reclamar, sin producción continua |
| Parcela construible | ~10 | El jugador coloca prefabs de un catálogo limitado (portales, decoración) |
| Mini-dungeon | ~10 | 5 de entrada libre, 5 requieren llave; límite 1 completado/día por dungeon y jugador |

### 3.1 Generación: zonas por bioma, no colocación a mano

El mapa se genera con el generador procedural clásico de Hytale, el mismo
mecanismo que reparte biomas y coloca pueblos/dungeons/vegetación en vanilla
(ver `technical-architecture.md` §3.1 para la cadena técnica exacta:
`WorldStructure → Biome → Props → Prefab`). Cada tipo de POI vive en su
propio "bioma" de la instancia, tallado en regiones de extensión similar
alrededor de un HUB central; ese bioma solo coloca prefabs de su tipo de POI.
Los POIs de biomas más alejados del HUB cuestan más oro/gemas para reclamar
pero dan mejor recompensa base (equivalente al criterio de precios 1-6 del
boceto, ahora expresado como distancia al HUB en vez de coordenadas fijas a
mano). El HUB central es zona neutral (spawn, banco, mercado de NPCs, punto
de retorno de las rutas de NPC recolector).

### 3.2 Semilla compartida por temporada

Todas las instancias abiertas durante la misma semana usan la misma semilla
de generación — mismo layout de POIs para todos los grupos, para que la
competición sea justa. Al iniciar una nueva temporada (7 días después), se
genera con una semilla distinta: variedad entre temporadas sin sacrificar
igualdad de condiciones dentro de una. Mecanismo exacto (reseed del asset de
instancia entre temporadas) en `technical-architecture.md` §3.3.

### 3.3 Cada POI trae su propia Trigger Volume

**Territorio = una Trigger Volume** con metadatos de propiedad (owner, tier,
tipo, estado de mejoras), pero a diferencia del diseño anterior (colocación a
mano), aquí **la Trigger Volume vive dentro del prefab de cada tipo de POI**
y se registra automáticamente cuando el generador la coloca — Hytale soporta
esto de forma nativa (`VolumeEntry.FromWorldGen`, ver hallazgo en
`technical-architecture.md` §3.1), es el mismo mecanismo que usan los
pueblos/dungeons vanilla. Esto reutiliza directamente la base de efectos de
Trigger Volumes de `mods/java/more-triggers` en vez de inventar un sistema de
regiones nuevo; lo que hay que añadir encima (ownership, coste, producción)
es lo mismo que ya se documentó — solo cambia cómo llega la Trigger Volume al
mundo (generada, no pegada a mano).

## 4. Reclamar un territorio

- Requiere: ≥1 token de conquista disponible + oro/gemas suficientes para el
  tier del POI.
- Un POI libre se reclama directamente. Un POI ya reclamado por otro jugador
  de la misma instancia **no se puede disputar ni arrebatar**: solo quedan
  disponibles los territorios que siguen libres. Decisión cerrada — sin
  combate por territorio, coherente con que el modo no es un minijuego PvP.
- **Riesgo aceptado conscientemente**: si un jugador reclama un POI bueno y
  luego abandona la partida (no vuelve a conectar), ese territorio queda
  bloqueado sin producir ni ser aprovechado el resto de la temporada — no
  hay ningún mecanismo de liberación por inactividad en este diseño. Si en
  playtesting resulta un problema real, la solución más simple sería un
  timeout de inactividad por territorio (no por jugador) que lo devuelve a
  libre; no está incluido en la v1 para no complicar el ownership model.
- Al reclamar se descuenta el coste y se consume 1 token; el territorio queda
  vinculado al UUID del jugador (no al equipo/instancia) durante toda la
  temporada.

## 5. Minas (oro / gemas)

- Producen recursos de forma autónoma con una tasa base y un **buffer**
  (capacidad interna). Si el buffer se llena, la mina deja de producir hasta
  que alguien (jugador o NPC recolector) lo vacíe.
- Mejoras disponibles por mina (compradas con recursos de la propia
  temporada, no dinero real):
  - Eficiencia (↑ tasa de producción).
  - Capacidad de buffer (mejora fija, escalón por escalón).
  - Instalación de almacenaje — cofres tier 1/2 — que permiten que los NPCs
    depositen ahí en vez de tener que ser recogidos manualmente.

## 6. Spawners de mobs

Reutilizan directamente `mods/java/configurable-mob-spawners` (perfiles
ponderados, élites, loot configurable) en vez de construir un sistema de
spawn nuevo. Cada uno de los 15 POIs de este tipo es un `Spawner Block`
configurado con el loot de recursos de crafteo específico del evento.

### 6.1 Curva de racha diaria

Lineal suave con tope, sobre una base de 1 token/día:

| Racha (días consecutivos) | Tokens extra | Tokens totales ese día |
| --- | --- | --- |
| 1 | +0 | 1 |
| 2 | +0 | 1 |
| 3–4 | +1 | 2 |
| 5–6 | +2 | 3 |
| 7 (máximo de la temporada) | +3 | 4 |

Tope en +3 extra: un jugador con racha perfecta durante los 7 días de la
temporada nunca supera 4 tokens/día. Un hueco de un día resetea la racha a 1
(vuelve a 1 token/día) — castiga faltar, no castiga entrar tarde a la
temporada. Cifras de partida para playtesting, no definitivas: ajustar junto
al balance económico general (ver §13).

## 7. NPCs de farmeo automático (offline)

Dos roles, pensados para que el progreso siga aunque el jugador esté
desconectado:

- **NPC Recolector**: recorre las minas del jugador (ruta entre sus propios
  territorios) y vacía sus buffers, depositando en un almacén central o en
  los cofres tier 1/2 instalados en cada mina.
- **NPC Minero/Guerrero**: estacionado en un spawner de mobs propiedad del
  jugador; mata a los mobs generados y recoge el drop.

Ambos se contratan con un "Summoner" (recompensa del catálogo, §9) y se basan
en el patrón de roles de `mods/asset-packs/raynor-npcs` (spawn markers +
`NPC/Roles/Custom`), pero necesitan **lógica nueva**: los roles actuales del
pack (`Worker_Route`, `Klops_Miner_Patrol`, `Static_Guard`) hacen patrulla,
emote o guardia estática — ninguno transfiere recursos entre un buffer de mina
y un almacén. Eso es trabajo de plugin nuevo (un `System` que, en cada ciclo
del NPC, lee el buffer de la mina más cercana en su ruta y lo transfiere).

**Punto crítico de arquitectura** (ver `technical-architecture.md` para la
evidencia en el Shared Source): para que esto funcione con el jugador
desconectado, la instancia **no puede usar `WorldEmptyCondition`** como
condición de cierre, porque esa condición destruye la copia del mundo en
cuanto se vacía de jugadores — mataría literalmente el farmeo offline que es
el punto 3-4 de la idea original. Hay que usar únicamente `TimeoutCondition`
(los 7 días fijos) y, como mucho, un `IdleTimeoutCondition` con un umbral muy
generoso (días, no minutos) para limpiar instancias real y permanentemente
abandonadas.

## 8. Parcelas construibles

10 POIs vacíos donde el dueño puede colocar prefabs de un catálogo cerrado
(no build libre): principalmente portales para viajar entre sus propios
territorios, y decoración. Restringir las herramientas creativas disponibles
reutilizando el patrón de whitelist de `RestrictBuildBattleCreativeTools`
(`mods/java/build-battle`) en vez de inventar un sistema de permisos de
construcción nuevo.

## 9. Mini-dungeons

10 entradas: 5 abiertas, 5 requieren llave (recompensa del catálogo). Límite:
1 completado por dungeon y por jugador cada día in-game. El port auxiliar
`auxiliary/java/dungeon-core-pre11` está fijado a `0.6.0-pre.11` y no es la
base de release de OrbGenesis — **no asumir que sirve tal cual para 0.6.3
estable**; para la primera versión, lo más seguro es mini-dungeons propias y
simples (una sala, un objetivo, un timer de cooldown por jugador) construidas
sobre Trigger Volumes, en vez of adoptar ese port sin validarlo.

## 10. Catálogo de recompensas (del boceto, sin cambios)

- Summoner de NPC recolector.
- Summoner de NPC minero/guerrero.
- Mejora de eficiencia de producción (taladros).
- Mejora de capacidad de buffer interno.
- Cofre (chest) tier 1 y tier 2 — almacenaje en minas.
- Teletransportador.
- Mapa consumible — revela coordenadas de un tipo de POI.
- Llave de dungeon.

## 11. Fin de temporada

Al llegar al día 7 (o antes si se decide cerrar manualmente):

1. Se calcula el "poder acumulado" final de cada jugador: la suma de todos
   los recursos que produjo/recogió durante la temporada (oro + gemas +
   recursos de crafteo de spawners), no los territorios que posea en el
   momento del cierre. Esto premia la eficiencia de farmeo sostenida a lo
   largo de los 7 días por encima de la pura expansión territorial de última
   hora — hay que acumular el contador en cada extracción/recolección (no
   solo leer el estado final de los buffers), para que valga lo mismo lo
   recogido el día 2 que lo recogido el día 7.
2. Se persiste `{playerUUID, instanceId, poder, fecha}` en un almacén del
   propio plugin (fuera del mundo efímero) para un ranking histórico
   consultable con un comando (`/frontier top`, `/frontier stats`).
3. Se muestra un aviso a los jugadores conectados antes del cierre (HUD de
   cuenta atrás) y se cierra la copia del mundo. Nada del estado territorial
   sobrevive.

## 12. Hook de monetización (sin implementar)

Para no cerrar la puerta a "buffs comprables" sin construir un backend de
pago ahora: el modelo de datos del jugador en la instancia reserva un campo
`premiumBuffs: List<BuffId>` que cualquier sistema externo (comando admin,
webhook futuro) puede rellenar. Ningún sistema del propio mod vende nada ni
gestiona pagos. Cuidado de diseño para cuando se implemente: los buffs deben
afectar comodidad/velocidad (ej. buffer extra, slot de NPC extra), nunca la
capacidad de reclamar territorios que otro jugador no podría igualar jugando
gratis — así se evita pay-to-win real.

## 13. Preguntas abiertas (a decidir antes de implementar, no bloquean el diseño)

Todas las dudas de diseño de la primera pasada quedaron resueltas (disputa de
territorio, formación de instancia, curva de racha, fórmula de poder — ver
Decisiones cerradas en §1 y las secciones enlazadas). Queda un único punto
pendiente, que es de números, no de diseño de sistemas:

- **Balance económico real**: costes de oro/gemas por tier (1-6), tasas base
  de producción de cada mina, tamaño de buffer inicial y por mejora, y qué
  tan rápido un jugador medio puede acumular recursos para no dejar el juego
  ni demasiado lento (frustrante) ni resuelto en día 2 (sin razón para volver
  el resto de la semana). Esto necesita una pasada de números concreta
  —posiblemente una hoja de cálculo con la progresión día a día— antes de
  fijar valores en el manifest/config del plugin. Recomendado: hacerla justo
  antes de la fase 5 del roadmap (pulido de economía), con datos reales del
  vertical slice de la fase 1 como referencia de cuánto produce una mina base
  en la práctica.
- **Tres detalles de la generación procedural** (§3, `technical-architecture.md`
  §3.2), no bloquean el diseño pero sí hay que resolverlos antes de construir
  las 6 zonas completas: cómo tallar el HUB como región fija en vez de bioma
  con ruido, si aceptar un total de POIs aproximado por semilla o forzar el
  total exacto con posiciones precalculadas, y si el bug de rotación 90/270
  de prefabs documentado para pegado en runtime también afecta al pegado en
  tiempo de generación (`PrefabPropAsset`) — verificar con un prefab de
  prueba antes de dar por buena la Trigger Volume embebida en los 6 tipos de
  POI reales.

## 14. Reutilización de sistemas existentes (resumen)

| Pieza del diseño | Se apoya en | Qué falta construir |
| --- | --- | --- |
| Territorios / Trigger Volumes | `mods/java/more-triggers` | Ownership, coste, tiers, producción |
| Spawners de mobs | `mods/java/configurable-mob-spawners` | Loot table del evento |
| NPCs recolector/guerrero | `mods/asset-packs/raynor-npcs` | Lógica de transferencia de recursos y combate anti-spawner |
| Whitelist de build en parcelas | `mods/java/build-battle` (`RestrictBuildBattleCreativeTools`) | Catálogo de prefabs propio del evento |
| HUD/objectives de racha y poder | `mods/java/scoreboards` | Definiciones de Objective específicas del evento |
| Instancia de 7 días, aislada por grupo | `HytaleServer/builtin/Instances` (Shared Source) | Ver `technical-architecture.md` |
| Mini-dungeons | Trigger Volumes propias | Todo — no reutilizar `dungeon-core-pre11` sin validar |
| Generación del mapa (zonas + POIs) | `HytaleServer/builtin/HytaleGenerator` clásico (Shared Source) — `WorldStructure`/`Biome`/`Props`/`PrefabPropAsset` | Todo: `WorldStructureAsset` con 6+1 zonas, prefabs de cada tipo de POI con su Trigger Volume embebida, mecanismo de reseed entre temporadas |

## 15. Roadmap sugerido (fases)

1. **Vertical slice**: un `WorldStructureAsset` mínimo (una sola zona de mina
   de oro alrededor de un HUB) con un prefab de mina que trae su propia
   Trigger Volume embebida (tags + efectos `ClaimTerritory`/`CollectMine`),
   más la lógica de plugin (wallet, tokens, producción). Sin NPCs ni
   dungeons todavía. Verifica dos cosas a la vez: que la Trigger Volume
   embebida en un prefab se registra bien al generarse
   (`VolumeEntry.FromWorldGen`, ver `technical-architecture.md` §3.1) y el
   ciclo completo de instancia (spawn, timeout simulado a minutos para
   pruebas, cierre, snapshot de ranking).
2. Ampliar a las 6 zonas completas (gemas, spawners, recompensas, parcelas,
   dungeons) y resolver los tres pendientes de generación procedural del §13.
3. Añadir spawners de mobs (reusar CMS) y parcelas construibles.
4. Añadir NPCs recolector/guerrero y persistencia de producción offline.
5. Añadir mini-dungeons propias con cooldown diario.
6. Pulido de economía, catálogo completo de recompensas, racha, ranking.
7. (Futuro, fuera de esta fase) integración de monetización externa sobre el
   hook de datos ya reservado.
