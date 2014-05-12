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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.Assert;
import org.w3c.dom.Element;

/**
 * Registers the {@code process} scope and associated expression-language
 * variable, {@code processVariables}.
 * 
 * @author Josh Long
 * @see org.activiti.spring.annotations.EnableActiviti for Java
 *      configuration-centric alternative.
 * @see org.activiti.engine.ProcessEngine
 * @since 5.3
 */
public class AnnotationDrivenBeanDefinitionParser implements
    BeanDefinitionParser {

	private RuntimeBeanReference processEngineRuntimeBeanReference;

	private RuntimeBeanReference sharedProcessInstanceHolderRuntimeBeanReference;

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		// process-engine
		String processEngineAttribute = "process-engine";
		String procEngineRef = element.getAttribute(processEngineAttribute);
		Assert
		    .hasText(procEngineRef, "you must specify a process-engine attribute");
		this.processEngineRuntimeBeanReference = new RuntimeBeanReference(
		    procEngineRef);

		// shared process instance holder
		// this.sharedProcessInstanceHolderRuntimeBeanReference =
		// registerSharedProcessInstanceHolder(element, parserContext);
		//
		// registerSharedProcessInstanceFactoryBean(element, parserContext);
		// registerProcessScopeBeanFactoryPostProcessor(element, parserContext);

		return null;
	}

	private BeanDefinitionBuilder build(Class<?> clz) {
		return BeanDefinitionBuilder.genericBeanDefinition(clz).setRole(
		    BeanDefinition.ROLE_INFRASTRUCTURE);
	}
	/*
	 * private RuntimeBeanReference
	 * registerSharedProcessInstanceFactoryBean(Element element, ParserContext
	 * parserContext) { BeanDefinition
	 * sharedProcessInstanceFactoryBeanBeanDefinition =
	 * build(SharedProcessInstanceFactoryBean.class)
	 * .addConstructorArgReference(this
	 * .sharedProcessInstanceHolderRuntimeBeanReference.getBeanName())
	 * .getBeanDefinition(); String sharedProcessInstanceHolderBeanDefinitionName
	 * = parserContext.getReaderContext().registerWithGeneratedName(
	 * sharedProcessInstanceFactoryBeanBeanDefinition); return new
	 * RuntimeBeanReference(sharedProcessInstanceHolderBeanDefinitionName); }
	 * 
	 * private RuntimeBeanReference registerSharedProcessInstanceHolder(Element
	 * element, ParserContext parserContext) { BeanDefinition
	 * sharedProcessInstanceHolderBeanDefinition =
	 * build(SharedProcessInstanceHolder.class).getBeanDefinition(); String
	 * sharedProcessInstanceHolderBeanDefinitionName =
	 * parserContext.getReaderContext
	 * ().registerWithGeneratedName(sharedProcessInstanceHolderBeanDefinition);
	 * return new
	 * RuntimeBeanReference(sharedProcessInstanceHolderBeanDefinitionName); }
	 * 
	 * private void registerProcessScopeBeanFactoryPostProcessor(Element element,
	 * ParserContext parserContext) { Class<?>
	 * processScopeBeanFactoryPostProcessorClass =
	 * ProcessScopeBeanFactoryPostProcessor.class; BeanDefinition
	 * processStartingBPPBeanDefinition =
	 * build(processScopeBeanFactoryPostProcessorClass) //
	 * .addConstructorArgReference
	 * (this.processEngineRuntimeBeanReference.getBeanName())
	 * .getBeanDefinition(); parserContext.getRegistry().registerBeanDefinition(
	 * processScopeBeanFactoryPostProcessorClass.getName(),
	 * processStartingBPPBeanDefinition); }
	 */
}
