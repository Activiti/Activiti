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

import java.io.Serializable;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionInfoEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionInfoEntityManager;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * @author Tijs Rademakers
 */
public class SaveProcessDefinitionInfoCmd implements Command<Void>, Serializable {
  
  private static final long serialVersionUID = 1L;

  protected String processDefinitionId;
  protected ObjectNode infoNode;
  
  public SaveProcessDefinitionInfoCmd(String processDefinitionId, ObjectNode infoNode) {
    this.processDefinitionId = processDefinitionId;
    this.infoNode = infoNode;
  }
  
  public Void execute(CommandContext commandContext) {
    if (processDefinitionId == null) {
      throw new ActivitiIllegalArgumentException("process definition id is null");
    }
    
    if (infoNode == null) {
      throw new ActivitiIllegalArgumentException("process definition info node is null");
    }
    
    ProcessDefinitionInfoEntityManager definitionInfoEntityManager = commandContext.getProcessDefinitionInfoEntityManager();
    ProcessDefinitionInfoEntity definitionInfoEntity = definitionInfoEntityManager.findProcessDefinitionInfoByProcessDefinitionId(processDefinitionId);
    if (definitionInfoEntity == null) {
      definitionInfoEntity = new ProcessDefinitionInfoEntity();
      definitionInfoEntity.setProcessDefinitionId(processDefinitionId);
      commandContext.getProcessDefinitionInfoEntityManager().insertProcessDefinitionInfo(definitionInfoEntity);
    } else {
      commandContext.getProcessDefinitionInfoEntityManager().updateProcessDefinitionInfo(definitionInfoEntity);
    }
    
    if (infoNode != null) {
      try {
        ObjectWriter writer = commandContext.getProcessEngineConfiguration().getObjectMapper().writer();
        commandContext.getProcessDefinitionInfoEntityManager().updateInfoJson(definitionInfoEntity.getId(), writer.writeValueAsBytes(infoNode));
      } catch (Exception e) {
        throw new ActivitiException("Unable to serialize info node " + infoNode);
      }
    }
    
    return null;
  }

}
