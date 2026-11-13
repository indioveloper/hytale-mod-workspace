# Guía operativa para desarrollar mods de Hytale en este PC

Actualizado: 2026-09-05.

Este documento es un handoff para otro modelo que vaya a continuar el desarrollo
en este mismo ordenador. Resume el flujo de trabajo real del repositorio, las
fuentes de verdad y, sobre todo, los fallos no evidentes que ya costaron varias
iteraciones de diagnóstico.

No sustituye la documentación de cada proyecto. Antes de modificar nada, lee en
este orden:

1. `C:\Users\andre\Documents\Hytale Mod Workspace\AGENTS.md`
2. `C:\Users\andre\Documents\Hytale Mod Workspace\README.md`
3. El `README.md`, `HANDOFF.md` o documento de aprendizajes del mod concreto.
4. Los cambios locales con `git status` y `git diff`.

La regla principal es sencilla: el repositorio es la fuente canónica del código,
pero el `HytaleServer.jar` exacto del canal que se va a probar es la fuente de
verdad para la API binaria. No uses antiguas carpetas de staging, JARs descargados
ni clases copiadas como base del desarrollo.

## 1. Mapa del entorno local

### Repositorio estable

```text
C:\Users\andre\Documents\Hytale Mod Workspace
```

La rama principal contiene los mods destinados a Hytale estable 0.6.x y puede
tener cambios del usuario aún sin commit. No limpies, reviertas ni sobrescribas
cambios que no sean parte explícita de la tarea.

### Worktree de Update 7 pre-release

```text
C:\Users\andre\Documents\Hytale-Mod-Workspace-update7
```

Rama:

```text
codex/update7-pre-release
```

Esa rama contiene variantes con nombres y versiones diferenciados para
`0.7.0-pre.1`. No mezcles sus manifests, artefactos ni cambios de compatibilidad
con la línea estable sin una decisión explícita.

### Instalaciones de Hytale

```text
%APPDATA%\Hytale\install\release\package\game\latest
%APPDATA%\Hytale\install\pre-release\package\game\latest
```

Archivos relevantes de cada canal:

```text
Server\HytaleServer.jar
Assets.zip
```

Logs recientes del cliente:

```text
%APPDATA%\Hytale\UserData\Logs
```

La ubicación de saves y mods ha cambiado entre builds antiguas. No codifiques
una ruta histórica de memoria: localiza el save real bajo `%APPDATA%\Hytale` y
comprueba tanto sus mods locales como los mods globales del canal.

### Java

Los builds recientes usan JDK 25. El runtime que incluye el juego puede ejecutar
Java, pero no necesariamente incluye `javac`. Verifica siempre:

```powershell
java -version
javac -version
jar --version
```

## 2. Acceso al Hytale Shared Source de GitHub

El repositorio oficial se consulta en:

```text
https://github.com/HypixelStudios/hytale-shared-source
```

En este PC está clonado fuera del monorepo, como dependencia de consulta:

```text
C:\Users\andre\Documents\hytale-shared-source
```

Estado validado al redactar este documento:

```text
remote: https://github.com/HypixelStudios/hytale-shared-source.git
branch: pre-release
commit: 634880ce888e66922881597667fb87fbc0851e12
```

Para clonarlo o actualizarlo usa el script del workspace:

```powershell
Set-Location 'C:\Users\andre\Documents\Hytale Mod Workspace'
.\scripts\Sync-HytaleSharedSource.ps1
```

El script usa HTTPS, crea por defecto `..\hytale-shared-source`, cambia a la
rama `pre-release` y hace `pull --ff-only`. No copies el Shared Source dentro del
repositorio Raynor y no versiones ninguna parte de él.

### Cómo usarlo correctamente

El Shared Source es excelente para:

- entender la arquitectura, tipos, codecs, sistemas ECS y patrones vanilla;
- buscar ejemplos reales de comandos, Custom UI, Trigger Volumes y assets;
- seguir cambios entre actualizaciones;
- comprobar intención y semántica que `javap` no puede mostrar.

Ejemplos de búsqueda:

```powershell
rg -n "CustomUIEventBindingType|UICommandBuilder" `
  'C:\Users\andre\Documents\hytale-shared-source'

rg -n "TriggerVolumeEffectEditorPage|PastePrefabEffect" `
  'C:\Users\andre\Documents\hytale-shared-source'

rg -n "EventTitleStyle|RequestCommonAssetsRebuild" `
  'C:\Users\andre\Documents\hytale-shared-source'
```

Sin embargo, la rama `pre-release` puede ir por delante o por detrás de la
instalación local. Por eso el Shared Source no decide por sí solo si un mod
compila contra estable. Ante cualquier discrepancia:

1. identifica la versión y revisión del `HytaleServer.jar` objetivo;
2. inspecciona esa clase con `javap` o el contenido del JAR;
3. compila contra ese JAR;
4. usa el Shared Source para comprender el diseño, no para inventar una firma.

Ejemplo:

```powershell
javap -classpath "$env:APPDATA\Hytale\install\release\package\game\latest\Server\HytaleServer.jar" `
  -p com.hypixel.hytale.server.core.HytaleServer
```

No incluyas credenciales, tokens de GitHub ni dependencias oficiales dentro de
commits o artefactos. El script de sincronización no necesita secretos embebidos.

## 3. Versiones y canales: no asumir compatibilidad

### Línea estable

La referencia actual es Hytale estable `0.6.3`, revisión:

```text
ff802bf5a538f7e4b1df43a575c72f9d2bebb504
```

Los manifests publicables estables usan:

```text
>=0.6.2 <0.7.0
```

Eso expresa la compatibilidad declarada, pero las pruebas más recientes se han
ejecutado contra 0.6.3. No afirmes que todas las 0.6.x están probadas solo porque
el rango las acepte.

### Update 7 pre-release

La referencia de la rama paralela es `0.7.0-pre.1`, revisión:

```text
e8b4d191fc98a977bf5546a951a7b25473d323e3
```

Un detalle poco evidente: escribir `0.7.0-pre.1` sin operador en `ServerVersion`
no impone igualdad exacta. Para aceptar únicamente esa build se usa:

```text
=0.7.0-pre.1
```

Las variantes estable y pre-release conservan el mismo `Group:Name` para no
romper datos persistentes. Eso significa que no se pueden instalar juntas,
aunque el nombre de los archivos JAR sea diferente.

### Inspeccionar el runtime antes de trabajar

El script de matriz lee `META-INF/MANIFEST.MF`, pero también puede comprobarse
manualmente. No deduzcas la versión por el nombre de una carpeta `latest`.

## 4. Flujo recomendado de desarrollo

### Inicio de una tarea

1. Lee `AGENTS.md`, el README raíz y el documento del mod.
2. Ejecuta `git status --short` y revisa diffs relacionados.
3. Identifica canal, versión exacta del servidor y `Assets.zip` correspondiente.
4. Busca primero patrones equivalentes en Shared Source.
5. Confirma firmas reales en el JAR con `javap` cuando haya dudas.
6. Conserva IDs persistentes y limita el cambio al proyecto pedido.

### Construcción común

Usa:

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\<proyecto> `
  -SourceRoot src `
  -PackageRoot . `
  -ArtifactName <archivo>.jar `
  -ServerJar '<ruta al HytaleServer.jar objetivo>'
```

Cada proyecto puede necesitar `PackageRoot src`, `AssetsRoot assets` o
parámetros adicionales. Copia el comando de su README, no uno genérico a ciegas.

El script:

- compila todos los `.java` con UTF-8;
- copia clases, manifest, `Common`, `Server` y `Samples`;
- valida que `icon-256.png` sea realmente un PNG 256x256;
- crea y valida el JAR;
- muestra su SHA-256.

Los outputs se generan en `.build/dist` y no se versionan.

### Matriz pública

Para todos los mods públicos:

```powershell
.\scripts\Test-ActiveJavaMods.ps1 -PublishedOnly
```

Para probar otro canal, pasa tanto el servidor como los assets correctos:

```powershell
.\scripts\Test-ActiveJavaMods.ps1 -PublishedOnly `
  -ServerJar '<canal>\Server\HytaleServer.jar' `
  -AssetsZip '<canal>\Assets.zip'
```

No uses el bundle histórico `New-ActiveModsBundle.ps1` para publicar: incluye
proyectos internos. Usa los scripts de exportación actuales y comprueba qué
artefactos entran realmente.

### Qué significa “validado”

No confundas estos niveles:

1. **Compila**: las firmas Java son compatibles.
2. **Pasa tests estáticos/unitarios**: contratos conocidos siguen presentes.
3. **Carga en servidor bare**: el plugin alcanza `Enabled` sin duplicados.
4. **Smoke test con cliente**: UI, red, mundo y persistencia funcionan.
5. **Validación jugable**: la mecánica se comporta correctamente en un save
   real, tras reconexión y reinicio.

El arranque `--bare` y offline genera errores vanilla de `PermissionsModule`
(`Stream closed`) y `Transport.shutdown` incluso sin mods. No atribuyas esos
errores automáticamente al plugin y no presentes un bare start como smoke test.

## 5. Empaquetado, instalación y despliegue seguro

Antes de sustituir un mod instalado:

1. termina compilación y tests sin errores;
2. inspecciona el `manifest.json` dentro del JAR, no solo el de fuentes;
3. comprueba mods globales y mods del save;
4. elimina o aparta la versión anterior con el mismo `Group:Name`;
5. copia el artefacto nuevo;
6. compara SHA-256 entre build e instalación;
7. reinicia el mundo/servidor o el cliente cuando corresponda.

Copiar un JAR encima de otro no recarga las clases ya iniciadas. Además, el
nombre del archivo no determina la versión activa: manda el manifest interior.

Fallos de empaquetado ya observados:

- `manifest.json` con BOM UTF-8: el parser ve `feff` antes de `{` y no carga el
  plugin. Guardar JSON como UTF-8 sin BOM.
- `Name` con espacios: puede producir nodos de permisos inválidos. Usa nombres
  compatibles con los identificadores esperados.
- `IncludesAssetPack` incoherente con el contenido: el servidor puede cargar el
  plugin pero el cliente no recibir su UI o assets.
- dos JARs con igual `Group:Name`: conexión rechazada o registros duplicados.
- icono declarado en manifest: no es necesario. El cliente busca
  `icon-256.png` en la raíz y exige exactamente 256x256.

## 6. Custom UI: la zona con más desconexiones engañosas

Custom UI es estricta. Un comando inválido no siempre produce un error local
benigno: puede desconectar al jugador del servidor.

### Rutas de documentos

`UICommandBuilder.append()` espera rutas relativas a `Common/UI/Custom`.

Correcto:

```java
commands.append("Pages/Scoreboards/ScoreboardEditor.ui");
```

Incorrecto:

```text
Common/UI/Custom/Pages/...
UI/Custom/Pages/...
```

Dentro de una página, las rutas de imports sí son relativas a la ubicación del
documento. Comprueba el patrón exacto en páginas vanilla equivalentes.

### Selectores: deben existir en el documento cargado

Un `set`, `clear`, `append` o binding contra un selector que no existe puede
causar una desconexión como:

```text
CustomUI Set command couldn't set value. Selector: #NativePreview.Text
Selected element in CustomUI command was not found...
```

Antes de enviar comandos dinámicos:

- verifica que el ID está en la versión empaquetada de la UI;
- comprueba que el fragmento fue añadido antes de manipular hijos;
- si el botón es la raíz del fragmento, no añadas un selector hijo ficticio;
- no emitas comandos contra listas vacías o índices aún no creados;
- crea tests que extraigan los selectores usados por Java y los contrasten con
  el documento empaquetado.

El navegador de Trigger Volumes fallaba porque intentaba actualizar
`#EffectBrowserSubFilters[0] #FilterButton.Text`, pero el botón ya era la raíz
del fragmento. El selector correcto debía apuntar directamente al nodo real.

### Los tipos de propiedades también son estrictos

En Update 6, `Label.Text` espera un `System.String`. Enviar un objeto `Message`
provocó una desconexión y el error:

```text
No parameterless constructor defined for type 'System.String'
```

La solución fue convertir la preview a texto plano antes de `set`; el renderer
3D puede seguir procesando el marcado enriquecido por separado.

No supongas que un tipo “parecido” se serializará. Contrasta el componente
vanilla, `javap`, Shared Source y el log del cliente.

### Bindings y datos de botones

Patrón probado:

```java
events.addEventBinding(
    CustomUIEventBindingType.Activating,
    "#ApplyButton",
    EventData.of("Action", "Apply"),
    false
);
```

Los cambios de campos suelen usar claves prefijadas por `@`; las acciones de
botón pueden viajar como un literal estable. No pidas al cliente una propiedad
como `@EffectType` si el nodo clicable no la expone: ese binding desconectó al
usuario. En el navegador se resolvió enviando el ID técnico como dato literal
del botón.

Si un botón no tiene hover ni recibe clicks, el problema suele ser de layout o
hitbox, no de `handleDataEvent`. Macros como `$C.@TextButton` pueden requerir
`@Anchor` en lugar de `Anchor`; confirma la definición de la macro. Si el
selector termina apuntando a un wrapper no clicable, utiliza el nodo real o un
`TextButton` directo.

### Valores de alineación

Update 6 no aceptó algunos valores intuitivos de `LabelStyle`, por ejemplo
`HorizontalAlignment: Right` o `VerticalAlignment: Top`. El parser intentó
resolverlos como `LabelAlignment` y mostró un markup error. Reutiliza valores
observados en UI vanilla de la misma versión y prueba el documento antes de
instalarlo.

### Caché del cliente

El cliente puede retener una versión anterior de un documento UI aun después de
reemplazar el JAR. Para cambios de contrato importantes, versiona la ruta del
documento, por ejemplo `HologramEditorV081.ui`, y reinicia completamente el
cliente. Cerrar solo el mundo no siempre invalida la caché.

### Packs parciales de Custom UI

En algunas pre-releases, recibir un pack que solo contiene una rama parcial de
`Common/UI/Custom` hace que el cliente pierda documentos vanilla necesarios.
Configurable Mob Spawners evita esto hidratando en el stage de build el árbol
vanilla completo desde el `Assets.zip` del mismo runtime:

```powershell
-VanillaCustomUiAssetsZip '<canal>\Assets.zip'
```

Esos assets son dependencias externas: solo viven en `.build`, nunca en Git.
Este workaround no debe generalizarse sin necesidad, porque un override de
`Common.ui` incorrecto también puede romper toda la interfaz.

### Localización

Los archivos `Server/Languages/*/server.lang` reciben automáticamente el
namespace `server.`. Dentro del archivo escribe:

```text
customUI...
```

No escribas:

```text
server.customUI...
```

Cada campo nuevo debe incluir, como mínimo, etiqueta en inglés y español,
tooltip útil, unidades y ejemplo cuando proceda, placeholder y todas las
opciones de enums. Ejecuta `Test-PluginLocalization.ps1` antes de empaquetar.

## 7. Hilos, ECS y bloqueos de assets

La compatibilidad de firmas no garantiza que una llamada sea segura desde el
hilo actual.

### Regla de separación

- Mutaciones ECS y del mundo: en el hilo del mundo o mediante su mecanismo de
  planificación.
- Descarga, decodificación, disco y preparación costosa: fuera del hilo del
  mundo.
- Registro, retirada o recarga del asset store: fuera del tick si este puede
  mantener un lock de lectura.
- Aplicación del resultado a una entidad: vuelve al hilo del mundo cuando la
  fase asíncrona haya terminado.

No hagas `join()` o esperas bloqueantes desde un sistema de mundo sobre una
tarea que necesita un lock retenido por ese mismo tick.

### Deadlock real de hologramas

Raynor Holograms 0.8.2 decodificaba un GIF, escribía un spritesheet y ejecutaba
`unregisterPack/registerPack` desde el comando en el hilo del mundo. El log
mostraba la recarga de un `.particlespawner` y luego el cliente dejaba de recibir
paquetes; `store.addEntity()` nunca se alcanzaba.

La solución de 0.8.3:

1. crear inmediatamente una entidad-ancla invisible;
2. lanzar una única `CompletableFuture` por holograma con
   `HytaleServer.SCHEDULED_EXECUTOR`;
3. decodificar y publicar assets fuera del mundo;
4. consumir el resultado terminado desde el sistema del mundo;
5. activar el asset de partículas tras una pequeña ventana de entrega.

### Deadlock real de Scoreboards

El tick mantiene `ASSET_LOCK` de lectura. Mutar sincrónicamente
`ObjectiveAsset` desde un evento UI puede esperar el lock de escritura mientras
el mundo espera a que termine el evento. Las altas, ediciones y eliminaciones de
assets se encolan fuera del hilo del mundo.

Además, antes de enviar paquetes nativos de Objective al cliente hay que enviar
sus traducciones dinámicas de título y descripción. Si se invierte el orden, el
HUD puede mostrar claves `server.objectives.*` en vez del texto editable.

### Colisión diferida en entidades

Al convertir bloques en entidades, el cliente necesita recibir primero la
geometría y el `NetworkId`. Aplicar inmediatamente la colisión de plataforma
falló de forma intermitente. Entity Motion Triggers usa un componente pendiente
y aplica la colisión dos ticks después.

## 8. Assets dinámicos, atlas, texto, imágenes y GIF

### El problema del atlas en Update 6

Update 6 prepara el atlas de texturas de entidades al entrar el cliente. Crear
una PNG de entidad después puede registrar bytes válidos, pero el modelo termina
muestreando una región transparente o negra. Los síntomas fueron planos negros,
caracteres diminutos o contenido invisible.

La arquitectura que funcionó combina:

- atlas estáticos conocidos antes de `PrepareEntitiesAtlas`;
- texturas y regiones con dimensiones válidas, padding y múltiplos esperados;
- modelos que apuntan exactamente a la ruta y región publicada;
- `RequestCommonAssetsRebuild` cuando se rellena el atlas de medios en runtime.

Al diagnosticar un plano negro, comprueba en este orden:

1. la textura está realmente en el JAR o pack generado;
2. mayúsculas, ruta y nombre coinciden exactamente;
3. ancho, alto, padding y región del atlas son válidos;
4. el cliente conocía el atlas al preparar entidades;
5. recibió una reconstrucción de Common assets tras el cambio;
6. se reinició o reconectó en una prueba limpia.

Recargar las clases del plugin no reconstruye automáticamente el atlas del
cliente.

### Una entidad por holograma

El enfoque por carácter fue descartado: producía una entidad por letra, grandes
separaciones al escalar, selección fragmentada y demasiado tráfico. El diseño
actual usa una única entidad de red por holograma. El texto enriquecido puede
tener muchos nodos dentro de un modelo o una textura compuesta, pero comparte
UUID, transform y ciclo de vida.

Eso permite que Entity Tool mueva, rote, escale, clone y borre el holograma
completo. Las clases de identidad reasignan ID al detectar una entidad clonada.

### Escala y separación

No escales las posiciones de glifos y el tamaño visual con factores distintos
sin querer. Ese error hacía que al aumentar escala creciera sobre todo el
espaciado. Calcula primero la composición en coordenadas tipográficas y aplica
una transformación uniforme al resultado completo.

En efectos como `glow`, separa el interlineado visual del tamaño del halo. Para
`highlight`, el fondo y los glifos no deben ocupar exactamente el mismo plano:
usa capas separadas por una distancia mínima para evitar z-fighting.

### Imágenes y GIF

Las fuentes soportadas por Holograms incluyen `builtin:`, `local:` y URL directa
HTTP/HTTPS. Una URL de una página web no es una URL de imagen o GIF. Valida
`Content-Type`, redirecciones y contenido real antes de culpar al decoder.

La implementación limita redirecciones, timeouts, bytes descargados,
megapíxeles, resolución de animación y número de frames. También bloquea hosts
privados/locales para evitar SSRF. Las operaciones remotas y el decoder nunca
deben bloquear el tick del mundo.

`Samples/prueba.gif` debe incluirse dentro del JAR; el script común copia la
carpeta `Samples` al stage.

### Billboard

Un holograma servidor con una única entidad solo puede tener una rotación
compartida. Puede orientarse al jugador más cercano, pero no mirar de forma
diferente a cada cliente a la vez. `BILLBOARD_Y` ajusta yaw; `BILLBOARD` puede
ajustar yaw y pitch. Si un asset de partículas ofrece billboard nativo de
cliente, es preferible para orientación por observador.

Consulta también:

```text
mods/java/raynor-holograms/DEVELOPMENT_LEARNINGS.md
```

## 9. Trigger Volumes: compatibilidad, categorías y prefabs

### IDs persistentes

Los saves guardan IDs de efectos, condiciones, reglas y componentes. No cambies
un ID interno para mejorar su etiqueta. Cambia localización o descripción y, si
es imprescindible migrar, implementa una migración explícita.

`NoMove` y `RestrictBuildBattleCreativeTools` son reglas de `Always Active`, no
efectos. Registrarlos en el tipo equivocado hace que no aparezcan donde se espera
y puede alterar su semántica.

### Descubrimiento dinámico

El navegador de efectos lee en runtime los registros de condiciones, efectos y
reglas. Así puede mostrar tipos aportados por otros mods sin hardcodearlos. Para
clasificar nuevos tipos, deriva primero el mod de origen y metadatos disponibles;
usa una categoría de fallback si el registro no aporta semántica suficiente.
Los botones “Por mod” solo se crean para fuentes que realmente registran tipos.

No confundas la etiqueta localizada con el ID técnico: el ID es la clave estable
para buscar, añadir y persistir; la interfaz puede mostrar nombre y descripción
breve.

### Integración con la herramienta vanilla

La tecla E de `BuilderTool.Id=TriggerVolume` ejecuta directamente
`/triggervolume effects`. El clon conserva ese `BuilderTool.Id` para reutilizar
creación y selección vanilla. El navegador instala un puente reversible que
abre su inspector solo cuando el clon está en la mano; la herramienta vanilla
mantiene su interfaz vanilla.

Modificar o sustituir assets vanilla fue evitado porque aumenta mucho el riesgo
de roturas entre actualizaciones. Cuando sea posible, reutiliza el editor y el
guardado vanilla después de seleccionar un tipo en el catálogo.

### Rotación de prefabs: el bug más engañoso

En versiones 0.6.x, pegar directamente un prefab con yaw 90 o 270 desplazaba
entidades internas —incluidos Trigger Volumes— una celda y no componía bien su
orientación, aunque los bloques parecían correctos.

La solución de More Triggers fue:

1. resolver el prefab;
2. clonar la selección completa;
3. rotar la copia con `BlockSelection.rotate`;
4. pegar la copia ya transformada con `Rotation.None`.

No pases además la rotación al pegado final o aplicarás una segunda rotación.
Las notas de Update 6 mencionan fixes vanilla en prefabs, pero no se eliminó el
workaround sin probar los cuatro yaw y entidades internas en partida.

La preview del inspector puede colocar el prefab sin representar su `Rotation`.
Una preview incorrecta no demuestra por sí sola que el pegado real ignore yaw;
prueba ambos caminos por separado.

### Antisolapamiento procedural

Para evitar que una sala nueva sobrescriba otra:

1. calcula la huella tridimensional después de aplicar el yaw;
2. comprueba solapamiento con reservas del mismo `OccupancyGroup`;
3. reserva la posición de forma atómica;
4. solo entonces pega el prefab;
5. si falla el pegado, libera o invalida la reserva según el contrato.

La implementación persiste reservas como Trigger Volumes técnicos desactivados
con tag `procedural_room`. Comprobar y reservar por separado sin exclusión abre
una carrera si dos eventos generan al mismo tiempo.

### Bucles de señales

`ControlSignalLoop` sigue disparando después de que el actor abandone el volumen,
pero su estado solo vive durante la sesión del servidor. No prometas persistencia
tras reinicio a menos que se añada almacenamiento explícito.

## 10. Entidades, bloques, items y selección de assets

`ConvertBlocksToEntities` distingue tres familias:

- items con modelo: `ModelComponent`;
- items-bloque normales: `BlockEntity`;
- otros items: `ItemComponent`.

La escala de `BlockEntity` debe ser `1.0` para mantener el tamaño real del
bloque. Aplicar una escala genérica puede encoger o agrandar la geometría de
forma inesperada.

El selector de assets debe partir de la fuente vanilla `Item`. Filtrar solo por
`Item.getModel()` excluye todos los items-bloque que no tienen modelo separado.
Cuando un catálogo parece incompleto, revisa el criterio de enumeración antes de
culpar a la UI.

Partículas ancladas y colisión dependen del `NetworkId`; por eso el orden de
creación, sincronización y configuración importa.

## 11. Configurable Mob Spawners

Lecciones relevantes más allá de su mecánica:

- APIs de iluminación de `BlockChunk` desaparecieron en pre.13.1/0.6.2. La
  consulta se migró a datos globales de `BlockSection` y conserva la corrección
  de cielo y mapa de altura.
- Un warning de API deprecated no es un error de compilación. Regístralo y
  busca sustituto, pero no cambies comportamiento estable solo para silenciarlo.
- Los comportamientos NPC pueden reescribir nameplates o componentes. Los
  nombres élite se refuerzan después del comportamiento correspondiente.
- Que la armadura esté configurada no garantiza que el modelo la renderice ni
  que sus stats o drops tengan el efecto deseado. Verifica componentes finales
  y muerte real en juego.
- El equipo no debe caer automáticamente salvo que esté declarado en la tabla
  de loot; esto requiere smoke test, no solo inspección de configuración.
- Los catálogos web extraídos de `Assets.zip` y el HTML offline con imágenes
  vanilla no se versionan.
- La UI ligera y el editor completo conservan perfiles que la pantalla básica
  no expone; al guardar campos básicos no se deben destruir datos importados.

## 12. Scoreboards/Objectives

Los IDs de definición son estables y se separan del título visible. Las
Objectives dinámicas se publican como assets; sus filas son `ObjectiveTask`, no
`ObjectiveLineAsset`.

Dos invariantes importantes:

1. mutar el asset store fuera del hilo del mundo para evitar `ASSET_LOCK`;
2. enviar traducciones dinámicas antes de los paquetes Objective.

No empaquetes un `Common/UI/Custom/Common.ui` inventado o vacío: ya produjo UI
negra y puede sobrescribir componentes fundamentales del cliente. Usa imports y
macros vanilla de la misma versión, o hidrata el árbol completo solo en los
proyectos cuyo workaround lo exige.

## 13. Depuración basada en evidencia

### Cuando el jugador se desconecta al abrir una UI

1. copia literalmente el selector y el mensaje de la pantalla;
2. abre el log cliente más reciente en `%APPDATA%\Hytale\UserData\Logs`;
3. busca `CustomUI`, `Markup Error`, el selector y el nombre de la página;
4. inspecciona la UI dentro del JAR instalado, no solo la fuente;
5. compara los selectores emitidos por Java con los IDs reales;
6. comprueba tipos de la propiedad (`String`, enum, número, etc.);
7. reinicia cliente y servidor para descartar caché/clases antiguas.

### Cuando un comando “no hace nada”

1. confirma que el plugin llegó a `Enabled`;
2. revisa que no haya BOM o manifest inválido;
3. busca el inicio y final de la ejecución en logs;
4. detecta el último paso alcanzado antes de un lock o excepción;
5. revisa si la tarea se ejecuta en el hilo del mundo;
6. comprueba si el recurso remoto es realmente directo y válido;
7. evita interpretar ausencia de chat como ausencia de ejecución.

### Inspeccionar el artefacto instalado

```powershell
jar tf '<mod.jar>'
jar --validate --file '<mod.jar>'
Get-FileHash -Algorithm SHA256 -LiteralPath '<mod.jar>'
```

Para manifests o assets concretos, extrae a una carpeta temporal segura y
compara con el stage. No edites el JAR instalado como fuente de verdad.

### Buscar antes de parchear

Usa `rg` en este orden:

1. fuentes activas del mod;
2. tests y docs del proyecto;
3. Shared Source;
4. referencias de la clase dentro del JAR con `javap`.

Los directorios `legacy`, `deprecated`, `experiments` y `auxiliary` sirven de
historial o apoyo, pero no deben convertirse automáticamente en implementación
activa.

## 14. Clean-room, licencias y referencias externas

Cuando se inspeccionan mods de terceros:

- estudia capacidades observables, formatos públicos, patrones de API y
  comportamiento;
- documenta la arquitectura inferida;
- implementa código, nombres y assets originales;
- no copies bloques significativos de bytecode descompilado, recursos,
  traducciones o formatos privados;
- respeta la licencia incluso si permite reutilización.

El objetivo de Raynor Holograms fue expresamente una implementación clean-room:
los mods de referencia demostraron qué era posible, pero no son la base de su
código.

## 15. Higiene Git y persistencia

No versionar:

- `HytaleServer.jar`, `Assets.zip` ni Shared Source;
- JARs, ZIPs, clases, `.build`, `dist`, `target` o caches;
- logs, backups, telemetría o saves completos;
- assets vanilla extraídos o HTML offline que los incruste;
- secretos o tokens.

Los binarios y snapshots restaurables se publican como artefactos de GitHub
Release y se documentan bajo `snapshots/` cuando corresponda.

No borres código legacy por estar duplicado hasta confirmar que la versión
activa conserva toda su funcionalidad. No uses `git reset --hard` ni descartes
cambios locales del usuario.

La marca pública es `Raynor` / `Raynor Mods`. `OrbGenesis` permanece en IDs
técnicos históricos y colaboraciones. Renombrar namespaces, paquetes, assets o
componentes persistentes exige una migración compatible.

## 16. Estado actual de los proyectos públicos

| Proyecto | Estable | Observación principal |
| --- | ---: | --- |
| More Triggers | 1.10.6 | Trigger Volumes, prefabs rotados, ocupación, señales y ExecuteCommand. |
| Entity Motion Triggers | 1.3.3 | Entidades, movimiento, partículas y colisión diferida. |
| Particle Shape VFX | 0.1.2 | Cubos, esferas y líneas de partículas. |
| Raynor Holograms | 0.8.3 | Experimental; texto, imagen, GIF, atlas y una entidad por holograma. |
| Editable Objectives | 2.0.12 | Assets dinámicos; smoke test de cliente aún importante. |
| Configurable Mob Spawners | 0.5.4 | WIP; UI, CMS1, NPCs, elites y catálogos web. |

Build Battle es interno. Trigger Volume Effect Browser es experimental. Los
proyectos bajo `mods/java/deprecated` no se distribuyen.

En la rama Update 7, los seis mods públicos tienen una variante
`*-u7pre.1` para `=0.7.0-pre.1`. Consulta:

```text
C:\Users\andre\Documents\Hytale-Mod-Workspace-update7\docs\UPDATE_7_PRE_RELEASE.md
```

## 17. Checklist antes de declarar una tarea terminada

- [ ] Se leyó `AGENTS.md` y la documentación del proyecto.
- [ ] Se preservaron los cambios locales ajenos.
- [ ] Se eligió el canal y `HytaleServer.jar` exactos.
- [ ] Se consultó Shared Source y se verificaron firmas dudosas con `javap`.
- [ ] No cambiaron IDs persistentes sin migración.
- [ ] Manifest, versión Java visible y nombre del artefacto están sincronizados.
- [ ] JSON sin BOM; icono raíz 256x256; assets requeridos dentro del JAR.
- [ ] Localización inglesa y española completa.
- [ ] Tests del mod y test de paquete correctos.
- [ ] No hay dos instalaciones con el mismo `Group:Name`.
- [ ] SHA-256 del build coincide con la copia instalada.
- [ ] Se reinició lo necesario para recargar clases, UI y atlas.
- [ ] Se revisaron logs de cliente y servidor.
- [ ] Se distinguió compilación, carga bare, smoke test y validación jugable.
- [ ] Se actualizaron README/handoff/AGENTS si cambió una limitación o proceso.
- [ ] No se añadieron binarios, dependencias oficiales, assets vanilla o saves a Git.

## 18. Documentos que concentran conocimiento adicional

```text
AGENTS.md
README.md
docs/UPDATE_6_3_PUBLIC_MODS.md
docs/MORE_TRIGGERS_CONSOLIDATION.md
mods/java/raynor-holograms/DEVELOPMENT_LEARNINGS.md
mods/java/configurable-mob-spawners/HANDOFF.md
mods/java/scoreboards/README.md
mods/java/trigger-volume-effect-browser/README.md
C:\Users\andre\Documents\Hytale-Mod-Workspace-update7\docs\UPDATE_7_PRE_RELEASE.md
```

Si una observación de este documento contradice `AGENTS.md` o un handoff más
reciente, prevalece el documento más reciente y la evidencia del runtime exacto.
