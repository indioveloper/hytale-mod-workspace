# Raynor Mods portal

Portal estático del catálogo público de Raynor. Está aislado del código de los
mods y se puede servir desde cualquier hosting de archivos estáticos.

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
