# Entity Motion Triggers

Mod independiente que agrupa los efectos para crear, convertir, mover y
configurar entidades usadas como props o plataformas. La version `1.3.2` esta
compilada y validada con Hytale Update 6 estable `0.6.2`.

## Efectos incluidos

| Efecto | Para que sirve |
| --- | --- |
| `SpawnItems` | Crea un item como prop persistente, con posicion, rotacion, escala y colision. |
| `ConvertBlocksToEntities` | Sustituye bloques del volumen por entidades que usan un item o bloque como aspecto. |
| `ApplyHorizontalPlatformMotion` | Mueve las entidades del volumen hacia un destino absoluto o relativo. |
| `StopHorizontalPlatformMotion` | Retira el movimiento y deja las entidades en su posicion actual. |
| `ApplyPlayerPlatformCollision` | Aplica una configuracion de colision para que una entidad pueda transportar jugadores. |
| `RemovePlayerPlatformCollision` | Retira la colision de plataforma de las entidades del volumen. |
| `AttachMovingParticles` | Ancla un sistema de particulas a una entidad para que el emisor siga su posicion y rotacion. |

Los IDs publicos se conservan sin cambios para mantener compatibles los Trigger
Volumes existentes.

## Creacion y conversion

`SpawnItems` crea un item como prop persistente y no recogible usando el
selector vanilla de items. Permite coordenadas absolutas o relativas al Trigger
Volume, rotacion XYZ en grados, escala configurable y colision `NONE`, `HARD` o
`SOFT`.

`ConvertBlocksToEntities`, trasladado desde Player Trigger Tags, reemplaza cada
bloque ordinario del volumen por una entidad con el item elegido. Admite items
con modelo, items-bloque y otros items. Con `HARD` o `SOFT`, la colision se
aplica dos ticks despues para que el cliente reciba primero la geometria y el
`NetworkId`. Se conserva el ID de componente
`OrbGenesis_PendingPlatformCollision`.

## Movimiento y colision

`ApplyHorizontalPlatformMotion` admite destinos absolutos o desplazamientos
relativos, velocidad independiente por eje, bucle, destruccion al llegar y giro
relativo a izquierda o derecha entre 0 y 180 grados. Los ejes con velocidad
cero permanecen quietos.

Los efectos `ApplyPlayerPlatformCollision` y
`RemovePlayerPlatformCollision` permiten convertir los props compatibles en
plataformas transportadoras o retirarles esa capacidad.

## Particulas ancladas

`AttachMovingParticles` vincula un `ParticleSystem` al `NetworkId` de cada
entidad encontrada. Para una estela de cohete:

1. Coloca la plataforma dentro del Trigger Volume.
2. Ejecuta `AttachMovingParticles` antes de moverla.
3. Ajusta los `Offset` locales y la rotacion XYZ en grados.
4. Deja `DetachEmittedParticles` desactivado para que el sistema siga a la
   entidad.
5. Ejecuta `ApplyHorizontalPlatformMotion`.

`TargetNodeName` es opcional. Si se indica un hueso o nodo real del modelo, el
emisor sigue ese punto animado; vacio usa la raiz de la entidad.

## Build

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\entity-motion-triggers `
  -SourceRoot src `
  -PackageRoot src `
  -ArtifactName Entity_Motion_Triggers-1_3_2.jar
```

Antes de instalar, retira cualquier JAR anterior con el mismo `Group:Name` y
reinicia la partida tras sustituirlo.
