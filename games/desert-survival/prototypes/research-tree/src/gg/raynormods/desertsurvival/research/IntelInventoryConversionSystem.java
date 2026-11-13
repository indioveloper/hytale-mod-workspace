package gg.raynormods.desertsurvival.research;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.InventoryChangeEvent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

/** Converts Intel inserted directly into an inventory, including creative grants. */
public final class IntelInventoryConversionSystem
    extends EntityEventSystem<EntityStore, InventoryChangeEvent> {
  private static final String INTEL_ITEM_ID = "Intel_Pickup_Prototype";

  public IntelInventoryConversionSystem() {
    super(InventoryChangeEvent.class);
  }

  @Override
  public Query<EntityStore> getQuery() {
    return Player.getComponentType();
  }

  @Override
  public void handle(
      int index,
      @Nonnull ArchetypeChunk<EntityStore> chunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commands,
      @Nonnull InventoryChangeEvent event) {
    convert(chunk.getReferenceTo(index), event.getItemContainer(), commands);
  }

  /** Also called on join for Intel left in a character's inventory by older builds. */
  static void convertExisting(Ref<EntityStore> player, Store<EntityStore> store) {
    convert(player, InventoryComponent.getCombined(store, player, InventoryComponent.EVERYTHING), store);
  }

  private static void convert(
      Ref<EntityStore> player,
      ItemContainer inventory,
      com.hypixel.hytale.component.ComponentAccessor<EntityStore> accessor) {
    if (inventory == null || !player.isValid()) {
      return;
    }
    int collected = 0;
    for (short slot = 0; slot < inventory.getCapacity(); slot++) {
      ItemStack stack = inventory.getItemStack(slot);
      if (ItemStack.isEmpty(stack) || !INTEL_ITEM_ID.equals(stack.getItemId())) {
        continue;
      }
      int quantity = stack.getQuantity();
      if (inventory.removeItemStackFromSlot(slot, quantity).succeeded()) {
        collected += quantity;
      }
    }
    if (collected == 0) {
      return;
    }
    ResearchProgressComponent current = accessor.getComponent(
        player, ResearchProgressComponent.getComponentType());
    ResearchProgressComponent updated =
        current == null ? ResearchProgressComponent.fresh() : current.clone();
    updated.addIntel(collected);
    accessor.putComponent(player, ResearchProgressComponent.getComponentType(), updated);
    PlayerRef playerRef = accessor.getComponent(player, PlayerRef.getComponentType());
    if (playerRef != null) {
      playerRef.sendMessage(Message.raw(
          "Intel recuperado: +" + collected + "  |  Disponible: "
              + updated.getAvailableIntel()));
    }
  }
}
