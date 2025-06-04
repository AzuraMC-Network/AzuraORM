package cc.azuramc.orm.example;

import cc.azuramc.orm.AzuraORM;
import cc.azuramc.orm.AzuraOrmClient;
import cc.azuramc.orm.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * HikariCP连接池使用示例
 * @author AzuraMC Team
 */
public class HikariCPExample {
    
    public static void main(String[] args) {
        try {
            // 示例1：基本的HikariCP连接池配置
            System.out.println("=== 示例1：基本连接池配置 ===");
            
            DatabaseConfig config = new DatabaseConfig()
                .setUrl("jdbc:h2:mem:testdb1")
                .setUsername("sa")
                .setPassword("")
                .setDriverClassName("org.h2.Driver")
                .setMaximumPoolSize(10)        // 最大连接数
                .setMinimumIdle(2)             // 最小空闲连接数
                .setConnectionTimeout(5000L)   // 连接超时5秒
                .setIdleTimeout(300000L)       // 空闲超时5分钟
                .setMaxLifetime(900000L)       // 连接最大生命周期15分钟
                .setPoolName("Example-Pool-1")
                .setAutoCommit(false);
            
            AzuraOrmClient client1 = new AzuraOrmClient("pool1");
            client1.initialize(config);
            
            System.out.println("连接池信息: " + client1.getPoolInfo());
            
            // 示例2：使用Builder模式配置连接池
            System.out.println("\n=== 示例2：Builder模式配置 ===");
            
            AzuraOrmClient client2 = AzuraOrmClient.builder()
                .h2("mem:testdb2")
                .configName("pool2")
                .poolConfig(15, 3, 8000L)  // 最大15个连接，最小3个空闲，8秒超时
                .poolName("Example-Pool-2")
                .leakDetection(60000L)     // 启用连接泄漏检测，60秒阈值
                .build();
            
            System.out.println("连接池信息: " + client2.getPoolInfo());
            
            // 示例3：全局配置和监控
            System.out.println("\n=== 示例3：全局配置和监控 ===");
            
            // 使用全局快速配置
            AzuraORM.initializeH2("mem:testdb3", 20, 5);
            System.out.println("默认连接池信息: " + AzuraORM.getPoolInfo());
            
            // 查看所有连接池信息
            System.out.println("\n所有连接池状态:");
            System.out.println(AzuraORM.getAllPoolsInfo());
            
            // 示例4：并发连接测试
            System.out.println("\n=== 示例4：并发连接测试 ===");
            testConcurrentConnections(client1, 5);
            
            // 查看测试后的连接池状态
            System.out.println("\n并发测试后的连接池状态:");
            System.out.println("Pool1: " + client1.getPoolInfo());
            System.out.println("Pool2: " + client2.getPoolInfo());
            System.out.println("Default: " + AzuraORM.getPoolInfo());
            
            // 示例5：高级配置
            System.out.println("\n=== 示例5：高级配置 ===");
            
            DatabaseConfig advancedConfig = new DatabaseConfig()
                .setUrl("jdbc:h2:mem:advanced")
                .setUsername("sa")
                .setPassword("")
                .setMaximumPoolSize(25)
                .setMinimumIdle(5)
                .setConnectionTimeout(10000L)
                .setValidationTimeout(3000L)
                .setLeakDetectionThreshold(30000L)  // 30秒连接泄漏检测
                .setConnectionTestQuery("SELECT 1") // 连接测试查询
                .setPoolName("Advanced-Pool")
                .setRegisterMbeans(true);           // 启用JMX监控
            
            AzuraOrmClient advancedClient = new AzuraOrmClient("advanced");
            advancedClient.initialize(advancedConfig);
            
            System.out.println("高级配置连接池信息: " + advancedClient.getPoolInfo());
            
            // 清理资源
            System.out.println("\n=== 清理资源 ===");
            client1.close();
            client2.close();
            advancedClient.close();
            AzuraORM.shutdownAll();
            
            System.out.println("所有连接池已关闭");
            
        } catch (Exception e) {
            System.err.println("示例执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 并发连接测试
     */
    private static void testConcurrentConnections(AzuraOrmClient client, int threadCount) {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Connection> connections = new ArrayList<>();
        
        System.out.println("启动 " + threadCount + " 个线程进行并发连接测试...");
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    Connection conn = client.getConnection();
                    synchronized (connections) {
                        connections.add(conn);
                    }
                    System.out.println("线程 " + threadId + " 获取连接成功");
                    
                    // 模拟一些数据库操作
                    Thread.sleep(1000);
                    
                    System.out.println("线程 " + threadId + " 完成操作");
                } catch (Exception e) {
                    System.err.println("线程 " + threadId + " 连接失败: " + e.getMessage());
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 关闭所有连接
        synchronized (connections) {
            for (Connection conn : connections) {
                try {
                    if (conn != null && !conn.isClosed()) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    System.err.println("关闭连接失败: " + e.getMessage());
                }
            }
        }
        
        System.out.println("并发测试完成，关闭了 " + connections.size() + " 个连接");
    }
} 