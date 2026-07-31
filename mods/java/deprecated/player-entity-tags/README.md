# Player Entity Tags (deprecated)

Codigo historico de `OrbGenesis:Player Trigger Tags`, apartado de la
distribucion activa durante la consolidacion de More Triggers.

Conserva:

- el efecto `ModifyPlayerTag`;
- la condicion `PlayerTagCondition`;
- el componente persistente `OrbGenesis_PlayerTriggerTags` y sus utilidades.

`ConvertBlocksToEntities` ya no pertenece a este proyecto: se traslado a
`mods/java/entity-motion-triggers`, junto con la colision diferida que necesitan
las entidades recien creadas.

El manifest queda desactivado por defecto. No debe empaquetarse ni instalarse
como parte del conjunto activo; la fuente se conserva para consultar o migrar
saves antiguos.
