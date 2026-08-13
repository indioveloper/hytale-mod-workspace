package gg.orbgenesis.configurablespawners;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

final class SpawnerTickSystem extends EntityTickingSystem<ChunkStore> {
  private static final int GROUND_SCAN_DISTANCE = 16;
  private static final String SPAWN_PARTICLE = "Effect_Fire";
  private static final float SPAWN_PARTICLE_DURATION_SECONDS = 0.75f;
  private final Query<ChunkStore> query = Query.and(
      ConfigurableSpawnerComponent.getComponentType(),
      BlockModule.BlockStateInfo.getComponentType());

  @Override
  @Nonnull
  public Query<ChunkStore> getQuery() {
    return query;
  }

  @Override
  public void tick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
      @Nonnull Store<ChunkStore> store,
      @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
    ConfigurableSpawnerComponent config = archetypeChunk.getComponent(
        index, ConfigurableSpawnerComponent.getComponentType());
    BlockModule.BlockStateInfo info = archetypeChunk.getComponent(
        index, BlockModule.BlockStateInfo.getComponentType());
    if (config == null || info == null) return;

    config.normalize();
    config.ensureSpawnerId();
    World world = store.getExternalData().getWorld();
    Ref<ChunkStore> blockRef = archetypeChunk.getReferenceTo(index);
    var blockPosition = SpawnerBlockHelper.getPosition(blockRef, store);
    if (blockPosition == null) return;
    Vector3d center = new Vector3d(blockPosition).add(0.5, 1.0, 0.5);

    config.maintenanceSeconds += dt;
    if (config.maintenanceSeconds >= 1.0) {
      config.maintenanceSeconds = 0.0;
      pruneTracked(config, world);
    }
    if (!config.enabled || config.roleId.isBlank() || !hasNearbyPlayer(world, center, config.activationRadius)) {
      return;
    }

    config.cooldownSeconds -= dt;
    if (config.cooldownSeconds > 0.0) return;

    int roleIndex = NPCPlugin.get().getIndex(config.roleId);
    if (roleIndex < 0 || !NPCPlugin.get().getRoleTemplateNames(true).contains(config.roleId)) {
      config.cooldownSeconds = 2.0;
      return;
    }

    int capacity = config.maxAlive - config.trackedMobs.length;
    if (capacity <= 0) {
      config.cooldownSeconds = 1.0;
      return;
    }
    int requested = randomInclusive(config.spawnCountMin, config.spawnCountMax);
    int spawned = 0;
    var entityStore = world.getEntityStore().getStore();
    ArrayList<UUID> tracked = new ArrayList<>(Arrays.asList(config.trackedMobs));
    for (int i = 0; i < Math.min(requested, capacity); i++) {
      Vector3d position = findSpawnPosition(world, store, entityStore, center, config);
      if (position == null) continue;
      var result = NPCPlugin.get().spawnEntity(
          entityStore,
          roleIndex,
          position,
          new Rotation3f(0.0f, ThreadLocalRandom.current().nextFloat() * (float) (Math.PI * 2.0), 0.0f),
          null,
          (npc, holder, ignored) -> holder.addComponent(
              SpawnedBySpawnerComponent.getComponentType(),
              new SpawnedBySpawnerComponent(config.spawnerId, config)),
          null);
      if (result == null) continue;
      UUIDComponent uuid = entityStore.getComponent(result.first(), UUIDComponent.getComponentType());
      if (uuid != null) tracked.add(uuid.getUuid());
      spawned++;
    }
    config.trackedMobs = tracked.toArray(UUID[]::new);
    if (spawned > 0) {
      ParticleUtil.spawnParticleEffect(
          SPAWN_PARTICLE,
          new Vector3d(center).add(0.0, 0.35, 0.0),
          0.0f,
          0.0f,
          0.0f,
          1.0f,
          SPAWN_PARTICLE_DURATION_SECONDS,
          entityStore);
    }
    config.cooldownSeconds = spawned == 0
        ? 1.0
        : random(config.cadenceMinSeconds, config.cadenceMaxSeconds);
    info.markNeedsSaving();
  }

  private static boolean hasNearbyPlayer(World world, Vector3d center, double radius) {
    double radiusSquared = radius * radius;
    for (var player : world.getPlayerRefs()) {
      if (player.getTransform().getPosition().distanceSquared(center) <= radiusSquared) return true;
    }
    return false;
  }

  private static void pruneTracked(ConfigurableSpawnerComponent config, World world) {
    config.trackedMobs = Arrays.stream(config.trackedMobs)
        .filter(uuid -> {
          Ref<EntityStore> ref = world.getEntityRef(uuid);
          return ref != null && ref.isValid()
              && ref.getStore().getComponent(ref, DeathComponent.getComponentType()) == null;
        })
        .toArray(UUID[]::new);
  }

  private static Vector3d findSpawnPosition(
      World world,
      Store<ChunkStore> chunkComponentStore,
      Store<EntityStore> entityStore,
      Vector3d center,
      ConfigurableSpawnerComponent config) {
    double sunlight = entityStore.getResource(WorldTimeResource.getResourceType()).getSunlightFactor();
    ThreadLocalRandom random = ThreadLocalRandom.current();
    for (int attempt = 0; attempt < config.spawnAttempts; attempt++) {
      double angle = random.nextDouble(Math.PI * 2.0);
      double radius = Math.sqrt(random.nextDouble()) * config.horizontalRadius;
      int x = (int) Math.floor(center.x + Math.cos(angle) * radius);
      int z = (int) Math.floor(center.z + Math.sin(angle) * radius);
      Ref<ChunkStore> chunkRef = world.getChunkStore().getChunkReference(
          ChunkUtil.indexChunkFromBlock(x, z));
      if (chunkRef == null || !chunkRef.isValid()) continue;
      BlockChunk chunk = chunkComponentStore.getComponent(chunkRef, BlockChunk.getComponentType());
      if (chunk == null) continue;
      int y = findGroundY(chunk, x, z, (int) Math.floor(center.y));
      if (y == Integer.MIN_VALUE) continue;
      int light = SpawnerLightLevel.calculate(
          chunk.getBlockLightIntensity(x, y, z),
          chunk.getSkyLight(x, y, z),
          chunk.getHeight(x, z),
          y,
          sunlight);
      if (light > config.maxLight) continue;
      return new Vector3d(x + 0.5, y, z + 0.5);
    }
    return null;
  }

  private static int findGroundY(BlockChunk chunk, int x, int z, int originY) {
    for (int distance = 0; distance <= GROUND_SCAN_DISTANCE; distance++) {
      int below = originY - distance;
      if (isWalkable(chunk, x, below, z)) return below;
      if (distance > 0) {
        int above = originY + distance;
        if (isWalkable(chunk, x, above, z)) return above;
      }
    }
    return Integer.MIN_VALUE;
  }

  private static boolean isWalkable(BlockChunk chunk, int x, int y, int z) {
    BlockType floor = BlockType.getAssetMap().getAsset(chunk.getBlock(x, y - 1, z));
    BlockType body = BlockType.getAssetMap().getAsset(chunk.getBlock(x, y, z));
    BlockType head = BlockType.getAssetMap().getAsset(chunk.getBlock(x, y + 1, z));
    return floor != null && floor.getMaterial() == BlockMaterial.Solid
        && body != null && body.getMaterial() == BlockMaterial.Empty
        && head != null && head.getMaterial() == BlockMaterial.Empty;
  }

  private static int randomInclusive(int minimum, int maximum) {
    return minimum == maximum ? minimum : ThreadLocalRandom.current().nextInt(minimum, maximum + 1);
  }

  private static double random(double minimum, double maximum) {
    return minimum == maximum ? minimum : ThreadLocalRandom.current().nextDouble(minimum, maximum);
  }
}
