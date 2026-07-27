# Contexto para continuar: Hytale Scoreboards Mod

Fecha: 2026-05-17

## Objetivo

Mod de Hytale para abrir una interfaz grafica con `/scoreboard` y crear/editar scoreboards.

## Problema original

El mod cargaba en el mundo y anadia comandos, pero al usar la UI el juego crasheaba. La linea sospechosa era:

```java
commands.append("Common/UI/Custom/Pages/Scoreboards/ScoreboardEditor.ui");
```

## Hallazgos

1. El `.jar` original en `C:\Users\andre\Downloads\Scoreboards_Mod-1_0_0_repo.jar` si tenia:
   - `Common/UI/Custom/Pages/Scoreboards/ScoreboardEditor.ui`

2. Pero no tenia:
   - `Common/UI/Custom/Common.ui`

   Esa UI comun declara tipos como `TextField`, `TextButton`, `Group`, `Label`, etc. Sin eso, la UI puede fallar al cargarse o parsearse.

3. En logs de Hytale aparecia otro problema claro:

```text
Invalid permission node: orbgenesis.scoreboards mod.command.scoreboards
```

La causa era el manifest:

```json
"Name": "Scoreboards Mod"
```

El espacio en `Scoreboards Mod` hacia que Hytale generase permisos invalidos. Se cambio a:

```json
"Name": "scoreboards"
```

4. El mod original registraba varios comandos/aliases:
   - `/scoreboards`
   - `/scoreboardui`
   - `/scoreui`

   Se pidio dejar solo:
   - `/scoreboard`

## Cambios hechos

Carpeta fuente corregida:

```text
C:\Users\andre\Documents\Mod interfaces\scoreboards_fixed
```

Cambios principales:

1. `manifest.json`
   - `Name` cambiado de `"Scoreboards Mod"` a `"scoreboards"`.

2. `ScoreboardsCommand.java`
   - comando principal cambiado de `"scoreboards"` a `"scoreboard"`.

3. `ScoreboardsPlugin.java`
   - se dejo solo el registro de `new ScoreboardsCommand()`.
   - se quitaron registros de aliases.

4. El `.jar` nuevo incluye assets completos:
   - `Common/UI/Custom/Common.ui`
   - `Common/UI/Custom/Pages/Scoreboards/ScoreboardEditor.ui`
   - `Server/Languages/en-US/scoreboards.lang`

## Jar final generado

```text
C:\Users\andre\Documents\Mod interfaces\Scoreboards_Mod-1_0_0_scoreboard_only_20260517_175647.jar
```

## Verificacion hecha

Se verifico con `jar tf` que el `.jar` contiene los archivos UI completos.

Se verifico con `javap` que:

- `ScoreboardsPlugin.setup()` solo registra `ScoreboardsCommand`.
- `ScoreboardsCommand()` usa el nombre de comando `"scoreboard"`.

Nota: `AliasCommand.class` sigue dentro del `.jar`, pero no se registra ni se usa.

## Pendiente

1. El 2026-05-17 20:56 se detecto que el juego no reconocia `/scoreboards`
   porque el JAR cargado era la version `scoreboard_only`.

2. Se genero un JAR nuevo que registra ambos comandos:

```text
/scoreboard
/scoreboards
```

3. Artefacto generado:

```text
C:\Users\andre\Documents\New project\mod-interfaces\Mod interfaces\Scoreboards_Mod-1_0_0_scoreboard_and_scoreboards_20260517_2055.jar
```

4. Se reemplazo el JAR cargado por Hytale en:

```text
C:\Users\andre\AppData\Roaming\Hytale\data\pre-release\Mods
```

El archivo reemplazado conserva el nombre:

```text
Scoreboards_Mod-1_0_0_scoreboard_only_20260517_175647.jar
```

pero su contenido ahora es el JAR corregido. Hash SHA256 verificado:

```text
F1BD7151B445C92DAE5E58C615BC000D978815CD29DF63AAF36A15ACC813461E
```

5. Tras probar en juego, el 2026-05-17 se vio que no reconocia ningun comando
   `/scoreb...`. El log del servidor mostro que Hytale ni cargaba el plugin:

```text
Failed to load manifest file!
java.io.IOException: Unexpected character: feff, 'ï»¿' expected '{'!
```

La causa era que `manifest.json` estaba guardado con BOM UTF-8. Se convirtio a
UTF-8 sin BOM, se reempaqueto y se copio de nuevo al JAR cargado por Hytale.

Artefacto sin BOM:

```text
C:\Users\andre\Documents\New project\mod-interfaces\Mod interfaces\Scoreboards_Mod-1_0_0_scoreboard_and_scoreboards_no_bom_20260517_2100.jar
```

Hash SHA256 verificado contra el JAR copiado a Mods:

```text
316BCCFE269EBC7938CC84DD0A20CF90682CA5C4FA8118556FADAF3648CAF04C
```

Tambien se verifico que el `manifest.json` dentro del JAR empieza con `{`
(`7B`) y no con BOM (`EF BB BF`).

6. Quitar o reemplazar versiones anteriores del mod si se vuelven a copiar para evitar duplicados.

7. Reiniciar el mundo/servidor para que Hytale vuelva a cargar el JAR y probar:

```text
/scoreboards
/scoreboard
```

8. Tras probar, la UI cargaba pero se veia negra. Se comparo con:

```text
C:\Users\andre\Downloads\Scoreboards_Mod-1_0_0_v2 (1).jar
```

Ese JAR antiguo cargaba comandos, pero fallaba al usar el comando porque
`IncludesAssetPack` estaba en `false`, aunque si contenia:

```text
Common/UI/Custom/Pages/Scoreboards/ScoreboardEditor.ui
```

Diferencias encontradas:

- El JAR antiguo NO tenia `Common/UI/Custom/Common.ui`.
- El JAR nuevo anterior SI tenia `Common/UI/Custom/Common.ui`, con definiciones vacias.
- El JAR antiguo usaba `PageOverlay #ScoreboardEditorPage` como raiz de `ScoreboardEditor.ui`.
- El JAR nuevo anterior habia cambiado la raiz a `Group`.

Se genero una variante conservadora:

```text
C:\Users\andre\Documents\New project\mod-interfaces\Mod interfaces\Scoreboards_Mod-1_0_0_no_common_old_ui_20260517_2106.jar
```

Propiedades:

- Manifest sin BOM.
- `IncludesAssetPack: true`.
- Comandos `/scoreboard` y `/scoreboards`.
- Incluye `Common/UI/Custom/Pages/Scoreboards/ScoreboardEditor.ui`.
- NO incluye `Common/UI/Custom/Common.ui`.
- Restaura la raiz `PageOverlay #ScoreboardEditorPage`.

Se copio a:

```text
C:\Users\andre\AppData\Roaming\Hytale\data\pre-release\Mods\Scoreboards_Mod-1_0_0_scoreboard_only_20260517_175647.jar
```

Hash SHA256 verificado:

```text
187EEA7A039F11BECC2BA7DD7F2A22F370CBFAB6BA60A00B10DEF5271C0C816B
```

9. Si crashea otra vez, revisar logs recientes:

10. Tras seguir viendose negro, se confirmo por log que el mod si cargaba:

```text
Loaded pack: OrbGenesis:scoreboards
Enabled plugin OrbGenesis:scoreboards
```

Se inspeccionaron UIs nativas de `Assets.zip`. Las paginas reales usan:

```text
$C = "../Common.ui";
$C.@Container { ... }
$C.@TextField { ... }
$C.@TextButton { ... }
```

Para una pagina dentro de `Common/UI/Custom/Pages/Scoreboards`, la ruta correcta es:

```text
$C = "../../Common.ui";
```

Se reescribio `ScoreboardEditor.ui` con componentes nativos:

- `$C.@Container`
- `$C.@TextField`
- `$C.@TextButton`
- `$C.@SecondaryTextButton`
- `$C.@CancelTextButton`

Artefacto copiado a Mods:

```text
C:\Users\andre\Documents\New project\mod-interfaces\Mod interfaces\Scoreboards_Mod-1_0_0_native_container_ui_20260517_2124.jar
```

Hash SHA256 verificado:

```text
185948B0873BB46F02E173434CFD1D0B5ECF104776888312F979B5BCBA023938
```

11. Tras probar, el juego reconocia el comando y la UI ya no se veia negra,
    pero crasheaba con:

```text
Could not find document Common/UI/Custom/Pages/Scoreboards/ScoreboardEditor.ui for Custom UI Append command.
```

El client log mostro que el cliente cachea el asset como:

```text
UI/Custom/Pages/Scoreboards/ScoreboardEditor.ui
```

es decir, sin el prefijo `Common/`. Se corrigio `ScoreboardEditorPage.java`:

```java
commands.append("UI/Custom/Pages/Scoreboards/ScoreboardEditor.ui");
```

Artefacto copiado a Mods:

```text
C:\Users\andre\Documents\New project\mod-interfaces\Mod interfaces\Scoreboards_Mod-1_0_0_asset_path_fixed_20260517_2129.jar
```

Hash SHA256 verificado:

```text
BDECFB62355FD91D603DAEC3D1FF781C577F8E7A355254CE2432349837CBFA1D
```

12. El error real tras la correccion anterior era:

```text
Could not find document UI/Custom/Pages/Scoreboards/ScoreboardEditor.ui
```

Se inspecciono bytecode nativo de Hytale:

- `PlaySoundPage` usa `commands.append("Pages/PlaySoundPage.ui")`.
- `CommandListPage` usa `commands.append("Pages/CommandListPage.ui")`.
- `TriggerVolumeEffectEditorPage` usa `commands.append("Pages/TriggerVolumeEffectEditor.ui")`.

Conclusion: `UICommandBuilder.append()` espera paths relativos a
`Common/UI/Custom`, no relativos a `Common` ni a `Common/UI/Custom` escrito
entero.

Se corrigio `ScoreboardEditorPage.java`:

```java
commands.append("Pages/Scoreboards/ScoreboardEditor.ui");
```

Artefacto copiado a Mods:

```text
C:\Users\andre\Documents\New project\mod-interfaces\Mod interfaces\Scoreboards_Mod-1_0_0_append_pages_path_20260517_2137.jar
```

Hash SHA256 verificado:

```text
7E1FE69BDF729688ECCE6232030C0AD08FED58936CA90D08BB798DE1FCF03A53
```

13. La UI ya abre y los campos de texto se pueden editar, pero los botones no
    ejecutaban ninguna accion. Se ajustaron los bindings de
    `ScoreboardEditorPage.java` para seguir el patron nativo:

- Los cambios de texto usan claves con `@`, por ejemplo `@Title`.
- Los botones usan eventos `Activating` con claves directas: `Apply`, `Hide`,
  `Reset`.
- `Apply` envia `TrackOrUpdateObjective`, muestra mensaje y cierra la UI.
- `Hide` envia `UntrackObjective`, muestra mensaje y cierra la UI.
- `Reset` restaura valores y repinta la UI sin cerrarla.

Artefacto copiado a Mods:

```text
C:\Users\andre\Documents\New project\mod-interfaces\Mod interfaces\Scoreboards_Mod-1_0_0_button_events_20260517_2239.jar
```

Hash SHA256 verificado:

```text
C276FCF48A35C1C7776BC56FCD3E73C933724198A099074C16A19CBC1F571317
```

14. Tras probar, los botones seguian sin hacer nada. Se comparo con paginas
    nativas (`PlaySoundPage`, `TriggerVolumeEffectEditorPage`) y se encontro
    que los botones nativos usan:

```java
events.addEventBinding(CustomUIEventBindingType.Activating, "#Play",
    EventData.of("Type", "Play"), false);
```

Se cambio la UI de scoreboards a un campo unico `Action` y bindings de cuatro
argumentos con `false`:

```java
events.addEventBinding(CustomUIEventBindingType.Activating, "#ApplyButton",
    EventData.of("Action", "Apply"), false);
```

`handleDataEvent` ahora acepta `Action = Apply|Hide|Reset`.

Artefacto copiado a Mods:

```text
C:\Users\andre\Documents\New project\mod-interfaces\Mod interfaces\Scoreboards_Mod-1_0_0_button_action_20260517_2250.jar
```

Hash SHA256 verificado:

```text
D17315EADF5EB2369F2A41EDDDF3DBC8F69CCC88CE8F6D3DC3A19161C34BD379
```

15. Tras probar, los botones seguian sin enviar eventos. Se reviso el `.ui` y
    se encontro que los componentes reutilizables `$C.@TextButton`,
    `$C.@SecondaryTextButton` y `$C.@CancelTextButton` esperan el tamano en
    `@Anchor`; los botones del mod estaban usando `Anchor`, a diferencia de las
    paginas nativas. Se corrigio:

```ui
$C.@TextButton #ApplyButton {
  @Text = "Apply";
  @Anchor = (Width: 140);
}
```

Artefacto copiado a Mods:

```text
C:\Users\andre\Documents\New project\mod-interfaces\Mod interfaces\Scoreboards_Mod-1_0_0_button_anchor_20260517_2258.jar
```

Hash SHA256 verificado:

```text
37BF2DA43D908ECBC241DEBC088DE38F2F40C2AE93803C596958E3E5AB0CB353
```

16. Tras probar, los botones seguian sin enviar eventos. Se elimino la capa de
    macros `$C.@TextButton` en los tres botones y se reemplazaron por nodos
    `TextButton` directos con `Text`, `Anchor`, `Padding` y `Style` explicitos,
    para que los selectores `#ApplyButton`, `#HideButton` y `#ResetButton`
    apunten al nodo clicable real.

Artefacto copiado a Mods:

```text
C:\Users\andre\Documents\New project\mod-interfaces\Mod interfaces\Scoreboards_Mod-1_0_0_direct_buttons_20260517_2307.jar
```

Hash SHA256 verificado:

```text
09B5C7DE553848037947CB7DBFB134FB271C9ED0C1E7B571E3C72166EB2ACE92
```

17. Tras probar, seguia sin haber hover ni clicks. Se interpreto como problema
    de layout/hitbox, no de Java. Se cambio el bloque de lineas a altura fija
    para evitar que solape la fila inferior, y la fila de botones se adapto al
    patron nativo:

```ui
Group #BottomButtons {
  LayoutMode: Center;
  Anchor: (Top: 8, Height: 44);
}
```

Los botones directos ahora usan `FlexWeight: 1` y anchors tipo
`Right/Left`, como en `TriggerVolumeEffectEditor.ui`.

Artefacto copiado a Mods:

```text
C:\Users\andre\Documents\New project\mod-interfaces\Mod interfaces\Scoreboards_Mod-1_0_0_button_layout_20260517_2316.jar
```

Hash SHA256 verificado:

```text
DC63B7757648498235D594357C7E28A4A4AB7B5E460AB3C6E1130A4FC70B50B3
```

18. Se anadio una fila de botones superior (`#ApplyTopButton`,
    `#HideTopButton`, `#ResetTopButton`) para diagnosticar si el problema era
    solo la zona inferior. El usuario confirmo que funciono: `Apply` cierra la
    UI, muestra `Scoreboard applied.` y crea el scoreboard lateral.

Conclusion: Java y bindings funcionan; el fallo era la zona inferior/hitbox del
layout. Se preparo una build limpia quitando los botones inferiores muertos y
dejando solo la fila superior funcional.

Artefacto copiado a Mods:

```text
C:\Users\andre\Documents\New project\mod-interfaces\Mod interfaces\Scoreboards_Mod-1_0_0_working_buttons_clean_20260517_2334.jar
```

Hash SHA256 verificado:

```text
972F8806DFD3CECDCC1A59D3BB2E17CB81CECF48BE77FA03505E4AB7F52BCE93
```

19. Primer pase de logica real:

- `ScoreboardsPlugin` crea un `ScoreboardTracker` compartido.
- `ScoreboardTracker` registra los scoreboards activos por jugador.
- Se registra listener global de `EntityRemoveEvent`.
- En removal, si la entidad tiene `DeathComponent`, fuente de daño tipo
  `Damage.EntitySource`, killer `PlayerRef`, y `ModelComponent` cuyo
  `ModelAssetId` contiene `skeleton`, incrementa la primera linea del
  scoreboard del jugador.
- Al incrementar, envia `TrackOrUpdateObjective` de nuevo y mensaje:
  `Skeleton kill counted: X/Y`.
- La UI arranca con 1 linea por defecto (`Skeletons killed`) y boton
  `Add line` para mostrar hasta 5 lineas.

Notas:

- La descripcion viaja en el `Objective`, pero el panel lateral nativo no la
  renderiza visualmente en la captura; solo muestra titulo y tareas.
- La persistencia y visibilidad por scoreboard quedan pendientes para una
  siguiente iteracion.

Artefacto copiado a Mods:

```text
C:\Users\andre\Documents\New project\mod-interfaces\Mod interfaces\Scoreboards_Mod-1_0_0_skeleton_logic_20260517_2345.jar
```

Hash SHA256 verificado:

```text
9A6862B80B949413CE0520534CAF8FE991A4DE2756DE11D19347438D31B10319
```

20. La build anterior crasheo al abrir la UI con:

```text
CustomUI Set command couldn't set value. Selector: #LineRow0.Visible
```

Se quitaron los `commands.set("#LineRowX.Visible", ...)` y
`#AddLineButton.Disabled` dinamicos. La build queda estable con una sola linea
visible; las lineas 2-5 estan marcadas `Visible: false` en el `.ui` y
`Add line` queda desactivado temporalmente. La gestion dinamica de filas se
hara despues con un patron distinto, probablemente reconstruyendo el contenedor
o usando append/remove de nodos.

Artefacto copiado a Mods:

```text
C:\Users\andre\Documents\New project\mod-interfaces\Mod interfaces\Scoreboards_Mod-1_0_0_logic_no_visible_set_20260518_0001.jar
```

Hash SHA256 verificado:

```text
A773826D2A7965CBFCAF1818D22A079D21B974845EA0CAC5BD9AA05C0841918E
```

21. Se detecto que el panel nativo `/objective panel` / `/objectives` no
    mostraba nada aunque el scoreboard lateral funcionaba. Causa: el mod solo
    enviaba paquetes `TrackOrUpdateObjective` al HUD, pero el panel nativo lee
    `ObjectivePlugin.get().getObjectiveDataStore().getObjectiveCollection()`.

Se inspeccionaron clases nativas:

- `ObjectiveCommand` registra `/objective` con subcomandos `start`, `panel`,
  `complete`, `history`, etc.
- `ObjectivePanelCommand` abre `ObjectiveAdminPanelPage`.
- `ObjectiveAdminPanelPage` lista `ObjectiveDataStore.getObjectiveCollection()`.
- `ObjectiveStartCommand` usa `ObjectivePlugin.get().startObjective(...)` con
  assets de `Server/Objective/Objectives`.

Cambio aplicado:

- `ScoreboardTracker.apply(...)` ahora registra tambien un
  `com.hypixel.hytale.builtin.adventure.objectives.Objective` nativo en el
  `ObjectiveDataStore`, con el mismo UUID del scoreboard visual.
- Se marca el jugador como activo en el objective nativo y en
  `PlayerConfigData.getActiveObjectiveUUIDs()`.
- `Hide` elimina ese objective nativo y lo quita de la config del jugador.

Esto une el scoreboard visual actual con el listado nativo del panel de
objectives. Es una primera integracion: la logica de progreso sigue siendo la
del `ScoreboardTracker` propio; mas adelante se debe decidir si migrar cada tipo
de evento a tasks nativas (`KillNPC`, `Gather`, `Craft`, etc.) o mantener un
motor propio que sincronice con `ObjectiveDataStore`.

Artefacto copiado a Mods:

```text
C:\tmp\scoreboards_native_build\Scoreboards_Mod-1_0_0_native_objectives_20260518_0023.jar
```

Archivo activo reemplazado:

```text
C:\Users\andre\AppData\Roaming\Hytale\data\pre-release\Mods\Scoreboards_Mod-1_0_0_scoreboard_only_20260517_175647.jar
```

Hash SHA256 verificado:

```text
C82E2E659178BE4F52D3B963608964E2230A80944C93CD9DFA4606A995918210
```

22. La build del punto 21 crasheo al abrir `/scoreboards` con:

```text
Could not find document Pages/Scoreboards/ScoreboardEditor.ui for Custom UI Append command.
```

La causa no era Java ni el path de `commands.append("Pages/Scoreboards/ScoreboardEditor.ui")`,
que seguia siendo correcto, sino el empaquetado del `.jar`: por error los assets
se metieron bajo:

```text
assets/Common/UI/...
```

en vez de en la raiz esperada:

```text
Common/UI/...
```

Se comparo contra las builds funcionales (`working_buttons_clean` y
`logic_no_visible_set`) y ambas guardaban `Common/UI/...` en la raiz del `.jar`.

Se rehizo el empaquetado correcto copiando:

- `assets/Common` -> `Common`
- `assets/Server` -> `Server`
- `classes/gg` -> `gg`
- `manifest.json` en raiz

Artefacto corregido:

```text
C:\tmp\scoreboards_native_build\Scoreboards_Mod-1_0_0_native_objectives_fixed_pack_20260518_2325.jar
```

Archivo activo reemplazado:

```text
C:\Users\andre\AppData\Roaming\Hytale\data\pre-release\Mods\Scoreboards_Mod-1_0_0_scoreboard_only_20260517_175647.jar
```

Hash SHA256 verificado:

```text
592CDC7666F411ED48C70249E93B649E416948D4175036B8F058A732E782178E
```

23. Tras corregir el path de empaquetado, volvio un bug peor: el juego se
    quedaba negro al entrar al mundo y no terminaba de cargar.

Comparacion entre la build estable `logic_no_visible_set` y la build nueva:

- La build estable NO incluia `Common/UI/Custom/Common.ui`.
- La build nueva SI incluia `Common/UI/Custom/Common.ui`.

Ese `Common.ui` era un stub/minimo y estaba pisando el Common UI global de
Hytale, provocando que el mundo se quedara negro durante la carga. Se rehizo el
jar conservando:

- `Common/UI/Custom/Pages/Scoreboards/ScoreboardEditor.ui`
- `Server/Languages/en-US/scoreboards.lang`
- `gg/orbgenesis/scoreboards/*.class`
- `manifest.json`

pero eliminando por completo:

- `Common/UI/Custom/Common.ui`

Artefacto corregido:

```text
C:\tmp\scoreboards_native_build\Scoreboards_Mod-1_0_0_native_objectives_no_common_20260518_2330.jar
```

Archivo activo reemplazado:

```text
C:\Users\andre\AppData\Roaming\Hytale\data\pre-release\Mods\Scoreboards_Mod-1_0_0_scoreboard_only_20260517_175647.jar
```

Hash SHA256 verificado:

```text
08FD948823D4B0C34AF61F9284B56F4606B3A7BD2CBE2D20C9820BD4C265731F
```

24. El objective ya aparecia en `/objective panel`, pero matar un esqueleto no
    actualizaba el progreso. La sospecha se confirmo al inspeccionar el codigo
    nativo de Hytale:

- Los objetivos nativos de matar NPC no esperan a `EntityRemoveEvent`.
- Usan `DeathSystems$OnDeathSystem`, es decir, reaccionan cuando se anade
  `DeathComponent` a la entidad.

La implementacion anterior del mod escuchaba `EntityRemoveEvent`, que llega mas
tarde y puede dejar la entidad sin los componentes necesarios o con referencias
menos fiables.

Se cambio la arquitectura:

- `ScoreboardsPlugin` deja de registrar `EntityRemoveEvent`.
- Registra `ScoreboardDeathSystem`, una clase nueva que extiende
  `DeathSystems.OnDeathSystem`.
- `ScoreboardDeathSystem` filtra entidades con `ModelComponent` y delega en
  `ScoreboardTracker.handleDeath(...)`.
- `ScoreboardTracker.handleDeath(...)` centraliza el conteo:
  - comprueba `DeathComponent`
  - comprueba si el modelo contiene `skeleton`
  - resuelve el killer
  - incrementa la primera linea
  - reenvia `TrackOrUpdateObjective`
  - manda mensaje `Skeleton kill counted: X/Y`

Artefacto generado y copiado a Mods:

```text
C:\tmp\scoreboards_native_build\Scoreboards_Mod-1_0_0_death_system_20260518_2341.jar
```

Archivo activo reemplazado:

```text
C:\Users\andre\AppData\Roaming\Hytale\data\pre-release\Mods\Scoreboards_Mod-1_0_0_scoreboard_only_20260517_175647.jar
```

Hash SHA256 verificado:

```text
FDFE7D995942086B95CF74746D51A159BF4445493C701B824ABEBE8FAE56637E
```

25. Se mejoro la UI y la base de logica para futuros tipos de scoreboard:

- `Add line` vuelve a estar activo.
- Ya no depende de `Visible` ni de `commands.set("#LineRowX.Visible", ...)`,
  que crasheaba.
- La UI ahora reconstruye `#LineRows` con `commands.clear("#LineRows")` +
  `commands.append(...)` usando parciales:
  - `LineRow0.ui`
  - `LineRow1.ui`
  - `LineRow2.ui`
  - `LineRow3.ui`
  - `LineRow4.ui`

Esto permite empezar con 1 linea y anadir mas sin volver al bug anterior.

Tambien se corrigio un problema de captura de datos:

- Al pulsar `Apply` o `Add line`, si el cursor seguia dentro del `TextField`,
  el ultimo valor tecleado podia no llegar al backend.
- Se anadieron bindings `Activating` extra para enviar snapshot actual de:
  - `@Title`
  - `@Description`
  - `@LineId`
  - `@Task0..4`
  - `@Current0..4`
  - `@Needed0..4`

Con eso, cambiar el titulo justo antes de pulsar `Apply` ya deberia reflejarse
correctamente en el HUD.

Ademas, el tracker quedo un paso mas generalizado:

- `ScoreboardDefinition` ahora usa `TriggerType[]` por linea.
- Por ahora solo esta implementado:
  - `SKELETON_KILL`
- El resto de lineas se crean con `NONE`.

Esto deja preparada la estructura para soportar mas tipos de evento sin tener
que rehacer otra vez el modelo interno.

Artefacto generado y copiado a Mods:

```text
C:\tmp\scoreboards_native_build\Scoreboards_Mod-1_0_0_add_line_and_title_20260518_2348.jar
```

Archivo activo reemplazado:

```text
C:\Users\andre\AppData\Roaming\Hytale\data\pre-release\Mods\Scoreboards_Mod-1_0_0_scoreboard_only_20260517_175647.jar
```

Hash SHA256 verificado:

```text
CBC3C088ABF8B4A4C7A26F8C982E5B54D3C8350240EA1FDEC9465C530D29A02A
```

```text
C:\Users\andre\AppData\Roaming\Hytale\data\pre-release\Logs
C:\Users\andre\AppData\Roaming\Hytale\data\pre-release\Saves\<nombre_del_mundo>\logs
```

## Detalles utiles

Durante compilacion con JDK 25 aparecio un error interno de `AccessDeniedException` al cerrar `HytaleServer.jar`, pero los `.class` se generaron correctamente y el `.jar` final se pudo empaquetar.
