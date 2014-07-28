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
package org.activiti.workflow.simple.converter.step;

import org.activiti.bpmn.model.ScriptTask;
import org.activiti.workflow.simple.converter.ConversionConstants;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.ScriptStepDefinition;
import org.activiti.workflow.simple.definition.StepDefinition;


/**
 * @author Joram Barrez
 */
public class ScriptStepDefinitionConverter extends BaseStepDefinitionConverter<ScriptStepDefinition, ScriptTask> {

  private static final long serialVersionUID = 1L;

  @Override
  public Class< ? extends StepDefinition> getHandledClass() {
    return ScriptStepDefinition.class;
  }

  @Override
  protected ScriptTask createProcessArtifact(ScriptStepDefinition stepDefinition, WorkflowDefinitionConversion conversion) {
    
    ScriptTask scriptTask = new ScriptTask();
    scriptTask.setId(conversion.getUniqueNumberedId(ConversionConstants.SCRIPT_TASK_ID_PREFIX));
    scriptTask.setName(stepDefinition.getName());
    scriptTask.setScript(stepDefinition.getScript());
    
    if (stepDefinition.getScriptLanguage() != null) {
      scriptTask.setScriptFormat(stepDefinition.getScriptLanguage());
    } else {
      scriptTask.setScriptFormat("JavaScript");
    }
    
    addFlowElement(conversion, scriptTask, true);
    
    return scriptTask;
  }
  
}
