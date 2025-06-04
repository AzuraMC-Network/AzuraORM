package cc.azuramc.orm.example;

import cc.azuramc.orm.manager.ChangeManager;

import java.util.List;

/**
 * ChangeManager使用示例
 * @author AzuraMC Team
 */
public class ChangeManagerExample {
    
    public static void main(String[] args) {
        // 示例1：User实体的ChangeManager
        System.out.println("=== 示例1：User实体管理 ===");
        ChangeManager<User> userChangeManager = new ChangeManager<>(
            users -> {
                System.out.println("批量更新 " + users.size() + " 个用户:");
                users.forEach(user -> System.out.println("  - 更新用户: " + user.getName()));
                // 这里可以调用实际的数据库更新操作
                // userDao.updateBatch(users);
            }
        );
        
        // 创建一些用户
        User user1 = new User("张三", 25);
        User user2 = new User("李四", 30);
        User user3 = new User("王五", 28);
        
        // 修改用户并注册到变更管理器
        user1.setAge(26); // 这会设置dirty标记
        userChangeManager.registerDirty(user1);
        
        user2.setName("李四(修改)");
        userChangeManager.registerDirty(user2);
        
        user3.setAge(29);
        userChangeManager.registerDirty(user3);
        
        // 手动刷新
        userChangeManager.flush();
        
        // 示例2：Product实体的ChangeManager
        System.out.println("\n=== 示例2：Product实体管理 ===");
        ChangeManager<Product> productChangeManager = new ChangeManager<>(
            products -> {
                System.out.println("批量更新 " + products.size() + " 个产品:");
                products.forEach(product -> System.out.println("  - 更新产品: " + product.getName()));
                // productDao.updateBatch(products);
            },
            2, // 批量大小为2
            3000 // 3秒刷新间隔
        );
        
        Product product1 = new Product("笔记本电脑", 5999.99);
        Product product2 = new Product("无线鼠标", 99.99);
        
        product1.setPrice(5799.99); // 降价
        productChangeManager.registerDirty(product1);
        
        product2.setName("无线鼠标(升级版)");
        productChangeManager.registerDirty(product2);
        
        // 查看状态
        System.out.println("当前脏实体数量: " + productChangeManager.getDirtyCount());
        System.out.println("批量大小: " + productChangeManager.getBatchSize());
        
        // 清理资源
        userChangeManager.shutdown();
        productChangeManager.shutdown();
        
        System.out.println("\n=== 示例完成 ===");
    }
    
    // 示例User类
    public static class User implements ChangeManager.DirtyTracker {
        private String name;
        private int age;
        private boolean dirty = false;
        
        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            if (!this.name.equals(name)) {
                this.name = name;
                this.dirty = true;
            }
        }
        
        public int getAge() {
            return age;
        }
        
        public void setAge(int age) {
            if (this.age != age) {
                this.age = age;
                this.dirty = true;
            }
        }
        
        @Override
        public boolean isDirty() {
            return dirty;
        }
        
        @Override
        public void cleanDirty() {
            this.dirty = false;
        }
        
        @Override
        public String toString() {
            return "User{name='" + name + "', age=" + age + ", dirty=" + dirty + "}";
        }
    }
    
    // 示例Product类
    public static class Product implements ChangeManager.DirtyTracker {
        private String name;
        private double price;
        private boolean dirty = false;
        
        public Product(String name, double price) {
            this.name = name;
            this.price = price;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            if (!this.name.equals(name)) {
                this.name = name;
                this.dirty = true;
            }
        }
        
        public double getPrice() {
            return price;
        }
        
        public void setPrice(double price) {
            if (this.price != price) {
                this.price = price;
                this.dirty = true;
            }
        }
        
        @Override
        public boolean isDirty() {
            return dirty;
        }
        
        @Override
        public void cleanDirty() {
            this.dirty = false;
        }
        
        @Override
        public String toString() {
            return "Product{name='" + name + "', price=" + price + ", dirty=" + dirty + "}";
        }
    }
} 