package org.voxelware.coretuff.Utility;

import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Features.Teleportation.DelayedTeleporter;
import org.voxelware.coretuff.Features.Teleportation.Handler.ExecutionHandler;
import org.voxelware.coretuff.Features.Teleportation.Handler.FoliaTeleportHandler;
import org.voxelware.coretuff.Features.Teleportation.Handler.PaperTeleportHandler;
import org.voxelware.coretuff.Features.Teleportation.Handler.TeleportHandler;
import org.voxelware.coretuff.Features.Teleportation.IgnoreService;
import org.voxelware.coretuff.Features.Teleportation.RtpService;
import org.voxelware.coretuff.Features.Teleportation.TpManager;
import org.voxelware.coretuff.api.economy.EconomyApi;
import org.voxelware.coretuff.api.economy.InterestApi;
import org.voxelware.coretuff.api.economy.TransactionApi;

public final class CoreTuffProvider {

	private static CoreTuff plugin;
	private static TpManager tpManager;
	private static ExecutionHandler executionHandler;
	private static TeleportHandler teleportHandler;
	private static DelayedTeleporter delayedTeleporter;
	private static RtpService rtpService;
	private static IgnoreService ignoreService;

	private CoreTuffProvider() {}

	public static void init(CoreTuff plugin, IgnoreService ignoreService, boolean folia) {
		CoreTuffProvider.plugin = plugin;
		CoreTuffProvider.ignoreService = ignoreService;
		executionHandler = new ExecutionHandler(plugin, folia);
		teleportHandler = folia ? new FoliaTeleportHandler(plugin) : new PaperTeleportHandler(plugin);
		delayedTeleporter = new DelayedTeleporter(plugin, teleportHandler);
		tpManager = new TpManager(plugin, ignoreService, delayedTeleporter);
		rtpService = new RtpService(plugin, tpManager, executionHandler);
	}

	public static EconomyApi getEconomy() {
		return plugin.getEconomyApi();
	}

	public static TransactionApi getTransactions() {
		return plugin.getTransactionApi();
	}

	public static InterestApi getInterest() {
		return plugin.getInterestApi();
	}

	public static TpManager getTpManager() {
		return tpManager;
	}

	public static ExecutionHandler getExecutionHandler() {
		return executionHandler;
	}

	public static TeleportHandler getTeleportHandler() {
		return teleportHandler;
	}

	public static DelayedTeleporter getDelayedTeleporter() {
		return delayedTeleporter;
	}

	public static CoreTuff getPlugin() {
		return plugin;
	}

	public static RtpService getRtpService() {
		return rtpService;
	}

	public static IgnoreService getIgnoreService() {
		return ignoreService;
	}
}
