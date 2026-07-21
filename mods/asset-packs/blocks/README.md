# OrbGenesis Blocks

Pack fusionado de bloques tecnicos y de previsualizacion.

Incluye:

- Barrier block: una unica barrera tecnica con textura de contorno rojo sutil.
- Ghost outline block: bloque transparente/no colisionable para previsualizar
  plataformas bloqueadas o futuras posiciones.
- Tally marks: un unico item mural que coloca una raya tallada y cambia entre
  I, II, III, IIII y cuatro rayas tachadas al volver a usarlo sobre la marca.
  Su hitbox tiene `0.05` bloques de profundidad (aprox. 1.6 pixeles).

La barrera visible no puede ocultarse solo en modo aventura mediante datos de
bloque. Para ese caso de uso, emplea una entidad con
`HiddenFromAdventurePlayers`.

Origen:

- `mods/asset-packs/visible-barrier-block`
- `mods/asset-packs/ghost-outline-blocks`

Estado: pack orientado a pre-release 0.6.x. Requiere prueba visual en 0.6.8
para confirmar iconos, texturas e IDs de item.

## Marcas de conteo

Busca `OrbGenesis_Tally_Marks` o "Marcas de conteo" en creativo. Colocalo
sobre una cara vertical solida para crear la primera raya. Usa el item otra vez
sobre la marca ya colocada para avanzar el conteo; despues de cinco vuelve a
uno. El bloque tambien responde a la interaccion de uso con la mano vacia.

Las texturas se regeneran con:

```powershell
python .\tools\make_tally_mark_textures.py
```
