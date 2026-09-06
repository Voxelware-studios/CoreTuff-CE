package org.voxelware.coretuff.Features.Utility.Kits.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Features.Utility.Kits.Kit;
import org.voxelware.coretuff.Features.Utility.Kits.KitService;
import org.voxelware.coretuff.commandExecuter.BaseCommand;

import java.util.List;

public class KitsCommand extends BaseCommand {

    private final KitService kitService;
    private final CoreTuff plugin;

    public KitsCommand(KitService kitService, CoreTuff plugin) {
        this.kitService = kitService;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> names = kitService.getKitNames().stream().sorted().toList();
        if (names.isEmpty()) {
            sender.sendMessage(plugin.format(null, "&cNo kits are configured.", null));
            return true;
        }

        sender.sendMessage(plugin.format(null, "&6Available Kits:", null));

        for (String name : names) {
            Kit kit = kitService.getKit(name);
            String display = kit != null ? kit.getDisplayName() : name;
            sender.sendMessage(plugin.formatRaw("&8- &f" + display));
        }
        return true;
    }

    @Override
    protected List<String> generateCompletions(CommandSender sender, String label, String[] args) {
        return List.of();
    }
}
