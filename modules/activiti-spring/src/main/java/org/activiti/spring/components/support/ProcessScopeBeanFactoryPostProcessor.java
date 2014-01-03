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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.spring.components.support.util.BeanDefinitionUtils;
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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * binds variables to a currently executing Activiti business process (a {@link org.activiti.engine.runtime.ProcessInstance}).
 * <p/>
 * Parts of this code are lifted wholesale from Dave Syer's work on the Spring 3.1 RefreshScope.
 *
 * @author Josh Long
 * @since 5.3
 */
public class ProcessScopeBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    /**
     * Map of the {@code processVariables}. Supports correct, scoped access to process variables so that
     * <p/>
     * {@code [at]Value("#{ processVariables['customerId'] }") long customerId; }
     */
    public final static String PROCESS_SCOPE_PROCESS_VARIABLES_SINGLETON = "processVariables";
    public final static String PROCESS_SCOPE_NAME = "process";

    private ClassLoader classLoader = ClassUtils.getDefaultClassLoader();

    private boolean proxyTargetClass = true;

    private Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * creates a proxy that dispatches invocations to the currently bound {@link ProcessInstance}
     *
     * @return shareable {@link ProcessInstance}
     */
    private Object createSharedProcessInstance() {
        ProxyFactory proxyFactoryBean = new ProxyFactory(ProcessInstance.class, new MethodInterceptor() {
            public Object invoke(MethodInvocation methodInvocation) throws Throwable {
                String methodName = methodInvocation.getMethod().getName();
                logger.info("method invocation for {}.", methodName);
                if (methodName.equals("toString")){
                    return "SharedProcessInstance";
                }
                ProcessInstance processInstance = Context.getExecutionContext().getProcessInstance();
                Method method = methodInvocation.getMethod();
                Object[] args = methodInvocation.getArguments();
                return method.invoke(processInstance, args);
            }
        });
        return proxyFactoryBean.getProxy(this.classLoader);
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

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        Assert.isInstanceOf(BeanDefinitionRegistry.class, beanFactory,
                "BeanFactory was not a BeanDefinitionRegistry, so ProcessScopeBeanFactoryPostProcessor cannot be used.");

        final String processScopeName = PROCESS_SCOPE_NAME;

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

        BeanDefinition processEngineBeanDefinition = BeanDefinitionUtils.beanDefinition(beanFactory, "processEngine", ProcessEngine.class);

        RootBeanDefinition processScopeRootBeanDefinition = new RootBeanDefinition(ProcessScope.class);
        processScopeRootBeanDefinition.getPropertyValues().add("processEngine", processEngineBeanDefinition);

        registry.registerBeanDefinition(processScopeName, processScopeRootBeanDefinition);

        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
            // Replace this or any of its inner beans with scoped proxy if it has this scope
            boolean scoped = processScopeName.equals(definition.getScope());
            Scopifier scopifier = new Scopifier(registry, processScopeName, proxyTargetClass, scoped);
            scopifier.visitBeanDefinition(definition);
            if (scoped) {
                Scopifier.createScopedProxy(beanName, definition, registry, proxyTargetClass);
            }
        }

        beanFactory.registerSingleton(PROCESS_SCOPE_PROCESS_VARIABLES_SINGLETON, this.processVariablesMap);
        beanFactory.registerResolvableDependency(ProcessInstance.class, createSharedProcessInstance());
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

    private boolean proxyTargetClass = true;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private ProcessEngine processEngine;

    private RuntimeService runtimeService;

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
        } catch (Throwable th) {
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

    private Object createDirtyCheckingProxy(final String name, final Object scopedObject) throws Throwable {
        ProxyFactory proxyFactoryBean = new ProxyFactory(scopedObject);
        proxyFactoryBean.setProxyTargetClass(this.proxyTargetClass);
        proxyFactoryBean.addAdvice(new MethodInterceptor() {
            public Object invoke(MethodInvocation methodInvocation) throws Throwable {
                Object result = methodInvocation.proceed();
                persistVariable(name, scopedObject);
                return result;
            }
        });
        return proxyFactoryBean.getProxy(this.classLoader);
    }

    private void persistVariable(String variableName, Object scopedObject) {
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