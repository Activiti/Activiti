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
package org.activiti.engine.impl.persistence.entity;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;

/**
 * Contains a predefined set of states for process definitions and process instances
 * 
 * @author Daniel Meyer
 */
public interface SuspensionState {
  
  SuspensionState ACTIVE = new SuspensionStateImpl(1, "active");
  SuspensionState SUSPENDED = new SuspensionStateImpl(2, "suspended");
  
  int getStateCode();
  
  ///////////////////////////////////////////////////// default implementation 
  
  static class SuspensionStateImpl implements SuspensionState {

    public final int stateCode;
    protected final String name;   

    public SuspensionStateImpl(int suspensionCode, String string) {
      this.stateCode = suspensionCode;
      this.name = string;
    }    
   
    public int getStateCode() {     
      return stateCode;
    }
    
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + stateCode;
      return result;
    }
    
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      SuspensionStateImpl other = (SuspensionStateImpl) obj;
      if (stateCode != other.stateCode)
        return false;
      return true;
    }
    
    @Override
    public String toString() {
      return name;
    }
  }
  
  /////////////////////////////////////////// helper class
  
  public static class SuspensionStateUtil{
    
    public static void setSuspensionState(ProcessDefinitionEntity processDefinitionEntity, SuspensionState state) {
      if(processDefinitionEntity.getSuspensionState() == state.getStateCode()) {
        throw new ActivitiException("Cannot set suspension state '"+state+"' for "+processDefinitionEntity+"': already in state '"+state+"'.");
      }
      processDefinitionEntity.setSuspensionState(state.getStateCode());
      dispatchStateChangeEvent(processDefinitionEntity, state);
    }
    
    public static void setSuspensionState(ExecutionEntity executionEntity, SuspensionState state) {
      if(executionEntity.getSuspensionState() == state.getStateCode()) {
        throw new ActivitiException("Cannot set suspension state '"+state+"' for "+executionEntity+"': already in state '"+state+"'.");
      }
      executionEntity.setSuspensionState(state.getStateCode());
      dispatchStateChangeEvent(executionEntity, state);
    }
    
    public static void setSuspensionState(TaskEntity taskEntity, SuspensionState state) {
      if (taskEntity.getSuspensionState() == state.getStateCode()) {
        throw new ActivitiException("Cannot set suspension state '"+state+"' for " + taskEntity + "': already in state '"+state+"'.");
      }
      taskEntity.setSuspensionState(state.getStateCode());
      dispatchStateChangeEvent(taskEntity, state);
    }
    
    protected static void dispatchStateChangeEvent(Object entity, SuspensionState state) {
    	if(Context.getCommandContext() != null && Context.getCommandContext().getEventDispatcher().isEnabled()) {
      	ActivitiEventType eventType = null;
      	if(state == SuspensionState.ACTIVE) {
      		eventType = ActivitiEventType.ENTITY_ACTIVATED;
      	} else {
      		eventType = ActivitiEventType.ENTITY_SUSPENDED;
      	}
      	Context.getCommandContext().getEventDispatcher().dispatchEvent(
      			ActivitiEventBuilder.createEntityEvent(eventType, entity));
      }
    }
  }
  
}
