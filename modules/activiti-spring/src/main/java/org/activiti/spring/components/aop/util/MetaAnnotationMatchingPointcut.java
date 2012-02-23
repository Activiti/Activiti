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

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.annotation.AnnotationClassFilter;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;

/**
 * this code is taken almost (99.99%) verbatim from the Spring Integration
 * project's source code where it's a static
 * private inner class.
 *
 * @author Mark Fisher
 */
public class MetaAnnotationMatchingPointcut implements Pointcut {

	private final ClassFilter classFilter;

	private final MethodMatcher methodMatcher;


	/**
	 * Create a new MetaAnnotationMatchingPointcut for the given annotation type.
	 *
	 * @param classAnnotationType the annotation type to look for at the class level
	 * @param checkInherited			whether to explicitly check the superclasses and
	 *                            interfaces for the annotation type as well (even if the annotation type
	 *                            is not marked as inherited itself)
	 */
	public MetaAnnotationMatchingPointcut(Class<? extends Annotation> classAnnotationType, boolean checkInherited) {
		this.classFilter = new AnnotationClassFilter(classAnnotationType, checkInherited);
		this.methodMatcher = MethodMatcher.TRUE;
	}

	/**
	 * Create a new MetaAnnotationMatchingPointcut for the given annotation type.
	 *
	 * @param classAnnotationType	the annotation type to look for at the class level
	 *                             (can be <code>null</code>)
	 * @param methodAnnotationType the annotation type to look for at the method level
	 *                             (can be <code>null</code>)
	 */
	public MetaAnnotationMatchingPointcut(
			Class<? extends Annotation> classAnnotationType, Class<? extends Annotation> methodAnnotationType) {

		Assert.isTrue((classAnnotationType != null || methodAnnotationType != null),
				"Either Class annotation type or Method annotation type needs to be specified (or both)");

		if (classAnnotationType != null) {
			this.classFilter = new AnnotationClassFilter(classAnnotationType);
		} else {
			this.classFilter = ClassFilter.TRUE;
		}

		if (methodAnnotationType != null) {
			this.methodMatcher = new MetaAnnotationMethodMatcher(methodAnnotationType);
		} else {
			this.methodMatcher = MethodMatcher.TRUE;
		}
	}


	public ClassFilter getClassFilter() {
		return this.classFilter;
	}

	public MethodMatcher getMethodMatcher() {
		return this.methodMatcher;
	}
}

