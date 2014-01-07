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


import org.activiti.spring.components.support.ProcessScopeBeanFactoryPostProcessor;
import org.activiti.spring.components.support.ProcessStartingBeanPostProcessor;
import org.activiti.spring.components.support.SharedProcessInstanceFactoryBean;
import org.activiti.spring.components.support.SharedProcessInstanceHolder;
import org.activiti.spring.components.support.util.BeanDefinitionUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.Conventions;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Registers support for handling the annotations in the {@code org.activiti.engine.annotations} package.
 * <p/>
 * Registers a {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor}
 * that in turn registers a  {@link org.activiti.spring.components.registry.StateHandlerRegistry},
 * if none exist.
 *
 * @author Josh Long
 * @since 5.3
 */
public class AnnotationDrivenBeanDefinitionParser implements BeanDefinitionParser {

    private final String processEngineAttribute = "process-engine";

    private RuntimeBeanReference processEngineRuntimeBeanReference;

    private RuntimeBeanReference sharedProcessInstanceHolderRuntimeBeanReference;

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        // process-engine
        String procEngineRef = element.getAttribute(processEngineAttribute);
        Assert.hasText(procEngineRef, "you must specify a process-engine attribute");
        this.processEngineRuntimeBeanReference = new RuntimeBeanReference(procEngineRef);

        // shared process instance holder
        this.sharedProcessInstanceHolderRuntimeBeanReference = registerSharedProcessInstanceHolder(element, parserContext);

        registerSharedProcessInstanceFactoryBean(element, parserContext);
        registerProcessScopeBeanFactoryPostProcessor(element, parserContext);
        registerProcessStartingBeanPostProcessor(element, parserContext);

        return null;
    }

    private BeanDefinitionBuilder build(Class<?> clz) {
        return BeanDefinitionBuilder.genericBeanDefinition(clz)
                .setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
    }

    private RuntimeBeanReference registerSharedProcessInstanceFactoryBean(Element element, ParserContext parserContext) {

        BeanDefinition sharedProcessInstanceFactoryBeanBeanDefinition =
                build(SharedProcessInstanceFactoryBean.class)
                        .addConstructorArgReference(this.sharedProcessInstanceHolderRuntimeBeanReference.getBeanName())
                        .getBeanDefinition();

        String sharedProcessInstanceHolderBeanDefinitionName = parserContext.getReaderContext().registerWithGeneratedName(
                sharedProcessInstanceFactoryBeanBeanDefinition);

        return new RuntimeBeanReference(sharedProcessInstanceHolderBeanDefinitionName);
    }

    private RuntimeBeanReference registerSharedProcessInstanceHolder(Element element, ParserContext parserContext) {
        BeanDefinition sharedProcessInstanceHolderBeanDefinition = build(SharedProcessInstanceHolder.class).getBeanDefinition();
        String sharedProcessInstanceHolderBeanDefinitionName =
                parserContext.getReaderContext().registerWithGeneratedName(
                        sharedProcessInstanceHolderBeanDefinition);
        return new RuntimeBeanReference(sharedProcessInstanceHolderBeanDefinitionName);
    }

    private void registerProcessStartingBeanPostProcessor(Element element, ParserContext parserContext) {
        Class<?> startingBeanPostProcessorClass = ProcessStartingBeanPostProcessor.class;

        BeanDefinition processStartingBPP = build(startingBeanPostProcessorClass)
                .addConstructorArgReference(this.processEngineRuntimeBeanReference.getBeanName())
                .addConstructorArgReference(this.sharedProcessInstanceHolderRuntimeBeanReference.getBeanName())
                .getBeanDefinition();

        parserContext.getRegistry().registerBeanDefinition(
                startingBeanPostProcessorClass.getName(), processStartingBPP);
    }

    private void registerProcessScopeBeanFactoryPostProcessor(Element element, ParserContext parserContext) {

        Class<?> processScopeBeanFactoryPostProcessorClass = ProcessScopeBeanFactoryPostProcessor.class;

        BeanDefinition processStartingBPPBeanDefinition = build(processScopeBeanFactoryPostProcessorClass)
                .addConstructorArgReference(  this.processEngineRuntimeBeanReference.getBeanName())
                .getBeanDefinition();

        parserContext.getRegistry().registerBeanDefinition(
                processScopeBeanFactoryPostProcessorClass.getName(),
                processStartingBPPBeanDefinition);

    }
}

