package cn.varin.test;

import cn.varin.config.BeanConfig;
import cn.varin.handwriting.annotation.AnnotationConfigApplicationContext;
import cn.varin.service.PersonService;
import cn.varin.service.UserService;

import java.lang.reflect.Constructor;

public class SpringBeanTest {
    public static void main(String[] args) {

        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(BeanConfig.class);
        UserService userService1 = (UserService) annotationConfigApplicationContext.getBean("UserService");
        UserService userService2 = (UserService) annotationConfigApplicationContext.getBean("UserService");

        System.out.println(userService1);
        System.out.println(userService2);

        PersonService personService1 = (PersonService) annotationConfigApplicationContext.getBean("PersonService");
        PersonService personService2 = (PersonService) annotationConfigApplicationContext.getBean("PersonService");
        System.out.println(personService1);
        System.out.println(personService2);
        personService1.test();
        personService2.test();



    }
}
