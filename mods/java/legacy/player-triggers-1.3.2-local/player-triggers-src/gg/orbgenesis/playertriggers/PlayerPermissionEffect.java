package gg.orbgenesis.playertriggers;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.Set;

public class PlayerPermissionEffect extends TriggerEffect {
  public enum Operation {
    ADD_PERMISSION,
    REMOVE_PERMISSION,
    TOGGLE_PERMISSION,
    ADD_GROUP,
    REMOVE_GROUP,
    SET_GROUP
  }

  public static final BuilderCodec<PlayerPermissionEffect> CODEC =
      BuilderCodec.builder(
              PlayerPermissionEffect.class,
              PlayerPermissionEffect::new,
              TriggerEffect.BASE_CODEC)
          .append(
              new KeyedCodec<>("Operation", new EnumCodec<>(Operation.class)),
              (effect, value) -> effect.operation = value,
              effect -> effect.operation)
          .add()
          .append(
              new KeyedCodec<>("Permission", Codec.STRING, false),
              (effect, value) -> effect.permission = value,
              effect -> effect.permission)
          .add()
          .append(
              new KeyedCodec<>("Group", Codec.STRING, false),
              (effect, value) -> effect.group = value,
              effect -> effect.group)
          .add()
          .build();

  private Operation operation = Operation.ADD_PERMISSION;
  private String permission = "";
  private String group = "";

  @Override
  public void execute(TriggerContext context) {
    PlayerRef player = PlayerTagAccess.getPlayer(context);
    PermissionsModule permissions = PermissionsModule.get();
    if (player == null || permissions == null) {
      return;
    }

    switch (operation) {
      case ADD_PERMISSION -> addPermission(player, permissions);
      case REMOVE_PERMISSION -> removePermission(player, permissions);
      case TOGGLE_PERMISSION -> togglePermission(player, permissions);
      case ADD_GROUP -> addGroup(player, permissions);
      case REMOVE_GROUP -> removeGroup(player, permissions);
      case SET_GROUP -> setGroup(player, permissions);
    }
  }

  private void addPermission(PlayerRef player, PermissionsModule permissions) {
    String value = normalizePermission(permission);
    if (!value.isEmpty()) {
      permissions.addUserPermission(player.getUuid(), Set.of(value));
    }
  }

  private void removePermission(PlayerRef player, PermissionsModule permissions) {
    String value = normalizePermission(permission);
    if (!value.isEmpty()) {
      permissions.removeUserPermission(player.getUuid(), Set.of(value));
    }
  }

  private void togglePermission(PlayerRef player, PermissionsModule permissions) {
    String value = normalizePermission(permission);
    if (value.isEmpty()) {
      return;
    }

    if (permissions.hasPermission(player.getUuid(), value, false)) {
      permissions.removeUserPermission(player.getUuid(), Set.of(value));
    } else {
      permissions.addUserPermission(player.getUuid(), Set.of(value));
    }
  }

  private void addGroup(PlayerRef player, PermissionsModule permissions) {
    String value = normalizeGroup(group);
    if (!value.isEmpty()) {
      permissions.addUserToGroup(player.getUuid(), value);
    }
  }

  private void removeGroup(PlayerRef player, PermissionsModule permissions) {
    String value = normalizeGroup(group);
    if (!value.isEmpty()) {
      permissions.removeUserFromGroup(player.getUuid(), value);
    }
  }

  private void setGroup(PlayerRef player, PermissionsModule permissions) {
    String value = normalizeGroup(group);
    if (!value.isEmpty()) {
      permissions.setUserGroup(player.getUuid(), value);
    }
  }

  private static String normalizePermission(String raw) {
    return raw == null ? "" : raw.trim();
  }

  private static String normalizeGroup(String raw) {
    if (raw == null || raw.isBlank()) {
      return "";
    }
    return HytalePermissionsProvider.resolveGroupName(raw.trim());
  }
}
