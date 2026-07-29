# Avatar Sword Runner Asset Source

This folder contains the vanilla NPC asset candidate for the first Nexus Siege
NPC.

## Files

- `Server/NPC/Roles/Nexus_Avatar_Sword_Runner.json`
- `Server/Languages/en-US/server.lang`
- `Server/Languages/es-ES/server.lang`
- `manifest.json`

## Runtime Contract

The role expects a stored position slot named `NexusTarget`.

The NPC will only run while this slot exists and is within `128` blocks. The
map-side setup, trigger-volume setup, spawn flow, or future plugin must write
the intended destination into that slot before or immediately after spawn.

For now, the role itself remains vanilla and does not include plugin code.

## Asset IDs To Confirm

- `randommodel`
- `vanilla_sword`
- `sword_swing`
