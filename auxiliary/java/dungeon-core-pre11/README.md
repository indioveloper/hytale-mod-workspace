# DungeonCore pre-release port

Compatibility port of DungeonCore 1.3.7 for Hytale `0.6.0-pre.11`
(`00cf2e930ab404ea983cb709c3e0a6deb45fda7a`). The original mod is distributed
under the MIT license by Limesta, MKOutlaw and LichtMarv.

This is an auxiliary third-party port, not an OrbGenesis mod. Internal plugin,
component, interaction and asset IDs are intentionally unchanged so existing
DungeonCore worlds remain compatible. The port changes the removed Hytale
vector classes to JOML, uses `Rotation3f`, migrates block component placement to
the section-based API, and repairs generic types lost while reconstructing the
MIT-licensed source from DungeonCore 1.3.7.

## Build

Requirements:

- Java 25;
- the `HytaleServer.jar` from `0.6.0-pre.11`;
- HyUI 0.9.8, which remains a runtime dependency.

Run:

```powershell
.\tools\Build.ps1
```

The resulting JAR is written to `.build/dist/DungeonCore-1.3.9-pre11.jar`.
Install HyUI and DungeonCore only once, either globally or in the save's `mods`
directory. Do not place the original DungeonCore 1.3.7 JAR alongside this port,
because both use `com.lol:DungeonCore`.

Version `1.3.9-pre11` also validates both ends of signal and teleporter links.
Clicking a block that cannot receive the selected link is ignored instead of
throwing from the interaction system and disconnecting the player.
