package gg.orbgenesis.moretriggers;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerRule;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

/** Configurable movement-blocking rule for the Trigger Volume Always Active section. */
public final class NoMoveRule extends TriggerRule {
  public static final BuilderCodec<NoMoveRule> CODEC =
      BuilderCodec.builder(NoMoveRule.class, NoMoveRule::new, TriggerRule.BASE_CODEC)
          .append(
              new KeyedCodec<>("ExcludePlayers", Codec.BOOLEAN, false),
              (rule, value) -> rule.excludePlayers = value,
              rule -> rule.excludePlayers)
          .add()
          .append(
              new KeyedCodec<>("ExcludedNpcRoles", Codec.STRING_ARRAY, false),
              (rule, value) ->
                  rule.excludedNpcRoles = value != null ? value : new String[0],
              rule -> rule.excludedNpcRoles)
          .add()
          .build();

  public boolean excludePlayers;
  public String[] excludedNpcRoles = new String[0];
}
