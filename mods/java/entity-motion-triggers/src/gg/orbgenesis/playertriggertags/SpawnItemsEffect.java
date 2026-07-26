package gg.orbgenesis.playertriggertags;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
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
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.logging.Level;
import org.joml.Vector3d;

/** Spawns one persistent item prop at configured world or volume-relative coordinates. */
public class SpawnItemsEffect extends TriggerEffect {
  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private static final float BLOCK_ENTITY_BASE_SCALE = 2.0F;
  private static final float MIN_SCALE = 0.01F;

  public enum CoordinateMode {
    ABSOLUTE,
    RELATIVE_TO_VOLUME
  }

  public enum CollisionMode {
    NONE,
    HARD,
    SOFT
  }

  public static final BuilderCodec<SpawnItemsEffect> CODEC =
      BuilderCodec.builder(SpawnItemsEffect.class, SpawnItemsEffect::new, TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("Item", Codec.STRING),
              (effect, value) -> effect.itemId = value,
              effect -> effect.itemId)
          .add()
          .append(
              new KeyedCodec<>("X", Codec.DOUBLE, false),
              (effect, value) -> effect.x = value,
              effect -> effect.x)
          .add()
          .append(
              new KeyedCodec<>("Y", Codec.DOUBLE, false),
              (effect, value) -> effect.y = value,
              effect -> effect.y)
          .add()
          .append(
              new KeyedCodec<>("Z", Codec.DOUBLE, false),
              (effect, value) -> effect.z = value,
              effect -> effect.z)
          .add()
          .append(
              new KeyedCodec<>("CoordinateMode", new EnumCodec<>(CoordinateMode.class), false),
              (effect, value) ->
                  effect.coordinateMode =
                      value != null ? value : CoordinateMode.RELATIVE_TO_VOLUME,
              effect -> effect.coordinateMode)
          .add()
          .append(
              new KeyedCodec<>("RotationX", Codec.FLOAT, false),
              (effect, value) -> effect.rotationX = value,
              effect -> effect.rotationX)
          .add()
          .append(
              new KeyedCodec<>("RotationY", Codec.FLOAT, false),
              (effect, value) -> effect.rotationY = value,
              effect -> effect.rotationY)
          .add()
          .append(
              new KeyedCodec<>("RotationZ", Codec.FLOAT, false),
              (effect, value) -> effect.rotationZ = value,
              effect -> effect.rotationZ)
          .add()
          .append(
              new KeyedCodec<>("Scale", Codec.FLOAT, false),
              (effect, value) -> effect.scale = value,
              effect -> effect.scale)
          .add()
          .append(
              new KeyedCodec<>("Collision", new EnumCodec<>(CollisionMode.class), false),
              (effect, value) ->
                  effect.collisionMode = value != null ? value : CollisionMode.NONE,
              effect -> effect.collisionMode)
          .add()
          .build();

  private String itemId = "";
  private double x;
  private double y;
  private double z;
  private CoordinateMode coordinateMode = CoordinateMode.RELATIVE_TO_VOLUME;
  private float rotationX;
  private float rotationY;
  private float rotationZ;
  private float scale = 1.0F;
  private CollisionMode collisionMode = CollisionMode.NONE;

  @Override
  public void execute(TriggerContext context) {
    Item item = itemId == null ? null : Item.getAssetMap().getAsset(itemId);
    if (item == null || context.getStore() == null) {
      LOGGER.at(Level.WARNING).log("SpawnItems: item '%s' was not found", itemId);
      return;
    }

    Vector3d position = resolvePosition(context);
    if (position == null) {
      LOGGER.at(Level.WARNING).log("SpawnItems: the Trigger Volume origin is unavailable");
      return;
    }

    float resolvedScale = Math.max(MIN_SCALE, scale);
    Rotation3f rotation =
        new Rotation3f(
            (float) Math.toRadians(rotationX),
            (float) Math.toRadians(rotationY),
            (float) Math.toRadians(rotationZ));
    var store = context.getStore();
    var holder = store.getRegistry().newHolder();
    holder.addComponent(
        TransformComponent.getComponentType(), new TransformComponent(position, rotation));

    String modelId = resolveItemModelId(item);
    ModelAsset modelAsset = modelId == null ? null : ModelAsset.getAssetMap().getAsset(modelId);
    if (modelAsset != null) {
      Model model = Model.createStaticScaledModel(modelAsset, resolvedScale);
      holder.addComponent(
          NetworkId.getComponentType(),
          new NetworkId(store.getExternalData().takeNextNetworkId()));
      holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
      holder.addComponent(
          PersistentModel.getComponentType(),
          new PersistentModel(new Model.ModelReference(modelId, resolvedScale, null, true)));
      holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(rotation));
    } else if (item.hasBlockType()) {
      holder.addComponent(BlockEntity.getComponentType(), new BlockEntity(itemId));
      holder.addComponent(
          EntityScaleComponent.getComponentType(),
          new EntityScaleComponent(resolvedScale * BLOCK_ENTITY_BASE_SCALE));
    } else {
      holder.addComponent(
          NetworkId.getComponentType(),
          new NetworkId(store.getExternalData().takeNextNetworkId()));
      holder.addComponent(
          EntityScaleComponent.getComponentType(), new EntityScaleComponent(resolvedScale));
      holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(rotation));
    }

    ItemStack itemStack = new ItemStack(itemId, 1);
    itemStack.setOverrideDroppedItemAnimation(true);
    holder.addComponent(ItemComponent.getComponentType(), new ItemComponent(itemStack));
    holder.addComponent(PreventPickup.getComponentType(), PreventPickup.INSTANCE);
    holder.addComponent(PreventItemMerging.getComponentType(), PreventItemMerging.INSTANCE);
    holder.addComponent(PropComponent.getComponentType(), PropComponent.get());
    holder.ensureComponent(UUIDComponent.getComponentType());
    String collisionConfigId = resolveCollisionConfigId();
    if (collisionConfigId != null) {
      holder.addComponent(
          PendingSpawnItemCollisionComponent.getComponentType(),
          new PendingSpawnItemCollisionComponent(collisionConfigId, 2));
    }
    store.addEntity(holder, AddReason.SPAWN);
  }

  private Vector3d resolvePosition(TriggerContext context) {
    Vector3d position = new Vector3d(x, y, z);
    if (coordinateMode == CoordinateMode.ABSOLUTE) {
      return position;
    }
    if (context.getVolume() == null) {
      return null;
    }
    return position.add(context.getVolume().getPosition());
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

  private String resolveCollisionConfigId() {
    return switch (collisionMode) {
      case NONE -> null;
      case HARD -> "HardCollision";
      case SOFT -> "SoftCollision";
    };
  }
}
