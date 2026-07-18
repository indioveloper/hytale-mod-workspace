package gg.orbgenesis.playertriggers;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

final class PlayerTagAccess {
  private PlayerTagAccess() {}

  static PlayerRef getPlayer(TriggerContext context) {
    if (context == null || context.getStore() == null || context.getEntityRef() == null) {
      return null;
    }
    return context
        .getStore()
        .getComponent(context.getEntityRef(), PlayerRef.getComponentType());
  }

  static PlayerTagsComponent getTags(TriggerContext context) {
    if (getPlayer(context) == null) {
      return null;
    }

    Store<EntityStore> store = context.getStore();
    Ref<EntityStore> ref = context.getEntityRef();
    return store.getComponent(
        ref, PlayerTriggersPlugin.get().getPlayerTagsComponentType());
  }

  static String normalizeKey(String key) {
    return key == null ? "" : key.trim().toLowerCase();
  }
}
