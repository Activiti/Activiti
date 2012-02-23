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


import org.activiti.spring.components.ActivitiContextUtils;
import org.activiti.spring.components.aop.ProcessStartAnnotationBeanPostProcessor;
import org.activiti.spring.components.scope.ProcessScope;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.Conventions;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * registers support for handling the annotations in the org.activiti.engine.annotations package.
 * <p/>
 * The first major component is the state handlers. For this to work, a BeanFactoryPostProcessor is registered which in turn registers a
 * {@link org.activiti.spring.components.registry.ActivitiStateHandlerRegistry} if none exists.
 *
 * @author Josh Long
 * @since 5.3
 */
public class ActivitiAnnotationDrivenBeanDefinitionParser implements BeanDefinitionParser {

	private final String processEngineAttribute = "process-engine";

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		registerProcessScope(element, parserContext);
		registerStateHandlerAnnotationBeanFactoryPostProcessor(element, parserContext);
		registerProcessStartAnnotationBeanPostProcessor(element, parserContext);
		return null;
	}

	private void configureProcessEngine(AbstractBeanDefinition abstractBeanDefinition, Element element) {
		String procEngineRef = element.getAttribute(processEngineAttribute);
		if (StringUtils.hasText(procEngineRef))
			abstractBeanDefinition.getPropertyValues().add(Conventions.attributeNameToPropertyName(processEngineAttribute), new RuntimeBeanReference(procEngineRef));
	}

	private void registerStateHandlerAnnotationBeanFactoryPostProcessor(Element element, ParserContext context) {
		Class clz = StateHandlerAnnotationBeanFactoryPostProcessor.class;
		BeanDefinitionBuilder postProcessorBuilder = BeanDefinitionBuilder.genericBeanDefinition(clz.getName());

		BeanDefinitionHolder postProcessorHolder = new BeanDefinitionHolder(
				postProcessorBuilder.getBeanDefinition(),
				ActivitiContextUtils.ANNOTATION_STATE_HANDLER_BEAN_FACTORY_POST_PROCESSOR_BEAN_NAME);
		configureProcessEngine(postProcessorBuilder.getBeanDefinition(), element);
		BeanDefinitionReaderUtils.registerBeanDefinition(postProcessorHolder, context.getRegistry());

	}

	private void registerProcessScope(Element element, ParserContext parserContext) {
		Class clz = ProcessScope.class;
		BeanDefinitionBuilder processScopeBDBuilder = BeanDefinitionBuilder.genericBeanDefinition(clz);
		AbstractBeanDefinition scopeBeanDefinition = processScopeBDBuilder.getBeanDefinition();
		scopeBeanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		configureProcessEngine(scopeBeanDefinition, element);
		String beanName = baseBeanName(clz);
		parserContext.getRegistry().registerBeanDefinition(beanName, scopeBeanDefinition);
	}

	private void registerProcessStartAnnotationBeanPostProcessor(Element element, ParserContext parserContext) {
		Class clz = ProcessStartAnnotationBeanPostProcessor.class;

		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clz);
		AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
		beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		configureProcessEngine(beanDefinition, element);

		String beanName = baseBeanName(clz);
		parserContext.getRegistry().registerBeanDefinition(beanName, beanDefinition);
	}

	private String baseBeanName(Class cl) {
		return cl.getName().toLowerCase();
	}
}

