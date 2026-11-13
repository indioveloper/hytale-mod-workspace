package gg.raynormods.cinema;

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

import java.util.List;
import java.util.Locale;

public final class CinemaCommand extends AbstractCommandCollection {
    CinemaCommand(CinemaSessionManager sessions) {
        super("cinema", "Gestiona el prototipo de sesiones sincronizadas de Raynor Cinema.");
        addSubCommand(new Create(sessions));
        addSubCommand(new Play(sessions));
        addSubCommand(new Pause(sessions));
        addSubCommand(new Seek(sessions));
        addSubCommand(new Stop(sessions));
        addSubCommand(new Status(sessions));
        addSubCommand(new ListAll(sessions));
        addSubCommand(new Remove(sessions));
        addSubCommand(new OfficialPlayer(sessions));
    }

    private abstract static class SessionCommand extends AbstractPlayerCommand {
        final CinemaSessionManager sessions;

        SessionCommand(CinemaSessionManager sessions, String name, String description) {
            super(name, description);
            this.sessions = sessions;
            setPermissionGroups(new String[]{"hytale:WorldEditor"});
        }

        CinemaSession require(CommandContext context, World world, String id) {
            CinemaSession session = sessions.get(world.getName(), id);
            if (session == null) send(context, "No existe esa sesion en este mundo.");
            return session;
        }
    }

    private static final class Create extends SessionCommand {
        private final RequiredArg<String> id = withRequiredArg("id", "ID estable de la sesion.", ArgTypes.STRING);
        private final RequiredArg<String> url = withRequiredArg("url", "URL HTTPS de YouTube.", ArgTypes.STRING);

        Create(CinemaSessionManager sessions) {
            super(sessions, "create", "Crea una sesion sincronizada sin descargar el video.");
        }

        @Override
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref,
                               PlayerRef playerRef, World world) {
            CinemaSessionManager.CreateResult result = sessions.create(world.getName(), id.get(context), url.get(context));
            if (!result.success()) {
                send(context, "Error: " + result.error());
                return;
            }
            CinemaSession.Snapshot session = result.session();
            send(context, "Sesion creada: " + session.id() + " (YouTube " + session.source().mediaId() + ").");
            send(context, "Este spike sincroniza el reloj; no descarga ni proyecta bytes de YouTube dentro del juego.");
        }
    }

    private static final class Play extends IdCommand {
        Play(CinemaSessionManager sessions) { super(sessions, "play", "Inicia o reanuda la sesion."); }
        @Override void apply(CommandContext context, CinemaSession session) { session.play(); send(context, describe(session.snapshot())); }
    }

    private static final class Pause extends IdCommand {
        Pause(CinemaSessionManager sessions) { super(sessions, "pause", "Pausa la sesion."); }
        @Override void apply(CommandContext context, CinemaSession session) { session.pause(); send(context, describe(session.snapshot())); }
    }

    private static final class Stop extends IdCommand {
        Stop(CinemaSessionManager sessions) { super(sessions, "stop", "Detiene y rebobina la sesion."); }
        @Override void apply(CommandContext context, CinemaSession session) { session.stop(); send(context, describe(session.snapshot())); }
    }

    private static final class Status extends IdCommand {
        Status(CinemaSessionManager sessions) { super(sessions, "status", "Muestra el reloj canonico de la sesion."); }
        @Override void apply(CommandContext context, CinemaSession session) { send(context, describe(session.snapshot())); }
    }

    private abstract static class IdCommand extends SessionCommand {
        private final RequiredArg<String> id = withRequiredArg("id", "ID de la sesion.", ArgTypes.STRING);

        IdCommand(CinemaSessionManager sessions, String name, String description) {
            super(sessions, name, description);
        }

        @Override
        protected final void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref,
                                     PlayerRef playerRef, World world) {
            CinemaSession session = require(context, world, id.get(context));
            if (session != null) apply(context, session);
        }

        abstract void apply(CommandContext context, CinemaSession session);
    }

    private static final class Seek extends SessionCommand {
        private final RequiredArg<String> id = withRequiredArg("id", "ID de la sesion.", ArgTypes.STRING);
        private final RequiredArg<Float> seconds = withRequiredArg("seconds", "Posicion desde cero.", ArgTypes.FLOAT);

        Seek(CinemaSessionManager sessions) {
            super(sessions, "seek", "Mueve el reloj de reproduccion.");
        }

        @Override
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref,
                               PlayerRef playerRef, World world) {
            CinemaSession session = require(context, world, id.get(context));
            if (session == null) return;
            float value = seconds.get(context);
            if (!Float.isFinite(value) || value < 0) {
                send(context, "Los segundos deben ser un numero positivo.");
                return;
            }
            session.seek(value);
            send(context, describe(session.snapshot()));
        }
    }

    private static final class ListAll extends SessionCommand {
        ListAll(CinemaSessionManager sessions) { super(sessions, "list", "Lista las sesiones de este mundo."); }

        @Override
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref,
                               PlayerRef playerRef, World world) {
            List<CinemaSession.Snapshot> values = sessions.list(world.getName());
            if (values.isEmpty()) {
                send(context, "No hay sesiones de cine en este mundo.");
                return;
            }
            send(context, "Sesiones: " + values.size());
            for (CinemaSession.Snapshot value : values) send(context, describe(value));
        }
    }

    private static final class Remove extends SessionCommand {
        private final RequiredArg<String> id = withRequiredArg("id", "ID de la sesion.", ArgTypes.STRING);
        Remove(CinemaSessionManager sessions) { super(sessions, "remove", "Elimina una sesion."); }

        @Override
        protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref,
                               PlayerRef playerRef, World world) {
            send(context, sessions.remove(world.getName(), id.get(context)) ? "Sesion eliminada." : "No existe esa sesion.");
        }
    }

    private static final class OfficialPlayer extends IdCommand {
        OfficialPlayer(CinemaSessionManager sessions) {
            super(sessions, "player", "Muestra la URL del reproductor oficial en la posicion sincronizada.");
        }

        @Override
        void apply(CommandContext context, CinemaSession session) {
            CinemaSession.Snapshot value = session.snapshot();
            send(context, value.source().embedUrl((long) Math.floor(value.positionSeconds())));
        }
    }

    private static String describe(CinemaSession.Snapshot value) {
        return String.format(Locale.ROOT, "%s: %s @ %.2fs, revision %d, video %s",
            value.id(), value.state(), value.positionSeconds(), value.revision(), value.source().mediaId());
    }

    private static void send(CommandContext context, String message) {
        context.sendMessage(Message.raw(message));
    }
}
