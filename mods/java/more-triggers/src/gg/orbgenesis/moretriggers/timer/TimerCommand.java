package gg.orbgenesis.moretriggers.timer;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import gg.orbgenesis.moretriggers.MoreTriggersPlugin;
import java.util.List;

public class TimerCommand extends AbstractCommandCollection {
  public TimerCommand(MoreTriggersPlugin plugin) {
    super("timer", "Control the circular countdown timer.");
    addSubCommand(new Start(plugin));
    addSubCommand(new Simple(plugin, "pause", TimerAction.PAUSE));
    addSubCommand(new Simple(plugin, "resume", TimerAction.RESUME));
    addSubCommand(new Simple(plugin, "show", TimerAction.SHOW));
    addSubCommand(new Simple(plugin, "hide", TimerAction.HIDE));
    addSubCommand(new Simple(plugin, "cancel", TimerAction.CANCEL));
    addSubCommand(new Status(plugin));
  }

  private abstract static class TimerPlayerCommand extends AbstractPlayerCommand {
    protected final MoreTriggersPlugin plugin;

    protected TimerPlayerCommand(MoreTriggersPlugin plugin, String name, String description) {
      super(name, description);
      this.plugin = plugin;
    }
  }

  private static final class Start extends TimerPlayerCommand {
    private final RequiredArg<Integer> secondsArg =
        withRequiredArg("seconds", "Countdown duration in seconds (1-359999).", ArgTypes.INTEGER);

    private Start(MoreTriggersPlugin plugin) {
      super(plugin, "start", "Start or replace your circular timer.");
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      int seconds = secondsArg.get(context);
      int changed = plugin.getTimerManager().apply(TimerAction.START, seconds, List.of(playerRef));
      context.sendMessage(
          Message.raw(
              changed > 0
                  ? "Timer iniciado: " + TimerManager.formatRemaining(seconds * 1_000_000_000L)
                  : "La duracion debe estar entre 1 y 359999 segundos."));
    }
  }

  private static final class Simple extends TimerPlayerCommand {
    private final TimerAction action;

    private Simple(MoreTriggersPlugin plugin, String name, TimerAction action) {
      super(plugin, name, "Apply " + name + " to your circular timer.");
      this.action = action;
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      int changed = plugin.getTimerManager().apply(action, 0, List.of(playerRef));
      context.sendMessage(
          Message.raw(changed > 0 ? successMessage(action) : "No hay un timer compatible con esa accion."));
    }

    private static String successMessage(TimerAction action) {
      return switch (action) {
        case PAUSE -> "Timer pausado.";
        case RESUME -> "Timer reanudado.";
        case SHOW -> "Timer mostrado.";
        case HIDE -> "Timer ocultado; la cuenta continua.";
        case CANCEL -> "Timer cancelado.";
        case START -> "Timer iniciado.";
      };
    }
  }

  private static final class Status extends TimerPlayerCommand {
    private Status(MoreTriggersPlugin plugin) {
      super(plugin, "status", "Show the current circular timer state.");
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      TimerManager.TimerSnapshot snapshot = plugin.getTimerManager().snapshot(playerRef);
      if (snapshot == null) {
        context.sendMessage(Message.raw("No hay ningun timer activo."));
        return;
      }
      String state = snapshot.complete() ? "terminado" : snapshot.paused() ? "pausado" : "en marcha";
      context.sendMessage(
          Message.raw(
              "Timer: "
                  + snapshot.remaining()
                  + " ("
                  + state
                  + ", "
                  + (snapshot.visible() ? "visible" : "oculto")
                  + ")."));
    }
  }
}
