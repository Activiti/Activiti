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
package org.activiti.cdi.impl.context;

import org.activiti.engine.ActivitiException;

/**
 * Represents a means for associating a business process instance and / or a
 * task instance with a context.
 * <p />
 * This enables activiti-cdi to provide contextual business process management
 * services, without relying on a specific context like i.e. the conversation
 * context.
 * 
 * @author Daniel Meyer
 */
public interface BusinessProcessAssociationManager {
  
  /**
   * Associates a processInstanceId with a context/scope
   * 
   * @param processInstanceId
   */
  public void associateProcessInstance(String processInstanceId);
    
  /**
   * Disassociates the current process instance with a context / scope
   * 
   * @throws ActivitiException if no process instance is currently associated
   */
  public void disAssociateProcessInstance();
  
  /**
   * Associates a task with a context/scope
   * 
   * @param processInstanceId
   */
  public void associateTask(String taskId);
    
  /**
   * Disassociates the current process instance with a context / scope
   * 
   * @throws ActivitiException if no process instance is currently associated
   */
  public void disAssociateTask();
  
  /**
   * @return the id of the process instance currently associated or null.
   */
  public String getProcessInstanceId();
  
  /**
   * @return get the id of the task currently associated or null.
   */
  public String getTaskId();
  
  /**
   * @return the beanstore for storing process variables between flushes
   */
  public CachingBeanStore getBeanStore();
}
