package org.activiti.spring.components.support.util;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 *
 */
public abstract class BeanDefinitionUtils {

    public static BeanDefinition findBeanDefinitionByBeanNameOrType(ConfigurableListableBeanFactory configurableListableBeanFactory,
                                                                    String possibleBeanName, Class<?> type) {
        String[] existingBeans = configurableListableBeanFactory.getBeanNamesForType(type, true, true);
        Assert.isTrue(existingBeans.length > 0, "there must be at least one bean for the type "+ type);
        String beanIdToReturn = null;

        if (StringUtils.hasText(possibleBeanName)) {
            for (String existingBean : existingBeans) {
                if (existingBean.equals(possibleBeanName)) {
                    beanIdToReturn = existingBean;
                }
            }
        }
        else {

            if (existingBeans.length == 1) {
                beanIdToReturn = existingBeans[0];
            }
        }
        return beanIdToReturn == null ? null : configurableListableBeanFactory.getBeanDefinition(beanIdToReturn);
    }

}
