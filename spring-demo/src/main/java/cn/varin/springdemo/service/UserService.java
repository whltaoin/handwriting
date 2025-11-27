package cn.varin.springdemo.service;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component(value = "UserService")
public class UserService {
    public  UserService() {
        System.out.println("UserService init");
    }
}
