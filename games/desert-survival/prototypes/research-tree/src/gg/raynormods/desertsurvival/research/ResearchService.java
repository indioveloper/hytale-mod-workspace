package gg.raynormods.desertsurvival.research;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class ResearchService {
  public record Result(boolean success, String message) {}

  private ResearchService() {}

  public static ResearchProgressComponent getOrCreate(
      Ref<EntityStore> ref, Store<EntityStore> store) {
    ResearchProgressComponent progress =
        store.getComponent(ref, ResearchProgressComponent.getComponentType());
    if (progress == null) {
      progress = ResearchProgressComponent.fresh();
      store.addComponent(ref, ResearchProgressComponent.getComponentType(), progress);
    }
    return progress;
  }

  public static Result research(
      Ref<EntityStore> ref, Store<EntityStore> store, String nodeId) {
    ResearchNode node = ResearchCatalog.get(nodeId);
    if (node == null) {
      return new Result(false, "Nodo de investigación desconocido.");
    }
    ResearchProgressComponent current = getOrCreate(ref, store);
    Result validation = validate(current, node);
    if (validation != null) {
      return validation;
    }
    ResearchProgressComponent updated = current.clone();
    if (!updated.spendAndUnlock(node)) {
      return new Result(false, "No se pudo investigar el nodo.");
    }
    store.putComponent(ref, ResearchProgressComponent.getComponentType(), updated);
    return new Result(true, "Investigado: " + node.name() + ".");
  }

  static Result validate(ResearchProgressComponent progress, ResearchNode node) {
    if (progress.isUnlocked(node.id())) {
      return new Result(false, node.name() + " ya está investigado.");
    }
    if (!progress.hasPrerequisites(node)) {
      return new Result(false, "Faltan requisitos: " + String.join(", ", node.prerequisites()));
    }
    if (progress.getAvailableIntel() < node.intelCost()) {
      return new Result(false, "Intel insuficiente: necesitas " + node.intelCost() + ".");
    }
    return null;
  }

  public static ResearchProgressComponent addIntel(
      Ref<EntityStore> ref, Store<EntityStore> store, int amount) {
    ResearchProgressComponent updated = getOrCreate(ref, store).clone();
    updated.addIntel(amount);
    store.putComponent(ref, ResearchProgressComponent.getComponentType(), updated);
    return updated;
  }
}
