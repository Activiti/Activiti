package org.activiti.cycle.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.action.Action;
import org.activiti.cycle.action.DownloadContentAction;
import org.activiti.cycle.action.ParameterizedAction;

/**
 * Annotation used to define a sorting on components. Component instances are
 * listed in various places, for example in the UI. Putting this annotation on a
 * component is a <strong>hint</strong> for the cycle component infrastructure
 * to attempt to list instances of this component <em>before</em> instances of
 * the components referenced using this annotation. If this annotation is set
 * for a cycle component and no parameters are given, cycle will try to list the
 * corresponding component instances as first items in the list.
 * 
 * <p>
 * <strong>Note:</strong> the sorting on components using
 * {@link ListBeforeComponents} and {@link ListAfterComponents} is only
 * supported above the service layer, ie. on component lists returned by a cycle
 * service.
 * </p>
 * 
 * <p>
 * <strong>Note:</strong> the sorting on components is currently only supported
 * for some component types:
 * <ul>
 * <li>{@link Action} (which subsumes {@link ParameterizedAction},
 * {@link DownloadContentAction} etc...)</li>
 * <li> {@link ContentRepresentation}</li>
 * </ul>
 * </p>
 * 
 * @author daniel.meyer@camunda.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface ListBeforeComponents {

  String[] value() default {};

  String[] names() default {};

  @SuppressWarnings("rawtypes")
  Class[] classes() default {};

}
