package gg.raynormods.desertsurvival.research;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;

/**
 * Research wheel: one disc with five 72-degree wedges (one per sector) and
 * three concentric bands (tier 0 inner, tier 1 middle, tier 2 outer). All 48
 * nodes are visible at once; the right panel describes the selected node.
 */
public final class ResearchTreePage
    extends InteractiveCustomUIPage<ResearchTreePage.PageEventData> {
  // Geometry in pixels of the 790x620 wheel group of ResearchTree.ui.
  // ResearchWheel.png and ResearchLinks.png (580 px) are drawn at (105, 20), so the
  // hub sits at (395, 310). Band limits: hub 58, tier 0 to 150, tier 1 to 225,
  // tier 2 to 288. Nodes alternate between one, two or three radii inside their
  // band so that neighbours in a crowded wedge do not overlap.
  static final int WHEEL_CENTER_X = 395;
  static final int WHEEL_CENTER_Y = 310;
  static final int NODE_WIDTH = 32;
  static final int NODE_HEIGHT = 32;
  private static final int[][][] BAND_RADII = {
      {{104}, {84, 124}, {80, 115, 145}},
      {{188}, {168, 208}, {160, 188, 214}},
      {{256}, {240, 272}, {234, 256, 278}},
  };

  private final String selectedNodeId;
  private final String feedback;

  public ResearchTreePage(PlayerRef playerRef) {
    this(playerRef, "AG-0.1", "");
  }

  private ResearchTreePage(PlayerRef playerRef, String selectedNodeId, String feedback) {
    super(playerRef, CustomPageLifetime.CanDismiss, PageEventData.CODEC);
    this.selectedNodeId = selectedNodeId;
    this.feedback = feedback;
  }

  @Override
  public void build(
      Ref<EntityStore> ref,
      UICommandBuilder commands,
      UIEventBuilder events,
      Store<EntityStore> store) {
    commands.append("Pages/DesertResearch/ResearchTree.ui");
    ResearchProgressComponent progress = ResearchService.getOrCreate(ref, store);

    commands.set("#IntelValue.Text", "INTEL " + progress.getAvailableIntel());
    commands.set("#ResearchCount.Text", progress.getUnlockedCount() + "/48 investigados");
    commands.set("#Feedback.Text", feedback);
    bindAction(events, "#CloseButton", "Close", "");

    // Every node is appended once. The appended root is addressed as
    // "#RingNodes[i]"; adding its own id would select a descendant and the
    // client disconnects (seen in 0.1.7).
    int index = 0;
    for (ResearchSector sector : ResearchSector.values()) {
      commands.set("#Sector" + sector.code() + ".Text", sector.shortLabel().toUpperCase());
      List<ResearchNode> sectorNodes = ResearchCatalog.forSector(sector);
      int[] tierCounts = new int[3];
      for (ResearchNode node : sectorNodes) {
        tierCounts[node.tier()]++;
      }
      int[] positions = new int[3];
      for (ResearchNode node : sectorNodes) {
        commands.append("#RingNodes", "Pages/DesertResearch/" + nodeDocument(progress, node));
        String selector = "#RingNodes[" + index++ + "]";
        int position = positions[node.tier()]++;
        commands.setObject(selector + ".Anchor",
            ringAnchor(sector.ordinal(), node.tier(), position, tierCounts[node.tier()]));
        bindAction(events, selector, "Select", node.id());
      }
    }

    ResearchNode selected = ResearchCatalog.get(selectedNodeId);
    if (selected == null) {
      selected = ResearchCatalog.forSector(ResearchSector.WATER).get(0);
    }
    commands.set("#NodeName.Text", selected.name());
    commands.set("#NodeMeta.Text", costLabel(selected));
    commands.set("#NodeDescription.Text", selected.description());
    commands.set("#NodeStatus.Text", statusLabel(progress, selected));
    commands.set("#ResearchButton.Text", buttonLabel(progress, selected));
    commands.set("#ResearchButton.Disabled", !isAvailable(progress, selected));
    bindAction(events, "#ResearchButton", "Research", selected.id());
  }

  @Override
  public void handleDataEvent(
      Ref<EntityStore> ref, Store<EntityStore> store, PageEventData data) {
    if (data == null || data.action == null) {
      return;
    }
    switch (data.action) {
      case "Close" -> close();
      case "Select" -> {
        ResearchNode node = ResearchCatalog.get(data.value);
        if (node != null) {
          reopen(ref, store, node.id(), "");
        }
      }
      case "Research" -> {
        ResearchService.Result result = ResearchService.research(ref, store, selectedNodeId);
        reopen(ref, store, selectedNodeId, result.message());
      }
      default -> {}
    }
  }

  private void reopen(
      Ref<EntityStore> ref, Store<EntityStore> store, String nodeId, String message) {
    Player player = store.getComponent(ref, Player.getComponentType());
    if (player == null) {
      return;
    }
    player.getPageManager().openCustomPage(
        ref, store, new ResearchTreePage(playerRef, nodeId, message));
  }

  /** Position of a node: wedge of its sector, band of its tier, slot by order. */
  static Anchor ringAnchor(int sectorIndex, int tier, int position, int count) {
    int[] radii = BAND_RADII[tier][count <= 2 ? 0 : count <= 4 ? 1 : 2];
    int radius = radii[position % radii.length];
    double wedgeStart = Math.toRadians(-90.0 + 72.0 * sectorIndex);
    double angle = wedgeStart + Math.toRadians(72.0) * (position + 0.5) / count;
    Anchor anchor = new Anchor();
    anchor.setLeft(Value.of((int) Math.round(
        WHEEL_CENTER_X + radius * Math.cos(angle) - NODE_WIDTH / 2.0)));
    anchor.setTop(Value.of((int) Math.round(
        WHEEL_CENTER_Y + radius * Math.sin(angle) - NODE_HEIGHT / 2.0)));
    anchor.setWidth(Value.of(NODE_WIDTH));
    anchor.setHeight(Value.of(NODE_HEIGHT));
    return anchor;
  }

  private static String costLabel(ResearchNode node) {
    return node.intelCost() == 0 ? "GRATIS" : node.intelCost() + " INTEL";
  }

  private static boolean isAvailable(ResearchProgressComponent progress, ResearchNode node) {
    return !progress.isUnlocked(node.id())
        && progress.hasPrerequisites(node)
        && progress.getAvailableIntel() >= node.intelCost();
  }

  private static String nodeDocument(ResearchProgressComponent progress, ResearchNode node) {
    if (progress.isUnlocked(node.id())) {
      return "ResearchRingNodeResearched.ui";
    }
    return isAvailable(progress, node)
        ? "ResearchRingNode.ui" : "ResearchRingNodeBlocked.ui";
  }

  private static String statusLabel(
      ResearchProgressComponent progress, ResearchNode node) {
    if (progress.isUnlocked(node.id())) {
      return "INVESTIGADO";
    }
    if (!progress.hasPrerequisites(node)) {
      return "BLOQUEADO";
    }
    if (progress.getAvailableIntel() < node.intelCost()) {
      return "BLOQUEADO";
    }
    return "DISPONIBLE";
  }

  private static String buttonLabel(
      ResearchProgressComponent progress, ResearchNode node) {
    if (progress.isUnlocked(node.id())) {
      return "Hecho";
    }
    return node.intelCost() == 0 ? "Activar" : "Investigar";
  }

  private static void bindAction(
      UIEventBuilder events, String selector, String action, String value) {
    events.addEventBinding(
        CustomUIEventBindingType.Activating,
        selector,
        new EventData().append("Action", action).append("Value", value),
        false);
  }

  public static final class PageEventData {
    public static final BuilderCodec<PageEventData> CODEC =
        BuilderCodec.builder(PageEventData.class, PageEventData::new)
            .append(
                new KeyedCodec<>("Action", Codec.STRING, false),
                (data, value) -> data.action = value,
                data -> data.action)
            .add()
            .append(
                new KeyedCodec<>("Value", Codec.STRING, false),
                (data, value) -> data.value = value,
                data -> data.value)
            .add()
            .build();

    public String action;
    public String value;
  }
}
