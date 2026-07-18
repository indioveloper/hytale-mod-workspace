# Entity Motion Triggers

Mod separado de `Player Trigger Tags` para mantener fuera de los tags de jugador
los efectos que mueven entidades o preparan props como plataformas.

Incluye:

- `ApplyHorizontalPlatformMotion`
- `StopHorizontalPlatformMotion`
- `ApplyPlayerPlatformCollision`
- `RemovePlayerPlatformCollision`

Estado: funcional en fuente para pre-release 0.6.8. La revision 1.0.1 corrige
el descarte erroneo de movimientos horizontales cuando los ejes Y/Z permanecen
quietos. Requiere una prueba manual en juego para confirmar el resultado final.
