package gg.raynormods.desertsurvival.research;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class ResearchCommand extends AbstractPlayerCommand {
  public ResearchCommand() {
    super("research", "Open and test the Desert Survival research prototype.");
    addSubCommand(new Open());
    addSubCommand(new Status());
    addSubCommand(new Grant());
    addSubCommand(new Tier());
  }

  @Override
  protected void execute(
      CommandContext context,
      Store<EntityStore> store,
      Ref<EntityStore> ref,
      PlayerRef playerRef,
      World world) {
    openTree(store, ref, playerRef);
  }

  private static void openTree(
      Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef) {
    Player player = store.getComponent(ref, Player.getComponentType());
    if (player != null) {
      player.getPageManager().openCustomPage(ref, store, new ResearchTreePage(playerRef));
    }
  }

  private abstract static class PlayerCommand extends AbstractPlayerCommand {
    PlayerCommand(String name, String description) {
      super(name, description);
    }
  }

  private static final class Open extends PlayerCommand {
    Open() {
      super("open", "Open the research tree.");
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      openTree(store, ref, playerRef);
    }
  }

  private static final class Status extends PlayerCommand {
    Status() {
      super("status", "Show research and Intel progress.");
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      ResearchProgressComponent progress = ResearchService.getOrCreate(ref, store);
      context.sendMessage(Message.raw(
          "Intel " + progress.getAvailableIntel() + " | investigado "
              + progress.getUnlockedCount() + "/48"));
    }
  }

  /** Prototype-only helper. Remove or permission-gate before a public build. */
  private static final class Grant extends PlayerCommand {
    private final RequiredArg<Integer> amount =
        withRequiredArg("amount", "Intel amount to grant for testing.", ArgTypes.INTEGER);

    Grant() {
      super("grant", "DEV: grant Intel to your character.");
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      int requested = Math.max(0, Math.min(1000, amount.get(context)));
      ResearchProgressComponent progress = ResearchService.addIntel(ref, store, requested);
      context.sendMessage(Message.raw(
          "Intel de prueba: +" + requested + " | disponible " + progress.getAvailableIntel()));
    }
  }

  /** Prototype bridge until biome progression owns this value. */
  private static final class Tier extends PlayerCommand {
    private final RequiredArg<Integer> tier =
        withRequiredArg("tier", "Maximum research tier: 0, 1 or 2.", ArgTypes.INTEGER);

    Tier() {
      super("tier", "DEV: set the current biome research tier.");
    }

    @Override
    protected void execute(
        CommandContext context,
        Store<EntityStore> store,
        Ref<EntityStore> ref,
        PlayerRef playerRef,
        World world) {
      ResearchProgressComponent updated = ResearchService.getOrCreate(ref, store).clone();
      updated.setMaxMaterialTier(tier.get(context));
      store.putComponent(ref, ResearchProgressComponent.getComponentType(), updated);
      context.sendMessage(Message.raw(
          "Tier de material disponible: " + updated.getMaxMaterialTier()
              + " (no bloquea la investigación)."));
    }
  }
}
