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
package org.activiti.spring.components.aop.util;

import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanDefinitionVisitor;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.util.StringValueResolver;

/**
 * this class was copied wholesale from Spring 3.1's RefreshScope, which Dave Syer wrote.
 *
 * @author Dave Syer
 */
public class Scopifier extends BeanDefinitionVisitor {

	private final boolean proxyTargetClass;

	private final BeanDefinitionRegistry registry;

	private final String scope;

	private final boolean scoped;

	public static BeanDefinitionHolder createScopedProxy(String beanName, BeanDefinition definition, BeanDefinitionRegistry registry, boolean proxyTargetClass) {
		BeanDefinitionHolder proxyHolder = ScopedProxyUtils.createScopedProxy(new BeanDefinitionHolder(definition, beanName), registry, proxyTargetClass);
		registry.registerBeanDefinition(beanName, proxyHolder.getBeanDefinition());
		return proxyHolder;
	}

	public Scopifier(BeanDefinitionRegistry registry, String scope, boolean proxyTargetClass, boolean scoped) {
		super(new StringValueResolver() {
			public String resolveStringValue(String value) {
				return value;
			}
		});
		this.registry = registry;
		this.proxyTargetClass = proxyTargetClass;
		this.scope = scope;
		this.scoped = scoped;
	}

	@Override
	protected Object resolveValue(Object value) {

		BeanDefinition definition = null;
		String beanName = null;
		if (value instanceof BeanDefinition) {
			definition = (BeanDefinition) value;
			beanName = BeanDefinitionReaderUtils.generateBeanName(definition, registry);
		} else if (value instanceof BeanDefinitionHolder) {
			BeanDefinitionHolder holder = (BeanDefinitionHolder) value;
			definition = holder.getBeanDefinition();
			beanName = holder.getBeanName();
		}

		if (definition != null) {
			boolean nestedScoped = scope.equals(definition.getScope());
			boolean scopeChangeRequiresProxy = !scoped && nestedScoped;
			if (scopeChangeRequiresProxy) {
				// Exit here so that nested inner bean definitions are not
				// analysed
				return createScopedProxy(beanName, definition, registry, proxyTargetClass);
			}
		}

		// Nested inner bean definitions are recursively analysed here
		value = super.resolveValue(value);
		return value;
	}
}
