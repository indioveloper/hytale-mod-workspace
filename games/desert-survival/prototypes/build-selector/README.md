# Desert Survival Build Selector Prototype 0.6.2

Design reconciliation (2026-11-17): the current generator has a fixed 100-unit
output and local purifier connection; the proposed shared base energy grid,
priorities, blackouts and functional penta-shields are **not implemented**.
Current costs use vanilla basalt as a placeholder. See
[base power design](../../docs/BASE_POWER_GRID.md) and
[STATUS](../../STATUS.md) before changing installations.

Prototipo aislado para Hytale 0.7.7. No modifica Research, Vitals ni los
otros mods. El catalogo contiene cuatro secciones de base y cinco maquinas:
fabricador, purificador de sangre, generador y dos refinerias. Las maquinas
aparecen cuando se investiga su nodo; no tienen receta de bolsillo. El
reciclador de sangre de bloques y el prefab de consola siguen en el paquete
para conservar los saves antiguos, pero ya no se ofrecen en el catalogo.
Todos los prefabs nuevos usan basalto vanilla como coste temporal: un bloque
por cada bloque colocado. Las refinerias y el fabricador son modelos de
prototipo sin procesamiento. El marco aun no es una puerta interactiva.

La consola, el purificador de agua y el generador de energia son items de
bloque nativos en `Furniture.Benches`: `Desert_Base_Console_Core_Prototype`,
`Desert_Water_Purifier_Prototype` y `Desert_Power_Generator_Prototype`.
Se colocan como bancos normales desde el inventario creativo. Reutilizan
modelos y texturas vanilla como placeholders. El purificador y el generador
son estaciones propias. El bloque antiguo `Desert_Water_Purifier_Prototype`
abre ahora la misma interfaz de sangre que el nuevo
`Desert_Blood_Purifier_Prototype`, conservando su ID para los saves.

El generador tiene cuatro ranuras que solo aceptan `Fuel_Cell_Prototype`.
Cada celula aporta cinco minutos. El generador abre el inventario nativo de
cuatro ranuras; la potencia y la autonomia aparecen en el HUD de la base
sin mensajes repetidos en el chat. La potencia activa
es fija: 100 unidades; el excedente se disipa, sin almacenamiento. El
purificador consume energia de un generador activo del mismo claim, o de uno
a 12 bloques si no hay claim. El HUD de base muestra potencia y autonomia.
Combustible y tiempo activo persisten por bloque en `desert-machinery.properties`.

`Desert_Blood_Purifier_Prototype` abre una interfaz propia. Una pulsacion
procesa una bolsa llena de 250 ml de sangre y devuelve la bolsa vacia;
genera 50 ml de agua si recibe energia. La otra pulsacion transfiere 250 ml
al flask vacio o medio lleno. La bolsa llena es un item creativo de prueba: el extractor
improvisado tiene receta, pero aun no extrae sangre de cadaveres.
La receta de bolsillo del generador y del purificador se ha retirado: su
investigacion los incorpora al selector. El coste temporal del prototipo es
un bloque de basalto para cada estacion.

Los crafteos vanilla de fogata, cama basica, antorcha crude, cofre pequeno,
espada crude, hachas, picos y bancos vanilla se ocultan en el cliente de los
jugadores de este modo y se rechazan en el servidor. Se usan mensajes
`UpdateRecipes.Remove` por jugador; el catalogo global de recetas no se toca.

## Uso

Coloca primero el item de consola en una casilla libre. Su bloque reclama
40 x 40 x 100 bloques cuando la colocacion se confirma. Mientras lo sostienes,
un perimetro naranja marca la ubicacion prevista y los de bases cercanas se
ven rojos. El borde es una aproximacion al bloque apuntado desde arriba;
comprueba la ubicacion final antes de colocar en la cara lateral de otro bloque.

En creativo o aventura, selecciona el plano `Desert_Build_Planner_Prototype`:
el suelo 3 x 3 aparece como prefab inicial. `/desertbuild` sigue abriendo el
catalogo para elegir otra pieza, ver su preview 3D y equipar el plano.
El clic izquierdo coloca la pieza un bloque por encima de ese bloque y deja
la misma pieza activa para seguir construyendo. Usar (F normalmente) alterna
entre colocar y desmontar; en el segundo modo, el clic izquierdo retira la
pieza registrada que se apunta. El clic derecho cambia de categoria al colocar
y sale del modo desmontar. Q recorre las piezas de la categoria actual; Pick
gira 90 grados. Cambiar a otra casilla de la barra rapida oculta el plano. El panel fijo
inferior derecho usa el fondo, los pictogramas del raton y las teclas del
legend nativo de Builder Tools. Muestra modo, categoria, pieza y coste temporal
como `requerido/disponible`, verde si alcanza y rojo si falta. La cantidad
disponible suma inventario y cofres cargados del claim propio, con refresco
cada medio segundo. Q y F son las teclas habituales; las pistas nativas del
objeto muestran las asignaciones reales si el cliente las ha cambiado. E y R
no estaban llegando al objeto: Ability2 y Ability3 se usan para habilidades
equipadas y no sirven para navegar este catalogo desde un mod de servidor.
En desmontaje aparece un contorno naranja alrededor de la pieza registrada
bajo el cursor. Dentro de la base propia aparece un perimetro cian a la altura
de la consola. Son vistas privadas
enviadas como geometria de visualizacion;
no crean Trigger Volumes funcionales. El HUD de construccion cambia a rojo en
desmontaje y oculta las pistas de navegacion. Al colocar, el panel es marron
para piezas de base y azul para maquinaria. Cambiar de categoria no escribe
en el chat; los errores si se notifican. Al salir se restaura la casilla anterior.
El plugin de servidor no intercepta la rueda del raton. La rueda cambia
la casilla de la hotbar y no distingue un giro intencionado de un cambio de
herramienta. Si la huella esta ocupada por
bloques, liquidos o un jugador, o queda a mas de 12 bloques, el pegado se
rechaza. No se usa FORCE.

Primero coloca una consola de base. Reclama 40 x 40 x 100 bloques centrados
en ella: X/Z desde -20 hasta +19 y Y desde -50 hasta +49. Rechaza solapes de
claims y cofres que ya existieran dentro del area; despues puedes poner cofres
vanilla que quedan protegidos por el claim. Las piezas deben caber totalmente
en el claim propio. Los eventos de colocar, romper, danar y usar bloques
bloquean a otros jugadores en el claim. Las piezas registradas no se rompen
con herramientas normales: se desmontan con el plano. Mientras el plano esta
equipado, no se pueden abrir cofres ni la consola. Para quitar una consola hay
que retirar primero sus celulas almacenadas y confirmar con un segundo clic
en menos de 10 segundos tras leer el aviso rojo. Las otras piezas y cofres
permanecen fisicamente, pero pierden el claim. Las piezas dejan de estar
registradas, no dan reembolso posterior y pueden romperse normalmente. Otro
jugador puede reclamar la base colocando una consola en el mismo origen de la
anterior, incluso si quedaron cofres. Con otro objeto o la mano vacia, el dueno
puede interactuar con la consola para abrir un contenedor nativo de cuatro ranuras para
`Fuel_Cell_Prototype`. Solo el dueno puede abrirlo. Cada celula aporta cinco
minutos de energia y se consume al comenzar ese tramo. El saldo de combustible
y el tiempo restante se guardan en el registro del mundo. El HUD de base
muestra dueno, estado y tiempo de energia mientras el jugador esta dentro del
claim, con independencia del plano de construccion. El estado de escudo es un
prototipo de energia; no modifica aun la proteccion del claim. Requiere el mod
Cutterray 0.1.9 para obtener las celulas del nodo nuevo. Las consolas colocadas
con 0.3.1 convierten su bloque central al abrir el mundo; el claim se conserva.

El coste temporal de los prefabs es `Rock_Basalt_Brick_Smooth`: un bloque por
cada bloque solido (1-30 por pieza). Se consume primero del inventario y
despues de los cofres cargados dentro del claim. Al desmontar se acredita
el coste pagado y se entrega primero a cofres con ese item, despues a otros
cofres con espacio y finalmente al inventario. El resto queda pendiente en
el registro persistente del mundo y se reclama en `/desertbuild`. El archivo
es `desert-build-selector.properties` dentro de la carpeta de datos del mundo.
La consola y el purificador de agua legacy no tienen receta; la consola colocada como
bloque se recupera como item al desmontarla con el plano, si hay espacio en
el inventario. El generador y fabricador aparecen al investigar `CN-0.3`,
el purificador en `AG-0.2` y las refinerias en `CN-0.4` y `CN-0.5`.

Las piezas colocadas con la 0.2.4 y anteriores no tienen registro: esta
version no las desmonta ni devuelve materiales por ellas. Se pueden retirar
manualmente en creativo. Los desbloqueos de Research muestran las maquinas
en el catalogo; aun faltan grupos,
aliados ni proteccion frente a herramientas creativas que pegan bloques sin
pasar por los eventos normales. La consola y las dos maquinas aun usan
figuras placeholder; las maquinas no tienen un modelo final.

## Compilar y verificar

Desde la raiz del workspace:

    .\games\desert-survival\prototypes\build-selector\tools\Generate-PrototypePrefabs.ps1
    .\scripts\Build-JavaMod.ps1 -ProjectPath .\games\desert-survival\prototypes\build-selector -ArtifactName DesertSurvival-BuildSelector-0.6.2.jar -ServerJar C:\tmp\HytaleServer-0.7.7-survival-vitals.jar
    .\games\desert-survival\prototypes\build-selector\tools\Test-Package.ps1 -ArchivePath .\games\desert-survival\prototypes\build-selector\.build\dist\DesertSurvival-BuildSelector-0.6.2.jar

Usa una copia local del HytaleServer.jar instalado para javac si Windows
deniega el cierre del JAR bajo AppData. Comprueba que la copia tenga el mismo
SHA-256 que el runtime de destino. El script de build no detecta siempre la
excepcion interna de javac cuando este devuelve codigo 0: exige una salida
sin excepciones y la prueba de paquete.

Instala solo en un save de prueba despues de cerrar el juego. No lo combines
con otra copia que tenga el mismo Group:Name. El smoke test debe comprobar
entrada al mundo, apertura del menu, cambio de preview, giro, cierre del menu,
proyeccion sobre el terreno, cobro desde inventario y cofre, claim y permisos,
desmontaje, reembolso con cofres llenos y reclamacion pendiente. No usar en un
save importante hasta completar esa prueba.

Estado 2026-11-15: la 0.1.0 no encontro los prefabs porque buscaba sus
nombres sin `.prefab.json`. La 0.2.0 corrigio la ruta y mostro el fantasma,
pero el log de dune1 registro `Unused key(s): Type, Next` en los dos archivos
colocados erroneamente bajo `RootInteractions`, seguido de `Missing interaction`
al hacer clic. El fantasma tambien permanecio fijo porque se actualizaba solo
con eventos de movimiento del raton. La 0.2.1 coloca las definiciones bajo
`Item/Interactions`, sincroniza el slot activo con el cliente y recalcula el
objetivo a partir de la camara en cada tick mientras el plano este equipado.
Compilacion y prueba de paquete completadas. Instalado en `dune1/mods` con
SHA-256 `28A92980C7AD42E8ED830EEA86D755B5D84F679D9DA80DB64EBEAFF1EE1CA840`,
igual al build. La 0.2.0 queda archivada; smoke test del usuario pendiente.

La 0.2.2 mantiene la misma pieza tras cada colocacion y anade navegacion
sin abrir de nuevo el selector. Compila y pasa la prueba de paquete. Instalada
en `dune1/mods` con SHA-256
`A27F0061D3D58EFA092E521BDC3E2C8E290A3DDCDFCFCDEBF9C1E3A571DD9852`,
igual al build; la 0.2.1 se archivo. Prueba en el juego pendiente del usuario.

La 0.2.3 anade `HudInputBindingEntry` a las seis acciones del plano, con
etiquetas traducidas en ingles y espanol, y `Ability2` para cambiar de
categoria. El HUD usa los iconos y teclas que resuelve el propio cliente; no
contiene imagenes fijas de Q/E. En el MVP hay una sola pieza por categoria, de
modo que avanzar categoria y avanzar pieza llevan al mismo resultado visual.
Compila y pasa el test de paquete. Instalada en `dune1/mods` con SHA-256
`AD57528E9058172EDEDAD32F5DC465CF0A851BC4D65A7D7BDCFED01FB3A87E84`,
igual al build; la 0.2.2 queda archivada. Smoke test pendiente del usuario.

La 0.2.4 agrupa las cuatro piezas estructurales en Base y anade una segunda
categoria de Maquinaria con dos prefabs colocables de basalto. Habilidades
1/2 ciclan piezas dentro de la categoria; 3/4 ciclan categorias. El chat
muestra categoria y pieza en cada cambio. Habilidad 4 debe vincularse a T
en el cliente para obtener el esquema Q/E/R/T. Compila y pasa el test de
paquete contra 0.7.7. Instalada en `dune1/mods` con SHA-256
`63C19BDF6B4C49197848E7FCA4C47B21D23EAF5068C8DA6494A1BBFADCA78BDD`,
igual al build; la 0.2.3 queda archivada. Smoke test pendiente.

La 0.3.0 incorpora HUD fijo de controles, consola de base y claim persistente,
costes temporales de basalto, cobro desde inventario/cofres, modo desmontaje y
reserva reclamable desde el menu. Q/E ciclan piezas y R categorias; F cambia
entre colocar y desmontar. Compila y pasa la prueba de paquete contra 0.7.7.
Instalada en `dune1/mods` con SHA-256
`460CD76189650CC0175BCE092C56B3E8D81CF79C21E0966AAF23E7467F18712C`,
igual al build; la 0.2.4 queda archivada. Requiere smoke test dentro del juego.

La 0.3.1 reemplaza las teclas rotas por componentes del legend vanilla,
anade coste/disponibilidad, estado real del escudo y energia, un perimetro
visual del claim y contorno naranja del objetivo de desmontaje. Compila contra
0.7.7 y pasa la prueba de paquete. Instalada en `dune1/mods` con SHA-256
`1B85EE45B5D6218A40B3127A01F1FF65107B6165D75F69E8C4C218B73BFB1574`,
igual al build; la 0.3.0 queda archivada. Smoke test de la UI y las vistas
pendiente del usuario.

La 0.4.0 integra el combustible de Cutterray 0.1.9. Q recorre todas las piezas
porque E/R no despachaban Ability2/3 al plano; el HUD ya no anuncia esas teclas.
El chat omite confirmaciones de piezas. Desmontaje tiene panel rojo sin controles
de navegacion. El HUD de base es independiente y muestra dueno, escudo y tiempo.
La consola abre un inventario de cuatro ranuras, solo para celulas de
combustible, y consume una por cada cinco minutos activos. Las consolas 0.3.1
se migran conservando el claim. Compila y pasa la prueba de paquete contra
0.7.7. Instalada en `dune1/mods` con SHA-256
`BDF76B90C0B466A8D00769B84F308A5BC98D9AAAC024D77092BE68D0E841BE84`,
igual al build; la 0.3.1 queda archivada. Cutterray 0.1.9 esta activo en
`UserData/Mods` con SHA-256
`6587F8E59B33F00A9C799791FC2C7E6DFBBE4B71C94B23BB631FD5506386E962`.
No hay IDs de plugin activos duplicados. Smoke test en el juego pendiente.

La 0.4.1 se valido en fuentes, pero no se instalo por estar Hytale abierto.
Sus cambios de Q y agacharse + Q entran en la 0.5.0. La 0.5.0 tambien amplía
los dos paneles para evitar recortes; equipa automaticamente el fantasma de
consola al seleccionar el plano; bloquea el uso de bloques con el plano en
mano; muestra el perimetro futuro; y exige dos clics con advertencia para
retirar una consola. El retiro abandona las piezas en el mundo, elimina sus
registros y permite reclamar la base desde el origen de la consola antigua.
Compila y pasa test de paquete contra 0.7.7. Instalada en `dune1/mods` con
SHA-256 `5EF818AD714BA0BF04840D69E41ABD9F12149F492C75D23C18559896408EA6A3`,
igual al build; la 0.4.0 esta archivada. Smoke test pendiente.

La 0.5.1 sustituye el `HotkeyLabel` de giro, que aparecia vacio y recortado,
por el icono nativo `RowHintIconMouseMiddleClick` del legend vanilla. Compila
y pasa test de paquete contra 0.7.7. Instalada en `dune1/mods` con SHA-256
`2C43322254558E860D7AF8ECA6A03DA09F14655FF217079589F5826EC651653F`,
igual al build; la 0.5.0 esta archivada. Smoke test pendiente.

La 0.5.2 muestra en rojo los limites de otras bases cercanas mientras se
coloca una consola. El clic derecho cicla categorias al colocar y sale del
desmontaje; Q cicla piezas. El bloque central de la consola declara
`Open_Container` y el inventario de combustible abre la pagina nativa Bench.
Compila y pasa test de paquete contra 0.7.7. Instalada en `dune1/mods` con
SHA-256 `C9A11D544496ABCB0777E4AF5F26D893DB99B724024931BC32F8A8F747703197`,
igual al build; la 0.5.1 queda archivada. Smoke test en juego pendiente.

La 0.5.3 promueve la consola a bloque colocable y registra su claim tras
confirmar que el bloque se coloco. Anade purificador de agua y generador como
bancos nativos vacios, conserva el prefab de consola solo para datos antiguos,
quita el mensaje de cambio de categoria y da fondo marron o azul al panel
segun el tipo de pieza. Compila y pasa test de paquete contra 0.7.7.
Instalada en `dune1/mods` con SHA-256
`396B0ABC66261856D88B120EDD721F59C5C52CD6C681092174D6207872D1D783`,
igual al build; la 0.5.2 queda archivada. Smoke test en juego pendiente.
