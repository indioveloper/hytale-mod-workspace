# Base enemiga (prototipo 0.3.2)

Conciliación 2026-11-17: las bases, cadáveres y llaves son prototipos; 0.3.2
todavía requiere smoke test para prompt, orientación tras recargar chunk y
obtención/consumo de llaves. El botín de cofres que se llama «plano» no otorga
permisos de fabricación de únicos. Su integración con las blueprints físicas
del [plan maestro](../../docs/DESERT_SURVIVAL_MASTER_PLAN_2026-11-17.md)
queda pendiente, al igual que la persistencia del inventario/sangre del
cadáver al reiniciar el servidor.

Complemento Java del pack `desert-worldgen` 0.4.1 para Hytale 0.7.7.
Solo procesa los roles `Desert_Raider_Swordsman` y `Desert_Raider_Gunner`.
WorldGen debe estar activo en el save para que existan los puestos y NPC, pero
no se declara como dependencia Java: es un pack de assets y el gestor de
plugins rechazaba Enemy Base al intentar resolverlo como plugin.

- `Template_Intelligent` controla movimiento, visión, respuesta al daño y
  ataques. Ambos NPCs deambulan hasta 3 bloques del spawn. Los marcadores
  reaparecen a los 10 minutos tras una baja.
- El sistema Java retiene el objetivo mientras está en el puesto (radio
  provisional de 11 bloques desde el spawn). Fuera, solo lo libera cuando
  está a más de 30 bloques del spawn **y** ya no hay línea de visión.
  Acercarse a 5 bloques de un guardia también inicia el combate.
- El espadachín usa un sable vanilla; el tirador empuña la pistola vanilla
  de GunPvP y dispara con la interacción NPC del pirata. Modelos y armas son
  referencias: no se modifican los assets vanilla.
- El cuerpo persiste hasta 300 s. Con `Usar` se intenta abrir como contenedor
  de ocho huecos. La 0.3.1 corrige el formato de `Desert_Corpse_Loot.json`:
  el log de la 0.3.0 indicaba que `Type` y `Next` se ignoraban. Falta confirmar
  en juego que aparezca el prompt y que se pueda abrir.
- Mientras tanto, el botín normal cae al suelo al morir: 8-20 Solaris,
  ración especiada 55 %, poción pequeña 40 %, pechera de lino 4 % y arma
  del rol 3 %. No hay materiales de construcción ni componentes de armas o
  herramientas. El objeto `Cadáver de saqueador` permanece dentro del cuerpo,
  sin duplicarse como drop.
- Con el extractor improvisado equipado, `Usar` sobre el cadáver llena una
  bolsa hemática vacía con 250 ml de sangre. Reserva provisional: 1000 ml por
  saqueador. Si faltan bolsas vacías, no se consume sangre y aparece un error.
  El tier 1 futuro está preparado para 500 ml y dos bolsas por uso.
  Tras vaciar inventario y sangre, el cadáver desaparece inmediatamente;
  de lo contrario desaparece al cumplirse los 300 s.
- La entidad muerta anuncia `Saquear / extraer sangre` al apuntarla. La misma
  interacción propia abre el contenedor o llena la bolsa según el objeto
  equipado. La 0.2.1 cancelaba `UseEntityEvent.Pre`, lo que permitía la
  extracción pero no dejaba avanzar la interacción del inventario.
- En 0.3.2 se envía explícitamente el hint nativo de `Interactable` a quienes
  ven el cadáver y se muestra un nombre de cadáver con la instrucción de uso.
  Tras la animación de muerte, el cuerpo se inclina 90 grados; la rotación
  pertenece al transform persistido, para que no vuelva a verse de pie al
  recargar el chunk. Falta validar ambas cosas visualmente en juego.
- Mientras probamos las bases existentes, cada espadachín deja una llave
  garantizada en el inventario del cadáver. La llave del cofre T2-T4 sigue
  disponible solo en bases generadas desde WorldGen 0.4.1. Al transferir la
  llave al jugador, desaparece del inventario y aumenta su contador persistente.

El inventario y la sangre del cadáver permanecen en memoria en esta versión:
no reiniciar el servidor mientras se saquea. Los Solaris son un objeto físico apilable;
todavía no hay vendedor ni saldo monetario. La pistola usa el prototipo vanilla
de GunPvP; su uso por jugadores se revisará con el combate propio.

Build:

```powershell
.\scripts\Build-JavaMod.ps1 -ProjectPath games/desert-survival/prototypes/enemy-base -ArtifactName DesertSurvival-EnemyBase-0.3.2.jar
```

Smoke test: abrir `Raynor_Desert_EnemyBase_Test` con `/instances`, ir a la
zona 3 (aprox. 560-900 bloques del origen), localizar un puesto, provocar a
ambos NPCs, cortar visión a más de 30 bloques, derrotarlos y abrir los cuerpos.
