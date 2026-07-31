package gg.orbgenesis.scoreboards;

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
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;

public class ScoreboardsCommand extends AbstractCommandCollection {
  public ScoreboardsCommand(ScoreboardsPlugin plugin) {
    super("scoreboards", "Create and control editable native Objectives.");
    addAliases("objectivesedit");
    addSubCommand(new Ui(plugin));
    addSubCommand(new Edit(plugin));
    addSubCommand(new ListDefinitions(plugin));
    addSubCommand(new Info(plugin));
    addSubCommand(new Create(plugin));
    addSubCommand(new Delete(plugin));
    addSubCommand(new Start(plugin));
    addSubCommand(new SetValue(plugin, "set", ScoreboardManager.ModifyOperation.SET));
    addSubCommand(new SetValue(plugin, "add", ScoreboardManager.ModifyOperation.ADD));
    addSubCommand(
        new SetValue(plugin, "subtract", ScoreboardManager.ModifyOperation.SUBTRACT));
    addSubCommand(new Lifecycle(plugin, "show", Lifecycle.Action.SHOW));
    addSubCommand(new Lifecycle(plugin, "hide", Lifecycle.Action.HIDE));
    addSubCommand(new Lifecycle(plugin, "complete", Lifecycle.Action.COMPLETE));
    addSubCommand(new Lifecycle(plugin, "cancel", Lifecycle.Action.CANCEL));
  }

  private abstract static class PluginPlayerCommand extends AbstractPlayerCommand {
    protected final ScoreboardsPlugin plugin;

    protected PluginPlayerCommand(ScoreboardsPlugin plugin, String name, String description) {
      super(name, description);
      this.plugin = plugin;
    }

    protected void openList(
        Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef) {
      Player player = store.getComponent(ref, Player.getComponentType());
      if (player != null) {
        player.getPageManager().clearCustomPageAcknowledgements();
        player
            .getPageManager()
            .openCustomPage(ref, store, new ScoreboardListPage(playerRef, plugin));
      }
    }
  }

  public static class Ui extends PluginPlayerCommand {
    public Ui(ScoreboardsPlugin plugin) {
      super(plugin, "ui", "Open the Objective editor.");
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      openList(store, ref, playerRef);
    }
  }

  public static class Edit extends PluginPlayerCommand {
    private final RequiredArg<String> idArg =
        withRequiredArg("id", "Stable Objective definition ID.", ArgTypes.STRING);

    public Edit(ScoreboardsPlugin plugin) {
      super(plugin, "edit", "Edit an Objective definition in the UI.");
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      String id = idArg.get(context);
      ScoreboardDefinition definition = plugin.getManager().getDefinition(id);
      if (definition == null) {
        context.sendMessage(Message.raw("Unknown scoreboard: " + id));
        return;
      }
      Player player = store.getComponent(ref, Player.getComponentType());
      if (player != null) {
        player.getPageManager().clearCustomPageAcknowledgements();
        player
            .getPageManager()
            .openCustomPage(
                ref, store, new ScoreboardEditorPage(playerRef, plugin, definition));
      }
    }
  }

  public static class ListDefinitions extends PluginPlayerCommand {
    public ListDefinitions(ScoreboardsPlugin plugin) {
      super(plugin, "list", "List editable Objective definitions.");
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      List<ScoreboardDefinition> definitions = plugin.getManager().listDefinitions();
      if (definitions.isEmpty()) {
        context.sendMessage(Message.raw("No scoreboards defined."));
        return;
      }
      context.sendMessage(
          Message.raw(
              "Scoreboards: "
                  + String.join(
                      ", ", definitions.stream().map(ScoreboardDefinition::getId).toList())));
    }
  }

  public static class Create extends PluginPlayerCommand {
    private final RequiredArg<String> idArg =
        withRequiredArg("id", "Stable Objective definition ID.", ArgTypes.STRING);
    private final RequiredArg<List<String>> titleArg =
        withListRequiredArg("title", "Visible Objective title.", ArgTypes.STRING);

    public Create(ScoreboardsPlugin plugin) {
      super(plugin, "create", "Create an Objective with one editable score task.");
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      String id = ScoreboardIds.cleanDefinitionId(idArg.get(context));
      if (plugin.getManager().getDefinition(id) != null) {
        context.sendMessage(Message.raw("Scoreboard already exists: " + id));
        return;
      }
      String title = String.join(" ", titleArg.get(context)).trim();
      plugin
          .getManager()
          .upsertDefinition(
              new ScoreboardDefinition(
                  id,
                  title,
                  "",
                  new ScoreboardTaskDefinition[] {
                    new ScoreboardTaskDefinition("score", "Score", 0, 100)
                  }));
      context.sendMessage(Message.raw("Created scoreboard: " + id));
    }
  }

  public static class Info extends PluginPlayerCommand {
    private final RequiredArg<String> idArg =
        withRequiredArg("id", "Stable Objective definition ID.", ArgTypes.STRING);

    public Info(ScoreboardsPlugin plugin) {
      super(plugin, "info", "Explain an Objective and how its tasks are updated.");
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      String id = idArg.get(context);
      ScoreboardDefinition definition = plugin.getManager().getDefinition(id);
      if (definition == null) {
        context.sendMessage(Message.raw("Unknown scoreboard: " + id));
        return;
      }
      context.sendMessage(
          Message.raw(
              definition.getTitle()
                  + " ["
                  + definition.getId()
                  + "]: "
                  + (definition.getDescription().isBlank()
                      ? "No scoring description configured."
                      : definition.getDescription())));
      for (ScoreboardTaskDefinition task : definition.getTasks()) {
        context.sendMessage(
            Message.raw(
                "- "
                    + task.getLabel()
                    + " ["
                    + task.getId()
                    + "]: "
                    + task.getInitialValue()
                    + " -> "
                    + task.getGoal()
                    + " | /scoreboards add "
                    + definition.getId()
                    + " "
                    + task.getId()
                    + " <value>"));
      }
      context.sendMessage(
          Message.raw(
              "Automatic scoring is configured with Trigger Volume effect ModifyScoreboardTask."));
    }
  }

  public static class Delete extends PluginPlayerCommand {
    private final RequiredArg<String> idArg =
        withRequiredArg("id", "Objective definition ID.", ArgTypes.STRING);

    public Delete(ScoreboardsPlugin plugin) {
      super(plugin, "delete", "Delete an Objective definition and cancel its instances.");
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      String id = idArg.get(context);
      boolean deleted = plugin.getManager().deleteDefinition(id, store);
      context.sendMessage(
          Message.raw(deleted ? "Deleted scoreboard: " + id : "Unknown scoreboard: " + id));
    }
  }

  public static class Start extends PluginPlayerCommand {
    private final RequiredArg<String> idArg =
        withRequiredArg("id", "Objective definition ID.", ArgTypes.STRING);

    public Start(ScoreboardsPlugin plugin) {
      super(plugin, "start", "Start an individual Objective for yourself.");
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      boolean started =
          !plugin
              .getManager()
              .start(
                  idArg.get(context),
                  List.of(playerRef),
                  ObjectiveInstanceScope.INDIVIDUAL,
                  null,
                  store)
              .isEmpty();
      context.sendMessage(
          Message.raw(started ? "Objective started." : "Could not start that Objective."));
    }
  }

  public static class SetValue extends PluginPlayerCommand {
    private final RequiredArg<String> idArg =
        withRequiredArg("id", "Objective definition ID.", ArgTypes.STRING);
    private final RequiredArg<String> taskArg =
        withRequiredArg("task", "Stable task ID.", ArgTypes.STRING);
    private final RequiredArg<Integer> valueArg =
        withRequiredArg("value", "Integer value.", ArgTypes.INTEGER);
    private final ScoreboardManager.ModifyOperation operation;

    public SetValue(
        ScoreboardsPlugin plugin, String name, ScoreboardManager.ModifyOperation operation) {
      super(plugin, name, name + " an Objective task value.");
      this.operation = operation;
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      int changed =
          plugin
              .getManager()
              .modify(
                  idArg.get(context),
                  taskArg.get(context),
                  valueArg.get(context),
                  operation,
                  List.of(playerRef),
                  ObjectiveInstanceScope.INDIVIDUAL,
                  null,
                  store);
      context.sendMessage(
          Message.raw(changed > 0 ? "Objective updated." : "Active task not found."));
    }
  }

  public static class Lifecycle extends PluginPlayerCommand {
    private enum Action {
      SHOW,
      HIDE,
      COMPLETE,
      CANCEL
    }

    private final RequiredArg<String> idArg =
        withRequiredArg("id", "Objective definition ID.", ArgTypes.STRING);
    private final Action action;

    public Lifecycle(ScoreboardsPlugin plugin, String name, Action action) {
      super(plugin, name, name + " an individual Objective.");
      this.action = action;
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      int changed =
          switch (action) {
            case SHOW ->
                plugin
                    .getManager()
                    .setTracked(
                        idArg.get(context),
                        true,
                        List.of(playerRef),
                        ObjectiveInstanceScope.INDIVIDUAL,
                        null,
                        store);
            case HIDE ->
                plugin
                    .getManager()
                    .setTracked(
                        idArg.get(context),
                        false,
                        List.of(playerRef),
                        ObjectiveInstanceScope.INDIVIDUAL,
                        null,
                        store);
            case COMPLETE ->
                plugin
                    .getManager()
                    .finish(
                        idArg.get(context),
                        false,
                        List.of(playerRef),
                        ObjectiveInstanceScope.INDIVIDUAL,
                        null,
                        store);
            case CANCEL ->
                plugin
                    .getManager()
                    .finish(
                        idArg.get(context),
                        true,
                        List.of(playerRef),
                        ObjectiveInstanceScope.INDIVIDUAL,
                        null,
                        store);
          };
      context.sendMessage(
          Message.raw(changed > 0 ? "Objective updated." : "Objective instance not found."));
    }
  }
}
