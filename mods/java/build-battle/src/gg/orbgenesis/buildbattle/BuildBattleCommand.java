package gg.orbgenesis.buildbattle;

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

public final class BuildBattleCommand extends AbstractCommandCollection {
  public BuildBattleCommand(BuildBattleManager manager) {
    super("buildbattle", "Join and control Build Battle rounds.");
    addAliases("bb");
    addSubCommand(new Join(manager));
    addSubCommand(new JoinTeam(manager));
    addSubCommand(new TeamPreference(manager));
    addSubCommand(new Leave(manager));
    addSubCommand(new Status(manager));
    addSubCommand(new ThemeVote(manager));
    addSubCommand(new BuildVote(manager));
    addSubCommand(new Start(manager, "startsolo", BuildBattleMode.SOLO));
    addSubCommand(new Start(manager, "startteams", BuildBattleMode.TEAMS));
    addSubCommand(new Stop(manager));
    addSubCommand(new Reload(manager));
  }

  private abstract static class PlayerCommand extends AbstractPlayerCommand {
    protected final BuildBattleManager manager;

    private PlayerCommand(BuildBattleManager manager, String name, String description) {
      super(name, description);
      this.manager = manager;
    }

    protected final void reply(CommandContext context, String text) {
      context.sendMessage(Message.raw("[Build Battle] " + text));
    }
  }

  private static final class Join extends PlayerCommand {
    private Join(BuildBattleManager manager) {
      super(manager, "join", "Join the Build Battle lobby in this world.");
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      reply(context, manager.join(playerRef, ref, store, world, ""));
    }
  }

  private static final class JoinTeam extends PlayerCommand {
    private final RequiredArg<String> team =
        withRequiredArg("team", "Team code (letters, numbers, hyphens or underscores).", ArgTypes.STRING);

    private JoinTeam(BuildBattleManager manager) {
      super(manager, "jointeam", "Join the Build Battle lobby with a preferred team.");
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      reply(context, manager.join(playerRef, ref, store, world, team.get(context)));
    }
  }

  private static final class TeamPreference extends PlayerCommand {
    private final RequiredArg<String> team =
        withRequiredArg("team", "Team code.", ArgTypes.STRING);

    private TeamPreference(BuildBattleManager manager) {
      super(manager, "team", "Change your preferred team before the round starts.");
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      reply(context, manager.setPreferredTeam(playerRef.getUuid(), team.get(context)));
    }
  }

  private static final class Leave extends PlayerCommand {
    private Leave(BuildBattleManager manager) {
      super(manager, "leave", "Leave the Build Battle lobby or round.");
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      reply(context, manager.leave(playerRef.getUuid()));
    }
  }

  private static final class Status extends PlayerCommand {
    private Status(BuildBattleManager manager) {
      super(manager, "status", "Show the current Build Battle state.");
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      reply(context, manager.status(world, playerRef.getUuid()));
    }
  }

  private static final class ThemeVote extends PlayerCommand {
    private final RequiredArg<String> theme =
        withRequiredArg("theme", "Exact theme label shown by the vote.", ArgTypes.GREEDY_STRING);

    private ThemeVote(BuildBattleManager manager) {
      super(manager, "votetheme", "Vote for a theme without using the UI.");
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      boolean accepted = manager.voteTheme(playerRef.getUuid(), theme.get(context).trim());
      reply(context, accepted ? "Theme vote registered." : "That theme vote is not available now.");
    }
  }

  private static final class BuildVote extends PlayerCommand {
    private final RequiredArg<Integer> score =
        withRequiredArg("score", "Score from 1 to 5.", ArgTypes.INTEGER);

    private BuildVote(BuildBattleManager manager) {
      super(manager, "vote", "Vote for the build currently being shown.");
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      int value = score.get(context);
      boolean accepted = manager.voteBuild(playerRef.getUuid(), value);
      reply(context, accepted ? "Build vote registered: " + value + "/5." : "That build vote is not available now.");
    }
  }

  private static final class Start extends PlayerCommand {
    private final BuildBattleMode mode;

    private Start(BuildBattleManager manager, String name, BuildBattleMode mode) {
      super(manager, name, "Start Build Battle in " + mode.name().toLowerCase() + " mode.");
      this.mode = mode;
      setPermissionGroups(new String[] {"hytale:WorldEditor"});
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      reply(context, manager.start(world, mode));
    }
  }

  private static final class Stop extends PlayerCommand {
    private Stop(BuildBattleManager manager) {
      super(manager, "stop", "Stop the Build Battle round in this world.");
      setPermissionGroups(new String[] {"hytale:WorldEditor"});
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      reply(context, manager.stop(world));
    }
  }

  private static final class Reload extends PlayerCommand {
    private Reload(BuildBattleManager manager) {
      super(manager, "reload", "Reload build-battle.json and themes.json.");
      setPermissionGroups(new String[] {"hytale:WorldEditor"});
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      reply(context, manager.reload());
    }
  }
}
