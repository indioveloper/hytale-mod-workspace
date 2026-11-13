# ModelVFX de élites: recurso diferido

Los élites no aplican actualmente un `ModelVFX`. Se retiró su uso en la versión
0.5.7 porque el ciclo del efecto podía convertir algunos modelos en una silueta
violeta opaca y ocultar su textura.

Los assets se conservan deliberadamente sin referencias de runtime:

- `assets/Server/Entity/Effects/OrbGenesis/OrbGenesis_Spawner_Elite.json`
- `assets/Server/Entity/ModelVFX/OrbGenesis_Spawner_Elite.json`

El recurso usaba `PostColor #17121f`, opacidad `0.34`, highlight violeta y un
bucle `LoopMirror`. Antes de reactivarlo, probar la mezcla y opacidad con roles
de materiales y siluetas distintos. Solo debe restaurarse una referencia desde
`SpawnerMobSetupSystem` si conserva íntegra la textura base en todos los roles
probados.

La diferenciación actual de un élite es funcional: nombre `Prefijo Nombre`,
partícula de aparición, mensaje de aparición/derrota y Event Title individual
al entrar a 10 bloques.
