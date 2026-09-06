package org.voxelware.coretuff.Features.Moderation.Jail.Commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Features.Moderation.ConfigEngine.ModConfig;
import org.voxelware.coretuff.Features.Moderation.Jail.Jail;
import org.voxelware.coretuff.Features.Moderation.Jail.JailService;
import org.voxelware.coretuff.commandExecuter.PlayerCommand;

import java.util.List;

public class SetJailCommand extends PlayerCommand {

    private final JailService jailService;
    private final ModConfig config;
    private final CoreTuff plugin;

    public SetJailCommand(JailService jailService, ModConfig config, CoreTuff plugin) {
        this.jailService = jailService;
        this.config = config;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull Player player, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            player.sendMessage(plugin.format(player, config.jailMessage("usage-setjail", "&cUsage: /setjail <name>"), null));
            return true;
        }

        try {
            jailService.setJail(args[0], player);
            player.sendMessage(plugin.format(player, config.jailMessage("jail-set", "&aJail &f{jail} &aset.").replace("{jail}", args[0]), null));
        } catch (Exception e) {
            player.sendMessage(plugin.format(player, "&cFailed to create jail.", null));
        }
        return true;
    }

    @Override
    protected List<String> generateCompletions(org.bukkit.command.CommandSender sender, String label, String[] args) {
        return List.of();
    }
}
