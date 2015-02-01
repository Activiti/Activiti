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

import java.util.HashMap;

import org.activiti.workflow.simple.exception.SimpleWorkflowException;

import com.fasterxml.jackson.annotation.JsonTypeName;


/**
 * @author Joram Barrez
 */
@JsonTypeName("script-step")
public class ScriptStepDefinition extends AbstractNamedStepDefinition {

  private static final long serialVersionUID = 1L;
  
  protected String script;
  protected String scriptLanguage;

  public String getScript() {
    return script;
  }

  public void setScript(String script) {
    this.script = script;
  }

  public String getScriptLanguage() {
    return scriptLanguage;
  }

  public void setScriptLanguage(String scriptLanguage) {
    this.scriptLanguage = scriptLanguage;
  }
  
  @Override
  public StepDefinition clone() {
    ScriptStepDefinition clone = new ScriptStepDefinition();
    clone.setValues(this);
    return clone;
  }
  
  @Override
  public void setValues(StepDefinition otherDefinition) {
    if(!(otherDefinition instanceof ScriptStepDefinition)) {
      throw new SimpleWorkflowException("An instance of ScriptStepDefinition is required to set values");
    }
    
    ScriptStepDefinition stepDefinition = (ScriptStepDefinition) otherDefinition;
    setId(stepDefinition.getId());
    setName(stepDefinition.getName());
    setScript(stepDefinition.getScript());
    setScriptLanguage(stepDefinition.getScriptLanguage());
    setStartsWithPrevious(stepDefinition.isStartsWithPrevious());
    
    setParameters(new HashMap<String, Object>(otherDefinition.getParameters()));
  }
}
