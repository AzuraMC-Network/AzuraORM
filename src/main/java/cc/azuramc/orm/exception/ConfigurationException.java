package cc.azuramc.orm.exception;

/**
 * 配置异常类
 * @author AzuraMC Team
 */
public class ConfigurationException extends AzuraOrmException {
    
    public ConfigurationException(String message) {
        super("CONFIG_ERROR", message);
    }
    
    public ConfigurationException(String message, Throwable cause) {
        super("CONFIG_ERROR", message, cause);
    }
} 