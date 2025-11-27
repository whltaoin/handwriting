package cn.varin.springdemo.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration // 定义配置类
@ComponentScan(value = "cn.varin.springdemo.service") // 扫描路径
public class SpringConfig {
}
