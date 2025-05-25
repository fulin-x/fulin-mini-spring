package com.fulin.spring;

/**
 * @Author: Fulin
 * @Description: MyBeanPostProcessor
 * @DateTime: 2025/5/15 上午12:26
 **/
@Component
public class MyBeanPostProcessor implements BeanPostProcessor {

    public Object beforeInitializedBean(Object bean, String beanName) {
        return bean;
    }

    public Object afterInitializedBean(Object bean, String beanName) {
        System.out.println(beanName + "初始化完成");
        return bean;
    }
}
