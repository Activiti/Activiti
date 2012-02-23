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
package org.activiti.spring.components.registry;


import org.apache.commons.lang.builder.ToStringBuilder;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * an instance of a bean discovered to both have an {@link org.activiti.engine.annotations.ActivitiComponent}
 * and one or more {@link org.activiti.engine.annotations.ActivitiComponent} annotations present.
 * <p/>
 * Describes the metadata extracted from the bean at configuration time
 *
 * @author Josh Long
 * @since 1.0
 */
public class ActivitiStateHandlerRegistration {
	private Map<Integer, String> processVariablesExpected = new ConcurrentHashMap<Integer, String>();
	private Method handlerMethod;
	private Object handler;
	private String stateName;
	private String beanName;
	private int processVariablesIndex = -1;
	private int processIdIndex = -1;
	private String processName;

	public ActivitiStateHandlerRegistration(
			Map<Integer, String> processVariablesExpected, Method handlerMethod,
			Object handler, String stateName, String beanName,
			int processVariablesIndex, int processIdIndex, String processName) {
		this.processVariablesExpected = processVariablesExpected;
		this.handlerMethod = handlerMethod;
		this.handler = handler;
		this.stateName = stateName;
		this.beanName = beanName;
		this.processVariablesIndex = processVariablesIndex;
		this.processIdIndex = processIdIndex;
		this.processName = processName;
	}

	public int getProcessVariablesIndex() {
		return processVariablesIndex;
	}

	public int getProcessIdIndex() {
		return processIdIndex;
	}

	public boolean requiresProcessId() {
		return this.processIdIndex > -1;
	}

	public boolean requiresProcessVariablesMap() {
		return processVariablesIndex > -1;
	}

	public String getBeanName() {
		return beanName;
	}

	public Map<Integer, String> getProcessVariablesExpected() {
		return processVariablesExpected;
	}

	public Method getHandlerMethod() {
		return handlerMethod;
	}

	public Object getHandler() {
		return handler;
	}

	public String getStateName() {
		return stateName;
	}

	public String getProcessName() {
		return processName;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
