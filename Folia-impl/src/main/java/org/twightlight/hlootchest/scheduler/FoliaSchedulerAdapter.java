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
    public ScheduledTask runTaskAsynchronouslyLater(Runnable task, long delayMillis) {
        long delayTicks = delayMillis * 50L;

        return wrap(Bukkit.getAsyncScheduler()
                .runDelayed(plugin,
                        t -> task.run(),
                        delayTicks,
                        TimeUnit.MILLISECONDS));
    }

    @Override
    public ScheduledTask runTaskAsynchronouslyTimer(Runnable task, long delayMillis, long periodMillis) {
        long delayTicks = delayMillis * 50L;
        long periodTicks = periodMillis * 50L;

        return wrap(Bukkit.getAsyncScheduler()
                .runAtFixedRate(plugin,
                        t -> task.run(),
                        delayTicks, periodTicks,
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
    public ScheduledTask runTaskTimer(Entity entity, Runnable task, long delayTicks, long periodTicks) {
        return wrap(entity.getScheduler()
                .runAtFixedRate(plugin, t -> task.run(), null, delayTicks, periodTicks));
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
    public ScheduledTask runTaskTimer(Location location, Runnable task, long delayTicks, long periodTicks) {
        return wrap(Bukkit.getRegionScheduler()
                .runAtFixedRate(plugin, location, t -> task.run(), delayTicks, periodTicks));
    }



    @Override
    public void cancelAll() {
        Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
        Bukkit.getAsyncScheduler().cancelTasks(plugin);
    }
}