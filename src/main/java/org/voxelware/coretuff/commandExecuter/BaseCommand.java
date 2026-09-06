package org.voxelware.coretuff.commandExecuter;

import java.util.Collection;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public abstract class BaseCommand implements CommandExecutor, TabCompleter {
	
	protected static final String ERR_INVALID_MAIN_WORLD = "Main world is misconfigured. Please contact server staff.";
	protected static final String ERR_INVALID_SELECTION = "The WorldEdit selection must be a simple cuboid";
	protected static final String ERR_INVALID_SUBCOMMAND = "Unknown subcommand %s";
	protected static final String ERR_NO_SELECTION = "You must first make a valid cuboid selection with WorldEdit";
	protected static final String ERR_NOT_PLAYER = "This command can only be executed by players";
	private static final String ERR_COORD_NOT_A_NUMBER = "One of the coordinates it not a valid integer";
	private static final String ERR_NO_SUCH_WORLD = "World %s doesn't exist";
	
	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
	                                            @NotNull String label, @NotNull String @NotNull [] args) {
		List<String> completions = generateCompletions(sender, label, args);
		if (completions != null) {
			return filterCompletions(completions, args);
		}
		return null;
	}
	
	protected abstract List<String> generateCompletions(CommandSender sender, String label, String[] args);
	
	public static Component createErrorComponent(String text, Object... args) {
		return createSimpleComponent(NamedTextColor.RED, text, args);
	}
	
	public static Component createSuccessComponent(String text, Object... args) {
		return createSimpleComponent(NamedTextColor.GREEN, text, args);
	}
	
	private static Component createSimpleComponent(TextColor color, String text, Object... args) {
		return Component.text(text.formatted(args)).color(color);
	}
	
	private static List<String> filterCompletions(Collection<String> completions, String[] args) {
		return completions.stream().filter(item -> item.startsWith(args[args.length - 1])).toList();
	}
}
