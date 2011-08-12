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
package org.activiti.spring.components.aop;

import org.activiti.engine.ProcessEngine;
import org.activiti.spring.annotations.ProcessVariable;
import org.activiti.spring.annotations.StartProcess;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.ProxyConfig;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ClassUtils;

/**
 * Proxies beans with methods annotated with {@link StartProcess}.
 * If the method is invoked successfully, the process described by the annotaton is created.
 * Parameters passed to the method annotated with {@link ProcessVariable}
 * are passed to the business process.
 *
 * @author Josh Long
 * @since 5,3
 */
public class ProcessStartAnnotationBeanPostProcessor extends ProxyConfig implements BeanPostProcessor, InitializingBean {

	private Log log = LogFactory.getLog(getClass()) ;

	/**
	 * the process engine as created by a {@link org.activiti.spring.ProcessEngineFactoryBean}
	 */
	private ProcessEngine processEngine;

	private ProcessStartingPointcutAdvisor advisor;

	private volatile ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	public void setProcessEngine(ProcessEngine processEngine) {
		this.processEngine = processEngine;
	}

	public void afterPropertiesSet() throws Exception {
		this.advisor = new ProcessStartingPointcutAdvisor(this.processEngine);
	}

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
	 	if (bean instanceof AopInfrastructureBean) {
			// Ignore AOP infrastructure such as scoped proxies.
			return bean;
		}
		Class<?> targetClass = AopUtils.getTargetClass(bean);
		if (AopUtils.canApply(this.advisor, targetClass)) {
			if (bean instanceof Advised) {
				((Advised) bean).addAdvisor(0, this.advisor);
				return bean;
			}
			else {
				ProxyFactory proxyFactory = new ProxyFactory(bean);
				// Copy our properties (proxyTargetClass etc) inherited from ProxyConfig.
				proxyFactory.copyFrom(this);
				proxyFactory.addAdvisor(this.advisor);
				return proxyFactory.getProxy(this.beanClassLoader);
			}
		}
		else {
			// No async proxy needed.
			return bean;
		}
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
}
