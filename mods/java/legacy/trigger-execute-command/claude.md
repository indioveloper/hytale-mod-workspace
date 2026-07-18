He hecho un mod para añadir la funcion custom Execute Command a los trigger volumes de Hytale.
Funciona visualmente, pero hay Dos problemas visibles:

Las labels muestran la clave completa en vez del texto traducido — la clave del .lang no coincide con lo que el juego espera
No ejecuta — el método para acceder al servidor/consola en TriggerContext no es el que pusimos

El problema de ejecución está en esta línea:
javacontext.getServer().getConsoleSender().executeCommand(resolved);

Revisando el HytaleServer.jar hemos visto que:

TriggerContext solo tiene: Ref, Store, TriggerEventType, VolumeEntry — NO tiene getServer(). Para ejecutar comandos hay que ir por HytaleServer.get() → CommandManager
ConsoleSender no tiene executeCommand() — solo logging. Los comandos van por CommandManager.handleCommands(CommandSender, Deque)
PlayerRef tiene getUsername() y getUuid() — no getName()
KeyedCodec tiene constructor (String, Codec, Z) pero hay también (String, Codec) sin boolean
TriggerEffect tiene setEventType/getEventType — los campos Event/Interval/Delay de la UI son del efecto base, no nuestros

Los hallazgos críticos del servidor real:

CommandManager.get() → estático, devuelve CommandManager
CommandManager tiene handleCommands(CommandSender, Deque) — para ejecutar un string de comando hace falta Deque
TriggerContext solo contiene Ref, Store — para el servidor hay que usar HytaleServer.get()
PlayerRef tiene getUsername() (no getName()) y getPacketHandler()
ConsoleSender NO tiene executeCommand — es solo para logging
El método correcto en CommandManager para ejecutar desde string es: handleCommands(CommandSender sender, Deque deque) donde el sender puede ser ConsoleSender