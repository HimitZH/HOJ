package top.hcode.hoj.utils;

import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @Author Himit_ZH
 * @Date 2022/5/9
 */
public class ServiceContextUtils {
    /**
     * 先从method上获取注解，获取不到再从class上获取
     *
     * @param method
     * @param clazz
     * @param annotationClass
     * @param <T>
     * @return 注解对象
     */
    public static <T extends Annotation> T getAnnotation(Method method, Class<?> clazz, Class<T> annotationClass) {
        T annotation = AnnotationUtils.getAnnotation(method, annotationClass);
        if (annotation == null) {
            annotation = AnnotationUtils.findAnnotation(clazz, annotationClass);
        }
        return annotation;
    }
}
