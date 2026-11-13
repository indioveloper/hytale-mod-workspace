# Menu magico para Minecraft Bedrock - 1.2.0

Add-on familiar para Bedrock 26.40 o posterior, separado de los mods de Hytale.
Objeto persistente: `raynor:admin_menu`. El objeto ahora tiene el icono de reloj.

## Instalar o actualizar

1. Ejecuta `tools/Build-Pack.ps1` en PowerShell.
2. Abre `.build/Menu_Magico_1_2_0.mcaddon` con Minecraft: importa comportamiento e iconos.
3. Activa **Menu magico familiar v1.2.0** y su pack de recursos **Menu magico - Iconos v1.2.0**.
4. Conserva los trucos activados. No requiere APIs beta.
5. Sal del mundo y vuelve a entrar para recargar scripts y texturas.
6. Usa el reloj que ya tenias, o `/give @s raynor:admin_menu`.

Se conservan UUID e ID del objeto de 1.0.0: es una actualizacion, no otro mod.
No actives simultaneamente copias importadas con UUID alterados. Si el juego
retiene la version anterior, desactiva el pack, importa 1.2.0 y reactivalo.

## Controles con mando

- Todos los botones tienen iconos: espada, pocion, mesa de encantamientos, leche,
  mesa de trabajo, estrella del Inframundo, flecha de volver, +, - y estado.
- Efectos: solo beneficiosos, disponibles en el runtime. Se excluyen debufos,
  presagios y efectos de riesgo/neutros (levitacion, infestacion, tejido, etc.).
- + aplica inmediatamente el nivel 1 y luego aumenta; - reduce, y nivel 0 quita.
  Los efectos de un nivel usan + Aplicar nivel 1 / - Quitar efecto. No hay boton
  Activar ni paso de confirmacion, sliders o selector de particulas.
- Efectos temporales usan el comando nativo `infinite`, con particulas ocultas.
  Pueden terminar al morir, beber leche o ser consumidos por una mecanica vanilla.
  El menu refleja el estado real; no los reactiva a escondidas.
- Curacion instantanea y saturacion se aplican en el mismo clic y se repiten
  cada segundo hasta bajar a 0 o quitar. Persisten entre sesiones. Dano instantaneo
  ya no se ofrece ni se repite aunque hubiera quedado activado en una version anterior.
- Los niveles de efectos estan limitados al rango de amplificador de Bedrock
  (niveles 1-256). Algunos efectos no cambian con el nivel y no muestran +/-.
- Encantar: inventario completo, cuatro piezas equipadas y mano secundaria.
  Los nombres se traducen mediante las claves nativas y se conservan nombres propios.
- Solo aparecen encantamientos compatibles y los que ya estan aplicados, salvo
  las maldiciones de ligamiento/desaparicion, que nunca se ofrecen. Los
  encantamientos existentes se pueden reducir o quitar; + aplica/sube, - baja y nivel 0 elimina.
  Se respetan los niveles maximos vanilla y las incompatibilidades.
- Si otro proceso cambia el objeto mientras el menu esta abierto, se cancela
  la escritura para no sustituir sus datos. No se duplican las entradas de mano principal.

## Iconos y limites de la interfaz

Los formularios nativos tienen un icono por boton. Los controles +/- viven en
la pantalla del efecto/encantamiento, no incrustados en una fila horizontal.
Todos los simbolos de encantamientos reutilizan pixel art vanilla representativo
(pluma, espada, diamante, etc.). La mesa tiene un PNG propio generado; ver ART.md.
El escudo usa la silueta vanilla. Los items desconocidos de otros add-ons usan
un libro encantado de reserva: la API estable no proporciona su textura de inventario.
La armadura de cuero usa la textura base; no reproduce su tinte personalizado.

Este objeto concede capacidades de administrador a cualquiera que lo posea.
No se instalan ni modifican mundos automaticamente.

## Desarrollo y verificacion

- `behavior_pack/`: scripts y objeto; server 2.9.0, server-ui 2.1.0 estables.
- `resource_pack/`: icono adicional y manifiesto, dependencia explicita del BP.
- `tools/Test-Pack.ps1`: manifest y pruebas de logica con mocks de Script API.
- `tools/Test-Menu.mjs`: catalogo, iconos, niveles, armadura, cambios concurrentes.
- `tools/Generate-IconIndex.mjs`: genera solo metadatos desde el arbol oficial de
  Mojang/bedrock-samples guardado en `.build/reference/tree.json` (SHA en el output).
  No copia texturas vanilla al repositorio.
- `tools/Build-Pack.ps1`: valida y produce dos `.mcpack` y un `.mcaddon` conjunto.

1.2.0 supera pruebas automaticas y chequeo de tipos contra los paquetes oficiales.
Falta smoke test en el cliente real; 1.0.0 fue confirmado funcional por el usuario.
Probar con mando: apertura, iconos, cambio de modo, efecto aplicar/subir/bajar/quitar,
duracion infinita y sin particulas, pieza equipada y objeto de inventario, reducir
encantamiento existente y quitarlo, cancelar/volver sin cambios, salir y reentrar.

Referencias: [efectos](https://learn.microsoft.com/en-us/minecraft/creator/scriptapi/minecraft/server/effecttypes?view=minecraft-bedrock-stable),
[comando infinite](https://learn.microsoft.com/it-it/minecraft/creator/commands/commands/effect?view=minecraft-bedrock-stable),
[assets oficiales](https://github.com/Mojang/bedrock-samples).

### Actualizacion 1.1.1

Incluye version visible en nombres de ambos packs y titulo del menu, y
`pack_icon.png` en ambos paquetes (reutiliza la mesa de encantamientos).
La importacion puede conservar varias versiones del mismo UUID: comprobar la
version seleccionada por el mundo, no solo reiniciar Minecraft.

### Actualizacion 1.2.0

Sin debufos ni maldiciones en los menus, aplicacion inmediata con +/- y nivel 0
para quitar. No se borran automaticamente maldiciones preexistentes de objetos
ni efectos vanilla recibidos de enemigos: el filtro limita lo que ofrece este mod.
Se conservan IDs, UUID y propiedades persistentes. Pruebas nuevas cubren filtros,
primer +, reduccion hasta 0, efectos instantaneos y opciones de un solo nivel.
La imagen de vista previa es ilustrativa, generada desde el diseno de estos menus;
no es una captura ni una prueba del cliente Minecraft. Ver previews/PROMPT.md.
