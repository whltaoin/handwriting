package cn.varin.service;

import cn.varin.handwriting.annotation.ClassType;
import cn.varin.handwriting.annotation.Component;

@Component
@ClassType(value = "prototype")
public class PersonService {
    public void test(){
        System.out.println("PersonService");
    }
}
