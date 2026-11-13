# Survival vitals prototype

Version `0.2.6`: first playable heat, sun exposure, hydration and stillsuit
recycling loop for Hytale 0.7.x.

## Hydration

- Every player has `1500 ml` of hydration.
- Above `1000 ml`: health regenerates at `2 HP/s` and maximum health is `+20%`.
- From `500` through `1000 ml`: health regenerates at `1 HP/s`.
- Below `500 ml`: health regeneration stops.
- At `0 ml`: effective maximum health is capped at half without rescaling the
  native health bar, the player receives `2` environmental damage every `10 s`,
  and positive stamina regeneration is reduced by `50%`. A HUD marker shows the
  unavailable half of the vanilla health bar. The cap is enforced every server
  tick after native regeneration, so healing cannot raise health above it.
- `Water_Flask_Prototype` restores up to `500 ml` and becomes
  `Water_Flask_Empty_Prototype`.

## Heat and sun exposure

The system samples global sky light at the player's head once per second.
Daylight with sky light `>=14` is direct sun; roofs, caves and sufficiently
dense foliage are shade.

The provisional environment mapping is:

- Zone 1: heat `1`.
- Zone 2: heat `2`.
- Zone 3: heat `0`.
- Zone 4: heat `3`.
- Unknown environments fall back to Zone 1.

Water loss in `ml/s` is:

```text
max(0, biomeHeat * (inSun ? 1.25 : 1.0)
       + (burning ? 6 : 0)
       - heatResistance / 100)
```

Direct sun adds `5%` exposure per second. Shade removes `10%` per second.
Burning activates at `100%` and remains active until exposure reaches `90%`.

## T1 stillsuit

The prototype reuses the vanilla crude diving set presentation:

- `Armor_Stillsuit_Prototype` (chest).
- `Armor_Stillsuit_T1_Head_Prototype` (hood).
- `Armor_Stillsuit_T1_Hands_Prototype` (gloves).
- `Armor_Stillsuit_T1_Legs_Prototype` (boots/leg slot).

Each item adds `5` heat-resistance points, for `20` with the complete set.
Only the chest supplies a `500 ml` recovery pocket. T1 stores one recovered ml
for every three ml of hydration lost. It does not reduce the original loss.

`/drinkstillsuit` transfers at most `150 ml` per invocation from the equipped
chest pocket. Update 7.7 exposes no server-side custom keyboard/keybind event,
so this command is the reusable hotkey action until a client integration can
bind a key to it. There is no separate drinking-tube item.

## Smoke-test assets

```text
/give Water_Flask_Prototype 1
/give Armor_Stillsuit_Prototype 1
/give Armor_Stillsuit_T1_Head_Prototype 1
/give Armor_Stillsuit_T1_Hands_Prototype 1
/give Armor_Stillsuit_T1_Legs_Prototype 1
/drinkstillsuit
```

Build from the workspace root:

```powershell
.\scripts\Build-JavaMod.ps1 `
  -ProjectPath .\games\desert-survival\prototypes\survival-vitals `
  -SourceRoot src `
  -PackageRoot . `
  -VanillaCustomUiAssetsZip C:\path\to\Assets.zip `
  -AdditionalCustomUiRoots .\games\desert-survival\prototypes\research-tree\Common\UI\Custom `
  -ForceCustomUiTransfer Hud/ReturnToHubButton.ui `
  -ArtifactName Raynor_Desert_Survival_Vitals_Prototype-0_2_6.jar
```

Then validate the distributable before installing it:

```powershell
.\games\desert-survival\prototypes\survival-vitals\tools\Test-Package.ps1 `
  -ArchivePath .\games\desert-survival\prototypes\survival-vitals\.build\dist\Raynor_Desert_Survival_Vitals_Prototype-0_2_6.jar `
  -AssetsZip C:\path\to\Assets.zip
```

The vanilla `Common/UI/Custom` tree is hydrated only under `.build`. Update 7.7
loads this pack after the Research prototype; shipping only
`HUD/SurvivalVitals.ui` would hide vanilla documents such as
`Hud/ReturnToHubButton.ui` and disconnect the client during world join. Since
Vitals is also the last active pack in this prototype suite, its build overlays
the Research Custom UI root after hydration so `/research` remains available.
Update 7.7 also omits byte-identical vanilla documents from `RequiredAssets`,
even when the client has not registered them. The build therefore adds only a
whitespace line to `Hud/ReturnToHubButton.ui`, forcing that compatibility
document to be transported without changing its parsed markup.

## Prototype limits

- The final map should replace Environment-ID inference with explicit biome
  configuration.
- Heat resistance is currently mapped by the four prototype item IDs; future
  tiers need data-driven item values.
- Empty flasks cannot yet be refilled from water sources or base machinery.
- The stillsuit hotkey needs client-side keybind support; the server action is
  currently exposed as `/drinkstillsuit`.
- Storms, movement, wet status and custom stillsuit models are not included.
