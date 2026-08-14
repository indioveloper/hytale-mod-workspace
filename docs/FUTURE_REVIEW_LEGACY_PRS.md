# Contenido legacy pendiente de revision

Esta rama, `codex/future-review-legacy-prs`, conserva en un unico lugar el
material de los antiguos PR #1, #6 y #7. Parte de `main` tras fusionar los PR
#13 y #5 (`aa47f28`). No debe fusionarse completa ni distribuirse como un pack:
su finalidad es conservar fuentes y contexto para una revision selectiva.

## PR #6: assets recuperados de la instalacion

Fuente: `agent/import-installed-mods-inventory`, commit `2f3a58e`.

Su contenido editable si esta presente en el arbol de esta rama:

- inventario de 27 partidas en
  `docs/PRE_RELEASE_INSTALLATION_INVENTORY_2026-07-29.md`;
- prototipos Connect4 e Infinity Pillars bajo `experiments/asset-packs`;
- botones de esencia, NPC de Nexus Siege y palanca animada legacy bajo
  `experiments/asset-packs`;
- tally marks incorporados a `mods/asset-packs/blocks`;
- packs independientes antiguos de ghost outlines y barrera visible;
- prefabs recuperados de Nexus Siege Props.

Los conflictos de README y manifest se resolvieron conservando la metadata
moderna de `main`. Las versiones exactas del PR siguen disponibles en su padre
historico y pueden consultarse con:

```powershell
git show 2f3a58e:<ruta>
git diff 2f3a58e^..2f3a58e
```

Antes de promover cualquier asset hay que comparar sus IDs con `blocks`,
`mechanisms`, `raynor-npcs` y `nexus-siege-props`. Varios packs son fuentes
legacy de proyectos que luego se consolidaron y no deben instalarse juntos.

## PR #7: particulas ancladas a entidades

Fuente: `codex/entity-particle-trails`, commit `abb923c`.

El PR contenia la primera implementacion de `AttachMovingParticles` para Entity
Motion Triggers 1.2.3 y fue validado en Hytale 0.6.8. No se aplico su arbol
porque `main` ya contiene una implementacion posterior en Entity Motion
Triggers 1.3.1, con mas campos, documentacion y compatibilidad pre.12.

El commit se conserva como padre historico de esta rama. Para compararlo:

```powershell
git diff abb923c^..abb923c
git show abb923c:mods/java/entity-motion-triggers/src/gg/orbgenesis/playertriggertags/AttachMovingParticlesEffect.java
```

Solo deben rescatarse ideas o pruebas que no existan en la version activa; no
se debe registrar por segunda vez el ID `AttachMovingParticles`.

## PR #1: primer workspace de mods y snapshots

Fuente: `agent/tally-marks-and-068-fixes`, commits `c2d78b1`, `109687f` y
`f33afc9`.

Contenia:

- una version inicial de tally marks;
- More Triggers 1.1.0 con `PasteRandomPrefab`, `RandomTagSelection` y
  `RemoveEventTitle`;
- la primera guia HTML de Trigger Volumes;
- ajustes antiguos de Scoreboards y Chest Labels;
- snapshots de test roguelike y dungeon, incluidos ZIPs y metadatos LPF.

No se aplico su arbol. La guia ya esta en `main`, los tally marks quedan
preservados desde el PR #6 y More Triggers ha evolucionado hasta 1.9.2.
`RandomTagSelection` fue eliminado deliberadamente y no debe reintroducirse.
Los ZIPs, saves y outputs tampoco deben volver al historial; si un snapshot
sigue siendo util, debe publicarse como artifact de GitHub Release.

Los commits se conservan como padres historicos y se pueden inspeccionar con:

```powershell
git diff c2d78b1^..c2d78b1
git diff 109687f^..109687f
git diff f33afc9^..f33afc9
```

## Lista para una futura limpieza

1. Revisar manifests e IDs antes de mover un asset fuera de `experiments`.
2. Comparar visualmente tally marks, barreras, ghost outlines y prefabs Nexus.
3. Conservar atribuciones de sonidos, modelos y codigo de terceros.
4. Portar cualquier codigo rescatado a la API Hytale vigente y ejecutar sus
   pruebas de localizacion, build y paquete.
5. No copiar JARs, ZIPs, clases, saves, assets vanilla ni Shared Source.
6. No reintroducir plugins o efectos ya consolidados bajo el mismo ID.

## Estado de los PR originales

Los PR #1, #6 y #7 se cierran con la etiqueta `future-review`. Sus ramas
originales se mantienen en GitHub como referencia y esta rama es el punto unico
para continuar la revision.
