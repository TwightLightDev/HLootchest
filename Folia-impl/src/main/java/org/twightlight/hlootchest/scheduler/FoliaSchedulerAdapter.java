package org.twightlight.hlootchest.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class FoliaSchedulerAdapter implements SchedulerAdapter {

    private final Plugin plugin;

    public FoliaSchedulerAdapter(Plugin plugin) {
        this.plugin = plugin;
    }

    private ScheduledTask wrap(io.papermc.paper.threadedregions.scheduler.ScheduledTask task) {
        return new ScheduledTask() {
            @Override
            public void cancel() {
                task.cancel();
            }

            @Override
            public boolean isCancelled() {
                return task.isCancelled();
            }
        };
    }


    @Override
    public ScheduledTask runTask(Runnable task) {
        return wrap(Bukkit.getGlobalRegionScheduler()
                .run(plugin, t -> task.run()));
    }

    @Override
    public ScheduledTask runTaskLater(Runnable task, long delayTicks) {
        return wrap(Bukkit.getGlobalRegionScheduler()
                .runDelayed(plugin, t -> task.run(), delayTicks));
    }

    @Override
    public ScheduledTask runTaskTimer(Runnable task, long delayTicks, long periodTicks) {
        return wrap(Bukkit.getGlobalRegionScheduler()
                .runAtFixedRate(plugin, t -> task.run(), delayTicks, periodTicks));
    }


    @Override
    public ScheduledTask runTaskAsynchronously(Runnable task) {
        return wrap(Bukkit.getAsyncScheduler()
                .runNow(plugin, t -> task.run()));
    }

    @Override
    public ScheduledTask runTaskAsynchronouslyLater(Runnable task, long delayTicks) {
        long delayMillis = delayTicks * 50L;

        return wrap(Bukkit.getAsyncScheduler()
                .runDelayed(plugin,
                        t -> task.run(),
                        delayMillis,
                        TimeUnit.MILLISECONDS));
    }


    @Override
    public ScheduledTask runTask(Entity entity, Runnable task) {
        return wrap(entity.getScheduler()
                .run(plugin, t -> task.run(), null));
    }

    @Override
    public ScheduledTask runTaskLater(Entity entity, Runnable task, long delayTicks) {
        return wrap(entity.getScheduler()
                .runDelayed(plugin, t -> task.run(), null, delayTicks));
    }


    @Override
    public ScheduledTask runTask(Location location, Runnable task) {
        return wrap(Bukkit.getRegionScheduler()
                .run(plugin, location, t -> task.run()));
    }

    @Override
    public ScheduledTask runTaskLater(Location location, Runnable task, long delayTicks) {
        return wrap(Bukkit.getRegionScheduler()
                .runDelayed(plugin, location, t -> task.run(), delayTicks));
    }


    @Override
    public void cancelAll() {
        Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
    }
}