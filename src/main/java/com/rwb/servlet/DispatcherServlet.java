package com.rwb.servlet;

import com.rwb.annotation.Autowired;
import com.rwb.annotation.Controller;
import com.rwb.annotation.RequestMapping;
import com.rwb.annotation.Service;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DispatcherServlet extends HttpServlet{

    // 保存所有的.class文件名
    List<String> classNames = new ArrayList<String>();

    // 注解中value与对象的映射
    Map<String, Object> beans = new HashMap<String, Object>();

    // 请求路径与方法的映射
    Map<String, Object> handerMap = new HashMap<String, Object>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        // mySpringMVC/rwb/query
        String requestURI = req.getRequestURI();

        String context = req.getContextPath();

        // rwb/query
        String path = requestURI.replace(context, "");

        Method method = (Method) handerMap.get(path);
    }

    /**
     * 初始化执行
     * @param config
     */
    public void init(ServletConfig config) {

        // 1. 扫描所有的.class文件
        doScanClassFile("com.rwb");

        // 2. 根据扫描到的class文件反射生成对象
        doInstance();

        // 3. 依赖注入
        doAutowired();

        // 4. 建立请求路径与方法的映射关系
        urlMapping();
    }

    /**
     * 建立请求路径与方法的映射关系
     */
    private void urlMapping() {

        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();

            if (clazz.isAnnotationPresent(RequestMapping.class)) {

                RequestMapping clazzMapping = clazz.getAnnotation(RequestMapping.class);
                String classPath = clazzMapping.value();
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping methodMapping = method.getAnnotation(RequestMapping.class);
                        String methodPath = methodMapping.value();

                        handerMap.put(classPath + methodPath, method);
                    }
                }
            }
        }
    }

    /**
     * 设置依赖注入
     */
    private void doAutowired() {

        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();

            if (clazz.isAnnotationPresent(Controller.class)) {

                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(Autowired.class)) {
                        Autowired autowired = field.getAnnotation(Autowired.class);
                        String value = autowired.value();

                        field.setAccessible(true);
                        try {
                            field.set(instance, beans.get(value));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * 根据class全限定名反射生成对象
     */
    private void doInstance() {

        for (String className : classNames) {
            className = className.replace(".class", "");
            try {
                Class<?> clazz = Class.forName(className);

                if (clazz.isAnnotationPresent(Controller.class)) {

                    Object instance = clazz.newInstance();
                    RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                    String key = requestMapping.value();

                    beans.put(key, instance);
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    Object instance = clazz.newInstance();
                    Service service = clazz.getAnnotation(Service.class);
                    String key = service.value();

                    if (key == null || "".equals(key)) {
                        key = clazz.getSimpleName();
                    }
                    beans.put(key, instance);
                } else {
                    continue;
                }
            } catch (Exception e) {
                
                e.printStackTrace();
            } 
        }
    }

    /**
     * 扫描指定目录下所有的class文件
     * @param basePackage       指定的目录
     */
    private void doScanClassFile(String basePackage) {

        // 扫描所有的类路径
        URL url = this.getClass().getClassLoader().getResource(File.separator + basePackage.replaceAll("\\.", "/"));
        // F:/IdeaWorkSpace/mySpringMVC/src/main/java\com
        String fileStr = url.getFile();

        File file = new File(fileStr);

        String[] filesPath = file.list();
        for (String path : filesPath) {
            File filePath = new File(fileStr + path);

            if (filePath.isDirectory()) {
                // 该目录仍为文件夹
                doScanClassFile(basePackage + "." + path);
            } else {
                // 找到了.class文件
                classNames.add(basePackage + "." + filePath.getName());
            }
        }

    }
}
