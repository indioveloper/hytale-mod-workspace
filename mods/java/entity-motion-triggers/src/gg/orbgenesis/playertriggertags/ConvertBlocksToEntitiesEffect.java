package gg.orbgenesis.playertriggertags;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.shape.TriggerVolumeShape;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.BlockEntity;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.PropComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entity.item.PreventItemMerging;
import com.hypixel.hytale.server.core.modules.entity.item.PreventPickup;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.entity.hitboxcollision.HitboxCollisionConfig;
import com.hypixel.hytale.server.core.universe.world.SetBlockSettings;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import org.joml.Vector3d;

/** Replaces ordinary blocks inside a Trigger Volume with static prop entities. */
public class ConvertBlocksToEntitiesEffect extends TriggerEffect {
  public enum CollisionMode {
    NONE,
    HARD,
    SOFT
  }

  public static final BuilderCodec<ConvertBlocksToEntitiesEffect> CODEC =
      BuilderCodec.builder(
              ConvertBlocksToEntitiesEffect.class,
              ConvertBlocksToEntitiesEffect::new,
              TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("Item", Codec.STRING),
              (effect, value) -> effect.itemId = value,
              effect -> effect.itemId)
          .add()
          .append(
              new KeyedCodec<>("Collision", new EnumCodec<>(CollisionMode.class)),
              (effect, value) -> effect.collisionMode = value != null ? value : CollisionMode.NONE,
              effect -> effect.collisionMode)
          .add()
          .build();

  private String itemId;
  private CollisionMode collisionMode = CollisionMode.NONE;

  @Override
  public void execute(TriggerContext context) {
    Item item = itemId != null ? Item.getAssetMap().getAsset(itemId) : null;
    if (item == null) {
      return;
    }
    HitboxCollisionConfig collisionConfig = resolveCollisionConfig();

    var world = context.getStore().getExternalData().getWorld();
    var chunkStore = world.getChunkStore();
    var chunkComponentStore = chunkStore.getStore();
    var min = new Vector3d();
    var max = new Vector3d();
    var blockCenter = new Vector3d();

    for (var volume : context.getSpatialVolumes()) {
      TriggerVolumeShape shape = volume.getShape();
      Vector3d origin = volume.getPosition();
      shape.getWorldAABB(origin, min, max);
      for (int x = MathUtil.floor(min.x()); x <= MathUtil.floor(max.x()); x++) {
        for (int y = MathUtil.floor(min.y()); y <= MathUtil.floor(max.y()); y++) {
          if (y < ChunkUtil.MIN_Y || y >= ChunkUtil.HEIGHT) {
            continue;
          }
          for (int z = MathUtil.floor(min.z()); z <= MathUtil.floor(max.z()); z++) {
            blockCenter.set(x + 0.5D, y + 0.5D, z + 0.5D);
            if (!shape.contains(origin, blockCenter)) {
              continue;
            }
            convertBlock(
                context.getStore(),
                chunkStore,
                chunkComponentStore,
                item,
                itemId,
                collisionConfig,
                x,
                y,
                z);
          }
        }
      }
    }
  }

  private HitboxCollisionConfig resolveCollisionConfig() {
    return switch (collisionMode) {
      case HARD -> HitboxCollisionConfig.getAssetMap().getAsset("HardCollision");
      case SOFT -> HitboxCollisionConfig.getAssetMap().getAsset("SoftCollision");
      case NONE -> null;
    };
  }

  private static void convertBlock(
      Store<EntityStore> entityStore,
      ChunkStore chunkStore,
      Store<ChunkStore> chunkComponentStore,
      Item item,
      String itemId,
      HitboxCollisionConfig collisionConfig,
      int x,
      int y,
      int z) {
    Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(ChunkUtil.indexChunkFromBlock(x, z));
    if (chunkRef == null || !chunkRef.isValid()) {
      return;
    }
    BlockChunk blockChunk = chunkComponentStore.getComponent(chunkRef, BlockChunk.getComponentType());
    WorldChunk worldChunk = chunkComponentStore.getComponent(chunkRef, WorldChunk.getComponentType());
    if (blockChunk == null || worldChunk == null) {
      return;
    }
    BlockSection section = blockChunk.getSectionAtBlockY(y);
    if (blockChunk.getBlock(x, y, z) == BlockType.EMPTY_ID
        || section.getFiller(x, y, z) != FillerBlockUtil.NO_FILLER) {
      return;
    }

    Rotation3f rotation = new Rotation3f();
    RotationTuple.get(section.getRotationIndex(x, y, z)).applyRotationTo(rotation);
    var holder = entityStore.getRegistry().newHolder();
    holder.addComponent(
        TransformComponent.getComponentType(),
        new TransformComponent(new Vector3d(x + 0.5D, y + 0.5D, z + 0.5D), rotation));

    String modelId = resolveItemModelId(item);
    ModelAsset modelAsset = modelId != null ? ModelAsset.getAssetMap().getAsset(modelId) : null;
    if (modelAsset != null) {
      Model model = Model.createStaticScaledModel(modelAsset, 1.0F);
      holder.addComponent(
          NetworkId.getComponentType(),
          new NetworkId(entityStore.getExternalData().takeNextNetworkId()));
      holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
      holder.addComponent(
          PersistentModel.getComponentType(),
          new PersistentModel(new Model.ModelReference(modelId, 1.0F, null, true)));
      holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(rotation));
    } else if (item.hasBlockType()) {
      // Ordinary block-items are rendered through BlockEntity, not Item.Model.
      holder.addComponent(BlockEntity.getComponentType(), new BlockEntity(itemId));
      holder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(1.0F));
    } else {
      holder.addComponent(
          NetworkId.getComponentType(),
          new NetworkId(entityStore.getExternalData().takeNextNetworkId()));
      holder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(1.0F));
      holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(rotation));
    }

    ItemStack itemStack = new ItemStack(itemId, 1);
    itemStack.setOverrideDroppedItemAnimation(true);
    holder.addComponent(ItemComponent.getComponentType(), new ItemComponent(itemStack));
    holder.addComponent(PreventPickup.getComponentType(), PreventPickup.INSTANCE);
    holder.addComponent(PreventItemMerging.getComponentType(), PreventItemMerging.INSTANCE);
    holder.addComponent(PropComponent.getComponentType(), PropComponent.get());
    holder.ensureComponent(UUIDComponent.getComponentType());
    if (collisionConfig != null) {
      holder.addComponent(
          PendingPlatformCollisionComponent.getComponentType(),
          new PendingPlatformCollisionComponent(collisionConfig.getId(), 2));
    }
    entityStore.addEntity(holder, AddReason.SPAWN);
    worldChunk.setBlock(x, y, z, BlockType.EMPTY_ID, SetBlockSettings.PERFORM_BLOCK_UPDATE);
  }

  private static String resolveItemModelId(Item item) {
    String modelId = item.getModel();
    if (modelId == null && item.hasBlockType()) {
      BlockType blockType = BlockType.getAssetMap().getAsset(item.getId());
      if (blockType != null) {
        modelId = blockType.getCustomModel();
      }
    }
    return modelId;
  }
}
