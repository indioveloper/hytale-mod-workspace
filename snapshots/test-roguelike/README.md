# Test Roguelike

Snapshot de la partida de pruebas creada el 23 de julio de 2026 con Hytale
pre-release `0.6.0-pre.8.1`.

## Contenido

- `test-roguelike-2026-07-23.zip`: save restaurable, sin logs, telemetria,
  locks ni backups historicos.
- `asset-packs/tests.tests`: copia editable del asset pack activo, incluidas
  sus `PrefabList`, prefabs y metadatos `.lpf`.

SHA-256 del ZIP:

```text
614EA2A96CB37BD0EBFF0A2A7A6886A75B1E9C7C4BDE52E9F14259DFBA91DE93
```

## Restaurar

1. Clona el repositorio con Git LFS habilitado (`git lfs install`).
2. Extrae el ZIP en `%APPDATA%\Hytale\data\pre-release\Saves\test roguelike`.
3. Copia `asset-packs/tests.tests` a
   `%APPDATA%\Hytale\data\pre-release\Mods\tests.tests`.
4. Compila o instala `OrbGenesis:More Triggers` desde
   `mods/java/more-triggers` si no quieres usar el JAR incluido en el ZIP.

## Plugins activos

- `tests:tests`: assets y prefabs propios del prototipo.
- `OrbGenesis:More Triggers` `1.1.0`: contiene `PasteRandomPrefab`.
- `BeyondSmash:EffectShowcase` `1.0.3`: dependencia externa de pruebas; su JAR
  global no se redistribuye en este repositorio.

El ZIP contiene el JAR de `More Triggers` dentro de la carpeta `mods` del save.
