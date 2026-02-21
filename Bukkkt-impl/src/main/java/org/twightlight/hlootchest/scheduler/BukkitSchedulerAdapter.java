package org.twightlight.hlootchest.scheduler;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class BukkitSchedulerAdapter implements SchedulerAdapter {

    private final Plugin plugin;

    public BukkitSchedulerAdapter(Plugin plugin) {
        this.plugin = plugin;
    }

    private ScheduledTask wrap(BukkitTask task) {
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
    public ScheduledTask run(Runnable task) {
        return wrap(Bukkit.getScheduler().runTask(plugin, task));
    }

    @Override
    public ScheduledTask runLater(Runnable task, long delayTicks) {
        return wrap(Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks));
    }

    @Override
    public ScheduledTask runTimer(Runnable task, long delayTicks, long periodTicks) {
        return wrap(Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks));
    }


    @Override
    public ScheduledTask runAsync(Runnable task) {
        return wrap(Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }

    @Override
    public ScheduledTask runAsyncLater(Runnable task, long delayTicks) {
        return wrap(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks));
    }


    @Override
    public ScheduledTask runAtEntity(Entity entity, Runnable task) {
        return run(task);
    }

    @Override
    public ScheduledTask runAtEntityLater(Entity entity, Runnable task, long delayTicks) {
        return runLater(task, delayTicks);
    }

    @Override
    public ScheduledTask runAtLocation(Location location, Runnable task) {
        return run(task);
    }

    @Override
    public ScheduledTask runAtLocationLater(Location location, Runnable task, long delayTicks) {
        return runLater(task, delayTicks);
    }

    @Override
    public void cancelAll() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }
}
