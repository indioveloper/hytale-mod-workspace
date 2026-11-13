package gg.raynormods.desertsurvival.vitals;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/** Server-side action intended to be bound to the stillsuit hotkey by a client integration. */
final class DrinkStillsuitCommand extends AbstractPlayerCommand {
  DrinkStillsuitCommand() {
    super("drinkstillsuit", "Drink up to 150 ml from the equipped stillsuit reservoir.");
  }

  @Override
  protected void execute(
      CommandContext context,
      Store<EntityStore> store,
      Ref<EntityStore> ref,
      PlayerRef playerRef,
      World world) {
    if (!SurvivalVitalsSystem.isWearingStillsuitChest(ref, store)) {
      context.sendMessage(Message.raw("Necesitas equipar la pechera del destiltraje."));
      return;
    }
    SurvivalVitalsComponent current = store.getComponent(
        ref, SurvivalVitalsComponent.getComponentType());
    if (current == null) {
      context.sendMessage(Message.raw("El sistema de hidratacion aun no esta listo."));
      return;
    }
    SurvivalVitalsComponent updated = current.clone();
    updated.migrateLegacyScale();
    double restored = updated.drinkFromStillsuit(SurvivalVitalsComponent.STILLSUIT_DRINK_ML);
    if (restored <= 0.0) {
      context.sendMessage(Message.raw(
          updated.getStillsuitReservoirMl() <= 0.0
              ? "El deposito del destiltraje esta vacio."
              : "Ya tienes la hidratacion al maximo."));
      return;
    }
    store.putComponent(ref, SurvivalVitalsComponent.getComponentType(), updated);
    context.sendMessage(Message.raw(
        "Has bebido " + (int) Math.round(restored) + " ml del destiltraje. Reserva: "
            + (int) Math.round(updated.getStillsuitReservoirMl()) + " ml."));
  }
}
