package gg.raynormods.desertsurvival.cutterray;

import com.hypixel.hytale.builtin.beam.BeamComponent;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import javax.annotation.Nonnull;

public final class CutterrayBeamOffInteraction extends SimpleInstantInteraction {
  public static final BuilderCodec<CutterrayBeamOffInteraction> CODEC =
      BuilderCodec.builder(
              CutterrayBeamOffInteraction.class,
              CutterrayBeamOffInteraction::new,
              SimpleInstantInteraction.CODEC)
          .documentation("Removes the temporary cutterray beam from its source entity.")
          .build();

  @Override
  protected void firstRun(
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nonnull CooldownHandler cooldownHandler) {
    var source = context.getEntity();
    var commandBuffer = context.getCommandBuffer();
    if (source == null || !source.isValid() || commandBuffer == null) {
      return;
    }
    commandBuffer.tryRemoveComponent(source, BeamComponent.getComponentType());
  }
}
