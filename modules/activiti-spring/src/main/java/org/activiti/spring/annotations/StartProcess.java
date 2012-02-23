/*
 * Copyright 2011 the original author or authors
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
package org.activiti.spring.annotations;

import java.lang.annotation.*;

/**
 * this annotation instructs the component model to start an Activiti business process on
 * sucessful invocation of a method that's annotated with it.
 *
 * @author Josh Long
 * @since 1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StartProcess {
	/**
	 * the name of the business process to start (by key)
	 */
	String processKey() ;

	/**
	 * returns the ID of the {@link org.activiti.engine.runtime.ProcessInstance}. If specified, it'll only work if
	 * the return type of the invocation is compatabile with a {@link org.activiti.engine.runtime.ProcessInstance}'s ID
	 * (which is a String, at the moment)
	 *
	 * @return  whether to return the process instance ID
	 */
	boolean returnProcessInstanceId() default false;




}
