# Player Trigger Tags Index

> Indice historico. Las rutas y responsabilidades descritas debajo son previas
> a la consolidacion; consulta el README del directorio deprecated.

`mods/java/player-trigger-tags` es la fuente canonica para tags persistentes de
jugador y efectos relacionados con Trigger Volumes.

## Estado Actual

- Version: `1.5.6`.
- Server range: `>=0.6.0-pre <0.7.0`.
- Compila contra la instalacion local de pre-release `0.6.8`.
- Las localizaciones `en-US` y `es-ES` pasan la comprobacion del repo.

## Incluye

- `ModifyPlayerTag`.
- `PlayerTagCondition`.
- `ConvertBlocksToEntities`.
- Componente persistente `PlayerTagsComponent`.
- Colision diferida para entidades creadas desde bloques mediante
  `PendingPlatformCollisionComponent/System`.

## Unificacion

Las variantes historicas `Player Triggers` y `Player Triggers 1.3.2 local` se
han cerrado como fuentes separadas. Las funciones modernas de tags viven aqui.

Los efectos de movimiento y colision de plataforma se sacaron a
`mods/java/entity-motion-triggers` para evitar mezclar movimiento de entidades
con tags de jugador.
