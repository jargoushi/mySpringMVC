package com.rwb.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
// 注解可以被继承
@Inherited
public @interface Controller {

    String value() default "";

}
