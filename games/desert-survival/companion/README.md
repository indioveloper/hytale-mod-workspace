# Desert Survival companion web

Herramienta paralela al mod, sin las limitaciones de la Custom UI de Hytale.
Primera entrega (2026-11-15): visor navegable del arbol de investigacion.

## Que hace

- Rueda con la misma geometria que `/research` en el juego: cinco cunas de
  72 grados (una por rama) y tres bandas concentricas (chatarra, cobre,
  hierro; interior a exterior). Los 48 nodos a la vez, con enlaces de
  prerrequisito dibujados.
- Paneo con arrastre, zoom con rueda o botones, `Encajar` para volver a ver
  todo, `Centrar` desde el panel para acercar un nodo.
- Clic en un nodo: rama, banda, coste en Intel, descripcion, prerrequisitos,
  nodos que habilita y recetas que desbloquea. Los chips de nodo navegan.
- Cada objeto desbloqueado indica si ya existe su JSON en algun prototipo
  (verde) o esta pendiente (rojo), y en que prototipo vive. En el nodo se ven
  como puntos bajo el nombre.
- Busqueda por nombre de nodo, ID o ID/nombre de objeto; filtro por rama.
- `Simular`: al activarlo, el clic sobre un nodo disponible lo investiga y
  sobre uno investigado lo olvida junto a sus dependientes, para comprobar
  rutas de desbloqueo. Se guarda en `localStorage` del navegador y
  `Reiniciar` lo borra. No toca el juego.

## Fuente de datos

`data/research-tree.json` y `data/research-tree.js` se generan desde el
codigo Java del prototipo, que sigue siendo la unica fuente de verdad:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File games/desert-survival/companion/tools/Export-ResearchCatalog.ps1
```

El exportador parsea `ResearchCatalog.java` (nodos, prerrequisitos, coste),
`ResearchUnlocks.java` (nodo -> IDs de item) y `ResearchSector.java`
(etiquetas), y recorre `prototypes/*/Server/Item/Items/**` y los
`server.lang` para marcar que objetos existen y su nombre visible. Falla si no
encuentra 48 nodos o un nodo sin desbloqueos. Volver a ejecutarlo tras tocar
el catalogo; los ficheros generados se versionan para que la web funcione sin
build.

## Como abrirla

Es HTML estatico sin dependencias. `data/research-tree.js` carga como script
global, asi que funciona al abrir `index.html` directamente desde disco. Para
servirla en local:

```powershell
python -m http.server 8765 --directory games/desert-survival/companion
```

y abrir `http://localhost:8765/`. Tambien hay una entrada `desert-companion`
en `.claude/launch.json` de la raiz del workspace para el preview del agente.

## Pendiente / ideas

- Imagen por nodo (iconos de item) cuando existan assets propios.
- Ficha de objeto: receta, ingredientes y estacion, leidos del JSON del item
  y de `docs/CRAFTING_MATERIALS_ZONES_AND_BUILDING.md`.
- Leer el progreso real de un personaje (requiere exportar
  `ResearchProgressComponent` desde el servidor o un endpoint).
- Lore, enciclopedia de NPCs y registro de misiones (idea original).
- Despliegue junto a `website/raynor-mods` en Vercel; el enlace desde el
  juego solo puede ir por chat (`Message.raw(url).link(url)`).
