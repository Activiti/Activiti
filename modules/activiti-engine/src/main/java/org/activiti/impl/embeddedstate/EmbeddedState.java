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
package org.activiti.impl.embeddedstate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.ActivitiException;
import org.activiti.ProcessInstance;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.pvm.Activity;
import org.activiti.pvm.Transition;

/**
 * Why would you want to use an embedded processInstance ?
 * 
 *  - Often, there are situations where using a full blown process is just overkill 
 *    (for example a lifecycle state flow of your domain model). 
 *  
 *  - For some use cases, it is interesting to have the embedded process close to the domain model.
 *    By having the process instances as member field of the domain model, maintaining the process definition
 *    gets very easy.
 *    
 *  - The logic contained in the process is typically found in a business layer.
 *    By grouping this logic however within the embedded process the logic lives very close to the domain model.
 *    This avoids using *a lot* of if-else constructs in the business layer which are hard to maintain.
 *    
 *  - Having a separate process instead of scattered logic on the business layer makes it easier to 
 *    discuss business logic with business people.
 *  
 *  - Using an embedded process instance sometimes feels like defining rules for your domain object.
 *    An embedded process instance however, doesn't have the disconnection with the code which business rules typically have.
 * 
 * @author Joram Barrez
 */
public abstract class EmbeddedState {

  protected ExecutionImpl embeddedProcessInstance;
  
  /**
   * Concrete embdedded process instances need to implement this method
   * to associate the process definition with this embedded process instance.
   */
  protected abstract ProcessDefinitionImpl getProcessDefinition();
  
  protected abstract String getEmbeddedState();
  
  protected abstract void setEmbeddedState(String embeddedState);

  public void serializeExecutionTree() {
    if (embeddedProcessInstance != null) {
      StringBuilder strb = new StringBuilder();
      if (!embeddedProcessInstance.hasExecutions()) {
        if (embeddedProcessInstance.getActivity() != null) {
          strb.append("activity:" + embeddedProcessInstance.getActivity().getId()); // TODO: dummy serialisation. Need to use something more clever
        }
      } else {
        throw new ActivitiException("Didn't yet implement serialization of non-root executions");
      }
      setEmbeddedState(strb.toString());
    }
  }
  
  public void deserializeExecutionTree() {
    String procInstString = getEmbeddedState();

    if (procInstString != null && procInstString.length() > 0) {
      String[] splittedSerialization = procInstString.split(",");
      String activityId = splittedSerialization[0].replace("activity:", "");
      ProcessDefinitionImpl processDefinitionImpl = getProcessDefinition();
      embeddedProcessInstance = new ExecutionImpl(processDefinitionImpl);
      embeddedProcessInstance.setActivity(processDefinitionImpl.findActivity(activityId));
    }
  }

  public void start() {
    start(null);
  }

  public void start(String variableName, Object value) {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put(variableName, value);
    start(vars);
  }

  public void start(Map<String, Object> variables) {
    
    if (embeddedProcessInstance == null) {
      embeddedProcessInstance = new ExecutionImpl(getProcessDefinition());
    }
    
    if (variables != null) {
      for (String key : variables.keySet()) {
        embeddedProcessInstance.setVariable(key, variables.get(key));
      }
    }
    embeddedProcessInstance.startEmbedded();
    serializeExecutionTree();
  }
  
  public List<String> getCurrentState() {
    deserializeExecutionTree();
    List<Activity> activities =  embeddedProcessInstance.getProcessInstance().getActivities();
    List<String> activityNames = new ArrayList<String>();
    for (Activity activity : activities) {
      activityNames.add(activity.getId());
    }
    return activityNames;
  }
  
  public List<String> getAllAvailableTransitions() {
    deserializeExecutionTree();
    if (embeddedProcessInstance != null) {
      List<String> availableTransitions = new ArrayList<String>();
      for (Transition transition : embeddedProcessInstance.getOutgoingTransitions()) {
        // TODO: condition evaluation
        if (!availableTransitions.contains(transition.getId())) {
          availableTransitions.add(transition.getId());
        }
      }
      return availableTransitions;
    }
    return Collections.emptyList();
  }

  public void fireEvent(Object event) {
    deserializeExecutionTree();
    embeddedProcessInstance.event(event);
    serializeExecutionTree();
  }

  public ProcessInstance getProcessInstance() {
    return embeddedProcessInstance;
  }

}
