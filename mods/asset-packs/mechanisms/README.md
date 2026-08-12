# OrbGenesis Mechanisms

Pack fusionado de props accionables/animados. La version actual es `1.1.1`.

Incluye:

- Real lever: item/modelo de palanca real y variante de pared con animaciones
  `on`/`off`; el centro pasa a naranja al activarse y la colocacion sigue la
  cara del bloque seleccionada.
- Essence buttons: cuatro botones flotantes animados con iconos y texturas de
  esencia `Ice`, `Life`, `Lightning` y `Void`. Se apoyan sobre cualquier
  bloque sin requisito de cara completa, usan el aviso nativo de activacion y
  tienen el modelo colocado al 75% de escala.
- No-build stone: el item `OrbGenesis_NoBuild_Stone` hereda el lanzamiento de
  `Rubble_Stone`, pero usa el modelo, textura e icono vanilla de
  `Ingredient_Void_Essence`. Al impactar crea un Trigger Volume centrado en el
  impacto, de `10 x 10 x 10` bloques. En `VOLUME_CREATE`, un
  efecto `ModifyRules` con operacion `SET` inserta la regla vanilla `NoBuild`;
  este paso evita el bug de 0.6.8 por el que `SpawnTriggerVolume` no copia las
  reglas del asset al volumen recien creado. Un segundo `ModifyRules` retira
  `NoBuild` a los 10 segundos.
- `SpawnParticleShape` muestra una esfera de 10 bloques con separacion 1,
  escala 2 y limite 1500. Es roja de 0 a 9 segundos, amarillo-naranja de 9 a
  10 como aviso, y verde de 10 a 11 tras desbloquear la construccion.
- Reproduce `SFX_PORTAL_NEUTRAL_OPEN` en el centro al impactar y de nuevo en el
  segundo 10.

Requiere el mod `OrbGenesis:Particle Shape VFX` 0.1.0 o posterior.

## Prueba de la piedra

```text
/give OrbGenesis_NoBuild_Stone 1
```

Lanza la piedra e intenta colocar un bloque dentro de la esfera. La colocacion
debe rechazarse durante las fases roja y amarillo-naranja y volver a funcionar
al comenzar la fase verde en el segundo 10. Confirma tambien el segundo sonido
en ese momento.

Validacion estatica:

```powershell
.\mods\asset-packs\mechanisms\tools\Test-NoBuildProjectile.ps1

.\scripts\Build-AssetPack.ps1 `
  -ProjectPath .\mods\asset-packs\mechanisms `
  -ArtifactName OrbGenesis_Mechanisms-1_1_1.zip

.\mods\asset-packs\mechanisms\tools\Test-Package.ps1 `
  -ArchivePath .\mods\asset-packs\mechanisms\.build\dist\OrbGenesis_Mechanisms-1_1_1.zip
```

Origen:

- `mods/asset-packs/real-lever-animation`
- `mods/asset-packs/essence-buttons`

Estado: pack orientado a pre-release 0.6.x. La integracion con
`SpawnParticleShape` requiere smoke test visual dentro del juego.
