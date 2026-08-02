# Particle Shape VFX

Mod independiente para Hytale pre-release 0.6.x. Registra el efecto de Trigger
Volumes `SpawnParticleShape`, que calcula posiciones exactas y crea figuras con
un `ParticleSystem` puntual en cada posicion.

## Figuras

- `CUBE_EDGES`: las 12 aristas de un cubo cuyo lado mide `Size` bloques.
- `SPHERE_SURFACE`: puntos repartidos de forma uniforme por una esfera cuyo
  diametro mide `Size` bloques.
- `LINE`: segmento exacto desde `Start` hasta `End`, incluyendo ambos extremos.

`CoordinateMode` permite interpretar `Center`, `Start` y `End` como coordenadas
absolutas del mundo o como offsets desde el centro del Trigger Volume. `Spacing`
controla la densidad y `MaxPoints` limita el coste; si el limite se alcanza, el
generador aumenta la separacion de manera uniforme.

El mod incluye los sistemas puntuales `OrbGenesis_Shape_Point_Red`,
`OrbGenesis_Shape_Point_YellowOrange` y `OrbGenesis_Shape_Point_Green`,
pensados para figuras limpias. Tambien se puede seleccionar cualquier otro
`ParticleSystem` desde el editor.

Al anadir el JAR a una partida existente, comprueba que `Particle Shape VFX`
queda habilitado en la configuracion de mods del save. Hytale puede registrar
los mods nuevos inicialmente como desactivados; si Mechanisms esta activo y
este mod no, el servidor rechazara la dependencia antes de cargar el mundo.

## Ejemplo: cubo de 5 bloques

```text
Shape: CubeEdges
ParticleSystem: OrbGenesis_Shape_Point_Red
CoordinateMode: RelativeToVolume
Center: (0, 0, 0)
Size: 5
Spacing: 0.12
ParticleScale: 1
Duration: 10
MaxPoints: 512
```

## Build y pruebas

```powershell
.\mods\java\particle-shape-vfx\tools\Test-PluginLocalization.ps1
.\mods\java\particle-shape-vfx\tools\Test-Geometry.ps1

.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\particle-shape-vfx `
  -SourceRoot src `
  -PackageRoot src `
  -ArtifactName Particle_Shape_VFX-0_1_0.jar

.\mods\java\particle-shape-vfx\tools\Test-Package.ps1 `
  -ArchivePath .\mods\java\particle-shape-vfx\.build\dist\Particle_Shape_VFX-0_1_0.jar
```
