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
import java.util.List;

public final class BuildBattleThemeVotePage
    extends InteractiveCustomUIPage<BuildBattleThemeVotePage.PageData> {
  private final BuildBattleManager manager;
  private final List<String> candidates;

  public BuildBattleThemeVotePage(
      PlayerRef playerRef, BuildBattleManager manager, List<String> candidates) {
    super(playerRef, CustomPageLifetime.CanDismiss, PageData.CODEC);
    this.manager = manager;
    this.candidates = List.copyOf(candidates);
  }

  @Override
  public void build(
      Ref<EntityStore> ref,
      UICommandBuilder commands,
      UIEventBuilder events,
      Store<EntityStore> store) {
    commands.append("Pages/BuildBattle/ThemeVote.ui");
    for (int index = 0; index < candidates.size(); index++) {
      String candidate = candidates.get(index);
      commands.append("#ThemeList", "Pages/BuildBattle/ThemeVoteRow.ui");
      String selector = "#ThemeList[" + index + "] #ThemeButton";
      commands.set(selector + ".Text", candidate);
      events.addEventBinding(
          CustomUIEventBindingType.Activating,
          selector,
          new EventData().append("Action", "Vote").append("Theme", candidate),
          false);
    }
  }

  @Override
  public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, PageData data) {
    if (data != null && "Vote".equals(data.action) && manager.voteTheme(playerRef.getUuid(), data.theme)) {
      playerRef.sendMessage(Message.raw("[Build Battle] Theme vote registered: " + data.theme));
      close();
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
                new KeyedCodec<>("Theme", Codec.STRING, false),
                (data, value) -> data.theme = value,
                data -> data.theme)
            .add()
            .build();

    private String action;
    private String theme;
  }
}
