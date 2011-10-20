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
 * Indicates that the given bean is an Activiti handler. An activiti handler is a bean
 * that is so annotated to respond to events ("states") in an Activiti BPM process.
 * Generically, it is a class that has been adapted to be usable in an Activiti process
 *
 * <p/>
 * For example, suppose we have registered a BPMN process that has
 * the following declaration:
 * <p/>
 * <code>
 * &lt;service-task activiti:expression = "myBean" id = "confirm-receipt" /&gt;
 * </code>
 * <p/>
 * This is a state that will be entered from Activiti and execution will flow through to the bean
 * registered in the context as "myBean." To subscribe to that, a POJO need only implement
 * (optionally) {@link ActivitiComponent} and, on a method, add
 * {@link State} to indicate that the method in particular is
 * tasked with responding to a state. If applied to a bean and there are no {@link org.activiti.engine.annotations.ActivitiComponent}
 * annotations present, then one option might be to automatically enlist all public methods
 * as handlers for states whose IDs or names are inferred from the method name:
 * <p/>
 * <code>public void confirmReceipt(..)</code> would be treated the same as
 * <p/>
 * <code>@State( "confirm-receipt") public void confirmReceipt (..)</code>,
 *
 * @author Josh Long
 * @since 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
//@Component
public @interface ActivitiComponent {
	String processKey() default "";
}
