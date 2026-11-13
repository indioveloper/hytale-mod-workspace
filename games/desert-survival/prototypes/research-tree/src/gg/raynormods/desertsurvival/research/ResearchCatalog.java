package gg.raynormods.desertsurvival.research;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ResearchCatalog {
  private static final Map<String, ResearchNode> BY_ID = build();

  private ResearchCatalog() {}

  public static ResearchNode get(String id) {
    return BY_ID.get(id);
  }

  public static List<ResearchNode> all() {
    return List.copyOf(BY_ID.values());
  }

  public static List<ResearchNode> forSector(ResearchSector sector) {
    return BY_ID.values().stream().filter(node -> node.sector() == sector).toList();
  }

  private static Map<String, ResearchNode> build() {
    LinkedHashMap<String, ResearchNode> nodes = new LinkedHashMap<>();

    // Water discipline: preserved as individual Dune-inspired unlocks.
    add(nodes, "AG-0.1", ResearchSector.WATER, 0, "Kit básico de exanguinación", "Extrae sangre como recurso de emergencia.", 0);
    add(nodes, "AG-0.2", ResearchSector.WATER, 0, "Purificador de sangre", "Convierte sangre procesada en agua utilizable.", 1, "AG-0.1");
    add(nodes, "AG-1.1", ResearchSector.WATER, 1, "Destiltraje tier 1", "Desbloquea el primer set de reciclaje de agua.", 1, "AG-0.2");
    add(nodes, "AG-1.2", ResearchSector.WATER, 1, "Literjon", "Recipiente portátil de agua de mayor capacidad.", 1, "AG-0.2");
    add(nodes, "AG-1.3", ResearchSector.WATER, 1, "Cisterna", "Almacenamiento fijo de agua para la base.", 1, "AG-1.2");
    add(nodes, "AG-2.1", ResearchSector.WATER, 2, "Destiltraje Kirab", "Mejora de recuperación y resistencia térmica.", 1, "AG-1.1");
    add(nodes, "AG-2.2", ResearchSector.WATER, 2, "Recolector de rocío", "Genera agua lentamente desde la humedad ambiental.", 1, "AG-1.3");
    add(nodes, "AG-2.3", ResearchSector.WATER, 2, "Extractor de sangre", "Versión mejorada del kit de exanguinación.", 1, "AG-0.1");
    add(nodes, "AG-2.4", ResearchSector.WATER, 2, "Saco de sangre", "Almacena sangre antes de procesarla.", 1, "AG-2.3");
    add(nodes, "AG-2.5", ResearchSector.WATER, 2, "Purificador mejorado", "Procesa sangre con mayor eficiencia.", 1, "AG-0.2");

    // Combat: deliberately compressed armor taxonomy for the MVP.
    add(nodes, "CO-0.1", ResearchSector.COMBAT, 0, "Armadura de chatarra", "Set defensivo improvisado de tier 0.", 0);
    add(nodes, "CO-0.2", ResearchSector.COMBAT, 0, "Kindjal tier 0", "Hoja corta básica.", 0);
    add(nodes, "CO-0.3", ResearchSector.COMBAT, 0, "Pistola Maula tier 0", "Arma de proyectiles básica.", 0);
    add(nodes, "CO-0.4", ResearchSector.COMBAT, 0, "Healkit tier 0", "Curación de emergencia básica.", 0);
    add(nodes, "CO-1.1", ResearchSector.COMBAT, 1, "Armadura tier 1", "Set defensivo de cobre.", 1, "CO-0.1");
    add(nodes, "CO-1.2", ResearchSector.COMBAT, 1, "Kindjal tier 1", "Hoja corta mejorada.", 1, "CO-0.2");
    add(nodes, "CO-1.3", ResearchSector.COMBAT, 1, "Pistola Maula tier 1", "Pistola mejorada para la zona de cobre.", 1, "CO-0.3");
    add(nodes, "CO-1.4", ResearchSector.COMBAT, 1, "Karpov 38 tier 1", "Rifle de entrada para combate a distancia.", 1, "CO-0.3");
    add(nodes, "CO-2.1", ResearchSector.COMBAT, 2, "Armadura tier 2", "Set defensivo unico de hierro.", 1, "CO-1.1");
    add(nodes, "CO-2.2", ResearchSector.COMBAT, 2, "Escudo Holtzman", "Protección energética simplificada para el MVP.", 1, "CO-1.1");
    add(nodes, "CO-2.3", ResearchSector.COMBAT, 2, "Kindjal tier 2", "Hoja corta de hierro.", 1, "CO-1.2");
    add(nodes, "CO-2.4", ResearchSector.COMBAT, 2, "Espada tier 2", "Arma cuerpo a cuerpo pesada.", 1, "CO-1.2");
    add(nodes, "CO-2.5", ResearchSector.COMBAT, 2, "Pistola Maula tier 2", "Pistola del tier de hierro.", 1, "CO-1.3");
    add(nodes, "CO-2.6", ResearchSector.COMBAT, 2, "Karpov 38 tier 2", "Rifle mejorado del tier de hierro.", 1, "CO-1.4");

    // Construction and fabrication.
    add(nodes, "CN-0.1", ResearchSector.CONSTRUCTION, 0, "Power Pack básico", "Fuente de energía portátil de chatarra.", 0);
    add(nodes, "CN-0.2", ResearchSector.CONSTRUCTION, 0, "Cutterray tier 1", "Corta chatarra y cobre.", 0, "CN-0.1");
    add(nodes, "CN-0.3", ResearchSector.CONSTRUCTION, 0, "Fabricador", "Banco central de fabricación.", 0);
    add(nodes, "CN-0.4", ResearchSector.CONSTRUCTION, 0, "Refinería pequeña de mineral", "Procesa los primeros minerales.", 1, "CN-0.3");
    add(nodes, "CN-0.5", ResearchSector.CONSTRUCTION, 0, "Refinería química pequeña", "Procesa compuestos y consumibles.", 1, "CN-0.3");
    add(nodes, "CN-1.1", ResearchSector.CONSTRUCTION, 1, "Power Pack tier 2", "Energía portátil del tier de cobre.", 1, "CN-0.1");
    add(nodes, "CN-1.2", ResearchSector.CONSTRUCTION, 1, "Cutterray tier 2", "Habilita la extracción de hierro.", 1, "CN-0.2");
    add(nodes, "CN-1.3", ResearchSector.CONSTRUCTION, 1, "Cofre", "Almacenamiento básico de base.", 1, "CN-0.3");
    add(nodes, "CN-2.1", ResearchSector.CONSTRUCTION, 2, "Power Pack tier 3", "Energía portátil del tier de hierro.", 1, "CN-1.1");
    add(nodes, "CN-2.2", ResearchSector.CONSTRUCTION, 2, "Cutterray tier 3", "Habilita la extracción de aluminio.", 1, "CN-1.2");
    add(nodes, "CN-2.3", ResearchSector.CONSTRUCTION, 2, "Compactador estático", "Compacta arena sin recompensa AQL.", 1, "CN-1.2");
    add(nodes, "CN-2.4", ResearchSector.CONSTRUCTION, 2, "Estación de reparación", "Repara equipo y herramientas en la base.", 1, "CN-1.3");

    // Exploration.
    add(nodes, "EX-0.1", ResearchSector.EXPLORATION, 0, "Prismáticos", "Observación a larga distancia.", 0);
    add(nodes, "EX-0.2", ResearchSector.EXPLORATION, 0, "Luz portátil", "Iluminación personal reutilizable.", 0);
    add(nodes, "EX-0.3", ResearchSector.EXPLORATION, 0, "Baliza de reaparición", "Nodo provisional; requiere diseño separado.", 1);
    add(nodes, "EX-1.1", ResearchSector.EXPLORATION, 1, "Cinturón suspensor pasivo", "Versión simplificada para reducir daño de caída.", 1, "EX-0.1");
    add(nodes, "EX-1.2", ResearchSector.EXPLORATION, 1, "Kit de topografía", "Versión simplificada para marcar puntos de interés.", 1, "EX-0.1");

    // Vehicles.
    add(nodes, "VE-1.1", ResearchSector.VEHICLES, 1, "Moto de arena tier 1", "Vehículo personal simplificado.", 1);
    add(nodes, "VE-1.2", ResearchSector.VEHICLES, 1, "Pistola de recuperar vehículos", "Recupera un vehículo atascado o abandonado.", 1, "VE-1.1");
    add(nodes, "VE-2.1", ResearchSector.VEHICLES, 2, "Ruedas mejoradas", "Mejora tracción y estabilidad.", 1, "VE-1.1");
    add(nodes, "VE-2.2", ResearchSector.VEHICLES, 2, "Motor mejorado", "Aumenta velocidad y aceleración.", 1, "VE-1.1");
    add(nodes, "VE-2.3", ResearchSector.VEHICLES, 2, "Disipador mejorado", "Reduce la acumulación de calor del vehículo.", 1, "VE-1.1");
    add(nodes, "VE-2.4", ResearchSector.VEHICLES, 2, "Módulo de maletero", "Añade almacenamiento al vehículo.", 1, "VE-1.1");
    add(nodes, "VE-2.5", ResearchSector.VEHICLES, 2, "Módulo de turbo", "Impulso temporal; requiere el motor mejorado.", 1, "VE-2.2");

    if (nodes.size() != 48) {
      throw new IllegalStateException("Expected 48 MVP research nodes, found " + nodes.size());
    }
    return Collections.unmodifiableMap(nodes);
  }

  private static void add(
      Map<String, ResearchNode> nodes,
      String id,
      ResearchSector sector,
      int tier,
      String name,
      String description,
      int cost,
      String... prerequisites) {
    ResearchNode node = new ResearchNode(
        id, sector, tier, name, description, cost, List.of(prerequisites));
    if (nodes.put(id, node) != null) {
      throw new IllegalStateException("Duplicate research node: " + id);
    }
  }
}
