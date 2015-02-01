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
 * Annotation signaling that a task is to be completed after the annotated
 * method returns. Requires that the current unit of work (conversation 
 * or request) is associated with a task. This has the same effect as
 * calling {@link BusinessProcess#completeTask()}.
 * 
 * <p />
 * Example: after this method returns, the current task is completed 
 * <pre>
 * {@code @CompleteTask} 
 * public void respond(String response, Message message) {
 *  message.setResponse(response);
 * } 
 * </pre>
 * If the annotated method throws an exception, the task is not completed.
 * 
 * @see BusinessProcess#startTask(String)
 * @see BusinessProcess#completeTask()
 *  
 * @author Daniel Meyer
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface CompleteTask {

  /**
   * Specifies whether the current conversation should be ended.
   */
  @Nonbinding
  boolean endConversation() default false;
}
