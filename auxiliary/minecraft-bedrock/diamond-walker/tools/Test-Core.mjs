import assert from "node:assert/strict";
import { blockUnderFeet, shouldReplace } from "../behavior_pack/scripts/core.js";

assert.deepEqual(blockUnderFeet({ x: 3.9, y: 65, z: -1.2 }), { x: 3, y: 64, z: -2 });
assert.deepEqual(blockUnderFeet({ x: -0.1, y: 64.5, z: 2 }), { x: -1, y: 64, z: 2 });
assert.equal(shouldReplace(undefined), false);
assert.equal(shouldReplace({ isAir: true, typeId: "minecraft:air" }), false);
assert.equal(shouldReplace({ isAir: false, typeId: "minecraft:diamond_block" }), false);
assert.equal(shouldReplace({ isAir: false, typeId: "minecraft:grass_block" }), true);
console.log("OK: posicion bajo los pies y filtros de bloque validados.");
