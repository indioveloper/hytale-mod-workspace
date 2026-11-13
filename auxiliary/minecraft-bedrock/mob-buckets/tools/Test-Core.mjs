import assert from "node:assert/strict";
import { captureProblem, isFilled, mobLabel, MOB_PROPERTY } from "../behavior_pack/scripts/core.js";

const entity = (typeId, components = {}, nameTag = "") => ({
  typeId, nameTag, getComponent: (id) => components[id]
});

assert.equal(mobLabel("minecraft:snow_golem"), "Snow Golem");
assert.equal(captureProblem(entity("minecraft:cow", { "minecraft:health": {} })), undefined);
assert.match(captureProblem(entity("minecraft:wither", { "minecraft:health": {} })), /no se puede/);
assert.match(captureProblem(entity("minecraft:wolf", {
  "minecraft:health": {}, "minecraft:tameable": { isTamed: true }
})), /domesticadas/);
assert.match(captureProblem(entity("minecraft:cow", { "minecraft:health": {} }, "Lola")), /nombre/);
assert.equal(isFilled({ getDynamicProperty: (key) => key === MOB_PROPERTY ? "minecraft:cow" : undefined }), true);
assert.equal(isFilled({ getDynamicProperty: () => undefined }), false);
console.log("OK: reglas de captura y estado del cubo validados.");
