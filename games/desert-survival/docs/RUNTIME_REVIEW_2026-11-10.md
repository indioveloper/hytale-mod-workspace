# Runtime review - 2026-11-10

## Local installation

- Stable: Hytale `0.7.7`, revision
  `12fc465604eecca402bbd479aafb423e0a1f0dc9`.
- Pre-release: Hytale `0.7.0-pre.5.1`, revision
  `4371ccdc8822a826b4bbb8a84e180c521d716fd0`.

The installed pre-release is older than the installed stable build. It should
not be used as the reference runtime for this prototype.

## Available upstream

- Stable was updated locally to `0.7.7` (build 39) on 2026-11-10.
- Update 8 pre-release reached Part 3 on 2026-11-05. It includes a preview of
  the new asset editor, model conditional attachments and additional asset/API
  migrations.

Official sources:

- https://hytale.com/news/2026/10/2026-10-13-hotfixes-update-7/
- https://hytale.com/news/2026/10/pre-release-patch-notes-update-8

## Prototype target

The first cutterray targets installed stable `0.7.7` and declares
`>=0.7.0 <0.8.0`. It uses the current `.blockymodel` format and the vanilla
`Drill` player animation ID, but no copied Hytale model or texture data.

The playable prototype compiles and passes full asset validation against the
installed `0.7.7` `HytaleServer.jar`. Do not move to Update 8-only fields until
the team intentionally chooses pre-release as the development target.
