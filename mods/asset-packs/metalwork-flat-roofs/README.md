# Metalwork Flat Roofs

Version 0.1.3: Void y las nuevas copias Raynor_Metal_Iron_Roof_Flat y
Raynor_Metal_Copper_Roof_Flat usan SeamSafe_Roof_Flat, modelo propio de una
pieza sin inclinacion ni salientes. Huella 31.984x31.984, Y=0..6 unidades;
la separacion de 0.0005 bloques evita caras coplanares entre vecinos.
Corrige la interseccion de las tapas vanilla (stretch 1.059). Se conservan
las UV y se referencian las texturas e iconos originales de hierro/cobre.
Los bloques vanilla existentes requieren sustitucion por las copias nuevas.
Void mantiene su ID y se actualiza al recargar. Prueba visual pendiente.

Version 0.1.2: nuevo Raynor_Metal_Void_Roof_Flat (Ornate Void Flat Roof).
Hereda Metal_Iron_Roof_Flat, referencia su modelo vanilla y usa un atlas
original opaco 160x352 con ornamento superior en (68,284), 32x32 pixeles.
Build-VoidAtlas.ps1 adapta art/Void_Ornament_Source.png a las UV.
Arte generado con image_gen integrado: baldosa cenital violeta oscuro,
doble borde amatista, grabados simetricos y sigilo central de rombos.
Sin emision de luz. Prueba visual en juego pendiente.

Version 0.1.2 incluye un decimo bloque: Raynor_Metal_Void_Roof_Flat,
Ornate Void Flat Roof. Hereda Metal_Iron_Roof_Flat y referencia su modelo
vanilla sin copiarlo. Atlas original opaco 160x352; ornamento superior en
(68,284), 32x32. Laterales violeta oscuro. Sin emision de luz.
Se obtiene en creativo buscando Void o Vacio. Prueba visual pendiente.
Build-VoidAtlas.ps1 adapta el arte original a las UV del modelo.
La fuente artistica vive en art/Void_Ornament_Source.png.
Generada con image_gen integrado: baldosa cuadrada vista cenital, metal
violeta oscuro, doble borde amatista, grabados angulares simetricos en
esquinas y sigilo central de cuadrados/rombos, sin texto ni transparencias.

Version 0.1.1: Adamantite usa UV de 13x13 en (1,0) y laterales
de 13x1 en (1,13), las regiones opacas de la textura original.
Stretch conserva un bloque de 32x4x32 unidades. Las otras ocho variantes
siguen pendientes de la misma correccion. Validacion visual en juego pendiente.
Instalacion actual: UserData/Saves/Anima 0.6.X/mods; habilitar
RaynorMods:Metalwork Flat Roofs en la configuracion de la partida.

Asset pack experimental para Hytale estable 0.6.x. Anade nueve tejados planos
metalicos con los acabados de Metalwork 1.0.0:

- Adamantite
- Cobalt
- Gold
- Mithril
- Onyxium
- Prisma
- Refined Iron
- Silver
- Thorium

Bronze, Copper, Iron y Zinc no se duplican porque Hytale ya incluye sus propios
`Metal_*_Roof_Flat`.

Cada item hereda `Rock_Stone_Brick_Roof_Flat`, igual que los tejados metalicos
vanilla, y sustituye sonidos, material fisico, particulas, modelo y textura por
sus variantes metalicas. El modelo propio ocupa un bloque completo y cuatro
unidades de alto, y usa directamente las texturas 32x32 de Metalwork sin copiar
el atlas vanilla.

Las recetas estan desactivadas como en `Metal_Bronze_Roof_Flat`; de momento los
bloques se obtienen desde el inventario creativo, categoria `Blocks.Metal`.

Los iconos son provisionales y reutilizan la silueta de placa de Metalwork para
identificar bien cada color hasta renderizar iconos de tejado definitivos.

## Build

```powershell
.\mods\asset-packs\metalwork-flat-roofs\tools\Test-Pack.ps1
.\mods\asset-packs\metalwork-flat-roofs\tools\Build-Pack.ps1
```

Los recursos derivados de Metalwork conservan su licencia GPL-3.0-only y su
atribucion en `THIRD_PARTY_NOTICES.md`.
