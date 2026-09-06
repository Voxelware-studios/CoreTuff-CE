package org.voxelware.coretuff.api.scheduler;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

public interface Scheduler {

    void run(Runnable task);

    void runLater(Runnable task, long delayTicks);

    void runRepeating(Runnable task, long delayTicks, long periodTicks);

    CompletableFuture<Void> runAsync(Runnable task);

    void runEntity(Entity entity, Runnable task);

    Plugin plugin();

    boolean isFolia();
}
