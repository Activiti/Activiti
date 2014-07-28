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

import org.activiti.bpmn.model.ServiceTask;
import org.activiti.workflow.simple.alfresco.conversion.script.ScriptServiceTaskBuilder;
import org.activiti.workflow.simple.alfresco.step.AlfrescoEmailStepDefinition;
import org.activiti.workflow.simple.converter.ConversionConstants;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.converter.step.BaseStepDefinitionConverter;
import org.activiti.workflow.simple.definition.StepDefinition;

public class AlfrescoEmailStepConverter extends BaseStepDefinitionConverter<AlfrescoEmailStepDefinition, ServiceTask> {

	private static final long serialVersionUID = 1L;

	@Override
	public Class<? extends StepDefinition> getHandledClass() {
		return AlfrescoEmailStepDefinition.class;
	}

	@Override
	protected ServiceTask createProcessArtifact(AlfrescoEmailStepDefinition stepDefinition,
	  WorkflowDefinitionConversion conversion) {
		
		ScriptServiceTaskBuilder builder = new ScriptServiceTaskBuilder();
		builder.addLine("var mail = actions.create('mail');");
		addMailActionParameter("to", getSafeJsString(stepDefinition.getTo()), builder);
		addMailActionParameter("cc", getSafeJsString(stepDefinition.getCc()), builder);
		addMailActionParameter("from", getSafeJsString(stepDefinition.getFrom()), builder);
		addMailActionParameter("subject", getSafeJsString(stepDefinition.getSubject()), builder);
		addMailActionParameter("text", getSafeJsString(stepDefinition.getBody()), builder);
		builder.addLine("mail.execute(bpm_package);");
		
		// Build the actual task and add it to the process
		ServiceTask serviceTask = builder.build();
		serviceTask.setName(stepDefinition.getName());
		serviceTask.setId(conversion.getUniqueNumberedId(ConversionConstants.SERVICE_TASK_ID_PREFIX));
		addFlowElement(conversion, serviceTask, true);
		return serviceTask;
	}
	
	protected String getSafeJsString(String string) {
		if(string != null) {
			string = string.replace("'", "\\'");
		}
  	return string;
	}
	
	protected void addMailActionParameter(String name, String value, ScriptServiceTaskBuilder builder) {
		builder.addLine("mail.parameters." + name + "='" + getSafeScriptLiteral(value) + "';");
	}
	
	protected String getSafeScriptLiteral(String value) {
		if(value != null) {
			return value.replace("\n", "\\n\\\n");
		} else {
			return "";
		}
	}
}
