package org.voxelware.coretuff.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Utility.ComponentUtils;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticDumpService;
import org.voxelware.coretuff.commandExecuter.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CoreTuffCommand extends BaseCommand {

	private final CoreTuff plugin;

	public CoreTuffCommand(CoreTuff plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(
			@NotNull CommandSender sender,
			@NotNull Command command,
			@NotNull String label,
			@NotNull String[] args
	) {
		if (args.length == 0) {
			sender.sendMessage(createErrorComponent("Usage: /%s <reload|dump|dumpdata|bug>", label));
			return true;
		}

		switch (args[0].toLowerCase()) {
			case "reload" -> {
				if (!sender.hasPermission("coretuff.reload")) {
					sender.sendMessage(plugin.format(null, plugin.getCoreString("nopermission"), null));
					return true;
				}

				plugin.reloadPluginState();
				sender.sendMessage(createSuccessComponent("CoreTuff reloaded."));
				return true;
			}
			case "dump" -> {
				if (!sender.hasPermission("coretuff.admin.dump")) {
					sender.sendMessage(plugin.format(null, plugin.getCoreString("nopermission"), null));
					return true;
				}

				new Thread(() -> {
					DiagnosticDumpService ds = plugin.diagnosticDumpService();
					if (ds == null) {
						sender.sendMessage(createErrorComponent("Diagnostic system not initialized."));
						return;
					}
					DiagnosticDumpService.DiagnosticResult result = ds.generate();
					if (result.success()) {
						sender.sendMessage(createSuccessComponent("CoreTuff diagnostic dump generated successfully."));
						sender.sendMessage(createSuccessComponent("Location:"));
						sender.sendMessage(ComponentUtils.translateLegacyText("  &f" + result.message()));
					} else {
						sender.sendMessage(createErrorComponent("Failed to generate diagnostic dump: " + result.message()));
					}
				}, "CoreTuff-DiagnosticDump").start();
				return true;
			}
			case "bug" -> {
				if (sender.isOp()) {
					sender.sendMessage(
							ComponentUtils.translateLegacyText(
									"&f&l[&b&lCoreTuff&f&l]:&r "
											+ "If you encounter any bugs you can report here: "
							).append(
									Component.text(
													"GitHub Issues",
													NamedTextColor.AQUA
											)
											.clickEvent(
													ClickEvent.openUrl(
															"https://github.com/Voxelware-studios/CoreTuff/issues"
													)
											)
											.hoverEvent(
													HoverEvent.showText(
															Component.text(
																	"Click to open GitHub Issues"
															)
													)
											)
							)
					);
				}
				return true;
			}
			default -> {
				sender.sendMessage(createErrorComponent("Unknown subcommand."));
				return true;
			}
		}
	}

	@Override
	protected List<String> generateCompletions(CommandSender sender, String label, String[] args) {
		if (args.length == 1) {
			List<String> completions = new ArrayList<>();
			if (sender.hasPermission("coretuff.reload")) {
				completions.add("reload");
			}
			if (sender.hasPermission("coretuff.admin.dump")) {
				completions.add("dump");
			}
			if (sender.isOp()) {
				completions.add("bug");
			}
			return completions;
		}

		return List.of();
	}
}
