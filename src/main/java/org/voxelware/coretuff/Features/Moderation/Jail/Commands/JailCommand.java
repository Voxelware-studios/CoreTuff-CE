package org.voxelware.coretuff.Features.Moderation.Jail.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Features.Moderation.ConfigEngine.ModConfig;
import org.voxelware.coretuff.Features.Moderation.Jail.Jail;
import org.voxelware.coretuff.Features.Moderation.Jail.JailService;
import org.voxelware.coretuff.commandExecuter.PlayerCommand;

import java.util.List;

public class JailCommand extends PlayerCommand {

    private final JailService jailService;
    private final ModConfig config;
    private final CoreTuff plugin;

    public JailCommand(JailService jailService, ModConfig config, CoreTuff plugin) {
        this.jailService = jailService;
        this.config = config;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull Player player, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            player.sendMessage(plugin.format(player, config.jailMessage("usage-jail", "&cUsage: /jail <name> [player]"), null));
            return true;
        }

        try {
            Jail jail = jailService.getJail(args[0]);
            if (jail == null) {
                player.sendMessage(plugin.format(player, config.jailMessage("no-jail-exists", "&cJail &f{jail} &cdoes not exist.").replace("{jail}", args[0]), null));
                return true;
            }

            Player target = args.length >= 2 ? Bukkit.getPlayer(args[1]) : player;
            if (target == null || !target.isOnline()) {
                player.sendMessage(plugin.format(player, "&cPlayer not found.", null));
                return true;
            }

            if (jailService.isPlayerJailed(target.getUniqueId())) {
                player.sendMessage(plugin.format(player, config.jailMessage("already-jailed", "&c{player} is already jailed.").replace("{player}", target.getName()), null));
                return true;
            }

            String reason = args.length >= 3 ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)) : "Jailed";
            jailService.jailPlayer(target, args[0], player.getName(), reason);
            jailService.teleportToJail(target, jail);
            target.sendMessage(plugin.format(target, config.jailMessage("jailed", "&cYou have been jailed in &f{jail}&c.").replace("{jail}", args[0]), null));
            if (!target.equals(player)) {
                player.sendMessage(plugin.format(player, "&aJailed &f" + target.getName() + " &ain &f" + args[0] + "&a.", null));
            }

        } catch (Exception e) {
            player.sendMessage(plugin.format(player, "&cAn error occurred.", null));
        }
        return true;
    }

    @Override
    protected List<String> generateCompletions(org.bukkit.command.CommandSender sender, String label, String[] args) {
        if (args.length == 1) {
            try {
                return jailService.getAllJails().stream().map(Jail::getName).toList();
            } catch (Exception ignored) {}
        }
        if (args.length == 2 && sender.hasPermission("coretuff.moderation.jail.others")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return List.of();
    }
}
