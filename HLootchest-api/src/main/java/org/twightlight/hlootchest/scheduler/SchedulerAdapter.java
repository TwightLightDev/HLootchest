package org.twightlight.hlootchest.scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface SchedulerAdapter {


    ScheduledTask run(Runnable task);

    ScheduledTask runLater(Runnable task, long delayTicks);

    ScheduledTask runTimer(Runnable task, long delayTicks, long periodTicks);


    ScheduledTask runAsync(Runnable task);

    ScheduledTask runAsyncLater(Runnable task, long delayTicks);


    ScheduledTask runAtEntity(Entity entity, Runnable task);

    ScheduledTask runAtEntityLater(Entity entity, Runnable task, long delayTicks);


    ScheduledTask runAtLocation(Location location, Runnable task);

    ScheduledTask runAtLocationLater(Location location, Runnable task, long delayTicks);

    void cancelAll();

    interface ScheduledTask {
        void cancel();
        boolean isCancelled();
    }
}
