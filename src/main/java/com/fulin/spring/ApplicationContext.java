package com.fulin.spring;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Fulin
 * @Description: 上下文
 * @DateTime: 2025/5/12 下午10:23
 **/
public class ApplicationContext {

    public ApplicationContext(String packageName) throws Exception {
        initContext(packageName);
    }

    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    private Map<String, Object> ioc = new HashMap<>();

    private Map<String,Object> lodingIoc = new HashMap<>();

    private List<BeanPostProcessor> postProcessors = new ArrayList<>();

    public void initContext(String packageName) throws Exception {
        scanPackage(packageName).stream().filter(this::scanCreate).forEach(this::wrapper);
        initBeanPostProcessor();
        beanDefinitionMap.values().forEach(this::createBean);
    }

    private void initBeanPostProcessor() {
        beanDefinitionMap.values().stream()
                .filter(bd -> BeanPostProcessor.class.isAssignableFrom(bd.getBeanType()))
                .map(this::createBean)
                .map(bean -> (BeanPostProcessor) bean)
                .forEach(postProcessors::add);
    }

    protected boolean scanCreate(Class<?> type) {
        return type.isAnnotationPresent(Component.class);
    }

    protected Object createBean(BeanDefinition beanDefinition) {
        String name = beanDefinition.getName();
        if (ioc.containsKey(name)) {
            return ioc.get(name);
        }
        // 二级缓存
        if(lodingIoc.containsKey(name)) {
            return lodingIoc.get(name);
        }
        return doCreateBean(beanDefinition);
    }

    private Object doCreateBean(BeanDefinition beanDefinition) {
        Constructor<?> constructor = beanDefinition.getConstructor();
        Object bean = null;
        try {
            bean = constructor.newInstance();
            lodingIoc.put(beanDefinition.getName(),bean);
            autowiredBean(bean,beanDefinition);
            bean = initializedBean(bean,beanDefinition);
            lodingIoc.remove(beanDefinition.getName());
            ioc.put(beanDefinition.getName(), bean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return bean;
    }

    private Object initializedBean(Object bean, BeanDefinition beanDefinition) throws Exception {
        for(BeanPostProcessor postProcessor : postProcessors){
            bean = postProcessor.beforeInitializedBean(bean,beanDefinition.getName());
        }

        Method postConstructMethod = beanDefinition.getPostConstructMethod();
        if (postConstructMethod != null) {
            postConstructMethod.invoke(bean);
        }

        for(BeanPostProcessor postProcessor : postProcessors){
            bean = postProcessor.afterInitializedBean(bean,beanDefinition.getName());
        }
        return bean;
    }

    private void autowiredBean(Object bean, BeanDefinition beanDefinition) throws IllegalAccessException {
        for (Field autowriedField : beanDefinition.getAutowriedFields()) {
            autowriedField.setAccessible(true);
            autowriedField.set(bean, getBean(autowriedField.getType()));
        }
    }

    protected BeanDefinition wrapper(Class<?> type) {
        BeanDefinition beanDefinition = new BeanDefinition(type);
        beanDefinitionMap.containsKey(beanDefinition.getName());
        beanDefinitionMap.put(beanDefinition.getName(), beanDefinition);
        return beanDefinition;
    }

    private List<Class<?>> scanPackage(String packageName) throws Exception {
        System.out.println("扫描包" + packageName);
        List<Class<?>> classList = new ArrayList<>();
        // a.b.c
        URL resource = this.getClass().getClassLoader().getResource(packageName.replace(".", File.separator));
        Path path = null;
        if (resource != null) {
            path = Paths.get(resource.toURI());
        }
        if (path != null) {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    System.out.println("文件：" + file);
                    Path absolutePath = file.toAbsolutePath();
                    if (absolutePath.toString().endsWith(".class")) {
                        String replaceStr = absolutePath.toString().replace(File.separator, ".");
                        int packageIndex = replaceStr.indexOf(packageName);
                        String className = replaceStr.substring(packageIndex, replaceStr.length() - ".class".length());
                        try {
                            classList.add(Class.forName(className));
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        return classList;
    }

    public Object getBean(String name) {
        if(name == null)
            return null;
        Object bean = this.ioc.get(name);
        if(bean!=null){
            return bean;
        }
        if(beanDefinitionMap.containsKey(name)){
            return createBean(beanDefinitionMap.get(name));
        }
        return null;
    }

    public <T> T getBean(Class<T> beanType) {
        String beanName = this.beanDefinitionMap.values().stream()
                .filter(bd -> beanType.isAssignableFrom(bd.getBeanType()))
                .map(BeanDefinition::getName)
                .findFirst()
                .orElse(null);
        return (T) getBean(beanName);
    }

    public <T> List<T> getBeans(Class<T> beanType) {
        return this.beanDefinitionMap.values().stream()
                .filter(bd -> beanType.isAssignableFrom(bd.getBeanType()))
                .map(BeanDefinition::getName)
                .map(this::getBean)
                .map(bean -> (T) bean)
                .toList();
    }
}
