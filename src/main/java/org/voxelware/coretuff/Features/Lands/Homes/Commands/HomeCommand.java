package org.voxelware.coretuff.Features.Lands.Homes.Commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.voxelware.coretuff.Features.Lands.Homes.Home;
import org.voxelware.coretuff.Features.Lands.Homes.HomeRepository;
import org.voxelware.coretuff.Features.Lands.Homes.HomeUtil;
import org.voxelware.coretuff.Features.Lands.LandConfig;
import org.voxelware.coretuff.Utility.CoreTuffProvider;
import org.voxelware.coretuff.commandExecuter.PlayerCommand;

import java.util.List;

public class HomeCommand extends PlayerCommand {

    private final HomeRepository repository;
    private final LandConfig config;
    private final org.voxelware.coretuff.CoreTuff plugin;

    public HomeCommand(HomeRepository repository, LandConfig config, org.voxelware.coretuff.CoreTuff plugin) {
        this.repository = repository;
        this.config = config;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull Player player, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            if (args.length == 0) {
                listHomes(player);
                return true;
            }

            String name = args[0];
            if (name.equalsIgnoreCase("list")) {
                listHomes(player);
                return true;
            }

            Home home = repository.getHome(player.getUniqueId(), name);
            if (home == null) {
                player.sendMessage(plugin.format(player, "&cHome &f" + name + " &cdoes not exist.", null));
                return true;
            }

            Location location = repository.toLocation(home);
            if (location == null) {
                player.sendMessage(plugin.format(player, "&cHome world is no longer available.", null));
                return true;
            }

            int delay = config.homeTeleportDelay();

            if (delay > 0 && CoreTuffProvider.getDelayedTeleporter() != null) {
                CoreTuffProvider.getDelayedTeleporter().teleport(player, location, delay)
                        .whenComplete((success, throwable) -> {
                            if (Boolean.TRUE.equals(success)) {
                                player.sendMessage(plugin.format(player, "&aTeleported to home &f" + home.getName() + "&a.", null));
                            }
                        });
            } else {
                player.teleportAsync(location).whenComplete((success, throwable) -> {
                    if (Boolean.TRUE.equals(success)) {
                        player.sendMessage(plugin.format(player, "&aTeleported to home &f" + home.getName() + "&a.", null));
                    }
                });
            }
        } catch (Exception e) {
            player.sendMessage(plugin.format(player, "&cAn error occurred.", null));
        }
        return true;
    }

    private void listHomes(Player player) throws Exception {
        List<Home> homes = repository.getHomes(player.getUniqueId());
        if (homes.isEmpty()) {
            player.sendMessage(plugin.format(player, "&cYou have no homes set.", null));
            return;
        }
        player.sendMessage(plugin.format(player, "&aYour homes:", null));
        for (Home h : homes) {
            player.sendMessage(plugin.formatRaw("&8- &f" + h.getName()));
        }
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
