# Configurable Mob Spawners

Plugin Java independiente para Hytale pre-release que añade un bloque sólido y
minable en aventura capaz de generar oleadas de NPC. La configuración se guarda
en la entidad del bloque y se edita interactuando con él en creativo.

## Versión 0.4.0

- selector limitado a roles NPC generables, con búsqueda y previsualización temporal del modelo 3D;
- nombre visible opcional y persistente para los NPC generados;
- cadencia, cantidad por oleada, máximo de mobs vivos y radio de activación;
- radio horizontal de spawn con detección automática del suelo cercano;
- luz efectiva máxima de 0 a 15, incluyendo cielo directo: cualquier valor igual o inferior permite el spawn;
- vida máxima personalizada (`0` conserva la vida original del rol);
- escala del mob entre `0,1x` y `5,0x`, reflejada también en la preview;
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
2. En creativo, busca `OrbGenesis_Configurable_Mob_Spawner`, colócalo e
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
los accesorios sobre el cuerpo en 3D. Los catálogos locales se regeneran desde
`Assets.zip` con:

```powershell
.\tools\web-configurator\Generate-WebAssetIds.ps1
.\tools\web-configurator\Generate-WebMobPreviews.ps1
```

Los JavaScript y PNG generados de modelos y objetos se ignoran en Git: son una caché local de la
pre-release instalada y no forman parte del código fuente ni del JAR.

La apariencia oscura original está en `tools/web-configurator/Configurador Spawner de Mobs.html`. La
variante visual clara y pastel se abre con
`tools/web-configurator/index-light.html`; comparte exactamente los mismos
campos, distribución, datos y lógica. El selector NPC es un autocomplete propio:
al abrirlo enseña el catálogo completo y solo filtra mientras se escribe.

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
  -VanillaCustomUiAssetsZip "$env:APPDATA\Hytale\install\pre-release\package\game\latest\Assets.zip" `
  -ArtifactName ConfigurableMobSpawners-0.4.0.jar
.\tools\Test-Package.ps1 -ArchivePath .\.build\dist\ConfigurableMobSpawners-0.4.0.jar
```

Compila siempre contra el `HytaleServer.jar` de la pre-release instalada. El
campo persistente antiguo `VerticalRadius` se conserva únicamente para poder
leer bloques 0.1.x; la versión 0.2.x ya no lo muestra ni lo usa.

La integración con `Send Signal` de Trigger Volumes queda para una versión
posterior; la tag ya forma parte del formato persistente.

Las versiones 0.1.4-0.1.8 corrigieron, respectivamente, el árbol Custom UI
vanilla requerido por pre.11, la sintaxis de desplegables, textos raw, la clave
literal `Action`, la ortografía española y el cálculo de luz bajo cielo directo.
