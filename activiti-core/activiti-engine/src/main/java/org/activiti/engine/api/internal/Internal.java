package org.activiti.engine.api.internal;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE;

/*
 * In Activiti 7  and Activiti Cloud we discourage the use of @Internal marked classes. These classes are
 * internal services and we recommend to use the new API interfaces located under "org.activiti.api"
 * Internal services might be deprecated or radically changed in further releases of Activiti 7 and beyond.
 * We recommend to get in touch via http://github.com/Activiti/Activiti/issues if you want to expand the capabilities of
 * the external/public APIs
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value={CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE})
public @interface Internal {
}
