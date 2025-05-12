package com.fulin.spring;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @Author: Fulin
 * @Description: 上下文
 * @DateTime: 2025/5/12 下午10:23
 **/
public class ApplicationContext {

    public ApplicationContext(String packageName) throws Exception {
        initContext(packageName);
    }

    private Map<String,Object> ioc = new HashMap<>();

    public void initContext(String packageName) throws Exception{
        scanPackage(packageName).stream().filter(this::scanCreate).map(this::wrapper).forEach(this::createBean);
    }

    protected  boolean scanCreate(Class<?> type){
        return type.isAnnotationPresent(Component.class);
    }

    protected void createBean(BeanDefinition beanDefinition){
        System.out.println("创建bean");
        String name = beanDefinition.getName();
        if (ioc.containsKey(name)) {
            return;
        }
        doCreateBean(beanDefinition);
    }

    private void doCreateBean(BeanDefinition beanDefinition) {
        Constructor<?> constructor = beanDefinition.getConstructor();
        Object bean = null;
        try {
            bean = constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ioc.put(beanDefinition.getName(),bean);
    }

    protected BeanDefinition wrapper(Class<?> type){
        return new BeanDefinition(type);
    }

    private List<Class<?>> scanPackage(String packageName) throws Exception{
        System.out.println("扫描包"+packageName);
        List<Class<?>> classList = new ArrayList<>();
        // a.b.c
        URL resource = this.getClass().getClassLoader().getResource(packageName.replace(".", File.separator));
        Path path = null;
        if (resource != null) {
            path = Paths.get(resource.toURI());
        }
        if (path != null) {
            Files.walkFileTree(path, new SimpleFileVisitor<>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    System.out.println("文件：" + file);
                    Path absolutePath = file.toAbsolutePath();
                    if(absolutePath.toString().endsWith(".class")){
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

    public Object getBean(String name){
        return this.ioc.get(name);
    }

    public <T> T getBean(Class<T> beanType){
        return this.ioc.values().stream()
                .filter(bean -> beanType.isAnnotationPresent((Class<? extends Annotation>) bean.getClass()))
                .map(bean -> (T) bean)
                .findAny().orElseGet(null);
    }

    public <T> List<T> getBeans(Class<T> beanType){
        return this.ioc.values().stream()
                .filter(bean -> beanType.isAnnotationPresent((Class<? extends Annotation>) bean.getClass()))
                .map(bean -> (T) bean)
                .toList();
    }
}
