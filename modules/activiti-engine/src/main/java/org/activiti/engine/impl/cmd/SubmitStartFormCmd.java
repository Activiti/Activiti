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

package org.activiti.engine.impl.cmd;

import java.util.Map;

import org.activiti.engine.impl.form.StartFormHandler;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.runtime.ProcessInstance;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class SubmitStartFormCmd extends NeedsActiveProcessDefinitionCmd<ProcessInstance> {

  private static final long serialVersionUID = 1L;
  
  protected final String businessKey;
  protected Map<String, String> properties;
  
  public SubmitStartFormCmd(String processDefinitionId, String businessKey, Map<String, String> properties) {
    super(processDefinitionId);
    this.businessKey = businessKey;
    this.properties = properties;
  }
  
  protected ProcessInstance execute(CommandContext commandContext, ProcessDefinitionEntity processDefinition) {
    ExecutionEntity processInstance = null;
    if (businessKey != null) {
      processInstance = processDefinition.createProcessInstance(businessKey);
    } else {
      processInstance = processDefinition.createProcessInstance();
    }

    commandContext.getHistoryManager()
      .reportFormPropertiesSubmitted(processInstance, properties, null);
    
    StartFormHandler startFormHandler = processDefinition.getStartFormHandler();
    startFormHandler.submitFormProperties(properties, processInstance);

    processInstance.start();
    
    return processInstance;
  }
}
