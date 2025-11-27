package cn.varin.springdemo.test;

import cn.varin.springdemo.config.SpringConfig;
import cn.varin.springdemo.service.PersonService;
import cn.varin.springdemo.service.UserService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringBeanTest {
    public static void main(String[] args) {
      AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
      // 根据类型获取Bean
        UserService userService = context.getBean(UserService.class);
        // 根据名称获取Bean
        PersonService persionService = (PersonService) context.getBean("personService");
        context.close();

    }
}
