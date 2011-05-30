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
package org.activiti.cdi.impl;

import java.util.EmptyStackException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.activiti.cdi.Actor;
import org.activiti.cdi.impl.context.BusinessProcessAssociationManager;
import org.activiti.cdi.impl.context.CachingBeanStore;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

/**
 * allowing subclasses to reuse process and task resuming logic. 
 * 
 * @author Daniel Meyer
 *
 */
public abstract class AbstractProcessResuming {
  
  private Logger logger = Logger.getLogger(AbstractProcessResuming.class.getName());
  
  @Inject protected BusinessProcessAssociationManager associationManager;
  
  @Inject protected ProcessEngine processEngine;
  
  @Inject protected Actor actor;

  protected boolean isActive() { 
    return hasProcess();
  }

  protected boolean hasProcess() {
    return associationManager.getProcessInstanceId() != null;
  }

  /**
   * Spend our best effort to automagically resume a process instance
   */
  protected void resumeProcess() {
    if (associationManager.getProcessInstanceId() != null) {
      return;
    }
    try {
      if (Context.getExecutionContext() != null) {
        ProcessInstance instance = Context.getExecutionContext().getProcessInstance();
        if (instance != null) {
          associationManager.associateProcessInstance(instance.getId());         
        }
      }
    } catch (EmptyStackException e) {
      // means that no execution context is available
      return;
    }
  }
  
  /**
   * Spend our best effort to automagically resume a task
   */
  protected void resumeTask() {
    if (associationManager.getTaskId() != null) {
      return;
    }
    if (associationManager.getProcessInstanceId() == null) {
      resumeProcess();
    }
    if (associationManager.getProcessInstanceId() != null) {
      Task task = null;
      try {
        task = processEngine
                .getTaskService()
                .createTaskQuery()
                .processInstanceId(associationManager.getProcessInstanceId())
                .singleResult();
        
      }catch (ActivitiException e) {
        // probably more than one task active, try to qualify with the current actor:
        String actorId = actor.getActorId();
        if(actorId == null || actorId.length() == 0) {
          return;
        }
        try {
          task = processEngine
                  .getTaskService()
                  .createTaskQuery()
                  .processInstanceId(associationManager.getProcessInstanceId())
                  .taskAssignee(actorId)
                  .singleResult();
        
        }catch (ActivitiException ex) {
          return;
        }                                 
      }
      
      if (task != null) {
        associationManager.associateTask(task.getId());        
      }
    }
  }

  protected void assertProcessIdSet() {
    if (associationManager.getProcessInstanceId() == null) {
      resumeProcess();
    }
    if (associationManager.getProcessInstanceId() == null) {
      logger.log(Level.SEVERE, "No business process associated with this conversation.");
      throw new ActivitiException("No business process associated with this conversation.");
    }
  }

  protected void assertTaskIdSet() {
    if (associationManager.getTaskId() == null) {
      resumeTask();
    }
    if (associationManager.getTaskId() == null) {
      logger.log(Level.SEVERE, "No task associated with this conversation.");
      throw new ActivitiException("No task associated with this conversation.");
    }
  }
  
  // ------------------------------------------------------------------------------------


  protected void associateBusinessProcessInstance(String processInstanceId) {
    associationManager.associateProcessInstance(processInstanceId);
  }
  
  protected void associateTaskInstance(String taskId) {
    associationManager.associateTask(taskId);
  }
  
  public CachingBeanStore getBeanStore() {
   return associationManager.getBeanStore();
  }

}
