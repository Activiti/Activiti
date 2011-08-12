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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.activiti.engine.ProcessEngine;
import org.activiti.spring.annotations.StartProcess;
import org.activiti.spring.components.aop.util.MetaAnnotationMatchingPointcut;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;

/**
 * AOP advice for methods annotated with (by default) {@link StartProcess}.
 *
 * Advised methods start a process after the method executes.
 *
 * Advised methods can declare a return
 * type of {@link org.activiti.engine.runtime.ProcessInstance} and then subsequently
 * return null. The real return ProcessInstance value will be given by the aspect.
 * 
 *
 * @author Josh Long
 * @since 5.3
 */
public class ProcessStartingPointcutAdvisor implements PointcutAdvisor, Serializable {


	/**
	 * annotations that shall be scanned
	 */
	private Set<Class<? extends Annotation>> annotations = new HashSet<Class<? extends Annotation>>(Arrays.asList(StartProcess.class));

	/**
	 * the {@link org.aopalliance.intercept.MethodInterceptor} that handles launching the business process.
	 */
	protected MethodInterceptor advice;

	/**
	 * matches any method containing the {@link StartProcess} annotation.
	 */
	protected Pointcut pointcut;

	/**
	 * the injected reference to the {@link org.activiti.engine.ProcessEngine}
	 */
	protected ProcessEngine processEngine;

	public ProcessStartingPointcutAdvisor(ProcessEngine pe) {
		this.processEngine = pe;
		this.pointcut = buildPointcut();
		this.advice = buildAdvise();

	}

	protected MethodInterceptor buildAdvise() {
		return new ProcessStartingMethodInterceptor(this.processEngine);
	}

	public Pointcut getPointcut() {
		return pointcut;
	}

	public Advice getAdvice() {
		return advice;
	}

	public boolean isPerInstance() {
		return true;
	}

	private Pointcut buildPointcut() {
		ComposablePointcut result = null;
		for (Class<? extends Annotation> publisherAnnotationType : this.annotations) {
			Pointcut mpc = new MetaAnnotationMatchingPointcut(null, publisherAnnotationType);
			if (result == null) {
				result = new ComposablePointcut(mpc);
			} else {
				result.union(mpc);
			}
		}
		return result;
	}


}

