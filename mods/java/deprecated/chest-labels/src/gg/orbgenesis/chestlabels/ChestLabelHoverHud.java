package gg.orbgenesis.chestlabels;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class ChestLabelHoverHud extends CustomUIHud {
  public static final String HUD_KEY = "OrbGenesis_ChestLabelHover";
  private static final String EDIT_HINT = "Use /chestlabel edit to rename";

  private String name = "";
  private String iconKey = ChestIconRegistry.DEFAULT_ICON_KEY;
  private boolean hasLabel;
  private boolean visible;

  public ChestLabelHoverHud(PlayerRef playerRef) {
    super(playerRef, HUD_KEY, 50);
  }

  public static ChestLabelHoverHud getOrCreate(Player player, PlayerRef playerRef) {
    CustomUIHud existing = player.getHudManager().getCustomHud(HUD_KEY);
    if (existing instanceof ChestLabelHoverHud hud) {
      return hud;
    }

    ChestLabelHoverHud hud = new ChestLabelHoverHud(playerRef);
    player.getHudManager().addCustomHud(playerRef, hud);
    return hud;
  }

  public void showForChest(String nextName, String nextIconTexturePath, boolean nextHasLabel) {
    hasLabel = nextHasLabel;
    name = nextName == null ? "" : nextName;
    iconKey = ChestIconRegistry.normalizeKey(nextIconTexturePath);

    UICommandBuilder commands = new UICommandBuilder();
    if (!visible) {
      commands.append("HUD/ChestLabelHover.ui");
      visible = true;
    }

    applyState(commands);
    update(false, commands);
  }

  public void hideLabel() {
    if (!visible) {
      return;
    }

    visible = false;
    update(true, new UICommandBuilder());
  }

  @Override
  protected void build(UICommandBuilder commands) {
    if (!visible) {
      return;
    }

    commands.append("HUD/ChestLabelHover.ui");
    applyState(commands);
  }

  private void applyState(UICommandBuilder commands) {
    commands.set("#LabelRow.Visible", hasLabel);
    commands.set("#ChestIconStack.Visible", hasLabel);
    commands.set("#ChestName.Text", name);
    commands.set("#ChestName.Visible", hasLabel);
    commands.set("#EditHint.Text", EDIT_HINT);
    for (String candidate : ChestIconRegistry.getKeys()) {
      commands.set(ChestIconRegistry.getSelector(candidate) + ".Visible", candidate.equals(iconKey));
    }
  }
}
