# Configurable Mob Spawners

El estado de la prueba más reciente y las tareas concretas para retomar el
desarrollo están en [HANDOFF.md](HANDOFF.md).

Plugin Java independiente para Hytale Update 6 que añade un bloque sólido y
minable en aventura capaz de generar oleadas de NPC. La configuración se guarda
en la entidad del bloque y se edita interactuando con él en creativo.

## Versión 0.5.3

- recompilado y validado contra Hytale estable `0.6.2` (Update 6, hotfix 2);
- el manifest público apunta a la línea estable `>=0.6.2 <0.7.0` y conserva
  los IDs técnicos existentes para no romper bloques ni configuraciones CMS1;
- el configurador web incorpora interfaz completa en español e inglés y un
  aviso WIP visible junto al título;
- la autoría pública y el enlace del proyecto se muestran como Raynor Mods.

## Versión 0.5.2

- portado a Hytale `0.6.0-pre.13.1`: la luz global se consulta ahora desde la
  sección del bloque, conservando el cálculo de luz efectiva y cielo directo;
- al aparecer un élite se emite además un estallido de partículas sobre la
  posición de spawn, como refuerzo visual independiente del `ModelVFX`;
- el bloque spawner declara `ParticleColor` y `TextureComputedColor` propios
  para que sus partículas de rotura y su color computado dejen de heredar los
  del grupo `Metal`;

> Pendiente de confirmar el resto del changelog: esta versión recoge cambios en
> `SpawnerTickSystem` y en el asset del bloque que no estaban en el JAR 0.5.1.

## Versión 0.5.1

- validado contra el hotfix Hytale `0.6.0-pre.12.2`;
- el nombre personalizado se vuelve a imponer después del tick de comportamiento
  NPC, evitando que ciertos roles sustituyan u oculten el nombre configurado;
- los élites reciben un tinte violeta oscuro y un barrido sutil mediante un
  `ModelVFX` infinito propio, sin cambiar las texturas originales de cada rol;
- al entrar a 10 bloques de un élite aparece un Event Title breve con su nombre;
  la detección es por entrada y aplica cooldown para no saturar el HUD;
- añadida una regresión CMS1 de cuatro perfiles equiponderados para garantizar
  que el formato compuesto no vuelve a colapsar al primer mob;

## Versión 0.5.0

- un mismo spawner admite hasta 12 perfiles de mob y elige cada aparición por
  peso relativo;
- el configurador web presenta `Mob 1`, `Mob 2`, etc. en pestañas y conserva
  todos los perfiles dentro de un único string `CMS1:`;
- cada perfil puede habilitar su propia variante élite con probabilidad,
  prefijo, multiplicadores de vida, escala y velocidad;
- las variantes élite son siempre hostiles, pueden sustituir el equipo base y
  tienen una tabla adicional de loot que se suma al loot normal;
- al aparecer y morir un élite se emiten los mensajes de enfrentamiento y
  derrota atribuidos al jugador activador/último atacante;
- los CMS1 anteriores se migran automáticamente a un único `Mob 1`. El editor
  completo dentro del juego modifica solo los campos básicos de Mob 1 y
  conserva los demás perfiles y su configuración élite;

## Versión 0.4.6

- eliminado el interruptor manual de activación de la web y del editor del
  juego: el spawner funciona automáticamente cuando se cumplen proximidad,
  luz, cadencia y capacidad;
- `Máx. vivos` cuenta únicamente los mobs generados por ese bloque que sigan
  dentro de su radio de activación. Los mobs que salgan liberan capacidad y
  vuelven a contar si regresan;
- los saves y códigos CMS1 antiguos con `enabled=false` siguen siendo legibles,
  pero el campo se ignora y ya no se exporta;

- los bloques recién colocados comienzan sin rol, no cargan una preview ni
  generan mobs y muestran el estado apagado hasta recibir una configuración;
- los bloques ya guardados y los códigos CMS1 existentes conservan sus roles;

- velocidad horizontal configurable entre `0,0x` y `3,0x`, relativa a la
  velocidad natural del rol y compatible con efectos vanilla acumulables;
- la escala usa ahora el modelo escalado nativo: tamaño visual, hitbox física,
  zonas de impacto, altura de ojos y controladores de movimiento permanecen
  sincronizados;
- `speed` es opcional en CMS1 y toma `1,0x` al importar códigos antiguos;

- portada ligera con instrucciones numeradas y enlace al configurador web;
- `Guardar y cerrar` importa el `CMS1`, persiste el bloque y cierra la interfaz;
  `Cancelar` cierra sin aplicar el borrador;
- la exportación del bloque solo aparece dentro del editor completo;
- la portada muestra la URL en un campo de solo lectura y ofrece `Copiar URL`;
  la acción prepara el texto en el chat para copiarlo, porque la Custom UI del
  servidor no puede escribir directamente en el portapapeles del cliente;
- al entrar en la partida, cada jugador recibe en el chat la guía de uso y un
  enlace clicable al configurador;
- el editor pesado, su catálogo NPC y la preview 3D no se cargan hasta pulsar
  **Acceder a la configuración completa**;
- `Spawner Block` usa calidad `Common` y aparece en el inventario creativo
  normal, sin depender de `/give`;
- el log de arranque recuerda el nombre del bloque y enlaza el configurador web
  público en `https://hytale-mob-spawner-configurator.vercel.app`;

- selector limitado a roles NPC generables, con búsqueda y previsualización temporal del modelo 3D;
- nombre visible opcional y persistente para los NPC generados;
- cadencia, cantidad por oleada y máximo de mobs propios vivos dentro del radio
  de activación;
- radio horizontal de spawn con detección automática del suelo cercano;
- luz efectiva máxima de 0 a 15, incluyendo cielo directo: cualquier valor igual o inferior permite el spawn;
- vida máxima personalizada (`0` conserva la vida original del rol);
- escala física del mob entre `0,1x` y `5,0x`, reflejada también en la preview;
- objeto opcional en la mano y actitud original, hostil, pacífica o neutral;
- armadura personalizada por cuatro ranuras: sus propiedades se aplican a todos
  los mobs, aunque solo algunos modelos puedan representarla visualmente;
- loot estándar, ninguno, suma o sustitución y cinco drops visibles como filas;
- importación de toda la configuración mediante un string portable `CMS1:`;
- configurador web local, modelo propio del bloque y `Effect_Fire` durante
  0,75 segundos al generar una oleada;
- varias tags persistentes reservadas para la futura recepción de señales de
  Trigger Volumes.

La previsualización usa el mismo mecanismo admitido por el selector NPC vanilla:
una entidad visual temporal, no serializada, delante del jugador. El componente
3D de la página de inventario solo admite al jugador local y no permite mostrar
un rol NPC arbitrario desde una Custom UI de servidor.

## Uso

1. Instala el JAR y reinicia la partida.
2. En creativo, busca `Spawner Block`, colócalo e
   interactúa con él.
3. Busca y selecciona un rol, ajusta los valores y pulsa Guardar.
4. Para preparar configuraciones fuera del juego, abre
   `tools/web-configurator/Configurador Spawner de Mobs.html`, genera el string `CMS1:` y pégalo en el
   editor del bloque con **Importar string**.

El configurador ofrece selectores de objetos con búsqueda diferida, categorías,
nombres e iconos oficiales. El objeto de mano y cada ranura de armadura se
filtran por su tipo real; los bloques se eligen aparte mediante las categorías
de la biblioteca creativa vanilla. El loot conserva el catálogo público de
objetos no bloque y despliega sus filas según se necesitan. La lista de mobs contiene solo roles
realmente generables (`Generic` y `Variant` en la pre-release actual). Al
seleccionar uno muestra sus HP basales, su miniatura oficial y su altura a escala
frente a una referencia de jugador de 1,85 m. La comparación se actualiza al
mover el selector de escala. Los objetos de mano y armadura se representan con
su icono oficial; las miniaturas vanilla ya renderizadas no permiten recomponer
los accesorios sobre el cuerpo en 3D.

La preview funciona como una tarjeta compacta: muestra la vida configurada, la
defensa física acumulada de las piezas de armadura y un ataque estimado que suma
el daño base resoluble del rol y el daño básico del objeto equipado. Estos
valores se extraen de la misma `Assets.zip`; si un rol no publica un daño
resoluble, la tarjeta lo indica en vez de inventarlo. Los iconos de equipo no se
superponen sobre la preview porque las miniaturas precalculadas los recortaban.

Los catálogos locales se regeneran desde `Assets.zip` con:

```powershell
.\tools\web-configurator\Generate-WebAssetIds.ps1
.\tools\web-configurator\Generate-WebMobPreviews.ps1
```

Los JavaScript y PNG generados de modelos y objetos se ignoran en Git: son una caché local de la
pre-release instalada y no forman parte del código fuente ni del JAR.

Para pruebas internas se puede generar un unico HTML autonomo con todos los
catalogos, iconos, previews y temas incrustados. El tester solo tiene que abrir
el archivo; no necesita PowerShell, el repositorio ni acceso a `Assets.zip`:

```powershell
.\tools\web-configurator\Export-OfflineConfigurator.ps1
.\tools\web-configurator\Test-OfflineConfigurator.ps1 `
  -HtmlPath .\.build\offline-configurator\Configurable-Mob-Spawner-Configurator-offline.html
```

El exportador regenera por defecto los catalogos desde la pre-release local.
Usa `-UseExistingCatalogs` para reutilizar la cache ya generada. El HTML final
se escribe bajo `.build`, contiene assets vanilla extraidos y no debe
versionarse; compartelo solo como artefacto de pruebas internas y regeneralo
despues de cada actualizacion de Hytale.

La apariencia oscura original está en `tools/web-configurator/Configurador Spawner de Mobs.html`. La
variante visual clara y pastel se abre con
`tools/web-configurator/index-light.html`; comparte exactamente los mismos
campos, distribución, datos y lógica. El selector NPC es un autocomplete propio:
al abrirlo enseña el catálogo completo y solo filtra mientras se escribe.

La interfaz principal incluye español e inglés en la misma página. Los botones
`ES` y `EN` del encabezado cambian todos los textos estáticos y dinámicos,
guardan la preferencia en el navegador y permiten enlazar directamente la
versión inglesa mediante `?lang=en`.

`MinLight` y `lightMin` se siguen leyendo únicamente para mantener la
compatibilidad con bloques y strings CMS1 antiguos, pero se ignoran y se
normalizan a 0.

Los IDs de objetos son IDs de assets. Los campos vacíos no aplican un objeto.
Cada ID de armadura se valida como un asset de armadura antes de guardar. El
mapa del configurador simula las oleadas con la cantidad, el rango y una
cadencia aleatoria dentro de los valores configurados.

## Compilación y pruebas

```powershell
.\tools\Test-PluginLocalization.ps1
.\tools\Test-LightLevelMath.ps1
.\tools\Test-ConfigString.ps1
..\..\..\scripts\Build-JavaMod.ps1 `
  -ProjectPath . `
  -SourceRoot src `
  -PackageRoot . `
  -AssetsRoot assets `
  -VanillaCustomUiAssetsZip "$env:APPDATA\Hytale\install\release\package\game\latest\Assets.zip" `
  -ArtifactName ConfigurableMobSpawners-0.5.3.jar
.\tools\Test-Package.ps1 -ArchivePath .\.build\dist\ConfigurableMobSpawners-0.5.3.jar `
  -AssetsZip "$env:APPDATA\Hytale\install\release\package\game\latest\Assets.zip"
```

Todo lo anterior, más la instalación en la carpeta `mods` de la partida de
pruebas, está encadenado en un único comando que lee la versión de
`manifest.json`:

```powershell
.\tools\Build-And-Install.ps1
```

Acepta `-SaveName` para elegir otra partida, `-SkipTests` y `-NoInstall`.
Antes de copiar retira cualquier JAR previo del paquete a una subcarpeta
`backup-<fecha>`, porque cargar dos versiones del mismo mod rompe el arranque
del servidor.

Para una release pública, compila siempre contra el `HytaleServer.jar` de la
instalación estable que se vaya a publicar. El
campo persistente antiguo `VerticalRadius` se conserva únicamente para poder
leer bloques 0.1.x; la versión 0.2.x ya no lo muestra ni lo usa.

La integración con `Send Signal` de Trigger Volumes queda para una versión
posterior; la tag ya forma parte del formato persistente.

Las versiones 0.1.4-0.1.8 corrigieron, respectivamente, el árbol Custom UI
vanilla requerido por pre.11, la sintaxis de desplegables, textos raw, la clave
literal `Action`, la ortografía española y el cálculo de luz bajo cielo directo.
