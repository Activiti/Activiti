package org.activiti.cycle.impl.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark classes defining a plugin
 * 
 * @author ruecker
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface ActivitiCyclePlugin {

}
