package org.activiti.spring.components.config.annotation;

import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.TransactionManagementConfigurationSelector;

import java.lang.annotation.*;

/**
 * Annotation used to enable Activiti in much the same way as the
 * <activiti:annotation-driven/> support does.
 *
 *
 *
 * @author Josh Long
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(TransactionManagementConfigurationSelector.class)
public @interface EnableActiviti {
}
