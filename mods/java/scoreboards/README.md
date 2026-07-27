# Scoreboards Mod

Mod de Hytale para crear y editar scoreboards/objectives desde una UI nativa.

Esta es la carpeta canonica para seguir desarrollandolo dentro del workspace:

- `mods/java/scoreboards`

Estado actual:

- comandos disponibles: `/scoreboard` y `/scoreboards`
- crea el objective nativo y tambien el HUD lateral
- la primera linea escucha kills de skeleton como prueba funcional
- la UI usa paginas estaticas `ScoreboardEditor1.ui` a `ScoreboardEditor5.ui`

Notas:

- el contexto historico de debugging esta en `CONTEXTO_SCOREBOARDS.md`
- el siguiente paso natural es generalizar triggers mas alla de `SKELETON_KILL`
- sigue siendo una rama legacy basada en Hytale 0.5.x; antes de usarlo en pre-release 0.6.x hay que volver a validar API y UI
