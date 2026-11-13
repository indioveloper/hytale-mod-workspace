package gg.raynormods.holograms;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class HologramCommand extends AbstractPlayerCommand {
    HologramCommand() {
        super("holo", "Gestiona hologramas de texto, imagen y GIF de Raynor.");
        addAliases("hologram", "holograms");
        setPermissionGroups(new String[]{"hytale:WorldEditor"});
        addSubCommand(new CreateText());
        addSubCommand(new ShowTemporary());
        addSubCommand(new CreateImage());
        addSubCommand(new ListAll());
        addSubCommand(new Info());
        addSubCommand(new AddLine());
        addSubCommand(new SetLine());
        addSubCommand(new RemoveLine());
        addSubCommand(new Billboard());
        addSubCommand(new Align());
        addSubCommand(new TextMode());
        addSubCommand(new Scale());
        addSubCommand(new TeleportHere());
        addSubCommand(new Remove());
        addSubCommand(new Images());
        addSubCommand(new Optimize());
        addUsageVariant(new FancyStyleCreateImage());
        addUsageVariant(new FancyStyleCreateImageWithSize());
        addUsageVariant(new FancyStyleBillboard());
    }

    protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref,
                           PlayerRef playerRef, World world) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) { message(context, "No se pudo abrir el editor de hologramas."); return; }
        player.getPageManager().openCustomPage(ref, store, new HologramEditorPage(playerRef));
    }

    private static void message(CommandContext context, String text) { context.sendMessage(Message.raw(text)); }
    private static String result(String error, String success) { return error == null ? success : "Error: " + error; }

    private static final class CreateText extends AbstractPlayerCommand {
        private final RequiredArg<String> key = withRequiredArg("key", "Clave unica.", ArgTypes.STRING);
        private final RequiredArg<String> text = withRequiredArg("text", "Texto visible.", ArgTypes.GREEDY_STRING);
        CreateText() { super("create", "Crea un holograma de texto tres bloques delante de ti."); }
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef, World world) {
            TextHologramService.SpawnResult result = TextHologramService.spawn(store, ref, key.get(context), text.get(context).trim());
            message(context, result.success() ? "Holograma de texto creado: " + result.id() : "Error: " + result.error());
        }
    }

    private static final class ShowTemporary extends AbstractPlayerCommand {
        private final RequiredArg<Float> duration = withRequiredArg("seconds", "Duracion entre 1 y 300 segundos.", ArgTypes.FLOAT);
        private final RequiredArg<String> text = withRequiredArg("text", "Texto visible.", ArgTypes.GREEDY_STRING);

        ShowTemporary() {
            super("show", "Muestra un holograma temporal con fundido y borrado automatico.");
            addAliases("temp", "fade");
        }

        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref,
                               PlayerRef playerRef, World world) {
            TextHologramService.SpawnResult result = TextHologramService.spawnTemporary(
                store, ref, duration.get(context), text.get(context).trim());
            message(context, result.success()
                ? "Holograma temporal creado: " + result.id()
                : "Error: " + result.error());
        }
    }

    private static final class CreateImage extends AbstractPlayerCommand {
        private final RequiredArg<String> key = withRequiredArg("key", "Clave unica.", ArgTypes.STRING);
        private final RequiredArg<String> source = withRequiredArg("source", "URL, local:archivo.png/gif o builtin:orb.", ArgTypes.STRING);
        private final OptionalArg<Float> size = withOptionalArg("size", "Lado mayor en bloques (2 por defecto).", ArgTypes.FLOAT);
        CreateImage() {
            super("createimage", "Crea una imagen o GIF desde URL, archivo local o recurso incluido.");
            addAliases("image", "gif");
            addUsageVariant(new CreateImageWithSize());
        }
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef, World world) {
            float amount = size.provided(context) ? size.get(context) : 2.0f;
            create(context, store, ref, key.get(context), source.get(context), amount);
        }

        private static final class CreateImageWithSize extends AbstractPlayerCommand {
            private final RequiredArg<String> key = withRequiredArg("key", "Clave unica.", ArgTypes.STRING);
            private final RequiredArg<String> source = withRequiredArg("source", "URL, local:archivo.png/gif o builtin:orb.", ArgTypes.STRING);
            private final RequiredArg<Float> size = withRequiredArg("size", "Lado mayor en bloques.", ArgTypes.FLOAT);

            CreateImageWithSize() { super("Crea una imagen o GIF indicando el tamano en bloques."); }

            protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref,
                                   PlayerRef playerRef, World world) {
                create(context, store, ref, key.get(context), source.get(context), size.get(context));
            }
        }

        private static void create(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref,
                                   String key, String source, float size) {
            ImageHologramService.SpawnResult result = ImageHologramService.spawn(store, ref, key, source, size);
            message(context, result.success() ? "Holograma de imagen creado: " + result.id() : "Error: " + result.error());
        }
    }

    /** Compatibility with FancyHolograms' /holo <key> createimage <source> syntax. */
    private static final class FancyStyleCreateImage extends AbstractPlayerCommand {
        private final RequiredArg<String> key = withRequiredArg("key", "Clave unica.", ArgTypes.STRING);
        private final RequiredArg<String> action = withRequiredArg("action", "createimage.", ArgTypes.STRING);
        private final RequiredArg<String> source = withRequiredArg("source", "URL o local:archivo.png/gif.", ArgTypes.STRING);

        FancyStyleCreateImage() {
            super("Compatibilidad: /holo <clave> createimage <fuente>.");
        }

        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref,
                               PlayerRef playerRef, World world) {
            if (!isCreateImage(action.get(context))) {
                message(context, "Accion desconocida. Usa createimage.");
                return;
            }
            CreateImage.create(context, store, ref, key.get(context), source.get(context), 2.0f);
        }

        private static boolean isCreateImage(String value) {
            return value != null && (value.equalsIgnoreCase("createimage") || value.equalsIgnoreCase("image")
                || value.equalsIgnoreCase("gif"));
        }

    }

    private static final class FancyStyleCreateImageWithSize extends AbstractPlayerCommand {
        private final RequiredArg<String> key = withRequiredArg("key", "Clave unica.", ArgTypes.STRING);
        private final RequiredArg<String> action = withRequiredArg("action", "createimage.", ArgTypes.STRING);
        private final RequiredArg<String> source = withRequiredArg("source", "URL o local:archivo.png/gif.", ArgTypes.STRING);
        private final RequiredArg<Float> size = withRequiredArg("size", "Lado mayor en bloques.", ArgTypes.FLOAT);

        FancyStyleCreateImageWithSize() { super("Compatibilidad con tamano explicito."); }

        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref,
                               PlayerRef playerRef, World world) {
            if (!FancyStyleCreateImage.isCreateImage(action.get(context))) {
                message(context, "Accion desconocida. Usa createimage.");
                return;
            }
            CreateImage.create(context, store, ref, key.get(context), source.get(context), size.get(context));
        }
    }

    private static final class FancyStyleBillboard extends AbstractPlayerCommand {
        private final RequiredArg<String> key = withRequiredArg("key", "Clave del holograma.", ArgTypes.STRING);
        private final RequiredArg<String> action = withRequiredArg("action", "billboard.", ArgTypes.STRING);

        FancyStyleBillboard() { super("Compatibilidad: /holo <clave> billboard."); }

        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref,
                               PlayerRef playerRef, World world) {
            if (!"billboard".equalsIgnoreCase(action.get(context))) {
                message(context, "Accion desconocida. Usa billboard.");
                return;
            }
            String name = key.get(context);
            String error = TextHologramService.find(store, name) != null
                ? TextHologramService.setBillboard(store, name, BillboardMode.BILLBOARD)
                : ImageHologramService.setBillboard(store, name, BillboardMode.BILLBOARD);
            message(context, result(error, "Billboard completo activado para " + name + "."));
        }
    }

    private static final class ListAll extends AbstractPlayerCommand {
        ListAll() { super("list", "Lista los hologramas cargados en este mundo."); }
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef, World world) {
            List<TextHologramService.LoadedHologram> texts = TextHologramService.list(store).stream().sorted(Comparator.comparing(TextHologramService.LoadedHologram::id)).toList();
            List<ImageHologramService.LoadedHologram> images = ImageHologramService.list(store).stream().sorted(Comparator.comparing(ImageHologramService.LoadedHologram::id)).toList();
            if (texts.isEmpty() && images.isEmpty()) { message(context, "No hay hologramas cargados en este mundo."); return; }
            message(context, "Hologramas cargados: " + (texts.size() + images.size()));
            for (TextHologramService.LoadedHologram hologram : texts) message(context, String.format(Locale.ROOT,
                "- %s [texto/%s] %.2fx %s (%.1f, %.1f, %.1f): %s", hologram.id(), hologram.renderMode(), hologram.size(), hologram.billboard(),
                hologram.x(), hologram.y(), hologram.z(), preview(hologram.text())));
            for (ImageHologramService.LoadedHologram hologram : images) message(context, String.format(Locale.ROOT,
                "- %s [imagen%s] %.2fx %s (%.1f, %.1f, %.1f): %s", hologram.id(), isGif(hologram.source()) ? "/GIF" : "",
                hologram.size(), hologram.billboard(), hologram.x(), hologram.y(), hologram.z(), hologram.source()));
        }
    }

    private static final class Info extends AbstractPlayerCommand {
        private final RequiredArg<String> key = withRequiredArg("key", "Clave del holograma.", ArgTypes.STRING);
        Info() { super("info", "Muestra todos los datos y lineas de un holograma."); }
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef, World world) {
            String name = key.get(context);
            TextHologramService.LoadedHologram text = TextHologramService.find(store, name);
            if (text != null) {
                message(context, String.format(Locale.ROOT, "%s [texto] escala=%.2f billboard=%s alineacion=%s render=%s pos=(%.2f, %.2f, %.2f)",
                    text.id(), text.size(), text.billboard(), text.alignment(), text.renderMode(), text.x(), text.y(), text.z()));
                String[] lines = text.text().split("\\R", -1);
                for (int index = 0; index < lines.length; index++) message(context, "  [" + index + "] " + lines[index]);
                return;
            }
            ImageHologramService.LoadedHologram image = ImageHologramService.find(store, name);
            if (image != null) {
                message(context, String.format(Locale.ROOT, "%s [imagen%s] escala=%.2f billboard=%s pos=(%.2f, %.2f, %.2f) fuente=%s",
                    image.id(), isGif(image.source()) ? "/GIF" : "", image.size(), image.billboard(), image.x(), image.y(), image.z(), image.source()));
                return;
            }
            message(context, "No existe un holograma con la clave " + name + ".");
        }
    }

    private static final class AddLine extends AbstractPlayerCommand {
        private final RequiredArg<String> key = withRequiredArg("key", "Clave del texto.", ArgTypes.STRING);
        private final RequiredArg<String> text = withRequiredArg("text", "Nueva linea.", ArgTypes.GREEDY_STRING);
        AddLine() { super("addline", "Anade una linea a un holograma de texto."); }
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef, World world) {
            message(context, result(TextHologramService.addLine(store, key.get(context), text.get(context).trim()), "Linea anadida."));
        }
    }

    private static final class SetLine extends AbstractPlayerCommand {
        private final RequiredArg<String> key = withRequiredArg("key", "Clave del texto.", ArgTypes.STRING);
        private final RequiredArg<Integer> index = withRequiredArg("index", "Indice desde cero.", ArgTypes.INTEGER);
        private final RequiredArg<String> text = withRequiredArg("text", "Nuevo contenido.", ArgTypes.GREEDY_STRING);
        SetLine() { super("setline", "Sustituye una linea de texto."); }
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef, World world) {
            message(context, result(TextHologramService.setLine(store, key.get(context), index.get(context), text.get(context).trim()), "Linea actualizada."));
        }
    }

    private static final class RemoveLine extends AbstractPlayerCommand {
        private final RequiredArg<String> key = withRequiredArg("key", "Clave del texto.", ArgTypes.STRING);
        private final RequiredArg<Integer> index = withRequiredArg("index", "Indice desde cero.", ArgTypes.INTEGER);
        RemoveLine() { super("removeline", "Elimina una linea de texto."); }
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef, World world) {
            message(context, result(TextHologramService.removeLine(store, key.get(context), index.get(context)), "Linea eliminada."));
        }
    }

    private static final class Billboard extends AbstractPlayerCommand {
        private final RequiredArg<String> key = withRequiredArg("key", "Clave del holograma.", ArgTypes.STRING);
        private final RequiredArg<BillboardMode> mode = withRequiredArg("mode", "FIXED, BILLBOARD_Y o BILLBOARD.", ArgTypes.forEnum("billboard", BillboardMode.class));
        Billboard() { super("billboard", "Cambia el modo de orientacion."); }
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef, World world) {
            String name = key.get(context); BillboardMode value = mode.get(context);
            String error = TextHologramService.find(store, name) != null ? TextHologramService.setBillboard(store, name, value) : ImageHologramService.setBillboard(store, name, value);
            message(context, result(error, "Billboard actualizado a " + value + "."));
        }
    }

    private static final class Align extends AbstractPlayerCommand {
        private final RequiredArg<String> key = withRequiredArg("key", "Clave del texto.", ArgTypes.STRING);
        private final RequiredArg<TextAlignment> alignment = withRequiredArg("alignment", "LEFT, CENTER o RIGHT.", ArgTypes.forEnum("alignment", TextAlignment.class));
        Align() { super("align", "Cambia la alineacion del texto."); }
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef, World world) {
            message(context, result(TextHologramService.setAlignment(store, key.get(context), alignment.get(context)), "Alineacion actualizada."));
        }
    }

    private static final class TextMode extends AbstractPlayerCommand {
        private final RequiredArg<String> key = withRequiredArg("key", "Clave del texto.", ArgTypes.STRING);
        private final RequiredArg<TextRenderMode> mode = withRequiredArg("mode", "AUTO, LIVE o RICH.", ArgTypes.forEnum("textmode", TextRenderMode.class));
        TextMode() { super("textmode", "Elige texto nativo nitido o renderer visual con estilos."); }
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef, World world) {
            TextRenderMode value = mode.get(context);
            String suffix = value == TextRenderMode.LIVE
                ? " LIVE usa texto nativo; tamano, color, glow, alineacion y FIXED no los expone Hytale."
                : "";
            message(context, result(TextHologramService.setRenderMode(store, key.get(context), value),
                "Modo de texto actualizado a " + value + "." + suffix));
        }
    }

    private static final class Scale extends AbstractPlayerCommand {
        private final RequiredArg<String> key = withRequiredArg("key", "Clave del holograma.", ArgTypes.STRING);
        private final RequiredArg<Float> amount = withRequiredArg("amount", "Escala en bloques.", ArgTypes.FLOAT);
        Scale() { super("scale", "Cambia el tamano del holograma."); }
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef, World world) {
            String name = key.get(context); float value = amount.get(context);
            String error = TextHologramService.find(store, name) != null ? TextHologramService.setScale(store, name, value) : ImageHologramService.setScale(store, name, value);
            message(context, result(error, "Escala actualizada a " + value + "."));
        }
    }

    private static final class TeleportHere extends AbstractPlayerCommand {
        private final RequiredArg<String> key = withRequiredArg("key", "Clave del holograma.", ArgTypes.STRING);
        TeleportHere() { super("tphere", "Mueve el holograma delante de ti."); addAliases("movehere"); }
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef, World world) {
            String name = key.get(context);
            String error = TextHologramService.find(store, name) != null ? TextHologramService.moveHere(store, ref, name) : ImageHologramService.moveHere(store, ref, name);
            message(context, result(error, "Holograma movido."));
        }
    }

    private static final class Remove extends AbstractPlayerCommand {
        private final RequiredArg<String> key = withRequiredArg("key", "Clave del holograma.", ArgTypes.STRING);
        Remove() { super("remove", "Elimina definitivamente un holograma."); addAliases("delete"); }
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef, World world) {
            String name = key.get(context);
            int removed = TextHologramService.remove(store, name) + ImageHologramService.remove(store, name);
            message(context, removed > 0 ? "Holograma eliminado: " + name : "No existe un holograma con la clave " + name + ".");
        }
    }

    private static final class Images extends AbstractPlayerCommand {
        Images() { super("images", "Lista las imagenes incluidas y la carpeta local."); }
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef, World world) {
            message(context, "Imagenes incluidas:");
            for (ImageCatalog.Entry entry : ImageCatalog.entries()) message(context, "- builtin:" + entry.key() + " - " + entry.description());
            message(context, "Locales: local:archivo.png o local:animacion.gif dentro de " + HologramRuntime.imageDirectory());
        }
    }

    private static final class Optimize extends AbstractPlayerCommand {
        Optimize() { super("optimize", "Vacia caches temporales para regenerarlas bajo demanda."); }
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef, World world) {
            HologramRuntime.clearCaches();
            message(context, "Caches de hologramas limpiadas. Los recursos activos se regeneraran bajo demanda.");
        }
    }

    private static String preview(String value) {
        String flat = value == null ? "" : value.replace('\n', ' ');
        return flat.length() <= 64 ? flat : flat.substring(0, 61) + "...";
    }

    private static boolean isGif(String source) {
        String value = source == null ? "" : source.toLowerCase(Locale.ROOT);
        return value.contains(".gif") || value.startsWith("gif:");
    }
}
