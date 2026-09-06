package org.voxelware.coretuff.internal.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.voxelware.coretuff.api.scheduler.Scheduler;

import java.util.concurrent.CompletableFuture;

public class SchedulerImpl implements Scheduler {

    private final Plugin plugin;
    private final boolean folia;

    public SchedulerImpl(Plugin plugin, boolean folia) {
        this.plugin = plugin;
        this.folia = folia;
    }

    @Override
    public void run(Runnable task) {
        if (folia) {
            Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    @Override
    public void runLater(Runnable task, long delayTicks) {
        if (folia) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin,
                    scheduledTask -> task.run(), delayTicks < 1 ? 1 : delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task,
                    delayTicks < 1 ? 1 : delayTicks);
        }
    }

    @Override
    public void runRepeating(Runnable task, long delayTicks, long periodTicks) {
        if (folia) {
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin,
                    scheduledTask -> task.run(),
                    delayTicks < 1 ? 1 : delayTicks,
                    periodTicks < 1 ? 1 : periodTicks);
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, task,
                    delayTicks < 1 ? 1 : delayTicks,
                    periodTicks < 1 ? 1 : periodTicks);
        }
    }

    @Override
    public CompletableFuture<Void> runAsync(Runnable task) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (folia) {
            Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
                try {
                    task.run();
                    future.complete(null);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            });
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    task.run();
                    future.complete(null);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            });
        }
        return future;
    }

    @Override
    public void runEntity(Entity entity, Runnable task) {
        if (folia) {
            entity.getScheduler().run(plugin, scheduledTask -> task.run(), null);
        } else {
            if (entity.isValid()) {
                task.run();
            }
        }
    }

    @Override
    public Plugin plugin() { return plugin; }

    @Override
    public boolean isFolia() { return folia; }
}
