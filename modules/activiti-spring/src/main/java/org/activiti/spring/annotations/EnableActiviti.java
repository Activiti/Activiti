package org.activiti.spring.annotations;

import org.activiti.spring.components.config.annotations.EnableActivitiImportSelector;
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
@Import(EnableActivitiImportSelector.class)
public @interface EnableActiviti {
}
