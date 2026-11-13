package gg.raynormods.holograms;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class HologramEditorPage extends InteractiveCustomUIPage<HologramEditorPage.PageData> {
    private static final Pattern HEX_COLOR = Pattern.compile("[0-9a-fA-F]{6}");
    private Vector3d suggestedPosition;
    private float suggestedYaw;

    public HologramEditorPage(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, PageData.CODEC);
    }

    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commands,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ensurePlacement(ref, store);
        // Version the page asset path whenever its selector contract changes. The Hytale
        // client can retain an older Custom UI document across plugin-only reloads.
        commands.append("Pages/RaynorHolograms/HologramEditorV081.ui");
        commands.set("#KeyInput.Value", nextSuggestedKey(store));
        commands.set("#TextInput.Value", "<#ff55ff>Raynor</> <#55ffff:glow>Holograms</>\n<yellow>Online:</> {online}");
        commands.set("#ColorInput.Value", "ff55ff");
        commands.set("#Color2Input.Value", "55ffff");
        commands.set("#TextSizeInput.Value", "1");
        commands.set("#SourceInput.Value", "builtin:orb");
        commands.set("#ImageSizeInput.Value", "2");
        commands.set("#BillboardDropdown.Entries", enumEntries(List.of("FIXED", "BILLBOARD_Y", "BILLBOARD")));
        commands.set("#BillboardDropdown.Value", "BILLBOARD_Y");
        commands.set("#AlignmentDropdown.Entries", enumEntries(List.of("LEFT", "CENTER", "RIGHT")));
        commands.set("#AlignmentDropdown.Value", "CENTER");
        commands.set("#TextModeDropdown.Entries", enumEntries(List.of("AUTO", "LIVE", "RICH")));
        commands.set("#TextModeDropdown.Value", "AUTO");
        commands.set("#EffectDropdown.Entries", enumEntries(List.of("NORMAL", "GLOW", "SHADOW", "HIGHLIGHT", "OUTLINE", "BOLD")));
        commands.set("#EffectDropdown.Value", "GLOW");
        commands.set("#NativePreview.Text", nativePreview("<#ff55ff>Raynor</> <#55ffff:glow>Holograms</>\n<yellow>Online:</> {online}"));
        commands.set("#XInput.Value", coordinate(suggestedPosition.x));
        commands.set("#YInput.Value", coordinate(suggestedPosition.y));
        commands.set("#ZInput.Value", coordinate(suggestedPosition.z));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#CreateTextButton", formData("CREATE_TEXT"), false);
        events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#TextInput",
            new EventData().append("Action", "PREVIEW").append("@Text", "#TextInput.Value"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ApplyStyleButton", styleData("APPLY_STYLE"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ClearStyleButton", styleData("CLEAR_STYLE"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#Preview3DButton", formData("PREVIEW_3D"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#CreateImageButton", formData("CREATE_IMAGE"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", EventData.of("Action", "CLOSE"), false);
    }

    private static EventData formData(String action) {
        return new EventData().append("Action", action)
            .append("@Key", "#KeyInput.Value").append("@Text", "#TextInput.Value")
            .append("@TextSize", "#TextSizeInput.Value").append("@Source", "#SourceInput.Value")
            .append("@ImageSize", "#ImageSizeInput.Value").append("@Billboard", "#BillboardDropdown.Value")
            .append("@Alignment", "#AlignmentDropdown.Value").append("@TextMode", "#TextModeDropdown.Value")
            .append("@X", "#XInput.Value")
            .append("@Y", "#YInput.Value").append("@Z", "#ZInput.Value");
    }

    private static EventData styleData(String action) {
        return new EventData().append("Action", action)
            .append("@Text", "#TextInput.Value")
            .append("@Color", "#ColorInput.Value")
            .append("@Color2", "#Color2Input.Value")
            .append("@Effect", "#EffectDropdown.Value");
    }

    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PageData data) {
        if ("CLOSE".equals(data.action)) { close(); return; }
        if ("PREVIEW".equals(data.action)) {
            updatePreview(data.text, null);
            return;
        }
        if ("APPLY_STYLE".equals(data.action)) {
            String color = normalizeColor(data.color);
            if (color == null) {
                show("El color debe tener 6 digitos hexadecimales, por ejemplo ff55ff.");
                return;
            }
            String color2 = text(data.color2).isBlank() ? color : normalizeColor(data.color2);
            if (color2 == null) {
                show("El segundo color debe quedar vacio o tener 6 digitos hexadecimales.");
                return;
            }
            String effect = normalizeEffect(data.effect);
            String plain = RichTextParser.plainText(RichTextParser.parse(data.text));
            String gradient = color.equals(color2) ? "" : "~#" + color2;
            String tag = "<#" + color + gradient + ("normal".equals(effect) ? "" : ":" + effect) + ">";
            updatePreview(tag + plain + "</>", "Estilo aplicado a todo el texto.");
            return;
        }
        if ("CLEAR_STYLE".equals(data.action)) {
            updatePreview(RichTextParser.plainText(RichTextParser.parse(data.text)), "Formato eliminado.");
            return;
        }
        Vector3d position;
        float size;
        try {
            position = new Vector3d(number(data.x), number(data.y), number(data.z));
            boolean textAction = "CREATE_TEXT".equals(data.action) || "PREVIEW_3D".equals(data.action);
            size = Float.parseFloat(text(textAction ? data.textSize : data.imageSize));
        } catch (RuntimeException exception) {
            show("Coordenadas o escala no validas.");
            return;
        }
        BillboardMode billboard = BillboardMode.parse(data.billboard);
        if ("PREVIEW_3D".equals(data.action)) {
            TextHologramService.SpawnResult result = TextHologramService.spawnPreview(store, data.text, size,
                position, suggestedYaw, billboard, TextAlignment.parse(data.alignment));
            show(result.success() ? "Vista previa 3D creada durante 12 segundos." : result.error());
            return;
        }
        if ("CREATE_TEXT".equals(data.action)) {
            TextHologramService.SpawnResult result = TextHologramService.spawnAt(store, data.key, data.text, size,
                position, suggestedYaw, billboard, TextAlignment.parse(data.alignment), TextRenderMode.parse(data.textMode));
            showResult(result.success(), result.id(), result.error());
        } else if ("CREATE_IMAGE".equals(data.action)) {
            ImageHologramService.SpawnResult result = ImageHologramService.spawnAt(store, data.key, data.source, size,
                position, suggestedYaw, billboard);
            showResult(result.success(), result.id(), result.error());
        }
    }

    private void showResult(boolean success, String id, String error) {
        show(success ? "Holograma creado: " + id : error);
    }

    private void show(String message) {
        UICommandBuilder update = new UICommandBuilder();
        update.set("#Status.Text", message == null ? "" : message);
        sendUpdate(update, false);
    }

    private void updatePreview(String text, String status) {
        String value = text == null ? "" : text;
        UICommandBuilder update = new UICommandBuilder();
        update.set("#TextInput.Value", value);
        update.set("#NativePreview.Text", nativePreview(value));
        if (status != null) update.set("#Status.Text", status);
        sendUpdate(update, false);
    }

    private static String nativePreview(String source) {
        // Label.Text is a System.String in Update 6. Passing a Message object makes
        // the client attempt to instantiate System.String from structured data and
        // disconnect with "No parameterless constructor defined for System.String".
        return RichTextParser.plainText(RichTextParser.parse(source));
    }

    private static String normalizeColor(String value) {
        String color = text(value).replace("#", "");
        return HEX_COLOR.matcher(color).matches() ? color.toLowerCase(Locale.ROOT) : null;
    }

    private static String normalizeEffect(String value) {
        RichTextParser.Effect effect = RichTextParser.Effect.parse(text(value));
        return effect == null ? "normal" : effect.name().toLowerCase(Locale.ROOT);
    }

    private void ensurePlacement(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (suggestedPosition != null) return;
        TextHologramService.Placement placement = TextHologramService.placement(store, ref, 1.5);
        if (placement == null) { suggestedPosition = new Vector3d(); suggestedYaw = 0; }
        else { suggestedPosition = placement.position(); suggestedYaw = placement.yaw(); }
    }

    private static String nextSuggestedKey(Store<EntityStore> store) {
        for (int index = 1; index < 1000; index++) {
            String key = "hologram" + index;
            if (!HologramLookup.exists(store, key)) return key;
        }
        return "hologram";
    }

    private static List<DropdownEntryInfo> enumEntries(List<String> values) {
        return values.stream().map(value -> new DropdownEntryInfo(LocalizableString.fromString(value.replace('_', ' ')), value)).toList();
    }

    private static double number(String value) {
        double parsed = Double.parseDouble(text(value));
        if (!Double.isFinite(parsed) || Math.abs(parsed) > 30_000_000) throw new NumberFormatException();
        return parsed;
    }

    private static String coordinate(double value) { return String.format(Locale.ROOT, "%.2f", value); }
    private static String text(String value) { return value == null ? "" : value.trim(); }

    public static final class PageData {
        static final BuilderCodec<PageData> CODEC = BuilderCodec.builder(PageData.class, PageData::new)
            .append(new KeyedCodec<>("Action", Codec.STRING), (data, value) -> data.action = value, data -> data.action).add()
            .append(new KeyedCodec<>("@Key", Codec.STRING, false), (data, value) -> data.key = value, data -> data.key).add()
            .append(new KeyedCodec<>("@Text", Codec.STRING, false), (data, value) -> data.text = value, data -> data.text).add()
            .append(new KeyedCodec<>("@TextSize", Codec.STRING, false), (data, value) -> data.textSize = value, data -> data.textSize).add()
            .append(new KeyedCodec<>("@Source", Codec.STRING, false), (data, value) -> data.source = value, data -> data.source).add()
            .append(new KeyedCodec<>("@ImageSize", Codec.STRING, false), (data, value) -> data.imageSize = value, data -> data.imageSize).add()
            .append(new KeyedCodec<>("@Billboard", Codec.STRING, false), (data, value) -> data.billboard = value, data -> data.billboard).add()
            .append(new KeyedCodec<>("@Alignment", Codec.STRING, false), (data, value) -> data.alignment = value, data -> data.alignment).add()
            .append(new KeyedCodec<>("@TextMode", Codec.STRING, false), (data, value) -> data.textMode = value, data -> data.textMode).add()
            .append(new KeyedCodec<>("@Color", Codec.STRING, false), (data, value) -> data.color = value, data -> data.color).add()
            .append(new KeyedCodec<>("@Color2", Codec.STRING, false), (data, value) -> data.color2 = value, data -> data.color2).add()
            .append(new KeyedCodec<>("@Effect", Codec.STRING, false), (data, value) -> data.effect = value, data -> data.effect).add()
            .append(new KeyedCodec<>("@X", Codec.STRING, false), (data, value) -> data.x = value, data -> data.x).add()
            .append(new KeyedCodec<>("@Y", Codec.STRING, false), (data, value) -> data.y = value, data -> data.y).add()
            .append(new KeyedCodec<>("@Z", Codec.STRING, false), (data, value) -> data.z = value, data -> data.z).add()
            .build();
        String action = "";
        String key = "";
        String text = "";
        String textSize = "1";
        String source = "builtin:orb";
        String imageSize = "2";
        String billboard = "BILLBOARD_Y";
        String alignment = "CENTER";
        String textMode = "AUTO";
        String color = "ff55ff";
        String color2 = "55ffff";
        String effect = "GLOW";
        String x = "0", y = "0", z = "0";
    }
}
