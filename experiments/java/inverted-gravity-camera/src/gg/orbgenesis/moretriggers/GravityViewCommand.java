package gg.orbgenesis.moretriggers;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/** Toggles the perspective of an active upside-down custom camera. */
/** Archived perspective-toggle command from the inverted-camera experiment. */
final class GravityViewCommand extends AbstractPlayerCommand {
  private final MoreTriggersPlugin plugin;

  GravityViewCommand(MoreTriggersPlugin plugin) {
    super("gravityview", "Toggle first/third person while the gravity view is active.");
    this.plugin = plugin;
  }

  @Override
  protected void execute(
      CommandContext context,
      Store<EntityStore> store,
      Ref<EntityStore> ref,
      PlayerRef playerRef,
      World world) {
    SetPlayerGravityViewEffect.Perspective perspective =
        plugin.getGravityViewController().toggle(playerRef);
    if (perspective == null) {
      context.sendMessage(Message.raw("La vista de gravedad invertida no esta activa."));
      return;
    }
    context.sendMessage(
        Message.raw(
            perspective == SetPlayerGravityViewEffect.Perspective.FIRST_PERSON
                ? "Vista de gravedad: primera persona."
                : "Vista de gravedad: tercera persona."));
  }
}
