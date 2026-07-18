package gg.orbgenesis.playertriggers;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class KillCounterHud extends CustomUIHud {
  public static final String HUD_KEY = "OrbGenesis_PlayerKillCounter";
  public static final String TOTAL_KILLS_TAG = "mobs_killed";

  private String count;

  public KillCounterHud(PlayerRef playerRef, String count) {
    super(playerRef, HUD_KEY, 10);
    this.count = count;
  }

  public static KillCounterHud getOrCreate(
      Player player, PlayerRef playerRef) {
    CustomUIHud existing = player.getHudManager().getCustomHud(HUD_KEY);
    if (existing instanceof KillCounterHud killCounterHud) {
      return killCounterHud;
    }

    KillCounterHud hud = new KillCounterHud(playerRef, "0");
    player.getHudManager().addCustomHud(playerRef, hud);
    return hud;
  }

  public void updateCount(String value) {
    count = value == null || value.isBlank() ? "0" : value;
    UICommandBuilder commands = new UICommandBuilder();
    commands.set("#KillCount.Text", count);
    update(false, commands);
  }

  @Override
  protected void build(UICommandBuilder commands) {
    commands.append("HUD/PlayerTriggersKillCounter.ui");
    commands.set("#KillCount.Text", count);
  }
}
