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
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.spring.components.support.util.Scopifier;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.scope.ScopedObject;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p/>
 * Binds variables to a currently executing Activiti business process
 * (specifically, to the {@link org.activiti.engine.runtime.ProcessInstance process instance}).
 * <p/>
 * <strong>NB</strong>: Parts of this code are lifted wholesale from
 * <A href="https://twitter.com/david_syer">Dr. Syer's</A> {@code RefreshScope} implementation
 * <A href="https://jira.springsource.org/browse/SPR-8075?page=com.atlassian.jira.plugin.system.issuetabpanels:all-tabpanel">
 * from this JIRA ticket</a>.
 *
 * @author Josh Long
 * @since 5.3
 */
public class ProcessScopeBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    public final static String PROCESS_SCOPE_PROCESS_VARIABLES_SINGLETON = "processVariables";

    public final static String PROCESS_SCOPE_NAME = "process";

    private final String oneAndOnlyOneMessage =
            String.format("there should be one and only one %s process-engine in the context.", ProcessEngine.class.getName());

    private Map<String, Object> processVariablesMap =
        new ConcurrentHashMap<String, Object>() {
            @Override
            public Object get(java.lang.Object o) {

                Assert.isInstanceOf(String.class, o, "the 'key' must be a String");

                String varName = (String) o;

                ProcessInstance processInstance = Context.getExecutionContext().getProcessInstance();
                ExecutionEntity executionEntity = (ExecutionEntity) processInstance;
                if (executionEntity.getVariableNames().contains(varName)) {
                    return executionEntity.getVariable(varName);
                }

                throw new RuntimeException(
                        String.format("no processVariable by the name of '%s' is available!", varName));
            }
        };

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        String[] processEngineBeanNames = beanFactory.getBeanNamesForType(ProcessEngine.class);
        Assert.isTrue(processEngineBeanNames.length == 1, oneAndOnlyOneMessage);
        RuntimeBeanReference processEngineRuntimeBeanReference = new RuntimeBeanReference(processEngineBeanNames[0]);
        String processScopeName = PROCESS_SCOPE_NAME;
        Assert.isInstanceOf(BeanDefinitionRegistry.class, beanFactory,
                "BeanFactory was not a BeanDefinitionRegistry, so " + ProcessScopeBeanFactoryPostProcessor.class.getName() + " cannot be used.");
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        final boolean proxyTargetClass = true;

        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
            boolean scoped = processScopeName.equals(definition.getScope());
            Scopifier scopifier = new Scopifier(registry, processScopeName, proxyTargetClass, scoped);
            scopifier.visitBeanDefinition(definition);
            if (scoped) {
                Scopifier.createScopedProxy(beanName, definition, registry, proxyTargetClass);
            }
        }
        registry.registerBeanDefinition(processScopeName,
                BeanDefinitionBuilder.genericBeanDefinition(ProcessScope.class)
                        .addConstructorArgReference(processEngineRuntimeBeanReference.getBeanName())
                        .getBeanDefinition());
        beanFactory.registerSingleton(PROCESS_SCOPE_PROCESS_VARIABLES_SINGLETON, this.processVariablesMap);
    }

}

class ProcessScope implements Scope, InitializingBean, DisposableBean, BeanFactoryAware {
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof ConfigurableListableBeanFactory) {
            ConfigurableListableBeanFactory configurableApplicationContext =
                    (ConfigurableListableBeanFactory) beanFactory;
            configurableApplicationContext.registerScope(ProcessScopeBeanFactoryPostProcessor.PROCESS_SCOPE_NAME, this);
        }
    }

    private ClassLoader classLoader = ClassUtils.getDefaultClassLoader();

    private Logger logger = LoggerFactory.getLogger(getClass());

    private ProcessEngine processEngine;

    private RuntimeService runtimeService;

    public ProcessScope() {
    }

    public ProcessScope(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    // set through Namespace reflection if nothing else
    @SuppressWarnings("unused")
    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    public Object get(String name, ObjectFactory<?> objectFactory) {

        ExecutionEntity executionEntity = null;
        try {
            logger.debug("returning scoped object having beanName '{}' for conversation ID '{}'.", name, this.getConversationId());

            ProcessInstance processInstance = Context.getExecutionContext().getProcessInstance();
            executionEntity = (ExecutionEntity) processInstance;

            Object scopedObject = executionEntity.getVariable(name);
            if (scopedObject == null) {
                scopedObject = objectFactory.getObject();
                if (scopedObject instanceof ScopedObject) {
                    ScopedObject sc = (ScopedObject) scopedObject;
                    scopedObject = sc.getTargetObject();
                    logger.debug("de-referencing {}#targetObject before persisting variable", ScopedObject.class.getName());
                }
                persistVariable(name, scopedObject);
            }
            return createDirtyCheckingProxy(name, scopedObject);
        } catch (Exception th) {
            logger.warn("couldn't return value from process scope! {}", ExceptionUtils.getStackTrace(th));
        } finally {
            if (executionEntity != null) {
                logger.debug("set variable '{}' on executionEntity#{}", name, executionEntity.getId());
            }
        }
        return null;
    }

    public void registerDestructionCallback(String name, Runnable callback) {
        logger.debug("no support for registering descruction callbacks implemented currently. registerDestructionCallback('{}',callback) will do nothing.", name);
    }

    private String getExecutionId() {
        return Context.getExecutionContext().getExecution().getId();
    }

    public Object remove(String name) {

        logger.debug("remove '{}'", name);
        return runtimeService.getVariable(getExecutionId(), name);
    }

    public Object resolveContextualObject(String key) {

        if ("executionId".equalsIgnoreCase(key))
            return Context.getExecutionContext().getExecution().getId();

        if ("processInstance".equalsIgnoreCase(key))
            return Context.getExecutionContext().getProcessInstance();

        if ("processInstanceId".equalsIgnoreCase(key))
            return Context.getExecutionContext().getProcessInstance().getId();

        return null;
    }

    public String getConversationId() {
        return getExecutionId();
    }

    private final ConcurrentHashMap<String, Object> processVariablesMap = new ConcurrentHashMap<String, Object>() {
        @Override
        public java.lang.Object get(java.lang.Object o) {

            Assert.isInstanceOf(String.class, o, "the 'key' must be a String");

            String varName = (String) o;

            ProcessInstance processInstance = Context.getExecutionContext().getProcessInstance();
            ExecutionEntity executionEntity = (ExecutionEntity) processInstance;
            if (executionEntity.getVariableNames().contains(varName)) {
                return executionEntity.getVariable(varName);
            }
            throw new RuntimeException("no processVariable by the name of '" + varName + "' is available!");
        }
    };


    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.processEngine, "the 'processEngine' must not be null!");
        this.runtimeService = this.processEngine.getRuntimeService();
    }

    protected Object createDirtyCheckingProxy(final String name, final Object scopedObject) {
        ProxyFactory proxyFactoryBean = new ProxyFactory(scopedObject);
        proxyFactoryBean.setProxyTargetClass(true);
        proxyFactoryBean.addAdvice(new MethodInterceptor() {
            public Object invoke(MethodInvocation methodInvocation) throws Throwable {
                Object result = methodInvocation.proceed();
                persistVariable(name, scopedObject);
                return result;
            }
        });
        return proxyFactoryBean.getProxy(this.classLoader);
    }

    protected void persistVariable(String variableName, Object scopedObject) {
        ProcessInstance processInstance = Context.getExecutionContext().getProcessInstance();
        ExecutionEntity executionEntity = (ExecutionEntity) processInstance;
        Assert.isTrue(scopedObject instanceof Serializable, "the scopedObject is not " + Serializable.class.getName() + "!");
        executionEntity.setVariable(variableName, scopedObject);
    }

    @Override
    public void destroy() throws Exception {
        logger.debug("destroy() on " + getClass().getName());
    }

}