# Handoff de Configurable Mob Spawners

Última actualización: 2026-08-23.

Este documento conserva el estado funcional observado durante las pruebas y la
lista concreta de trabajo para la siguiente sesión. La versión actual del mod
es `0.5.2` y se compila contra Hytale `0.6.0-pre.13.1`.

La migración a pre.13.1 mueve la lectura de luz desde los métodos eliminados de
`BlockChunk` a `BlockSection.getGlobalLight()`. Se conservan las mismas
coordenadas, el factor solar, la corrección de cielo directo mediante el mapa de
altura y el umbral máximo configurado.

> Estado del build: `0.5.2` compila y supera sus pruebas contra pre.13.1. El
> artefacto local se genera con `.\tools\Build-And-Install.ps1`; no se versiona.
>
> El JAR se instala por partida, no en la carpeta global: la partida de pruebas
> es `%APPDATA%\Hytale\data\pre-release\Saves\mod spawners\mods`. El JRE que
> acompaña a Hytale no trae `javac`; hace falta un JDK en el PATH.
>
> El asset del bloque ya no declara `State` `On/Off`: el color computado y las
> partículas propios lo sustituyen intencionadamente. `Test-Package.ps1`
> comprueba este contrato y que un bloque sin rol permanezca inactivo.

## Implementado en esta iteración

### Mecánica del spawner

- Un spawner admite hasta 12 perfiles de mob dentro de un único código `CMS1`.
- Cada criatura de una oleada elige su perfil mediante pesos relativos.
- Cada perfil conserva modelo, vida, actitud, velocidad, escala, nombre,
  equipo, loot y sus propios ajustes de élite.
- `Máx. vivos` cuenta los mobs creados por ese bloque que continúan dentro de
  su radio de activación. Salir del radio libera capacidad y volver a entrar
  hace que vuelvan a contar.
- Los bloques nuevos se colocan vacíos y no generan nada hasta importar o
  guardar una configuración.
- La velocidad es configurable y la escala se aplica al modelo, hitbox, zonas
  de impacto, altura de ojos y controladores de movimiento.
- El interruptor manual de spawner activado/desactivado fue eliminado: el
  funcionamiento depende de proximidad, luz, cadencia y capacidad.

### Variantes élite

- Cada perfil puede activar una variante élite con probabilidad individual.
- Permite prefijo editable, multiplicadores de vida, velocidad y escala,
  equipo alternativo y una tabla de loot élite adicional.
- Los élites son hostiles, refuerzan su nombre y muestran avisos de aparición,
  proximidad y derrota.
- Se incorporó un `ModelVFX` violeta para distinguirlos sin sustituir la
  textura original del rol.
- Se corrigió la lectura de configuraciones compuestas para que no colapsen en
  el primer perfil y se dejó una prueba de regresión con cuatro perfiles.

### Interfaz dentro del juego

- La primera pantalla es una portada ligera con instrucciones, campo para
  pegar el código `CMS1`, `Guardar y cerrar`, `Cancelar`, copia de URL y acceso
  opcional al editor completo.
- Exportar el spawner solo aparece en la configuración completa.
- El bloque aparece en el inventario creativo normal y los jugadores reciben
  en el chat la guía y URL del configurador al entrar.

### Configurador web

- Rediseño sobrio claro/oscuro, conservando la versión anterior como backup.
- Secciones reordenadas: mob, objetos equipados, loot y spawner.
- Perfiles editables por pestañas (`Mob 1`, `Mob 2`, etc.) y configuración élite
  independiente por mob.
- Selectores de mobs y objetos con catálogos, nombres, iconos y previews
  extraídos de la instalación local de Hytale.
- Filas de equipo y loot compactas: el objeto seleccionado sustituye el campo
  vacío y la `X` lo restaura.
- Modelo, vida y actitud comparten fila; velocidad y escala comparten otra;
  peso y nombre del mob quedan debajo.
- La preview es una tarjeta más compacta. Ya no superpone iconos de equipo
  porque las miniaturas los recortaban.
- La tarjeta muestra HP, defensa física estimada y ataque estimado. El ataque
  suma el valor base resoluble del rol y el daño básico del objeto de mano.
- Los generadores extraen estadísticas heredadas de `Assets.zip`; el catálogo
  de mobs pasó a regenerarse en unos 12 segundos mediante cachés.
- El HTML offline contiene 3321 PNG incrustados y funciona sin instalación ni
  conexión. No debe versionarse porque contiene assets vanilla.

## Observaciones de la última prueba

### 1. Tinte élite inestable

El tinte se aplica, pero el `ModelVFX` cicla entre un resultado aceptable
(textura visible, ligeramente oscurecida y violeta) y fases con tanta opacidad
que el mob se convierte en una silueta morada plana. Debe conservarse un tinte
sutil y estable; nunca debe ocultar el detalle de la textura.

Revisar la curva/timeline, mezcla y alpha del asset VFX. Si el runtime no
permite fijar una mezcla estable, probar una combinación más segura de tinte
leve con outline o partículas, sin reemplazar el material completo.

En 0.5.2 se añadió un estallido de partículas al aparecer el élite como refuerzo
visual independiente. Es un complemento, no la corrección: la inestabilidad del
`ModelVFX` sigue sin resolverse y debe verificarse en partida.

### 2. Explicar el peso de aparición

Añadir ayuda visible y tooltip junto al campo. Texto recomendado:

> El peso indica la frecuencia relativa con la que se elige este mob. No es un
> porcentaje directo: con pesos 3 y 1, el primero aparecerá aproximadamente el
> 75 % de las veces y el segundo el 25 %. Cada criatura de la oleada se elige
> de forma independiente.

### 3. Defensa de la tarjeta y defensa real

- La tarjeta debe mostrar la defensa total aportada por todas las piezas
  equipadas, con unidades claras y sin omitir valores heredados.
- Comprobar en el plugin que esas propiedades se aplican realmente al mob,
  también cuando su modelo no renderiza la armadura.
- Comparar la suma web con los componentes finales de una entidad generada y
  crear una prueba de regresión con cuatro piezas conocidas.

### 4. Drops del equipo

- Verificar si el objeto de mano o las piezas equipadas caen al morir.
- Requisito: el equipo no debe dropearse automáticamente. Solo deben aparecer
  los objetos declarados expresamente en las tablas de loot normal o élite.
- Probar muerte normal y élite, tanto con loot vanilla como con loot
  personalizado/sustituido.

### 5. Ajustes pendientes de la tarjeta

- El tipo de mob aparece dos veces (por ejemplo, `Skeleton`); debe mostrarse
  una sola vez.
- Colocar la escala bajo el modelo del mob y justo encima de los indicadores,
  de forma equivalente a la referencia `Jugador · 1,85 m`.
- Quitar el texto independiente `Vida aumentada`. Si la vida difiere de la
  basal, mostrar esa indicación como texto secundario bajo el recuadro de HP.
- Añadir un cuarto recuadro de velocidad.
- Mantener los modelos centrados y equidistantes dentro de la tarjeta.

## Orden sugerido para retomar

1. Corregir primero el VFX élite y verificarlo con varios modelos y distancias.
2. Auditar defensa y drops en runtime antes de presentar cifras definitivas en
   la tarjeta.
3. Ajustar la tarjeta y añadir la explicación del peso.
4. Regenerar los catálogos y el HTML offline.
5. Ejecutar las pruebas del plugin, web y paquete, compilar un nuevo JAR y hacer
   smoke test en una partida reiniciada.
