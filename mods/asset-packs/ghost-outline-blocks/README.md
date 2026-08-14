# Ghost Outline Blocks

Asset pack with non-colliding transparent outline blocks for locked platform previews.

The included block is `Ghost_Outline_Rock_Stone`. It renders as a cube outline using the stone texture, but its block material is `Empty`, so players and physics pass through it.

To create another associated-block variant:

1. Generate a texture:

   ```powershell
   python .\tools\make_outline_texture.py <source_texture.png> .\Common\BlockTextures\OrbGenesis\GhostOutline\Ghost_Outline_<BlockId>.png --border 4 --alpha 190
   ```

2. Copy `Server/Item/Items/OrbGenesis/GhostOutline/Ghost_Outline_Rock_Stone.json`.
3. Rename the item id, translations, icon path and texture path.
4. Keep `"Material": "Empty"`, `"RequiresAlphaBlending": true`, `"Opacity": "Transparent"` and `"Group": "@Tech"`.
