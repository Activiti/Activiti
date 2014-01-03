/*
 * Copyright 2010 the original author or authors
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.activiti.spring.components.support;

import org.activiti.engine.ProcessEngine;
import org.activiti.spring.components.ActivitiContextUtils;
import org.activiti.spring.components.registry.StateHandlerRegistry;
import org.activiti.spring.components.support.util.BeanDefinitionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;


/**
 * this class is responsible for registering the other {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor}s
 * and {@link BeanFactoryPostProcessor}s.
 * <p/>
 * Particularly, this will register the {@link org.activiti.spring.components.registry.StateHandlerRegistry} which is used to react to states.
 *
 * @author Josh Long
 */
public class StateHandlerBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private Logger log = LoggerFactory.getLogger(getClass());



    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        String activitiBeanRegistryBeanName = ActivitiContextUtils.ACTIVITI_REGISTRY_BEAN_NAME;

        if (beanFactory instanceof BeanDefinitionRegistry) {
            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;


            BeanDefinition beanDefinition = BeanDefinitionUtils.beanDefinition(beanFactory, activitiBeanRegistryBeanName, StateHandlerRegistry.class);
            if (null == beanDefinition) {

                String registryClassName = StateHandlerRegistry.class.getName();
                log.info("registering a {} instance under bean name {}.", registryClassName, activitiBeanRegistryBeanName);


                BeanDefinition processEngineBeanDefinition = BeanDefinitionUtils.beanDefinition(beanFactory, "processEngine", ProcessEngine.class);


                RootBeanDefinition rootBeanDefinition = new RootBeanDefinition();
                rootBeanDefinition.setBeanClassName(registryClassName);
                rootBeanDefinition.getPropertyValues().addPropertyValue("processEngine", processEngineBeanDefinition);

                BeanDefinitionHolder holder = new BeanDefinitionHolder(rootBeanDefinition, activitiBeanRegistryBeanName);
                BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
            }

        } else {
            log.info("BeanFactory is not a BeanDefinitionRegistry. " +
                    "The default '{}' cannot be configured.", activitiBeanRegistryBeanName);
        }
    }


}
