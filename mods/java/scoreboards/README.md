# Scoreboards Mod

Carpeta canonica unificada para el mod de scoreboards.

Este proyecto conserva la variante mas completa, con editor UI, tracker de
scoreboards y sistema de muerte. La carpeta local `scoreboards-mod-local` fue
eliminada como copia separada.

Estado: legacy de Hytale 0.5.x. Antes de usarlo en pre-release 0.6.8 hay que
revisar API, manifest, UI nativa y eventos de muerte/scoreboard. En concreto,
`Common/UI/Custom/Common.ui` usa `PageOverlay`, un nodo que pre-release 0.6.8
no reconoce; el JAR no debe instalarse hasta sustituirlo por la estructura de
UI vigente y validar todos los documentos.
