package com.fulin.spring;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: Fulin
 * @Description: 定义bean
 * @DateTime: 2025/5/12 下午10:46
 **/
public class BeanDefinition {

    private final String name;

    private final Constructor<?> constructor;

    private final Method postConstructMethod;

    private final List<Field> autowriedFields;

    private final Class<?> beanType;

    public BeanDefinition(Class<?> type) {
        this.beanType = type;
        Component component = type.getDeclaredAnnotation(Component.class);
        this.name = component.name().isEmpty() ? type.getSimpleName() : component.name();
        try {
            this.constructor = type.getConstructor();
            this.postConstructMethod =
                    Arrays.stream(type.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(PostConstruct.class))
                            .findFirst().orElse(null);
            this.autowriedFields = Arrays.stream(type.getDeclaredFields())
                    .filter(f -> f.isAnnotationPresent(Autowired.class))
                    .toList();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public Method getPostConstructMethod() {
        return postConstructMethod;
    }

    public List<Field> getAutowriedFields() {
        return autowriedFields;
    }

    public Class<?> getBeanType() {
        return beanType;
    }
}
