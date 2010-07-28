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

import org.activiti.engine.ActivitiException;
import org.activiti.impl.definition.ActivityImpl;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.definition.ScopeElementImpl;
import org.activiti.impl.definition.TransitionImpl;
import org.activiti.impl.variable.DefaultVariableTypes;
import org.activiti.impl.variable.VariableTypes;


/**
 * @author Tom Baeyens
 */
public class ProcessDefinitionBuilder {
  
  protected ProcessDefinitionImpl processDefinition;
  protected Stack<ScopeElementImpl> scopeElementStack;
  protected List<UnresolvedTransition> unresolvedTransitions;
  
  private ProcessDefinitionBuilder(VariableTypes variableTypes) {
    this.processDefinition = new ProcessDefinitionImpl(variableTypes);
    this.scopeElementStack = new Stack<ScopeElementImpl>();
    this.scopeElementStack.push(processDefinition);
    this.unresolvedTransitions = new ArrayList<UnresolvedTransition>();
  }

  public static ProcessDefinitionBuilder createProcessDefinitionBuilder() {
    return new ProcessDefinitionBuilder(new DefaultVariableTypes());
  }

  public ObjectProcessDefinition build() {
    resolveTransitions();
    return processDefinition;
  }

  public ProcessDefinitionBuilder createActivity(String id) {
    ActivityImpl activity = scopeElementStack.peek().createActivity(id);
    scopeElementStack.push(activity);
    return this;
  }
  
  /**
   * Sets the last created activity as initial node of the process definition.
   */
  public ProcessDefinitionBuilder initial() {
    ActivityImpl activity = (ActivityImpl) scopeElementStack.peek();
    processDefinition.setInitial(activity);
    return this;
  }

  public ProcessDefinitionBuilder endActivity() {
    scopeElementStack.pop();
    return this;
  }

  public ProcessDefinitionBuilder name(String name) {
    scopeElementStack.peek().setName(name);
    return this;
  }

  public ProcessDefinitionBuilder scope() {
    ActivityImpl activity = (ActivityImpl) scopeElementStack.peek();
    activity.setScope(true);
    return this;
  }

  public ProcessDefinitionBuilder transition(String destinationActivityId) {
    return transition(destinationActivityId, null, null);
  }
  
  public ProcessDefinitionBuilder transition(String destinationActivityId, Condition condition) {
    return transition(destinationActivityId, null, condition);
  }
  
  public ProcessDefinitionBuilder transition(String destinationActivityId, String transitionId) {
    return transition(destinationActivityId, transitionId, null);
  }
  
  public ProcessDefinitionBuilder transition(String destinationActivityId, String transitionId, Condition condition) {
    ActivityImpl activity = (ActivityImpl) scopeElementStack.peek();
    TransitionImpl transition = activity.createTransition();
    transition.setId(transitionId);
    transition.setCondition(condition);
    unresolvedTransitions.add(new UnresolvedTransition(transition, destinationActivityId));
    return this;
  }
  
  public ProcessDefinitionBuilder behavior(ActivityBehavior activityBehavior) {
    ActivityImpl activity = (ActivityImpl) scopeElementStack.peek();
    activity.setActivityBehavior(activityBehavior);
    return this;
  }

  protected void resolveTransitions() {
    for (UnresolvedTransition unresolvedTransition: unresolvedTransitions) {
      unresolvedTransition.resolve();
    }
  }

  class UnresolvedTransition {
    TransitionImpl transition;
    String destinationActivityId;
    public UnresolvedTransition(TransitionImpl transition, String destinationActivityId) {
      this.transition = transition;
      this.destinationActivityId = destinationActivityId;
    }
    public void resolve() {
      ActivityImpl destination = processDefinition.findActivity(destinationActivityId);
      if (destination==null) {
        throw new ActivitiException("destination activity '"+destinationActivityId+"' does not exist");
      }
      transition.setDestination(destination);
    }
  }
}
