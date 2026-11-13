package gg.raynor.triggervolumebrowser;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import java.util.Locale;

public final class TriggerVolumeEffectBrowserPlugin extends JavaPlugin {
  private AbstractCommand vanillaEffectsCommand;
  private AbstractCommand bridgeEffectsCommand;

  public TriggerVolumeEffectBrowserPlugin(JavaPluginInit init) {
    super(init);
  }

  @Override
  public void setup() {
    super.setup();
    OpenCustomUIInteraction.registerCustomPageSupplier(
        this,
        BrowserPageConfig.class,
        "RaynorTriggerEffectBrowser",
        (ref, componentAccessor, playerRef, context) -> {
          var store = ref.getStore();
          var world = store.getExternalData().getWorld();
          if (world == null) {
            return null;
          }
          var triggerVolumes =
              store.getResource(
                  com.hypixel.hytale.builtin.triggervolumes.TriggerVolumesPlugin.get()
                      .getManagerResourceType());
          var selectedVolume =
              triggerVolumes == null ? null : triggerVolumes.getPlayerSelection(playerRef.getUuid());
          if (selectedVolume == null || triggerVolumes.getVolume(selectedVolume) == null) {
            playerRef.sendMessage(
                Message.translation("server.customUI.triggerEffectBrowser.tool.selectFirst"));
            return null;
          }
          return new EffectBrowserInspectorPage(
              playerRef, world.getName().toLowerCase(Locale.ROOT), selectedVolume);
        });
    getCommandRegistry().registerCommand(new TriggerEffectBrowserCommand());
    installEffectsCommandBridge();
  }

  @Override
  public void shutdown() {
    restoreVanillaEffectsCommand();
    super.shutdown();
  }

  private void installEffectsCommandBridge() {
    var triggerVolumeCommand = CommandManager.get().resolveCommand("triggervolume");
    if (triggerVolumeCommand == null) {
      getLogger().atWarning().log("Could not locate /triggervolume; E-key bridge was not installed.");
      return;
    }

    var subCommands = triggerVolumeCommand.getSubCommands();
    vanillaEffectsCommand = subCommands.get("effects");
    if (vanillaEffectsCommand == null) {
      getLogger().atWarning().log("Could not locate /triggervolume effects; E-key bridge was not installed.");
      return;
    }

    var bridge = new TriggerVolumeEffectsBridgeCommand();
    bridge.setOwner(this);
    try {
      bridge.completeRegistration();
    } catch (GeneralCommandException exception) {
      getLogger().atSevere().withCause(exception).log("Could not register the Trigger Volume E-key bridge.");
      vanillaEffectsCommand = null;
      return;
    }

    bridgeEffectsCommand = bridge;
    subCommands.put("effects", bridge);
    getLogger().atInfo().log("Installed enhanced /triggervolume effects bridge for the cloned tool.");
  }

  private void restoreVanillaEffectsCommand() {
    if (vanillaEffectsCommand == null || bridgeEffectsCommand == null) {
      return;
    }
    var triggerVolumeCommand = CommandManager.get().resolveCommand("triggervolume");
    if (triggerVolumeCommand != null
        && triggerVolumeCommand.getSubCommands().get("effects") == bridgeEffectsCommand) {
      triggerVolumeCommand.getSubCommands().put("effects", vanillaEffectsCommand);
    }
    vanillaEffectsCommand = null;
    bridgeEffectsCommand = null;
  }

  private static final class BrowserPageConfig {}
}
