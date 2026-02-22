package org.twightlight.hlootchest.scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface SchedulerAdapter {


    ScheduledTask runTask(Runnable task);

    ScheduledTask runTaskLater(Runnable task, long delayTicks);

    ScheduledTask runTaskTimer(Runnable task, long delayTicks, long periodTicks);


    ScheduledTask runTaskAsynchronously(Runnable task);

    ScheduledTask runTaskAsynchronouslyLater(Runnable task, long delayTicks);


    ScheduledTask runTask(Entity entity, Runnable task);

    ScheduledTask runTaskLater(Entity entity, Runnable task, long delayTicks);


    ScheduledTask runTask(Location location, Runnable task);

    ScheduledTask runTaskLater(Location location, Runnable task, long delayTicks);

    void cancelAll();

}
