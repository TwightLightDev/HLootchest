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
    public ScheduledTask runTask(Runnable task) {
        return wrap(Bukkit.getScheduler().runTask(plugin, task));
    }

    @Override
    public ScheduledTask runTaskLater(Runnable task, long delayTicks) {
        return wrap(Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks));
    }

    @Override
    public ScheduledTask runTaskTimer(Runnable task, long delayTicks, long periodTicks) {
        return wrap(Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks));
    }


    @Override
    public ScheduledTask runTaskAsynchronously(Runnable task) {
        return wrap(Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }

    @Override
    public ScheduledTask runTaskAsynchronouslyLater(Runnable task, long delayTicks) {
        return wrap(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks));
    }


    @Override
    public ScheduledTask runTask(Entity entity, Runnable task) {
        return runTask(task);
    }

    @Override
    public ScheduledTask runTaskLater(Entity entity, Runnable task, long delayTicks) {
        return runTaskLater(task, delayTicks);
    }

    @Override
    public ScheduledTask runTask(Location location, Runnable task) {
        return runTask(task);
    }

    @Override
    public ScheduledTask runTaskLater(Location location, Runnable task, long delayTicks) {
        return runTaskLater(task, delayTicks);
    }

    @Override
    public void cancelAll() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }
}
