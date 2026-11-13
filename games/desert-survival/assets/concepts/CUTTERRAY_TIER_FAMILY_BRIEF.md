# Familia de cutterrays y recursos — borrador v1

## Referencias visuales

- `cutterray-tier-family-v1.png`: seis herramientas y materiales.
- `cutterray-beam-tiers-v1.png`: núcleo, halo, partículas e impacto por tier.
- `industrial-resource-nodes-v1.png`: hierro, aluminio, carbono y acero.
- `rare-resource-nodes-v1.png`: jasmio, titanio y estravidio.

## Familia de herramientas

Todas las variantes conservan el tamaño aproximado de `1,20 m`, dos puntos de
agarre y los grupos estructurales `body`, `emitter`, `battery`, `rear_grip`,
`front_grip`, `vents` y `trigger`. Esto permitirá compartir animaciones y lógica
de apuntado.

| Tier | Nombre provisional | Recurso nuevo | Cambio principal |
| ---: | --- | --- | --- |
| 1 | Recuperado | Scrap y cobre | bobina expuesta y piezas desparejadas |
| 2 | Reforzado | Hierro | jaula de hierro y batería protegida |
| 3 | Industrial | Aluminio | cerámica arena y emisor cian estable |
| 4 | Térmico | Carbono | disipadores, doble refrigeración y halo caliente |
| 5 | Aislado | Jasmio | blindaje sellado y emisión verde controlada |
| 6 | Resonante | Titanio y estravidio | doble anillo y haz violeta-cian resonante |

Los tiers superiores pueden cortar todos los recursos anteriores. Las
variaciones visuales no deben cambiar la posición de manos, origen del rayo ni
volumen general del modelo.

## Recursos

Cada nodo natural mide alrededor de `2,50 x 1,70 m`, tiene seis lóbulos
extraíbles y conserva un resto rocoso agotado hasta su regeneración.

- **Hierro:** hematita roja, magnetita oscura y polvo rojizo.
- **Aluminio:** placas claras dentro de roca ocre y polvo blanco.
- **Carbono:** estratos negros, basalto y polvo fino oscuro.
- **Jasmio:** cristal irradiado verde con inclusiones amarillo ácido.
- **Titanio:** costillas metálicas plateadas dentro de roca volcánica.
- **Estravidio:** haces fibrosos magenta y cian dentro de matriz violeta.

El acero no tiene nodo. Se obtiene en refinería a partir de hierro, carbono y
agua, y se representa mediante lingotes, placas y componentes.

## Contrato inicial de VFX

Todos los rayos comparten cuatro sistemas: núcleo, halo, partículas de trayecto
e impacto. El tier determina color, estabilidad y densidad, pero no el alcance.

| Tier | Núcleo | Halo | Impacto |
| ---: | --- | --- | --- |
| 1 | cian irregular | casi ausente | pequeño e inestable |
| 2 | azul pálido | cian estrecho | blanco con polvo |
| 3 | azul-blanco segmentado | cian suave | cobre fundido |
| 4 | blanco-cian | naranja térmico | gotas y distorsión de calor |
| 5 | blanco-esmeralda | cian sellado | fractura cristalina verde |
| 6 | blanco-violeta doble | interferencia cian/magenta | fibras resonantes |

## Próximo paso de producción

1. Modelar T3 y fijar pivotes, agarres y escala en Blockbench.
2. Derivar T1/T2/T4/T5/T6 sobre el mismo esqueleto.
3. Producir primero el nodo de cobre modular y validar el ocultado de lóbulos.
4. Reutilizar esa estructura para los demás materiales.
5. Implementar un único rayo T3 antes de crear las variantes de partículas.
