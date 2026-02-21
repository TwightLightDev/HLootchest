package org.twightlight.hlootchest.scheduler;

public final class SchedulerProvider {

    private final SchedulerAdapter adapter;

    public SchedulerProvider(org.bukkit.plugin.Plugin plugin) {
        this.adapter = isFolia()
                ? new FoliaSchedulerAdapter(plugin)
                : new BukkitSchedulerAdapter(plugin);
    }

    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public SchedulerAdapter get() {
        return adapter;
    }
}
