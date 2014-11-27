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
import java.util.HashMap;
import java.util.List;

import org.activiti.workflow.simple.converter.step.FeedbackStepDefinitionConverter;
import org.activiti.workflow.simple.definition.form.FormDefinition;
import org.activiti.workflow.simple.exception.SimpleWorkflowException;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 * A feedback step is a step where one person initiates the gathering of 
 * feedback from multiple other persons.
 * 
 * The feedback initiator has the possibility to stop to the collection
 * of the feedback, even if not all selected users have provided feedback.
 * 
 * There are a few options when it comes to selecting which people will need to provide feedback:
 * <ul>
 *   <li>A fixed list of users</li>
 *   <li>A fixed list of groups</li>
 *   <li>A mix of the two above</li>
 *   <li>
 *     The list of users/groups is provided at runtime through the form by the user.
 *     In this case, the name of the variables which are submitted through the form
 *     <b>must</b> must match the default one, see {@link FeedbackStepDefinitionConverter}.     
 *   </li>
 * </ul>
 * 
 * @author Joram Barrez
 */
@JsonTypeName("feedback-step")
public class FeedbackStepDefinition extends AbstractNamedStepDefinition {
  
  private static final long serialVersionUID = 1L;

	/**
   * The person who want to collect feedback.
   */
  protected String feedbackInitiator;
  
  /**
   * The users providing the feedback.
   */
  protected List<String> feedbackProviders;
  
  /**
   * The text the feedback providers will see when providing feedback.
   */
  protected String descriptionForFeedbackProviders;
  
  /**
   * The form that will be displayed to the feedback providers.
   */
  protected FormDefinition formDefinitionForFeedbackProviders;

  
  public String getFeedbackInitiator() {
    return feedbackInitiator;
  }
  
  public void setFeedbackInitiator(String feedbackInitiator) {
    this.feedbackInitiator = feedbackInitiator;
  }

  @JsonSerialize(contentAs=String.class)
  public List<String> getFeedbackProviders() {
    return feedbackProviders;
  }

  public void setFeedbackProviders(List<String> feedbackProviders) {
    this.feedbackProviders = feedbackProviders;
  }
  
  public String getDescriptionForFeedbackProviders() {
    return descriptionForFeedbackProviders;
  }

  public void setDescriptionForFeedbackProviders(String descriptionForFeedbackProviders) {
    this.descriptionForFeedbackProviders = descriptionForFeedbackProviders;
  }
  
  public FormDefinition getFormDefinitionForFeedbackProviders() {
    return formDefinitionForFeedbackProviders;
  }
  
  public void setFormDefinitionForFeedbackProviders(FormDefinition formDefinitionForFeedbackProviders) {
    this.formDefinitionForFeedbackProviders = formDefinitionForFeedbackProviders;
  }
  
  @Override
  public StepDefinition clone() {
    FeedbackStepDefinition clone = new FeedbackStepDefinition();
    clone.setValues(this);
    return clone;
  }
  
  @Override
  public void setValues(StepDefinition otherDefinition) {
    if(!(otherDefinition instanceof FeedbackStepDefinition)) {
      throw new SimpleWorkflowException("An instance of FeedbackStepDefinition is required to set values");
    }
    
    FeedbackStepDefinition stepDefinition = (FeedbackStepDefinition) otherDefinition;
    setDescription(stepDefinition.getDescription());
    setDescriptionForFeedbackProviders(stepDefinition.getDescriptionForFeedbackProviders());
    setFeedbackInitiator(stepDefinition.getFeedbackInitiator());
    if (stepDefinition.getFeedbackProviders() != null && !stepDefinition.getFeedbackProviders().isEmpty()) {
      setFeedbackProviders(new ArrayList<String>(stepDefinition.getFeedbackProviders()));
    }
    if (stepDefinition.getFormDefinitionForFeedbackProviders() != null) {
      setFormDefinitionForFeedbackProviders(stepDefinition.getFormDefinitionForFeedbackProviders().clone());
    } else {
      setFormDefinitionForFeedbackProviders(null);
    }
    setId(stepDefinition.getId());
    setName(stepDefinition.getName());
    setStartsWithPrevious(stepDefinition.isStartsWithPrevious());
    
    setParameters(new HashMap<String, Object>(otherDefinition.getParameters()));
  }
}
