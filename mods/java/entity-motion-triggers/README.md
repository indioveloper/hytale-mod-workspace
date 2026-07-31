# Entity Motion Triggers

Mod separado de `Player Trigger Tags` para mantener fuera de los tags de jugador
los efectos que mueven entidades o preparan props como plataformas.

Incluye:

- `ApplyHorizontalPlatformMotion`
- `StopHorizontalPlatformMotion`
- `ApplyPlayerPlatformCollision`
- `RemovePlayerPlatformCollision`
- `SpawnItems`
- `AttachMovingParticles`

Estado: funcional en fuente para pre-release 0.6.8. La revision 1.1.0 admite
destinos absolutos o desplazamientos relativos para cada entidad del volumen.
El movimiento puede incluir un giro relativo a izquierda o derecha de 0 a 180
grados, que se completa al llegar al destino. Los ejes con velocidad cero se
mantienen en su posicion actual. La opcion `DestroyAtDestination` elimina la
entidad al alcanzar por primera vez el destino, incluso si tambien se marco el
bucle. Requiere una prueba manual en juego para confirmar el resultado final.

`SpawnItems` crea un item como prop persistente y no recogible, usando el
selector vanilla de items. Permite coordenadas absolutas o relativas al Trigger
Volume, rotacion XYZ en grados, escala configurable y colision `NONE`, `HARD` o
`SOFT`. La colision se aplica dos ticks despues del spawn para sincronizarse con
el cliente.

La version 1.2.0 anade `AttachMovingParticles`. El efecto vincula un sistema de
particulas al `NetworkId` de cada entidad encontrada dentro del Trigger Volume,
por lo que el emisor sigue automaticamente el movimiento y el giro de la
plataforma en el cliente. La correccion 1.2.1 deja
`DetachEmittedParticles` desactivado por defecto y registra en el log cuantas
entidades y jugadores recibieron el efecto. Al activarlo, el cliente desacopla
el sistema del modelo y deja de seguir la entidad.

La correccion 1.2.2 envia `RotationX`, `RotationY` y `RotationZ` en los grados
que espera `ModelParticle`. Las particulas de mundo usan otra convencion en la
API 0.6.8, por lo que convertir estos offsets a radianes producia giros casi
imperceptibles.

La revision 1.2.3 mejora las ayudas del editor. La clave interna
`OnlyFirstMatch` se conserva para no romper volumenes guardados, pero ahora
selecciona de forma determinista la entidad mas cercana al centro del volumen.
Para una sola plataforma es preferible dejarla desactivada y ajustar el volumen
para que solo contenga la entidad deseada.

Para una estela de cohete:

1. Ejecuta `AttachMovingParticles` sobre el mismo volumen que contiene la
   plataforma.
2. Elige el `ParticleSystem` y ajusta `OffsetY` hasta situarlo bajo la base.
3. Deja `DetachEmittedParticles` desactivado para que el sistema siga la
   entidad. La forma de la estela depende del sistema de particulas elegido.
4. Ejecuta `ApplyHorizontalPlatformMotion` sobre esa plataforma.

`TargetNodeName` es opcional y solo debe rellenarse si el modelo tiene un nodo
conocido al que anclar el emisor. El efecto se envia a los jugadores que estan
a 75 bloques cuando se activa; requiere validacion manual en juego para
confirmar el sistema de particulas concreto y su orientacion.
