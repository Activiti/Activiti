package org.activiti.spring.annotations;

import org.activiti.spring.components.config.annotations.ActivitiConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Josh Long
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ActivitiConfiguration.class)
public @interface EnableActiviti {
}
