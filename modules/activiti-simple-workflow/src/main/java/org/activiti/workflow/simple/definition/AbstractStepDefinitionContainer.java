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
package org.activiti.workflow.simple.definition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
@SuppressWarnings("unchecked")
public abstract class AbstractStepDefinitionContainer<T> implements StepDefinitionContainer<T> {
  
  protected String id;
  protected List<StepDefinition> steps;
  
  public AbstractStepDefinitionContainer() {
    this.steps = new ArrayList<StepDefinition>();
  }
    
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void addStep(StepDefinition stepDefinition) {
    steps.add(stepDefinition);
  }

  public List<StepDefinition> getSteps() {
    return steps;
  }
  
  // Human step

  public T addHumanStep(String id, String name, String assignee) {
    return (T) addHumanStep(id, name, assignee, false);
  }
  
  public T addHumanStep(String name, String assignee) {
    return (T) addHumanStep(null, name, assignee, false);
  }
  
  public T addHumanStepForWorkflowInitiator(String id, String name) {
    return (T) addHumanStep(id, name, null, true);
  }

  public T addHumanStepForWorkflowInitiator(String name) {
    return (T) addHumanStep(null, name, null, true);
  }
  
  public T addHumanStepForGroup(String name, List<String> groups) {
    HumanStepDefinition humanStepDefinition = createHumanStepDefinition(name);
    humanStepDefinition.setCandidateGroups(groups);
    return (T) this;
  }
  
  public T addHumanStepForGroup(String id, String name, List<String> groups) {
    HumanStepDefinition humanStepDefinition = createHumanStepDefinition(name);
    humanStepDefinition.setCandidateGroups(groups);
    return (T) this;
  }
  
  public T addHumanStepForGroup(String name, String...groups) {
    return addHumanStepForGroup(name, Arrays.asList(groups));
  }

  protected T addHumanStep(String id, String name, String assignee, boolean initiator) {
    createHumanStepDefinition(name, assignee, initiator);
    return (T) this;
  }
  
  protected HumanStepDefinition createHumanStepDefinition(String name) {
    return createHumanStepDefinition(name, null);
  }
  
  protected HumanStepDefinition createHumanStepDefinition(String name, String assignee) {
    return createHumanStepDefinition(name, assignee, false);
  }
  
  protected HumanStepDefinition createHumanStepDefinition(String name, String assignee, boolean initiator) {
    return createHumanStepDefinition(null, name, assignee, initiator);
  }
  
  protected HumanStepDefinition createHumanStepDefinition(String id, String name, String assignee, boolean initiator) {
    HumanStepDefinition humanStepDefinition = new HumanStepDefinition();
    humanStepDefinition.setId(id);
    humanStepDefinition.setName(name);
    humanStepDefinition.setAssignee(assignee);
    // TODO
    // humanStepDefinition.setAssigneeIsInitiator(initiator);

    addStep(humanStepDefinition);
    return humanStepDefinition;
  }
  
  // Feedback step
  
  public T addFeedbackStep(String name, String initiator) {
    return addFeedbackStep(name, initiator, null);
  }
  
  public T addFeedbackStep(String name, String initiator, List<String> feedbackProviders) {
    FeedbackStepDefinition feedbackStepDefinition = new FeedbackStepDefinition();
    feedbackStepDefinition.setName(name);
    feedbackStepDefinition.setFeedbackInitiator(initiator);
    
    if (feedbackProviders != null) {
      feedbackStepDefinition.setFeedbackProviders(feedbackProviders);
    }
    
    addStep(feedbackStepDefinition);
    
    return (T) this;
  }
  
  // Script step
  
  public T addScriptStep(String script) {
    return addScriptStep(null, script);
  }
  
  public T addScriptStep(String name, String script) {
    ScriptStepDefinition scriptStepDefinition = new ScriptStepDefinition();
    scriptStepDefinition.setName(name);
    scriptStepDefinition.setScript(script);
    
    addStep(scriptStepDefinition);
    
    return (T) this;
  }
}
