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
package org.activiti.spring.components.config.xml;

import org.activiti.engine.ProcessEngine;
import org.activiti.spring.components.ActivitiContextUtils;
import org.activiti.spring.components.registry.ActivitiStateHandlerRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.util.logging.Logger;

/**
 * this class is responsible for registering the other {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor}s
 * and {@link BeanFactoryPostProcessor}s.
 * <p/>
 * Particularly, this will register the {@link ActivitiStateHandlerRegistry} which is used to react to states.
 *
 * @author Josh Long
 */
public class StateHandlerAnnotationBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	private ProcessEngine processEngine ;
	private Logger log = Logger.getLogger(getClass().getName());

	public void setProcessEngine(ProcessEngine processEngine) {
		this.processEngine = processEngine;
	}

	private void configureDefaultActivitiRegistry(String registryBeanName, BeanDefinitionRegistry registry) {


		if (!beanAlreadyConfigured(registry, registryBeanName, ActivitiStateHandlerRegistry.class)) {
			String registryName =ActivitiStateHandlerRegistry.class.getName();
			log.info( "registering a " + registryName + " instance under bean name "+ ActivitiContextUtils.ACTIVITI_REGISTRY_BEAN_NAME+ ".");

			RootBeanDefinition rootBeanDefinition = new RootBeanDefinition();
			rootBeanDefinition.setBeanClassName( registryName );
			rootBeanDefinition.getPropertyValues().addPropertyValue("processEngine", this.processEngine);

			BeanDefinitionHolder holder = new BeanDefinitionHolder(rootBeanDefinition, registryBeanName);
			BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
		}
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			configureDefaultActivitiRegistry(ActivitiContextUtils.ACTIVITI_REGISTRY_BEAN_NAME, registry);


		} else {
			log.info("BeanFactory is not a BeanDefinitionRegistry. The default '"
					+ ActivitiContextUtils.ACTIVITI_REGISTRY_BEAN_NAME + "' cannot be configured.");
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
