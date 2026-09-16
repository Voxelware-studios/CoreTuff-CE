package org.voxelware.coretuff;

import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.voxelware.coretuff.Commands.CoreTuffCommand;
import org.voxelware.coretuff.database.DatabaseManager;
import org.voxelware.coretuff.Features.Economy.Commands.BalanceCommand;
import org.voxelware.coretuff.Features.Economy.Commands.EconomyCommand;
import org.voxelware.coretuff.Features.Economy.Commands.PayCommand;
import org.voxelware.coretuff.Features.Economy.CurrencyFormatter;
import org.voxelware.coretuff.Features.Economy.EconomyConfig;
import org.voxelware.coretuff.Features.Economy.EconomyRepository;
import org.voxelware.coretuff.Features.Economy.EconomyService;
import org.voxelware.coretuff.Features.Economy.Transaction.TransactionRepository;
import org.voxelware.coretuff.Features.Economy.VaultHook;
import org.voxelware.coretuff.Features.Lands.Homes.Commands.DelHomeCommand;
import org.voxelware.coretuff.Features.Lands.Homes.Commands.HomeCommand;
import org.voxelware.coretuff.Features.Lands.Homes.Commands.SetHomeCommand;
import org.voxelware.coretuff.Features.Lands.Homes.HomeRepository;
import org.voxelware.coretuff.Features.Lands.LandConfig;
import org.voxelware.coretuff.Features.Lands.Warps.Commands.DelWarpCommand;
import org.voxelware.coretuff.Features.Lands.Warps.Commands.SetWarpCommand;
import org.voxelware.coretuff.Features.Lands.Warps.Commands.WarpCommand;
import org.voxelware.coretuff.Features.Lands.Warps.Commands.WarpsCommand;
import org.voxelware.coretuff.Features.Lands.Warps.WarpRepository;
import org.voxelware.coretuff.Features.Lands.Warps.WarpService;
import org.voxelware.coretuff.Features.Placeholder.CoreTuffExpansion;
import org.voxelware.coretuff.Features.Teleportation.Commands.*;
import org.voxelware.coretuff.Features.Teleportation.IgnoreRepository;
import org.voxelware.coretuff.Features.Teleportation.IgnoreService;
import org.voxelware.coretuff.Features.Teleportation.Listener.DeathListener;
import org.voxelware.coretuff.Features.Teleportation.Listener.TeleportCancelListener;
import org.voxelware.coretuff.Features.Teleportation.TeleportConfig;
import org.voxelware.coretuff.Features.Utility.UtilityConfig;
import org.voxelware.coretuff.Features.Utility.commandShorthand.dayCommand;
import org.voxelware.coretuff.Features.Utility.commandShorthand.gmaCommand;
import org.voxelware.coretuff.Features.Utility.commandShorthand.gmcCommand;
import org.voxelware.coretuff.Features.Utility.commandShorthand.gmsCommand;
import org.voxelware.coretuff.Features.Utility.commandShorthand.gmspCommand;
import org.voxelware.coretuff.Features.Utility.commandShorthand.midnightCommand;
import org.voxelware.coretuff.Features.Utility.commandShorthand.nightCommand;
import org.voxelware.coretuff.Features.Utility.commandShorthand.noonCommand;
import org.voxelware.coretuff.Features.Utility.commandShorthand.sunsetCommand;
import org.voxelware.coretuff.Features.Utility.miscellaneous.*;
import org.voxelware.coretuff.Features.Moderation.Commands.*;
import org.voxelware.coretuff.Features.Moderation.ConfigEngine.ModConfig;
import org.voxelware.coretuff.Features.Moderation.Jail.JailRepository;
import org.voxelware.coretuff.Features.Moderation.Jail.JailService;
import org.voxelware.coretuff.Features.Moderation.Jail.Commands.*;
import org.voxelware.coretuff.Features.Moderation.Listerner.BanListener;
import org.voxelware.coretuff.Features.Moderation.Service.ModerationService;
import org.voxelware.coretuff.Features.Moderation.Service.OfflineModerationService;
import org.voxelware.coretuff.Features.Moderation.Service.OnlineModerationService;
import org.voxelware.coretuff.Features.Moderation.Service.WarnService;
import org.voxelware.coretuff.Features.Utility.Kits.KitConfig;
import org.voxelware.coretuff.Features.Utility.Kits.KitService;
import org.voxelware.coretuff.Features.Utility.Kits.Commands.*;
import org.voxelware.coretuff.Features.Security.SecurityConfig;
import org.voxelware.coretuff.api.economy.*;
import org.voxelware.coretuff.Utility.*;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticDumpService;
import org.voxelware.coretuff.Utility.Diagnostic.ExceptionRingBuffer;
import org.voxelware.coretuff.Utility.Diagnostic.collectors.*;
import org.bstats.bukkit.Metrics;

import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.lang.reflect.Field;

public final class CoreTuff extends JavaPlugin {

	private static final String CYAN = "\u001B[36m";
	private static final String BLUE = "\u001B[38;2;0;0;255m";
	private static final String RESET = "\u001B[0m";
	private static final String LOGOGRAY = "\u001B[38;2;127;132;140m";
	private static final String CHANNEL = "Nightly";
	private static final boolean DEV_MODE = false;
	public String getChannel() { return CHANNEL; }
	public boolean isDevMode() { return DEV_MODE; }
	private CoreConfigLoader coreConfigLoader;

	private DatabaseManager databaseManager;
	private ModerationService moderationService;
	private WarnService warnService;
	private ModConfig modConfig;
	private SecurityConfig securityConfig;
	private UtilityConfig utilityConfig;
	private TeleportConfig teleportConfig;
	private EconomyConfig economyConfig;
	private EconomyService economyService;
	private HomeRepository homeRepository;
	private LandConfig landConfig;
	private WarpService warpService;
	private JailService jailService;
	private KitConfig kitConfig;
	private KitService kitService;
	private UpdateChecker updateChecker;
	private ExceptionRingBuffer exceptionBuffer;
	private DiagnosticDumpService diagnosticDumpService;
	private CoreTuffExpansion coreTuffExpansion;
	private EconomyApi economyApi;
	private TransactionApi transactionApi;
	private InterestApi interestApi;
	private Instant enableTime;

	public EconomyService economyService() { return economyService; }
	public WarpService warpService() { return warpService; }
	public JailService jailService() { return jailService; }
	public KitService kitService() { return kitService; }
	public ModerationService moderationService() { return moderationService; }
	public WarnService warnService() { return warnService; }
	public DatabaseManager databaseManager() { return databaseManager; }
	public LandConfig getLandConfig() { return landConfig; }
	public DiagnosticDumpService diagnosticDumpService() { return diagnosticDumpService; }
	public ExceptionRingBuffer exceptionBuffer() { return exceptionBuffer; }
	public CoreTuffExpansion getExpansion() { return coreTuffExpansion; }
	public EconomyApi getEconomyApi() { return economyApi; }
	public TransactionApi getTransactionApi() { return transactionApi; }
	public InterestApi getInterestApi() { return interestApi; }

	public boolean isFolia() {
		try {
			Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public @NonNull String VER ()
	{
		if(CHANNEL.equalsIgnoreCase("Nightly"))
			return "\u001B[38;2;255;0;0m";
		if(CHANNEL.equalsIgnoreCase("Insider"))
			return "\u001B[38;2;245;242;7m";
		if(CHANNEL.equalsIgnoreCase("RC"))
			return "\u001B[38;2;255;85;255m";
		if(CHANNEL.equalsIgnoreCase("Release"))
			return "\u001B[38;2;85;255;255m";
		else
			return "\u001B[38;2;255;255;255m";
	}

	@Override
	public void onEnable() {
		this.enableTime = Instant.now();
		this.exceptionBuffer = new ExceptionRingBuffer();

		saveDefaultConfig();
		ConfigVersionUpdater.checkAndUpdate(this);
		reloadConfig();

		logStartup(Bukkit.getConsoleSender(), isFolia());

		coreConfigLoader = new CoreConfigLoader();
		coreConfigLoader.load(getConfig());
		initMetrics();

		securityConfig = new SecurityConfig();
		securityConfig.load(getConfig());
		utilityConfig = new UtilityConfig();
		utilityConfig.load(getConfig());
		teleportConfig = new TeleportConfig();
		teleportConfig.load(getConfig());
		this.modConfig = new ModConfig();
		modConfig.load(getConfig());

		try {
			databaseManager = new DatabaseManager();
			databaseManager.connect(this);

			IgnoreRepository ignoreRepository = new IgnoreRepository(databaseManager.getConnection());
			IgnoreService ignoreService = new IgnoreService(ignoreRepository, this);
			ignoreService.load();

			CoreTuffProvider.init(this, ignoreService, isFolia());
			if (getServer().getOnlineMode()) {
				this.moderationService = new OnlineModerationService(databaseManager.getConnection());
			} else {
				this.moderationService = new OfflineModerationService(databaseManager.getConnection());
			}
			moderationService.syncVanillaBans();
			moderationService.syncVanillaIpBans();
			this.warnService = new WarnService();

			homeRepository = new HomeRepository(databaseManager);
			economyConfig = new EconomyConfig();
			economyConfig.load(getConfig());
			EconomyRepository economyRepository = new EconomyRepository(databaseManager);
			CurrencyFormatter currencyFormatter = new CurrencyFormatter(economyConfig.currencySymbol(), economyConfig.decimalPlaces());
			TransactionRepository transactionRepository = new TransactionRepository(databaseManager);
			economyService = new EconomyService(this, economyRepository, transactionRepository, economyConfig, currencyFormatter);
			economyApi = new EconomyApiImpl(this, economyService, transactionRepository);
			transactionApi = new TransactionApiImpl(transactionRepository, databaseManager.getConnection());
			interestApi = new InterestApiImpl(economyConfig);
			Bukkit.getServicesManager().register(EconomyApi.class, economyApi, this, ServicePriority.Normal);
			Bukkit.getServicesManager().register(TransactionApi.class, transactionApi, this, ServicePriority.Normal);
			Bukkit.getServicesManager().register(InterestApi.class, interestApi, this, ServicePriority.Normal);
		} catch (Exception e) {
			getLogger().severe("Failed to initialize CoreTuff platform!");
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		registerVault();
		registerCommands();
		registerEvents();
		registerPlaceholderExpansion();
		startInterestTask();

		diagnosticDumpService = new DiagnosticDumpService(this, exceptionBuffer);
		diagnosticDumpService.registerCollector(new SystemCollector(this));
		diagnosticDumpService.registerCollector(new ServerEnvironmentCollector());
		diagnosticDumpService.registerCollector(new IntegrationCollector());
		diagnosticDumpService.registerCollector(new ModuleCollector(this));
		diagnosticDumpService.registerCollector(new DatabaseCollector(this));
		diagnosticDumpService.registerCollector(new ConfigVersionCollector(this));
		diagnosticDumpService.registerCollector(new RuntimeStatsCollector(this));
		diagnosticDumpService.registerCollector(new PluginInfoCollector(this, enableTime));
		diagnosticDumpService.registerCollector(new SchedulerCollector(this));
		diagnosticDumpService.registerCollector(new ConfigSummaryCollector(this));
		diagnosticDumpService.registerCollector(new PlaceholderAPICollector(this));
		diagnosticDumpService.registerCollector(new PermissionCollector());
		diagnosticDumpService.registerCollector(new SecurityCollector());
		diagnosticDumpService.registerCollector(new ExceptionCollector(exceptionBuffer));

		updateChecker = new UpdateChecker(this);
		updateChecker.check();
	}

	@Override
	public void onDisable() {
		if (databaseManager != null && databaseManager.getConnection() != null) {
			try {
				databaseManager.getConnection().close();
			} catch (Exception ignored) {
			}
		}
	}

	private void registerCommand(String name, CommandExecutor executor, String... aliases) {
		registerCommandInternal(name, executor, true, null, aliases);
	}

	private void registerCommand(String name, CommandExecutor executor, boolean overrideExisting, String... aliases) {
		registerCommandInternal(name, executor, overrideExisting, null, aliases);
	}

	private void registerCommandWithPermission(String name, CommandExecutor executor, String permission, String... aliases) {
		registerCommandInternal(name, executor, true, permission, aliases);
	}

	private void registerCommandInternal(String name,
	                                     CommandExecutor executor,
	                                     boolean overrideExisting,
	                                     String permission,
	                                     String... aliases) {
		org.bukkit.command.Command cmd = new org.bukkit.command.Command(name) {
			@Override
			public boolean execute(CommandSender sender, String label, String[] args) {
				return executor.onCommand(sender, this, label, args);
			}

			@Override
			public java.util.List<String> tabComplete(CommandSender sender, String alias, String[] args)
					throws IllegalArgumentException {
				if (executor instanceof TabCompleter completer) {
					java.util.List<String> completions =
							completer.onTabComplete(sender, this, alias, args);
					return completions == null ? java.util.List.of() : completions;
				}
				return java.util.List.of();
			}
		};

		cmd.setAliases(Arrays.asList(aliases));
		if (permission != null && !permission.isBlank()) {
			cmd.setPermission(permission);
		}
		CommandMap commandMap = getServer().getCommandMap();
		commandMap.register(name, cmd);

		if (overrideExisting) {
			overrideCommandMapping(commandMap, cmd, name, aliases);
		}
	}

	@SuppressWarnings("unchecked")
	private void overrideCommandMapping(CommandMap commandMap, org.bukkit.command.Command command, String name, String... aliases) {
		try {
			Field knownCommandsField = findField(commandMap.getClass(), "knownCommands");
			if (knownCommandsField == null) {
				return;
			}

			knownCommandsField.setAccessible(true);
			Map<String, org.bukkit.command.Command> knownCommands =
					(Map<String, org.bukkit.command.Command>) knownCommandsField.get(commandMap);

			knownCommands.put(name.toLowerCase(Locale.ROOT), command);
			for (String alias : aliases) {
				knownCommands.put(alias.toLowerCase(Locale.ROOT), command);
			}
		} catch (ReflectiveOperationException exception) {
			getLogger().warning("Failed to override command mapping for " + name);
		}
	}

	private Field findField(Class<?> type, String name) {
		Class<?> current = type;
		while (current != null) {
			try {
				return current.getDeclaredField(name);
			} catch (NoSuchFieldException ignored) {
				current = current.getSuperclass();
			}
		}
		return null;
	}

	private void registerEvents() {
		PluginManager pluginManager = Bukkit.getPluginManager();
		pluginManager.registerEvents(new TeleportCancelListener(CoreTuffProvider.getDelayedTeleporter(), this), this);
		pluginManager.registerEvents(new DeathListener(), this);
		pluginManager.registerEvents(new BanListener(moderationService), this);
		pluginManager.registerEvents(new Listener() {
			@EventHandler
			public void onJoin(PlayerJoinEvent event) {
				if (updateChecker != null) {
					updateChecker.notifyPlayer(event.getPlayer());
				}
			}
		}, this);
	}

	private void registerCommands() {
		kitConfig = new KitConfig();
		kitConfig.load(getConfig());
		kitService = new KitService(this, kitConfig);
		registerCommand("org/voxelware/coretuff", new CoreTuffCommand(this), "ct");
		registerCommand("gmc", new gmcCommand(this));
		registerCommand("gma", new gmaCommand(this));
		registerCommand("gmsp", new gmspCommand(this));
		registerCommand("gms", new gmsCommand(this));
		registerCommand("day", new dayCommand(this));
		registerCommand("noon", new noonCommand(this));
		registerCommand("sunset", new sunsetCommand(this));
		registerCommand("night", new nightCommand(this));
		registerCommand("midnight", new midnightCommand(this));
		registerCommand("feed", new hungerCommand(this));
		registerCommand("heal", new healCommand(this));
		registerCommand("fly", new flyCommand(this));
		registerCommand("tpask", new TpAskCommand(this), "tpa");
		registerCommand("tpdeny", new TpDenyCommand(this), "tpd");
		registerCommand("tpignore", new TpIgnoreCommand(this), "tpig");
		registerCommand("back", new BackCommand(this));
		registerCommand("rtp", new RtpCommand(this));
		registerCommand("tphere", new TpHereCommand(this), "tph");
		registerCommand("tpunignore", new TpUnignoreCommand(this), "tpuig");
		registerCommand("tpaccept", new TpAcceptCommand(this), "tpac");
		registerCommand("ban", new BanCommand(moderationService));
		registerCommand("tempban", new TempBanCommand(moderationService));
		registerCommand("ban-ip", new BanIpCommand(moderationService), "banip");
		registerCommand("pardon", new PardonCommand(moderationService), true, "unban");
		registerCommand("kick", new KickCommand());
		registerCommand("warn", new WarnCommand(warnService));
		registerCommand("pid", new PidCommand(moderationService));
		registerCommand("hat", new HatCommand());
		registerCommand("crafting",new WorkBench("craftt"));
		registerCommand("anvil",new WorkBench("anvil"));
		registerCommand("stonecutter",new WorkBench("stonecut"));
		registerCommand("smithing",new WorkBench("smitht"));
		registerCommand("loom",new WorkBench("loom"));
		registerCommand("enchanting",new WorkBench("encht"));
		registerCommand("cartography",new WorkBench("cartot"));
		registerCommand("grindstone",new WorkBench("grind"));
		registerCommand("balance", new BalanceCommand(economyService));
		registerCommand("pay", new PayCommand(economyService));
		registerCommand("eco", new EconomyCommand(economyService));
		var landConfig = new LandConfig();
		landConfig.load(getConfig());
		this.landConfig = landConfig;
		registerCommand("home", new HomeCommand(homeRepository, landConfig, this));
		registerCommand("sethome", new SetHomeCommand(homeRepository, landConfig, this));
		registerCommand("delhome", new DelHomeCommand(homeRepository, landConfig, this));
		WarpRepository warpRepository = new WarpRepository(databaseManager);
		WarpService warpService = new WarpService(this, warpRepository, landConfig, economyService);
		this.warpService = warpService;
		registerCommand("warp", new WarpCommand(warpService));
		registerCommand("setwarp", new SetWarpCommand(warpService));
		registerCommand("delwarp", new DelWarpCommand(warpService));
		registerCommand("warps", new WarpsCommand(warpService));
		JailRepository jailRepository = new JailRepository(databaseManager);
		jailService = new JailService(this, jailRepository, modConfig);
		registerCommand("jail", new JailCommand(jailService, modConfig, this));
		registerCommand("setjail", new SetJailCommand(jailService, modConfig, this));
		registerCommand("deljail", new DelJailCommand(jailService, modConfig, this));
		registerCommand("jails", new JailsCommand(jailService, modConfig, this));
		registerCommand("kit", new KitCommand(kitService, this));
		registerCommand("kits", new KitsCommand(kitService, this));
	}

	public void reloadPluginState() {
		reloadConfig();
		FileConfiguration config = getConfig();
		coreConfigLoader.load(config);
		securityConfig.load(config);
		utilityConfig.load(config);
		teleportConfig.load(config);
		modConfig.load(config);
		if (economyConfig != null) {
			economyConfig.load(config);
		}
		if (landConfig != null) {
			landConfig.load(config);
		}
		if (kitConfig != null) {
			kitConfig.load(config);
		}

		if (CoreTuffProvider.getIgnoreService() != null) {
			CoreTuffProvider.getIgnoreService().reload();
		}
		if (CoreTuffProvider.getTpManager() != null) {
			CoreTuffProvider.getTpManager().reload();
		}
		if (CoreTuffProvider.getRtpService() != null) {
			CoreTuffProvider.getRtpService().reload();
		}
	}

	private void logStartup(CommandSender console, boolean folia) {
		String teleportSystem = folia
				? "Using folia async teleport system."
				: "Using Paper async teleport system.";

		console.sendMessage(" ");
		console.sendMessage(LOGOGRAY + " ██████╗████████╗    ██████╗███████╗" + RESET);
		console.sendMessage(LOGOGRAY + "██╔════╝╚══██╔══╝   ██╔════╝██╔════╝" + RESET);
		console.sendMessage(LOGOGRAY + "██║        ██║      ██║     █████╗  " + RESET);
		console.sendMessage(LOGOGRAY + "██║        ██║      ██║     ██╔══╝  " + RESET);
		console.sendMessage(LOGOGRAY + "╚██████╗   ██║      ╚██████╗███████╗" + RESET);
		console.sendMessage(LOGOGRAY + " ╚═════╝   ╚═╝       ╚═════╝╚══════╝" + RESET);
		console.sendMessage(" ");
		console.sendMessage(CYAN + "CoreTuff Community Edition " + VER() + "v" + getDescription().getVersion() + RESET);
		console.sendMessage(BLUE + "Running on " + getServer().getName() + RESET);
		console.sendMessage(BLUE + "Using H2 Data Base" + RESET);
		console.sendMessage(BLUE + teleportSystem + RESET);
		console.sendMessage(CYAN + "Built by Voxelware Studios" + RESET);
		console.sendMessage(" ");
	}

	public Component formatRaw(String msg) {
		if (msg == null) msg = "";
		return ComponentUtils.translateLegacyText(msg);
	}

	public Component format(Player player, String msg, Map<String, String> placeholders) {
		String prefix = "[CoreTuff] ";

		if (msg == null) {
			msg = "";
		}

		if (placeholders != null) {
			for (Map.Entry<String, String> entry : placeholders.entrySet()) {
				msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
			}
		}

		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && player != null) {
			msg = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, msg);
		}

		return ComponentUtils.translateLegacyText(prefix + msg);
	}

	private void registerVault() {
		if (economyConfig == null || economyService == null) return;
		if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
			doRegisterVault();
		} else {
			Bukkit.getPluginManager().registerEvents(new Listener() {
				@EventHandler
				public void onVaultEnable(PluginEnableEvent event) {
					if (event.getPlugin().getName().equals("Vault")) {
						doRegisterVault();
					}
				}
			}, this);
		}
	}

	private void doRegisterVault() {
		try {
			new VaultHook(this, economyService, economyConfig, economyService.formatter()).register();
		} catch (Exception e) {
			getLogger().warning("Failed to register Vault economy: " + e.getMessage());
		}
	}

	private void initMetrics() {
		try {
			new Metrics(this, 33882);
		} catch (Throwable t) {
			getLogger().warning("Failed to initialize bStats metrics: " + t.getMessage());
		}
	}

	private void registerPlaceholderExpansion() {
		try {
			if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
				coreTuffExpansion = new CoreTuffExpansion(this);
				coreTuffExpansion.register();
				getLogger().info("PlaceholderAPI expansion registered.");
			}
		} catch (Exception e) {
			getLogger().warning("Failed to register PlaceholderAPI expansion: " + e.getMessage());
		}
	}

	private void startInterestTask() {
		if (economyConfig == null || economyService == null || !economyConfig.interestEnabled()) return;

		long intervalHours = economyConfig.interestIntervalHours();
		long ticks = intervalHours * 72000L;

		if (isFolia()) {
			Bukkit.getGlobalRegionScheduler().runAtFixedRate(
					this,
					scheduledTask -> economyService.applyInterest(),
					1L,
					ticks > 0 ? ticks : 72000L
			);
		} else {
			Bukkit.getScheduler().runTaskTimer(
					this,
					() -> economyService.applyInterest(),
					1L,
					ticks > 0 ? ticks : 72000L
			);
		}
		getLogger().info("Economy interest scheduler started (every " + intervalHours + " hours).");
	}

	public SecurityConfig getSecurityConfig() {
		return securityConfig;
	}

	public ModConfig getModConfig() {
		return modConfig;
	}

	public UtilityConfig getUtilityConfig() {
		return utilityConfig;
	}

	public TeleportConfig getTeleportConfig() {
		return teleportConfig;
	}

	public String getCoreString(String path) {
		return coreConfigLoader.getString(path);
	}

	public String getCoreString(String path, String fallback) {
		return coreConfigLoader.getString(path, fallback);
	}

	public EconomyConfig getEconomyConfig() {
		return economyConfig;
	}

	public CoreConfigLoader getCoreConfigLoader() {
		return coreConfigLoader;
	}
}
