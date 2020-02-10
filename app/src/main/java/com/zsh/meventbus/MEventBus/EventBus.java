package com.zsh.meventbus.MEventBus;

import android.os.Looper;

import com.zsh.meventbus.MEventBus.annotation.Subscribe;
import com.zsh.meventbus.MEventBus.core.MethodManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;

public class EventBus {

    private static volatile EventBus instance;
    //用来保存这些带注解的方法
    private Map<Object, List<MethodManager>> cachMap;

    private Handler handler;
    private ExecutorService executorService;//线程池


    private EventBus() {
        cachMap = new HashMap<>();

        //handler高级用法，把handler绑定到主线程中使用
        handler = new Handler(Looper.getMainLooper());

        //创建一个子线程(缓存线程池)
        executorService = Executors.newCachedThreadPool();

    }

    public static EventBus getDefault() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }


    //找到MainActivity所有带有注解的方法
    private List<MethodManager> findAnnotationMethod(Object getter) {
        List<MethodManager> methodList = new ArrayList<>();
        //获取类
        Class<?> clazz = getter.getClass();
        //获取所有方法
        Method[] methods = clazz.getMethods();

        //性能优化,N个父类不可能有自定义注解,排除后再反射
        while (clazz != null) {
            //找出系统类直接跳出,不添加
            String clazzName = clazz.getName();
            if (clazzName.startsWith("java.") || clazzName.startsWith("javax.") ||
                    clazzName.startsWith("android.")) {
                break;
            }
            //循环方法
            for (Method method : methods) {
                //获取方法的注解
                Subscribe subscribe = method.getAnnotation(Subscribe.class);
                //判断注解不为空
                if (subscribe == null) {
                    continue;
                }
                //严格控制方法格式和规范
                //方法必须范围Void
                Type returnType = method.getGenericReturnType();
                if (!"void".equals(returnType.toString())) {
                    throw new RuntimeException(method.getName() + "返回方法必须是void");
                }
                //方法参数必须有值(二次匹配)
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1) {
                    throw new RuntimeException(method.getName() + "方法有且只有一个参数");
                }

                //完全符合要求(三个参数:参数，线程模式，方法)
                MethodManager manager = new MethodManager(parameterTypes[0], subscribe.threadMode(), method);
                methodList.add(manager);
            }
            //不断循环找出父类中含有订阅者(注解方法)的类，直到为空，例如AppCompatActivity没有订阅者
            clazz = clazz.getSuperclass();
        }
        return methodList;
    }

    //注册
    public void register(Object getter) {
        //获取到MainActivity和其父类的所有注解的方法
        List<MethodManager> methodList = cachMap.get(getter);
        if (methodList == null) {
            methodList = findAnnotationMethod(getter);
            cachMap.put(getter, methodList);
        }
    }


    //发送消息方法
    public void post(final Object setter) {
        //订阅者已经登记，从登记表中提出
        Set<Object> set = cachMap.keySet();
        //比如获取Mactivity对象
        for (final Object getter : set) {

            //获取Mactivity中有注解的方法
            List<MethodManager> methodList = cachMap.get(getter);
            if (methodList != null) {

                //循环每个方法
                for (final MethodManager method : methodList) {
                    //如果匹配上了再invoke
                    //这个方法的参数类型和EventBean是匹配的
                    if (method.getType().isAssignableFrom(setter.getClass())) {
                        //线程调度
                        switch (method.getThreadMode()) {
                            case POSTING:
                                invoke(method, getter, setter);
                                break;
                            case MAIN:
                                //先判断发送方是否在主线程
                                if (Looper.myLooper() == Looper.getMainLooper()) {
                                    invoke(method, getter, setter);
                                } else {
                                    //子线程->主线程，切换线程(用到Handler)
                                    handler.post(() -> {
                                        invoke(method, getter, setter);
                                    });
                                }
                            case BACKGROUND:
                                if (Looper.myLooper() == Looper.getMainLooper()) {
                                    //主线程-子线程。创建一个子线程(缓存线程池)
                                    executorService.execute(() -> {
                                        invoke(method, getter, setter);
                                    });
                                } else {
                                    invoke(method, getter, setter);
                                }
                            case ASYNC:
                                if (Looper.myLooper() == Looper.getMainLooper()) {
                                    //主线程-子线程。创建一个子线程(缓存线程池)
                                    executorService.execute(() -> {
                                        invoke(method, getter, setter);
                                    });
                                } else {
                                    invoke(method, getter, setter);
                                }
                        }
                    }
                }
            }
        }
    }

    private void invoke(MethodManager method, Object getter, Object setter) {
        Method exectue = method.getMethod();
        try {
            exectue.invoke(getter, setter);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }
}
