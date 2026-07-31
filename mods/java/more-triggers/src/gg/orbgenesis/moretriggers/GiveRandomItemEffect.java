package gg.orbgenesis.moretriggers;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryUtils;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** Gives the triggering player one uniformly selected item from the loaded Item assets. */
public class GiveRandomItemEffect extends TriggerEffect {
  public enum OverflowBehavior {
    DROP_REMAINDER,
    IGNORE_REMAINDER,
    REQUIRE_FULL_STACK
  }

  public static final BuilderCodec<GiveRandomItemEffect> CODEC =
      BuilderCodec.builder(
              GiveRandomItemEffect.class, GiveRandomItemEffect::new, TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("Quantity", Codec.INTEGER, false),
              (effect, value) -> effect.quantity = value,
              effect -> effect.quantity)
          .add()
          .append(
              new KeyedCodec<>(
                  "OverflowBehavior", new EnumCodec<>(OverflowBehavior.class), false),
              (effect, value) ->
                  effect.overflowBehavior =
                      value != null ? value : OverflowBehavior.DROP_REMAINDER,
              effect -> effect.overflowBehavior)
          .add()
          .build();

  private int quantity = 1;
  private OverflowBehavior overflowBehavior = OverflowBehavior.DROP_REMAINDER;

  @Override
  public void execute(TriggerContext context) {
    if (quantity <= 0 || context.getStore() == null || context.getEntityRef() == null) {
      return;
    }

    var store = context.getStore();
    var playerRef = context.getEntityRef();
    if (store.getComponent(playerRef, Player.getComponentType()) == null) {
      return;
    }

    Item item = selectRandomItem();
    if (item == null) {
      return;
    }

    ItemStack itemStack = new ItemStack(item.getId(), quantity);
    OverflowBehavior resolvedBehavior =
        overflowBehavior != null ? overflowBehavior : OverflowBehavior.DROP_REMAINDER;
    ItemStackTransaction transaction;

    if (resolvedBehavior == OverflowBehavior.REQUIRE_FULL_STACK) {
      PlayerSettings settings =
          store.getComponent(playerRef, PlayerSettings.getComponentType());
      if (settings == null) {
        settings = PlayerSettings.defaults();
      }
      ItemContainer container =
          InventoryUtils.getContainerForItemPickup(playerRef, item, settings, store);
      transaction = container.addItemStack(itemStack, true, false, true);
    } else {
      transaction = Player.giveItem(itemStack, playerRef, store);
    }

    if (resolvedBehavior == OverflowBehavior.DROP_REMAINDER) {
      ItemStack remainder = transaction.getRemainder();
      if (!ItemStack.isEmpty(remainder)) {
        ItemUtils.dropItem(playerRef, remainder, store);
      }
    }
  }

  static Item selectRandomItem() {
    Map<String, Item> items = Item.getAssetMap().getAssetMap();
    if (items.isEmpty()) {
      return null;
    }

    List<Item> candidates = new ArrayList<>(items.size());
    for (Item item : items.values()) {
      if (item != null && item.getId() != null && !item.getId().isBlank()) {
        candidates.add(item);
      }
    }
    if (candidates.isEmpty()) {
      return null;
    }
    return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
  }
}
