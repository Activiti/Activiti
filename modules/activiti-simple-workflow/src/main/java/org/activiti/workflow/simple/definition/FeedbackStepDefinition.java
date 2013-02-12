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

import java.util.List;


/**
 * A feedback step is a step where one person initiates the gathering of 
 * feedback from multiple other persons.
 * 
 * The feedback initiator has the possibility to stop to the collection
 * of the feedback, even if not all selected users have provided feedback.
 * 
 * @author Joram Barrez
 */
public class FeedbackStepDefinition extends AbstractNamedStepDefinition {
  
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
  
}
