package org.voxelware.coretuff.Features.Lands.Homes.Commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.voxelware.coretuff.Features.Lands.Homes.HomeRepository;
import org.voxelware.coretuff.Features.Lands.Homes.HomeUtil;
import org.voxelware.coretuff.Features.Lands.LandConfig;
import org.voxelware.coretuff.commandExecuter.PlayerCommand;

import java.util.List;

public class SetHomeCommand extends PlayerCommand {

    private final HomeRepository repository;
    private final LandConfig config;
    private final org.voxelware.coretuff.CoreTuff plugin;

    public SetHomeCommand(HomeRepository repository, LandConfig config, org.voxelware.coretuff.CoreTuff plugin) {
        this.repository = repository;
        this.config = config;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull Player player, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String name = args.length > 0 ? args[0] : config.message("homes.default-home", "home");

        if (!isValidName(name)) {
            player.sendMessage(plugin.format(player, "&cHome name must be 1-32 alphanumeric characters.", null));
            return true;
        }

        try {
            int maxHomes = HomeUtil.getMaxHomes(player, config);
            int currentCount = repository.getHomeCount(player.getUniqueId());
            boolean alreadyExists = repository.getHome(player.getUniqueId(), name) != null;

            if (!alreadyExists && currentCount >= maxHomes) {
                player.sendMessage(plugin.format(player, "&cYou can only set up to &f" + maxHomes + " &chomes.", null));
                return true;
            }

            repository.setHome(player.getUniqueId(), name, player.getLocation());
            player.sendMessage(plugin.format(player, "&aHome &f" + name + " &ahas been set.", null));
        } catch (Exception e) {
            player.sendMessage(plugin.format(player, "&cFailed to save home.", null));
        }
        return true;
    }

    private boolean isValidName(String name) {
        return name.length() >= 1 && name.length() <= 32 && name.matches("[a-zA-Z0-9_-]+");
    }

    @Override
    protected List<String> generateCompletions(org.bukkit.command.CommandSender sender, String label, String[] args) {
        return List.of();
    }
}
