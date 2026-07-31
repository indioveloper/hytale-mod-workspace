# Mods Java deprecated

Esta carpeta conserva implementaciones retiradas como referencia y para futuras
migraciones. Sus manifests estan desactivados por defecto y no forman parte de
los builds ni de la distribucion activa.

- `player-entity-tags`: `ModifyPlayerTag` y `PlayerTagCondition` legacy.
- `chest-labels`: prototipo de nombres e iconos de contenedores con UI sin
  portar a 0.6.8.
- `trigger-execute-command-standalone`: sustituido por More Triggers 1.6.0.
- `more-triggers-retired-effects`: conserva `RemoveEventTitle`; el efecto
  `RandomTagSelection` se elimino completamente.

No instales estos proyectos junto a sus sustitutos: los IDs legacy se conservan
intencionadamente y podrian producir registros duplicados.
