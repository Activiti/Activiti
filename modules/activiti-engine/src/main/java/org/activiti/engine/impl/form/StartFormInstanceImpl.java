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

package org.activiti.engine.impl.form;

import org.activiti.engine.form.StartFormInstance;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.repository.ProcessDefinitionEntity;
import org.activiti.engine.repository.ProcessDefinition;


/**
 * @author Tom Baeyens
 */
public class StartFormInstanceImpl extends FormInstanceImpl implements StartFormInstance, Command<Object> {
  
  private static final long serialVersionUID = 1L;
  
  protected ProcessDefinition processDefinition;

  public StartFormInstanceImpl(ProcessDefinitionEntity processDefinition) {
    this.formKey = (String) processDefinition.getFormKey();
    this.deploymentId = processDefinition.getDeploymentId();
    this.processDefinition = processDefinition;
  }

  public Object execute(CommandContext commandContext) {
    return null;
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public ProcessDefinition getProcessDefinition() {
    return processDefinition;
  }
}
