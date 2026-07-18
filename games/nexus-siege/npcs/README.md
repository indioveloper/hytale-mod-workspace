# NPCs

Trabajo vanilla de NPCs para Nexus Siege.

## Objetivo De Esta Seccion

Crear y probar NPCs que funcionen sin depender del plugin del servidor. Aqui
iremos definiendo roles, spawns, comportamientos, balance inicial y pruebas.

## Primer NPC

- Carpeta: `vanilla/avatar-sword-runner/`
- Concepto: avatar humano con `randommodel`.
- Comportamiento: al aparecer, corre hacia una posicion objetivo mientras blande
  una espada.
- Restriccion actual: vanilla-only, sin Java ni logica de plugin.

## Pendientes De Confirmar En Vanilla

- ID exacto del modelo humano `randommodel`.
- ID exacto del item de espada vanilla.
- ID exacto de la animacion de blandir espada.
- Forma mas limpia de fijar la posicion objetivo sin plugin.
