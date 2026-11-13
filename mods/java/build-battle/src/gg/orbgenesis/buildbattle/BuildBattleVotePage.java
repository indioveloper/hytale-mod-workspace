package gg.orbgenesis.buildbattle;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class BuildBattleVotePage
    extends InteractiveCustomUIPage<BuildBattleVotePage.PageData> {
  private final BuildBattleManager manager;
  private final String targetName;

  public BuildBattleVotePage(
      PlayerRef playerRef, BuildBattleManager manager, String targetName) {
    super(playerRef, CustomPageLifetime.CanDismiss, PageData.CODEC);
    this.manager = manager;
    this.targetName = targetName;
  }

  @Override
  public void build(
      Ref<EntityStore> ref,
      UICommandBuilder commands,
      UIEventBuilder events,
      Store<EntityStore> store) {
    commands.append("Pages/BuildBattle/BuildVote.ui");
    commands.set("#TargetName.Text", targetName);
    for (int score = 1; score <= 5; score++) {
      events.addEventBinding(
          CustomUIEventBindingType.Activating,
          "#Score" + score,
          new EventData().append("Action", "Vote").append("Score", Integer.toString(score)),
          false);
    }
  }

  @Override
  public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, PageData data) {
    if (data == null || !"Vote".equals(data.action)) {
      return;
    }
    try {
      int score = Integer.parseInt(data.score);
      if (manager.voteBuild(playerRef.getUuid(), score)) {
        playerRef.sendMessage(Message.raw("[Build Battle] Vote registered: " + score + "/5"));
        close();
      }
    } catch (NumberFormatException ignored) {
      // Malformed client events are ignored.
    }
  }

  public static final class PageData {
    static final BuilderCodec<PageData> CODEC =
        BuilderCodec.builder(PageData.class, PageData::new)
            .append(
                new KeyedCodec<>("Action", Codec.STRING, false),
                (data, value) -> data.action = value,
                data -> data.action)
            .add()
            .append(
                new KeyedCodec<>("Score", Codec.STRING, false),
                (data, value) -> data.score = value,
                data -> data.score)
            .add()
            .build();

    private String action;
    private String score;
  }
}
