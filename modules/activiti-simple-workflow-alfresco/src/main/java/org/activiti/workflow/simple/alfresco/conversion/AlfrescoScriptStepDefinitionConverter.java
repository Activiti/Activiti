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
import org.activiti.workflow.simple.converter.ConversionConstants;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.converter.step.BaseStepDefinitionConverter;
import org.activiti.workflow.simple.definition.ScriptStepDefinition;
import org.activiti.workflow.simple.definition.StepDefinition;


/**
 * Script definition converter that uses A service-task evaluating Alfresco javascript instead of
 * normal BPMN Script-task.
 * 
 * @author Frederik Heremans
 */
public class AlfrescoScriptStepDefinitionConverter extends BaseStepDefinitionConverter<ScriptStepDefinition, ServiceTask> {

  private static final long serialVersionUID = 1L;

  @Override
  public Class< ? extends StepDefinition> getHandledClass() {
    return ScriptStepDefinition.class;
  }

  @Override
  protected ServiceTask createProcessArtifact(ScriptStepDefinition stepDefinition, WorkflowDefinitionConversion conversion) {
  	ScriptServiceTaskBuilder builder = new ScriptServiceTaskBuilder();
  	builder.add(stepDefinition.getScript());
  	
  	if(stepDefinition.getParameters() != null && stepDefinition.getParameters().containsKey(AlfrescoConversionConstants.PARAMETER_SCRIPT_TASK_RUNAS)) {
  		builder.setRunAs((String) stepDefinition.getParameters().get(AlfrescoConversionConstants.PARAMETER_SCRIPT_TASK_RUNAS));
  	}
  	
  	ServiceTask serviceTask = builder.build();
  	serviceTask.setName(stepDefinition.getName());
  	serviceTask.setDefaultFlow(stepDefinition.getDescription());
  	serviceTask.setId(conversion.getUniqueNumberedId(ConversionConstants.SERVICE_TASK_ID_PREFIX));
  	
    addFlowElement(conversion, serviceTask, true);
    
    return serviceTask;
  }
}
