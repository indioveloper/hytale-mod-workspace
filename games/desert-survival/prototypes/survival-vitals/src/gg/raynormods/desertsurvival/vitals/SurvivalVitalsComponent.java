package gg.raynormods.desertsurvival.vitals;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/** Persistent hydration and recycled-water state for one player. */
public final class SurvivalVitalsComponent implements Component<EntityStore> {
  public static final double MAX_HYDRATION_ML = 1500.0;
  public static final double LOW_HYDRATION_ML = 500.0;
  public static final double HIGH_HYDRATION_ML = 1000.0;
  public static final double MAX_RESERVOIR_ML = 500.0;
  public static final double STILLSUIT_T1_RECOVERY_RATE = 1.0 / 3.0;
  public static final double STILLSUIT_DRINK_ML = 150.0;

  private static final int CURRENT_SCHEMA = 2;

  public static final BuilderCodec<SurvivalVitalsComponent> CODEC =
      BuilderCodec.builder(SurvivalVitalsComponent.class, SurvivalVitalsComponent::new)
          .append(
              new KeyedCodec<>("Hydration", Codec.DOUBLE, false),
              (component, value) -> component.hydrationMl =
                  clamp(value, 0.0, MAX_HYDRATION_ML),
              component -> component.hydrationMl)
          .add()
          .append(
              new KeyedCodec<>("StillsuitReservoir", Codec.DOUBLE, false),
              (component, value) -> component.stillsuitReservoirMl =
                  clamp(value, 0.0, MAX_RESERVOIR_ML),
              component -> component.stillsuitReservoirMl)
          .add()
          .append(
              new KeyedCodec<>("SchemaVersion", Codec.INTEGER, false),
              (component, value) -> component.schemaVersion = value == null ? 1 : value,
              component -> component.schemaVersion)
          .add()
          .build();

  private static ComponentType<EntityStore, SurvivalVitalsComponent> componentType;

  // Version 1 stored both values on a 0-100 / 0-40 scale.
  private double hydrationMl = 100.0;
  private double stillsuitReservoirMl;
  private int schemaVersion = 1;

  private transient float tickAccumulator;
  private transient double heatExposurePercent;
  private transient boolean burning;
  private transient int dehydrationSeconds;
  private transient float lastStamina = Float.NaN;

  static SurvivalVitalsComponent fresh() {
    SurvivalVitalsComponent component = new SurvivalVitalsComponent();
    component.hydrationMl = MAX_HYDRATION_ML;
    component.schemaVersion = CURRENT_SCHEMA;
    return component;
  }

  public static void setComponentType(
      ComponentType<EntityStore, SurvivalVitalsComponent> value) {
    componentType = value;
  }

  public static ComponentType<EntityStore, SurvivalVitalsComponent> getComponentType() {
    return componentType;
  }

  void migrateLegacyScale() {
    if (schemaVersion >= CURRENT_SCHEMA) {
      return;
    }
    hydrationMl = clamp(hydrationMl * 15.0, 0.0, MAX_HYDRATION_ML);
    stillsuitReservoirMl = clamp(stillsuitReservoirMl * 12.5, 0.0, MAX_RESERVOIR_ML);
    schemaVersion = CURRENT_SCHEMA;
  }

  public double getHydrationMl() {
    return hydrationMl;
  }

  public double getStillsuitReservoirMl() {
    return stillsuitReservoirMl;
  }

  public double getHeatExposurePercent() {
    return heatExposurePercent;
  }

  public boolean isBurning() {
    return burning;
  }

  boolean advance(float deltaSeconds) {
    tickAccumulator += deltaSeconds;
    if (tickAccumulator < 1.0f) {
      return false;
    }
    tickAccumulator -= 1.0f;
    return true;
  }

  void updateSunExposure(boolean exposed) {
    heatExposurePercent = clamp(
        heatExposurePercent + (exposed ? 5.0 : -10.0), 0.0, 100.0);
    if (!burning && heatExposurePercent >= 100.0) {
      burning = true;
    } else if (burning && heatExposurePercent <= 90.0) {
      burning = false;
    }
  }

  void loseWater(double lossMl, double recoveryRate, boolean hasRecoveryPocket) {
    double safeLoss = Math.max(0.0, lossMl);
    double before = hydrationMl;
    hydrationMl = clamp(hydrationMl - safeLoss, 0.0, MAX_HYDRATION_ML);
    double actualLoss = before - hydrationMl;
    if (hasRecoveryPocket && recoveryRate > 0.0) {
      stillsuitReservoirMl = clamp(
          stillsuitReservoirMl + (actualLoss * recoveryRate), 0.0, MAX_RESERVOIR_ML);
    }
  }

  double drinkWater(double amountMl) {
    double before = hydrationMl;
    hydrationMl = clamp(hydrationMl + Math.max(0.0, amountMl), 0.0, MAX_HYDRATION_ML);
    return hydrationMl - before;
  }

  double drinkFromStillsuit(double maximumTransferMl) {
    double transfer = Math.min(
        Math.max(0.0, maximumTransferMl),
        Math.min(stillsuitReservoirMl, MAX_HYDRATION_ML - hydrationMl));
    hydrationMl += transfer;
    stillsuitReservoirMl -= transfer;
    return transfer;
  }

  boolean tickDehydrationDamage() {
    if (hydrationMl > 0.0) {
      dehydrationSeconds = 0;
      return false;
    }
    dehydrationSeconds++;
    if (dehydrationSeconds < 10) {
      return false;
    }
    dehydrationSeconds = 0;
    return true;
  }

  float limitStaminaRegeneration(float currentStamina) {
    float result = currentStamina;
    if (hydrationMl <= 0.0 && Float.isFinite(lastStamina) && currentStamina > lastStamina) {
      result = lastStamina + ((currentStamina - lastStamina) * 0.5f);
    }
    lastStamina = result;
    return result;
  }

  String hydrationState() {
    if (hydrationMl <= 0.0) {
      return "DESHIDRATADO";
    }
    if (hydrationMl < LOW_HYDRATION_ML) {
      return "CRITICO";
    }
    if (hydrationMl <= HIGH_HYDRATION_ML) {
      return "REDUCIDO";
    }
    return "OPTIMO";
  }

  @Override
  public SurvivalVitalsComponent clone() {
    SurvivalVitalsComponent copy = new SurvivalVitalsComponent();
    copy.hydrationMl = hydrationMl;
    copy.stillsuitReservoirMl = stillsuitReservoirMl;
    copy.schemaVersion = schemaVersion;
    copy.tickAccumulator = tickAccumulator;
    copy.heatExposurePercent = heatExposurePercent;
    copy.burning = burning;
    copy.dehydrationSeconds = dehydrationSeconds;
    copy.lastStamina = lastStamina;
    return copy;
  }

  private static double clamp(double value, double minimum, double maximum) {
    return Math.max(minimum, Math.min(maximum, value));
  }
}
