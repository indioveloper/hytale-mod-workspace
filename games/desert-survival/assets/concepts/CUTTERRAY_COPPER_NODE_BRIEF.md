# Cutterray T3 y nodo de cobre — borrador v1

Referencia visual: `cutterray-copper-node-v1.png`.

Este documento fija la dirección artística y técnica del primer prototipo. El
nombre del proyecto y los IDs definitivos siguen pendientes.

## Cutterray T3

- Herramienta de dos manos de aproximadamente `1,20 m` de largo.
- Silueta formada por cuboides grandes y legibles para facilitar el modelado en
  Blockbench y el estilo visual de Hytale.
- Cuerpo principal de cerámica color arena, bastidor de grafito, refuerzos de
  cobre y emisor cian.
- Grupos previstos: `body`, `emitter`, `battery`, `rear_grip`, `front_grip`,
  `vents` y `trigger`.
- El emisor y las tres ranuras laterales usan material fullbright. El resto no
  necesita transparencia.
- La batería y el emisor deben quedar separados para reutilizar el cuerpo en
  futuras variantes de nivel.
- Esta silueta se asigna al `Tier 3`, capaz de cortar aluminio según la
  progresión propia del mod. Las variantes T1/T2 serán más improvisadas y las
  T4/T5/T6 añadirán refrigeración, aislamiento y emisores más complejos.

## Efecto del rayo

El rayo se compone de cuatro capas independientes:

1. núcleo azul-blanco, fino y continuo;
2. halo cian de menor opacidad;
3. segmentos luminosos, polvo y chispas distribuidos a lo largo del trayecto;
4. impacto naranja-blanco con chispas de cobre y gotas fundidas.

Para el prototipo, el núcleo puede generarse como una línea de partículas entre
el emisor y el punto de impacto. El halo usa la misma línea con menos densidad y
mayor tamaño. El impacto es un sistema separado anclado al punto alcanzado.

El alcance inicial propuesto es `12 m`. El efecto debe actualizar su extremo al
mover la cámara sin crear una nueva entidad de red por partícula.

## Nodo de cobre

- Prop grande de aproximadamente `2,50 m` de ancho y `1,70 m` de alto.
- Roca base de arenisca y basalto con cobre nativo y zonas de oxidación verde.
- Seis lóbulos extraíbles con fracturas visibles. Cada lóbulo tiene hitbox y
  progreso propios.
- Estados visuales: `intact`, `partial` y `depleted`.
- El estado parcial no será una simple textura: ocultará los lóbulos agotados y
  revelará roca interior.
- El resto rocoso permanece en el mundo hasta que el nodo se regenere, evitando
  que el recurso desaparezca bruscamente.

## Primer alcance de producción

- Un `.bbmodel` fuente y un `.blockymodel` exportado para el cutterray.
- Una textura de `64x64` o `128x128` con los cinco materiales de la lámina.
- Tres modelos del nodo o un modelo modular con seis piezas conmutables.
- Partículas para núcleo, halo, trayecto e impacto.
- Iconos provisionales y un prefab de escala para comprobar el conjunto junto a
  un jugador.

## Pendientes antes de modelar

- Confirmar si la herramienta debe verse más industrial o más artesanal.
- Decidir si el corte consume batería, genera calor o utiliza ambos recursos.
- Confirmar si cada lóbulo entrega mineral al instante o genera fragmentos que
  deben recogerse.
