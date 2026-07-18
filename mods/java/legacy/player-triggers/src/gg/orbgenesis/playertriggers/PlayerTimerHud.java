package gg.orbgenesis.playertriggers;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class PlayerTimerHud extends CustomUIHud {
  public static final String HUD_KEY = "OrbGenesis_PlayerTimer";

  private String label;
  private String time = "00:00";

  public PlayerTimerHud(PlayerRef playerRef, String label) {
    super(playerRef, HUD_KEY, 11);
    this.label = label;
  }

  public static PlayerTimerHud getOrCreate(
      Player player, PlayerRef playerRef, String label) {
    CustomUIHud existing = player.getHudManager().getCustomHud(HUD_KEY);
    if (existing instanceof PlayerTimerHud timerHud) {
      return timerHud;
    }

    PlayerTimerHud hud = new PlayerTimerHud(playerRef, label);
    player.getHudManager().addCustomHud(playerRef, hud);
    return hud;
  }

  public void updateTimer(String nextLabel, String nextTime) {
    label = nextLabel;
    time = nextTime;
    UICommandBuilder commands = new UICommandBuilder();
    commands.set("#TimerLabel.Text", label);
    commands.set("#TimerValue.Text", time);
    update(false, commands);
  }

  @Override
  protected void build(UICommandBuilder commands) {
    commands.append("HUD/PlayerTriggersTimer.ui");
    commands.set("#TimerLabel.Text", label);
    commands.set("#TimerValue.Text", time);
  }
}
