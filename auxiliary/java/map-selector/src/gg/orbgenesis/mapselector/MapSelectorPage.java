package gg.orbgenesis.mapselector;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolPrefabPreview;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.EditorBlocksChange;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class MapSelectorPage
    extends InteractiveCustomUIPage<MapSelectorPage.MapSelectorEventData> {
  private static final int PREVIEW_TILT = 23;
  private static final int PREVIEW_SPIN_SPEED = 27;
  private static final int PREVIEW_SCALE = 100;
  private static final int DEFAULT_BIOME_TINT = 0xFFFFFF;
  private static final int DEFAULT_WATER_TINT = 0x4A90E2;

  private final MapSelectorPlugin plugin;
  private MapDefinition selectedMap;

  public MapSelectorPage(
      MapSelectorPlugin plugin, PlayerRef playerRef, MapDefinition initialMap) {
    super(playerRef, CustomPageLifetime.CanDismiss, MapSelectorEventData.CODEC);
    this.plugin = plugin;
    this.selectedMap = initialMap == null ? MapDefinition.MAP_1 : initialMap;
  }

  @Override
  public void build(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull UICommandBuilder commandBuilder,
      @Nonnull UIEventBuilder eventBuilder,
      @Nonnull Store<EntityStore> store) {
    commandBuilder.append("Pages/MapSelectorPage.ui");
    updateSelectionLabels(commandBuilder);

    bindMapButton(eventBuilder, "#Map1Button", MapDefinition.MAP_1);
    bindMapButton(eventBuilder, "#Map2Button", MapDefinition.MAP_2);
    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#GoButton",
        EventData.of(MapSelectorEventData.KEY_ACTION, MapSelectorEventData.ACTION_GO),
        false);
    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#CloseButton",
        EventData.of(MapSelectorEventData.KEY_ACTION, MapSelectorEventData.ACTION_CLOSE),
        false);
  }

  @Override
  public void handleDataEvent(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Store<EntityStore> store,
      @Nonnull MapSelectorEventData data) {
    if (MapSelectorEventData.ACTION_SELECT.equals(data.action)) {
      MapDefinition requestedMap = MapDefinition.fromEventValue(data.map);
      if (requestedMap != null) {
        selectedMap = requestedMap;
        if (sendPreview(selectedMap)) {
          UICommandBuilder commands = new UICommandBuilder();
          updateSelectionLabels(commands);
          sendUpdate(commands, null, false);
        }
      }
      return;
    }

    if (MapSelectorEventData.ACTION_GO.equals(data.action)) {
      plugin.selectMap(playerRef.getUuid(), selectedMap);
      clearPreview();
      close();
      teleportToSelectedMap(ref, store);
      return;
    }

    if (MapSelectorEventData.ACTION_CLOSE.equals(data.action)) {
      clearPreview();
      close();
    }
  }

  @Override
  public void onDismiss(
      @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
    clearPreview();
  }

  public void showInitialPreview() {
    sendPreview(selectedMap);
  }

  private void bindMapButton(
      UIEventBuilder eventBuilder, String selector, MapDefinition map) {
    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        selector,
        new EventData()
            .append(MapSelectorEventData.KEY_ACTION, MapSelectorEventData.ACTION_SELECT)
            .append(MapSelectorEventData.KEY_MAP, map.name()),
        false);
  }

  private void updateSelectionLabels(UICommandBuilder commands) {
    commands.set("#SelectedMap.Text", selectedMap.getDisplayName());
    commands.set(
        "#Map1Marker.Text", selectedMap == MapDefinition.MAP_1 ? ">" : "");
    commands.set(
        "#Map2Marker.Text", selectedMap == MapDefinition.MAP_2 ? ">" : "");
  }

  private void teleportToSelectedMap(
      Ref<EntityStore> ref, Store<EntityStore> store) {
    Vector3d destination =
        new Vector3d(
            selectedMap.getDestinationX(),
            selectedMap.getDestinationY(),
            selectedMap.getDestinationZ());
    Teleport teleport =
        Teleport.createForPlayer(destination, playerRef.getTransform().getRotation())
            .setHeadRotation(playerRef.getHeadRotation());
    store.addComponent(ref, Teleport.getComponentType(), teleport);
    playerRef.sendMessage(
        Message.raw(
            "Viajando a " + selectedMap.getDisplayName() + " ("
                + formatCoordinate(selectedMap.getDestinationX()) + ", "
                + formatCoordinate(selectedMap.getDestinationY()) + ", "
                + formatCoordinate(selectedMap.getDestinationZ()) + ")."));
  }

  private String formatCoordinate(double coordinate) {
    return coordinate == Math.rint(coordinate)
        ? Long.toString((long) coordinate)
        : Double.toString(coordinate);
  }

  private boolean sendPreview(MapDefinition map) {
    PrefabStore prefabStore = PrefabStore.get();
    Path prefabPath = prefabStore.findAssetPrefabPath(map.getPrefabPath());
    if (prefabPath == null) {
      playerRef.sendMessage(
          Message.raw(
              "No se encontro el prefab '" + map.getPrefabPath()
                  + "'. Comprueba que el Asset Pack tests:tests este habilitado."));
      clearPreview();
      return false;
    }

    BlockSelection selection = prefabStore.getPrefab(prefabPath);
    if (selection == null) {
      playerRef.sendMessage(
          Message.raw("No se pudo cargar el prefab '" + map.getPrefabPath() + "'."));
      clearPreview();
      return false;
    }

    EditorBlocksChange selectionPacket = selection.toPacket();
    BuilderToolPrefabPreview preview = new BuilderToolPrefabPreview();
    preview.tilt = PREVIEW_TILT;
    preview.spinSpeed = PREVIEW_SPIN_SPEED;
    preview.previewScale = PREVIEW_SCALE;
    preview.biomeTint = DEFAULT_BIOME_TINT;
    preview.waterTint = DEFAULT_WATER_TINT;
    preview.blocksChange = selectionPacket.blocksChange;
    preview.fluidsChange = selectionPacket.fluidsChange;
    preview.entityChanges = selectionPacket.entityChanges;
    playerRef.getPacketHandler().write(preview);
    return true;
  }

  private void clearPreview() {
    playerRef.getPacketHandler().write(new BuilderToolPrefabPreview());
  }

  public static final class MapSelectorEventData {
    static final String KEY_ACTION = "Action";
    static final String KEY_MAP = "Map";

    static final String ACTION_SELECT = "Select";
    static final String ACTION_GO = "Go";
    static final String ACTION_CLOSE = "Close";

    static final BuilderCodec<MapSelectorEventData> CODEC =
        BuilderCodec.builder(MapSelectorEventData.class, MapSelectorEventData::new)
            .append(
                new KeyedCodec<>(KEY_ACTION, Codec.STRING),
                (entry, value) -> entry.action = value,
                entry -> entry.action)
            .add()
            .append(
                new KeyedCodec<>(KEY_MAP, Codec.STRING),
                (entry, value) -> entry.map = value,
                entry -> entry.map)
            .add()
            .build();

    private String action;
    private String map;
  }
}
