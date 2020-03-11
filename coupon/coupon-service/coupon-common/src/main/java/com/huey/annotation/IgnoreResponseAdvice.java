package com.huey.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: huey
 * @Desc: 忽略统一响应注解定义   实现在advice
 */
//可以标级到类和注解上面
@Target({ElementType.TYPE,ElementType.METHOD})
//运行时得环境
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreResponseAdvice {


}
