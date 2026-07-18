# Chest Labels Mod

Mod base para Hytale que permite poner nombre e icono a cofres o contenedores y mostrarlo encima del texto normal de interaccion al apuntarlos.

Uso principal:

- abre el cofre
- edita el nombre desde el panel que aparece junto a la ventana del cofre
- elige el icono y pulsa `Save`

Comandos:

- `/chestlabel set "Tesoro del jefe" star`
- `/chestlabel clear`
- `/chestlabel icons`

Iconos incluidos:

- `loot`
- `star`
- `key`
- `check`
- `warning`

Notas:

- El nombre se guarda en el bloque contenedor, asi que persiste con el mundo.
- El HUD se dibuja como una capa custom por encima del hint base.
- El editor principal ahora aparece directamente al abrir la interfaz del cofre.
- Los iconos se cambian activando paneles predefinidos con `Visible`, porque este runtime no permite mutar `TexturePath` en caliente.
