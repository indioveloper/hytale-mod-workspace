# More Triggers

Trigger Volume extensions for Hytale pre-release 0.6.x.

## Effects

- `PasteRandomPrefab`: pastes one random prefab at the volume origin, with an optional XYZ offset. Prefer `PrefabList`; the legacy manual prefab and weight fields remain compatible with existing volumes.
- `RandomTagSelection`: chooses a random value and writes it to a Trigger Volume tag.
- `RemoveEventTitle`: hides the event title for the triggering player.

## Build

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\mods\java\more-triggers `
  -PackageRoot src `
  -ArtifactName More_Triggers-1_1_0.jar
```

The build must use the `HytaleServer.jar` from the same pre-release being tested.
