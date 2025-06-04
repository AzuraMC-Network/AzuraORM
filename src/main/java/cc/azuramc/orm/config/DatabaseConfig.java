package cc.azuramc.orm.config;

/**
 * 数据库配置类，支持HikariCP连接池配置
 * @author AzuraMC Team
 */
public class DatabaseConfig {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    
    // HikariCP连接池配置
    private int maximumPoolSize = 10;
    private int minimumIdle = 1;
    private long connectionTimeout = 30000L; // 30秒
    private long idleTimeout = 600000L; // 10分钟
    private long maxLifetime = 1800000L; // 30分钟
    private long leakDetectionThreshold = 0L; // 禁用连接泄漏检测
    private boolean autoCommit = false;
    private String poolName;
    
    // 数据库连接参数
    private long validationTimeout = 5000L;
    private String connectionTestQuery;
    private boolean allowPoolSuspension = false;
    private boolean readOnly = false;
    private boolean registerMbeans = false;
    private String catalog;
    private String schema;
    private String transactionIsolation;
    
    public DatabaseConfig() {}
    
    public DatabaseConfig(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.driverClassName = detectDriverClass(url);
        this.poolName = "AzuraORM-" + System.currentTimeMillis();
    }
    
    public DatabaseConfig(String url, String username, String password, String driverClassName) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.driverClassName = driverClassName;
        this.poolName = "AzuraORM-" + System.currentTimeMillis();
    }
    
    private String detectDriverClass(String url) {
        if (url.startsWith("jdbc:mysql:")) {
            return "com.mysql.cj.jdbc.Driver";
        } else if (url.startsWith("jdbc:h2:")) {
            return "org.h2.Driver";
        } else if (url.startsWith("jdbc:postgresql:")) {
            return "org.postgresql.Driver";
        } else if (url.startsWith("jdbc:sqlite:")) {
            return "org.sqlite.JDBC";
        }
        return null;
    }
    
    // 基本配置的 Getters and Setters
    public String getUrl() {
        return url;
    }
    
    public DatabaseConfig setUrl(String url) {
        this.url = url;
        if (this.driverClassName == null) {
            this.driverClassName = detectDriverClass(url);
        }
        return this;
    }
    
    public String getUsername() {
        return username;
    }
    
    public DatabaseConfig setUsername(String username) {
        this.username = username;
        return this;
    }
    
    public String getPassword() {
        return password;
    }
    
    public DatabaseConfig setPassword(String password) {
        this.password = password;
        return this;
    }
    
    public String getDriverClassName() {
        return driverClassName;
    }
    
    public DatabaseConfig setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
        return this;
    }
    
    // HikariCP配置的 Getters and Setters
    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }
    
    public DatabaseConfig setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
        return this;
    }
    
    public int getMinimumIdle() {
        return minimumIdle;
    }
    
    public DatabaseConfig setMinimumIdle(int minimumIdle) {
        this.minimumIdle = minimumIdle;
        return this;
    }
    
    public long getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public DatabaseConfig setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }
    
    public long getIdleTimeout() {
        return idleTimeout;
    }
    
    public DatabaseConfig setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
        return this;
    }
    
    public long getMaxLifetime() {
        return maxLifetime;
    }
    
    public DatabaseConfig setMaxLifetime(long maxLifetime) {
        this.maxLifetime = maxLifetime;
        return this;
    }
    
    public long getLeakDetectionThreshold() {
        return leakDetectionThreshold;
    }
    
    public DatabaseConfig setLeakDetectionThreshold(long leakDetectionThreshold) {
        this.leakDetectionThreshold = leakDetectionThreshold;
        return this;
    }
    
    public boolean isAutoCommit() {
        return autoCommit;
    }
    
    public DatabaseConfig setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
        return this;
    }
    
    public String getPoolName() {
        return poolName;
    }
    
    public DatabaseConfig setPoolName(String poolName) {
        this.poolName = poolName;
        return this;
    }
    
    public long getValidationTimeout() {
        return validationTimeout;
    }
    
    public DatabaseConfig setValidationTimeout(long validationTimeout) {
        this.validationTimeout = validationTimeout;
        return this;
    }
    
    public String getConnectionTestQuery() {
        return connectionTestQuery;
    }
    
    public DatabaseConfig setConnectionTestQuery(String connectionTestQuery) {
        this.connectionTestQuery = connectionTestQuery;
        return this;
    }
    
    public boolean isAllowPoolSuspension() {
        return allowPoolSuspension;
    }
    
    public DatabaseConfig setAllowPoolSuspension(boolean allowPoolSuspension) {
        this.allowPoolSuspension = allowPoolSuspension;
        return this;
    }
    
    public boolean isReadOnly() {
        return readOnly;
    }
    
    public DatabaseConfig setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }
    
    public boolean isRegisterMbeans() {
        return registerMbeans;
    }
    
    public DatabaseConfig setRegisterMbeans(boolean registerMbeans) {
        this.registerMbeans = registerMbeans;
        return this;
    }
    
    public String getCatalog() {
        return catalog;
    }
    
    public DatabaseConfig setCatalog(String catalog) {
        this.catalog = catalog;
        return this;
    }
    
    public String getSchema() {
        return schema;
    }
    
    public DatabaseConfig setSchema(String schema) {
        this.schema = schema;
        return this;
    }
    
    public String getTransactionIsolation() {
        return transactionIsolation;
    }
    
    public DatabaseConfig setTransactionIsolation(String transactionIsolation) {
        this.transactionIsolation = transactionIsolation;
        return this;
    }
    
    // 便捷方法，保持向后兼容
    public int getMaxPoolSize() {
        return maximumPoolSize;
    }
    
    public DatabaseConfig setMaxPoolSize(int maxPoolSize) {
        this.maximumPoolSize = maxPoolSize;
        return this;
    }
    
    public int getMinPoolSize() {
        return minimumIdle;
    }
    
    public DatabaseConfig setMinPoolSize(int minPoolSize) {
        this.minimumIdle = minPoolSize;
        return this;
    }
    
    /**
     * 验证配置是否有效
     * @return 是否有效
     */
    public boolean isValid() {
        return url != null && !url.trim().isEmpty() 
            && username != null && !username.trim().isEmpty()
            && password != null
            && driverClassName != null && !driverClassName.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return "DatabaseConfig{" +
                "url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", driverClassName='" + driverClassName + '\'' +
                ", maximumPoolSize=" + maximumPoolSize +
                ", minimumIdle=" + minimumIdle +
                ", connectionTimeout=" + connectionTimeout +
                ", idleTimeout=" + idleTimeout +
                ", autoCommit=" + autoCommit +
                ", poolName='" + poolName + '\'' +
                '}';
    }
} 