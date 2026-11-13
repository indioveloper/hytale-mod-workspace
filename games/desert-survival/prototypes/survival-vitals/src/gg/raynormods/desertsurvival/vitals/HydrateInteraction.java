package gg.raynormods.desertsurvival.vitals;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public final class HydrateInteraction extends SimpleInstantInteraction {
  public static final BuilderCodec<HydrateInteraction> CODEC =
      BuilderCodec.builder(
              HydrateInteraction.class,
              HydrateInteraction::new,
              SimpleInstantInteraction.CODEC)
          .append(
              new KeyedCodec<>("Source", Codec.STRING, false),
              (interaction, value) -> interaction.source = value,
              interaction -> interaction.source)
          .add()
          .append(
              new KeyedCodec<>("Amount", Codec.DOUBLE, false),
              (interaction, value) -> interaction.amount = value,
              interaction -> interaction.amount)
          .add()
          .build();

  private String source = "FLASK";
  private double amount = 500.0;

  public HydrateInteraction() {
  }

  @Override
  protected void firstRun(
      InteractionType type,
      InteractionContext context,
      CooldownHandler cooldownHandler) {
    var entity = context.getEntity();
    var commands = context.getCommandBuffer();
    if (entity == null || !entity.isValid() || commands == null) {
      return;
    }
    SurvivalVitalsComponent vitals = commands.getComponent(
        entity, SurvivalVitalsComponent.getComponentType());
    if (vitals == null) {
      vitals = SurvivalVitalsComponent.fresh();
    } else {
      vitals = vitals.clone();
      vitals.migrateLegacyScale();
    }

    double restored = vitals.drinkWater(amount);
    if (restored <= 0.0) {
      send(context, "Ya tienes la hidratacion al maximo.");
      return;
    }
    context.setHeldItem(new ItemStack("Water_Flask_Empty_Prototype", 1));

    commands.putComponent(entity, SurvivalVitalsComponent.getComponentType(), vitals);
    send(context, "Hidratacion recuperada: +" + (int) Math.round(restored) + " ml");
  }

  private static void send(InteractionContext context, String text) {
    PlayerRef playerRef = context.getCommandBuffer().getComponent(
        context.getEntity(), PlayerRef.getComponentType());
    if (playerRef != null) {
      playerRef.sendMessage(Message.raw(text));
    }
  }
}
