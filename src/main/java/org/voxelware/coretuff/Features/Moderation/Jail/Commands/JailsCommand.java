package org.voxelware.coretuff.Features.Moderation.Jail.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Features.Moderation.ConfigEngine.ModConfig;
import org.voxelware.coretuff.Features.Moderation.Jail.Jail;
import org.voxelware.coretuff.Features.Moderation.Jail.JailService;
import org.voxelware.coretuff.commandExecuter.BaseCommand;

import java.util.List;

public class JailsCommand extends BaseCommand {

    private final JailService jailService;
    private final ModConfig config;
    private final CoreTuff plugin;

    public JailsCommand(JailService jailService, ModConfig config, CoreTuff plugin) {
        this.jailService = jailService;
        this.config = config;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            List<Jail> jails = jailService.getAllJails();
            if (jails.isEmpty()) {
                sender.sendMessage(plugin.format(null, config.jailMessage("jails-none", "&cNo jails exist."), null));
                return true;
            }

            sender.sendMessage(plugin.format(null, config.jailMessage("jail-list-header", "&6Jails:"), null));
            for (Jail j : jails) {
                sender.sendMessage(plugin.formatRaw(config.jailMessage("jail-list-entry", "&8- &f{name}").replace("{name}", j.getName())));
            }
        } catch (Exception e) {
            sender.sendMessage(plugin.format(null, "&cFailed to load jails.", null));
        }
        return true;
    }

    @Override
    protected List<String> generateCompletions(CommandSender sender, String label, String[] args) {
        return List.of();
    }
}
