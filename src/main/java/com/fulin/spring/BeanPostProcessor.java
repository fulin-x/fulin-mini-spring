package com.fulin.spring;

/**
 * @Author: Fulin
 * @Description: BeanPostProcessor
 * @DateTime: 2025/5/15 上午12:19
 **/
public interface BeanPostProcessor {

    Object beforeInitializedBean(Object bean, String beanName);

    Object afterInitializedBean(Object bean, String beanName);
}
