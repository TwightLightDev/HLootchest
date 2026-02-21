package org.twightlight.hlootchest.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.util.concurrent.TimeUnit;

public class FoliaSchedulerAdapter implements SchedulerAdapter {

    private final Plugin plugin;

    public FoliaSchedulerAdapter(Plugin plugin) {
        this.plugin = plugin;
    }

    private SchedulerAdapter.ScheduledTask wrap(io.papermc.paper.threadedregions.scheduler.ScheduledTask task) {
        return new SchedulerAdapter.ScheduledTask() {
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
    public SchedulerAdapter.ScheduledTask run(Runnable task) {
        return wrap(Bukkit.getGlobalRegionScheduler()
                .run(plugin, t -> task.run()));
    }

    @Override
    public SchedulerAdapter.ScheduledTask runLater(Runnable task, long delayTicks) {
        return wrap(Bukkit.getGlobalRegionScheduler()
                .runDelayed(plugin, t -> task.run(), delayTicks));
    }

    @Override
    public SchedulerAdapter.ScheduledTask runTimer(Runnable task, long delayTicks, long periodTicks) {
        return wrap(Bukkit.getGlobalRegionScheduler()
                .runAtFixedRate(plugin, t -> task.run(), delayTicks, periodTicks));
    }


    @Override
    public SchedulerAdapter.ScheduledTask runAsync(Runnable task) {
        return wrap(Bukkit.getAsyncScheduler()
                .runNow(plugin, t -> task.run()));
    }

    @Override
    public SchedulerAdapter.ScheduledTask runAsyncLater(Runnable task, long delayTicks) {
        long delayMillis = delayTicks * 50L;

        return wrap(Bukkit.getAsyncScheduler()
                .runDelayed(plugin,
                        t -> task.run(),
                        delayMillis,
                        TimeUnit.MILLISECONDS));
    }


    @Override
    public SchedulerAdapter.ScheduledTask runAtEntity(Entity entity, Runnable task) {
        return wrap(entity.getScheduler()
                .run(plugin, t -> task.run(), null));
    }

    @Override
    public SchedulerAdapter.ScheduledTask runAtEntityLater(Entity entity, Runnable task, long delayTicks) {
        return wrap(entity.getScheduler()
                .runDelayed(plugin, t -> task.run(), null, delayTicks));
    }


    @Override
    public SchedulerAdapter.ScheduledTask runAtLocation(Location location, Runnable task) {
        return wrap(Bukkit.getRegionScheduler()
                .run(plugin, location, t -> task.run()));
    }

    @Override
    public SchedulerAdapter.ScheduledTask runAtLocationLater(Location location, Runnable task, long delayTicks) {
        return wrap(Bukkit.getRegionScheduler()
                .runDelayed(plugin, location, t -> task.run(), delayTicks));
    }


    @Override
    public void cancelAll() {
        Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
    }
}