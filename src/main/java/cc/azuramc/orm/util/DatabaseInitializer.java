package cc.azuramc.orm.util;

import cc.azuramc.orm.config.DatabaseConfig;
import cc.azuramc.orm.exception.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据库初始化工具类，用于自动创建数据库
 * @author AzuraMC Team
 */
public class DatabaseInitializer {
    
    private static final Pattern DB_URL_PATTERN = Pattern.compile("jdbc:mysql://([^/]+)/(\\w+)(.*)");
    
    /**
     * 从完整的数据库配置中自动创建数据库（如果不存在）
     * @param config 数据库配置
     * @return 是否成功创建或数据库已存在
     */
    public static boolean ensureDatabaseExists(DatabaseConfig config) {
        if (!config.getUrl().startsWith("jdbc:mysql:")) {
            System.out.println("非MySQL数据库，跳过数据库创建检查");
            return true;
        }
        
        String databaseName = extractDatabaseName(config.getUrl());
        if (databaseName == null) {
            throw new DatabaseException("无法从URL中提取数据库名称: " + config.getUrl());
        }
        
        return createDatabaseIfNotExists(config, databaseName);
    }
    
    /**
     * 创建指定的数据库（如果不存在）
     * @param config 数据库配置
     * @param databaseName 数据库名称
     * @return 是否成功创建或数据库已存在
     */
    public static boolean createDatabaseIfNotExists(DatabaseConfig config, String databaseName) {
        // 构建不包含数据库名的连接URL
        String serverUrl = getServerUrl(config.getUrl());
        
        try {
            // 连接到MySQL服务器（不指定数据库）
            Connection conn = DriverManager.getConnection(
                serverUrl, 
                config.getUsername(), 
                config.getPassword()
            );
            
            try {
                // 创建数据库
                String createDbSql = String.format(
                    "CREATE DATABASE IF NOT EXISTS `%s` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci", 
                    databaseName
                );
                
                PreparedStatement stmt = conn.prepareStatement(createDbSql);
                stmt.executeUpdate();
                stmt.close();
                
                System.out.println("数据库 '" + databaseName + "' 创建成功或已存在");
                return true;
                
            } finally {
                conn.close();
            }
            
        } catch (SQLException e) {
            System.err.println("创建数据库失败: " + e.getMessage());
            throw new DatabaseException("创建数据库失败: " + databaseName, e);
        }
    }
    
    /**
     * 从JDBC URL中提取数据库名称
     * @param jdbcUrl JDBC URL
     * @return 数据库名称，如果提取失败返回null
     */
    public static String extractDatabaseName(String jdbcUrl) {
        Matcher matcher = DB_URL_PATTERN.matcher(jdbcUrl);
        if (matcher.matches()) {
            return matcher.group(2);
        }
        return null;
    }
    
    /**
     * 从JDBC URL中获取服务器URL（不包含数据库名）
     * @param jdbcUrl 完整的JDBC URL
     * @return 服务器URL
     */
    public static String getServerUrl(String jdbcUrl) {
        Matcher matcher = DB_URL_PATTERN.matcher(jdbcUrl);
        if (matcher.matches()) {
            String hostPort = matcher.group(1);
            String params = matcher.group(3);
            return "jdbc:mysql://" + hostPort + "/" + params;
        }
        
        // 如果匹配失败，尝试简单的字符串替换
        int lastSlashIndex = jdbcUrl.lastIndexOf('/');
        if (lastSlashIndex > 0) {
            int questionMarkIndex = jdbcUrl.indexOf('?', lastSlashIndex);
            if (questionMarkIndex > 0) {
                return jdbcUrl.substring(0, lastSlashIndex + 1) + jdbcUrl.substring(questionMarkIndex);
            } else {
                return jdbcUrl.substring(0, lastSlashIndex + 1);
            }
        }
        
        throw new DatabaseException("无法解析JDBC URL: " + jdbcUrl);
    }
    
    /**
     * 快速方法：基于连接参数创建数据库
     * @param host 主机地址
     * @param port 端口
     * @param databaseName 数据库名称
     * @param username 用户名
     * @param password 密码
     * @return 是否成功创建
     */
    public static boolean createDatabase(String host, int port, String databaseName, String username, String password) {
        String serverUrl = String.format("jdbc:mysql://%s:%d/?useSSL=false&serverTimezone=UTC", host, port);
        
        try {
            Connection conn = DriverManager.getConnection(serverUrl, username, password);
            
            try {
                String createDbSql = String.format(
                    "CREATE DATABASE IF NOT EXISTS `%s` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci", 
                    databaseName
                );
                
                PreparedStatement stmt = conn.prepareStatement(createDbSql);
                stmt.executeUpdate();
                stmt.close();
                
                System.out.println("数据库 '" + databaseName + "' 创建成功");
                return true;
                
            } finally {
                conn.close();
            }
            
        } catch (SQLException e) {
            System.err.println("创建数据库失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 删除数据库（谨慎使用！）
     * @param config 数据库配置
     * @param databaseName 数据库名称
     * @return 是否成功删除
     */
    public static boolean dropDatabase(DatabaseConfig config, String databaseName) {
        String serverUrl = getServerUrl(config.getUrl());
        
        try {
            Connection conn = DriverManager.getConnection(
                serverUrl, 
                config.getUsername(), 
                config.getPassword()
            );
            
            try {
                String dropDbSql = String.format("DROP DATABASE IF EXISTS `%s`", databaseName);
                PreparedStatement stmt = conn.prepareStatement(dropDbSql);
                stmt.executeUpdate();
                stmt.close();
                
                System.out.println("数据库 '" + databaseName + "' 删除成功");
                return true;
                
            } finally {
                conn.close();
            }
            
        } catch (SQLException e) {
            System.err.println("删除数据库失败: " + e.getMessage());
            return false;
        }
    }
} 