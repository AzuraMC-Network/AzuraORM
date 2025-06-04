package cc.azuramc.orm.util;

import cc.azuramc.orm.config.DatabaseConfig;
import cc.azuramc.orm.exception.DatabaseException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 数据库工具类，提供基于HikariCP的数据库连接管理
 * @author AzuraMC Team
 */
public class DBUtil {
    private static final Map<String, HikariDataSource> dataSources = new ConcurrentHashMap<>();
    private static String defaultConfigName = "default";
    
    /**
     * 注册数据库配置并创建连接池
     * @param name 配置名称
     * @param config 数据库配置
     */
    public static void registerConfig(String name, DatabaseConfig config) {
        if (!config.isValid()) {
            throw new DatabaseException("Invalid database configuration: " + config);
        }
        
        // 如果已存在相同名称的数据源，先关闭它
        HikariDataSource existingDataSource = dataSources.get(name);
        if (existingDataSource != null && !existingDataSource.isClosed()) {
            existingDataSource.close();
        }
        
        try {
            // 创建HikariCP配置
            HikariConfig hikariConfig = createHikariConfig(config);
            
            // 创建数据源
            HikariDataSource dataSource = new HikariDataSource(hikariConfig);
            
            // 测试连接
            try (Connection testConn = dataSource.getConnection()) {
                System.out.println("Successfully created connection pool for config: " + name);
            }
            
            dataSources.put(name, dataSource);
            System.out.println("Registered database config: " + name + " with pool: " + config.getPoolName());
            
        } catch (Exception e) {
            throw new DatabaseException("Failed to create connection pool for config: " + name, e);
        }
    }
    
    /**
     * 根据DatabaseConfig创建HikariConfig
     */
    private static HikariConfig createHikariConfig(DatabaseConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        
        // 基本连接配置
        hikariConfig.setJdbcUrl(config.getUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setDriverClassName(config.getDriverClassName());
        
        // 连接池配置
        hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize());
        hikariConfig.setMinimumIdle(config.getMinimumIdle());
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
        hikariConfig.setIdleTimeout(config.getIdleTimeout());
        hikariConfig.setMaxLifetime(config.getMaxLifetime());
        hikariConfig.setAutoCommit(config.isAutoCommit());
        
        // 连接池名称
        if (config.getPoolName() != null) {
            hikariConfig.setPoolName(config.getPoolName());
        }
        
        // 连接泄漏检测
        if (config.getLeakDetectionThreshold() > 0) {
            hikariConfig.setLeakDetectionThreshold(config.getLeakDetectionThreshold());
        }
        
        // 连接验证
        hikariConfig.setValidationTimeout(config.getValidationTimeout());
        if (config.getConnectionTestQuery() != null) {
            hikariConfig.setConnectionTestQuery(config.getConnectionTestQuery());
        }
        
        // 其他配置
        hikariConfig.setAllowPoolSuspension(config.isAllowPoolSuspension());
        hikariConfig.setReadOnly(config.isReadOnly());
        hikariConfig.setRegisterMbeans(config.isRegisterMbeans());
        
        if (config.getCatalog() != null) {
            hikariConfig.setCatalog(config.getCatalog());
        }
        if (config.getSchema() != null) {
            hikariConfig.setSchema(config.getSchema());
        }
        if (config.getTransactionIsolation() != null) {
            hikariConfig.setTransactionIsolation(config.getTransactionIsolation());
        }
        
        return hikariConfig;
    }
    
    /**
     * 注册默认数据库配置
     * @param config 数据库配置
     */
    public static void registerDefaultConfig(DatabaseConfig config) {
        registerConfig(defaultConfigName, config);
    }
    
    /**
     * 设置默认配置名称
     * @param configName 配置名称
     */
    public static void setDefaultConfigName(String configName) {
        if (!dataSources.containsKey(configName)) {
            throw new DatabaseException("Database config not found: " + configName);
        }
        defaultConfigName = configName;
    }
    
    /**
     * 获取默认数据库连接
     * @return 数据库连接
     * @throws SQLException SQL异常
     */
    public static Connection getConnection() throws SQLException {
        return getConnection(defaultConfigName);
    }
    
    /**
     * 获取指定配置的数据库连接
     * @param configName 配置名称
     * @return 数据库连接
     * @throws SQLException SQL异常
     */
    public static Connection getConnection(String configName) throws SQLException {
        HikariDataSource dataSource = dataSources.get(configName);
        if (dataSource == null) {
            throw new DatabaseException("Database config not found: " + configName);
        }
        
        if (dataSource.isClosed()) {
            throw new DatabaseException("Connection pool is closed for config: " + configName);
        }
        
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            System.err.println("Failed to get database connection for config: " + configName + ", " + e.getMessage());
            throw new DatabaseException("Failed to get database connection", e);
        }
    }
    
    /**
     * 关闭数据库连接
     * @param conn 数据库连接
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * 提交事务并关闭连接
     * @param conn 数据库连接
     */
    public static void commitAndClose(Connection conn) {
        if (conn != null) {
            try {
                conn.commit();
            } catch (SQLException e) {
                System.err.println("Error committing transaction: " + e.getMessage());
                try {
                    conn.rollback();
                    System.out.println("Transaction rolled back");
                } catch (SQLException ex) {
                    System.err.println("Error rolling back transaction: " + ex.getMessage());
                }
            } finally {
                closeConnection(conn);
            }
        }
    }
    
    /**
     * 回滚事务并关闭连接
     * @param conn 数据库连接
     */
    public static void rollbackAndClose(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
                System.out.println("Transaction rolled back");
            } catch (SQLException e) {
                System.err.println("Error rolling back transaction: " + e.getMessage());
            } finally {
                closeConnection(conn);
            }
        }
    }
    
    /**
     * 检查是否有已注册的配置
     * @param configName 配置名称
     * @return 是否存在
     */
    public static boolean hasConfig(String configName) {
        return dataSources.containsKey(configName);
    }
    
    /**
     * 获取所有已注册的配置名称
     * @return 配置名称集合
     */
    public static java.util.Set<String> getConfigNames() {
        return dataSources.keySet();
    }
    
    /**
     * 获取连接池信息
     * @param configName 配置名称
     * @return 连接池信息
     */
    public static String getPoolInfo(String configName) {
        HikariDataSource dataSource = dataSources.get(configName);
        if (dataSource == null) {
            return "Config not found: " + configName;
        }
        
        return String.format("Pool[%s] - Active: %d, Idle: %d, Total: %d, Pending: %d",
                configName,
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
    }
    
    /**
     * 关闭指定配置的连接池
     * @param configName 配置名称
     */
    public static void closePool(String configName) {
        HikariDataSource dataSource = dataSources.remove(configName);
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Closed connection pool: " + configName);
        }
    }
    
    /**
     * 关闭所有连接池
     */
    public static void closeAllPools() {
        dataSources.forEach((name, dataSource) -> {
            if (!dataSource.isClosed()) {
                dataSource.close();
                System.out.println("Closed connection pool: " + name);
            }
        });
        dataSources.clear();
        System.out.println("All connection pools closed");
    }
} 