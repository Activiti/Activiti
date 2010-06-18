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
package org.activiti.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.activiti.ActivitiException;
import org.activiti.activity.ActivityBehavior;
import org.activiti.impl.definition.ActivityImpl;
import org.activiti.impl.definition.ProcessDefinitionImpl;
import org.activiti.impl.definition.ScopeElementImpl;
import org.activiti.impl.definition.TransitionImpl;


/**
 * @author Tom Baeyens
 */
public class ProcessDefinitionBuilder {
  
  protected ProcessDefinitionImpl processDefinition;
  protected Stack<ScopeElementImpl> scopeElementStack;
  protected List<UnresolvedTransition> unresolvedTransitions; 
  
  protected ProcessDefinitionBuilder(String id) {
    this.processDefinition = new ProcessDefinitionImpl();
    this.processDefinition.setId(id);
    this.scopeElementStack = new Stack<ScopeElementImpl>();
    this.scopeElementStack.push(processDefinition);
    this.unresolvedTransitions = new ArrayList<UnresolvedTransition>();
  }

  /**
   * Creates a new process definition with an unspecified id.
   */
  public static ProcessDefinitionBuilder createProcessDefinition() {
    return createProcessDefinition(null);
  }

  /**
   * Creates a new process definition with a specific id.
   */
  public static ProcessDefinitionBuilder createProcessDefinition(String id) {
    return new ProcessDefinitionBuilder(id);
  }
  
  

  public ClientProcessDefinition endProcessDefinition() {
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
    return transition(null, destinationActivityId);
  }
  
  public ProcessDefinitionBuilder transition(String transitionId, String destinationActivityId) {
    ActivityImpl activity = (ActivityImpl) scopeElementStack.peek();
    TransitionImpl transition = activity.createTransition();
    transition.setId(transitionId);
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
