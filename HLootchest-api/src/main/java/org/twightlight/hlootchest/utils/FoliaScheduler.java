package org.twightlight.hlootchest.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public final class FoliaScheduler {

    private static final boolean IS_FOLIA;
    private static Method getGlobalRegionScheduler;
    private static Method getAsyncScheduler;
    private static Method getEntityScheduler;
    private static Method getRegionScheduler;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        IS_FOLIA = folia;

        if (IS_FOLIA) {
            try {
                getGlobalRegionScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler");
                getAsyncScheduler = Bukkit.class.getMethod("getAsyncScheduler");
                getRegionScheduler = Bukkit.class.getMethod("getRegionScheduler");
            } catch (Exception ignored) {}
        }
    }

    public static boolean isFolia() {
        return IS_FOLIA;
    }

    public static void runTask(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            try {
                Object scheduler = getGlobalRegionScheduler.invoke(null);
                scheduler.getClass()
                        .getMethod("run", Plugin.class, Object.class)
                        .invoke(scheduler, plugin, (Object) (java.util.function.Consumer<?>) (t) -> task.run());
            } catch (Exception e) {
                task.run();
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public static void runTaskLater(Plugin plugin, Runnable task, long delayTicks) {
        if (IS_FOLIA) {
            try {
                Object scheduler = getGlobalRegionScheduler.invoke(null);
                scheduler.getClass()
                        .getMethod("runDelayed", Plugin.class, Object.class, long.class)
                        .invoke(scheduler, plugin, (Object) (java.util.function.Consumer<?>) (t) -> task.run(), delayTicks);
            } catch (Exception e) {
                task.run();
            }
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    public static void runTaskTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        if (IS_FOLIA) {
            try {
                Object scheduler = getGlobalRegionScheduler.invoke(null);
                scheduler.getClass()
                        .getMethod("runAtFixedRate", Plugin.class, Object.class, long.class, long.class)
                        .invoke(scheduler, plugin,
                                (Object) (java.util.function.Consumer<?>) (t) -> task.run(),
                                Math.max(1, delayTicks), periodTicks);
            } catch (Exception e) {
                task.run();
            }
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
        }
    }

    public static void runAsync(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            try {
                Object scheduler = getAsyncScheduler.invoke(null);
                scheduler.getClass()
                        .getMethod("runNow", Plugin.class, Object.class)
                        .invoke(scheduler, plugin, (Object) (java.util.function.Consumer<?>) (t) -> task.run());
            } catch (Exception e) {
                task.run();
            }
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    public static void runAtEntity(Plugin plugin, Entity entity, Runnable task) {
        if (IS_FOLIA) {
            try {
                Object scheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
                scheduler.getClass()
                        .getMethod("run", Plugin.class, Object.class, Runnable.class)
                        .invoke(scheduler, plugin, (Object) (java.util.function.Consumer<?>) (t) -> task.run(), null);
            } catch (Exception e) {
                task.run();
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public static void runAtEntityLater(Plugin plugin, Entity entity, Runnable task, long delayTicks) {
        if (IS_FOLIA) {
            try {
                Object scheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
                scheduler.getClass()
                        .getMethod("runDelayed", Plugin.class, Object.class, Runnable.class, long.class)
                        .invoke(scheduler, plugin,
                                (Object) (java.util.function.Consumer<?>) (t) -> task.run(),
                                null, delayTicks);
            } catch (Exception e) {
                task.run();
            }
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    public static void runAtLocation(Plugin plugin, Location location, Runnable task) {
        if (IS_FOLIA) {
            try {
                Object scheduler = getRegionScheduler.invoke(null);
                scheduler.getClass()
                        .getMethod("run", Plugin.class, Location.class, Object.class)
                        .invoke(scheduler, plugin, location,
                                (Object) (java.util.function.Consumer<?>) (t) -> task.run());
            } catch (Exception e) {
                task.run();
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    public static void runAtLocationLater(Plugin plugin, Location location, Runnable task, long delayTicks) {
        if (IS_FOLIA) {
            try {
                Object scheduler = getRegionScheduler.invoke(null);
                scheduler.getClass()
                        .getMethod("runDelayed", Plugin.class, Location.class, Object.class, long.class)
                        .invoke(scheduler, plugin, location,
                                (Object) (java.util.function.Consumer<?>) (t) -> task.run(), delayTicks);
            } catch (Exception e) {
                task.run();
            }
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    public static void cancelAllTasks(Plugin plugin) {
        if (IS_FOLIA) {
            try {
                Object scheduler = getGlobalRegionScheduler.invoke(null);
                scheduler.getClass().getMethod("cancelTasks", Plugin.class).invoke(scheduler, plugin);
            } catch (Exception ignored) {}
        } else {
            Bukkit.getScheduler().cancelTasks(plugin);
        }
    }
}

