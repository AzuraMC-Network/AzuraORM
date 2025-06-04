package cc.azuramc.orm.exception;

/**
 * AzuraORM基础异常类
 * @author AzuraMC Team
 */
public class AzuraOrmException extends RuntimeException {
    
    private final String errorCode;
    
    public AzuraOrmException(String message) {
        super(message);
        this.errorCode = "UNKNOWN";
    }
    
    public AzuraOrmException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "UNKNOWN";
    }
    
    public AzuraOrmException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public AzuraOrmException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    @Override
    public String toString() {
        return "AzuraOrmException{" +
                "errorCode='" + errorCode + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
} 