# Mods publicos - Update 6 / Hytale 0.6.3

Revision: 2026-09-03. Notas oficiales leidas:
https://hytale.com/news/2026/8/update-6-patch-notes (27 de agosto).

## Alcance y resultado

Los cinco proyectos del catalogo `website/raynor-mods/portal-config.js`.
No incluye Build Battle, NPCs internos, hologramas ni el inspector experimental.
No se han publicado archivos en CurseForge, GitHub o Vercel ni modificado saves.

| Mod | Version nueva | Archivo |
| --- | --- | --- |
| More Triggers | 1.10.6 | More_Triggers-1_10_6.jar |
| Entity Motion Triggers | 1.3.3 | Entity_Motion_Triggers-1_3_3.jar |
| Particle Shape VFX | 0.1.2 | Particle_Shape_VFX-0_1_2.jar |
| Editable Objectives | 2.0.12 | Scoreboards-2_0_12.jar |
| Configurable Mob Spawners | 0.5.4 | ConfigurableMobSpawners-0.5.4.jar |

Son recompilaciones de mantenimiento, sin cambios de mecanicas, IDs o datos
persistentes. Conservan el rango declarado `>=0.6.2 <0.7.0`; la prueba de estos
binarios se ha ejecutado en 0.6.3, no en cada version de ese rango.

## Dependencias y reproduccion

- JDK 25.
- Servidor release 0.6.3, revision `ff802bf5a538f7e4b1df43a575c72f9d2bebb504`.
- SHA-256 servidor: `BB4ECBE2D1BC189C22E63491EB4078C95B42EF6CBE6650CCFA3E6160AAED7102`.
- Assets.zip de la misma instalacion release; no se incorpora al repositorio.

```powershell
./scripts/Test-ActiveJavaMods.ps1 -PublishedOnly
./scripts/Export-PublishedMods.ps1
```

El build comun usa release por defecto. La matriz pasa el mismo ServerJar a
los tests CMS1, geometria y resolucion de tags; antes algunos probaban contra
pre-release aun cuando el build principal usaba estable. Incluye ahora el test
de localizacion de Entity Motion Triggers. No usar New-ActiveModsBundle.ps1 para
publicar: ese bundle historico incluye proyectos internos.

## Revision tecnica

Las notas anuncian cambios de protocolo, chunks/luz, NPC ECS, colisiones,
senales y prefabs. Los cinco proyectos ya contenian la adaptacion a 0.6.2 y
compilan contra 0.6.3 sin roturas de API. Spawners conserva un aviso javac de
API deprecated, no un error. No se han cambiado APIs funcionales solo por el aviso.

More Triggers pre-rota el prefab mediante BlockSelection.rotate y pasa
Rotation.None al pegado final: no solicita una segunda rotacion. Se conserva
esta ruta y sus reservas, pendiente de verificar visualmente los cuatro yaw.
Los mensajes con tags propios conservan sus IDs aunque vanilla tenga funciones
similares. No se eliminan efectos usados en saves.

## Evidencia y limites

Revalidacion adicional: el 10 de septiembre de 2026, la matriz publicada con
Raynor Holograms 0.8.3 tambien paso contra Hytale estable `0.6.4`, revision
`108a26d534805e68dcf0d00638914cb0db82555b`. El 16 de septiembre se repitio
contra el hotfix estable `0.6.6`, revision
`ad11b033782bd8f5e715d2e8497d491f5780640c`, con compilacion, localizacion,
pruebas funcionales y empaquetado correctos. Esta nota conserva 0.6.3 en el
titulo porque es el objetivo de la release y el nombre de sus artefactos.

- Compilacion de los cinco JARs correcta.
- Localizacion ES/EN, tests funcionales disponibles y tests de paquete correctos.
- CMS1 round-trip, luz, geometria, ocupacion, timer y senales correctos.
- Scoreboards: comprobaciones de threading y traducciones dinamicas correctas.
- Test web correcto con los catalogos locales existentes; no se regeneraron.
- Arranque aislado `--bare`, offline, sin mundos: los cinco plugins Enabled,
  sin identificadores duplicados y con cierre de los cinco plugins.
- Bare produce errores de PermissionsModule (Stream closed), dependencias
  vanilla y Transport.shutdown nulo. Reproducidos tambien sin mods; no se
  presenta este ensayo como un arranque completo de servidor jugable.
- No hay smoke test nuevo con cliente: verificar UI, conexion, spawners/loot,
  plataformas, VFX, prefabs rotados y persistencia de scoreboards antes de
  declarar estabilidad. Los pendientes del HANDOFF de Spawners siguen vigentes.

## SHA-256 de los artefactos probados

```text
950841E5BF4B537D7E2AC46319D03E8BD92EA9ED113CBF12C9663FBF714B2744  More_Triggers-1_10_6.jar
56BA265652A010EF796428214E93EDD33E019F778B32291F992008BC8633BB84  Entity_Motion_Triggers-1_3_3.jar
EAE39C9FD22AFC54710561B3C9A2B6562DEA6B3E95F2169A7B7194061151E732  Particle_Shape_VFX-0_1_2.jar
DA0ECD8A0F6CD603DDC85F7D2354952492B9BDC2610624B18979280597CCAD6E  Scoreboards-2_0_12.jar
B2B7354A34F8CB5DDFD69869F480918027A8ADE3A5C03506F6C0C8310B1E80DA  ConfigurableMobSpawners-0.5.4.jar
```

Una recompilacion puede cambiar hashes por timestamps del ZIP. El exportador
calcula los hashes de los archivos efectivamente incluidos.
