package org.voxelware.coretuff.Features.Lands.Warps.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Features.Lands.Warps.Warp;
import org.voxelware.coretuff.Features.Lands.Warps.WarpService;
import org.voxelware.coretuff.commandExecuter.BaseCommand;

import java.util.List;

public class WarpsCommand extends BaseCommand {

    private final WarpService warpService;

    public WarpsCommand(WarpService warpService) {
        this.warpService = warpService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("coretuff.land.warps.list")) {
            sender.sendMessage(warpService.plugin().format(null, "&cYou don't have permission to list warps.", null));
            return true;
        }

        try {
            List<Warp> warps = warpService.getAllWarps();
            if (warps.isEmpty()) {
                sender.sendMessage(warpService.plugin().format(null, "&cNo warps exist on this server.", null));
                return true;
            }

            int page = 1;
            if (args.length > 0) {
                try {
                    page = Integer.parseInt(args[0]);
                } catch (NumberFormatException ignored) {}
            }

            int perPage = 10;
            int totalPages = (int) Math.ceil((double) warps.size() / perPage);
            page = Math.max(1, Math.min(page, totalPages));
            int start = (page - 1) * perPage;
            int end = Math.min(start + perPage, warps.size());

            var plugin = warpService.plugin();
            sender.sendMessage(plugin.format(null, "&6Warps &7(Page " + page + "/" + totalPages + "):", null));
            for (int i = start; i < end; i++) {
                Warp w = warps.get(i);
                String desc = w.getDescription() != null ? " &7- " + w.getDescription() : "";
                sender.sendMessage(plugin.formatRaw("&8- &f" + w.getName() + desc));
            }

        } catch (Exception e) {
            sender.sendMessage(warpService.plugin().format(null, "&cFailed to load warps.", null));
        }
        return true;
    }

    @Override
    protected List<String> generateCompletions(CommandSender sender, String label, String[] args) {
        return List.of();
    }
}
