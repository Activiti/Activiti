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

import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.annotation.AnnotationMethodMatcher;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * this code is taken almost verbatim from the Spring Integration
 * project's source code where it's a static
 * private inner class.
 *
 * @author Mark Fisher
 *
 */
public class MetaAnnotationMethodMatcher extends AnnotationMethodMatcher {

	private final Class<? extends Annotation> annotationType;


	/**
	 * Create a new AnnotationClassFilter for the given annotation type.
	 *
	 * @param annotationType the annotation type to look for
	 */
	public MetaAnnotationMethodMatcher(Class<? extends Annotation> annotationType) {
		super(annotationType);
		this.annotationType = annotationType;
	}


	@Override
	@SuppressWarnings("rawtypes")
	public boolean matches(Method method, Class targetClass) {
		if (AnnotationUtils.getAnnotation(method, this.annotationType) != null) {
			return true;
		}
		// The method may be on an interface, so let's check on the target class as well.
		Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
		return (specificMethod != method &&
				(AnnotationUtils.getAnnotation(specificMethod, this.annotationType) != null));
	}
}
