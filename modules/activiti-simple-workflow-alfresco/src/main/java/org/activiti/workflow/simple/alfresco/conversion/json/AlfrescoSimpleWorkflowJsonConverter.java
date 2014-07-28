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
package org.activiti.workflow.simple.alfresco.conversion.json;

import java.util.ArrayList;

import org.activiti.workflow.simple.alfresco.form.AlfrescoTransitionsPropertyDefinition;
import org.activiti.workflow.simple.alfresco.step.AlfrescoEmailStepDefinition;
import org.activiti.workflow.simple.alfresco.step.AlfrescoReviewStepDefinition;
import org.activiti.workflow.simple.converter.json.SimpleWorkflowJsonConverter;
import org.activiti.workflow.simple.definition.WorkflowDefinition;

/**
 * A {@link SimpleWorkflowJsonConverter} that is capable of converting {@link WorkflowDefinition}s using
 * custom alfresco definitions.
 * 
 *  @author Frederik Heremans
 */
public class AlfrescoSimpleWorkflowJsonConverter extends SimpleWorkflowJsonConverter {

	public AlfrescoSimpleWorkflowJsonConverter() {
		additionalModelClasses = new ArrayList<Class<?>>();
		
		// Custom form properties
		additionalModelClasses.add(AlfrescoTransitionsPropertyDefinition.class);
		
		// Custom step definitions
		additionalModelClasses.add(AlfrescoEmailStepDefinition.class);
		additionalModelClasses.add(AlfrescoReviewStepDefinition.class);
  }
}
