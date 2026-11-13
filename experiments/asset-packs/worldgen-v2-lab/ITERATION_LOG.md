# Registro de iteraciones

## 0.1.0 - baseline de humo

- Mundo: `Raynor_Lab_Rolling_Hills`.
- Un unico bioma de colinas generado con `SimplexNoise2D` y una caida vertical
  basada en el framework `Base`.
- Superficie de hierba, subsuelo de tierra y piedra como material de fondo.
- Sin props, agua ni cuevas: esta revision aisla terreno, materiales, spawn y
  carga de la instancia antes de introducir nodos mas caros.
- Instancia creativa efimera: no guarda jugadores ni chunks.
- Estado: estructura y JSON validados; el Biome tambien ha sido aceptado por
  los codecs reales del `HytaleServer.jar` de `0.7.0-pre.1`.
- Artefacto: `Raynor_WorldGen_V2_Lab-0_1_0-u7pre1.zip`.
- Pendiente: smoke test dentro del cliente para confirmar la forma visual, el
  material por profundidad, el spawn y la apertura desde `/instances`.

Cada vuelta de prompt -> pack -> prueba debe anadir una entrada con:

1. intencion;
2. nodos o assets cambiados;
3. seed usada;
4. resultado observado;
5. siguiente hipotesis.
# Iteracion 2 - Jardines del Cielo sin prefabs

- Fecha: 2026-09-05.
- Instancia: `Raynor_Sky_Gardens`.
- Objetivo: islas flotantes de varios tamanos y alturas; solo terreno.
- Distribucion: reticula 3D 210 x 72 x 210 con jitter 25 y seed fija.
- Formas: tres escalas de elipsoide, seleccionadas por ruido blanco por celda.
- Acabado: ruido Simplex 3D suave sobre la silueta, hierba/tierra/piedra.
- Spawn: isla garantizada centrada en Y=160.
- Props y prefabs: ninguno.
- Limite conocido: el Biome usa `HytaleGenerator`, que en pre.1 sigue siendo
  columnar (Y 0..319). La altura infinita existe en `IWorldGen.Cubic`, pero aun
  no esta conectada a este adaptador V2.
- Feedback pendiente: tamanos, cantidad de vacio, facilidad de salto/vuelo y
  si las cuatro bandas verticales se perciben demasiado regulares.

# Iteracion 3 - Distribucion organica y primera decoracion

- Fecha: 2026-09-05.
- Version: `0.3.0` para `0.7.0-pre.1`.
- Feedback de entrada: cuadrilla visible, tamanos parecidos, alturas casi
  constantes, spawn artificial y superficies sin detalle.
- Distribucion: tres rejillas 3D independientes (grande, mediana y pequena),
  con escalas no conmensurables, offsets distintos, descarte probabilistico y
  jitter cercano a media celda. La superposicion ocasional es intencional.
- Formas: seis escalas efectivas, techos recortados de forma desigual y dos
  frecuencias de erosion 3D.
- Spawn: ventana acotada de los mismos PositionProviders de terreno. No se crea
  masa adicional; el proveedor individual elige candidato por UUID y
  FitToHeightMap resuelve la altura de superficie.
- Materiales: parches de tres hierbas, subsuelo seco/lush/humedo y nucleo con
  piedra musgosa.
- Agua: las islas grandes seleccionadas por la variante alta tallan una cuenca
  y el Empty provider rellena un volumen menor con `Water_Source` usando la
  misma seed de celda.
- Vegetacion: bloques Manual de hierba, flores y arbustos localizados sobre las
  tres superficies de hierba. No se han introducido prefabs.
- Estado: JSON, estructura y codecs de Biome validados; pendiente smoke test
  visual en una instancia nueva.
