package com.fulin.spring;

import java.lang.reflect.Constructor;

/**
 * @Author: Fulin
 * @Description: 定义bean
 * @DateTime: 2025/5/12 下午10:46
 **/
public class BeanDefinition {

    private String name;

    private Constructor<?> constructor;

    public BeanDefinition(Class<?> type){
        Component component = type.getDeclaredAnnotation(Component.class);
        this.name = component.name().isEmpty() ? type.getSimpleName() : component.name();
        try {
            this.constructor = type.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName(){
        return name;
    }

    public Constructor<?> getConstructor(){
        return constructor;
    }
}
