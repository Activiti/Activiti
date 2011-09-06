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
import org.activiti.engine.task.Task;

/**
 * Represents a means for associating an execution with a context.
 * <p />
 * This enables activiti-cdi to provide contextual business process management
 * services, without relying on a specific context like i.e. the conversation
 * context.
 * 
 * @author Daniel Meyer
 */
public interface BusinessProcessAssociationManager {
  
  /**
   * Associates an executionId
   * 
   * @param processInstanceId
   */
  public void associate(String executionId);
    
  /**
   * Disassociates the current process instance with a context / scope
   * 
   * @throws ActivitiException if no process instance is currently associated
   */
  public void disAssociate();
    
    
  /**
   * @return the id of the execution currently associated or null
   */
  public String getExecutionId();
   
  /**
   * @return the beanstore for storing process variables between flushes
   */
  public CachingBeanStore getBeanStore();
  
  /**
   * set this to true if you want the beanstrore flushed in the next command / transaction
   */
  public void setFlushBeanStore(boolean value);
  
  /**
   * returns true if the beanstore should be flushed in the next command
   */
  public boolean isFlushBeanStore();
  
  /**
   * set a current task
   */
  public void setTask(Task task);
  
  /**
   * get the current task
   */
  public Task getTask();  

}
