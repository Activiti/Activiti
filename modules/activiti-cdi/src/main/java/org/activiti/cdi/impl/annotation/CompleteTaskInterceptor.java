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
package org.activiti.cdi.impl.annotation;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.activiti.cdi.ActivitiCdiException;
import org.activiti.cdi.BusinessProcess;
import org.activiti.cdi.annotation.CompleteTask;

/**
 * {@link Interceptor} for handling the {@link CompleteTask}-Annotation
 * 
 * @author Daniel Meyer
 */
@Interceptor
@CompleteTask
public class CompleteTaskInterceptor implements Serializable {

  private static final long serialVersionUID = 1L;

  @Inject BusinessProcess businessProcess;

  @AroundInvoke
  public Object invoke(InvocationContext ctx) throws Exception {
    try {
      Object result = ctx.proceed();

      CompleteTask completeTaskAnnotation = ctx.getMethod().getAnnotation(CompleteTask.class);
      boolean endConversation = completeTaskAnnotation.endConversation();    
      businessProcess.completeTask(endConversation);     

      return result;
    } catch (InvocationTargetException e) {
      throw new ActivitiCdiException("Error while completing task: "+e.getCause().getMessage(), e.getCause());
    }
  }

}
