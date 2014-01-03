package org.activiti.spring.components.support.util;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 *
 */
public abstract class BeanDefinitionUtils {
    public static BeanDefinition beanDefinition(ConfigurableListableBeanFactory configurableListableBeanFactory,
                                                String beanName, Class<?> type) {


        String[] beanNames = configurableListableBeanFactory.getBeanNamesForType(type, true, true);

        Assert.isTrue(beanNames.length > 0, "there must be at least one ProcessEngine");

        String beanIdToReturn = null;

        if (StringUtils.hasText(beanName)) {
            for (String b : beanNames)
                if (b.equals(beanName)) {
                    beanIdToReturn = b;
                }
        } else {
            if (beanNames.length == 1) {
                beanIdToReturn = beanNames[0];
            }
        }
        return beanIdToReturn == null ? null : configurableListableBeanFactory.getBeanDefinition(beanIdToReturn);
    }

}
