package gg.raynormods.desertsurvival.cutterray;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.AudioComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.DynamicLight;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.entity.component.PropComponent;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SetBlockSettings;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.joml.Vector3d;
import org.joml.Vector3i;

/** Coordinates the five-second extraction sequence for prototype resource nodes. */
final class CutterrayExtractionManager {
  private static final Map<String, ResourceNodeDefinition> RESOURCE_NODES = Map.of(
      "Resource_Node_Copper_Prototype",
      new ResourceNodeDefinition("Ore_Copper", 1),
      "Resource_Node_Iron_Prototype",
      new ResourceNodeDefinition("Ore_Iron", 2));
  private static final String FLOW_PARTICLE_ID = "Cutterray_Flow_Pulse";
  private static final String BEAM_LOOP_SOUND_ID = "SFX_Cutterray_Beam_Loop";
  private static final String ROCK_MELT_SOUND_ID = "SFX_Cutterray_Rock_Melt";
  private static final String ROCK_BREAK_SOUND_ID = "SFX_Stone_Break";
  private static final String ANCHOR_MODEL_ID =
      "Blocks/Raynor/ResourceNodes/Copper_Node_Prototype.blockymodel";
  private static final long CONTACT_GRACE_NANOS = TimeUnit.MILLISECONDS.toNanos(650);
  private static final long SOUND_INTERVAL_NANOS = TimeUnit.MILLISECONDS.toNanos(700);
  private static final Map<NodeKey, Session> SESSIONS = new HashMap<>();
  private static final Map<Ref<EntityStore>, Ref<EntityStore>> CONTACT_ANCHORS =
      new HashMap<>();

  private CutterrayExtractionManager() {
  }

  static void touch(
      World world,
      Ref<EntityStore> source,
      Vector3i target,
      ItemStack itemInHand,
      ComponentAccessor<EntityStore> accessor) {
    touchGenericContact(world, source, target, accessor);
    ResourceNodeDefinition node = getResourceNode(world, target);
    if (node == null) {
      return;
    }

    long now = System.nanoTime();
    NodeKey key = new NodeKey(world.getName(), target.x, target.y, target.z);
    Session session;
    boolean startSession = false;
    CutterrayTierProfile profile = CutterrayTierProfile.fromItem(itemInHand);
    if (profile.tier < node.requiredCutterTier) {
      return;
    }
    synchronized (SESSIONS) {
      session = SESSIONS.get(key);
      if (session == null
          || now - session.lastContactNanos > CONTACT_GRACE_NANOS
          || session.node != node) {
        session = new Session(world, new Vector3i(target), source, profile, node, now);
        SESSIONS.put(key, session);
        startSession = true;
      } else {
        session.lastContactNanos = now;
        session.lastSource = source;
        session.profile = profile;
      }
    }

    spawnFlowParticles(source, session, accessor);
    playRockMeltSound(session, accessor, now);
    if (startSession) {
      scheduleExtraction(key, session);
    }
  }

  private static void scheduleExtraction(NodeKey key, Session session) {
    for (int second = 1; second <= 5; second++) {
      int step = second;
      session.world.scheduleAfter(
          () -> runExtractionStep(key, session, step),
          second,
          TimeUnit.SECONDS);
    }
  }

  private static void runExtractionStep(NodeKey key, Session session, int step) {
    long now = System.nanoTime();
    synchronized (SESSIONS) {
      if (SESSIONS.get(key) != session) {
        return;
      }
      if (now - session.lastContactNanos > CONTACT_GRACE_NANOS
          || getResourceNode(session.world, session.target) != session.node) {
        SESSIONS.remove(key);
        return;
      }
    }

    showDamageNumber(session);
    if (step < 5) {
      giveResource(session, session.profile.pulseYield);
      return;
    }

    if (removeNode(session.world, session.target, session.node)) {
      giveResource(
          session,
          ThreadLocalRandom.current().nextInt(
              session.profile.finalYieldMin,
              session.profile.finalYieldMax + 1));
      playSoundAtTarget(session, ROCK_BREAK_SOUND_ID, 1.0f, 0.9f);
    }
    synchronized (SESSIONS) {
      if (SESSIONS.get(key) == session) {
        SESSIONS.remove(key);
      }
    }
  }

  private static void touchGenericContact(
      World world,
      Ref<EntityStore> source,
      Vector3i target,
      ComponentAccessor<EntityStore> accessor) {
    var store = world.getEntityStore().getStore();
    Ref<EntityStore> anchor;
    synchronized (CONTACT_ANCHORS) {
      anchor = CONTACT_ANCHORS.get(source);
      if (anchor == null || !anchor.isValid()) {
        anchor = createContactAnchor(world, target);
        if (anchor != null) {
          CONTACT_ANCHORS.put(source, anchor);
        }
      }
    }
    if (anchor == null || !anchor.isValid()) {
      return;
    }
    store.putComponent(
        anchor,
        TransformComponent.getComponentType(),
        new TransformComponent(
            new Vector3d(target.x + 0.5, target.y + 0.65, target.z + 0.5),
            new Rotation3f()));
    store.putComponent(
        anchor,
        CutterrayContactAnchorComponent.getComponentType(),
        new CutterrayContactAnchorComponent(12));
  }

  private static Ref<EntityStore> createContactAnchor(World world, Vector3i target) {
    ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(ANCHOR_MODEL_ID);
    int soundIndex = SoundEvent.getAssetMap().getIndex(BEAM_LOOP_SOUND_ID);
    if (modelAsset == null || soundIndex < 0) {
      return null;
    }
    Model model = Model.createStaticScaledModel(modelAsset, 0.001f);
    var store = world.getEntityStore().getStore();
    var holder = store.getRegistry().newHolder();
    Rotation3f rotation = new Rotation3f();
    holder.addComponent(
        TransformComponent.getComponentType(),
        new TransformComponent(
            new Vector3d(target.x + 0.5, target.y + 0.65, target.z + 0.5),
            rotation));
    holder.addComponent(
        NetworkId.getComponentType(),
        new NetworkId(world.getEntityStore().takeNextNetworkId()));
    holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
    holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));
    holder.addComponent(MovementStatesComponent.getComponentType(), new MovementStatesComponent());
    holder.addComponent(Intangible.getComponentType(), Intangible.INSTANCE);
    holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(rotation));
    holder.addComponent(PropComponent.getComponentType(), PropComponent.get());
    holder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(1.0f));
    holder.addComponent(
        DynamicLight.getComponentType(),
        new DynamicLight(new ColorLight((byte) 5, (byte) 58, (byte) 225, (byte) 255)));
    AudioComponent audio = new AudioComponent();
    audio.addSound(soundIndex);
    holder.addComponent(AudioComponent.getComponentType(), audio);
    holder.addComponent(
        CutterrayContactAnchorComponent.getComponentType(),
        new CutterrayContactAnchorComponent(12));
    holder.ensureComponent(UUIDComponent.getComponentType());
    return store.addEntity(holder, AddReason.SPAWN);
  }

  private static void spawnFlowParticles(
      Ref<EntityStore> source,
      Session session,
      ComponentAccessor<EntityStore> accessor) {
    TransformComponent transform = accessor.getComponent(
        source, TransformComponent.getComponentType());
    if (transform == null) {
      return;
    }

    Vector3d end = new Vector3d(
        session.target.x + 0.5,
        session.target.y + 0.5,
        session.target.z + 0.5);
    Vector3d start = new Vector3d(transform.getPosition()).add(0.0, 1.35, 0.0);
    Vector3d direction = new Vector3d(end).sub(start);
    double distance = direction.length();
    if (distance < 0.01) {
      return;
    }
    direction.div(distance);
    start.fma(0.55, direction);

    synchronized (SESSIONS) {
      session.flowPhase = (session.flowPhase + 0.18) % 1.0;
      for (int pulse = 0; pulse < 4; pulse++) {
        double fraction = (session.flowPhase + pulse * 0.24) % 1.0;
        Vector3d position = new Vector3d(start).lerp(end, fraction);
        ParticleUtil.spawnParticleEffect(FLOW_PARTICLE_ID, position, accessor);
      }
    }
  }

  private static void playRockMeltSound(
      Session session,
      ComponentAccessor<EntityStore> accessor,
      long now) {
    synchronized (SESSIONS) {
      if (now - session.lastSoundNanos < SOUND_INTERVAL_NANOS) {
        return;
      }
      session.lastSoundNanos = now;
    }
    int soundIndex = SoundEvent.getAssetMap().getIndex(ROCK_MELT_SOUND_ID);
    if (soundIndex < 0) {
      return;
    }
    SoundUtil.playSoundEvent3d(
        soundIndex,
        SoundCategory.SFX,
        session.target.x + 0.5,
        session.target.y + 0.65,
        session.target.z + 0.5,
        0.82f,
        1.22f,
        accessor);
  }

  private static void playSoundAtTarget(
      Session session, String soundId, float volume, float pitch) {
    int soundIndex = SoundEvent.getAssetMap().getIndex(soundId);
    if (soundIndex < 0) {
      return;
    }
    SoundUtil.playSoundEvent3d(
        soundIndex,
        SoundCategory.SFX,
        session.target.x + 0.5,
        session.target.y + 0.65,
        session.target.z + 0.5,
        volume,
        pitch,
        session.world.getEntityStore().getStore());
  }

  private static ResourceNodeDefinition getResourceNode(World world, Vector3i position) {
    BlockSection section = getBlockSection(world, position);
    if (section == null) {
      return null;
    }
    BlockType blockType = BlockType.getAssetMap().getAsset(
        section.get(position.x, position.y, position.z));
    return blockType == null ? null : RESOURCE_NODES.get(blockType.getId());
  }

  private static boolean removeNode(
      World world, Vector3i position, ResourceNodeDefinition expectedNode) {
    ChunkStore chunkStore = world.getChunkStore();
    Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReferenceAtBlock(
        position.x, position.y, position.z);
    if (sectionRef == null
        || !sectionRef.isValid()
        || getResourceNode(world, position) != expectedNode) {
      return false;
    }
    int settings = SetBlockSettings.PERFORM_BLOCK_UPDATE | SetBlockSettings.NO_DROP_ITEMS;
    return BlockOperations.setBlock(
        chunkStore,
        sectionRef,
        position.x,
        position.y,
        position.z,
        BlockType.EMPTY_ID,
        BlockType.EMPTY,
        0,
        0,
        settings);
  }

  private static BlockSection getBlockSection(World world, Vector3i position) {
    ChunkStore chunkStore = world.getChunkStore();
    Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReferenceAtBlock(
        position.x, position.y, position.z);
    if (sectionRef == null || !sectionRef.isValid()) {
      return null;
    }
    return chunkStore.getStore().getComponent(sectionRef, BlockSection.getComponentType());
  }

  private static void giveResource(Session session, int quantity) {
    Ref<EntityStore> source = session.lastSource;
    if (quantity <= 0 || source == null || !source.isValid()) {
      return;
    }
    var store = session.world.getEntityStore().getStore();
    Vector3d pickupOrigin = new Vector3d(
        session.target.x + 0.5,
        session.target.y + 0.8,
        session.target.z + 0.5);
    ItemUtils.interactivelyPickupItem(
        source, new ItemStack(session.node.oreItemId, quantity), pickupOrigin, store);
  }

  private static Ref<EntityStore> createDamageText(
      World world, Vector3i target, int damage) {
    ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(ANCHOR_MODEL_ID);
    if (modelAsset == null) {
      return null;
    }
    Model model = Model.createStaticScaledModel(modelAsset, 0.001f);
    var store = world.getEntityStore().getStore();
    var holder = store.getRegistry().newHolder();
    Rotation3f rotation = new Rotation3f();
    Vector3d position = new Vector3d(
        target.x + 0.5 + ThreadLocalRandom.current().nextDouble(-0.16, 0.17),
        target.y + 1.85 + ThreadLocalRandom.current().nextDouble(0.0, 0.18),
        target.z + 0.5 + ThreadLocalRandom.current().nextDouble(-0.16, 0.17));
    holder.addComponent(
        TransformComponent.getComponentType(), new TransformComponent(position, rotation));
    holder.addComponent(
        NetworkId.getComponentType(),
        new NetworkId(world.getEntityStore().takeNextNetworkId()));
    holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
    holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));
    holder.addComponent(MovementStatesComponent.getComponentType(), new MovementStatesComponent());
    holder.addComponent(Intangible.getComponentType(), Intangible.INSTANCE);
    holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(rotation));
    holder.addComponent(PropComponent.getComponentType(), PropComponent.get());
    holder.addComponent(
        EntityScaleComponent.getComponentType(), new EntityScaleComponent(1.0f));
    holder.addComponent(Nameplate.getComponentType(), new Nameplate(Integer.toString(damage)));
    holder.ensureComponent(UUIDComponent.getComponentType());
    return store.addEntity(holder, AddReason.SPAWN);
  }

  private static void showDamageNumber(Session session) {
    Ref<EntityStore> anchor = createDamageText(
        session.world, session.target, session.profile.damagePerSecond);
    if (anchor == null || !anchor.isValid()) {
      return;
    }
    session.world.scheduleAfter(
        () -> {
          if (anchor.isValid()) {
            session.world.getEntityStore().getStore().removeEntity(anchor, RemoveReason.REMOVE);
          }
        },
        850,
        TimeUnit.MILLISECONDS);
  }

  private record NodeKey(String worldName, int x, int y, int z) {
  }

  private record ResourceNodeDefinition(String oreItemId, int requiredCutterTier) {
  }

  private static final class Session {
    private final World world;
    private final Vector3i target;
    private volatile long lastContactNanos;
    private volatile Ref<EntityStore> lastSource;
    private volatile CutterrayTierProfile profile;
    private final ResourceNodeDefinition node;
    private double flowPhase;
    private long lastSoundNanos;

    private Session(
        World world,
        Vector3i target,
        Ref<EntityStore> lastSource,
        CutterrayTierProfile profile,
        ResourceNodeDefinition node,
        long lastContactNanos) {
      this.world = world;
      this.target = target;
      this.lastSource = lastSource;
      this.profile = profile;
      this.node = node;
      this.lastContactNanos = lastContactNanos;
    }
  }
}
