package cc.azuramc.orm.example;

import cc.azuramc.orm.AzuraORM;
import cc.azuramc.orm.AzuraOrmClient;
import cc.azuramc.orm.builder.*;
import cc.azuramc.orm.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * AzuraORM SDK 快速开始示例
 * 展示完整的数据库操作流程：启动、建表、增删改查、事务、关闭
 * @author AzuraMC Team
 */
public class QuickStart {
    
    public static void main(String[] args) {
        AzuraOrmClient client = null;
        
        try {
            // 步骤1：启动AzuraORM
            System.out.println("=== 步骤1：启动AzuraORM ===");
            AzuraORM.initializeH2("mem:quickstart", 10, 2);
            client = AzuraORM.getClient();
            System.out.println("AzuraORM已启动，连接池状态: " + AzuraORM.getPoolInfo());
            
            // 步骤2：创建表格
            System.out.println("\n=== 步骤2：创建表格 ===");
            createTables(client);
            
            // 步骤3：插入数据
            System.out.println("\n=== 步骤3：插入数据 ===");
            insertData(client);
            
            // 步骤4：查询数据
            System.out.println("\n=== 步骤4：查询数据 ===");
            queryData(client);
            
            // 步骤5：更新数据
            System.out.println("\n=== 步骤5：更新数据 ===");
            updateData(client);
            
            // 步骤6：删除数据
            System.out.println("\n=== 步骤6：删除数据 ===");
            deleteData(client);
            
            // 步骤7：事务处理（延迟提交）
            System.out.println("\n=== 步骤7：事务处理（延迟提交）===");
            transactionDemo(client);
            
            // 步骤8：查看最终结果
            System.out.println("\n=== 步骤8：查看最终结果 ===");
            finalQuery(client);
            
        } catch (Exception e) {
            System.err.println("执行失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 步骤9：安全关闭处理
            System.out.println("\n=== 步骤9：安全关闭处理 ===");
            safeShutdown();
        }
    }
    
    /**
     * 创建表格
     */
    private static void createTables(AzuraOrmClient client) throws SQLException {
        try (Connection conn = client.getConnection()) {
            
            // 创建用户表 - 使用DataType的所有便利方法
            PreparedStatement createUsersStmt = client.createTable(conn)
                .createTable("users")
                .ifNotExists()
                .addIdColumn()  // 使用便利方法添加主键ID
                .column("name", DataType.VARCHAR_NOT_NULL(100))
                .column("email", DataType.VARCHAR_NOT_NULL(255))
                .column("age", DataType.INT_NULL())
                .column("status", DataType.Type.ENUM.values("active", "inactive"), 
                        DataType.DEFAULT("active"))
                .column("balance", DataType.Type.DECIMAL.precision(10, 2), 
                        DataType.Constraint.UNSIGNED.getSql(),
                        DataType.DEFAULT(0.00))
                .column("is_vip", DataType.Type.TINYINT.size(1), 
                        DataType.DEFAULT(0),
                        DataType.COMMENT("是否VIP用户"))
                .addTimestamps()  // 使用便利方法添加时间戳
                .uniqueKey("uk_email", "email")
                .index("idx_status", "status")
                .index("idx_age", "age")
                .engine("InnoDB")
                .charset("utf8mb4")
                .collate("utf8mb4_unicode_ci")
                .prepare();
            
            createUsersStmt.executeUpdate();
            System.out.println("用户表创建成功");
            
            // 创建订单表 - 展示外键和更多DataType功能
            PreparedStatement createOrdersStmt = client.createTable(conn)
                .createTable("orders")
                .ifNotExists()
                .column("id", DataType.PK_BIGINT())  // 使用便利方法
                .column("user_id", DataType.INT_NOT_NULL())
                .column("product_name", DataType.VARCHAR_NOT_NULL(255))
                .column("product_category", DataType.Type.ENUM.values("electronics", "books", "clothing", "food"))
                .column("quantity", DataType.Type.INT.getSql(), 
                        DataType.Constraint.UNSIGNED.getSql(),
                        DataType.DEFAULT(1))
                .column("unit_price", DataType.Type.DECIMAL.precision(10, 2), 
                        DataType.Constraint.UNSIGNED.getSql(),
                        DataType.Constraint.NOT_NULL.getSql())
                .column("total_amount", DataType.Type.DECIMAL.precision(12, 2), 
                        DataType.Constraint.UNSIGNED.getSql(),
                        DataType.Constraint.NOT_NULL.getSql())
                .column("order_status", DataType.Type.ENUM.values("pending", "paid", "shipped", "completed", "cancelled"), 
                        DataType.DEFAULT("pending"))
                .column("notes", DataType.Type.TEXT.getSql(), 
                        DataType.Constraint.NULL.getSql())
                .addCreatedAtColumn()  // 只添加创建时间
                .column("updated_at", DataType.TIMESTAMP_DEFAULT_CURRENT_ON_UPDATE())  // 使用DataType便利方法
                .foreignKey("user_id", "users", "id", 
                           DataType.Constraint.CASCADE.getSql(), 
                           DataType.Constraint.CASCADE.getSql())
                .index("idx_user_id", "user_id")
                .index("idx_status", "order_status")
                .index("idx_category", "product_category")
                .engine("InnoDB")
                .charset("utf8mb4")
                .collate("utf8mb4_unicode_ci")
                .prepare();
            
            createOrdersStmt.executeUpdate();
            System.out.println("订单表创建成功");
            
            // 创建用户角色关联表 - 展示复合主键
            PreparedStatement createUserRolesStmt = client.createTable(conn)
                .createTable("user_roles")
                .ifNotExists()
                .column("user_id", DataType.INT_NOT_NULL())
                .column("role_name", DataType.Type.ENUM.values("admin", "user", "guest"), 
                        DataType.Constraint.NOT_NULL.getSql())
                .column("assigned_by", DataType.INT_NULL())
                .addTimestamps()
                .primaryKey("user_id", "role_name")  // 使用builder的复合主键方法
                .foreignKey("user_id", "users", "id", 
                           DataType.Constraint.CASCADE.getSql(), 
                           DataType.Constraint.CASCADE.getSql())
                .foreignKey("assigned_by", "users", "id", 
                           DataType.Constraint.SET_NULL.getSql(), 
                           DataType.Constraint.CASCADE.getSql())
                .index("idx_role", "role_name")
                .prepare();
            
            createUserRolesStmt.executeUpdate();
            System.out.println("用户角色表创建成功");
            
            // 提交建表操作
            DBUtil.commitAndClose(conn);
        }
    }
    
    /**
     * 插入数据
     */
    private static void insertData(AzuraOrmClient client) throws SQLException {
        try (Connection conn = client.getConnection()) {
            
            // 插入用户数据 - 获取自增ID
            PreparedStatement insertUserStmt = client.insert(conn)
                .insertInto("users")
                .values("name", "张三")
                .values("email", "zhangsan@example.com")
                .values("age", 25)
                .values("balance", 1000.50)
                .values("is_vip", 1)
                .returnGeneratedKeys()
                .prepare();
            
            int userResult = insertUserStmt.executeUpdate();
            long userId1 = 0;
            if (userResult > 0) {
                ResultSet keys = insertUserStmt.getGeneratedKeys();
                if (keys.next()) {
                    userId1 = keys.getLong(1);
                    System.out.println("用户张三插入成功，ID: " + userId1);
                }
            }
            
            // 批量插入用户
            PreparedStatement batchInsertStmt = client.insert(conn)
                .insertInto("users")
                .columns("name", "email", "age", "balance", "is_vip")
                .addBatch("李四", "lisi@example.com", 30, 2000.00, 0)
                .addBatch("王五", "wangwu@example.com", 28, 1500.75, 1)
                .addBatch("赵六", "zhaoliu@example.com", 35, 3000.25, 1)
                .addBatch("钱七", "qianqi@example.com", 22, 500.00, 0)
                .prepare();
            
            int[] batchResults = batchInsertStmt.executeBatch();
            System.out.println("批量插入了 " + batchResults.length + " 个用户");
            
            // 插入订单数据
            PreparedStatement insertOrderStmt = client.insert(conn)
                .insertInto("orders")
                .values("user_id", userId1)
                .values("product_name", "MacBook Pro")
                .values("product_category", "electronics")
                .values("quantity", 1)
                .values("unit_price", 12999.00)
                .values("total_amount", 12999.00)
                .values("notes", "16寸 M3 Pro版本")
                .prepare();
            
            int orderResult = insertOrderStmt.executeUpdate();
            System.out.println("订单插入成功，影响行数: " + orderResult);
            
            // 插入用户角色关联
            PreparedStatement insertRoleStmt = client.insert(conn)
                .insertInto("user_roles")
                .values("user_id", userId1)
                .values("role_name", "admin")
                .values("assigned_by", userId1)
                .prepare();
            
            insertRoleStmt.executeUpdate();
            System.out.println("用户角色关联插入成功");
            
            DBUtil.commitAndClose(conn);
        }
    }
    
    /**
     * 查询数据
     */
    private static void queryData(AzuraOrmClient client) throws SQLException {
        try (Connection conn = client.getConnection()) {
            
            // 简单查询 - 使用whereEquals便利方法
            PreparedStatement selectStmt = client.select(conn)
                .select("id", "name", "email", "age", "balance", "is_vip")
                .from("users")
                .where("age", ">=", 25)
                .whereEquals("status", "active")  // 使用便利方法
                .orderBy("balance", "DESC")
                .limit(10)
                .prepare();
            
            ResultSet rs = selectStmt.executeQuery();
            System.out.println("年龄>=25且状态为active的用户（按余额降序）:");
            while (rs.next()) {
                System.out.printf("  ID: %d, 姓名: %s, 邮箱: %s, 年龄: %d, 余额: %.2f, VIP: %s%n",
                    rs.getInt("id"), rs.getString("name"), rs.getString("email"), 
                    rs.getInt("age"), rs.getDouble("balance"),
                    rs.getInt("is_vip") == 1 ? "是" : "否");
            }
            
            // JOIN查询
            PreparedStatement joinStmt = client.select(conn)
                .select("u.name", "u.email", "o.product_name", "o.product_category", "o.total_amount", "o.order_status")
                .from("users u")
                .join("orders o", "u.id = o.user_id")  // 使用builder的join方法
                .where("o.total_amount", ">", 1000)
                .orderBy("o.total_amount", "DESC")
                .prepare();
            
            ResultSet joinRs = joinStmt.executeQuery();
            System.out.println("\n订单金额>1000的用户订单:");
            while (joinRs.next()) {
                System.out.printf("  用户: %s (%s), 产品: %s [%s], 金额: %.2f, 状态: %s%n",
                    joinRs.getString("name"), joinRs.getString("email"),
                    joinRs.getString("product_name"), joinRs.getString("product_category"),
                    joinRs.getDouble("total_amount"), joinRs.getString("order_status"));
            }
            
            // 聚合查询 - 使用groupBy和having
            PreparedStatement groupStmt = client.select(conn)
                .select("is_vip", "COUNT(*) as user_count", "AVG(age) as avg_age", "SUM(balance) as total_balance")
                .from("users")
                .groupBy("is_vip")
                .having("COUNT(*)", ">", 0)  // 使用builder的having方法
                .orderBy("is_vip")
                .prepare();
            
            ResultSet groupRs = groupStmt.executeQuery();
            System.out.println("\nVIP用户统计:");
            while (groupRs.next()) {
                System.out.printf("  %s用户: 总数=%d, 平均年龄=%.1f, 总余额=%.2f%n",
                    groupRs.getInt("is_vip") == 1 ? "VIP" : "普通",
                    groupRs.getInt("user_count"), 
                    groupRs.getDouble("avg_age"), 
                    groupRs.getDouble("total_balance"));
            }
            
            // 左连接查询 - 使用leftJoin便利方法
            PreparedStatement leftJoinStmt = client.select(conn)
                .select("u.name", "ur.role_name")
                .from("users u")
                .leftJoin("user_roles ur", "u.id = ur.user_id")  // 使用便利方法
                .orderBy("u.name")
                .prepare();
            
            ResultSet leftJoinRs = leftJoinStmt.executeQuery();
            System.out.println("\n用户角色信息:");
            while (leftJoinRs.next()) {
                String roleName = leftJoinRs.getString("role_name");
                System.out.printf("  用户: %s, 角色: %s%n",
                    leftJoinRs.getString("name"), 
                    roleName != null ? roleName : "无角色");
            }
        }
    }
    
    /**
     * 更新数据
     */
    private static void updateData(AzuraOrmClient client) throws SQLException {
        try (Connection conn = client.getConnection()) {
            
            // 单条更新 - 使用whereEquals便利方法
            PreparedStatement updateStmt = client.update(conn)
                .update("users")
                .set("age", 26)
                .set("balance", 1200.50)
                .whereEquals("name", "张三")  // 使用便利方法
                .prepare();
            
            int updateResult = updateStmt.executeUpdate();
            System.out.println("更新张三的信息，影响行数: " + updateResult);
            
            // 批量更新 - 使用orWhere便利方法
            PreparedStatement batchUpdateStmt = client.update(conn)
                .update("users")
                .set("status", "active")
                .where("age", ">", 30)
                .orWhereEquals("is_vip", 1)  // 使用便利方法
                .prepare();
            
            int batchUpdateResult = batchUpdateStmt.executeUpdate();
            System.out.println("批量更新年龄>30或VIP用户状态，影响行数: " + batchUpdateResult);
            
            // 更新订单状态
            PreparedStatement orderUpdateStmt = client.update(conn)
                .update("orders")
                .set("order_status", "paid")
                .set("notes", "支付完成")
                .whereEquals("order_status", "pending")  // 使用便利方法
                .prepare();
            
            int orderUpdateResult = orderUpdateStmt.executeUpdate();
            System.out.println("更新订单状态为已支付，影响行数: " + orderUpdateResult);
            
            DBUtil.commitAndClose(conn);
        }
    }
    
    /**
     * 删除数据
     */
    private static void deleteData(AzuraOrmClient client) throws SQLException {
        try (Connection conn = client.getConnection()) {
            
            // 条件删除 - 使用whereEquals便利方法
            PreparedStatement deleteStmt = client.delete(conn)
                .deleteFrom("users")
                .where("balance", "<", 600)
                .whereEquals("is_vip", 0)  // 使用便利方法
                .limit(1)  // 限制只删除1条
                .prepare();
            
            int deleteResult = deleteStmt.executeUpdate();
            System.out.println("删除余额<600且非VIP的用户，影响行数: " + deleteResult);
            
            // 删除用户角色关联
            PreparedStatement deleteRoleStmt = client.delete(conn)
                .deleteFrom("user_roles")
                .whereEquals("role_name", "guest")  // 使用便利方法
                .prepare();
            
            int deleteRoleResult = deleteRoleStmt.executeUpdate();
            System.out.println("删除guest角色关联，影响行数: " + deleteRoleResult);
            
            DBUtil.commitAndClose(conn);
        }
    }
    
    /**
     * 事务处理演示（延迟提交）
     */
    private static void transactionDemo(AzuraOrmClient client) throws SQLException {
        Connection conn = null;
        try {
            conn = client.getConnection();
            conn.setAutoCommit(false);  // 开启事务
            System.out.println("开启事务模式");
            
            // 事务操作1：插入新用户
            PreparedStatement insertStmt = client.insert(conn)
                .insertInto("users")
                .values("name", "事务用户")
                .values("email", "transaction@example.com")
                .values("age", 40)
                .values("balance", 5000.00)
                .values("is_vip", 1)
                .returnGeneratedKeys()
                .prepare();
            
            int insertResult = insertStmt.executeUpdate();
            long newUserId = 0;
            if (insertResult > 0) {
                ResultSet keys = insertStmt.getGeneratedKeys();
                if (keys.next()) {
                    newUserId = keys.getLong(1);
                    System.out.println("事务中插入用户，ID: " + newUserId);
                }
            }
            
            // 事务操作2：为新用户分配角色
            PreparedStatement roleStmt = client.insert(conn)
                .insertInto("user_roles")
                .values("user_id", newUserId)
                .values("role_name", "user")
                .values("assigned_by", newUserId)
                .prepare();
            
            roleStmt.executeUpdate();
            System.out.println("事务中分配用户角色");
            
            // 事务操作3：为新用户创建订单
            PreparedStatement orderStmt = client.insert(conn)
                .insertInto("orders")
                .values("user_id", newUserId)
                .values("product_name", "iPhone 15 Pro")
                .values("product_category", "electronics")
                .values("quantity", 2)
                .values("unit_price", 8999.00)
                .values("total_amount", 17998.00)
                .values("notes", "事务测试订单")
                .prepare();
            
            int orderResult = orderStmt.executeUpdate();
            System.out.println("事务中插入订单，影响行数: " + orderResult);
            
            // 事务操作4：更新用户余额（模拟扣款）
            PreparedStatement balanceStmt = client.update(conn)
                .update("users")
                .set("balance", 1.00)  // 余额扣除到只剩1元
                .whereEquals("id", newUserId)  // 使用便利方法
                .prepare();
            
            int balanceResult = balanceStmt.executeUpdate();
            System.out.println("事务中更新余额，影响行数: " + balanceResult);
            
            // 模拟延迟处理
            System.out.println("模拟延迟处理（2秒）...");
            Thread.sleep(2000);
            
            // 提交事务
            conn.commit();
            System.out.println("事务提交成功！所有操作已持久化");
            
        } catch (Exception e) {
            System.err.println("事务执行失败: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("事务已回滚");
                } catch (SQLException rollbackEx) {
                    System.err.println("回滚失败: " + rollbackEx.getMessage());
                }
            }
            throw new SQLException("事务处理失败", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);  // 恢复自动提交
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("关闭连接失败: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * 最终查询
     */
    private static void finalQuery(AzuraOrmClient client) throws SQLException {
        try (Connection conn = client.getConnection()) {
            // 使用聚合查询展示最终结果
            PreparedStatement finalStmt = client.select(conn)
                .select("COUNT(*) as total_users", 
                        "SUM(balance) as total_balance",
                        "AVG(age) as avg_age")
                .from("users")
                .prepare();
            
            ResultSet rs = finalStmt.executeQuery();
            if (rs.next()) {
                System.out.printf("最终统计 - 用户总数: %d, 总余额: %.2f, 平均年龄: %.1f%n",
                    rs.getInt("total_users"),
                    rs.getDouble("total_balance"),
                    rs.getDouble("avg_age"));
            }
            
            // 查询订单统计
            PreparedStatement orderStmt = client.select(conn)
                .select("COUNT(*) as total_orders", "SUM(total_amount) as total_sales")
                .from("orders")
                .prepare();
            
            ResultSet orderRs = orderStmt.executeQuery();
            if (orderRs.next()) {
                System.out.printf("订单统计 - 总订单数: %d, 总销售额: %.2f%n",
                    orderRs.getInt("total_orders"),
                    orderRs.getDouble("total_sales"));
            }
        }
    }
    
    /**
     * 安全关闭处理
     */
    private static void safeShutdown() {
        try {
            // 添加关闭钩子，确保程序异常退出时也能正确清理资源
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("执行关闭钩子，清理资源...");
                AzuraORM.shutdownAll();
            }));
            
            // 正常关闭
            System.out.println("正在安全关闭AzuraORM...");
            AzuraORM.shutdownAll();
            System.out.println("AzuraORM已安全关闭");
            System.out.println("QuickStart示例执行完毕！");
            
        } catch (Exception e) {
            System.err.println("关闭过程中发生错误: " + e.getMessage());
        }
    }
} 