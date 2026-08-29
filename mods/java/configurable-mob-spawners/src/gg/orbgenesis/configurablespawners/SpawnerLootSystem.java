package gg.orbgenesis.configurablespawners;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.gameplay.DeathConfig;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

final class SpawnerLootSystem extends DeathSystems.OnDeathSystem {
  @Override
  @Nonnull
  public Query<EntityStore> getQuery() {
    return Query.and(
        SpawnedBySpawnerComponent.getComponentType(),
        TransformComponent.getComponentType(),
        HeadRotation.getComponentType());
  }

  @Override
  public void onComponentAdded(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull DeathComponent death,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer) {
    SpawnedBySpawnerComponent tracker = commandBuffer.getComponent(
        ref, SpawnedBySpawnerComponent.getComponentType());
    if (tracker == null) return;
    if (tracker.elite && tracker.lastPlayerAttacker != null) {
      for (PlayerRef player : store.getExternalData().getWorld().getPlayerRefs()) {
        if (player.getUuid().equals(tracker.lastPlayerAttacker)) {
          Message message = Message.raw("¡" + player.getUsername() + " ha derrotado a "
              + tracker.baseMobName + " "
              + tracker.elitePrefix.toLowerCase(java.util.Locale.ROOT) + "!");
          for (PlayerRef recipient : store.getExternalData().getWorld().getPlayerRefs()) {
            recipient.sendMessage(message);
          }
          break;
        }
      }
    }
    if (tracker.lootMode == LootMode.NONE || tracker.lootMode == LootMode.REPLACE) {
      death.setItemsLossMode(DeathConfig.ItemsLossMode.NONE);
    }

    ArrayList<ItemStack> drops = new ArrayList<>();
    ThreadLocalRandom random = ThreadLocalRandom.current();
    if (tracker.lootMode == LootMode.ADD || tracker.lootMode == LootMode.REPLACE) {
      addDrops(drops, tracker.lootEntries, random);
    }
    if (tracker.elite) addDrops(drops, tracker.eliteLootEntries, random);
    if (drops.isEmpty()) return;
    TransformComponent transform = commandBuffer.getComponent(ref, TransformComponent.getComponentType());
    HeadRotation head = commandBuffer.getComponent(ref, HeadRotation.getComponentType());
    if (transform == null || head == null) return;
    var holders = ItemComponent.generateItemDrops(
        store,
        drops,
        new Vector3d(transform.getPosition()).add(0.0, 1.0, 0.0),
        head.getRotation());
    commandBuffer.addEntities(holders, com.hypixel.hytale.component.AddReason.SPAWN);
  }

  private static void addDrops(
      ArrayList<ItemStack> drops, SpawnerLootEntry[] entries, ThreadLocalRandom random) {
    for (SpawnerLootEntry entry : entries) {
      if (entry == null || entry.itemId.isBlank() || Item.getAssetMap().getAsset(entry.itemId) == null) continue;
      if (random.nextDouble(100.0) >= entry.chancePercent) continue;
      int quantity = entry.minQuantity == entry.maxQuantity
          ? entry.minQuantity
          : random.nextInt(entry.minQuantity, entry.maxQuantity + 1);
      drops.add(new ItemStack(entry.itemId, quantity));
    }
  }
}
