package org.activiti.cycle.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.activiti.cycle.impl.Interceptor;

/**
 * Annotate ActivityCycle components with this 
 * @author meyerd
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Interceptors {
  
  Class<? extends Interceptor>[] value();

}
