package org.twightlight.hlootchest.database;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.bukkit.OfflinePlayer;
import org.twightlight.hlootchest.api.enums.DatabaseType;
import org.twightlight.hlootchest.api.interfaces.internal.TDatabase;
import org.twightlight.hlootchest.dependency.Classloader;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RedisDatabase implements TDatabase {

    private static final Logger LOGGER = Logger.getLogger("HLootChest");
    private static final String KEY_PREFIX = "hlootchest:";
    private static final int CACHE_TTL_SECONDS = 300;

    private final TDatabase backingDatabase;
    private final Object jedisPool;
    private final Gson gson = new Gson();
    private final ExecutorService executor;

    public RedisDatabase(TDatabase backingDatabase, Classloader classloader,
                         String host, int port, String password) {
        this.backingDatabase = backingDatabase;
        this.jedisPool = createJedisPool(classloader, host, port, password);
        this.executor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "HLootChest-Redis");
            t.setDaemon(true);
            return t;
        });
    }

    private static Object createJedisPool(ClassLoader libLoader, String host, int port, String password) {
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(libLoader);

            Class<?> jedisPoolConfigClass = Class.forName("redis.clients.jedis.JedisPoolConfig", true, libLoader);
            Object poolConfig = jedisPoolConfigClass.getDeclaredConstructor().newInstance();
            jedisPoolConfigClass.getMethod("setMaxTotal", int.class).invoke(poolConfig, 16);
            jedisPoolConfigClass.getMethod("setMaxIdle", int.class).invoke(poolConfig, 8);
            jedisPoolConfigClass.getMethod("setMinIdle", int.class).invoke(poolConfig, 2);
            jedisPoolConfigClass.getMethod("setTestOnBorrow", boolean.class).invoke(poolConfig, true);

            Class<?> jedisPoolClass = Class.forName("redis.clients.jedis.JedisPool", true, libLoader);

            if (password != null && !password.isEmpty()) {
                Class<?> baseObjPoolConfigClass = Class.forName("org.apache.commons.pool2.impl.GenericObjectPoolConfig", true, libLoader);
                return jedisPoolClass
                        .getConstructor(baseObjPoolConfigClass, String.class, int.class, int.class, String.class)
                        .newInstance(poolConfig, host, port, 2000, password);
            } else {
                Class<?> baseObjPoolConfigClass = Class.forName("org.apache.commons.pool2.impl.GenericObjectPoolConfig", true, libLoader);
                return jedisPoolClass
                        .getConstructor(baseObjPoolConfigClass, String.class, int.class)
                        .newInstance(poolConfig, host, port);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to init Redis pool", e);
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }

    private Object getJedis() {
        try {
            return jedisPool.getClass().getMethod("getResource").invoke(jedisPool);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get Jedis resource", e);
            return null;
        }
    }

    private void closeJedis(Object jedis) {
        if (jedis == null) return;
        try {
            jedis.getClass().getMethod("close").invoke(jedis);
        } catch (Exception ignored) {}
    }

    private String redisGet(String key) {
        Object jedis = getJedis();
        if (jedis == null) return null;
        try {
            Object result = jedis.getClass().getMethod("get", String.class).invoke(jedis, key);
            return result != null ? result.toString() : null;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Redis GET failed for key: " + key, e);
            return null;
        } finally {
            closeJedis(jedis);
        }
    }

    private void redisSetex(String key, int seconds, String value) {
        Object jedis = getJedis();
        if (jedis == null) return;
        try {
            jedis.getClass().getMethod("setex", String.class, long.class, String.class)
                    .invoke(jedis, key, (long) seconds, value);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Redis SETEX failed for key: " + key, e);
        } finally {
            closeJedis(jedis);
        }
    }

    private void redisDel(String key) {
        Object jedis = getJedis();
        if (jedis == null) return;
        try {
            jedis.getClass().getMethod("del", String[].class)
                    .invoke(jedis, (Object) new String[]{key});
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Redis DEL failed for key: " + key, e);
        } finally {
            closeJedis(jedis);
        }
    }

    private String cacheKey(OfflinePlayer player, String column) {
        return KEY_PREFIX + player.getUniqueId().toString() + ":" + column;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return backingDatabase.getConnection();
    }

    @Override
    public boolean isConnected() {
        return backingDatabase.isConnected();
    }

    @Override
    public void createPlayerData(OfflinePlayer player) {
        backingDatabase.createPlayerData(player);
    }

    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.REDIS;
    }

    @Override
    public <T> T getLootChestData(OfflinePlayer player, String column, TypeToken<T> typeToken, T fallback) {
        String key = cacheKey(player, column);
        String cached = redisGet(key);
        if (cached != null) {
            try {
                T result = gson.fromJson(cached, typeToken.getType());
                if (result != null) return result;
            } catch (Exception ignored) {}
        }

        T result = backingDatabase.getLootChestData(player, column, typeToken, fallback);
        if (result != null) {
            redisSetex(key, CACHE_TTL_SECONDS, gson.toJson(result));
        }
        return result;
    }

    @Override
    public Map<String, Integer> getLootChestData(OfflinePlayer player, String column) {
        return getLootChestData(player, column, new TypeToken<Map<String, Integer>>() {}, new java.util.HashMap<>());
    }

    @Override
    public <T> boolean updateData(OfflinePlayer player, T data, String column) {
        boolean result = backingDatabase.updateData(player, data, column);
        if (result) {
            String key = cacheKey(player, column);
            if (data == null) {
                redisDel(key);
            } else {
                redisSetex(key, CACHE_TTL_SECONDS, gson.toJson(data));
            }
        }
        return result;
    }

    @Override
    public boolean addLootchest(OfflinePlayer player, String lootchestId, Integer amount, String column) {
        boolean result = backingDatabase.addLootchest(player, lootchestId, amount, column);
        if (result) {
            redisDel(cacheKey(player, column));
        }
        return result;
    }

    @Override
    public boolean addColumnIfNotExists(String columnName, String columnType, String defaultValue) {
        return backingDatabase.addColumnIfNotExists(columnName, columnType, defaultValue);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
        if (jedisPool != null) {
            try {
                Method destroyMethod = jedisPool.getClass().getMethod("destroy");
                destroyMethod.invoke(jedisPool);
            } catch (Exception ignored) {}
        }
        backingDatabase.shutdown();
    }

    @Override
    public CompletableFuture<Void> createPlayerDataAsync(OfflinePlayer player) {
        return CompletableFuture.runAsync(() -> createPlayerData(player), executor);
    }

    @Override
    public <T> CompletableFuture<T> getLootChestDataAsync(OfflinePlayer player, String column, TypeToken<T> type, T fallback) {
        return CompletableFuture.supplyAsync(() -> getLootChestData(player, column, type, fallback), executor);
    }

    @Override
    public CompletableFuture<Map<String, Integer>> getLootChestDataAsync(OfflinePlayer player, String column) {
        return CompletableFuture.supplyAsync(() -> getLootChestData(player, column), executor);
    }

    @Override
    public <T> CompletableFuture<Boolean> updateDataAsync(OfflinePlayer player, T data, String column) {
        return CompletableFuture.supplyAsync(() -> updateData(player, data, column), executor);
    }

    @Override
    public CompletableFuture<Boolean> addLootchestAsync(OfflinePlayer player, String lootchestId, Integer amount, String column) {
        return CompletableFuture.supplyAsync(() -> addLootchest(player, lootchestId, amount, column), executor);
    }
}

