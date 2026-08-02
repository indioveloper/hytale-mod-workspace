# test roguelike

Snapshot de la partida de pruebas roguelike del 26 de julio de 2026.

El ZIP completo se publica en la GitHub Release
`test-roguelike-2026-07-26`, no en el historial Git. Contiene la configuracion,
preview y universo de la partida. Se excluyen backups, logs, telemetria,
archivos `.bak` y `mods`.

Para restaurarlo:

1. Extrae `test-roguelike-2026-07-26.zip` bajo
   `%APPDATA%\Hytale\data\pre-release\Saves`.
2. Instala `mods/asset-packs/roguelike-prefabs` como `Mods/tests.tests`.
3. Compila o instala More Triggers y Entity Motion Triggers.
4. Comprueba que solo haya una copia de cada `Group:Name`.

El snapshot original tambien contenia Map Selector, pero ese prototipo se
conserva ahora en `auxiliary/java/map-selector` y no es necesario para usar el
mapa. Instalalo solo si necesitas reproducir su antigua demo de `/mapas`.

Los mod IDs activos en el snapshot son:

- `tests:tests`
- `OrbGenesis:More Triggers`
- `OrbGenesis:Entity Motion Triggers`
- `OrbGenesis:Map Selector` (historico y opcional)
