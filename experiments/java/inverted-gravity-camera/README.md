# Inverted Gravity Camera experiment

Archived from More Triggers after the `1.8.0`-`1.8.8` test series on Hytale
pre-release `0.6.8`. This code is reference material, is not part of the active
More Triggers build and is not ready to distribute as a standalone plugin.

## Intended result

Pair Movement Commands `movement invertedgravity true` with a 180-degree player
camera roll, normal first/third-person locomotion animations, a head-height
camera anchor and camera-relative controls while walking on ceilings.

## What worked

- The custom camera roll and its lerped transition.
- A vanilla `Daggers/Backflip` transition in `AnimationSlot.ServerAction`,
  stopped after 1.1 seconds so locomotion animations resumed.
- First/third-person switching through `/gravityview`; the vanilla perspective
  key does not switch `ClientCameraView.Custom` on 0.6.8.
- A configurable inverted eye offset; `1.6` blocks placed the view near the
  inverted head.
- Mouse-look compensation with `lookMultiplier = (-1, -1)`.

## Unresolved control problem

A 180-degree roll preserves camera forward but reverses visible left/right.
Hytale 0.6.8 calculates character movement without a usable local-axis lateral
reflection:

- no multiplier: W/S is correct, A/D is reversed;
- `movementMultiplier = (1, 1, -1)` fixes the initial east-facing test but the
  multiplier is applied in world axes, so WASD becomes absolute after turning;
- `MovementForceRotationType.Custom` made W/S and A/D swap roles because the
  custom basis was not following the live camera;
- `MovementForceRotationType.CameraRotation` preserved W/S but still ignored
  roll for lateral movement;
- reflecting queued `PlayerInput` movement on the server was attempted in
  1.8.8, but it remained unsuitable as the desired client-relative control
  solution and risks fighting client prediction.

The exact final reproduction started facing east: W moved +X correctly, A
should move -Z and D +Z. A fixed Z reflection solved that orientation, but after
turning the mouse W no longer followed the camera. Resume only if a client-side
movement basis/matrix or a supported roll-aware input API becomes available.

## Archived files

- `SetPlayerGravityViewEffect.java`
- `GravityViewController.java`
- `GravityViewCommand.java`
- `GravityMovementCompensationSystem.java`

They retain their old package names and references to More Triggers, so they
must be refactored before they can compile independently.
