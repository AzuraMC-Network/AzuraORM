package cc.azuramc.orm.cache;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MemoryCache<K, V> implements Cache<K, V> {
    private static final Logger logger = Logger.getLogger(MemoryCache.class.getName());
    
    private final Map<K, CacheEntry<V>> cache;
    private final ScheduledExecutorService cleanupExecutor;
    
    public MemoryCache() {
        this.cache = new ConcurrentHashMap<>();
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
        startCleanupTask();
    }
    
    private void startCleanupTask() {
        cleanupExecutor.scheduleAtFixedRate(this::removeExpiredEntries, 1, 1, TimeUnit.MINUTES);
    }
    
    @Override
    public void put(K key, V value) {
        cache.put(key, new CacheEntry<>(value));
        logger.fine("Cached value for key: " + key);
    }
    
    @Override
    public void put(K key, V value, long duration, TimeUnit unit) {
        long expiryTime = System.currentTimeMillis() + unit.toMillis(duration);
        cache.put(key, new CacheEntry<>(value, expiryTime));
        logger.fine("Cached value for key: " + key + " with expiry: " + expiryTime);
    }
    
    @Override
    public Optional<V> get(K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        
        if (entry.isExpired()) {
            cache.remove(key);
            return Optional.empty();
        }
        
        return Optional.of(entry.getValue());
    }
    
    @Override
    public void remove(K key) {
        cache.remove(key);
        logger.fine("Removed value for key: " + key);
    }
    
    @Override
    public void clear() {
        cache.clear();
        logger.info("Cache cleared");
    }
    
    @Override
    public int size() {
        return cache.size();
    }
    
    @Override
    public boolean containsKey(K key) {
        CacheEntry<V> entry = cache.get(key);
        return entry != null && !entry.isExpired();
    }
    
    private void removeExpiredEntries() {
        int initialSize = cache.size();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        int removedCount = initialSize - cache.size();
        if (removedCount > 0) {
            logger.info("Removed " + removedCount + " expired entries");
        }
    }
    
    public void shutdown() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    private static class CacheEntry<V> {
        private final V value;
        private final long expiryTime;
        
        CacheEntry(V value) {
            this(value, -1);
        }
        
        CacheEntry(V value, long expiryTime) {
            this.value = value;
            this.expiryTime = expiryTime;
        }
        
        V getValue() {
            return value;
        }
        
        boolean isExpired() {
            return expiryTime > 0 && System.currentTimeMillis() > expiryTime;
        }
    }
} 