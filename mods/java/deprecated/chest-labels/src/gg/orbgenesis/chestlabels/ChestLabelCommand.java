package gg.orbgenesis.chestlabels;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import org.joml.Vector3i;

public class ChestLabelCommand extends AbstractCommandCollection {
  private final ChestLabelsPlugin plugin;

  public ChestLabelCommand(ChestLabelsPlugin plugin) {
    super("chestlabel", "Set, remove, or inspect custom labels for the chest you are aiming at.");
    this.plugin = plugin;
    addSubCommand(new Set(plugin));
    addSubCommand(new Edit(plugin));
    addSubCommand(new Clear(plugin));
    addSubCommand(new Icons());
  }

  public static class Set extends AbstractPlayerCommand {
    private static final Message NO_CONTAINER =
        Message.raw("Look at a storage container first.");
    private static final Message UPDATED =
        Message.raw("Chest label updated.");
    private static final Message USAGE =
        Message.raw("Usage: /chestlabel set <name> [icon]");

    private final ChestLabelsPlugin plugin;
    private final RequiredArg<List<String>> partsArg =
        withListRequiredArg("parts", "Visible chest name followed by an optional icon key.", ArgTypes.STRING);

    public Set(ChestLabelsPlugin plugin) {
      super("set", "Set the custom name and optional icon on the chest you are aiming at. Example: /chestlabel set Botin star");
      this.plugin = plugin;
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      Ref<ChunkStore> containerRef = ChestLabelTargeting.getTargetContainerRef(ref, world, store);
      if (containerRef == null) {
        context.sendMessage(NO_CONTAINER);
        return;
      }

      ParsedInput parsed = parse(partsArg.get(context));
      if (parsed == null) {
        context.sendMessage(USAGE);
        return;
      }

      ChestLabelComponent component =
          containerRef.getStore().ensureAndGetComponent(containerRef, plugin.getChestLabelComponentType());
      component.set(parsed.name, parsed.iconKey);
      context.sendMessage(UPDATED);
    }

    private ParsedInput parse(List<String> parts) {
      if (parts == null || parts.isEmpty()) {
        return null;
      }

      String iconKey = null;
      int nameEnd = parts.size();
      if (parts.size() > 1) {
        String last = parts.get(parts.size() - 1);
        String normalized = ChestIconRegistry.normalizeKey(last);
        if (normalized.equalsIgnoreCase(last)) {
          iconKey = normalized;
          nameEnd--;
        }
      }

      if (nameEnd <= 0) {
        return null;
      }

      String name = String.join(" ", parts.subList(0, nameEnd)).trim();
      if (name.isEmpty()) {
        return null;
      }

      return new ParsedInput(name, iconKey);
    }

    private static final class ParsedInput {
      private final String name;
      private final String iconKey;

      private ParsedInput(String name, String iconKey) {
        this.name = name;
        this.iconKey = iconKey;
      }
    }
  }

  public static class Edit extends AbstractPlayerCommand {
    private static final Message NO_CONTAINER =
        Message.raw("Look at a storage container first.");
    private static final Message OPEN_FAILED =
        Message.raw("Could not open the chest label editor right now.");

    private final ChestLabelsPlugin plugin;

    public Edit(ChestLabelsPlugin plugin) {
      super("edit", "Open a native chest label editor for the chest you are aiming at.");
      this.plugin = plugin;
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      Ref<ChunkStore> containerRef = ChestLabelTargeting.getTargetContainerRef(ref, world, store);
      if (containerRef == null) {
        context.sendMessage(NO_CONTAINER);
        return;
      }

      Player player = store.getComponent(ref, Player.getComponentType());
      if (player == null) {
        context.sendMessage(OPEN_FAILED);
        return;
      }

      ChestLabelComponent component =
          containerRef.getStore().getComponent(containerRef, plugin.getChestLabelComponentType());
      Vector3i blockPos = ChestLabelTargeting.getTargetBlock(ref, store);
      if (blockPos == null) {
        context.sendMessage(OPEN_FAILED);
        return;
      }

      ChestLabelEditorPage page =
          new ChestLabelEditorPage(
              plugin,
              playerRef,
              blockPos,
              component == null ? "" : component.getName(),
              component == null ? ChestIconRegistry.DEFAULT_ICON_KEY : component.getIcon());
      player.getPageManager().openCustomPage(ref, store, page);
    }
  }

  public static class Clear extends AbstractPlayerCommand {
    private static final Message NO_CONTAINER =
        Message.raw("Look at a storage container first.");
    private static final Message CLEARED =
        Message.raw("Chest label cleared.");

    private final ChestLabelsPlugin plugin;

    public Clear(ChestLabelsPlugin plugin) {
      super("clear", "Remove the custom label from the chest you are aiming at.");
      this.plugin = plugin;
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      Ref<ChunkStore> containerRef = ChestLabelTargeting.getTargetContainerRef(ref, world, store);
      if (containerRef == null) {
        context.sendMessage(NO_CONTAINER);
        return;
      }

      containerRef.getStore().tryRemoveComponent(containerRef, plugin.getChestLabelComponentType());
      context.sendMessage(CLEARED);
    }
  }

  public static class Icons extends AbstractPlayerCommand {
    public Icons() {
      super("icons", "List built-in icon keys for chest labels.");
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      context.sendMessage(Message.raw("Available icons: " + ChestIconRegistry.listIcons()));
    }
  }
}
