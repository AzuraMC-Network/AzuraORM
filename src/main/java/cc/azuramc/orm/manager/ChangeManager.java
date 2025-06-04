package cc.azuramc.orm.manager;

import lombok.Getter;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 通用的变更管理器，支持任何实体类型的批量更新和定时刷新
 * @param <T> 实体类型
 * @author AzuraMC Team
 */
public class ChangeManager<T extends ChangeManager.DirtyTracker> {
    private static final int DEFAULT_BATCH_SIZE = 3;
    private static final long DEFAULT_FLUSH_INTERVAL = 5000; // 5 seconds
    
    private final Set<T> dirtyEntities = Collections.synchronizedSet(new HashSet<>());
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Consumer<List<T>> updateFunction;
    @Getter private final int batchSize;
    @Getter private final long flushInterval;
    
    /**
     * 创建变更管理器
     * @param updateFunction 批量更新函数，接收要更新的实体列表
     */
    public ChangeManager(Consumer<List<T>> updateFunction) {
        this(updateFunction, DEFAULT_BATCH_SIZE, DEFAULT_FLUSH_INTERVAL);
    }
    
    /**
     * 创建变更管理器
     * @param updateFunction 批量更新函数，接收要更新的实体列表
     * @param batchSize 批量大小
     * @param flushInterval 刷新间隔（毫秒）
     */
    public ChangeManager(Consumer<List<T>> updateFunction, int batchSize, long flushInterval) {
        this.updateFunction = updateFunction;
        this.batchSize = batchSize;
        this.flushInterval = flushInterval;
        startScheduledFlush();
    }
    
    private void startScheduledFlush() {
        scheduler.scheduleAtFixedRate(this::flush, flushInterval, flushInterval, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 注册脏实体
     * @param entity 要注册的实体
     */
    public void registerDirty(T entity) {
        if (entity.isDirty()) {
            dirtyEntities.add(entity);
            System.out.println("Registered dirty entity: " + entity);
            
            if (dirtyEntities.size() >= batchSize) {
                flush();
            }
        }
    }
    
    /**
     * 立即刷新所有脏实体
     */
    public void flush() {
        if (dirtyEntities.isEmpty()) {
            return;
        }
        
        List<T> entitiesToUpdate = new ArrayList<>(dirtyEntities);
        System.out.println("Flushing " + entitiesToUpdate.size() + " dirty entities");
        
        try {
            updateFunction.accept(entitiesToUpdate);
            entitiesToUpdate.forEach(ChangeManager.DirtyTracker::cleanDirty);
            dirtyEntities.clear();
            System.out.println("Successfully flushed " + entitiesToUpdate.size() + " entities");
        } catch (Exception e) {
            System.err.println("Error during flush: " + e.getMessage());
            throw new RuntimeException("Failed to flush changes", e);
        }
    }
    
    /**
     * 获取当前脏实体数量
     * @return 脏实体数量
     */
    public int getDirtyCount() {
        return dirtyEntities.size();
    }

    /**
     * 关闭变更管理器
     */
    public void shutdown() {
        flush(); // 先刷新所有待处理的实体
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 脏数据跟踪接口，实体类需要实现此接口才能使用ChangeManager
     */
    public interface DirtyTracker {
        /**
         * 判断实体是否为脏数据
         * @return 是否为脏数据
         */
        boolean isDirty();
        
        /**
         * 清除脏数据标记
         */
        void cleanDirty();
    }
} 