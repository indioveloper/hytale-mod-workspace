package gg.raynormods.desertsurvival.research;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;

/** Converts a world pickup directly into persistent character Intel. */
public final class CollectIntelInteraction extends SimpleInstantInteraction {
  public static final BuilderCodec<CollectIntelInteraction> CODEC =
      BuilderCodec.builder(
              CollectIntelInteraction.class,
              CollectIntelInteraction::new,
              SimpleInstantInteraction.CODEC)
          .append(
              new KeyedCodec<>("Amount", Codec.INTEGER, false),
              (interaction, value) -> interaction.amount = Math.max(1, value),
              interaction -> interaction.amount)
          .add()
          .build();

  private int amount = 1;

  @Override
  protected void firstRun(
      InteractionType type,
      InteractionContext context,
      CooldownHandler cooldownHandler) {
    var player = context.getEntity();
    var commands = context.getCommandBuffer();
    if (player == null || !player.isValid() || commands == null) {
      return;
    }

    int quantity = 1;
    var pickup = context.getTargetEntity();
    if (pickup != null && pickup.isValid()) {
      ItemComponent item = commands.getComponent(pickup, ItemComponent.getComponentType());
      if (item != null && item.getItemStack() != null) {
        quantity = Math.max(1, item.getItemStack().getQuantity());
      }
    }

    int collected = Math.max(1, amount) * quantity;
    ResearchProgressComponent current = commands.getComponent(
        player, ResearchProgressComponent.getComponentType());
    ResearchProgressComponent updated =
        current == null ? ResearchProgressComponent.fresh() : current.clone();
    updated.addIntel(collected);
    commands.putComponent(player, ResearchProgressComponent.getComponentType(), updated);

    PlayerRef playerRef = commands.getComponent(player, PlayerRef.getComponentType());
    if (playerRef != null) {
      playerRef.sendMessage(Message.raw(
          "Intel recuperado: +" + collected + "  |  Disponible: "
              + updated.getAvailableIntel()));
    }
    // The engine removes a world item after its Pickup interaction chain. It never enters inventory.
  }
}
