package gg.orbgenesis.triggersnpcs;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.RoleUtils;

public class EquipNpcItemEffect extends TriggerEffect {
  public static final BuilderCodec<EquipNpcItemEffect> CODEC =
      BuilderCodec.builder(EquipNpcItemEffect.class, EquipNpcItemEffect::new, TriggerEffect.BASE_CODEC)
          .append(new KeyedCodec<>("Item", Codec.STRING), (effect, value) -> effect.itemId = value, effect -> effect.itemId)
          .add()
          .build();

  private String itemId;

  @Override
  public void execute(TriggerContext context) {
    if (itemId == null || itemId.isBlank()) {
      return;
    }

    var store = context.getStore();
    var npc = store.getComponent(context.getEntityRef(), NPCEntity.getComponentType());
    if (npc == null) {
      return;
    }

    var item = Item.getAssetMap().getAsset(itemId);
    if (item == null) {
      return;
    }

    if (item.getArmor() != null) {
      RoleUtils.setArmor(context.getEntityRef(), npc, item.getId(), store);
    } else {
      RoleUtils.setItemInHand(context.getEntityRef(), npc, item.getId(), store);
    }
  }
}
