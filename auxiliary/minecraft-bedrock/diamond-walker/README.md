# Camino de diamantes 1.0.2

Add-on independiente para Minecraft Bedrock. Cada jugador puede activar o
desactivar la conversion del bloque situado directamente bajo sus pies en un
bloque de diamante.

## Instalacion

Abre `.build/Camino_Diamantes_1_0_2.mcaddon` con Minecraft y activa el pack de
comportamiento **Camino de diamantes v1.0.2** en el mundo.

El mundo debe tener los trucos activados. No requiere pack de recursos ni
opciones experimentales.

## Comando

```text
/function diamantes
```

La primera ejecucion lo activa y la siguiente lo desactiva. El estado se guarda
por separado para cada jugador. Solo transforma el bloque que se pisa; si bajo
los pies hay aire, no hace nada. Los cambios realizados en el terreno son
permanentes y no se revierten al desactivar el comando.

## Desarrollo

Requiere Bedrock 26.40 o posterior y `@minecraft/server` 2.9.0.

```powershell
.\tools\Test-Pack.ps1
.\tools\Build-Pack.ps1
```
