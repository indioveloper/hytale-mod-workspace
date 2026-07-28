package gg.orbgenesis.buildbattle;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerRule;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public final class RestrictBuildBattleCreativeToolsRule extends TriggerRule {
  public static final BuilderCodec<RestrictBuildBattleCreativeToolsRule> CODEC =
      BuilderCodec.builder(
              RestrictBuildBattleCreativeToolsRule.class,
              RestrictBuildBattleCreativeToolsRule::new,
              TriggerRule.BASE_CODEC)
          .build();
}
