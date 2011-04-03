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

import java.lang.reflect.InvocationTargetException;

import javax.enterprise.context.Conversation;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.activiti.cdi.Actor;
import org.activiti.cdi.BusinessProcess;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.annotations.CompleteTask;
import org.activiti.engine.task.Task;

/**
 * {@link Interceptor} for handling the {@link CompleteTask}-Annotation
 * 
 * @author Daniel Meyer
 */
@Interceptor
@CompleteTask
public class CompleteTaskInterceptor {

  @Inject BusinessProcess businessProcess;

  @Inject Instance<Conversation> conversation;

  @Inject TaskService taskService;
  
  @Inject RepositoryService repositoryService;
  
  @Inject Actor actor;

  @AroundInvoke
  public Object invoke(InvocationContext ctx) throws Throwable {
    try {
      Object result = ctx.proceed();

      CompleteTask completeTaskAnnotation = ctx.getMethod().getAnnotation(CompleteTask.class);

      String taskKey = completeTaskAnnotation.value();
      String taskName = completeTaskAnnotation.name();
      boolean endConversation = completeTaskAnnotation.endConversation();

      Task task = null;
      if (taskName.length() > 0) {        
        task = taskService.createTaskQuery()
                            .processInstanceId(businessProcess.getProcessInstanceId())
                            .taskName(taskName)                            
                            .taskAssignee(actor.getActorId())
                          .singleResult();
        
      } else if (taskKey.length() > 0) {
        task = taskService.createTaskQuery()
                            .processInstanceId(businessProcess.getProcessInstanceId())
                            .taskDefinitionKey(taskKey)
                            .taskAssignee(actor.getActorId())
                          .singleResult();
      } else {
        task = taskService.createTaskQuery()
                            .processInstanceId(businessProcess.getProcessInstanceId())                         
                            .taskAssignee(actor.getActorId())
                          .singleResult();
      }

      businessProcess.resumeTaskById(task.getId());
      businessProcess.completeTask();

      if (endConversation) {   
        if(!conversation.isUnsatisfied()) {
          conversation.get().end();
        }
      }

      return result;
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

}
