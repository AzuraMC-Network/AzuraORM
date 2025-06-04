package cc.azuramc.orm.cache;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public interface Cache<K, V> {
    /**
     * 将值放入缓存
     * @param key 键
     * @param value 值
     */
    void put(K key, V value);
    
    /**
     * 将值放入缓存，并设置过期时间
     * @param key 键
     * @param value 值
     * @param duration 持续时间
     * @param unit 时间单位
     */
    void put(K key, V value, long duration, TimeUnit unit);
    
    /**
     * 从缓存中获取值
     * @param key 键
     * @return 值的Optional包装
     */
    Optional<V> get(K key);
    
    /**
     * 从缓存中移除值
     * @param key 键
     */
    void remove(K key);
    
    /**
     * 清空缓存
     */
    void clear();
    
    /**
     * 获取缓存大小
     * @return 缓存中的条目数
     */
    int size();
    
    /**
     * 检查键是否存在于缓存中
     * @param key 键
     * @return 如果键存在返回true
     */
    boolean containsKey(K key);
} 