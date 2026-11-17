# Red de energía de base: propuesta

Estado 2026-11-17: **diseño pendiente**. Build Selector 0.6.2 solo prototipa
generadores de 100 unidades con fuel cells y alimentación local del
purificador. No hay red, prioridades, acumulación ni pentaescudos funcionales.
Ver [estado](../STATUS.md) y [plan maestro](DESERT_SURVIVAL_MASTER_PLAN_2026-11-17.md).

La próxima especificación debe definir un grafo por claim, productores y
consumidores, suma de potencia disponible y demanda, prioridades de corte y
comportamiento al agotarse combustible. La producción no utilizada se disipa
según la regla vigente; no se almacena por defecto. El HUD debe distinguir
potencia instantánea de autonomía de combustible, y la consola debe persistir
estado y propiedad sin dar acceso a cofres ajenos.

Pentaescudos y resistencia de bases a tormentas están en el backlog, sin
valores ni permisos cerrados. Antes de implementarlos, decidir cobertura,
consumo, prioridad frente a maquinaria, efectos del apagón, reconexión y
persistencia al descargar chunks. Probar dos generadores y dos consumidores
en un mismo claim, combustible agotado y reinicio del servidor.
