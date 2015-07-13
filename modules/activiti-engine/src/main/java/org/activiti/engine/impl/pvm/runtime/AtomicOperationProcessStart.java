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

package org.activiti.engine.impl.pvm.runtime;

import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class AtomicOperationProcessStart extends AbstractEventAtomicOperation {

  @Override
  protected ScopeImpl getScope(InterpretableExecution execution) {
    return execution.getProcessDefinition();
  }

  @Override
  protected String getEventName() {
    return org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START;
  }

  @Override
  protected void eventNotificationsCompleted(InterpretableExecution execution) {
  	if (Context.getProcessEngineConfiguration() != null && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
  	  Map<String, Object> variablesMap = null;
  	  try {
  	    variablesMap = execution.getVariables();
  	  } catch (Throwable t) {
  	    // In some rare cases getting the execution variables can fail (JPA entity load failure for example)
  	    // We ignore the exception here, because it's only meant to include variables in the initialized event.
  	  }
    	Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
    			ActivitiEventBuilder.createEntityWithVariablesEvent(ActivitiEventType.ENTITY_INITIALIZED, 
    			    execution, variablesMap, false));
      Context.getProcessEngineConfiguration().getEventDispatcher()
              .dispatchEvent(ActivitiEventBuilder.createProcessStartedEvent(execution, variablesMap, false));
    }
  	
    ProcessDefinitionImpl processDefinition = execution.getProcessDefinition();
    StartingExecution startingExecution = execution.getStartingExecution();
    List<ActivityImpl> initialActivityStack = processDefinition.getInitialActivityStack(startingExecution.getInitial());  
    execution.setActivity(initialActivityStack.get(0));
    execution.performOperation(PROCESS_START_INITIAL);
  }
}
