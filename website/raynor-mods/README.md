# Raynor Mods portal

Portal estático del catálogo público de Raynor. Está aislado del código de los
mods y se puede servir desde cualquier hosting de archivos estáticos.

El catálogo publicado refleja las builds de Hytale estable `0.6.2` (Update 6).
`Configurable Mob Spawners` se etiqueta expresamente como WIP; los estados de
smoke test se mantienen visibles y no deben convertirse en "estable" solo
porque el proyecto compile.

La interfaz publica es deliberadamente minimalista y sigue la linea visual del
configurador de spawners: fondo calido, filas compactas y modo claro/oscuro. Los
asset packs permanecen ocultos tras una etiqueta WIP mientras sean privados.
Cada mod tiene un campo `curseforge` independiente; hasta recibir las fichas
definitivas, esos enlaces apuntan provisionalmente al perfil general de Raynor.

## Editar catálogo y enlaces

Todas las versiones, estados y URLs viven en `portal-config.js`. Los enlaces a
proyectos de CurseForge deben añadirse solamente cuando exista una ficha pública
en el [perfil oficial de Raynor](https://www.curseforge.com/members/raynor_hytale/projects).

Los IDs y namespaces técnicos `OrbGenesis:*` existentes no se renombran desde
esta web: forman parte de datos guardados y dependencias. Una migración de marca
técnica requerirá un proyecto separado con compatibilidad hacia atrás.

## Probar localmente

Desde esta carpeta:

```powershell
python -m http.server 8080
```

Abre `http://127.0.0.1:8080/`. La web no requiere compilación ni dependencias.
