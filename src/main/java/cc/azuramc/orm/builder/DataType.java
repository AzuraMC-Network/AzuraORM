package cc.azuramc.orm.builder;

import lombok.Getter;

/**
 * SQL数据类型和约束条件的枚举类
 * @author an5w1r@163.com
 */
public class DataType {

    /**
     * SQL数据类型枚举
     */
    @Getter
    public enum Type {
        // 整数类型 - 可选显示宽度
        INT("INT") {
            @Override
            public String withSize(int size) {
                return sql + "(" + size + ")";
            }
        },
        TINYINT("TINYINT") {
            @Override
            public String withSize(int size) {
                return sql + "(" + size + ")";
            }
        },
        SMALLINT("SMALLINT") {
            @Override
            public String withSize(int size) {
                return sql + "(" + size + ")";
            }
        },
        MEDIUMINT("MEDIUMINT") {
            @Override
            public String withSize(int size) {
                return sql + "(" + size + ")";
            }
        },
        BIGINT("BIGINT") {
            @Override
            public String withSize(int size) {
                return sql + "(" + size + ")";
            }
        },
        
        // 浮点数类型 - 可选精度
        FLOAT("FLOAT") {
            @Override
            public String withPrecision(int precision) {
                return sql + "(" + precision + ")";
            }
            @Override
            public String withPrecision(int precision, int scale) {
                return sql + "(" + precision + "," + scale + ")";
            }
        },
        DOUBLE("DOUBLE") {
            @Override
            public String withPrecision(int precision) {
                return sql + "(" + precision + ")";
            }
            @Override
            public String withPrecision(int precision, int scale) {
                return sql + "(" + precision + "," + scale + ")";
            }
        },
        DECIMAL("DECIMAL") {
            @Override
            public String withPrecision(int precision, int scale) {
                return sql + "(" + precision + "," + scale + ")";
            }
        },
        
        // 字符串类型 - 需要大小参数
        CHAR("CHAR") {
            @Override
            public String withSize(int size) {
                return sql + "(" + size + ")";
            }
        },
        VARCHAR("VARCHAR") {
            @Override
            public String withSize(int size) {
                return sql + "(" + size + ")";
            }
        },
        
        // 文本类型
        TEXT("TEXT"),
        TINYTEXT("TINYTEXT"),
        MEDIUMTEXT("MEDIUMTEXT"),
        LONGTEXT("LONGTEXT"),
        
        // 日期和时间类型
        DATE("DATE"),
        TIME("TIME"),
        DATETIME("DATETIME"),
        TIMESTAMP("TIMESTAMP"),
        YEAR("YEAR"),
        
        // 二进制类型
        BINARY("BINARY") {
            @Override
            public String withSize(int size) {
                return sql + "(" + size + ")";
            }
        },
        VARBINARY("VARBINARY") {
            @Override
            public String withSize(int size) {
                return sql + "(" + size + ")";
            }
        },
        
        // 二进制大对象类型 - 大小固定
        BLOB("BLOB"),
        TINYBLOB("TINYBLOB"),
        MEDIUMBLOB("MEDIUMBLOB"),
        LONGBLOB("LONGBLOB"),
        
        // 枚举和集合类型 - 需要值列表
        ENUM("ENUM") {
            @Override
            public String withValues(String... values) {
                StringBuilder sb = new StringBuilder(sql).append("(");
                for (int i = 0; i < values.length; i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append("'").append(values[i]).append("'");
                }
                sb.append(")");
                return sb.toString();
            }
        },
        SET("SET") {
            @Override
            public String withValues(String... values) {
                StringBuilder sb = new StringBuilder(sql).append("(");
                for (int i = 0; i < values.length; i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append("'").append(values[i]).append("'");
                }
                sb.append(")");
                return sb.toString();
            }
        },
        
        // 其他类型
        JSON("JSON");
        
        protected final String sql;
        
        Type(String sql) {
            this.sql = sql;
        }

        /**
         * 添加大小参数（仅适用于支持大小的类型）
         * @param size 大小参数
         * @return 带大小参数的SQL类型
         * @throws UnsupportedOperationException 如果类型不支持大小参数
         */
        public String withSize(int size) {
            throw new UnsupportedOperationException("类型 " + this.name() + " 不支持大小参数");
        }
        
        /**
         * 添加精度参数（仅适用于数值类型）
         * @param precision 精度
         * @return 带精度参数的SQL类型
         * @throws UnsupportedOperationException 如果类型不支持精度参数
         */
        public String withPrecision(int precision) {
            throw new UnsupportedOperationException("类型 " + this.name() + " 不支持单精度参数");
        }
        
        /**
         * 添加精度和小数位数（仅适用于数值类型）
         * @param precision 精度
         * @param scale 小数位数
         * @return 带精度和小数位数的SQL类型
         * @throws UnsupportedOperationException 如果类型不支持精度参数
         */
        public String withPrecision(int precision, int scale) {
            throw new UnsupportedOperationException("类型 " + this.name() + " 不支持精度和小数位数参数");
        }
        
        /**
         * 创建ENUM/SET类型的值列表
         * @param values 枚举值
         * @return 带值列表的SQL类型
         * @throws UnsupportedOperationException 如果类型不支持值列表
         */
        public String withValues(String... values) {
            throw new UnsupportedOperationException("类型 " + this.name() + " 不支持值列表参数");
        }

        // 兼容旧代码的方法（已弃用）
        
        /**
         * @deprecated 使用 {@link #withSize(int)} 替代
         */
        @Deprecated
        public String size(int size) {
            return withSize(size);
        }
        
        /**
         * @deprecated 使用 {@link #withPrecision(int, int)} 替代
         */
        @Deprecated
        public String precision(int precision, int scale) {
            return withPrecision(precision, scale);
        }
        
        /**
         * @deprecated 使用 {@link #withValues(String...)} 替代
         */
        @Deprecated
        public String values(String... values) {
            return withValues(values);
        }
    }
    
    /**
     * SQL约束条件枚举
     */
    @Getter
    public enum Constraint {
        // 基本约束
        NOT_NULL("NOT NULL"),
        NULL("NULL"),
        AUTO_INCREMENT("AUTO_INCREMENT"),
        PRIMARY_KEY("PRIMARY KEY"),
        UNIQUE("UNIQUE"),
        UNSIGNED("UNSIGNED"),
        ZEROFILL("ZEROFILL"),
        
        // 外键操作
        CASCADE("CASCADE"),
        RESTRICT("RESTRICT"),
        SET_NULL("SET NULL"),
        NO_ACTION("NO ACTION"),
        
        // 默认值约束
        DEFAULT_NULL("DEFAULT NULL"),
        DEFAULT_CURRENT_TIMESTAMP("DEFAULT CURRENT_TIMESTAMP"),
        ON_UPDATE_CURRENT_TIMESTAMP("ON UPDATE CURRENT_TIMESTAMP");
        
        private final String sql;
        
        Constraint(String sql) {
            this.sql = sql;
        }

    }
    
    /**
     * 创建DEFAULT约束
     * @param value 默认值
     * @return DEFAULT约束SQL
     */
    public static String DEFAULT(String value) {
        return "DEFAULT '" + value + "'";
    }
    
    /**
     * 创建DEFAULT约束
     * @param value 默认值
     * @return DEFAULT约束SQL
     */
    public static String DEFAULT(int value) {
        return "DEFAULT " + value;
    }
    
    /**
     * 创建DEFAULT约束
     * @param value 默认值
     * @return DEFAULT约束SQL
     */
    public static String DEFAULT(double value) {
        return "DEFAULT " + value;
    }
    
    /**
     * 创建COMMENT约束
     * @param comment 注释内容
     * @return COMMENT约束SQL
     */
    public static String COMMENT(String comment) {
        return "COMMENT '" + comment + "'";
    }
    
    // 常用组合（已更新使用新的类型安全方法）
    
    /**
     * 主键自增整数
     * @return 主键自增整数SQL
     */
    public static String PK_INT() {
        return Type.INT.getSql() + " " + Constraint.AUTO_INCREMENT.getSql() + " " + Constraint.PRIMARY_KEY.getSql();
    }
    
    /**
     * 主键自增大整数
     * @return 主键自增大整数SQL
     */
    public static String PK_BIGINT() {
        return Type.BIGINT.getSql() + " " + Constraint.AUTO_INCREMENT.getSql() + " " + Constraint.PRIMARY_KEY.getSql();
    }
    
    /**
     * 可为空的VARCHAR
     * @param size 大小
     * @return 可为空的VARCHAR SQL
     */
    public static String VARCHAR_NULL(int size) {
        return Type.VARCHAR.withSize(size) + " " + Constraint.NULL.getSql();
    }
    
    /**
     * 不可为空的VARCHAR
     * @param size 大小
     * @return 不可为空的VARCHAR SQL
     */
    public static String VARCHAR_NOT_NULL(int size) {
        return Type.VARCHAR.withSize(size) + " " + Constraint.NOT_NULL.getSql();
    }
    
    /**
     * 不可为空的INT
     * @return 不可为空的INT SQL
     */
    public static String INT_NOT_NULL() {
        return Type.INT.getSql() + " " + Constraint.NOT_NULL.getSql();
    }
    
    /**
     * 可为空的INT
     * @return 可为空的INT SQL
     */
    public static String INT_NULL() {
        return Type.INT.getSql() + " " + Constraint.NULL.getSql();
    }
    
    /**
     * 带默认当前时间戳的TIMESTAMP
     * @return 带默认当前时间戳的TIMESTAMP SQL
     */
    public static String TIMESTAMP_DEFAULT_CURRENT() {
        return Type.TIMESTAMP.getSql() + " " + Constraint.DEFAULT_CURRENT_TIMESTAMP.getSql();
    }
    
    /**
     * 带默认当前时间戳和自动更新的TIMESTAMP
     * @return 带默认当前时间戳和自动更新的TIMESTAMP SQL
     */
    public static String TIMESTAMP_DEFAULT_CURRENT_ON_UPDATE() {
        return Type.TIMESTAMP.getSql() + " " + Constraint.DEFAULT_CURRENT_TIMESTAMP.getSql() + " " + 
               Constraint.ON_UPDATE_CURRENT_TIMESTAMP.getSql();
    }
    
    // 新增的类型安全便捷方法
    
    /**
     * 不可为空的CHAR
     * @param size 大小
     * @return 不可为空的CHAR SQL
     */
    public static String CHAR_NOT_NULL(int size) {
        return Type.CHAR.withSize(size) + " " + Constraint.NOT_NULL.getSql();
    }
    
    /**
     * 可为空的CHAR
     * @param size 大小
     * @return 可为空的CHAR SQL
     */
    public static String CHAR_NULL(int size) {
        return Type.CHAR.withSize(size) + " " + Constraint.NULL.getSql();
    }
    
    /**
     * 不可为空的DECIMAL
     * @param precision 精度
     * @param scale 小数位数
     * @return 不可为空的DECIMAL SQL
     */
    public static String DECIMAL_NOT_NULL(int precision, int scale) {
        return Type.DECIMAL.withPrecision(precision, scale) + " " + Constraint.NOT_NULL.getSql();
    }
    
    /**
     * 可为空的DECIMAL
     * @param precision 精度
     * @param scale 小数位数
     * @return 可为空的DECIMAL SQL
     */
    public static String DECIMAL_NULL(int precision, int scale) {
        return Type.DECIMAL.withPrecision(precision, scale) + " " + Constraint.NULL.getSql();
    }
    
    /**
     * 不可为空的ENUM
     * @param values 枚举值
     * @return 不可为空的ENUM SQL
     */
    public static String ENUM_NOT_NULL(String... values) {
        return Type.ENUM.withValues(values) + " " + Constraint.NOT_NULL.getSql();
    }
    
    /**
     * 可为空的ENUM
     * @param values 枚举值
     * @return 可为空的ENUM SQL
     */
    public static String ENUM_NULL(String... values) {
        return Type.ENUM.withValues(values) + " " + Constraint.NULL.getSql();
    }
    
    /**
     * 不可为空的TEXT
     * @return 不可为空的TEXT SQL
     */
    public static String TEXT_NOT_NULL() {
        return Type.TEXT.getSql() + " " + Constraint.NOT_NULL.getSql();
    }
    
    /**
     * 可为空的TEXT
     * @return 可为空的TEXT SQL
     */
    public static String TEXT_NULL() {
        return Type.TEXT.getSql() + " " + Constraint.NULL.getSql();
    }
    
    /**
     * 不可为空的JSON
     * @return 不可为空的JSON SQL
     */
    public static String JSON_NOT_NULL() {
        return Type.JSON.getSql() + " " + Constraint.NOT_NULL.getSql();
    }
    
    /**
     * 可为空的JSON
     * @return 可为空的JSON SQL
     */
    public static String JSON_NULL() {
        return Type.JSON.getSql() + " " + Constraint.NULL.getSql();
    }
    
    /**
     * 带显示宽度的INT
     * @param displayWidth 显示宽度
     * @return 带显示宽度的INT SQL
     */
    public static String INT_DISPLAY_WIDTH(int displayWidth) {
        return Type.INT.withSize(displayWidth);
    }
    
    /**
     * 无符号整数
     * @return 无符号INT SQL
     */
    public static String INT_UNSIGNED() {
        return Type.INT.getSql() + " " + Constraint.UNSIGNED.getSql();
    }
    
    /**
     * 无符号大整数
     * @return 无符号BIGINT SQL
     */
    public static String BIGINT_UNSIGNED() {
        return Type.BIGINT.getSql() + " " + Constraint.UNSIGNED.getSql();
    }
} 