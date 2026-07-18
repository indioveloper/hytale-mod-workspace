package gg.orbgenesis.playertriggers;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

public class MobKillListenerSystem extends DeathSystems.OnDeathSystem {
  @Override
  public Query<EntityStore> getQuery() {
    return NPCEntity.getComponentType();
  }

  @Override
  public void onComponentAdded(
      Ref<EntityStore> victimRef,
      DeathComponent death,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> commands) {
    if (victimRef == null || death == null || death.getDeathInfo() == null) {
      return;
    }

    commands.run(liveStore -> processDeath(victimRef, death, liveStore));
  }

  private void processDeath(
      Ref<EntityStore> victimRef,
      DeathComponent death,
      Store<EntityStore> store) {
    if (victimRef == null || !victimRef.isValid()) {
      return;
    }
    if (store.getComponent(victimRef, PlayerRef.getComponentType()) != null) {
      return;
    }

    Ref<EntityStore> killerRef = getKillerRef(death.getDeathInfo());
    if (killerRef == null || !killerRef.isValid()) {
      return;
    }

    PlayerRef killer = store.getComponent(killerRef, PlayerRef.getComponentType());
    if (killer == null) {
      return;
    }

    PlayerTagsComponent listener =
        store.getComponent(
            killerRef, PlayerTriggersPlugin.get().getPlayerTagsComponentType());
    if (listener == null) {
      return;
    }
    incrementTag(listener.getTags(), KillCounterHud.TOTAL_KILLS_TAG, BigDecimal.ONE);
    updateHud(store, killerRef, killer, listener);

    if (!listener.isMobKillListenerEnabled()
        || isAutomaticCounterDuplicate(listener)) {
      return;
    }

    String counterTag = PlayerTagAccess.normalizeKey(listener.getMobKillCounterTag());
    BigDecimal points = parseNumber(listener.getMobKillPoints());
    if (counterTag.isEmpty() || points == null || !matchesFilter(store, victimRef, listener)) {
      return;
    }

    incrementTag(listener.getTags(), counterTag, points);
  }

  private void incrementTag(
      Map<String, String> tags, String counterTag, BigDecimal points) {
    BigDecimal current = parseNumber(tags.get(counterTag));
    if (current == null) {
      current = BigDecimal.ZERO;
    }
    tags.put(
        counterTag,
        current.add(points).stripTrailingZeros().toPlainString());
  }

  private boolean isAutomaticCounterDuplicate(PlayerTagsComponent listener) {
    return KillCounterHud.TOTAL_KILLS_TAG.equals(
            PlayerTagAccess.normalizeKey(listener.getMobKillCounterTag()))
        && (listener.getMobKillFilter() == null
            || listener.getMobKillFilter().isBlank())
        && BigDecimal.ONE.equals(parseNumber(listener.getMobKillPoints()));
  }

  private void updateHud(
      Store<EntityStore> store,
      Ref<EntityStore> killerRef,
      PlayerRef killer,
      PlayerTagsComponent listener) {
    Player player = store.getComponent(killerRef, Player.getComponentType());
    if (player == null) {
      return;
    }

    KillCounterHud hud =
        KillCounterHud.getOrCreate(player, killer);
    hud.updateCount(
        listener.getTags().getOrDefault(KillCounterHud.TOTAL_KILLS_TAG, "0"));
  }

  private Ref<EntityStore> getKillerRef(Damage damage) {
    if (!(damage.getSource() instanceof Damage.EntitySource entitySource)) {
      return null;
    }
    return entitySource.getRef();
  }

  private boolean matchesFilter(
      Store<EntityStore> store,
      Ref<EntityStore> victimRef,
      PlayerTagsComponent listener) {
    String filter = listener.getMobKillFilter();
    if (filter == null || filter.isBlank()) {
      return true;
    }

    ModelComponent model = store.getComponent(victimRef, ModelComponent.getComponentType());
    if (model == null || model.getModel() == null || model.getModel().getModelAssetId() == null) {
      return false;
    }

    String modelId = model.getModel().getModelAssetId().toLowerCase(Locale.ROOT);
    for (String candidate : filter.split("[,;|\\n\\r]+")) {
      String expected = candidate.trim().toLowerCase(Locale.ROOT);
      if (!expected.isEmpty() && modelId.contains(expected)) {
        return true;
      }
    }
    return false;
  }

  private static BigDecimal parseNumber(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      return new BigDecimal(raw.trim());
    } catch (NumberFormatException ignored) {
      return null;
    }
  }
}
