# 手写Spring框架与Spring Demo项目

## 项目结构

本项目包含两个主要模块，分别是手写的Spring框架实现和标准Spring框架的演示项目：

```
├── handwriting-springbean/   # 手写Spring框架实现模块
│   ├── src/main/java/cn/varin/  
│   │   ├── config/              # 配置类
│   │   ├── handwriting/         # 核心实现
│   │   ├── service/             # 测试服务类
│   │   └── test/                # 测试类
├── spring-demo/              # 标准Spring框架演示模块
│   ├── src/main/java/cn/varin/springdemo/
│   │   ├── config/              # Spring配置类
│   │   ├── service/             # 演示服务类
│   │   └── test/                # 测试类
└── pom.xml                   # 父项目Maven配置
```

## 模块说明

### 1. handwriting-springbean模块

这是一个手写的Spring容器框架核心实现，主要功能包括：

- **注解扫描与Bean注册**：通过自定义的`@ComponentScan`注解指定扫描路径，自动发现并注册带有`@Component`注解的类
- **单例与原型Bean管理**：支持单例（Singleton）和原型（Prototype）两种Bean作用域
- **Bean生命周期管理**：实现了基本的Bean实例化和获取功能
- **自定义注解实现**：
  - `@Component`：标记需要被容器管理的类
  - `@ComponentScan`：指定要扫描的包路径
  - `@ClassType`：指定Bean的作用域类型

**核心类说明**：
- `AnnotationConfigApplicationContext`：手写的Spring容器实现，负责Bean的扫描、注册和管理
- `DefinitionBean`：Bean定义类，存储Bean的相关信息
- `BeanConfig`：配置类，通过注解指定扫描路径

**使用示例**：
```java
// 初始化容器
AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(BeanConfig.class);
// 获取Bean实例
UserService userService = (UserService) context.getBean("UserService");
```

#### handwriting包详细代码分析

`handwriting-springbean/src/main/java/cn/varin/handwriting/annotation/` 包中包含了手写Spring框架的核心实现代码，下面对每个文件进行详细分析：

##### 1. AnnotationConfigApplicationContext.java

这是整个手写Spring框架的核心类，实现了类似Spring的`AnnotationConfigApplicationContext`的功能，主要负责：

```java
public class AnnotationConfigApplicationContext {
    // 用于存储bean定义信息的容器
    private ConcurrentHashMap<String, DefinitionBean> definitionBeanConcurrentHashMap = new ConcurrentHashMap<>();
    // 用于存储单例bean实例的容器
    private ConcurrentHashMap<String, Object> singletonBeanHashMap = new ConcurrentHashMap<>();
    private String scanPackage;
    private Class clazz;
    
    // 构造方法，接收配置类
    public AnnotationConfigApplicationContext(Class clazz) {
        this.clazz = clazz;
        // 扫描并注册Bean
        scan(clazz);
    }
    
    // 根据bean名称获取bean实例
    public Object getBean(String beanName) {
        // 检查Bean是否存在
        boolean containsKey = definitionBeanConcurrentHashMap.containsKey(beanName);
        if (!containsKey) {
            throw new RuntimeException("Bean not found: " + beanName);
        }
        
        // 判断是单例还是原型
        boolean singletonKey = singletonBeanHashMap.containsKey(beanName);
        if (singletonKey) {
            // 单例模式直接返回已存在的实例
            return singletonBeanHashMap.get(beanName);
        } else {
            // 原型模式每次创建新实例
            DefinitionBean definitionBean = definitionBeanConcurrentHashMap.get(beanName);
            String className = scanPackage.replace("/",".") + "." + definitionBean.getClassName();
            try {
                return getObject(clazz.forName(className), false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    // 扫描指定包路径下的类并注册Bean
    public void scan(Class clazz) {
        // 获取配置类上的ComponentScan注解
        ComponentScan componentScan = (ComponentScan) clazz.getAnnotation(ComponentScan.class);
        if (componentScan == null) {
            return;
        }
        
        // 获取扫描路径
        String scanValue = componentScan.value();
        String scanValuePath = scanValue.replace(".", "/");
        this.scanPackage = scanValuePath;
        
        // 通过类加载器获取资源
        ClassLoader classLoader = componentScan.getClass().getClassLoader();
        URL resource = classLoader.getResource(scanValuePath);
        File folder = new File(resource.getFile());
        
        // 遍历文件夹中的所有文件
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            for (File file : files) {
                try {
                    // 加载类并检查注解
                    Class<?> aClass = Class.forName(scanValue + "." + file.getName().replace(".class", ""));
                    Component component = aClass.getAnnotation(Component.class);
                    ClassType classType = aClass.getAnnotation(ClassType.class);
                    
                    // 如果是需要管理的Bean
                    if (aClass != null && component != null) {
                        // 创建Bean定义
                        DefinitionBean definitionBean = new DefinitionBean(aClass, file.getName().replace(".class", ""));
                        String createType = "";
                        
                        // 判断作用域类型
                        if (classType != null && classType.value().equals("prototype")) {
                            createType = "prototype";
                            // 原型模式不需要存储Class引用
                            definitionBean.setClazz(null);
                        }
                        definitionBean.setClassType(createType.equals("prototype") ? createType : "singleton");
                        
                        // 注册Bean定义
                        definitionBeanConcurrentHashMap.put(definitionBean.getClassName(), definitionBean);
                        
                        // 单例模式立即创建实例
                        if (definitionBean.getClassType().equals("singleton")) {
                            singletonBeanHashMap.put(file.getName().replace(".class", ""), getObject(aClass, true));
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    
    // 创建对象实例的方法
    public Object getObject(Class clazz, Boolean b) throws Exception {
        Constructor declaredConstructor = clazz.getDeclaredConstructor();
        declaredConstructor.setAccessible(b);
        return declaredConstructor.newInstance();
    }
}
```

**核心设计思路**：
- 使用两个ConcurrentHashMap分别存储Bean定义和单例Bean实例，确保线程安全
- 通过反射机制实现类的加载、实例化和注解解析
- 实现了单例和原型两种Bean作用域的管理逻辑
- 提供了getBean方法用于获取Bean实例

##### 2. DefinitionBean.java

Bean定义类，用于存储Bean的元数据信息：

```java
public class DefinitionBean {
    // Bean的Class对象引用
    private Class clazz;
    // Bean的作用域类型（singleton或prototype）
    private String classType;
    // Bean的名称
    private String className;
    
    // 构造方法
    public DefinitionBean(Class clazz, String classType, String className) {
        this.clazz = clazz;
        this.classType = classType;
        this.className = className;
    }
    
    public DefinitionBean(Class clazz, String className) {
        this.clazz = clazz;
        this.className = className;
    }
    
    // 无参构造方法
    public DefinitionBean() {}
    
    // Getter和Setter方法
    public Class getClazz() { return clazz; }
    public void setClazz(Class clazz) { this.clazz = clazz; }
    public String getClassType() { return classType; }
    public void setClassType(String classType) { this.classType = classType; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    
    // toString方法，用于调试
    @Override
    public String toString() {
        return "DefinitionBean{" +
                "clazz=" + clazz +
                ", classType='" + classType + '\'' +
                ", className='" + className + '\'' +
                '}';
    }
}
```

**设计思路**：
- 作为Bean的元数据容器，存储Bean的关键信息
- 为不同场景提供多种构造方法
- 实现了标准的Java Bean风格，包含getter和setter方法

##### 3. Component.java

自定义的@Component注解，用于标记需要被Spring容器管理的类：

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
    String value() default "";
}
```

**注解参数说明**：
- `value()`：Bean的名称，默认为空字符串
- `@Target({ElementType.TYPE})`：表示该注解只能应用于类、接口或枚举类型
- `@Retention(RetentionPolicy.RUNTIME)`：表示该注解在运行时仍然可见，可以通过反射获取

##### 4. ComponentScan.java

自定义的@ComponentScan注解，用于指定Spring容器需要扫描的包路径：

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentScan {
    String value();
}
```

**注解参数说明**：
- `value()`：需要扫描的包路径
- 与@Component类似，也是应用于类型且运行时可见

##### 5. ClassType.java

自定义的@ClassType注解，用于指定Bean的作用域类型：

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ClassType {
    String value() default "";
}
```

**注解参数说明**：
- `value()`：指定Bean的作用域类型，如"singleton"或"prototype"，默认为空字符串
- 同样是应用于类型且运行时可见的注解

#### 手写Spring框架的工作流程

1. **初始化阶段**：
   - 创建`AnnotationConfigApplicationContext`实例，传入配置类
   - 从配置类中获取`@ComponentScan`注解的扫描路径
   - 根据扫描路径查找并加载符合条件的类

2. **Bean注册阶段**：
   - 遍历扫描到的类，检查是否带有`@Component`注解
   - 如果有，创建`DefinitionBean`对象，记录Bean的元信息
   - 检查是否带有`@ClassType`注解，确定Bean的作用域类型
   - 将Bean定义注册到`definitionBeanConcurrentHashMap`中

3. **单例Bean实例化阶段**：
   - 对于单例Bean，在注册后立即通过反射创建实例
   - 将创建好的实例存储到`singletonBeanHashMap`中

4. **Bean获取阶段**：
   - 调用`getBean`方法时，根据Bean名称查找对应的Bean定义
   - 对于单例Bean，直接从`singletonBeanHashMap`中获取已创建的实例
   - 对于原型Bean，每次都通过反射创建新的实例并返回

#### 代码优化建议

1. **异常处理优化**：目前代码中大量使用`throw new RuntimeException(e)`，可以考虑定义更具体的异常类型

2. **资源关闭**：在处理文件和资源时，应确保适当关闭，避免资源泄漏

3. **并发安全**：虽然使用了ConcurrentHashMap，但在某些复合操作中可能仍需额外的同步措施

4. **包扫描递归**：当前实现只扫描了一级目录，应增加递归扫描子目录的功能

5. **依赖注入**：可以扩展实现属性注入和构造函数注入功能

6. **Bean生命周期回调**：可以添加初始化和销毁回调方法的支持

通过这个手写实现，可以清晰地理解Spring容器的核心工作原理，包括Bean的扫描、注册、实例化和管理过程。

### 2. spring-demo模块

这是一个使用标准Spring框架的演示项目，用于对比和参考：

- **使用标准Spring注解**：
  - `@Configuration`：标记配置类
  - `@ComponentScan`：指定扫描路径
  - `@Component`/`@Service`：标记Bean类
- **标准Spring容器使用**：展示如何使用Spring的`AnnotationConfigApplicationContext`
- **Bean生命周期演示**：展示Bean的初始化过程

**核心类说明**：
- `SpringConfig`：Spring配置类
- `UserService`和`PersonService`：演示服务类
- `SpringBeanTest`：测试类，展示如何获取和使用Spring容器中的Bean

## 对比分析

通过对比两个模块，可以清晰地理解Spring框架的核心原理：

1. **实现方式对比**：
   - 手写版本：直接使用反射API实现Bean的扫描、实例化和管理
   - 标准Spring：使用完整的Spring框架，提供更丰富的功能和更完善的错误处理

2. **功能对比**：
   - 手写版本：实现了基本的Bean容器功能，支持单例/原型模式
   - 标准Spring：提供完整的依赖注入、AOP、事件机制等企业级功能

## 运行说明

1. 确保JDK 17及以上版本
2. 使用Maven构建项目
3. 可以分别运行两个模块中的测试类来验证功能

## 学习价值

本项目通过手写Spring框架的核心功能，帮助理解：
- Spring容器的工作原理
- 注解的定义和使用
- 反射机制在框架开发中的应用
- Bean的生命周期管理
- 依赖注入的基本思想