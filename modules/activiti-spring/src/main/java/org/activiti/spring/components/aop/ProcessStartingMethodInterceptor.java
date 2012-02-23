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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.spring.annotations.BusinessKey;
import org.activiti.spring.annotations.ProcessVariable;
import org.activiti.spring.annotations.StartProcess;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link org.aopalliance.intercept.MethodInterceptor} that starts a business process
 * as a result of a successful method invocation.
 *
 * @author Josh Long
 */
public class ProcessStartingMethodInterceptor implements MethodInterceptor {

	private Logger log = Logger.getLogger(getClass().getName());

	/**
	 * injected reference - can be obtained via a {@link org.activiti.spring.ProcessEngineFactoryBean}
	 */
	protected ProcessEngine processEngine;

	/**
	 * @param processEngine takes a reference to a {@link org.activiti.engine.ProcessEngine}
	 */
	public ProcessStartingMethodInterceptor(ProcessEngine processEngine) {
		this.processEngine = processEngine;
	}

	boolean shouldReturnProcessInstance(StartProcess startProcess, MethodInvocation methodInvocation, Object result) {
		return (result instanceof ProcessInstance || methodInvocation.getMethod().getReturnType().isAssignableFrom(ProcessInstance.class));
	}

	boolean shouldReturnProcessInstanceId(StartProcess startProcess, MethodInvocation methodInvocation, Object result) {
		return startProcess.returnProcessInstanceId() && (result instanceof String || methodInvocation.getMethod().getReturnType().isAssignableFrom(String.class));
	}

	@SuppressWarnings("unused")
	boolean shouldReturnAsyncResultWithProcessInstance(StartProcess startProcess, MethodInvocation methodInvocation, Object result) {
		return (result instanceof Future || methodInvocation.getMethod().getReturnType().isAssignableFrom(Future.class));
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {

		Method method = invocation.getMethod();

		StartProcess startProcess = AnnotationUtils.getAnnotation(method, StartProcess.class);

		String processKey = startProcess.processKey();

		Assert.hasText(processKey, "you must provide the name of process to start");

		Object result;
		try {
			result = invocation.proceed();
			Map<String, Object> vars = this.processVariablesFromAnnotations(invocation);

			String businessKey = this.processBusinessKey(invocation);

			log.info("variables for the started process: " + vars.toString());

			RuntimeService runtimeService = this.processEngine.getRuntimeService();
			ProcessInstance pi ;
			if (null != businessKey && StringUtils.hasText(businessKey)) {
				pi = runtimeService.startProcessInstanceByKey(processKey, businessKey, vars);
				log.info("the business key for the started process is '" + businessKey + "' ");
			} else {
				pi = runtimeService.startProcessInstanceByKey(processKey, vars);
			}

			String pId = pi.getId();

			if (invocation.getMethod().getReturnType().equals(void.class))
				return null;

			if (shouldReturnProcessInstance(startProcess, invocation, result))
				return pi;

			if (shouldReturnProcessInstanceId(startProcess, invocation, result))
				return pId;

			if (shouldReturnAsyncResultWithProcessInstance(startProcess, invocation, result)) {
				return new AsyncResult<ProcessInstance>(pi);
			}

		} catch (Throwable th) {
			throw new RuntimeException(th);
		}
		return result;
	}

	protected String processBusinessKey(MethodInvocation invocation) throws Throwable {
		Map<BusinessKey, String> businessKeyAnnotations = this.mapOfAnnotationValues( BusinessKey.class ,invocation);
		if (businessKeyAnnotations.size() == 1) {
			BusinessKey processId = businessKeyAnnotations.keySet().iterator().next();
			return businessKeyAnnotations.get(processId);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private <K extends Annotation, V> Map<K, V> mapOfAnnotationValues(Class<K> annotationType, MethodInvocation invocation) {
		Method method = invocation.getMethod();
		Annotation[][] annotations = method.getParameterAnnotations();
		Map<K, V> vars = new HashMap<K, V>();
		int paramIndx = 0;
		for (Annotation[] annPerParam : annotations) {
			for (Annotation annotation : annPerParam) {
				if (!annotationType.isAssignableFrom(annotation.getClass())) {
					continue;
				}
				K pv = (K) annotation;
				V v = (V) invocation.getArguments()[paramIndx];
				vars.put(pv, v);

			}
			paramIndx += 1;
		}
		return vars;
	}


	/**
	 * if there any arguments with the {@link org.activiti.engine.annotations.ProcessVariable} annotation,
	 * then we feed those parameters into the business process
	 *
	 * @param invocation the invocation of the method as passed to the {@link org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)} method
	 * @return returns the map of process variables extracted from the parameters
	 * @throws Throwable thrown anything goes wrong
	 */
	protected Map<String, Object> processVariablesFromAnnotations(MethodInvocation invocation) throws Throwable {

		Map<ProcessVariable, Object> vars = this.mapOfAnnotationValues(ProcessVariable.class, invocation);

		Map<String, Object> varNameToValueMap = new HashMap<String, Object>();
		for (ProcessVariable processVariable : vars.keySet()) {
			varNameToValueMap.put(processVariable.value(), vars.get(processVariable));
		}
		return varNameToValueMap;

	}
}
