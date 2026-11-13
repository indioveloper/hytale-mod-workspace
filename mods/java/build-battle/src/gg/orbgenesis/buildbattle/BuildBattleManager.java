package gg.orbgenesis.buildbattle;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferUtil;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.PrefabUtil;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import org.joml.Vector3d;
import org.joml.Vector3i;

public final class BuildBattleManager {
  private final Path dataDirectory;
  private final HytaleLogger logger;
  private final CreativeToolRestrictionManager restrictions;
  private final Map<String, Session> sessions = new HashMap<>();
  private volatile BuildBattleConfig config;
  private volatile List<String> themes;

  public BuildBattleManager(
      Path dataDirectory,
      HytaleLogger logger,
      CreativeToolRestrictionManager restrictions) {
    this.dataDirectory = dataDirectory;
    this.logger = logger;
    this.restrictions = restrictions;
  }

  public synchronized void initialize() {
    reload();
  }

  public synchronized String reload() {
    try {
      BuildBattleConfig loadedConfig = BuildBattleConfig.load(dataDirectory);
      List<String> loadedThemes = BuildBattleThemeCatalog.load(dataDirectory);
      if (loadedConfig.plots().isEmpty()) {
        return "build-battle.json does not contain any valid plots.";
      }
      config = loadedConfig;
      themes = loadedThemes;
      return "Loaded " + loadedThemes.size() + " themes and " + loadedConfig.plots().size() + " plots.";
    } catch (IOException | RuntimeException exception) {
      logger.at(Level.SEVERE).withCause(exception).log("Could not load Build Battle configuration.");
      return "Could not load Build Battle configuration: " + exception.getMessage();
    }
  }

  public synchronized String join(
      PlayerRef playerRef,
      Ref<EntityStore> ref,
      Store<EntityStore> store,
      World world,
      String preferredTeam) {
    if (config == null || themes == null) {
      return "Build Battle configuration is not available.";
    }
    if (findSession(playerRef.getUuid()) != null) {
      return "You are already queued for Build Battle.";
    }
    String worldKey = key(world.getName());
    Session session = sessions.computeIfAbsent(worldKey, ignored -> new Session(world.getName()));
    if (session.phase != BuildBattlePhase.LOBBY) {
      return "A Build Battle round is already running in this world.";
    }
    Player player = store.getComponent(ref, Player.getComponentType());
    if (player == null) {
      return "Player state is not ready.";
    }
    String team = normalizeTeam(preferredTeam);
    Participant participant =
        new Participant(
            playerRef.getUuid(),
            playerRef.getUsername(),
            playerRef,
            ref,
            store,
            player.getGameMode(),
            new Vector3d(playerRef.getTransform().getPosition()),
            new Rotation3f(playerRef.getTransform().getRotation()),
            team);
    session.participants.put(participant.id, participant);
    broadcast(session, participant.name + " joined Build Battle (" + session.participants.size() + ").");
    return team.isEmpty()
        ? "Joined the Build Battle lobby."
        : "Joined the Build Battle lobby with team code '" + team + "'.";
  }

  public synchronized String setPreferredTeam(UUID playerId, String teamName) {
    Session session = findSession(playerId);
    if (session == null || session.phase != BuildBattlePhase.LOBBY) {
      return "Join a Build Battle lobby before choosing a team.";
    }
    Participant participant = session.participants.get(playerId);
    participant.preferredTeam = normalizeTeam(teamName);
    return participant.preferredTeam.isEmpty()
        ? "Team preference cleared."
        : "Team preference set to '" + participant.preferredTeam + "'.";
  }

  public synchronized String leave(UUID playerId) {
    Session session = findSession(playerId);
    if (session == null) {
      return "You are not in a Build Battle round.";
    }
    Participant participant = session.participants.remove(playerId);
    restrictions.releaseGame(playerId);
    if (participant != null) {
      restoreParticipant(participant);
      broadcast(session, participant.name + " left Build Battle.");
    }
    if (session.participants.isEmpty()) {
      sessions.remove(key(session.worldName));
    }
    return "Left Build Battle.";
  }

  public synchronized String start(World world, BuildBattleMode mode) {
    Session session = sessions.get(key(world.getName()));
    if (session == null || session.participants.isEmpty()) {
      return "Nobody has joined Build Battle in this world.";
    }
    if (session.phase != BuildBattlePhase.LOBBY) {
      return "A Build Battle round is already running.";
    }
    if (session.participants.size() < config.minimumPlayers()) {
      return "At least " + config.minimumPlayers() + " players are required.";
    }
    List<BuildBattlePlot> worldPlots = plotsFor(world.getName());
    List<Team> teams = assignTeams(session, mode);
    if (teams.size() < 2) {
      return "At least two competing players or teams are required.";
    }
    if (teams.size() > worldPlots.size()) {
      return "This world has " + worldPlots.size() + " plots but needs " + teams.size() + ".";
    }
    session.mode = mode;
    session.teams.clear();
    for (int index = 0; index < teams.size(); index++) {
      Team team = teams.get(index);
      team.plot = worldPlots.get(index);
      session.teams.add(team);
      for (UUID member : team.members) {
        session.participants.get(member).teamId = team.id;
      }
    }
    beginThemeVote(session);
    return "Build Battle started in " + mode.name().toLowerCase(Locale.ROOT) + " mode.";
  }

  public synchronized String stop(World world) {
    Session session = sessions.remove(key(world.getName()));
    if (session == null) {
      return "There is no Build Battle session in this world.";
    }
    broadcast(session, "Build Battle was stopped by an administrator.");
    finish(session);
    return "Build Battle stopped.";
  }

  public synchronized boolean voteTheme(UUID playerId, String theme) {
    Session session = findSession(playerId);
    if (session == null
        || session.phase != BuildBattlePhase.THEME_VOTE
        || !session.themeCandidates.contains(theme)) {
      return false;
    }
    Participant participant = session.participants.get(playerId);
    if (participant.themeVote != null) {
      session.themeVotes.computeIfPresent(
          participant.themeVote, (ignored, value) -> Math.max(0, value - 1));
    }
    participant.themeVote = theme;
    session.themeVotes.merge(theme, 1, Integer::sum);
    return true;
  }

  public synchronized boolean voteBuild(UUID playerId, int score) {
    Session session = findSession(playerId);
    if (session == null
        || session.phase != BuildBattlePhase.BUILD_VOTE
        || score < 1
        || score > 5
        || session.voteTargetIndex < 0
        || session.voteTargetIndex >= session.teams.size()) {
      return false;
    }
    Participant participant = session.participants.get(playerId);
    Team target = session.teams.get(session.voteTargetIndex);
    if (target.id.equals(participant.teamId)) {
      return false;
    }
    target.buildVotes.put(playerId, score);
    return true;
  }

  public synchronized boolean canModify(UUID playerId, String worldName, Vector3i position) {
    Session session = findSession(playerId);
    if (session == null) {
      return true;
    }
    if (session.phase != BuildBattlePhase.BUILDING) {
      return false;
    }
    Participant participant = session.participants.get(playerId);
    Team team = team(session, participant.teamId);
    return team != null && team.plot != null && team.plot.contains(worldName, position);
  }

  public synchronized String status(World world, UUID playerId) {
    Session session = sessions.get(key(world.getName()));
    if (session == null) {
      return "Build Battle: no session in this world.";
    }
    long seconds = Math.max(0L, (session.phaseEndsNanos - System.nanoTime() + 999_999_999L) / 1_000_000_000L);
    String teamLabel = "";
    Participant participant = session.participants.get(playerId);
    if (participant != null && participant.teamId != null) {
      Team team = team(session, participant.teamId);
      teamLabel = team == null ? "" : ", team=" + team.displayName;
    }
    return "Build Battle: phase="
        + session.phase
        + ", players="
        + session.participants.size()
        + ", mode="
        + (session.mode == null ? "pending" : session.mode)
        + ", theme="
        + (session.selectedTheme == null ? "pending" : session.selectedTheme)
        + ", seconds="
        + seconds
        + teamLabel;
  }

  public synchronized void tick(Store<EntityStore> store) {
    World world = store.getExternalData().getWorld();
    Session session = sessions.get(key(world.getName()));
    if (session == null) {
      return;
    }
    refreshReferences(session, store);
    if (session.phase == BuildBattlePhase.BUILDING) {
      for (Participant participant : session.participants.values()) {
        if (participant.ref != null && participant.ref.isValid() && participant.playerRef.isValid()) {
          restrictions.refreshForGame(participant.playerRef, participant.ref, store);
        }
      }
    }
    if (session.phase != BuildBattlePhase.LOBBY && System.nanoTime() >= session.phaseEndsNanos) {
      switch (session.phase) {
        case THEME_VOTE -> beginBuilding(session, store);
        case BUILDING -> beginBuildVoting(session, store);
        case BUILD_VOTE -> advanceBuildVoting(session, store);
        case RESULTS -> {
          sessions.remove(key(session.worldName));
          finish(session);
        }
        default -> {}
      }
    }
  }

  public synchronized void shutdown() {
    for (Session session : new ArrayList<>(sessions.values())) {
      finish(session);
    }
    sessions.clear();
  }

  private void beginThemeVote(Session session) {
    List<String> shuffled = new ArrayList<>(themes);
    Collections.shuffle(shuffled);
    int count = Math.min(config.themeCandidates(), shuffled.size());
    session.themeCandidates = List.copyOf(shuffled.subList(0, count));
    session.themeVotes.clear();
    for (String candidate : session.themeCandidates) {
      session.themeVotes.put(candidate, 0);
    }
    session.phase = BuildBattlePhase.THEME_VOTE;
    session.phaseEndsNanos = deadline(config.themeVoteSeconds());
    broadcast(session, "Theme voting has started. Choose one of: " + String.join(", ", session.themeCandidates));
    for (Participant participant : session.participants.values()) {
      openThemeVote(participant, session.themeCandidates);
    }
  }

  private void beginBuilding(Session session, Store<EntityStore> store) {
    session.selectedTheme = BuildBattleScoring.winningTheme(session.themeCandidates, session.themeVotes);
    session.phase = BuildBattlePhase.BUILDING;
    session.phaseEndsNanos = deadline(config.buildSeconds());
    resetPlots(session, store);
    broadcast(session, "Theme: " + session.selectedTheme + ". Build phase started!");
    for (Participant participant : session.participants.values()) {
      Team team = team(session, participant.teamId);
      closePage(participant);
      if (participant.ref != null && participant.ref.isValid() && team != null) {
        Player.setGameMode(participant.ref, GameMode.Creative, store);
        teleport(participant, team.plot.spawn(), team.plot.rotation());
        restrictions.refreshForGame(participant.playerRef, participant.ref, store);
      }
    }
  }

  private void beginBuildVoting(Session session, Store<EntityStore> store) {
    session.phase = BuildBattlePhase.BUILD_VOTE;
    session.voteTargetIndex = 0;
    for (Participant participant : session.participants.values()) {
      restrictions.releaseGame(participant.id);
      if (participant.ref != null && participant.ref.isValid()) {
        Player.setGameMode(participant.ref, GameMode.Adventure, store);
      }
    }
    showVotingTarget(session);
  }

  private void advanceBuildVoting(Session session, Store<EntityStore> store) {
    session.voteTargetIndex++;
    if (session.voteTargetIndex < session.teams.size()) {
      showVotingTarget(session);
      return;
    }
    beginResults(session, store);
  }

  private void showVotingTarget(Session session) {
    Team target = session.teams.get(session.voteTargetIndex);
    session.phaseEndsNanos = deadline(config.secondsPerBuildVote());
    broadcast(
        session,
        "Voting for " + target.displayName + " (" + (session.voteTargetIndex + 1) + "/" + session.teams.size() + ").");
    for (Participant participant : session.participants.values()) {
      closePage(participant);
      teleport(participant, target.plot.viewingPosition(), target.plot.rotation());
      if (!target.id.equals(participant.teamId)) {
        openBuildVote(participant, target.displayName);
      } else {
        participant.playerRef.sendMessage(Message.raw("You cannot vote for your own build."));
      }
    }
  }

  private void beginResults(Session session, Store<EntityStore> store) {
    session.phase = BuildBattlePhase.RESULTS;
    session.phaseEndsNanos = deadline(config.resultsSeconds());
    List<Team> ranking = new ArrayList<>(session.teams);
    ranking.sort(
        Comparator.comparingDouble((Team team) -> BuildBattleScoring.average(team.buildVotes.values()))
            .reversed()
            .thenComparing(team -> team.displayName));
    broadcast(session, "Results for theme '" + session.selectedTheme + "':");
    for (int index = 0; index < ranking.size(); index++) {
      Team team = ranking.get(index);
      double average = BuildBattleScoring.average(team.buildVotes.values());
      broadcast(
          session,
          (index + 1)
              + ". "
              + team.displayName
              + " - "
              + String.format(Locale.ROOT, "%.2f", average)
              + "/5 ("
              + team.buildVotes.size()
              + " votes)");
    }
    for (Participant participant : session.participants.values()) {
      closePage(participant);
      if (participant.ref != null && participant.ref.isValid()) {
        Player.setGameMode(participant.ref, GameMode.Adventure, store);
      }
    }
  }

  private void finish(Session session) {
    for (Participant participant : session.participants.values()) {
      restrictions.releaseGame(participant.id);
      restoreParticipant(participant);
    }
  }

  private void restoreParticipant(Participant participant) {
    if (participant.ref == null || !participant.ref.isValid() || participant.store == null) {
      return;
    }
    closePage(participant);
    Player.setGameMode(
        participant.ref,
        participant.originalMode == null ? GameMode.Adventure : participant.originalMode,
        participant.store);
    teleport(participant, participant.originalPosition, participant.originalRotation);
  }

  private void refreshReferences(Session session, Store<EntityStore> store) {
    for (Participant participant : session.participants.values()) {
      PlayerRef live = Universe.get().getPlayer(participant.id);
      if (live != null
          && live.isValid()
          && live.getReference() != null
          && live.getReference().getStore() == store) {
        participant.playerRef = live;
        participant.ref = live.getReference();
        participant.store = store;
      }
    }
  }

  private void openThemeVote(Participant participant, List<String> candidates) {
    Player player = player(participant);
    if (player != null) {
      player.getPageManager().clearCustomPageAcknowledgements();
      player.getPageManager().openCustomPage(
          participant.ref,
          participant.store,
          new BuildBattleThemeVotePage(participant.playerRef, this, candidates));
    }
  }

  private void openBuildVote(Participant participant, String targetName) {
    Player player = player(participant);
    if (player != null) {
      player.getPageManager().clearCustomPageAcknowledgements();
      player.getPageManager().openCustomPage(
          participant.ref,
          participant.store,
          new BuildBattleVotePage(participant.playerRef, this, targetName));
    }
  }

  private Player player(Participant participant) {
    if (participant.ref == null || !participant.ref.isValid() || participant.store == null) {
      return null;
    }
    return participant.store.getComponent(participant.ref, Player.getComponentType());
  }

  private void closePage(Participant participant) {
    Player player = player(participant);
    if (player != null) {
      player.getPageManager().setPage(participant.ref, participant.store, Page.None);
    }
  }

  private void teleport(Participant participant, Vector3d position, Rotation3f rotation) {
    if (participant.ref != null && participant.ref.isValid() && participant.store != null) {
      participant.store.putComponent(
          participant.ref,
          Teleport.getComponentType(),
          Teleport.createForPlayer(position, rotation));
    }
  }

  private List<Team> assignTeams(Session session, BuildBattleMode mode) {
    List<BuildBattleTeamPlanner.Entry> players = session.participants.values().stream()
        .map(participant -> new BuildBattleTeamPlanner.Entry(
            participant.id, participant.name, participant.preferredTeam))
        .toList();
    List<Team> result = new ArrayList<>();
    for (BuildBattleTeamPlanner.Plan plan :
        BuildBattleTeamPlanner.plan(players, mode, config.teamSize())) {
      Team team = new Team(plan.id(), plan.displayName());
      team.members.addAll(plan.members());
      result.add(team);
    }
    return result;
  }

  private void resetPlots(Session session, Store<EntityStore> store) {
    World world = store.getExternalData().getWorld();
    for (Team team : session.teams) {
      BuildBattlePlot plot = team.plot;
      if (plot == null || plot.resetPrefab().isBlank()) {
        continue;
      }
      Path prefabPath = resolvePrefab(plot.resetPrefab());
      if (prefabPath == null) {
        logger.at(Level.WARNING).log(
            "Build Battle plot '%s' reset prefab '%s' was not found.",
            plot.id(), plot.resetPrefab());
        continue;
      }
      try {
        var buffer = PrefabBufferUtil.loadBuffer(prefabPath);
        PrefabUtil.paste(
            buffer.newAccess(),
            world,
            new Vector3i(plot.resetOrigin()),
            Rotation.None,
            new Random(),
            1,
            4,
            store);
      } catch (Exception exception) {
        logger.at(Level.WARNING).withCause(exception).log(
            "Build Battle could not reset plot '%s' from prefab '%s'.",
            plot.id(), plot.resetPrefab());
      }
    }
  }

  private static Path resolvePrefab(String value) {
    String normalized = value.replace('\\', '/').trim();
    Path path = PrefabStore.get().findAssetPrefabPath(normalized);
    if (path == null && !normalized.endsWith(".prefab.json")) {
      path = PrefabStore.get().findAssetPrefabPath(normalized + ".prefab.json");
    }
    return path;
  }

  private List<BuildBattlePlot> plotsFor(String worldName) {
    return config.plots().stream()
        .filter(plot -> plot.world().equalsIgnoreCase(worldName))
        .toList();
  }

  private Session findSession(UUID playerId) {
    for (Session session : sessions.values()) {
      if (session.participants.containsKey(playerId)) {
        return session;
      }
    }
    return null;
  }

  private static Team team(Session session, String id) {
    if (id == null) {
      return null;
    }
    for (Team team : session.teams) {
      if (team.id.equals(id)) {
        return team;
      }
    }
    return null;
  }

  private static void broadcast(Session session, String text) {
    Message message = Message.raw("[Build Battle] " + text);
    for (Participant participant : session.participants.values()) {
      if (participant.playerRef != null && participant.playerRef.isValid()) {
        participant.playerRef.sendMessage(message);
      }
    }
  }

  private static long deadline(int seconds) {
    return System.nanoTime() + seconds * 1_000_000_000L;
  }

  private static String normalizeTeam(String value) {
    if (value == null) {
      return "";
    }
    String cleaned = value.trim().replaceAll("[^\\p{L}\\p{N}_-]", "");
    return cleaned.length() > 24 ? cleaned.substring(0, 24) : cleaned;
  }

  private static String key(String value) {
    return value.toLowerCase(Locale.ROOT);
  }

  private static final class Session {
    private final String worldName;
    private final Map<UUID, Participant> participants = new LinkedHashMap<>();
    private final List<Team> teams = new ArrayList<>();
    private final Map<String, Integer> themeVotes = new LinkedHashMap<>();
    private BuildBattlePhase phase = BuildBattlePhase.LOBBY;
    private BuildBattleMode mode;
    private List<String> themeCandidates = List.of();
    private String selectedTheme;
    private int voteTargetIndex = -1;
    private long phaseEndsNanos;

    private Session(String worldName) {
      this.worldName = worldName;
    }
  }

  private static final class Participant {
    private final UUID id;
    private final String name;
    private final GameMode originalMode;
    private final Vector3d originalPosition;
    private final Rotation3f originalRotation;
    private PlayerRef playerRef;
    private Ref<EntityStore> ref;
    private Store<EntityStore> store;
    private String preferredTeam;
    private String teamId;
    private String themeVote;

    private Participant(
        UUID id,
        String name,
        PlayerRef playerRef,
        Ref<EntityStore> ref,
        Store<EntityStore> store,
        GameMode originalMode,
        Vector3d originalPosition,
        Rotation3f originalRotation,
        String preferredTeam) {
      this.id = id;
      this.name = name;
      this.playerRef = playerRef;
      this.ref = ref;
      this.store = store;
      this.originalMode = originalMode;
      this.originalPosition = originalPosition;
      this.originalRotation = originalRotation;
      this.preferredTeam = preferredTeam;
    }
  }

  private static final class Team {
    private final String id;
    private final String displayName;
    private final List<UUID> members = new ArrayList<>();
    private final Map<UUID, Integer> buildVotes = new LinkedHashMap<>();
    private BuildBattlePlot plot;

    private Team(String id, String displayName) {
      this.id = id;
      this.displayName = displayName;
    }
  }
}
