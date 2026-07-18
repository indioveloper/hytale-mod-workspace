package gg.orbgenesis.playertriggers;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class PlayerCountHud extends CustomUIHud {
  public static final String HUD_KEY = "OrbGenesis_PlayerCount";

  private String count;

  public PlayerCountHud(PlayerRef playerRef, String count) {
    super(playerRef, HUD_KEY, 12);
    this.count = count;
  }

  public static PlayerCountHud getOrCreate(Player player, PlayerRef playerRef) {
    CustomUIHud existing = player.getHudManager().getCustomHud(HUD_KEY);
    if (existing instanceof PlayerCountHud playerCountHud) {
      return playerCountHud;
    }

    PlayerCountHud hud = new PlayerCountHud(playerRef, "0");
    player.getHudManager().addCustomHud(playerRef, hud);
    return hud;
  }

  public static void remove(Player player, PlayerRef playerRef) {
    if (player != null && playerRef != null) {
      player.getHudManager().removeCustomHud(playerRef, HUD_KEY);
    }
  }

  public void updateCount(String value) {
    count = value == null || value.isBlank() ? "0" : value;
    UICommandBuilder commands = new UICommandBuilder();
    commands.set("#PlayerCount.Text", count);
    update(false, commands);
  }

  @Override
  protected void build(UICommandBuilder commands) {
    commands.append("HUD/PlayerTriggersPlayerCount.ui");
    commands.set("#PlayerCount.Text", count);
  }
}
