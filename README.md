# AzuraORM SDK

AzuraORM 是一个轻量级的 Java ORM 框架，旨在简化数据库操作，提升开发效率。且集成了HikariCP高性能连接池。

## 特性
- 轻量级、易用的SDK架构
- 支持常见的CRUD操作
- 灵活的查询构建器
- 内存缓存支持
- 支持多数据库配置
- 标准化的客户端API
- **HikariCP高性能连接池**
- 连接池监控和管理
- 通用变更管理器
- **开箱即用**：内置MySQL和H2驱动

## 安装

### 基础使用（推荐）

只需要一个依赖即可开始使用：

```groovy
dependencies {
    implementation 'cc.azuramc:azura-orm:1.0.0'
}
```

**已包含的数据库支持：**
- ✅ **MySQL**（生产环境推荐）
- ✅ **H2**（开发测试推荐） 

### 额外数据库支持

如果需要使用其他数据库，添加对应驱动：

```groovy
dependencies {
    implementation 'cc.azuramc:azura-orm:1.0.0'
    
    // 可选：其他数据库驱动
    implementation 'org.postgresql:postgresql:42.6.0'        // PostgreSQL
    implementation 'org.xerial:sqlite-jdbc:3.42.0.0'         // SQLite
}
```

### 为什么这样设计？

```
应用程序 → AzuraORM → HikariCP连接池 → 数据库驱动 → 数据库
```

- **HikariCP**：负责连接池管理（连接复用、性能优化）
- **数据库驱动**：负责实际的数据库通信（JDBC实现）
- **内置常用驱动**：覆盖很多的使用场景，开箱即用
- **可选驱动**：按需添加，保持灵活性

## 快速开始

### 🚀 零配置启动（H2内存数据库）

```java
import cc.azuramc.orm.AzuraORM;

// 最简单的方式：使用H2内存数据库
AzuraORM.initializeH2("mem:testdb");

// 开始使用
AzuraOrmClient client = AzuraORM.getClient();
Connection conn = client.getConnection();

// 查看连接池状态
System.out.println(AzuraORM.getPoolInfo());
```

### 📊 生产环境（MySQL）

```java
import cc.azuramc.orm.AzuraORM;

// 生产环境MySQL配置
AzuraORM.initializeMySQL("localhost", 3306, "mydb", "user", "password", 20, 5);
// 参数：主机, 端口, 数据库, 用户名, 密码, 最大连接数, 最小空闲连接数

// 获取客户端
AzuraOrmClient client = AzuraORM.getClient();
```

### ⚙️ 高级配置（推荐）

```java
import cc.azuramc.orm.AzuraOrmClient;
import cc.azuramc.orm.config.DatabaseConfig;

// 使用构建器创建客户端，带连接池配置
AzuraOrmClient client = AzuraOrmClient.builder()
    .mysql("localhost", 3306, "testdb", "username", "password")
    .configName("myapp")
    .poolConfig(15, 3, 8000L)  // 最大15连接，最小3空闲，8秒超时
    .poolName("MyApp-Pool")
    .leakDetection(60000L)     // 启用连接泄漏检测
    .build();

// 获取连接池信息
System.out.println(client.getPoolInfo());
```

### 🔧 更进阶的配置

```java
import cc.azuramc.orm.config.DatabaseConfig;
import cc.azuramc.orm.AzuraOrmClient;

// 创建详细的HikariCP配置
DatabaseConfig config = new DatabaseConfig()
    .setUrl("jdbc:mysql://localhost:3306/mydb")
    .setUsername("user")
    .setPassword("pass")
    .setMaximumPoolSize(25)              // 最大连接数
    .setMinimumIdle(5)                   // 最小空闲连接数
    .setConnectionTimeout(10000L)        // 连接超时
    .setIdleTimeout(300000L)             // 空闲超时
    .setMaxLifetime(900000L)             // 连接最大生命周期
    .setLeakDetectionThreshold(30000L)   // 连接泄漏检测阈值
    .setConnectionTestQuery("SELECT 1")  // 连接测试查询
    .setPoolName("Production-Pool")
    .setRegisterMbeans(true)             // 启用JMX监控
    .setAutoCommit(false);

// 创建并初始化客户端
AzuraOrmClient client = new AzuraOrmClient("production");
client.initialize(config);
```

## 📝 SQL构建器使用指南

AzuraORM提供了强大的SQL构建器，支持链式调用，让SQL操作更加直观和安全。

### 1. 查询构建器 (SelectBuilder)

```java
import cc.azuramc.orm.builder.SelectBuilder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// 获取连接
try (Connection conn = client.getConnection()) {
    
    // 基础查询
    PreparedStatement stmt = client.select(conn)
        .select("id", "name", "email")  // 指定列，不指定则默认为 *
        .from("users")
        .prepare();
    
    // 带条件查询
    PreparedStatement stmt = client.select(conn)
        .select("*")
        .from("users")
        .where("age", ">", 18)          // WHERE age > 18
        .whereEquals("status", "active") // WHERE status = 'active'
        .prepare();
    
    // 复杂条件查询
    PreparedStatement stmt = client.select(conn)
        .select("id", "name", "email", "age")
        .from("users")
        .where("age", ">=", 18)
        .where("city", "=", "北京")
        .orWhere("city", "=", "上海")    // OR city = '上海'
        .orderBy("age", "DESC")         // ORDER BY age DESC
        .orderBy("name")                // ORDER BY name ASC (默认)
        .limit(10)                      // LIMIT 10
        .offset(20)                     // OFFSET 20
        .prepare();
    
    // JOIN查询
    PreparedStatement stmt = client.select(conn)
        .select("u.name", "p.title")
        .from("users u")
        .join("posts p", "u.id = p.user_id")           // INNER JOIN
        .leftJoin("comments c", "p.id = c.post_id")    // LEFT JOIN
        .where("u.status", "=", "active")
        .prepare();
    
    // GROUP BY 和 HAVING
    PreparedStatement stmt = client.select(conn)
        .select("city", "COUNT(*) as user_count")
        .from("users")
        .groupBy("city")
        .having("COUNT(*)", ">", 10)    // HAVING COUNT(*) > 10
        .orderBy("user_count", "DESC")
        .prepare();
    
    // 执行查询
    ResultSet rs = stmt.executeQuery();
    while (rs.next()) {
        System.out.println("ID: " + rs.getInt("id"));
        System.out.println("Name: " + rs.getString("name"));
    }
}
```

### 2. 插入构建器 (InsertBuilder)

```java
import cc.azuramc.orm.builder.InsertBuilder;

try (Connection conn = client.getConnection()) {
    
    // 单条插入
    PreparedStatement stmt = client.insert(conn)
        .insertInto("users")
        .values("name", "张三")
        .values("email", "zhangsan@example.com")
        .values("age", 25)
        .values("city", "北京")
        .prepare();
    
    int result = stmt.executeUpdate();
    System.out.println("插入了 " + result + " 条记录");
    
    // 获取自增ID
    PreparedStatement stmt = client.insert(conn)
        .insertInto("users")
        .values("name", "李四")
        .values("email", "lisi@example.com")
        .returnGeneratedKeys()  // 返回生成的主键
        .prepare();
    
    int result = stmt.executeUpdate();
    if (result > 0) {
        ResultSet keys = stmt.getGeneratedKeys();
        if (keys.next()) {
            long newId = keys.getLong(1);
            System.out.println("新插入记录的ID: " + newId);
        }
    }
    
    // 批量插入
    PreparedStatement stmt = client.insert(conn)
        .insertInto("users")
        .columns("name", "email", "age")  // 先定义列
        .addBatch("王五", "wangwu@example.com", 30)
        .addBatch("赵六", "zhaoliu@example.com", 28)
        .addBatch("孙七", "sunqi@example.com", 32)
        .prepare();
    
    int[] results = stmt.executeBatch();
    System.out.println("批量插入了 " + results.length + " 条记录");
}
```

### 3. 更新构建器 (UpdateBuilder)

```java
import cc.azuramc.orm.builder.UpdateBuilder;

try (Connection conn = client.getConnection()) {
    
    // 基础更新
    PreparedStatement stmt = client.update(conn)
        .update("users")
        .set("email", "newemail@example.com")
        .set("age", 26)
        .where("id", "=", 1)
        .prepare();
    
    int result = stmt.executeUpdate();
    System.out.println("更新了 " + result + " 条记录");
    
    // 复杂条件更新
    PreparedStatement stmt = client.update(conn)
        .update("users")
        .set("status", "inactive")
        .set("updated_at", "NOW()")
        .where("last_login", "<", "2023-01-01")
        .where("status", "=", "active")
        .orWhere("age", "<", 18)
        .prepare();
    
    // 批量更新
    PreparedStatement stmt = client.update(conn)
        .update("users")
        .set("city", "深圳")
        .where("city", "=", "广州")
        .prepare();
}
```

### 4. 删除构建器 (DeleteBuilder)

```java
import cc.azuramc.orm.builder.DeleteBuilder;

try (Connection conn = client.getConnection()) {
    
    // 基础删除
    PreparedStatement stmt = client.delete(conn)
        .deleteFrom("users")
        .where("id", "=", 1)
        .prepare();
    
    int result = stmt.executeUpdate();
    System.out.println("删除了 " + result + " 条记录");
    
    // 条件删除
    PreparedStatement stmt = client.delete(conn)
        .deleteFrom("users")
        .where("status", "=", "inactive")
        .where("last_login", "<", "2022-01-01")
        .prepare();
    
    // 限制删除数量
    PreparedStatement stmt = client.delete(conn)
        .deleteFrom("logs")
        .where("created_at", "<", "2023-01-01")
        .limit(1000)  // 限制删除1000条
        .prepare();
}
```

### 5. 建表构建器 (CreateTableBuilder)

AzuraORM提供了DataType工具类，让建表更加简洁和类型安全：

```java
import cc.azuramc.orm.builder.CreateTableBuilder;
import cc.azuramc.orm.builder.DataType;

try (Connection conn = client.getConnection()) {
    
    // 🔥 使用DataType - 推荐方式
    PreparedStatement stmt = client.createTable(conn)
        .createTable("users")
        .ifNotExists()
        .column("id", DataType.PK_INT())                    // 主键自增ID
        .column("name", DataType.VARCHAR_NOT_NULL(100))     // 不可为空的VARCHAR(100)
        .column("email", DataType.VARCHAR_NOT_NULL(255))    // 不可为空的VARCHAR(255)
        .column("age", DataType.INT_NULL())                 // 可为空的INT
        .column("bio", DataType.Type.TEXT.getSql())         // TEXT类型
        .column("salary", DataType.Type.DECIMAL.precision(10, 2))  // DECIMAL(10,2)
        .column("status", DataType.Type.ENUM.values("active", "inactive", "pending"))  // ENUM
        .column("created_at", DataType.TIMESTAMP_DEFAULT_CURRENT())  // 创建时间
        .column("updated_at", DataType.TIMESTAMP_DEFAULT_CURRENT_ON_UPDATE())  // 更新时间
        .prepare();
    
    // 传统方式（不推荐，容易出错）
    PreparedStatement stmt = client.createTable(conn)
        .createTable("users")
        .ifNotExists()
        .column("id", "INT", "AUTO_INCREMENT", "PRIMARY KEY")
        .column("name", "VARCHAR(100)", "NOT NULL")
        .column("email", "VARCHAR(255)", "UNIQUE", "NOT NULL")
        .column("age", "INT")
        .column("status", "VARCHAR(20)", "DEFAULT 'active'")
        .prepare();
    
    stmt.executeUpdate();
    System.out.println("表创建成功");
}
```

#### DataType常用方法：

```java
// 🎯 主键类型
DataType.PK_INT()           // INT AUTO_INCREMENT PRIMARY KEY
DataType.PK_BIGINT()        // BIGINT AUTO_INCREMENT PRIMARY KEY

// 🎯 字符串类型
DataType.VARCHAR_NOT_NULL(255)   // VARCHAR(255) NOT NULL
DataType.VARCHAR_NULL(100)       // VARCHAR(100) NULL
DataType.Type.TEXT.getSql()      // TEXT
DataType.Type.CHAR.size(10)      // CHAR(10)

// 🎯 数字类型
DataType.INT_NOT_NULL()          // INT NOT NULL
DataType.INT_NULL()              // INT NULL
DataType.Type.DECIMAL.precision(10, 2)  // DECIMAL(10,2)
DataType.Type.FLOAT.getSql()     // FLOAT
DataType.Type.DOUBLE.getSql()    // DOUBLE

// 🎯 时间类型
DataType.TIMESTAMP_DEFAULT_CURRENT()           // TIMESTAMP DEFAULT CURRENT_TIMESTAMP
DataType.TIMESTAMP_DEFAULT_CURRENT_ON_UPDATE() // TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
DataType.Type.DATE.getSql()                    // DATE
DataType.Type.DATETIME.getSql()                // DATETIME

// 🎯 枚举和集合
DataType.Type.ENUM.values("draft", "published", "archived")  // ENUM('draft','published','archived')
DataType.Type.SET.values("read", "write", "execute")         // SET('read','write','execute')

// 🎯 外键操作常量
DataType.Constraint.CASCADE.getSql()           // CASCADE
DataType.Constraint.RESTRICT.getSql()          // RESTRICT
DataType.Constraint.SET_NULL.getSql()          // SET NULL
DataType.Constraint.NO_ACTION.getSql()         // NO ACTION

// 🎯 其他约束
DataType.Constraint.NOT_NULL.getSql()          // NOT NULL
DataType.Constraint.UNIQUE.getSql()            // UNIQUE
DataType.Constraint.UNSIGNED.getSql()          // UNSIGNED
DataType.DEFAULT(值)                           // DEFAULT '值'
DataType.COMMENT("注释")                       // COMMENT '注释'

// 🎯 其他类型
DataType.Type.JSON.getSql()      // JSON
DataType.Type.BLOB.getSql()      // BLOB
```

#### 完整建表示例（正确使用DataType和CreateTableBuilder方法）：

```java
// 博客系统的posts表 - 展示所有功能
PreparedStatement stmt = client.createTable(conn)
    .createTable("posts")
    .ifNotExists()
    .column("id", DataType.PK_BIGINT())                     // 主键
    .column("user_id", DataType.INT_NOT_NULL())             // 外键列
    .column("title", DataType.VARCHAR_NOT_NULL(255))        // 标题
    .column("slug", DataType.VARCHAR_NOT_NULL(255))         // URL友好标题
    .column("content", DataType.Type.LONGTEXT.getSql())     // 内容
    .column("excerpt", DataType.VARCHAR_NULL(500))          // 摘要
    .column("status", DataType.Type.ENUM.values("draft", "published", "archived"))  // 状态
    .column("view_count", DataType.Type.INT.getSql(), 
            DataType.Constraint.UNSIGNED.getSql(),         // 无符号整数
            DataType.DEFAULT(0))                           // 默认值0
    .column("rating", DataType.Type.DECIMAL.precision(3, 2), 
            DataType.Constraint.UNSIGNED.getSql())         // 评分 0.00-9.99
    .column("tags", DataType.Type.JSON.getSql())           // 标签JSON
    .column("meta_data", DataType.Type.JSON.getSql(), 
            DataType.COMMENT("文章元数据"))                // 带注释的JSON字段
    .column("published_at", DataType.Type.TIMESTAMP.getSql(), 
            DataType.Constraint.NULL.getSql())
    .column("created_at", DataType.TIMESTAMP_DEFAULT_CURRENT())
    .column("updated_at", DataType.TIMESTAMP_DEFAULT_CURRENT_ON_UPDATE())
    
    // 使用CreateTableBuilder的专用方法
    .foreignKey("user_id", "users", "id", 
                DataType.Constraint.CASCADE.getSql(),      // ON DELETE CASCADE
                DataType.Constraint.CASCADE.getSql())      // ON UPDATE CASCADE
    .uniqueKey("uk_user_slug", "user_id", "slug")          // 唯一键：用户+slug
    .uniqueKey("uk_title", "title")                        // 标题唯一
    .index("idx_status", "status")                         // 状态索引
    .index("idx_published", "published_at")                // 发布时间索引
    .index("idx_user_status", "user_id", "status")         // 复合索引
    .engine("InnoDB")                                      // 存储引擎
    .charset("utf8mb4")                                    // 字符集
    .collate("utf8mb4_unicode_ci")                         // 排序规则
    .prepare();

// 用户表 - 展示复合主键和多种约束
PreparedStatement stmt = client.createTable(conn)
    .createTable("users")
    .ifNotExists()
    .column("id", DataType.PK_INT())
    .column("username", DataType.VARCHAR_NOT_NULL(50), 
            DataType.COMMENT("用户名"))
    .column("email", DataType.VARCHAR_NOT_NULL(255))
    .column("password_hash", DataType.Type.CHAR.size(60))   // bcrypt hash固定长度
    .column("first_name", DataType.VARCHAR_NULL(50))
    .column("last_name", DataType.VARCHAR_NULL(50))
    .column("avatar", DataType.VARCHAR_NULL(255))
    .column("bio", DataType.Type.TEXT.getSql())
    .column("balance", DataType.Type.DECIMAL.precision(15, 2), 
            DataType.Constraint.UNSIGNED.getSql(),         // 余额不能为负
            DataType.DEFAULT(0.00))
    .column("is_active", DataType.Type.TINYINT.size(1), 
            DataType.DEFAULT(1),                           // 默认激活
            DataType.COMMENT("是否激活"))
    .column("role", DataType.Type.ENUM.values("admin", "editor", "user"), 
            DataType.DEFAULT("user"))                      // 默认角色
    .column("preferences", DataType.Type.JSON.getSql())    // 用户偏好设置
    .column("last_login", DataType.Type.TIMESTAMP.getSql(), 
            DataType.Constraint.NULL.getSql())
    .addTimestamps()                                       // 快捷添加时间戳
    
    // 使用CreateTableBuilder的约束方法
    .uniqueKey("uk_username", "username")                  // 用户名唯一
    .uniqueKey("uk_email", "email")                        // 邮箱唯一
    .index("idx_role", "role")                             // 角色索引
    .index("idx_last_login", "last_login")                 // 最后登录索引
    .index("idx_active_role", "is_active", "role")         // 复合索引
    .engine("InnoDB")
    .charset("utf8mb4")
    .collate("utf8mb4_unicode_ci")
    .prepare();

// 订单表 - 展示外键约束的不同操作
PreparedStatement stmt = client.createTable(conn)
    .createTable("orders")
    .ifNotExists()
    .column("id", DataType.PK_BIGINT())
    .column("user_id", DataType.INT_NOT_NULL())
    .column("product_id", DataType.INT_NOT_NULL())
    .column("quantity", DataType.Type.INT.getSql(), 
            DataType.Constraint.UNSIGNED.getSql(),
            DataType.DEFAULT(1))
    .column("price", DataType.Type.DECIMAL.precision(10, 2), 
            DataType.Constraint.UNSIGNED.getSql(),
            DataType.Constraint.NOT_NULL.getSql())
    .column("status", DataType.Type.ENUM.values("pending", "paid", "shipped", "completed", "cancelled"))
    .addTimestamps()
    
    // 不同的外键策略
    .foreignKey("user_id", "users", "id", 
                DataType.Constraint.CASCADE.getSql(),      // 删除用户时删除订单
                DataType.Constraint.CASCADE.getSql())      // 更新用户ID时同步更新
    .foreignKey("product_id", "products", "id", 
                DataType.Constraint.RESTRICT.getSql(),     // 有订单时不能删除产品
                DataType.Constraint.CASCADE.getSql())      // 更新产品ID时同步更新
    .index("idx_user", "user_id")
    .index("idx_product", "product_id")
    .index("idx_status", "status")
    .prepare();

// 日志表 - 简单快速创建
PreparedStatement stmt = client.createTable(conn)
    .createTable("system_logs")
    .addIdColumn()                                          // 快捷添加ID
    .column("level", DataType.Type.ENUM.values("DEBUG", "INFO", "WARN", "ERROR"))
    .column("message", DataType.Type.TEXT.getSql(), 
            DataType.Constraint.NOT_NULL.getSql())
    .column("context", DataType.Type.JSON.getSql())
    .column("ip_address", DataType.Type.VARCHAR.size(45))   // IPv6最大长度
    .column("user_agent", DataType.VARCHAR_NULL(500))
    .addCreatedAtColumn()                                   // 只需要创建时间
    .index("idx_level", "level")
    .index("idx_created", "created_at")
    .prepare();

// 中间表 - 多对多关系
PreparedStatement stmt = client.createTable(conn)
    .createTable("user_roles")
    .column("user_id", DataType.INT_NOT_NULL())
    .column("role_id", DataType.INT_NOT_NULL())
    .addTimestamps()
    
    // 复合主键
    .primaryKey("user_id", "role_id")                       // 使用CreateTableBuilder的primaryKey方法
    .foreignKey("user_id", "users", "id", 
                DataType.Constraint.CASCADE.getSql(), 
                DataType.Constraint.CASCADE.getSql())
    .foreignKey("role_id", "roles", "id", 
                DataType.Constraint.CASCADE.getSql(), 
                DataType.Constraint.CASCADE.getSql())
    .prepare();
```

#### DataType vs 手写SQL对比：

```java
// ❌ 手写SQL - 容易出错，不够安全
.column("price", "DECIMAL(10,2)", "NOT NULL", "DEFAULT 0.00")
.column("status", "ENUM('active','inactive')")
.column("created_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
.foreignKey("user_id", "users", "id", "CASCADE", "CASCADE")

// ✅ 使用DataType - 类型安全，更简洁，常量化
.column("price", DataType.Type.DECIMAL.precision(10, 2), 
        DataType.Constraint.NOT_NULL.getSql(), 
        DataType.DEFAULT(0.00))
.column("status", DataType.Type.ENUM.values("active", "inactive"))
.column("created_at", DataType.TIMESTAMP_DEFAULT_CURRENT())
.foreignKey("user_id", "users", "id", 
            DataType.Constraint.CASCADE.getSql(), 
            DataType.Constraint.CASCADE.getSql())
```

#### 高级DataType用法：

```java
// 组合使用多个约束
.column("amount", 
    DataType.Type.DECIMAL.precision(15, 2),
    DataType.Constraint.NOT_NULL.getSql(),
    DataType.Constraint.UNSIGNED.getSql(),  // 无符号
    DataType.DEFAULT(0.00),
    DataType.COMMENT("金额字段"))

// 动态创建ENUM
String[] statusValues = {"pending", "processing", "completed", "failed"};
.column("order_status", DataType.Type.ENUM.values(statusValues))

// 带注释的字段
.column("user_id", 
    DataType.INT_NOT_NULL(),
    DataType.COMMENT("关联用户ID"))

// 外键的不同策略组合
.foreignKey("parent_id", "categories", "id", 
            DataType.Constraint.SET_NULL.getSql(),     // 删除父分类时设为NULL
            DataType.Constraint.CASCADE.getSql())      // 更新时级联

.foreignKey("created_by", "users", "id", 
            DataType.Constraint.RESTRICT.getSql(),     // 不能删除有创建记录的用户
            DataType.Constraint.NO_ACTION.getSql())    // 更新时不做操作
```

#### CreateTableBuilder专用方法总结：

```java
// 快捷列添加
.addIdColumn()              // 添加自增主键ID
.addCreatedAtColumn()       // 添加创建时间
.addUpdatedAtColumn()       // 添加更新时间
.addTimestamps()            // 添加创建和更新时间

// 约束管理
.primaryKey("col1", "col2")                    // 复合主键
.foreignKey("col", "ref_table", "ref_col", 
           onDelete, onUpdate)                 // 外键约束
.uniqueKey("name", "col1", "col2")            // 唯一键
.index("name", "col1", "col2")                // 索引

// 表属性
.ifNotExists()                                // IF NOT EXISTS
.engine("InnoDB")                             // 存储引擎
.charset("utf8mb4")                           // 字符集
.collate("utf8mb4_unicode_ci")                // 排序规则
```

### 6. 高级用法

```java
// SQL调试 - 查看生成的SQL
SelectBuilder selectBuilder = client.select(conn)
    .select("*")
    .from("users")
    .where("age", ">", 18);

String sql = selectBuilder.toSql();  // 获取SQL字符串用于调试
System.out.println("生成的SQL: " + sql);
PreparedStatement stmt = selectBuilder.prepare();

// 事务操作
try (Connection conn = client.getConnection()) {
    conn.setAutoCommit(false);  // 开启事务
    
    try {
        // 插入用户
        client.insert(conn)
            .insertInto("users")
            .values("name", "新用户")
            .values("email", "newuser@example.com")
            .prepare()
            .executeUpdate();
        
        // 更新统计
        client.update(conn)
            .update("statistics")
            .set("user_count", "user_count + 1")
            .where("id", "=", 1)
            .prepare()
            .executeUpdate();
        
        conn.commit();  // 提交事务
        System.out.println("事务提交成功");
        
    } catch (Exception e) {
        conn.rollback();  // 回滚事务
        System.err.println("事务回滚: " + e.getMessage());
        throw e;
    }
}

// 连接池便捷方法
try (Connection conn = client.getConnection()) {
    // 使用DBUtil的便捷方法
    try {
        // 执行操作...
        DBUtil.commitAndClose(conn);  // 提交并关闭
    } catch (Exception e) {
        DBUtil.rollbackAndClose(conn);  // 回滚并关闭
        throw e;
    }
}
```

### 变更管理器使用

```java
import cc.azuramc.orm.manager.ChangeManager;

// 创建变更管理器
ChangeManager<User> userChangeManager = client.createChangeManager(
    users -> {
        // 批量更新逻辑
        System.out.println("批量更新 " + users.size() + " 个用户");
        // userDao.updateBatch(users);
    },
    5,      // 批量大小
    3000L   // 刷新间隔（毫秒）
);

// 注册变更
user.setAge(30); // 这会标记为dirty
userChangeManager.registerDirty(user);

// 查看状态
System.out.println("脏实体数量: " + userChangeManager.getDirtyCount());
```

## 主要API

### 核心组件
- `AzuraOrmClient`：SDK主客户端，管理数据库连接和构建器
- `AzuraORM`：便捷入口类，提供静态方法快速初始化
- `DatabaseConfig`：数据库配置类，支持完整的HikariCP配置

### SQL构建器
- `SelectBuilder`：查询数据构建器，支持复杂查询、JOIN、分组等
- `InsertBuilder`：插入数据构建器，支持单条和批量插入
- `UpdateBuilder`：更新数据构建器，支持条件更新
- `DeleteBuilder`：删除数据构建器，支持条件删除和限制数量
- `CreateTableBuilder`：建表构建器，支持完整的DDL语句

### 连接池管理
- **HikariCP集成**：世界上最快的数据库连接池
- **连接池监控**：实时查看连接池状态
- **连接泄漏检测**：可配置的连接泄漏检测
- **JMX支持**：可选的JMX监控支持

### 其他功能
- `ChangeManager<T>`：通用变更管理器，支持批量更新和定时刷新
- `CacheManager`：缓存管理器，支持内存缓存

## HikariCP连接池特性

### 性能优势
- 极快的连接获取速度
- 零开销的连接池实现
- 字节码级别的优化

### 配置选项
- `maximumPoolSize`：最大连接数
- `minimumIdle`：最小空闲连接数
- `connectionTimeout`：连接超时时间
- `idleTimeout`：空闲连接超时时间
- `maxLifetime`：连接最大生命周期
- `leakDetectionThreshold`：连接泄漏检测阈值

### 监控功能
```java
// 查看单个连接池信息
String poolInfo = client.getPoolInfo();
// 输出: Pool[myapp] - Active: 2, Idle: 3, Total: 5, Pending: 0

// 查看所有连接池信息
String allPools = AzuraORM.getAllPoolsInfo();
```

## 数据库支持

### ✅ 内置支持（开箱即用）
- **MySQL** 8.0.33 - 推荐用于生产环境
- **H2** 2.2.224 - 推荐用于开发和测试

### 🔧 扩展支持（需要添加驱动）
- **PostgreSQL** 42.6.0
- **SQLite** 3.42.0.0

## 依赖

### 自动包含的依赖
- **HikariCP** 4.0.3（高性能连接池）
- **MySQL Connector/J** 8.0.33
- **H2 Database** 2.2.224
- **SLF4J API** 1.7.36
- **Logback Classic** 1.5.13

### 可选依赖（按需添加）
- PostgreSQL Driver 42.6.0
- SQLite JDBC 3.42.0.0

## 构建和发布

```bash
# 构建项目
./gradlew shadowJar

# 生成文档
./gradlew javadoc

```

## 示例项目

查看 `example` 目录下的示例：

- `QuickStart.java`：快速开始示例
- `ChangeManagerExample.java`：变更管理器使用示例
- `HikariCPExample.java`：HikariCP连接池使用示例

## 最佳实践

### 🏃‍♂️ 快速开始

**开发阶段：**
```groovy
dependencies {
    implementation 'cc.azuramc:azura-orm:1.0.0'
}
```
```java
AzuraORM.initializeH2("mem:testdb", 5, 1);
```

**生产阶段：**
```groovy
dependencies {
    implementation 'cc.azuramc:azura-orm:1.0.0'
    // MySQL驱动已内置，无需额外添加
}
```
```java
AzuraORM.initializeMySQL("prod-host", 3306, "proddb", "user", "pass", 50, 10);
```

### 🔧 多数据库项目

```groovy
dependencies {
    implementation 'cc.azuramc:azura-orm:1.0.0'
    implementation 'org.postgresql:postgresql:42.6.0'    // 额外支持PostgreSQL
}
```

### 连接池配置建议

**开发环境：**
```java
AzuraORM.initializeH2("mem:testdb", 5, 1);
```

**测试环境：**
```java
AzuraORM.initializeMySQL("test-host", 3306, "testdb", "user", "pass", 10, 2);
```

**生产环境：**
```java
DatabaseConfig config = new DatabaseConfig()
    .setUrl("jdbc:mysql://prod-host:3306/proddb")
    .setUsername("produser")
    .setPassword("prodpass")
    .setMaximumPoolSize(50)
    .setMinimumIdle(10)
    .setConnectionTimeout(5000L)
    .setLeakDetectionThreshold(60000L)
    .setRegisterMbeans(true);
```

### 资源管理

```java
// 使用 try-with-resources 确保连接正确关闭
try (Connection conn = client.getConnection()) {
    // 数据库操作
} catch (SQLException e) {
    // 异常处理
}

// 应用关闭时清理所有资源
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    AzuraORM.shutdownAll();
}));
```

## 贡献

欢迎提交 issue 和 PR！

### 贡献指南
1. Fork 本仓库
2. 新建分支 (`git checkout -b feature/xxx`)
3. 提交更改 (`git commit -am 'Add some feature'`)
4. 推送到分支 (`git push origin feature/xxx`)
5. 新建 Pull Request

## 许可证

MIT License 