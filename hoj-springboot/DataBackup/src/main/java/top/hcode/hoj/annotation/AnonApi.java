package top.hcode.hoj.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author Himit_ZH
 * @Date 2022/10/27
 * controller的方法或类标记了该注解，则说明对应接口是开放接口，无需登录认证
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AnonApi {
}