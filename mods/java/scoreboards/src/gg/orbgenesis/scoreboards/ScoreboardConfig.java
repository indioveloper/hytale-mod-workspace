package gg.orbgenesis.scoreboards;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;

public class ScoreboardConfig {
  public static final int MAX_TASKS = 5;

  public static final BuilderCodec<ScoreboardConfig> CODEC =
      BuilderCodec.builder(ScoreboardConfig.class, ScoreboardConfig::new)
          .append(
              new KeyedCodec<>(
                  "Definitions",
                  new ArrayCodec<>(ScoreboardDefinition.CODEC, ScoreboardDefinition[]::new),
                  false),
              (value, field) -> value.definitions = field,
              value -> value.definitions)
          .add()
          .build();

  private ScoreboardDefinition[] definitions = {
    new ScoreboardDefinition(
        "main",
        "Scoreboard",
        "Actualiza score con /scoreboards add main score <valor> o ModifyScoreboardTask.",
        new ScoreboardTaskDefinition[] {
          new ScoreboardTaskDefinition("score", "Score", 0, 100)
        })
  };

  public ScoreboardDefinition[] getDefinitions() {
    return definitions;
  }

  public void setDefinitions(ScoreboardDefinition[] definitions) {
    this.definitions = definitions == null ? new ScoreboardDefinition[0] : definitions;
  }
}
