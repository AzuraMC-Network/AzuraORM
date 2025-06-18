package cc.azuramc.orm.config;

/**
 * AzuraORM全局配置类
 * @author AzuraMC Team
 */
public class GlobalConfig {
    
    private static boolean debugMode = false;
    
    /**
     * 设置Debug模式
     * @param enabled 是否启用Debug模式
     */
    public static void setDebugMode(boolean enabled) {
        debugMode = enabled;
        if (enabled) {
            System.out.println("[AzuraORM] Debug mode enabled");
        } else {
            System.out.println("[AzuraORM] Debug mode disabled");
        }
    }
    
    /**
     * 获取Debug模式状态
     * @return 是否启用Debug模式
     */
    public static boolean isDebugMode() {
        return debugMode;
    }
    
    /**
     * Debug输出方法
     * @param message 要输出的消息
     */
    public static void debugLog(String message) {
        if (debugMode) {
            System.out.println("[AzuraORM-DEBUG] " + message);
        }
    }
    
    /**
     * Debug输出方法（带分类）
     * @param category 消息分类
     * @param message 要输出的消息
     */
    public static void debugLog(String category, String message) {
        if (debugMode) {
            System.out.println("[AzuraORM-DEBUG-" + category + "] " + message);
        }
    }
} 