# OrbGenesis Mechanisms

Pack fusionado de props accionables/animados. La version actual es `1.0.5`.

Incluye:

- Real lever: item/modelo de palanca real y variante de pared con animaciones
  `on`/`off`; el centro pasa a naranja al activarse y la colocacion sigue la
  cara del bloque seleccionada.
- Essence buttons: cuatro botones flotantes animados con iconos y texturas de
  esencia `Ice`, `Life`, `Lightning` y `Void`. Se apoyan sobre cualquier
  bloque sin requisito de cara completa, usan el aviso nativo de activacion y
  tienen el modelo colocado al 75% de escala.
- No-build stone: el item `OrbGenesis_NoBuild_Stone` hereda el lanzamiento de
  `Rubble_Stone`. Al impactar crea un Trigger Volume centrado en el impacto, de
  `5 x 5 x 5` bloques y con una vida de 10 segundos. En `VOLUME_CREATE`, un
  efecto `ModifyRules` con operacion `SET` inserta la regla vanilla `NoBuild`;
  este paso evita el bug de 0.6.8 por el que `SpawnTriggerVolume` no copia las
  reglas del asset al volumen recien creado.
- El mismo evento ejecuta 12 efectos `PlayVfx`, uno por cada arista, durante 10
  segundos. Todos reutilizan el sistema de particulas vanilla
  `Beam_Heal_Red3`: escala `5`, offsets en las esquinas negativas de cada eje y
  rotaciones de 90 grados para alinear los beams con `X`, `Y` y `Z`. El pack no
  define sistemas ni spawners de particulas propios.

## Prueba de la piedra

```text
/give OrbGenesis_NoBuild_Stone 1
```

Lanza la piedra, intenta colocar un bloque dentro de la zona marcada y repite
la accion tras 10 segundos. Durante la vida del volumen la colocacion debe ser
rechazada; despues de expirar debe volver a funcionar.

Validacion estatica:

```powershell
.\mods\asset-packs\mechanisms\tools\Test-NoBuildProjectile.ps1

.\mods\asset-packs\mechanisms\tools\Test-Package.ps1 `
  -ArchivePath .\mods\asset-packs\mechanisms\.build\dist\OrbGenesis_Mechanisms-1_0_5.zip
```

Origen:

- `mods/asset-packs/real-lever-animation`
- `mods/asset-packs/essence-buttons`

Estado: pack orientado a pre-release 0.6.x. La piedra requiere smoke test visual
en 0.6.8 para ajustar densidad, color y visibilidad del perimetro de particulas.
