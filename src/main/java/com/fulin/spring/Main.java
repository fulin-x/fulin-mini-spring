package com.fulin.spring;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @Author: Fulin
 * @Description: 主函数
 * @DateTime: 2025/5/6 下午11:15
 **/
public class Main {
    public static void main(String[] args) throws Exception {
        ApplicationContext ioc = new ApplicationContext("com.fulin.spring");
        Object dog = ioc.getBean("myDog");
        System.out.println(dog);
    }
}