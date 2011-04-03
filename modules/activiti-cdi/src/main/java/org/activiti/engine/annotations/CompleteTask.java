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
package org.activiti.engine.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

import org.activiti.cdi.Actor;

/**
 * Annotation signaling that a task is to be completed after the annotated
 * method returns. Requires a ProcessInstance to be managed. 
 * 
 * If neither an id ("key") nor a name is specified, we try to resolve a single
 * task assigned to the current user (see {@link Actor}) in the current process
 * instance.
 * 
 * TODO: explain implications for transaction handling.
 * 
 * @author Daniel Meyer
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface CompleteTask {

  /**
   * The id of the task to complete.
   */
  @Nonbinding
  String value() default "";

  /**
   * The name of the task to complete.
   */
  @Nonbinding
  String name() default "";

  /**
   * Specifies whether the current conversation should be ended.
   */
  @Nonbinding
  boolean endConversation() default true;
}
