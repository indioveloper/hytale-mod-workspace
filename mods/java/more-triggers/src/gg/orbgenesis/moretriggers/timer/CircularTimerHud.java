package gg.orbgenesis.moretriggers.timer;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class CircularTimerHud extends CustomUIHud {
  public static final String HUD_KEY = "OrbGenesis_CircularTimer";
  public static final int FRAME_COUNT = 60;

  private boolean visible;
  private int frame = -1;
  private String text = "";

  public CircularTimerHud(PlayerRef playerRef) {
    super(playerRef, HUD_KEY, 80);
  }

  public static CircularTimerHud getOrCreate(Player player, PlayerRef playerRef) {
    CustomUIHud existing = player.getHudManager().getCustomHud(HUD_KEY);
    if (existing instanceof CircularTimerHud hud) {
      return hud;
    }
    CircularTimerHud hud = new CircularTimerHud(playerRef);
    player.getHudManager().addCustomHud(playerRef, hud);
    return hud;
  }

  public void render(String nextText, int nextFrame) {
    int safeFrame = Math.max(0, Math.min(FRAME_COUNT, nextFrame));
    String safeText = nextText == null ? "00:00" : nextText;
    if (visible && safeFrame == frame && safeText.equals(text)) {
      return;
    }

    UICommandBuilder commands = new UICommandBuilder();
    if (!visible) {
      commands.append("HUD/CircularTimer.ui");
      for (int i = 0; i <= FRAME_COUNT; i++) {
        commands.set(frameSelector(i) + ".Visible", i == safeFrame);
      }
      visible = true;
    } else if (frame != safeFrame) {
      if (frame >= 0) {
        commands.set(frameSelector(frame) + ".Visible", false);
      }
      commands.set(frameSelector(safeFrame) + ".Visible", true);
    }
    if (!safeText.equals(text)) {
      commands.set("#TimerText.Text", safeText);
    }
    frame = safeFrame;
    text = safeText;
    update(false, commands);
  }

  public void hideTimer() {
    if (!visible) {
      return;
    }
    visible = false;
    frame = -1;
    text = "";
    update(true, new UICommandBuilder());
  }

  public boolean isTimerVisible() {
    return visible;
  }

  @Override
  protected void build(UICommandBuilder commands) {
    if (!visible) {
      return;
    }
    commands.append("HUD/CircularTimer.ui");
    for (int i = 0; i <= FRAME_COUNT; i++) {
      commands.set(frameSelector(i) + ".Visible", i == frame);
    }
    commands.set("#TimerText.Text", text);
  }

  private static String frameSelector(int frame) {
    return String.format("#Ring%02d", frame);
  }
}
