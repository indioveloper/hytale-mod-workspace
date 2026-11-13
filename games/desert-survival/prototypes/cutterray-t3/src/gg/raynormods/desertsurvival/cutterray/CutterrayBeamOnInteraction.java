package gg.raynormods.desertsurvival.cutterray;

import com.hypixel.hytale.builtin.beam.AttachedBeam;
import com.hypixel.hytale.builtin.beam.BeamComponent;
import com.hypixel.hytale.builtin.beam.asset.Beam;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

public final class CutterrayBeamOnInteraction extends SimpleBlockInteraction {
  public static final BuilderCodec<CutterrayBeamOnInteraction> CODEC =
      BuilderCodec.builder(
              CutterrayBeamOnInteraction.class,
              CutterrayBeamOnInteraction::new,
              SimpleBlockInteraction.CODEC)
          .documentation("Draws a short cutterray beam from the held tool to the target block.")
          .build();

  private static final String BEAM_ID = "Cutterray_Cyan";
  private static final String SOURCE_NODE = "R-Attachment";
  private static final float SCALE = 0.21f;
  private static final Vector3f SOURCE_OFFSET = new Vector3f(0.0f, 0.0f, 0.85f);
  private static final int EXPIRY_TICKS = 12;

  @Override
  protected void interactWithBlock(
      @Nonnull World world,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nullable ItemStack itemInHand,
      @Nonnull Vector3i targetBlock,
      @Nonnull CooldownHandler cooldownHandler) {
    var source = context.getEntity();
    if (source == null || !source.isValid()) {
      return;
    }
    int beamIndex = Beam.getAssetMap().getIndex(BEAM_ID);
    if (beamIndex < 0) {
      return;
    }
    var target = new Vector3d(
        targetBlock.x + 0.5,
        targetBlock.y + 0.5,
        targetBlock.z + 0.5);
    var beam = AttachedBeam.toPosition(
        beamIndex,
        SCALE,
        SOURCE_NODE,
        SOURCE_OFFSET,
        target);
    commandBuffer.putComponent(
        source,
        BeamComponent.getComponentType(),
        new BeamComponent(beam));
    commandBuffer.putComponent(
        source,
        CutterrayBeamExpiryComponent.getComponentType(),
        new CutterrayBeamExpiryComponent(EXPIRY_TICKS));
    CutterrayExtractionManager.touch(
        world, source, targetBlock, itemInHand, commandBuffer);
  }

  @Override
  protected void simulateInteractWithBlock(
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nullable ItemStack itemInHand,
      @Nonnull World world,
      @Nonnull Vector3i targetBlock) {
  }
}
