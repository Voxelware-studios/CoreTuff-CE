package org.voxelware.coretuff.Features.Moderation.Jail;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Features.Moderation.ConfigEngine.ModConfig;

import java.util.List;
import java.util.UUID;

public class JailService {

    private final CoreTuff plugin;
    private final JailRepository repository;
    private final ModConfig config;

    public JailService(CoreTuff plugin, JailRepository repository, ModConfig config) {
        this.plugin = plugin;
        this.repository = repository;
        this.config = config;
    }

    public void setJail(String name, Player creator) throws Exception {
        var loc = creator.getLocation();
        repository.setJail(new Jail(name, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()));
    }

    public boolean deleteJail(String name) throws Exception {
        return repository.deleteJail(name);
    }

    public Jail getJail(String name) throws Exception {
        return repository.getJail(name);
    }

    public List<Jail> getAllJails() throws Exception {
        return repository.getAllJails();
    }

    public Location toLocation(Jail jail) {
        return repository.toLocation(jail);
    }

    public boolean isPlayerJailed(UUID uuid) throws Exception {
        return repository.isPlayerJailed(uuid);
    }

    public void jailPlayer(Player target, String jailName, String by, String reason) throws Exception {
        long until = 0;
        repository.jailPlayer(target.getUniqueId(), jailName, by, reason, until);
    }

    public void unjailPlayer(UUID uuid) throws Exception {
        repository.unjailPlayer(uuid);
    }

    public void unjailPlayer(Player player) throws Exception {
        repository.unjailPlayer(player.getUniqueId());
    }

    public JailRepository.JailedPlayer getJailedPlayer(UUID uuid) throws Exception {
        return repository.getJailedPlayer(uuid);
    }

    public List<JailRepository.JailedPlayer> getAllJailedPlayers() throws Exception {
        return repository.getAllJailedPlayers();
    }

    public void teleportToJail(Player player, Jail jail) {
        Location loc = repository.toLocation(jail);
        if (loc == null) return;
        if (config.jailTeleportDelay() > 0) {
            var dp = org.voxelware.coretuff.Utility.CoreTuffProvider.getDelayedTeleporter();
            if (dp != null) {
                dp.teleport(player, loc, config.jailTeleportDelay()).whenComplete((s, t) -> {
                });
                return;
            }
        }
        player.teleportAsync(loc).whenComplete((s, t) -> {
        });
    }

}
