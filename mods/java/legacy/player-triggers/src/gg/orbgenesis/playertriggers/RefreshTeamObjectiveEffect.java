package gg.orbgenesis.playertriggers;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class RefreshTeamObjectiveEffect extends TriggerEffect {
  public static final BuilderCodec<RefreshTeamObjectiveEffect> CODEC =
      BuilderCodec.builder(
              RefreshTeamObjectiveEffect.class,
              RefreshTeamObjectiveEffect::new,
              TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("Title", Codec.STRING, false),
              (effect, value) -> effect.title = value,
              effect -> effect.title)
          .add()
          .append(
              new KeyedCodec<>("LineId", Codec.STRING, false),
              (effect, value) -> effect.lineId = value,
              effect -> effect.lineId)
          .add()
          .append(
              new KeyedCodec<>("TeamTag", Codec.STRING, false),
              (effect, value) -> effect.teamTag = value,
              effect -> effect.teamTag)
          .add()
          .append(
              new KeyedCodec<>("TeamValues", Codec.STRING, false),
              (effect, value) -> effect.teamValues = value,
              effect -> effect.teamValues)
          .add()
          .append(
              new KeyedCodec<>("TeamLabels", Codec.STRING, false),
              (effect, value) -> effect.teamLabels = value,
              effect -> effect.teamLabels)
          .add()
          .append(
              new KeyedCodec<>("ShowUnassigned", Codec.BOOLEAN, false),
              (effect, value) -> effect.showUnassigned = value,
              effect -> effect.showUnassigned)
          .add()
          .append(
              new KeyedCodec<>("TeamCapacity", Codec.INTEGER, false),
              (effect, value) -> effect.teamCapacity = value,
              effect -> effect.teamCapacity)
          .add()
          .build();

  private String title = TeamScoreboardService.DEFAULT_TITLE;
  private String lineId = TeamScoreboardService.DEFAULT_LINE_ID;
  private String teamTag = TeamScoreboardService.DEFAULT_TEAM_TAG;
  private String teamValues = TeamScoreboardService.DEFAULT_TEAM_VALUES;
  private String teamLabels = TeamScoreboardService.DEFAULT_TEAM_LABELS;
  private boolean showUnassigned = false;
  private int teamCapacity = TeamScoreboardService.DEFAULT_TEAM_CAPACITY;

  @Override
  public void execute(TriggerContext context) {
    TeamScoreboardService.refresh(
        context.getStore(),
        title,
        lineId,
        teamTag,
        teamValues,
        teamLabels,
        showUnassigned,
        teamCapacity);
  }
}
