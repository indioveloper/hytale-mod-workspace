# Desert Survival: estado (2026-11-17)

Resumen corto para retomar con cualquier herramienta. Distingue tres cosas
que suelen confundirse: la version en fuentes, el artefacto instalado en este
PC y lo que el usuario ha confirmado dentro del juego. Detalle narrativo en
[docs/HANDOFF_2026-11-16.md](docs/HANDOFF_2026-11-16.md) y cronologia en
`DESERT_SURVIVAL_CONTINUATION_2026-11-12.md` (raiz del workspace).

Runtime: Hytale estable `0.7.7`. `UserData` = `%APPDATA%\Hytale\UserData`.
El usuario hace los smoke tests; no abrir el juego desde el agente.

El [plan maestro del 2026-11-17](docs/DESERT_SURVIVAL_MASTER_PLAN_2026-11-17.md)
actualiza decisiones de diseño sobre cutterrays, permisos de blueprints,
Códice y desierto profundo. Es planificación: no cambia las versiones,
artefactos instalados ni pruebas confirmadas de la tabla siguiente.

## Tabla de estado

| Prototipo | Fuentes | Instalado (donde) | SHA-256 instalado | Probado en juego |
| --- | --- | --- | --- | --- |
| Cutterray T3 | 0.1.9 | 0.1.9 (`UserData/Mods`) | `6587F8E9...D4C9` | Extraccion de cobre/hierro si (sesiones previas). Celulas de combustible: nodos visibles en el mundo nuevo; extraccion sin confirmar. |
| Vitals | **0.2.6** | **0.2.2** (`UserData/Mods`) | `F0CB1C41...EA86` | 0.2.2 si. 0.2.3-0.2.6 desconectaban al entrar (Custom UI); no instalar 0.2.6 sin aislar su UI. |
| Research | 0.2.8 | 0.2.8 (`UserData/Mods`) | `F16AD31FC800B57C08FF93E92C8DDD897406B5B8410A7BB11F467D95F788712E` | `/research` y rueda si (0.1.8-0.2.2). Cuchillo de chatarra, extractor improvisado y bolsa: **sin confirmar**. 0.2.8 actualiza la descripción del extractor. |
| Build Selector | 0.6.2 | 0.6.2 (`Saves/dune1/mods`) | `AE6BA480...55E5` | Hasta 0.5.x si en parte. 0.6.x (generador con inventario, purificador de sangre, filtro de recetas vanilla): **pendiente**. |
| WorldGen (desert-worldgen) | **0.4.1** | **0.4.1 ZIP** (`UserData/Mods`) | `F478B0AE03B26476BF5299B0397E0F27D93906A1545F0BDE61217DF2AEFAB570` | Los cuatro tiers de base y sus cofres se vieron en versiones anteriores. **0.4.1 pendiente**: modelos de cofre por tier y cofre de llave garantizado en bases T2-T4. Los chunks existentes no se regeneran. |
| Enemy Base | **0.3.2** | **0.3.2 JAR** (`UserData/Mods`) | `36AE56218A91B1815956C8C1EDC05BBDBCD0A674AFD48F6A63334F5CD1274BB2` | **Pendiente**: 0.3.1 permitió saquear y extraer sangre, pero sin prompt; al recargar chunk el cadáver aparecía erguido. 0.3.2 envía hint nativo, añade nombre visible, inclina el cuerpo tras la animación y garantiza llave en espadachines. Véase `prototypes/enemy-base/README.md`. |
| Companion web | `companion/` | sin desplegar | - | Probado solo en navegador local (visor del arbol, zoom, paneo, simulacion). |

Hashes anteriores en `AGENTS.md` y en el handoff; hashes nuevos completos arriba. Los `.jar.replaced*` y
`.failed-ui` junto a los instalados no estan activos; no dejar dos artefactos
activos con el mismo `Group:Name`.

## Git

La rama `codex/desert-survival-docs-2026-11-17` publica únicamente Markdown
actualizado para consultarlo desde otro PC. Parte del commit `c7277ff`; los
cambios locales de código, assets y builds siguen **sin commit** en el PC de
desarrollo, incluyendo `research-tree`, `cutterray-t3`, `build-selector/`,
`desert-worldgen/`, `enemy-base/`, `companion/`, `.claude/launch.json` y
`output/`. No inferir que el código fuente publicado en GitHub coincide con
los JAR instalados. Revisar `git status` antes de editar y no descartar esos
cambios locales. Para las sesiones de diseño sin Hytale, empezar por
[`DOCS_INDEX.md`](DOCS_INDEX.md).

## Handoff breve: que falta

1. Smoke test de lo instalado sin cambiar JARs: Research 0.2.7 (receta del
   cuchillo en Herramientas tras investigar `CO-0.2`; extractor y bolsa tras
   `AG-0.1`), Build Selector 0.6.2 (generador con fuel, purificador, recetas
   vanilla ocultas), extraccion de celulas de combustible con el cutterray.
   Anotar resultado aqui y corregir antes de ampliar.
2. WorldGen, siguiente paso del plan (`docs/WORLDGEN_DESERT_PLAN.md`): frontera
   jugable real (hoy el bioma `Void` solo corta el terreno a 1400 bloques),
   balance de densidad de nodos y celulas por zona, ruinas propias con loot de
   chatarra en vez de los prefabs vanilla, exclusion entre prefabs y nodos,
   varias seeds. Reglas aprendidas: bloques sueltos con prop `Column`
   (`Locator` + `Manual` no coloca), `Mesh.Jitter` <= 0.5, el mod que define
   un bloque custom debe estar activo en el save (si no, `Unknown`).
   Regenerar biomas con `python -I tools/generate_biomes.py` y construir con
   `tools/Build-Pack.ps1`.
3. Vitals: decidir que hacer con el desfase 0.2.6 fuentes / 0.2.2 instalado
   (aislar la Custom UI y probar en una copia de `dune1`).
4. Bucle vertical de materiales: chatarra -> investigacion -> receta ->
   fabricacion -> energia/purificacion, con refineria y fabricador reales.
5. Companion: desplegar junto a `website/raynor-mods`, fichas de objeto con
   receta, iconos por nodo; lore y progreso real del personaje despues.
6. Commit del trabajo acumulado (el usuario decide cuando).

## Base enemiga añadida el 2026-11-16

WorldGen 0.3.0 sustituye los puestos vanilla de zona 3 por un prefab original
de 15x15 con dos spawn markers. `Enemy Base` 0.1.0 implementa aggro y botín
saqueable. Compilan/pasan validación estructural y los codecs de WorldGen
contra 0.7.7; los dos artefactos están instalados con hash verificado. El
WorldGen 0.2.0 quedó archivado como `.zip.replaced-enemybase-20261116`.
**No hay smoke test todavía** de la nueva base, NPC, arma ni cadáver. La
instancia antigua guardada puede seguir con `IsSpawningNPC=false`; usar la
instancia nueva para probar la mecánica sin cambiar el save anterior.

## Corrección de cadáveres 2026-11-16

El log de `dune2` mostró `Desert Survival Enemy Base Prototype` desactivado
por la configuración del save. Se habilitó y se instaló Enemy Base 0.2.0
junto a Research 0.2.8. Los JAR anteriores y el `config.json` original tienen
copias de respaldo junto a los instalados. Hashes completos en la tabla.
La nueva interacción usa `UseEntityEvent.Pre`, garantiza el objeto cadáver en
el inventario, y convierte bolsas vacías en bolsas de sangre al usar el
extractor sobre el cuerpo. El saqueo y la extracción aún requieren smoke test
en juego; el agente no abrió Hytale.

El smoke test posterior a 0.2.0 reveló en el log `Failed to load ... because
the dependency 'RaynorMods:Desert Survival WorldGen' could not be found`.
WorldGen sí cargó como pack de assets, pero PluginManager no lo admite como
dependencia Java obligatoria. Enemy Base 0.2.1 elimina esa dependencia y está
instalado con hash verificado. **Aún no está confirmado que cargue ni que los
cadáveres sean interactuables tras reiniciar**; comprobar el siguiente log.

El siguiente log confirmó la carga de 0.2.1 y el usuario pudo extraer sangre,
pero seguía sin prompt ni inventario saqueable. Enemy Base 0.2.2 evita
cancelar el evento de uso, agrega `InteractionHint` traducido y abre el
contenedor desde la raíz de interacción del cadáver. Compiló contra 0.7.7 y
el JAR instalado coincide por SHA-256. Smoke test del prompt y botín pendiente.

## Cadáveres y cofres: revisión 2026-11-17

En la prueba de Enemy Base 0.3.0, el usuario confirmó que no aparecía el prompt
de cadáver ni funcionaba la extracción de sangre. El log del cliente
`2026-11-17_07-42-47_client.log` contiene `Unused key(s)` para `Type` y `Next`
en `Desert_Corpse_Loot.json`: el documento de `RootInteractions` era inválido.
Enemy Base 0.3.1 usa `Interactions` con la interacción custom, siguiendo el
formato que ya funciona en Research. También deja el botín normal como drops
del suelo al morir; el objeto cadáver permanece en el inventario del cuerpo.

WorldGen 0.4.1 distingue cofres común, raro, ultrarraro y especial mediante
modelos vanilla diferentes y luz en el especial. Añade un cofre de llave
garantizado cerca de la entrada en T2, T3 y T4. Las bases ya generadas en
chunks guardados conservan su contenido anterior; probar las llaves en chunks
nuevos o en una instancia nueva. Ambas builds compilaron contra el servidor
0.7.7, pasaron las pruebas de paquete y codecs, y se instalaron con SHA-256
verificado. No hay duplicados activos en los saves. Falta probar dentro del
juego la interacción, extracción, drops y aspecto de los cofres.

## Prompt, postura y llave: revisión 2026-11-17

El usuario confirmó en Enemy Base 0.3.1 que podía saquear cadáveres y extraer
sangre. No veía el prompt de uso, el NPC muerto volvía a verse de pie al
descargar y recargar el chunk, y no encontraba la llave. El log
`2026-11-17_08-03-21_client.log` confirma que Enemy Base 0.3.1 y WorldGen
0.4.1 cargaron; no repite el error de la interacción del cadáver.

Enemy Base 0.3.2 envía `InteractableUpdate` con el hint a quienes ven el
cadáver y pone una indicación en su nombre. Tras dos segundos inclina 90 grados
el transform de la entidad muerta y vuelve a aplicarlo en ticks posteriores;
esto debe verificarse tras alejarse y regresar. El cadáver de cada espadachín
contiene una llave garantizada como vía de prueba para puestos en chunks
anteriores a WorldGen 0.4.1. Los cofres de llave de T2-T4 solo aparecen al
generar chunks nuevos. La llave transferida al inventario se consume de
inmediato para aumentar el contador persistente del personaje, con aviso en
chat. 0.3.2 compila contra 0.7.7 y está instalado con SHA-256 igual al build;
0.3.1 quedó respaldado. El usuario aún debe validar el resultado en juego.
