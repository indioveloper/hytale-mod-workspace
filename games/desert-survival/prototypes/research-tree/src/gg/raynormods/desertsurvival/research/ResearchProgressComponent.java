package gg.raynormods.desertsurvival.research;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/** Persistent per-character Intel balance, material access and researched node IDs. */
public final class ResearchProgressComponent implements Component<EntityStore> {
  private static final int CURRENT_SCHEMA = 1;

  public static final BuilderCodec<ResearchProgressComponent> CODEC =
      BuilderCodec.builder(ResearchProgressComponent.class, ResearchProgressComponent::new)
          .append(
              new KeyedCodec<>("AvailableIntel", Codec.INTEGER, false),
              (component, value) -> component.availableIntel = Math.max(0, value),
              component -> component.availableIntel)
          .add()
          .append(
              new KeyedCodec<>("LifetimeIntel", Codec.INTEGER, false),
              (component, value) -> component.lifetimeIntel = Math.max(0, value),
              component -> component.lifetimeIntel)
          .add()
          .append(
              new KeyedCodec<>("MaxResearchTier", Codec.INTEGER, false),
              (component, value) -> component.maxResearchTier = clampTier(value),
              component -> component.maxResearchTier)
          .add()
          .append(
              new KeyedCodec<>("UnlockedNodeIds", Codec.STRING_ARRAY, false),
              (component, value) -> component.unlockedNodeIds = value == null ? new String[0] : value,
              component -> component.unlockedNodeIds)
          .add()
          .append(
              new KeyedCodec<>("SchemaVersion", Codec.INTEGER, false),
              (component, value) -> component.schemaVersion = value == null ? CURRENT_SCHEMA : value,
              component -> component.schemaVersion)
          .add()
          .build();

  private static ComponentType<EntityStore, ResearchProgressComponent> componentType;

  private int availableIntel;
  private int lifetimeIntel;
  private int maxResearchTier;
  private String[] unlockedNodeIds = new String[0];
  private int schemaVersion = CURRENT_SCHEMA;

  public static void setComponentType(ComponentType<EntityStore, ResearchProgressComponent> value) {
    componentType = value;
  }

  public static ComponentType<EntityStore, ResearchProgressComponent> getComponentType() {
    return componentType;
  }

  public static ResearchProgressComponent fresh() {
    return new ResearchProgressComponent();
  }

  public int getAvailableIntel() {
    return availableIntel;
  }

  public int getLifetimeIntel() {
    return lifetimeIntel;
  }

  public int getMaxResearchTier() {
    return maxResearchTier;
  }

  /**
   * Highest material tier reached in the world. The persisted codec key keeps
   * its legacy name so existing character saves remain compatible.
   */
  public int getMaxMaterialTier() {
    return maxResearchTier;
  }

  public int getUnlockedCount() {
    return unlockedSet().size();
  }

  public boolean isUnlocked(String nodeId) {
    return unlockedSet().contains(nodeId);
  }

  public void addIntel(int amount) {
    int safeAmount = Math.max(0, amount);
    availableIntel += safeAmount;
    lifetimeIntel += safeAmount;
  }

  public boolean spendAndUnlock(ResearchNode node) {
    if (isUnlocked(node.id()) || availableIntel < node.intelCost()) {
      return false;
    }
    availableIntel -= node.intelCost();
    Set<String> unlocked = unlockedSet();
    unlocked.add(node.id());
    unlockedNodeIds = unlocked.toArray(String[]::new);
    return true;
  }

  public boolean hasPrerequisites(ResearchNode node) {
    Set<String> unlocked = unlockedSet();
    return unlocked.containsAll(node.prerequisites());
  }

  public void setMaxResearchTier(int tier) {
    maxResearchTier = clampTier(tier);
  }

  public void setMaxMaterialTier(int tier) {
    maxResearchTier = clampTier(tier);
  }

  @Override
  public ResearchProgressComponent clone() {
    ResearchProgressComponent copy = new ResearchProgressComponent();
    copy.availableIntel = availableIntel;
    copy.lifetimeIntel = lifetimeIntel;
    copy.maxResearchTier = maxResearchTier;
    copy.unlockedNodeIds = unlockedNodeIds.clone();
    copy.schemaVersion = schemaVersion;
    return copy;
  }

  private Set<String> unlockedSet() {
    return new LinkedHashSet<>(Arrays.asList(unlockedNodeIds));
  }

  private static int clampTier(int value) {
    return Math.max(0, Math.min(2, value));
  }
}
