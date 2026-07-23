# Dungeon Temporal

Snapshot de la partida de pruebas `dungeon temporal`, actualizado el 23 de
julio de 2026 a partir del save activo en Hytale pre-release.

## Contenido

- `dungeon-temporal-2026-07-23.zip`: save restaurable, sin `logs`,
  `telemetry`, backups historicos ni archivos `.bak`.

SHA-256 del ZIP:

```text
7D9DCB99D53AFF0DB47E1B8FA7A0DE7B1F91D513DB5962C6C7C24E5CD6293887
```

## Restaurar

1. Clona el repositorio con Git LFS habilitado (`git lfs install`).
2. Extrae el ZIP en
   `%APPDATA%\Hytale\data\pre-release\Saves\dungeon temporal`.
3. Conserva o recompila los mods del workspace si quieres validar el estado
   exacto del mapa con el mismo set de dependencias.

## Dependencias observadas en el save

El ZIP incluye la carpeta `mods` de la partida, con estos elementos visibles al
crear el snapshot:

- `ChestLabels-0_1_0.jar`
- `Entity_Motion_Triggers-1_0_8.jar`
- `Player_Trigger_Tags-1_5_6.jar`
- `Raynor_NPCs-1_1_0.jar`
- `Scoreboards-1_0_0.jar`
- `Trigger_Execute_Command-1_1_0.jar`
- `OrbGenesis-Blocks/`
- `OrbGenesis-Mechanisms/`
- `Hytale_HytaleGenerator/`
- `Hytale_Shop/`

Tambien incluye la estructura de mundo y datos de universo necesarios para
restaurar el estado actual de la partida.
