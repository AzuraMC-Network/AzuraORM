package cc.azuramc.orm.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存管理器。
 */
public class CacheManager {
    private static final CacheManager INSTANCE = new CacheManager();
    
    private final Map<String, Cache<?, ?>> caches;
    
    private CacheManager() {
        this.caches = new ConcurrentHashMap<>();
    }
    
    /**
     * 获取缓存实例。
     * @return Cache实例
     */
    public static CacheManager getInstance() {
        return INSTANCE;
    }
    
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getCache(String name) {
        return (Cache<K, V>) caches.computeIfAbsent(name, k -> new MemoryCache<K, V>());
    }
    
    public void removeCache(String name) {
        Cache<?, ?> cache = caches.remove(name);
        if (cache instanceof MemoryCache) {
            ((MemoryCache<?, ?>) cache).shutdown();
        }
        System.out.println("Removed cache: " + name);
    }
    
    public void clearAll() {
        caches.values().forEach(Cache::clear);
        System.out.println("Cleared all caches");
    }
    
    public void shutdown() {
        caches.values().forEach(cache -> {
            if (cache instanceof MemoryCache) {
                ((MemoryCache<?, ?>) cache).shutdown();
            }
        });
        caches.clear();
        System.out.println("Shutdown all caches");
    }
} 