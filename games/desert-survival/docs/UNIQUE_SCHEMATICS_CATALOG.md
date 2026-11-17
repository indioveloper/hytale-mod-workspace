# Esquemas únicos: contrato de diseño

Estado 2026-11-17: **aprobado como diseño, sin implementación**. Fuente:
[plan maestro](DESERT_SURVIVAL_MASTER_PLAN_2026-11-17.md), secciones 2–4.
Este catálogo no debe confundirse con los planos de bases o las recetas
normales desbloqueadas mediante Intel.

| Escalón | Nombre de variante de cutterray | Recurso adicional y rendimiento |
| --- | --- | --- |
| T1.5 | Sim | Pendiente de matriz detallada. |
| T2.5 | Olef | Pendiente de matriz detallada. |
| T3.5 | Callie | Pendiente de matriz detallada. |
| T4.5 | Sandflies | Pendiente de matriz detallada. |
| T5.5 | Tarl | Pendiente de matriz detallada. |
| T6.5 | Kynes | Mejora de rendimiento; cifras pendientes. |

Las variantes son intermedias entre tiers normales. Para evitar inventar
estadísticas, falta incorporar `CUTTERRAY_DUNE_PROGRESION_CANONICA.md` y
`DESERT_SURVIVAL_UNIQUE_SCHEMATICS_AND_CUTTERAYS.md`, citadas por el plan
pero ausentes en este workspace. Hay que revisar además sus fuentes y cualquier
regla antigua sobre reparación: **reparar exige un permiso disponible y no lo
consume**.

Un esquema físico se puede guardar y comerciar. Tenerlo no revela la receta.
Consumirlo descubre la receta y añade un permiso personal. Cada copia adicional
consumida añade otro permiso. Fabricar gasta uno solo tras finalizar con éxito;
reparar exige uno, gasta materiales de reparación y conserva el permiso. Con
cero permisos la ficha sigue descubierta, pero crafting y reparación se
bloquean. El servidor debe guardar `blueprintsConsumed` y `permitsAvailable`
por personaje y receta y efectuar la entrega/consumo de forma atómica.

El catálogo futuro del Códice tendrá una pestaña de Únicos independiente:
silueta anónima antes de consumir el primer esquema; después, nombre, receta,
saldo y requisitos. Quedan abiertos pools de loot, cantidades, materiales
intermedios, polvo especiado, agua como ingrediente y balance. Empezar con
**una** receta placeholder y probar descubrimiento, duplicados, transferencias,
fabricación, reparación, fallo y persistencia antes de ampliar el catálogo.
