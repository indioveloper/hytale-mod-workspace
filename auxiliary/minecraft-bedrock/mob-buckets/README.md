# Cubos para mobs 1.0.0

Add-on familiar e independiente para Minecraft Bedrock. Anade un cubo reutilizable
que captura una criatura al interactuar con ella y la libera al usar el cubo lleno.

## Instalacion

Abre `.build/Cubos_Para_Mobs_1_0_0.mcaddon` con Minecraft. En el mundo activa
el pack de comportamiento **Cubos para mobs v1.0.0**. No necesita pack de recursos.

## Obtener el cubo

- Creativo: busca **Cubo para mobs** en Objetos.
- Comando: `/give @s raynor:mob_bucket`.
- Supervivencia: combina un cubo vacio y una bola de slime en una mesa de trabajo.

## Uso

1. Con el cubo vacio en la mano, interactua con una criatura.
2. El texto descriptivo del objeto indica que criatura contiene.
3. Usa el cubo lleno mirando hacia un lugar despejado para liberarla.

Por seguridad no admite jugadores, jefes, guardianes, mascotas domesticadas,
criaturas con nombre ni criaturas con inventario. Bedrock no expone una forma
generica de copiar todo el estado interno de cualquier entidad; al soltarla se
crea una criatura nueva del mismo tipo, por lo que edad, variante, salud, equipo
y otros estados especiales no se conservan.

## Desarrollo

Requiere Bedrock 26.40 o posterior y `@minecraft/server` 2.9.0. Para validar y
empaquetar:

```powershell
.\tools\Test-Pack.ps1
.\tools\Build-Pack.ps1
```
