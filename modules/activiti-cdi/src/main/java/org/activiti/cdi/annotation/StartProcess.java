/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.cdi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

import org.activiti.cdi.BusinessProcess;

/**
 * Starts a new process instance after the annotated method returns. The process
 * instance is subsequently managed.
 * <p/>
 * Each process variable set through
 * {@link BusinessProcess#setVariable(String, Object)} within this
 * conversation is flushed to the process instance at process instantiation. The
 * same is true for instances of {@link BusinessProcessScoped} beans.
 * 
 * @author Daniel Meyer
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface StartProcess {

  /**
   * The key of the process definition to start, as provided in the 'id'
   * attribute of a bpmn20.xml process definition.
   */
  @Nonbinding
  String value() default "";

  /**
   * The name of the process definition to start, as provided in the 'name'
   * attribute of a bpmn20.xml process definition.
   */
  @Nonbinding
  String name() default "";

}
