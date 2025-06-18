package cc.azuramc.orm;

import cc.azuramc.orm.config.DatabaseConfig;
import cc.azuramc.orm.config.GlobalConfig;
import cc.azuramc.orm.util.DBUtil;

/**
 * AzuraORM 主入口类，提供便捷的SDK访问方法
 * @author AzuraMC Team
 */
public class AzuraORM {
    private static AzuraOrmClient defaultClient;
    
    /**
     * 设置AzuraORM的Debug模式
     * @param enabled 是否启用Debug模式
     */
    public static void setDebugMode(boolean enabled) {
        GlobalConfig.setDebugMode(enabled);
    }
    
    /**
     * 获取AzuraORM的Debug模式状态
     * @return 是否启用Debug模式
     */
    public static boolean isDebugMode() {
        return GlobalConfig.isDebugMode();
    }
    
    /**
     * 初始化默认的AzuraORM客户端
     * @param config 数据库配置
     */
    public static void initialize(DatabaseConfig config) {
        defaultClient = new AzuraOrmClient();
        defaultClient.initialize(config);
    }
    
    /**
     * 初始化默认的AzuraORM客户端，并自动创建数据库（如果不存在）
     * @param config 数据库配置
     * @param autoCreateDatabase 是否自动创建数据库
     */
    public static void initialize(DatabaseConfig config, boolean autoCreateDatabase) {
        defaultClient = new AzuraOrmClient();
        defaultClient.initialize(config, autoCreateDatabase);
    }
    
    /**
     * 快速初始化MySQL连接
     * @param host 主机地址
     * @param port 端口
     * @param database 数据库名
     * @param username 用户名
     * @param password 密码
     */
    public static void initializeMySQL(String host, int port, String database, String username, String password) {
        defaultClient = AzuraOrmClient.builder()
            .mysql(host, port, database, username, password)
            .build();
    }
    
    /**
     * 快速初始化MySQL连接，并自动创建数据库
     * @param host 主机地址
     * @param port 端口
     * @param database 数据库名
     * @param username 用户名
     * @param password 密码
     */
    public static void initializeMySQLWithAutoCreate(String host, int port, String database, String username, String password) {
        defaultClient = AzuraOrmClient.builder()
            .mysqlWithAutoCreate(host, port, database, username, password)
            .build();
    }
    
    /**
     * 快速初始化MySQL连接，带连接池配置
     * @param host 主机地址
     * @param port 端口
     * @param database 数据库名
     * @param username 用户名
     * @param password 密码
     * @param maxPoolSize 最大连接数
     * @param minIdle 最小空闲连接数
     */
    public static void initializeMySQL(String host, int port, String database, String username, String password, 
                                     int maxPoolSize, int minIdle) {
        defaultClient = AzuraOrmClient.builder()
            .mysql(host, port, database, username, password)
            .poolConfig(maxPoolSize, minIdle, 30000L)
            .poolName("AzuraORM-MySQL-Default")
            .build();
    }
    
    /**
     * 快速初始化MySQL连接，带连接池配置，并自动创建数据库
     * @param host 主机地址
     * @param port 端口
     * @param database 数据库名
     * @param username 用户名
     * @param password 密码
     * @param maxPoolSize 最大连接数
     * @param minIdle 最小空闲连接数
     */
    public static void initializeMySQLWithAutoCreate(String host, int port, String database, String username, String password, 
                                                   int maxPoolSize, int minIdle) {
        defaultClient = AzuraOrmClient.builder()
            .mysqlWithAutoCreate(host, port, database, username, password)
            .poolConfig(maxPoolSize, minIdle, 30000L)
            .poolName("AzuraORM-MySQL-Default")
            .build();
    }
    
    /**
     * 快速初始化H2数据库连接
     * @param filePath 数据库文件路径
     */
    public static void initializeH2(String filePath) {
        defaultClient = AzuraOrmClient.builder()
            .h2(filePath)
            .build();
    }
    
    /**
     * 快速初始化H2数据库连接，带连接池配置
     * @param filePath 数据库文件路径
     * @param maxPoolSize 最大连接数
     * @param minIdle 最小空闲连接数
     */
    public static void initializeH2(String filePath, int maxPoolSize, int minIdle) {
        defaultClient = AzuraOrmClient.builder()
            .h2(filePath)
            .poolConfig(maxPoolSize, minIdle, 30000L)
            .poolName("AzuraORM-H2-Default")
            .build();
    }
    
    /**
     * 获取默认客户端
     * @return 默认客户端实例
     */
    public static AzuraOrmClient getClient() {
        if (defaultClient == null) {
            throw new IllegalStateException("AzuraORM not initialized. Please call initialize() first.");
        }
        return defaultClient;
    }
    
    /**
     * 获取默认连接池信息
     * @return 连接池信息
     */
    public static String getPoolInfo() {
        if (defaultClient == null) {
            return "AzuraORM not initialized";
        }
        return defaultClient.getPoolInfo();
    }
    
    /**
     * 获取所有连接池信息
     * @return 所有连接池信息
     */
    public static String getAllPoolsInfo() {
        StringBuilder info = new StringBuilder("All Connection Pools:\n");
        for (String configName : DBUtil.getConfigNames()) {
            info.append("  ").append(DBUtil.getPoolInfo(configName)).append("\n");
        }
        return info.toString();
    }
    
    /**
     * 判断AzuraORM是否已初始化
     * @return 是否已初始化
     */
    public static boolean isInitialized() {
        return defaultClient != null && defaultClient.isInitialized();
    }
    
    /**
     * 关闭默认客户端
     */
    public static void shutdown() {
        if (defaultClient != null) {
            defaultClient.close();
            defaultClient = null;
        }
    }
    
    /**
     * 关闭所有连接池
     */
    public static void shutdownAll() {
        shutdown();
        DBUtil.closeAllPools();
    }
} 