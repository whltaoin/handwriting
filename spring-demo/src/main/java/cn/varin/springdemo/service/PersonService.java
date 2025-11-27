package cn.varin.springdemo.service;

import org.springframework.stereotype.Service;

@Service
public class PersonService {
    public PersonService() {
        System.out.println("PersionService init");
    }
}
