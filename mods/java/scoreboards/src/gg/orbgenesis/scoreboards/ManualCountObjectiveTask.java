package gg.orbgenesis.scoreboards;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.task.CountObjectiveTask;
import com.hypixel.hytale.builtin.adventure.objectives.transaction.TransactionRecord;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.ObjectiveTask;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class ManualCountObjectiveTask extends CountObjectiveTask {
  public static final BuilderCodec<ManualCountObjectiveTask> CODEC =
      BuilderCodec.builder(
              ManualCountObjectiveTask.class,
              ManualCountObjectiveTask::new,
              CountObjectiveTask.CODEC)
          .build();

  public ManualCountObjectiveTask() {}

  public ManualCountObjectiveTask(ManualCountObjectiveTaskAsset asset, int taskSet, int taskIndex) {
    super(asset, taskSet, taskIndex);
  }

  @Override
  public ManualCountObjectiveTaskAsset getAsset() {
    return (ManualCountObjectiveTaskAsset) super.getAsset();
  }

  public int getCurrentValue() {
    return count;
  }

  @Override
  protected TransactionRecord[] setup0(
      Objective objective, World world, Store<EntityStore> store) {
    return null;
  }

  @Override
  public ObjectiveTask toPacket(Objective objective) {
    return new ObjectiveTask(
        Message.raw(getAsset().getRawLabel()).getFormattedMessage(),
        count,
        getAsset().getCount());
  }
}
