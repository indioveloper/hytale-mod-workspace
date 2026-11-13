# Mineral Rush — GDD

Estado: concepto inicial  
Plataforma: servidor de Hytale  
Jugadores: 2 o más, individual o por equipos  
Duración objetivo: 10–15 minutos

## Concepto

Minijuego competitivo contrarreloj en el que los jugadores exploran una mina,
extraen minerales, los venden y deciden entre mejorar su eficiencia o comprar
sabotajes. Gana quien termine la partida con mayor valor acumulado.

## Pilares

1. **Progresión rápida:** cada compra cambia de forma perceptible la capacidad
   de minado o movimiento.
2. **Riesgo y recompensa:** las zonas compartidas ofrecen mejores minerales,
   pero exponen al jugador a los rivales.
3. **Decisiones económicas:** las monedas pueden producir más recursos o frenar
   a la competencia.
4. **Partidas variables:** distribución aleatoria de minerales, aparición
   escalonada y eventos evitan una ruta óptima fija.

## Bucle de juego

Explorar → extraer → transportar minerales → vender → comprar mejoras o
sabotajes → volver a la mina. Durante la ronda aumenta gradualmente la calidad
de los minerales disponibles.

## Mapa

- Un punto de aparición y una zona inicial de bajo valor por jugador/equipo.
- Zonas comunes más peligrosas con minerales escasos y valiosos.
- Vendedor central para vender, mejorar y comprar consumibles.
- Diseño ampliable mediante regiones y puntos de generación configurables.

## Economía y progresión

- Cobre: valor base `1`; hierro: valor base `2`.
- Minerales avanzados y custom: mayor valor, menor frecuencia y aparición más
  tardía.
- Mejoras: pico, velocidad minera, velocidad de movimiento, salto y visión de
  minerales a través del terreno.
- Las mejoras son personales durante la ronda y se reinician al terminar.

## Sabotajes

Consumibles de duración corta: parálisis, pérdida de minerales transportados,
controles invertidos, lentitud y fatiga minera. Tendrán coste, aviso visual,
enfriamiento y protección temporal para evitar cadenas frustrantes.

## Puntuación y victoria

El valor vendido se convierte en monedas para gastar. La puntuación representa
el valor total entregado y no disminuye al comprar. Al terminar el tiempo gana
el jugador o equipo con mayor puntuación; los empates se resuelven por minerales
raros vendidos y después por la venta más temprana.

## Alcance del primer prototipo

- Partida individual de 2–4 jugadores.
- Un mapa, tres tipos de mineral y generación aleatoria por zonas.
- Vendedor con venta, dos niveles de pico y una mejora de movimiento.
- Dos sabotajes: lentitud y pérdida de minerales transportados.
- Temporizador, marcador, resultado final y reinicio completo de la arena.

## Riesgos de diseño

- **Efecto bola de nieve:** precios crecientes, rendimientos decrecientes y
  acceso temporal a minerales valiosos para jugadores rezagados.
- **Sabotajes frustrantes:** nunca eliminan mejoras permanentes ni bloquean al
  jugador durante demasiado tiempo.
- **Acaparamiento:** límite de inventario o penalización por transportar mucho
  sin vender.

## Pendiente de decidir

- Duración exacta y número máximo de jugadores.
- Si la victoria usa valor vendido, saldo final o una combinación.
- Combate directo, colisión entre jugadores y pérdida al morir.
- Catálogo definitivo de minerales, probabilidades, precios y mejoras.
- Formato de equipos, matchmaking y variantes de mapa.
