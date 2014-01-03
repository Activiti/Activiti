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
package org.activiti.spring.components.config;

import org.activiti.engine.ProcessEngine;
import org.activiti.spring.components.ActivitiContextUtils;
import org.activiti.spring.components.registry.StateHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class StateHandlerAnnotationBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

     private Logger log = LoggerFactory.getLogger(getClass());


    public StateHandlerAnnotationBeanFactoryPostProcessor() {
    }
/*

    public StateHandlerAnnotationBeanFactoryPostProcessor(ProcessEngine pe) {
        setProcessEngine(pe);
    }
*/


    private String processEngineBeanName;

    public void setProcessEngineBeanName(String beanName) {
        this.processEngineBeanName = beanName;
    }

    private BeanDefinition beanDefinition(ConfigurableListableBeanFactory configurableListableBeanFactory,
                                          String beanName, Class<?> type) {


        String[] beanNames = configurableListableBeanFactory.getBeanNamesForType(type, true, true);

        Assert.isTrue(beanNames.length > 0, "there must be at least one ProcessEngine");

        String beanIdToReturn = null;

        // case 1: theyve specified a beanName that matches
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
      //  Assert.isTrue(beanIdToReturn != null, "please ensure there is either only one ProcessEngine in the context or that it's disambiguated using the processEngineBeanName property");
        return beanIdToReturn == null ? null :  configurableListableBeanFactory.getBeanDefinition(beanIdToReturn);
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        String activitiBeanRegistryBeanName = ActivitiContextUtils.ACTIVITI_REGISTRY_BEAN_NAME;

        if (beanFactory instanceof BeanDefinitionRegistry) {
            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

            BeanDefinition beanDefinition = beanDefinition( beanFactory, activitiBeanRegistryBeanName, StateHandlerRegistry.class );
            if(null == beanDefinition){

                String registryClassName = StateHandlerRegistry.class.getName();
                log.info("registering a {} instance under bean name {}.", registryClassName, activitiBeanRegistryBeanName);


          //      String[] processEngineBeanNames = beanFactory.getBeanNamesForType(ProcessEngine.class);

                BeanDefinition processEngineBeanDefinition = beanDefinition( beanFactory,  "processEngine", ProcessEngine.class);


                RootBeanDefinition rootBeanDefinition = new RootBeanDefinition();
                rootBeanDefinition.setBeanClassName(registryClassName);
                rootBeanDefinition.getPropertyValues().addPropertyValue("processEngine",  processEngineBeanDefinition);

                BeanDefinitionHolder holder = new BeanDefinitionHolder(rootBeanDefinition, activitiBeanRegistryBeanName);
                BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
            }

        } else {
            log.info("BeanFactory is not a BeanDefinitionRegistry. " +
                    "The default '{}' cannot be configured.", activitiBeanRegistryBeanName);
        }
    }

    private boolean beanAlreadyConfigured(BeanDefinitionRegistry registry, String beanName, Class clz) {
        if (registry.isBeanNameInUse(beanName)) {
            BeanDefinition bDef = registry.getBeanDefinition(beanName);
            if (bDef.getBeanClassName().equals(clz.getName())) {
                return true; // so the beans already registered, and of the right type. so we assume the user is overriding our configuration
            } else {
                throw new IllegalStateException("The bean name '" + beanName + "' is reserved.");
            }
        }
        return false;
    }
}
