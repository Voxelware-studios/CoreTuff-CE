package org.voxelware.coretuff.Features.Utility.miscellaneous;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.voxelware.coretuff.commandExecuter.PlayerCommand;

import java.util.List;

public class HatCommand extends PlayerCommand {
    @Override
    public boolean onCommand(@NotNull Player player, @NotNull Command command, @NotNull String label, @NonNull @NotNull String[] args) {
        if (!player.hasPermission(
                "coretuff.utility.hat"
        )) {

            player.sendMessage(
                    createErrorComponent(
                            "No permission."
                    )
            );

            return true;
        }

        ItemStack hand =
                player.getInventory()
                        .getItemInMainHand();

        if (hand.getType()
                == Material.AIR) {

            player.sendMessage(
                    createErrorComponent(
                            "You must hold an item."
                    )
            );

            return true;
        }

        ItemStack hat =
                hand.clone();

        hat.setAmount(1);

        ItemStack oldHelmet =
                player.getInventory()
                        .getHelmet();

        player.getInventory()
                .setHelmet(hat);

        if (hand.getAmount() <= 1) {

            player.getInventory()
                    .setItemInMainHand(
                            new ItemStack(Material.AIR)
                    );

        } else {

            hand.setAmount(
                    hand.getAmount() - 1
            );

            player.getInventory()
                    .setItemInMainHand(hand);
        }

        if (oldHelmet != null
                && oldHelmet.getType()
                != Material.AIR) {

            player.getInventory()
                    .addItem(oldHelmet);
        }

        player.sendMessage(
                createSuccessComponent(
                        "Hat equipped."
                )
        );

        return true;
    }

    @Override
    protected List<String> generateCompletions(CommandSender sender, String label, String[] args) {
        return List.of();
    }
}
