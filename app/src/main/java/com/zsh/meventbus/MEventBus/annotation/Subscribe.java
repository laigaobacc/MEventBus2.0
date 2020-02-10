package com.zsh.meventbus.MEventBus.annotation;

import com.zsh.meventbus.MEventBus.mode.ThreadMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)//标注是哪种java成员,这里是方法
@Retention(RetentionPolicy.RUNTIME)//策略属性,这里是运行是可通过反射访问
public @interface Subscribe {
    ThreadMode threadMode() default ThreadMode.POSTING;
}
