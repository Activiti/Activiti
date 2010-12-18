package org.activiti.cycle.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.activiti.cycle.context.CycleContextType;

/**
 * An ActivitiCycle component. Components can use services such as
 * {@link Interceptors}, {@link CycleConfigParameter}s ...
 * 
 * @author daniel.meyer@camunda.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface CycleComponent {

  String value() default "";

  String name() default "";

  /**
   * Optional parameter: if a {@link CycleContextType} is set, cycle will store
   * instances of the corresponding component in the corresponding context.
   */
  CycleContextType context() default CycleContextType.NONE;

}
