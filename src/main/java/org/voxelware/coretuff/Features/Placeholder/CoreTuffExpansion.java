package org.voxelware.coretuff.Features.Placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Features.Economy.EconomyService;
import org.voxelware.coretuff.Features.Lands.Warps.Warp;
import org.voxelware.coretuff.Features.Lands.Warps.WarpService;
import org.voxelware.coretuff.Features.Moderation.ModIO.BanCheckResult;
import org.voxelware.coretuff.Features.Moderation.Service.ModerationService;
import org.voxelware.coretuff.Features.Moderation.Service.WarnService;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CoreTuffExpansion extends PlaceholderExpansion {

    private static final Pattern RANK_PATTERN = Pattern.compile("^rank_(\\d+)_(name|balance)$");

    private final CoreTuff plugin;

    public CoreTuffExpansion(CoreTuff plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "coretuff";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.isEmpty()) return null;

        if (params.startsWith("economy_")) {
            return handleEconomy(player, params.substring("economy_".length()));
        }
        if (params.startsWith("warps_")) {
            return handleWarps(params.substring("warps_".length()));
        }
        if (params.startsWith("moderation_")) {
            return handleModeration(player, params.substring("moderation_".length()));
        }
        return null;
    }

    private String handleEconomy(Player player, String params) {
        EconomyService eco = plugin.economyService();
        if (eco == null) return "0";

        if (params.equals("currency_symbol")) return eco.config().currencySymbol();
        if (params.equals("currency_name")) return eco.config().currencyName();
        if (params.equals("currency_name_plural")) return eco.config().currencyNamePlural();

        if (params.startsWith("has_funds_")) {
            if (player == null) return "no";
            try {
                double amount = Double.parseDouble(params.substring("has_funds_".length()));
                try {
                    return eco.getBalance(player.getUniqueId()) >= amount ? "yes" : "no";
                } catch (Exception e) {
                    return "no";
                }
            } catch (NumberFormatException e) {
                return "no";
            }
        }

        if (params.startsWith("rank_")) {
            Matcher m = RANK_PATTERN.matcher(params);
            if (!m.matches()) return null;
            int pos = Integer.parseInt(m.group(1)) - 1;
            if (pos < 0) return null;
            List<Map.Entry<UUID, Double>> top = eco.getTopBalances(pos + 1);
            if (pos >= top.size()) return "0";
            double bal = top.get(pos).getValue();
            if ("balance".equals(m.group(2))) {
                return eco.formatter().format(bal);
            }
            OfflinePlayer p = Bukkit.getOfflinePlayer(top.get(pos).getKey());
            return p.getName() != null ? p.getName() : "Unknown";
        }

        OfflinePlayer target = player;
        boolean raw = false;
        boolean hasTargetSuffix = false;

        if (params.startsWith("balance_raw_")) {
            raw = true;
            hasTargetSuffix = true;
            String name = params.substring("balance_raw_".length());
            if (!name.isEmpty()) target = Bukkit.getOfflinePlayer(name);
        } else if (params.equals("balance_raw")) {
            raw = true;
        } else if (params.startsWith("balance_")) {
            hasTargetSuffix = true;
            String name = params.substring("balance_".length());
            if (!name.isEmpty()) target = Bukkit.getOfflinePlayer(name);
        } else if (!params.equals("balance")) {
            return null;
        }

        if (hasTargetSuffix && target == null) return "0";
        if (!hasTargetSuffix && target == null) return "0";
        if (target != null && !target.isOnline() && !target.hasPlayedBefore()) return "0";

        try {
            double balance = eco.getBalance(target.getUniqueId());
            return raw ? String.valueOf(balance) : eco.formatter().format(balance);
        } catch (Exception e) {
            return "0";
        }
    }

    private String handleWarps(String params) {
        WarpService ws = plugin.warpService();
        if (ws == null) return "0";

        try {
            List<Warp> warps = ws.getAllWarps();
            if (params.equals("count")) return String.valueOf(warps.size());
            if (params.equals("list")) {
                return warps.stream().map(Warp::getName).collect(Collectors.joining(", "));
            }
            if (params.equals("list_with_desc")) {
                return warps.stream()
                        .map(w -> w.getDescription() != null ? w.getName() + " (" + w.getDescription() + ")" : w.getName())
                        .collect(Collectors.joining(", "));
            }
        } catch (Exception ignored) {}

        return "0";
    }

    private String handleModeration(Player player, String params) {
        // %coretuff_moderation_warnings% or %coretuff_moderation_warnings_<player>%
        if (params.equals("warnings") || params.startsWith("warnings_")) {
            OfflinePlayer target = resolvePlayerParam(player, params, "warnings_");
            return getWarningsPoints(target);
        }

        // %coretuff_moderation_is_banned% or %coretuff_moderation_is_banned_<player>%
        if (params.equals("is_banned") || params.startsWith("is_banned_")) {
            OfflinePlayer target = resolvePlayerParam(player, params, "is_banned_");
            return isBanned(target) ? "yes" : "no";
        }

        // %coretuff_moderation_punishments% or %coretuff_moderation_punishments_<player>%
        if (params.equals("punishments") || params.startsWith("punishments_")) {
            OfflinePlayer target = resolvePlayerParam(player, params, "punishments_");
            return String.valueOf(getRecentPunishments(target));
        }

        return null;
    }

    private OfflinePlayer resolvePlayerParam(Player player, String params, String prefix) {
        if (params.startsWith(prefix)) {
            String name = params.substring(prefix.length());
            if (!name.isEmpty()) return Bukkit.getOfflinePlayer(name);
        }
        return player;
    }

    private String getWarningsPoints(OfflinePlayer target) {
        if (target == null || !target.hasPlayedBefore()) return "0";
        WarnService ws = plugin.warnService();
        if (ws == null) return "0";
        return String.valueOf(ws.getWarns(target.getUniqueId()));
    }

    private boolean isBanned(OfflinePlayer target) {
        if (target == null) return false;
        ModerationService ms = plugin.moderationService();
        if (ms == null) return false;
        BanCheckResult result = ms.checkBan(target.getUniqueId(), target.getName());
        return result != null && result.isBanned();
    }

    private int getRecentPunishments(OfflinePlayer target) {
        if (target == null || !target.hasPlayedBefore()) return 0;
        ModerationService ms = plugin.moderationService();
        if (ms == null) return 0;
        return (int) ms.getRecentPunishments(100).stream()
                .filter(p -> target.getUniqueId().toString().equals(p.getUuid()))
                .count();
    }
}
