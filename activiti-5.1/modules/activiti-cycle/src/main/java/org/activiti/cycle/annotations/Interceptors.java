package org.activiti.cycle.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.impl.Interceptor;

/**
 * Annotation for specifying a list of interceptors for a cycle component.
 * Interceptors are classes implementing the {@link Interceptor} interface.
 * 
 * <p>
 * <strong>Note:</strong> the {@link CycleComponentFactory} generates proxies
 * using the built-in java proxy mechanisms. Cycle component implementors must
 * therefore ensure that components annotated using this annotation implement at
 * least one interface which can be proxied. This is true for cycle components
 * with a special <em>component type</em> (see {@link CycleComponent}). Also
 * make sure not to cast instances of such components to the implementation type
 * but to the interface. If a component implements multiple interfaces, all of
 * the interfaces are proxied.
 * </p>
 * 
 * @see CycleComponent
 * @see CycleComponentFactory
 * @see Interceptor
 * 
 * @author daniel.meyer@camunda.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Interceptors {

  Class< ? extends Interceptor>[] value();

}
