package com.astronautlabs.mc.rezolve.common;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigProperty {
	//String category() default Configuration.CATEGORY_GENERAL;
	String comment() default "";
}
