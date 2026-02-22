package org.twightlight.hlootchest.scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface SchedulerAdapter {


    ScheduledTask runTask(Runnable task);

    ScheduledTask runTaskLater(Runnable task, long delayTicks);

    ScheduledTask runTaskTimer(Runnable task, long delayTicks, long periodTicks);


    ScheduledTask runTaskAsynchronously(Runnable task);

    ScheduledTask runTaskAsynchronouslyLater(Runnable task, long delayTicks);

    ScheduledTask runTaskAsynchronouslyTimer(Runnable task, long delayTicks, long periodTicks);


    ScheduledTask runTask(Entity entity, Runnable task);

    ScheduledTask runTaskLater(Entity entity, Runnable task, long delayTicks);

    ScheduledTask runTaskTimer(Entity entity, Runnable task, long delayTicks, long periodTicks);


    ScheduledTask runTask(Location location, Runnable task);

    ScheduledTask runTaskLater(Location location, Runnable task, long delayTicks);

    ScheduledTask runTaskTimer(Location location, Runnable task, long delayTicks, long periodTicks);

    void cancelAll();

}
