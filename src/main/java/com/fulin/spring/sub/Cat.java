package com.fulin.spring.sub;

import com.fulin.spring.Autowired;
import com.fulin.spring.Component;
import com.fulin.spring.PostConstruct;

/**
 * @Author: Fulin
 * @Description: Cat
 * @DateTime: 2025/5/12 下午11:18
 **/
@Component
public class Cat {

    @Autowired
    private Dog dog;

    @PostConstruct
    public void init() {
        System.out.println("cat init 属性:" + dog);
    }
}
