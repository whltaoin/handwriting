package cn.varin.handwriting.annotation;


import cn.varin.service.A;
import cn.varin.service.UserService;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class AnnotationConfigApplicationContext {
    // 用户存储bean定义
    private ConcurrentHashMap<String, DefinitionBean> definitionBeanConcurrentHashMap = new ConcurrentHashMap<>();
   // 用于存储单例bean
    private ConcurrentHashMap<String, Object>   singletonBeanHashMap= new ConcurrentHashMap<>();
    private String scanPackage;
  //   private URL[] urls;


    private Class clazz;
    public AnnotationConfigApplicationContext() {}
    public AnnotationConfigApplicationContext(Class clazz) {
        // 1. 获取到BeanConfig类
        this.clazz = clazz;
        // 扫描+bean定义
        scan(clazz);

    }


    /**
     * 根据对名称获取到bean
     * @param beanName
     * @return
     */
  public Object getBean(String beanName) {
      // 1. 首先判断是否存在与容器中
      boolean containsKey = definitionBeanConcurrentHashMap.containsKey(beanName);
      if (!containsKey) {
          throw  new RuntimeException("Bean not found: " + beanName);
      }
      boolean singletonKey  = singletonBeanHashMap.containsKey(beanName);
      // 2.判断是单例还是原型
      if (singletonKey) {
          // 单例
       return singletonBeanHashMap.get(beanName);


      }else{
          // 原型
          DefinitionBean definitionBean = definitionBeanConcurrentHashMap.get(beanName);
          String className = scanPackage.replace("/",".") + "." + definitionBean.getClassName();
          try {
              return getObject( clazz.forName(className),false);
          } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                   IllegalAccessException | NoSuchMethodException e) {
              throw new RuntimeException(e);
          }


      }


  }

  public void scan (Class clazz) {
      // 2. 获取到BeanConfig类上的注解
      ComponentScan componentScan =(ComponentScan) clazz.getAnnotation(ComponentScan.class);

      if (componentScan == null) {
          return;
      }
      // 3. 获取到扫描路径
      String scanValue = componentScan.value();
      // 4. 通过类加载器获取到同类型的所有对象
      ClassLoader classLoader = componentScan.getClass().getClassLoader();
      //cn.varin 转位cn/varin
      String scanValuePath = scanValue.replace(".", "/");
      this.scanPackage = scanValuePath;



      URL resource = classLoader.getResource(scanValuePath);
      File floder = new File(resource.getFile());

      if (floder.isDirectory()) {
          File[] files = floder.listFiles();
          for (File file : files) {
              // 现在已经获取到扫描文件夹的所有文件了


              try {

                  // 通过反射获取到类 cn.varin.service.UserService
                  Class<?> aClass = Class.forName(scanValue + "." + file.getName().replace(".class", ""));
                  Component component = aClass.getAnnotation(Component.class);
                  ClassType classType = aClass.getAnnotation(ClassType.class);
                  // 开始定义bean
                  if (aClass !=null && component != null) {
                      // 1 判断是否需要注入
                      DefinitionBean definitionBean = new DefinitionBean(aClass,file.getName().replace(".class", ""));
                      String createType = "";
                      // 2 判断需要创建的方式
                      if (classType != null && classType.value().equals("prototype")) {
                          createType="prototype";
                          //如果是原型并不需要存储，每次调用getBean的时候都重新new
                          definitionBean.setClazz(null);

                      }
                      definitionBean.setClassType(createType.equals("prototype")?createType:"singleton");
                      // bean定义
                      definitionBeanConcurrentHashMap.put(definitionBean.getClassName(), definitionBean);

                      // 存储单例bean
                      if (definitionBean.getClassType().equals("singleton")) {


                          singletonBeanHashMap.put(file.getName().replace(".class",""), getObject(aClass,true));
                      }



                  }
              } catch (ClassNotFoundException e) {
                  throw new RuntimeException(e);
              } catch (InvocationTargetException e) {
                  throw new RuntimeException(e);
              } catch (NoSuchMethodException e) {
                  throw new RuntimeException(e);
              } catch (InstantiationException e) {
                  throw new RuntimeException(e);
              } catch (IllegalAccessException e) {
                  throw new RuntimeException(e);
              }
          }


      }
  }
    // 获取到类
  public Object getObject(Class clazz,Boolean b) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
      Constructor declaredConstructor = clazz.getDeclaredConstructor();
      declaredConstructor.setAccessible(b);
      return declaredConstructor.newInstance();

  }

}
