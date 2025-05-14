package com.fulin.spring.sub;

import com.fulin.spring.Component;

/**
 * @Author: Fulin
 * @Description: Dog
 * @DateTime: 2025/5/12 下午11:39
 **/
@Component(name = "myDog")
public class Dog {

    @Autowired
    private Cat cat;

    @Autowired
    private Dog dog;

    @PostConstruct
    public void init() {
        System.out.println("dog创建完成，属性" + cat + ',' + dog);
    }
}
