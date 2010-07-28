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

package org.activiti.pvm;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.activiti.pvm.activity.ActivityBehaviour;
import org.activiti.pvm.impl.process.ActivityImpl;
import org.activiti.pvm.impl.process.ProcessDefinitionImpl;
import org.activiti.pvm.impl.process.ProcessElementImpl;
import org.activiti.pvm.impl.process.ScopeImpl;
import org.activiti.pvm.impl.process.TransitionImpl;
import org.activiti.pvm.process.PvmProcessDefinition;



/**
 * @author Tom Baeyens
 */
public class ProcessDefinitionBuilder {

  protected ProcessDefinitionImpl processDefinition = new ProcessDefinitionImpl();
  protected Stack<ScopeImpl> scopeStack = new Stack<ScopeImpl>();
  protected ProcessElementImpl processElement = processDefinition;
  protected TransitionImpl transition;
  protected List<Object[]> unresolvedTransitions = new ArrayList<Object[]>();
  
  public ProcessDefinitionBuilder() {
    scopeStack.push(processDefinition);
  }

  public ProcessDefinitionBuilder startActivity(String id) {
    ActivityImpl activity = scopeStack.peek().createActivity();
    activity.setId(id);
    scopeStack.push(activity);
    processElement = activity;
    
    transition = null;
    
    return this;
  }
  
  public ProcessDefinitionBuilder endActivity() {
    scopeStack.pop();
    processElement = scopeStack.peek();

    transition = null;
    
    return this;
  }
  
  public ProcessDefinitionBuilder initial() {
    processDefinition.setInitial(getActivity());
    return this;
  }

  public ProcessDefinitionBuilder startTransition(String destinationActivityId) {
    ActivityImpl activity = getActivity();
    transition = activity.createOutgoingTransition();
    unresolvedTransitions.add(new Object[]{transition, destinationActivityId});
    processElement = transition;

    return this;
  }
  
  public ProcessDefinitionBuilder endTransition() {
    processElement = scopeStack.peek();
    return this;
  }

  public ProcessDefinitionBuilder transition(String destinationActivityId) {
    startTransition(destinationActivityId);
    endTransition();
    return this;
  }

  public ProcessDefinitionBuilder id(String transitionId) {
    transition.setId(transitionId);
    return this;
  }

  public ProcessDefinitionBuilder behaviour(ActivityBehaviour activityBehaviour) {
    getActivity().setActivityBehaviour(activityBehaviour);
    return this;
  }

  public PvmProcessDefinition buildProcessDefinition() {
    for (Object[] unresolvedTransition: unresolvedTransitions) {
      TransitionImpl transition = (TransitionImpl) unresolvedTransition[0];
      String destinationActivityName = (String) unresolvedTransition[1];
      ActivityImpl destination = processDefinition.findActivity(destinationActivityName);
      if (destination == null) {
        throw new RuntimeException("destination '"+destinationActivityName+"' not found.  (referenced from transition in '"+transition.getSource().getId()+"')");
      }
      transition.setDestination(destination);
    }
    return processDefinition;
  }
  
  protected ActivityImpl getActivity() {
    return (ActivityImpl) scopeStack.peek(); 
  }
}
