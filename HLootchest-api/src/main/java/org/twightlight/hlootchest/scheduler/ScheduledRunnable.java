package org.twightlight.hlootchest.scheduler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public abstract class ScheduledRunnable implements Runnable {

    private ScheduledTask task;

    public abstract void run();

    public ScheduledTask runTask(SchedulerAdapter adapter) {
        this.task = adapter.runTask(this);
        return task;
    }

    public ScheduledTask runTaskLater(SchedulerAdapter adapter, long delayTicks) {
        this.task = adapter.runTaskLater(this, delayTicks);
        return task;
    }

    public ScheduledTask runTaskTimer(SchedulerAdapter adapter, long delayTicks, long periodTicks) {
        this.task = adapter.runTaskTimer(this, delayTicks, periodTicks);
        return task;
    }

    public ScheduledTask runTaskAsynchronously(SchedulerAdapter adapter) {
        this.task = adapter.runTaskAsynchronously(this);
        return task;
    }

    public ScheduledTask runTaskAsynchronouslyLater(SchedulerAdapter adapter, long delayTicks) {
        this.task = adapter.runTaskAsynchronouslyLater(this, delayTicks);
        return task;
    }

    public ScheduledTask runTaskAsynchronouslyTimer(SchedulerAdapter adapter, long delayTicks, long periodTicks) {
        this.task = adapter.runTaskAsynchronouslyTimer(this, delayTicks, periodTicks);
        return task;
    }

    public ScheduledTask runTask(Entity entity, SchedulerAdapter adapter) {
        this.task = adapter.runTask(entity, this);
        return task;
    }

    public ScheduledTask runTaskLater(Entity entity, SchedulerAdapter adapter, long delayTicks) {
        this.task = adapter.runTaskLater(entity, this, delayTicks);
        return task;
    }

    public ScheduledTask runTaskTimer(Entity entity, SchedulerAdapter adapter, long delayTicks, long periodTicks) {
        this.task = adapter.runTaskTimer(entity, this, delayTicks, periodTicks);
        return task;
    }

    public ScheduledTask runTask(Location location, SchedulerAdapter adapter) {
        this.task = adapter.runTask(location, this);
        return task;
    }

    public ScheduledTask runTaskLater(Location location, SchedulerAdapter adapter, long delayTicks) {
        this.task = adapter.runTaskLater(location, this, delayTicks);
        return task;
    }

    public ScheduledTask runTaskTimer(Location location, SchedulerAdapter adapter, long delayTicks, long periodTicks) {
        this.task = adapter.runTaskTimer(location, this, delayTicks, periodTicks);
        return task;
    }

    public void cancel() {
        if (task != null) {
            task.cancel();
        }
    }

    public ScheduledTask getTask() {
        return task;
    }
}
