package gg.raynormods.desertsurvival.research;

public enum ResearchSector {
  WATER("Disciplina del Agua", "Disciplina del Agua", "AG"),
  COMBAT("Combate", "Combate", "CO"),
  CONSTRUCTION("Construcción y fabricación", "Fabricación", "CN"),
  EXPLORATION("Exploración", "Exploración", "EX"),
  VEHICLES("Vehículos", "Vehículos", "VE");

  private final String label;
  private final String shortLabel;
  private final String code;

  ResearchSector(String label, String shortLabel, String code) {
    this.label = label;
    this.shortLabel = shortLabel;
    this.code = code;
  }

  public String label() {
    return label;
  }

  /** Short name that fits the sector selector buttons. */
  public String shortLabel() {
    return shortLabel;
  }

  public String code() {
    return code;
  }
}
