package gg.raynormods.desertsurvival.vitals;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.Locale;

final class SurvivalVitalsHud extends CustomUIHud {
  static final String HUD_KEY = "Raynor_DesertSurvivalVitals";
  private static final int BAR_WIDTH = 200;
  private static final int NATIVE_HEALTH_BAR_WIDTH = 450;
  private static final int DEHYDRATION_CAP_PERCENT = 50;

  private boolean visible;
  private String signature = "";

  private SurvivalVitalsHud(PlayerRef playerRef) {
    super(playerRef, HUD_KEY, 70);
  }

  static SurvivalVitalsHud getOrCreate(Player player, PlayerRef playerRef) {
    CustomUIHud existing = player.getHudManager().getCustomHud(HUD_KEY);
    if (existing instanceof SurvivalVitalsHud hud) {
      return hud;
    }
    SurvivalVitalsHud hud = new SurvivalVitalsHud(playerRef);
    player.getHudManager().addCustomHud(playerRef, hud);
    return hud;
  }

  void render(
      SurvivalVitalsComponent vitals,
      boolean exposed,
      HeatTier heatTier,
      SurvivalVitalsSystem.EquipmentProfile equipment,
      double waterLossMl) {
    int hydrationMl = (int) Math.round(vitals.getHydrationMl());
    int hydrationPercent = (int) Math.round(
        vitals.getHydrationMl() * 100.0 / SurvivalVitalsComponent.MAX_HYDRATION_ML);
    int reservoirMl = (int) Math.round(vitals.getStillsuitReservoirMl());
    int reservoirPercent = (int) Math.round(
        vitals.getStillsuitReservoirMl() * 100.0
            / SurvivalVitalsComponent.MAX_RESERVOIR_ML);
    int exposurePercent = (int) Math.round(vitals.getHeatExposurePercent());
    int resistance = (int) Math.round(equipment.heatResistance());
    String lossText = String.format(Locale.ROOT, "%.2f", waterLossMl);
    String nextSignature = hydrationMl + ":" + reservoirMl + ":" + exposed + ":"
        + exposurePercent + ":" + vitals.isBurning() + ":" + heatTier.level() + ":"
        + resistance + ":" + lossText + ":" + equipment.hasRecoveryPocket();
    if (visible && nextSignature.equals(signature)) {
      return;
    }

    UICommandBuilder commands = new UICommandBuilder();
    if (!visible) {
      commands.append("Hud/SurvivalVitals.ui");
      visible = true;
    }
    commands.set(
        "#HydrationValue.Text",
        "HIDRATACION " + hydrationMl + "/1500 ml - " + vitals.hydrationState());
    commands.setObject(
        "#HydrationFill.Anchor",
        barAnchor(BAR_WIDTH * hydrationPercent / 100, 10));
    commands.set("#SunValue.Text", exposed ? "SOL DIRECTO" : "SOMBRA");
    commands.set(
        "#ExposureValue.Text",
        "EXPOSICION " + exposurePercent + "%" + (vitals.isBurning() ? " - QUEMADURA" : ""));
    commands.setObject(
        "#ExposureFill.Anchor",
        barAnchor(BAR_WIDTH * exposurePercent / 100, 8));
    commands.set(
        "#HeatValue.Text",
        "CALOR " + heatTier.level() + " (" + heatTier.label() + ")  |  RESISTENCIA "
            + resistance);
    commands.set("#LossValue.Text", "PERDIDA " + lossText + " ml/s");
    boolean dehydrated = vitals.getHydrationMl() <= 0.0;
    commands.set("#HealthCapValue.Visible", dehydrated);
    commands.set("#HealthCapOverlay.Visible", dehydrated);
    int capWidth = NATIVE_HEALTH_BAR_WIDTH * DEHYDRATION_CAP_PERCENT / 100;
    commands.setObject(
        "#HealthCapUnavailable.Anchor",
        positionedAnchor(capWidth, NATIVE_HEALTH_BAR_WIDTH - capWidth, 18));
    commands.setObject(
        "#HealthCapMarker.Anchor",
        positionedAnchor(Math.max(0, capWidth - 4), 4, 18));
    commands.set("#ReservoirRow.Visible", equipment.hasRecoveryPocket());
    commands.set(
        "#ReservoirValue.Text",
        "BOLSILLO " + reservoirMl + "/500 ml  |  /drinkstillsuit: hasta 150 ml");
    commands.setObject(
        "#ReservoirFill.Anchor",
        barAnchor(BAR_WIDTH * reservoirPercent / 100, 8));
    signature = nextSignature;
    update(false, commands);
  }

  @Override
  protected void build(UICommandBuilder commands) {
    if (visible) {
      commands.append("Hud/SurvivalVitals.ui");
    }
  }

  private static Anchor barAnchor(int width, int height) {
    Anchor anchor = new Anchor();
    anchor.setWidth(Value.of(Math.max(0, Math.min(BAR_WIDTH, width))));
    anchor.setHeight(Value.of(height));
    return anchor;
  }

  private static Anchor positionedAnchor(int left, int width, int height) {
    Anchor anchor = barAnchor(width, height);
    anchor.setLeft(Value.of(left));
    return anchor;
  }
}
