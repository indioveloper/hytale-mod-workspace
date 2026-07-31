package gg.orbgenesis.scoreboards;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;

public class ScoreboardListPage
    extends InteractiveCustomUIPage<ScoreboardListPage.ListEventData> {
  private final ScoreboardsPlugin plugin;

  public ScoreboardListPage(PlayerRef playerRef, ScoreboardsPlugin plugin) {
    super(playerRef, CustomPageLifetime.CanDismiss, ListEventData.CODEC);
    this.plugin = plugin;
  }

  @Override
  public void build(
      Ref<EntityStore> ref,
      UICommandBuilder commands,
      UIEventBuilder events,
      Store<EntityStore> store) {
    commands.append("Pages/Scoreboards/ScoreboardList.ui");
    List<ScoreboardDefinition> definitions = plugin.getManager().listDefinitions();
    for (int i = 0; i < definitions.size(); i++) {
      ScoreboardDefinition definition = definitions.get(i);
      commands.append("#DefinitionList", "Pages/Scoreboards/ScoreboardListRow.ui");
      String row = "#DefinitionList[" + i + "]";
      commands.set(row + " #DefinitionTitle.Text", definition.getTitle());
      commands.set(row + " #DefinitionId.Text", definition.getId());
      commands.set(
          row + " #DefinitionDescription.Text",
          definition.getDescription().isBlank()
              ? "-"
              : definition.getDescription());
      commands.set(row + " #DefinitionTasks.Text", summarizeTasks(definition));
      commands.set(
          row + " #DefinitionScoring.Text",
          "/scoreboards add "
              + definition.getId()
              + " <task_id> <valor>  |  Trigger: ModifyScoreboardTask");
      bindRow(events, row + " #EditButton", "Edit", definition.getId());
      bindRow(events, row + " #StartButton", "Start", definition.getId());
    }
    events.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#NewButton",
        EventData.of("Action", "New"),
        false);
    events.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#CloseButton",
        EventData.of("Action", "Close"),
        false);
  }

  private void bindRow(
      UIEventBuilder events, String selector, String action, String definitionId) {
    events.addEventBinding(
        CustomUIEventBindingType.Activating,
        selector,
        new EventData()
            .append("Action", action)
            .append("DefinitionId", definitionId),
        false);
  }

  private String summarizeTasks(ScoreboardDefinition definition) {
    return String.join(
        "  |  ",
        java.util.Arrays.stream(definition.getTasks())
            .map(
                task ->
                    task.getId()
                        + ": "
                        + task.getLabel()
                        + " ("
                        + task.getInitialValue()
                        + " -> "
                        + task.getGoal()
                        + ")")
            .toList());
  }

  @Override
  public void handleDataEvent(
      Ref<EntityStore> ref, Store<EntityStore> store, ListEventData data) {
    if (data == null || data.action == null) {
      return;
    }
    switch (data.action) {
      case "Close" -> close();
      case "New" -> openEditor(ref, store, null);
      case "Edit" -> {
        ScoreboardDefinition definition =
            plugin.getManager().getDefinition(data.definitionId);
        if (definition != null) {
          openEditor(ref, store, definition);
        }
      }
      case "Start" -> {
        plugin
            .getManager()
            .start(
                data.definitionId,
                List.of(playerRef),
                ObjectiveInstanceScope.INDIVIDUAL,
                null,
                store);
        close();
      }
      default -> {}
    }
  }

  private void openEditor(
      Ref<EntityStore> ref, Store<EntityStore> store, ScoreboardDefinition definition) {
    Player player = store.getComponent(ref, Player.getComponentType());
    if (player != null) {
      player.getPageManager().clearCustomPageAcknowledgements();
      player
          .getPageManager()
          .openCustomPage(
              ref, store, new ScoreboardEditorPage(playerRef, plugin, definition));
    }
  }

  public static class ListEventData {
    public static final BuilderCodec<ListEventData> CODEC =
        BuilderCodec.builder(ListEventData.class, ListEventData::new)
            .append(
                new KeyedCodec<>("Action", Codec.STRING, false),
                (data, value) -> data.action = value,
                data -> data.action)
            .add()
            .append(
                new KeyedCodec<>("DefinitionId", Codec.STRING, false),
                (data, value) -> data.definitionId = value,
                data -> data.definitionId)
            .add()
            .build();

    public String action;
    public String definitionId;
  }
}
