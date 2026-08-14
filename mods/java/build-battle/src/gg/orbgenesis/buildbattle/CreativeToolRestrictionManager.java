package gg.orbgenesis.buildbattle;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolsEnabledTools;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BuilderTool;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public final class CreativeToolRestrictionManager {
  private static final long EXPIRY_NANOS = 1_500_000_000L;
  private static final long CATALOG_REFRESH_NANOS = 1_000_000_000L;

  private static final Set<String> ALLOWED_ITEM_IDS =
      Set.of(
          "EditorTool_Entity",
          "EditorTool_Paint",
          "EditorTool_Sculpt",
          "EditorTool_Boulder",
          "EditorTool_Cave",
          "EditorTool_Mountain",
          "EditorTool_Tentacle",
          "EditorTool_Decoration",
          "EditorTool_Forest",
          "EditorTool_GrassBrush",
          "EditorTool_Hotsprings",
          "EditorTool_Path",
          "EditorTool_River",
          "EditorTool_Spiral",
          "EditorTool_Noise",
          "EditorTool_Revolve",
          "EditorTool_Scatter",
          "EditorTool_Tint",
          "EditorTool_Layers");

  private static final Set<String> ALLOWED_TOOL_IDS =
      Set.of("Entity", "Paint", "Sculpt", "Noise", "Revolve", "Scatter", "Tint", "Layers");

  private final Path recoveryFile;
  private final HytaleLogger logger;
  private final Map<UUID, RestrictionState> active = new HashMap<>();

  public CreativeToolRestrictionManager(Path dataDirectory, HytaleLogger logger) {
    this.recoveryFile = dataDirectory.resolve("active-creative-tool-restrictions.txt");
    this.logger = logger;
  }

  public synchronized void recoverStaleRestrictions() {
    Map<UUID, Set<String>> stale = readRecoveryFile();
    PermissionsModule permissions = PermissionsModule.get();
    for (Map.Entry<UUID, Set<String>> entry : stale.entrySet()) {
      permissions.removeUserPermission(entry.getKey(), entry.getValue());
    }
    if (!stale.isEmpty()) {
      logger.at(Level.INFO).log(
          "Restored creative tool permissions for %d player(s) after an unclean stop.",
          stale.size());
    }
    writeRecoveryFile();
  }

  public synchronized void refresh(
      PlayerRef playerRef, Ref<EntityStore> entityRef, Store<EntityStore> store) {
    UUID playerId = playerRef.getUuid();
    long now = System.nanoTime();
    RestrictionState state = active.get(playerId);

    if (state == null) {
      Set<String> addedPermissions = applyPermissions(playerId);
      state =
          new RestrictionState(
              playerId, playerRef, entityRef, store, addedPermissions, now, 0L);
      active.put(playerId, state);
      writeRecoveryFile();
    } else {
      state.playerRef = playerRef;
      state.entityRef = entityRef;
      state.store = store;
      state.lastRefreshNanos = now;
    }

    scrubForbiddenBuilderTools(state);
    if (now - state.lastCatalogSentNanos >= CATALOG_REFRESH_NANOS) {
      sendRestrictedCatalog(playerRef);
      state.lastCatalogSentNanos = now;
    }
  }

  public synchronized void tick(Store<EntityStore> store) {
    long now = System.nanoTime();
    List<UUID> expired = new ArrayList<>();
    for (RestrictionState state : active.values()) {
      if (state.store != store) {
        continue;
      }
      if (now - state.lastRefreshNanos > EXPIRY_NANOS
          || state.entityRef == null
          || !state.entityRef.isValid()) {
        expired.add(state.playerId);
      } else {
        scrubForbiddenBuilderTools(state);
      }
    }
    for (UUID playerId : expired) {
      restore(playerId);
    }
  }

  public synchronized void restoreAll() {
    for (UUID playerId : new ArrayList<>(active.keySet())) {
      restore(playerId);
    }
  }

  private Set<String> applyPermissions(UUID playerId) {
    PermissionsModule permissions = PermissionsModule.get();
    PermissionProvider provider = permissions.getFirstPermissionProvider();
    Set<String> existing = provider.getUserPermissions(playerId);
    Set<String> required = new HashSet<>();
    required.add("-" + HytalePermissions.BUILDER_TOOLS_EDITOR.getId());
    required.add("-" + HytalePermissions.toolPermission("*"));
    required.add(HytalePermissions.EDITOR_BRUSH_USE.getId());
    required.add(HytalePermissions.EDITOR_BRUSH_CONFIG.getId());
    for (String toolId : ALLOWED_TOOL_IDS) {
      required.add(HytalePermissions.toolPermission(toolId));
    }

    Set<String> added = new HashSet<>(required);
    if (existing != null) {
      added.removeAll(existing);
    }

    if (!added.isEmpty()) {
      // Record first so an abrupt stop can never leave an untracked restriction.
      RestrictionState pending =
          new RestrictionState(playerId, null, null, null, added, System.nanoTime(), 0L);
      active.put(playerId, pending);
      writeRecoveryFile();
      permissions.addUserPermission(playerId, added);
      active.remove(playerId);
    }
    return added;
  }

  public synchronized void restore(UUID playerId) {
    RestrictionState state = active.remove(playerId);
    if (state == null) {
      return;
    }
    if (!state.addedPermissions.isEmpty()) {
      PermissionsModule.get().removeUserPermission(playerId, state.addedPermissions);
    }
    if (state.playerRef != null && state.playerRef.isValid()) {
      sendCurrentCatalog(state.playerRef);
    }
    writeRecoveryFile();
  }

  private void sendRestrictedCatalog(PlayerRef playerRef) {
    playerRef
        .getPacketHandler()
        .write(new BuilderToolsEnabledTools(ALLOWED_ITEM_IDS.toArray(String[]::new)));
  }

  private void sendCurrentCatalog(PlayerRef playerRef) {
    PermissionsModule permissions = PermissionsModule.get();
    UUID playerId = playerRef.getUuid();
    boolean hasEditorAccess =
        permissions.hasPermission(playerId, HytalePermissions.BUILDER_TOOLS_EDITOR);
    List<String> enabled = new ArrayList<>();

    for (Map.Entry<String, Item> entry : Item.getAssetMap().getAssetMap().entrySet()) {
      String itemId = entry.getKey();
      BuilderTool builderTool = entry.getValue().getBuilderTool();
      if (itemId == null
          || builderTool == null
          || !builderTool.isSurvivalAllowed()
          || enabled.contains(itemId)) {
        continue;
      }
      String toolId = builderTool.getId();
      if (hasEditorAccess
          || (toolId != null
              && permissions.hasPermission(
                  playerId, HytalePermissions.toolPermission(toolId)))) {
        enabled.add(itemId);
      }
    }

    playerRef
        .getPacketHandler()
        .write(new BuilderToolsEnabledTools(enabled.toArray(String[]::new)));
  }

  private void scrubForbiddenBuilderTools(RestrictionState state) {
    if (state.entityRef == null || !state.entityRef.isValid() || state.store == null) {
      return;
    }
    scrubComponent(state, InventoryComponent.Hotbar.getComponentType());
    scrubComponent(state, InventoryComponent.Storage.getComponentType());
    scrubComponent(state, InventoryComponent.Utility.getComponentType());
    scrubComponent(state, InventoryComponent.Tool.getComponentType());
    scrubComponent(state, InventoryComponent.Backpack.getComponentType());
  }

  private <T extends InventoryComponent> void scrubComponent(
      RestrictionState state, ComponentType<EntityStore, T> componentType) {
    T component = state.store.getComponent(state.entityRef, componentType);
    if (component == null) {
      return;
    }

    ItemContainer container = component.getInventory();
    List<Short> forbiddenSlots = new ArrayList<>();
    container.forEach(
        (slot, stack) -> {
          if (isForbiddenBuilderTool(stack)) {
            forbiddenSlots.add(slot);
          }
        });
    for (short slot : forbiddenSlots) {
      container.removeItemStackFromSlot(slot);
    }
  }

  private boolean isForbiddenBuilderTool(ItemStack stack) {
    if (stack == null || stack.isEmpty() || ALLOWED_ITEM_IDS.contains(stack.getItemId())) {
      return false;
    }
    Item item = stack.getItem();
    return item != null && item.getBuilderTool() != null;
  }

  private Map<UUID, Set<String>> readRecoveryFile() {
    Map<UUID, Set<String>> result = new HashMap<>();
    if (!Files.isRegularFile(recoveryFile)) {
      return result;
    }
    try {
      for (String line : Files.readAllLines(recoveryFile, StandardCharsets.UTF_8)) {
        int separator = line.indexOf('|');
        if (separator <= 0 || separator == line.length() - 1) {
          continue;
        }
        UUID playerId = UUID.fromString(line.substring(0, separator));
        result
            .computeIfAbsent(playerId, ignored -> new HashSet<>())
            .add(line.substring(separator + 1));
      }
    } catch (IOException | IllegalArgumentException exception) {
      logger.at(Level.WARNING).withCause(exception).log(
          "Could not read the creative tool restriction recovery file.");
    }
    return result;
  }

  private void writeRecoveryFile() {
    try {
      Files.createDirectories(recoveryFile.getParent());
      Path temporary = recoveryFile.resolveSibling(recoveryFile.getFileName() + ".tmp");
      List<String> lines = new ArrayList<>();
      for (RestrictionState state : active.values()) {
        for (String permission : state.addedPermissions) {
          lines.add(state.playerId + "|" + permission);
        }
      }
      Files.write(temporary, lines, StandardCharsets.UTF_8);
      Files.move(
          temporary,
          recoveryFile,
          StandardCopyOption.REPLACE_EXISTING,
          StandardCopyOption.ATOMIC_MOVE);
    } catch (IOException exception) {
      logger.at(Level.WARNING).withCause(exception).log(
          "Could not update the creative tool restriction recovery file.");
    }
  }

  private static final class RestrictionState {
    private final UUID playerId;
    private final Set<String> addedPermissions;
    private PlayerRef playerRef;
    private Ref<EntityStore> entityRef;
    private Store<EntityStore> store;
    private long lastRefreshNanos;
    private long lastCatalogSentNanos;

    private RestrictionState(
        UUID playerId,
        PlayerRef playerRef,
        Ref<EntityStore> entityRef,
        Store<EntityStore> store,
        Set<String> addedPermissions,
        long lastRefreshNanos,
        long lastCatalogSentNanos) {
      this.playerId = playerId;
      this.playerRef = playerRef;
      this.entityRef = entityRef;
      this.store = store;
      this.addedPermissions = Set.copyOf(addedPermissions);
      this.lastRefreshNanos = lastRefreshNanos;
      this.lastCatalogSentNanos = lastCatalogSentNanos;
    }
  }
}
