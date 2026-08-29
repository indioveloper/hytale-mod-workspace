package gg.orbgenesis.moretriggers.signalloop;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.TaggedVolumeEffectUtil;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import gg.orbgenesis.moretriggers.MoreTriggersPlugin;

public final class ControlSignalLoopEffect extends TriggerEffect {
  private static final String[] EMPTY = new String[0];

  public static final BuilderCodec<ControlSignalLoopEffect> CODEC =
      BuilderCodec.builder(
              ControlSignalLoopEffect.class, ControlSignalLoopEffect::new, TriggerEffect.BASE_CODEC)
          .append(new KeyedCodec<>("Action", new EnumCodec<>(SignalLoopAction.class), false),
              (effect, value) -> effect.action = value, effect -> effect.action).add()
          .append(new KeyedCodec<>("LoopId", Codec.STRING, false),
              (effect, value) -> effect.loopId = value, effect -> effect.loopId).add()
          .append(new KeyedCodec<>("IntervalSeconds", Codec.DOUBLE, false),
              (effect, value) -> effect.intervalSeconds = value, effect -> effect.intervalSeconds).add()
          .append(new KeyedCodec<>("FirstPulse", new EnumCodec<>(SignalLoopFirstPulse.class), false),
              (effect, value) -> effect.firstPulse = value, effect -> effect.firstPulse).add()
          .append(new KeyedCodec<>("StartBehavior", new EnumCodec<>(SignalLoopStartBehavior.class), false),
              (effect, value) -> effect.startBehavior = value, effect -> effect.startBehavior).add()
          .append(new KeyedCodec<>("DurationSeconds", Codec.DOUBLE, false),
              (effect, value) -> effect.durationSeconds = value, effect -> effect.durationSeconds).add()
          .append(new KeyedCodec<>("MaxPulses", Codec.INTEGER, false),
              (effect, value) -> effect.maxPulses = value, effect -> effect.maxPulses).add()
          .append(new KeyedCodec<>("MatchKey", Codec.STRING, false),
              (effect, value) -> effect.matchKey = value, effect -> effect.matchKey).add()
          .append(new KeyedCodec<>("MatchValue", Codec.STRING, false),
              (effect, value) -> effect.matchValue = value, effect -> effect.matchValue).add()
          .append(new KeyedCodec<>("Radius", Codec.DOUBLE, false),
              (effect, value) -> effect.radius = value, effect -> effect.radius).add()
          .append(new KeyedCodec<>("Center", new EnumCodec<>(TaggedVolumeEffectUtil.Center.class), false),
              (effect, value) -> effect.center = value, effect -> effect.center).add()
          .append(new KeyedCodec<>("SignalKeys", Codec.STRING_ARRAY, false),
              (effect, value) -> effect.signalKeys = value != null ? value : EMPTY,
              effect -> effect.signalKeys.length == 0 ? null : effect.signalKeys).add()
          .append(new KeyedCodec<>("SignalValues", Codec.STRING_ARRAY, false),
              (effect, value) -> effect.signalValues = value != null ? value : EMPTY,
              effect -> effect.signalValues.length == 0 ? null : effect.signalValues).add()
          .append(new KeyedCodec<>("ContinueTagKey", Codec.STRING, false),
              (effect, value) -> effect.continueTagKey = value,
              effect -> effect.continueTagKey).add()
          .append(new KeyedCodec<>("ContinueTagValue", Codec.STRING, false),
              (effect, value) -> effect.continueTagValue = value,
              effect -> effect.continueTagValue).add()
          .build();

  private SignalLoopAction action = SignalLoopAction.START;
  private String loopId = "signal_loop";
  private double intervalSeconds = 5.0;
  private SignalLoopFirstPulse firstPulse = SignalLoopFirstPulse.IMMEDIATE;
  private SignalLoopStartBehavior startBehavior = SignalLoopStartBehavior.IGNORE_IF_RUNNING;
  private double durationSeconds;
  private int maxPulses;
  private String matchKey;
  private String matchValue;
  private double radius = 50.0;
  private TaggedVolumeEffectUtil.Center center = TaggedVolumeEffectUtil.Center.VOLUME;
  private String[] signalKeys = EMPTY;
  private String[] signalValues = EMPTY;
  private String continueTagKey;
  private String continueTagValue;

  @Override
  public void execute(TriggerContext context) {
    MoreTriggersPlugin plugin = MoreTriggersPlugin.get();
    if (plugin != null && context != null) {
      plugin.getSignalLoopManager().apply(this, context);
    }
  }

  SignalLoopAction action() { return action; }
  String loopId() { return loopId; }
  double intervalSeconds() { return intervalSeconds; }
  SignalLoopFirstPulse firstPulse() { return firstPulse; }
  SignalLoopStartBehavior startBehavior() { return startBehavior; }
  double durationSeconds() { return durationSeconds; }
  int maxPulses() { return maxPulses; }
  String matchKey() { return matchKey; }
  String matchValue() { return matchValue; }
  double radius() { return radius; }
  TaggedVolumeEffectUtil.Center center() { return center; }
  String[] signalKeys() { return signalKeys; }
  String[] signalValues() { return signalValues; }
  String continueTagKey() { return continueTagKey; }
  String continueTagValue() { return continueTagValue; }
}
