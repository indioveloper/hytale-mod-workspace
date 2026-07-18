# Avatar Sword Runner

Primer NPC vanilla del proyecto.

## Fantasia

Un avatar humano aparece en la arena, empuna una espada y sigue el path que le
asignes manualmente, blandiendo el arma durante el avance. Sirve como primera
pieza para probar oleadas, presion de carril o ataques automaticos al nexo.

## Requisitos

- Modelo humano vanilla: `Player` como fallback, con `--randomModel` en spawn si quieres avatar aleatorio.
- Equipo visible: espada vanilla en mano principal.
- Movimiento: seguir un path asignado despues del spawn.
- Animacion: swing/ataque de espada repetido mientras avanza.
- Sin dependencia del plugin del servidor.

## Diseno Vanilla Propuesto

El NPC se define como un `Role` generico con:

- `Appearance`: `Player`.
- `HotbarItems`: `Weapon_Sword_Iron`.
- `MotionControllerList`: controlador `Walk` con velocidad alta.
- `InitialMotionController`: `Walk`.
- `Sensor`: `Path` con rango amplio para exponer el path activo.
- `BodyMotion`: `Path` con `Shape: LINE`.
- `Actions`: `Attack` primario y `PlayAnimation` en slot `Action`.

El role no contiene nodos ni distancias hardcodeadas. El path se asigna con el
comando vanilla `/npc path`, que recibe pares `rotacion,distancia` relativos a
la posicion y rotacion actuales del NPC.

## Parametros Iniciales

- `max_walk_speed`: `5.5`
- `path_sensor_range`: `128`
- `attack_pause`: `0.6s` a `0.9s`
- `spawn_lock_time`: `0.2s`
- `max_health`: `40`

## Prueba Manual

1. Colocar el spawn en una plataforma plana.
2. Spawnear un solo NPC en A.
3. Mirar al NPC o guardar su entity id.
4. Ejecutar `/npc path` con los nodos deseados.
5. Verificar que usa modelo humano.
6. Verificar que aparece con espada en mano.
7. Verificar que avanza por el path asignado.
8. Verificar que la animacion de swing se repite durante el avance.

## Riesgos

- Para modelo humano aleatorio, usar `/npc spawn Nexus_Avatar_Sword_Runner --randomModel`.
- Los nodos de `/npc path` son relativos, no coordenadas XYZ absolutas directas.
