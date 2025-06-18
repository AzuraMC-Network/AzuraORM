package cc.azuramc.orm;

import cc.azuramc.orm.builder.*;
import cc.azuramc.orm.cache.CacheManager;
import cc.azuramc.orm.config.DatabaseConfig;
import cc.azuramc.orm.config.GlobalConfig;
import cc.azuramc.orm.exception.AzuraOrmException;
import cc.azuramc.orm.exception.ConfigurationException;
import cc.azuramc.orm.manager.ChangeManager;
import cc.azuramc.orm.util.DBUtil;
import cc.azuramc.orm.util.DatabaseInitializer;

import java.util.List;
import java.util.function.Consumer;

/**
 * AzuraORM SDK客户端，提供标准化的API接口
 * @author AzuraMC Team
 */
public class AzuraOrmClient {
    
    private final String configName;
    private boolean initialized = false;
    
    /**
     * 使用默认配置创建客户端
     */
    public AzuraOrmClient() {
        this("default");
    }
    
    /**
     * 使用指定配置创建客户端
     * @param configName 配置名称
     */
    public AzuraOrmClient(String configName) {
        this.configName = configName;
    }
    
    /**
     * 设置Debug模式
     * @param enabled 是否启用Debug模式
     * @return 当前客户端实例
     */
    public AzuraOrmClient setDebugMode(boolean enabled) {
        GlobalConfig.setDebugMode(enabled);
        return this;
    }
    
    /**
     * 获取Debug模式状态
     * @return 是否启用Debug模式
     */
    public boolean isDebugMode() {
        return GlobalConfig.isDebugMode();
    }
    
    /**
     * 初始化客户端
     * @param config 数据库配置
     * @return 当前客户端实例
     */
    public AzuraOrmClient initialize(DatabaseConfig config) {
        try {
            DBUtil.registerConfig(configName, config);
            if ("default".equals(configName)) {
                DBUtil.setDefaultConfigName(configName);
            }
            initialized = true;
            return this;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ConfigurationException("Failed to initialize AzuraORM client", e);
        }
    }
    
    /**
     * 初始化客户端，并自动创建数据库（如果不存在）
     * @param config 数据库配置
     * @param autoCreateDatabase 是否自动创建数据库
     * @return 当前客户端实例
     */
    public AzuraOrmClient initialize(DatabaseConfig config, boolean autoCreateDatabase) {
        try {
            // 如果启用自动创建数据库，先尝试创建数据库
            if (autoCreateDatabase) {
                DatabaseInitializer.ensureDatabaseExists(config);
            }
            
            DBUtil.registerConfig(configName, config);
            if ("default".equals(configName)) {
                DBUtil.setDefaultConfigName(configName);
            }
            initialized = true;
            return this;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ConfigurationException("Failed to initialize AzuraORM client", e);
        }
    }
    
    /**
     * 检查客户端是否已初始化
     * @return 是否已初始化
     */
    public boolean isInitialized() {
        return initialized && DBUtil.hasConfig(configName);
    }
    
    /**
     * 确保客户端已初始化
     */
    private void ensureInitialized() {
        if (!isInitialized()) {
            throw new ConfigurationException("AzuraORM client is not initialized. Please call initialize() first.");
        }
    }
    
    /**
     * 创建插入构建器
     * @param connection 数据库连接
     * @return 插入构建器
     */
    public InsertBuilder insert(java.sql.Connection connection) {
        ensureInitialized();
        return new InsertBuilder(connection);
    }
    
    /**
     * 创建查询构建器  
     * @param connection 数据库连接
     * @return 查询构建器
     */
    public SelectBuilder select(java.sql.Connection connection) {
        ensureInitialized();
        return new SelectBuilder(connection);
    }
    
    /**
     * 创建更新构建器
     * @param connection 数据库连接
     * @return 更新构建器
     */
    public UpdateBuilder update(java.sql.Connection connection) {
        ensureInitialized();
        return new UpdateBuilder(connection);
    }
    
    /**
     * 创建删除构建器
     * @param connection 数据库连接
     * @return 删除构建器
     */
    public DeleteBuilder delete(java.sql.Connection connection) {
        ensureInitialized();
        return new DeleteBuilder(connection);
    }
    
    /**
     * 创建建表构建器
     * @param connection 数据库连接
     * @return 建表构建器
     */
    public CreateTableBuilder createTable(java.sql.Connection connection) {
        ensureInitialized();
        return new CreateTableBuilder(connection);
    }
    
    /**
     * 创建变更管理器
     * @param updateFunction 批量更新函数
     * @param <T> 实体类型
     * @return 变更管理器
     */
    public <T extends ChangeManager.DirtyTracker> ChangeManager<T> createChangeManager(Consumer<List<T>> updateFunction) {
        ensureInitialized();
        return new ChangeManager<>(updateFunction);
    }
    
    /**
     * 创建变更管理器
     * @param updateFunction 批量更新函数
     * @param batchSize 批量大小
     * @param flushInterval 刷新间隔（毫秒）
     * @param <T> 实体类型
     * @return 变更管理器
     */
    public <T extends ChangeManager.DirtyTracker> ChangeManager<T> createChangeManager(
            Consumer<List<T>> updateFunction, int batchSize, long flushInterval) {
        ensureInitialized();
        return new ChangeManager<>(updateFunction, batchSize, flushInterval);
    }
    
    /**
     * 获取数据库连接
     * @return 数据库连接
     * @throws java.sql.SQLException SQL异常
     */
    public java.sql.Connection getConnection() throws java.sql.SQLException {
        ensureInitialized();
        return DBUtil.getConnection(configName);
    }
    
    /**
     * 获取连接池信息
     * @return 连接池信息字符串
     */
    public String getPoolInfo() {
        ensureInitialized();
        return DBUtil.getPoolInfo(configName);
    }
    
    /**
     * 获取缓存管理器
     * @return 缓存管理器
     */
    public CacheManager getCacheManager() {
        return CacheManager.getInstance();
    }
    
    /**
     * 获取配置名称
     * @return 配置名称
     */
    public String getConfigName() {
        return configName;
    }
    
    /**
     * 关闭客户端，清理资源
     */
    public void close() {
        try {
            if (initialized) {
                // 关闭连接池
                DBUtil.closePool(configName);
                // 关闭缓存
                CacheManager.getInstance().shutdown();
                initialized = false;
            }
        } catch (Exception e) {
            // 记录错误但不抛出异常
            System.err.println("Error closing AzuraORM client: " + e.getMessage());
        }
    }
    
    /**
     * 创建客户端构建器
     * @return 客户端构建器
     */
    public static ClientBuilder builder() {
        return new ClientBuilder();
    }
    
    /**
     * 客户端构建器
     */
    public static class ClientBuilder {
        private DatabaseConfig config;
        private String configName = "default";
        private boolean autoCreateDatabase = false;
        
        public ClientBuilder config(DatabaseConfig config) {
            this.config = config;
            return this;
        }
        
        public ClientBuilder configName(String configName) {
            this.configName = configName;
            return this;
        }
        
        /**
         * 启用自动创建数据库
         * @return 构建器
         */
        public ClientBuilder autoCreateDatabase() {
            this.autoCreateDatabase = true;
            return this;
        }
        
        /**
         * 设置是否自动创建数据库
         * @param autoCreate 是否自动创建
         * @return 构建器
         */
        public ClientBuilder autoCreateDatabase(boolean autoCreate) {
            this.autoCreateDatabase = autoCreate;
            return this;
        }
        
        public ClientBuilder mysql(String host, int port, String database, String username, String password) {
            String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC", host, port, database);
            this.config = new DatabaseConfig(url, username, password);
            return this;
        }
        
        /**
         * 配置MySQL连接并自动创建数据库
         * @param host 主机地址
         * @param port 端口
         * @param database 数据库名
         * @param username 用户名
         * @param password 密码
         * @return 构建器
         */
        public ClientBuilder mysqlWithAutoCreate(String host, int port, String database, String username, String password) {
            return mysql(host, port, database, username, password).autoCreateDatabase();
        }
        
        public ClientBuilder h2(String filePath) {
            String url = "jdbc:h2:" + filePath;
            this.config = new DatabaseConfig(url, "sa", "");
            return this;
        }
        
        /**
         * 配置HikariCP连接池参数
         * @param maxPoolSize 最大连接数
         * @param minIdle 最小空闲连接数
         * @param connectionTimeout 连接超时时间（毫秒）
         * @return 构建器
         */
        public ClientBuilder poolConfig(int maxPoolSize, int minIdle, long connectionTimeout) {
            if (this.config == null) {
                throw new ConfigurationException("Must set database config first");
            }
            this.config.setMaximumPoolSize(maxPoolSize)
                      .setMinimumIdle(minIdle)
                      .setConnectionTimeout(connectionTimeout);
            return this;
        }
        
        /**
         * 设置连接池名称
         * @param poolName 连接池名称
         * @return 构建器
         */
        public ClientBuilder poolName(String poolName) {
            if (this.config == null) {
                throw new ConfigurationException("Must set database config first");
            }
            this.config.setPoolName(poolName);
            return this;
        }
        
        /**
         * 启用连接泄漏检测
         * @param threshold 检测阈值（毫秒）
         * @return 构建器
         */
        public ClientBuilder leakDetection(long threshold) {
            if (this.config == null) {
                throw new ConfigurationException("Must set database config first");
            }
            this.config.setLeakDetectionThreshold(threshold);
            return this;
        }
        
        public AzuraOrmClient build() {
            if (config == null) {
                throw new ConfigurationException("Database configuration is required");
            }
            
            AzuraOrmClient client = new AzuraOrmClient(configName);
            client.initialize(config, autoCreateDatabase);
            return client;
        }
    }
} 