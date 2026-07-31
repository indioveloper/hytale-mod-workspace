package gg.orbgenesis.moretriggers;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.logger.backend.HytaleLoggerBackend;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.UUID;
import org.joml.Vector3d;

/** Runs a server command from a Trigger Volume without changing the saved effect ID. */
public class ExecuteCommandEffect extends TriggerEffect {
  public enum ExecutorMode {
    SERVER,
    PLAYER
  }

  public String command = "";
  public ExecutorMode executor = ExecutorMode.SERVER;

  public static final BuilderCodec<ExecuteCommandEffect> CODEC =
      BuilderCodec.builder(
              ExecuteCommandEffect.class, ExecuteCommandEffect::new, TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("Command", Codec.STRING, false),
              (effect, value) -> effect.command = value,
              effect -> effect.command)
          .add()
          .append(
              new KeyedCodec<>("Executor", new EnumCodec<>(ExecutorMode.class), false),
              (effect, value) -> effect.executor = value != null ? value : ExecutorMode.SERVER,
              effect -> effect.executor)
          .add()
          .build();

  @Override
  public void execute(TriggerContext context) {
    if (command == null || command.isBlank()) {
      debug("Trigger fired with empty command; skipping.");
      return;
    }

    PlayerRef player = getPlayer(context);
    String resolved = resolveCommand(command, player).trim();
    if (resolved.isEmpty()) {
      debug("Trigger resolved to an empty command; skipping.");
      return;
    }

    ExecutorMode executorMode = executor != null ? executor : ExecutorMode.SERVER;
    String commandToRun = stripLeadingSlash(resolved);
    CommandSender sender =
        (executorMode == ExecutorMode.PLAYER || needsPlayerContext(commandToRun)) && player != null
            ? player
            : ConsoleSender.INSTANCE;
    debug(
        "Trigger fired. executor="
            + executorMode
            + ", sender="
            + sender.getUsername()
            + ", player="
            + (player != null ? nullToEmpty(player.getUsername()) : "<none>")
            + ", command="
            + commandToRun);

    Deque<String> commands = new ArrayDeque<>();
    commands.add(commandToRun);
    CommandManager.get()
        .handleCommands(sender, commands)
        .whenComplete(
            (ignored, error) -> {
              if (error != null) {
                debug("Command failed: " + error.getClass().getName() + ": " + error.getMessage());
              } else {
                debug("Command dispatched successfully: " + commandToRun);
              }
            });
  }

  private PlayerRef getPlayer(TriggerContext context) {
    Ref<EntityStore> entityRef = context.getEntityRef();
    Store<EntityStore> store = context.getStore();
    if (entityRef == null || store == null) {
      return null;
    }
    return store.getComponent(entityRef, PlayerRef.getComponentType());
  }

  private boolean needsPlayerContext(String commandToRun) {
    String normalized = commandToRun.toLowerCase(Locale.ROOT).trim();
    return normalized.startsWith("tp all ") || normalized.startsWith("teleport all ");
  }

  private String resolveCommand(String rawCommand, PlayerRef player) {
    String username = player != null ? nullToEmpty(player.getUsername()) : "";
    UUID uuid = player != null ? player.getUuid() : null;

    Vector3d position = new Vector3d();
    if (player != null) {
      Transform transform = player.getTransform();
      if (transform != null && transform.getPosition() != null) {
        position = transform.getPosition();
      }
    }

    return rawCommand
        .replace("{player}", username)
        .replace("{uuid}", uuid != null ? uuid.toString() : "")
        .replace("{x}", String.valueOf((int) position.x))
        .replace("{y}", String.valueOf((int) position.y))
        .replace("{z}", String.valueOf((int) position.z));
  }

  private String stripLeadingSlash(String value) {
    return value.startsWith("/") ? value.substring(1) : value;
  }

  private String nullToEmpty(String value) {
    return value == null ? "" : value;
  }

  private void debug(String message) {
    HytaleLoggerBackend.rawLog("[MoreTriggers:ExecuteCommand] " + message);
  }
}
