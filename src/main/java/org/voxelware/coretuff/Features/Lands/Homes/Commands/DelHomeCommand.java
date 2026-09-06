package org.voxelware.coretuff.Features.Lands.Homes.Commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.voxelware.coretuff.Features.Lands.Homes.Home;
import org.voxelware.coretuff.Features.Lands.Homes.HomeRepository;
import org.voxelware.coretuff.Features.Lands.LandConfig;
import org.voxelware.coretuff.commandExecuter.PlayerCommand;

import java.util.List;

public class DelHomeCommand extends PlayerCommand {

    private final HomeRepository repository;
    private final LandConfig config;
    private final org.voxelware.coretuff.CoreTuff plugin;

    public DelHomeCommand(HomeRepository repository, LandConfig config, org.voxelware.coretuff.CoreTuff plugin) {
        this.repository = repository;
        this.config = config;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull Player player, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            player.sendMessage(plugin.format(player, "&cUsage: /delhome <name>", null));
            return true;
        }

        try {
            Home home = repository.getHome(player.getUniqueId(), args[0]);
            if (home == null) {
                player.sendMessage(plugin.format(player, "&cHome &f" + args[0] + " &cdoes not exist.", null));
                return true;
            }

            repository.deleteHome(player.getUniqueId(), args[0]);
            player.sendMessage(plugin.format(player, "&cHome &f" + args[0] + " &cdeleted.", null));
        } catch (Exception e) {
            player.sendMessage(plugin.format(player, "&cFailed to delete home.", null));
        }
        return true;
    }

    @Override
    protected List<String> generateCompletions(org.bukkit.command.CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) return List.of();
        if (args.length == 1) {
            try {
                return repository.getHomes(player.getUniqueId()).stream().map(Home::getName).toList();
            } catch (Exception ignored) {}
        }
        return List.of();
    }
}
