# Entity Motion Triggers

Mod separado de `Player Trigger Tags` para mantener fuera de los tags de jugador
los efectos que mueven entidades o preparan props como plataformas.

Incluye:

- `ApplyHorizontalPlatformMotion`
- `StopHorizontalPlatformMotion`
- `ApplyPlayerPlatformCollision`
- `RemovePlayerPlatformCollision`
- `SpawnItems`

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
