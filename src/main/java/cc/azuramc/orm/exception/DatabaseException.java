package cc.azuramc.orm.exception;

/**
 * 数据库异常类
 * @author AzuraMC Team
 */
public class DatabaseException extends AzuraOrmException {
    
    public DatabaseException(String message) {
        super("DB_ERROR", message);
    }
    
    public DatabaseException(String message, Throwable cause) {
        super("DB_ERROR", message, cause);
    }
} 