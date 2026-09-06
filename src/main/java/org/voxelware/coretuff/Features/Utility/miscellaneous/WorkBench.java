package org.voxelware.coretuff.Features.Utility.miscellaneous;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.voxelware.coretuff.commandExecuter.PlayerCommand;

import java.util.List;

public class WorkBench extends PlayerCommand {
    String Cmd;
    public WorkBench(String Cmd)
    {
        this.Cmd=Cmd;
    }

    @Override
    public boolean onCommand(@NotNull Player player, @NotNull Command command, @NotNull String label, @NonNull @NotNull String[] args) {
        switch (Cmd){
            case "craftt": {
                if (!player.hasPermission("coretuff.utility.craftingtable")) {
                    player.sendMessage("You dont have permission");
                    break;
                }
                player.openWorkbench(null,true);
                break;
            }

            case "anvil":{
                if (!player.hasPermission("coretuff.utility.anvil")) {
                    player.sendMessage("You dont have permission");
                    break;
                }
                player.openAnvil(null,true);
                break;
            }
            case "stonecut":{
                if (!player.hasPermission("coretuff.utility.stonecutter")) {
                    player.sendMessage("You dont have permission");
                    break;
                }
                player.openStonecutter(null,true);
                break;
            }

            case"smitht":{
                if (!player.hasPermission("coretuff.utility.smithingtable")) {
                    player.sendMessage("You dont have permission");
                    break;
                }
                player.openSmithingTable(null,true);
                break;
            }

            case"loom":{
                if (!player.hasPermission("coretuff.utility.loom")) {
                    player.sendMessage("You dont have permission");
                    break;
                }
                player.openLoom(null,true);
                break;
            }

            case"cartot":{
                if (!player.hasPermission("coretuff.utility.cartographytable")) {
                    player.sendMessage("You dont have permission");
                    break;
                }
                player.openCartographyTable(null,true);
                break;
            }

            case"encht":{
                if (!player.hasPermission("coretuff.utility.enchantingtable")) {
                    player.sendMessage("You dont have permission");
                    break;
                }
                player.openEnchanting(null,true);
                break;
            }

            case"grind":{
                if (!player.hasPermission("coretuff.utility.grindstone")) {
                    player.sendMessage("You dont have permission");
                    break;
                }
                player.openGrindstone(null,true);
                break;
            }

        }
        return true;
    }

    @Override
    protected List<String> generateCompletions(CommandSender sender, String label, String[] args) {
        return List.of();
    }
}
