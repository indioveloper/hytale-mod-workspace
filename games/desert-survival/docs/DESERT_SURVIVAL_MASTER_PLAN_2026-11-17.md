# Desert Survival — Plan maestro de diseño y handoff a Codex

**Fecha:** 2026-11-17
**Estado:** Documento de planificación incorporado al repositorio. No implica implementación.
**Fuente de estado técnico:** `games/desert-survival/STATUS.md` del PDF `Desert_Survival_Documentacion_2026-11-17.pdf` (94 páginas). Revisar el estado real del repositorio antes de ejecutar tareas; los JAR instalados, fuentes y pruebas de juego no coinciden necesariamente.

## 0. Reglas de trabajo para Codex

1. No confundir **idea**, **diseño aprobado**, **prototipo en fuentes**, **artefacto instalado** y **prueba confirmada en juego**.
2. No sobrescribir cambios locales ni sustituir JARs sin comprobar estado, dependencias y autorización. Runtime de referencia en el PDF: Hytale 0.7.7.
3. Las reglas más recientes de este documento prevalecen sobre borradores históricos, especialmente en permisos de reparación.
4. Separar especificación, contratos persistentes, UI, assets, implementación y smoke tests.
5. Los objetos únicos no necesitan assets finales todavía.

## 1. Sistemas ya planteados o prototipados

| Área | Situación y límites |
|---|---|
| Supervivencia | Calor, exposición solar, hidratación, recipientes y destiltraje prototipados; balance pendiente. Vitals con desfase entre fuentes e instalado en el estado documentado. |
| Recursos | Nodos grandes y materiales; prototipo de extracción de cobre/hierro y fuel cells visibles, extracción de estas últimas pendiente de confirmar. |
| Cutterrays | Prototipo visual/funcional T3 y sistema de extracción; **la progresión canónica adoptada ahora es T0 improvisado + T1–T6**, con rendimiento por recurso y consumo energético propio. |
| Agua desde sangre | Extracción de cadáveres y purificación de sangre hacia agua parcialmente prototipadas; validar cadena completa. |
| Enemigos | Bases, NPCs hostiles, cadáveres, saqueo y extracción de sangre; algunos comportamientos probados, otros pendientes. |
| Investigación | Intel, 5 ramas, 48 nodos en bandas 0–2, rueda radial, desbloqueos persistentes; integración general con recetas incompleta. |
| Únicos | Blueprints y permisos definidos a nivel de diseño; catálogo y fabricación/reparación genéricos aún por implementar. |
| WorldGen | Cuatro zonas/biomas desérticos y distribución primitiva de nodos, fuel cells, bases; frontera de mapa todavía no plenamente jugable según estado documental. |
| Bases | Claim inicial 40×40×100, herramienta de construcción, piezas estructurales y maquinaria, selector y consola. |
| Energía | Generador alimentado por fuel cells y consumidores básicos en prototipo; no hay red de energía completa, almacenamiento ni pentaescudos funcionales. |

## 2. Decisiones aprobadas: cutterrays

La progresión del mod **no debe diferir deliberadamente de Dune: Awakening** en orden de acceso a recursos. Herramientas normales: improvisado T0 (cobre, granito, metal recuperado); T1 hierro; T2 carbono y eritrita; T3 basalto y aluminio; T4 jasmio; T5 titanio y estravidio; T6 mismos recursos, más rendimiento. Mantener matriz exacta de rendimientos por grupo de recurso y consumo/tensión de las capturas aportadas; ver `CUTTERRAY_DUNE_PROGRESION_CANONICA.md`.

Cada T1–T6 tiene variante única **intermedia** T1.5–T6.5: Sim, Olef, Callie, Sandflies, Tarl y Kynes. Normalmente mejora rendimientos y accede al recurso del siguiente escalón; T6.5 solo mejora rendimientos. Ver matriz y nombres en `DESERT_SURVIVAL_UNIQUE_SCHEMATICS_AND_CUTTERAYS.md`, pero **ignorar su regla antigua de que reparar consume un permiso**: está derogada. Los porcentajes no son probabilidades y no deben convertirse en multiplicadores universales. La duración de extracción de cinco segundos es una regla provisional de Hytale, no un dato confirmado de Dune.

## 3. Decisiones aprobadas: blueprints únicos y permisos imperiales

- BP físico obtenido como loot: inventariable, almacenable, regalable y vendible. **Tenerlo físicamente no enseña la receta**.
- **Consumir 1 BP = incorporar la receta al catálogo conocido + 1 permiso imperial personal, intransferible**. Copias consumidas adicionales añaden +1 permiso.
- **Fabricar** un objeto único requiere al menos un permiso del personaje que fabrica, además de materiales y estación; **consume exactamente 1 permiso** al completar la fabricación con éxito.
- **Reparar** un objeto único requiere que el personaje reparador tenga **al menos 1 permiso disponible de esa receta**; **NO consume el permiso**, porque no se fabrica una nueva copia. Consume únicamente los materiales/otros costes de reparación establecidos.
- Con cero permisos, la receta puede seguir figurando como descubierta históricamente, pero fabricar y reparar quedan bloqueados. Transferir el objeto fabricado no transfiere permisos.
- Estado persistente por personaje y receta: `blueprintsConsumed` (histórico, >0 = descubierto), `permitsAvailable` (saldo); autoridad y operaciones atómicas en servidor.
- Receta única: mayoritariamente metal y componentes del equivalente normal, más algún componente avanzado y polvo especiado del metal correspondiente. No exigir que *todos* los materiales sean de bioma superior.
- El agua permanece **provisionalmente** como ingrediente; estudiar si representa refrigeración o proceso industrial. Podría eliminarse o sustituirse más adelante; no afirmar justificación canónica no verificada.
- Sistema transversal para herramientas, armas, armaduras, garments y equipamiento único. No producir todavía assets finales.

## 4. Decisiones aprobadas: Códice y tecnología

**Siete pestañas iniciales:** global radial + Agua + Combate + Construcción + Exploración + Vehículos + Únicos. La radial continúa como vista global; cada rama dispone de vista dedicada escalable, con nodos agrupados/ordenados por tier 0–6, dependencias y panel de detalle. No comprimir todos los tiers en una sola rueda.

Los nodos de investigación normales muestran identidad, descripción y dependencias antes de desbloquearse y usan Intel. El catálogo de Únicos es anexo sin dependencias entre entradas. Permite ordenar/filtrar por **tipo**, **tier** y **estado**. Cada receta única ocupa una casilla; hasta consumir su BP se ve solo icono/silueta negra genérica de objeto desconocido, sin nombre, materiales ni estadísticas. Tras consumir BP se revela su ficha, saldo de permisos y requisitos. Al agotarse permisos, sigue visible como descubierta. La UI nunca sustituye la validación en servidor.

## 5. Backlog pendiente por subsistema

### 5.1 Materiales, economía y supervivencia
- Definir **materiales intermedios** con tier, bioma de origen, proceso de obtención, refinado y recetas; evitar saltos de progresión.
- Balance mínimo de hidratación, calor, vida, daño ambiental y recuperación; probar bucle de supervivencia.
- Revisar el encadenamiento nodos → cutterray → materiales → refinería → crafteo.
- Validar recetas y estaciones, incluyendo componentes y polvos especiados.

### 5.2 Dungeons, loot y llaves
- Comprobar spawn correcto de llaves en bases/dungeons T2–T4, llaves de espadachines, persistencia, consumo y acceso; probar en chunks nuevos y antiguos.
- Integrar BP únicos como loot con pools por dificultad/tier, sin bloquear injustamente progresión obligatoria.
- Ampliar dungeons, cofres y pruebas de botín.

### 5.3 Bases, maquinaria y energía
- Sistema de balance de energía por base: suma de producción de todos los generadores, suma de demanda de todas las máquinas y escudos, prioridades, apagones y persistencia.
- Añadir maquinaria faltante y sus procesos, entradas/salidas, almacenamiento, recetas, consumo y controles.
- Implementar **pentaescudos** y su dependencia de energía; mantener regla propuesta de que bases energizadas resisten tormentas y sin energía sus bloques sufren daño, pendiente de diseño fino.
- Mantener separación entre piezas de construcción y maquinaria en el selector.

### 5.4 Personaje y combate
- Habilidades, experiencia, niveles y progresión de personaje.
- Energía personal de Power Packs: capacidades, consumo, recarga y compatibilidad.
- Escudos personales, hoja lenta/rápida, parry, reglas de penetración y resistencia de proyectiles con escudo activado; integrar con armas y balance.

### 5.5 Especia
- Parches de especia, compactadores, recolección y procesamiento; alimentos y bebidas de especia.
- Integrar riesgos y recompensas del desierto profundo.

### 5.6 Facciones, misiones, lore y viaje
- Facciones Harkonnen y Arrakeen, bases y reputación/reglas de hostilidad (detalles pendientes).
- Puestos comerciales, vendedores, inventarios y economía.
- Misiones principales, secundarias y de facción; ruinas con lore y **misiones obligatorias para progresar de bioma** (ej.: desbloqueo del siguiente cutterray, equivalente funcional a AQL).
- Lore original adaptado a Hytale; ciudadelas y sistema de viaje entre ellas.

### 5.7 WorldGen y desierto profundo
- Evolucionar de límite cerrado a **anillos exteriores** alrededor del mapa principal:
  1. Primer anillo: dungeons, sin especia.
  2. Segundo: parches medianos de especia y cuevas.
  3. Tercero: parches grandes de especia.
  4. Cuarto: desierto profundo procedural infinito.
- Diseñar transiciones, densidades, generación persistente, peligros y acceso. Esto **sustituye como objetivo futuro** el límite cerrado actual; no asumir que ya está implementado.
- Investigar evento de gusano simplificado: acumulación de vibración por acciones, umbral de peligro, evento de muerte inevitable una vez disparado y posible cinemática top-down. **Concepto exploratorio, no decisión final**; el modelo visual completo del gusano queda fuera de alcance por ahora. Considerar aviso, contrajuego previo, accesibilidad y experiencia multijugador antes de fijarlo.

## 6. Secuencia de implementación propuesta (no aprobada como calendario)

**Fase A — estabilización:** smoke tests de Research/Build Selector/WorldGen/Enemy Base/Vitals, llaves, fuel cells y flujo de agua; correcciones y métricas de supervivencia.
**Fase B — progresión vertical:** materiales intermedios, cutterrays T0–T6, refinerías, recetas y desbloqueos; un recorrido jugable completo.
**Fase C — Códice y únicos:** vistas de ramas y catálogo, BP físico, permisos, crafting y reparación; un único item placeholder para test integral.
**Fase D — bases/combate:** balance energético, maquinaria, pentaescudos, Power Packs, habilidades y combate.
**Fase E — mundo narrativo:** facciones, comercio, ruinas, misiones y ciudadelas.
**Fase F — desierto profundo:** anillos, especia, cuevas, eventos y prototipo de vibración/gusano.

Cada fase requiere criterios de aceptación y smoke tests antes de ampliar alcance. La secuencia puede cambiar según dependencias reales y prioridad del usuario.

## 7. Archivos fuente que Codex debe reconciliar

Conciliación documental inicial completada el 2026-11-17; la
[tabla de conciliación](MASTER_PLAN_RECONCILIATION_2026-11-17.md) contiene las
decisiones posteriores, incluido el mapeo estable `CN-0.2`/`CN-1.2`/`CN-2.2`
a T0/T1/T2. Esta sección conserva la lista de fuentes revisadas.

- `STATUS.md` y `DOCS_INDEX.md` (estado e índice)
- `docs/RESOURCE_PROGRESSION_RESEARCH.md`
- `docs/RESOURCE_NODE_ARCHITECTURE.md`
- `docs/RESEARCH_TREE_MVP.md`
- `docs/RESEARCH_UNLOCKS_AND_ITEMS.md`
- `docs/LOOT_AND_BLUEPRINTS_DRAFT.md`
- `docs/CRAFTING_MATERIALS_ZONES_AND_BUILDING.md`
- `docs/WORLDGEN_DESERT_PLAN.md`
- README de prototipos cutterray, research-tree, build-selector, worldgen y enemy-base.

**Propuesta de nuevos archivos:** `docs/UNIQUE_SCHEMATICS_CATALOG.md`, `docs/BASE_POWER_GRID.md`, `docs/DEEP_DESERT_WORLDGEN.md`, `docs/CHARACTER_COMBAT_AND_POWER.md`, `docs/PROGRESSION_QUESTS_AND_LORE.md`.

## 8. Preguntas abiertas (no asumir resueltas)

- Catálogo exacto de materiales intermedios y recetas por tier.
- Valores de balance y relación energía/tensión/duración de cutterrays.
- Catálogo de máquinas y prioridades de corte eléctrico.
- Árbol de habilidades, clases o especializaciones y XP.
- Economía, reputación, fuentes de BP y estructura de misiones.
- Persistencia/rendimiento de anillos infinitos y transición con mapas guardados.
- Diseño de vibración, contramedidas y cinemática del gusano.
- Justificación y permanencia del agua como consumible de crafting.

## 9. Nota sobre documentos anteriores

`DESERT_SURVIVAL_UNIQUE_SCHEMATICS_AND_CUTTERAYS.md` contiene una **regla obsoleta**: afirma que reparar consume permiso. **No aplicarla**. La regla válida está en la sección 3 de este plan: reparación requiere permiso disponible, pero NO lo consume. Codex debe corregir también el documento anterior al sincronizar los `.md` del repositorio.
