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
package org.activiti.workflow.simple.alfresco.conversion;

import org.activiti.bpmn.model.EndEvent;
import org.activiti.workflow.simple.alfresco.step.AlfrescoEndProcessStepDefinition;
import org.activiti.workflow.simple.converter.ConversionConstants;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.converter.step.BaseStepDefinitionConverter;
import org.activiti.workflow.simple.definition.StepDefinition;

public class AlfrescoEndProcessStepConverter extends BaseStepDefinitionConverter<AlfrescoEndProcessStepDefinition, EndEvent> {

	private static final long serialVersionUID = 1L;

	@Override
	public Class<? extends StepDefinition> getHandledClass() {
		return AlfrescoEndProcessStepDefinition.class;
	}

	@Override
  protected EndEvent createProcessArtifact(AlfrescoEndProcessStepDefinition stepDefinition,
      WorkflowDefinitionConversion conversion) {
		EndEvent endEvent = new EndEvent();
		endEvent.setId(conversion.getUniqueNumberedId(ConversionConstants.EVENT_ID_PREFIX));
		
		addFlowElement(conversion, endEvent, true);
	  return endEvent;
  }
}
