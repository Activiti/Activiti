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

package org.activiti.engine.impl;

import org.activiti.engine.DynamicBpmnConstants;
import org.activiti.engine.DynamicBpmnService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cmd.GetProcessDefinitionInfoCmd;
import org.activiti.engine.impl.cmd.SaveProcessDefinitionInfoCmd;

import com.fasterxml.jackson.databind.node.ObjectNode;



/**
 * @author Tijs Rademakers
 */
public class DynamicBpmnServiceImpl extends ServiceImpl implements DynamicBpmnService, DynamicBpmnConstants {

  public DynamicBpmnServiceImpl(ProcessEngineConfigurationImpl processEngineConfiguration) {
    super(processEngineConfiguration);
  }
  
  public ObjectNode getProcessDefinitionInfo(String processDefinitionId) {
    return commandExecutor.execute(new GetProcessDefinitionInfoCmd(processDefinitionId));
  }
  
  public void saveProcessDefinitionInfo(String processDefinitionId, ObjectNode infoNode) {
    commandExecutor.execute(new SaveProcessDefinitionInfoCmd(processDefinitionId, infoNode));
  }
  
  public ObjectNode changeClassName(String id, String className) {
    ObjectNode infoNode = processEngineConfiguration.getObjectMapper().createObjectNode();
    changeClassName(id, className, infoNode);
    return infoNode;
  }
  
  public void changeClassName(String id, String className, ObjectNode infoNode) {
    setElementProperty(id, SERVICE_TASK_CLASS_NAME, className, infoNode);
  }
  
  public ObjectNode changeExpression(String id, String expression) {
    ObjectNode infoNode = processEngineConfiguration.getObjectMapper().createObjectNode();
    changeExpression(id, expression, infoNode);
    return infoNode;
  }
  
  public void changeExpression(String id, String expression, ObjectNode infoNode) {
    setElementProperty(id, SERVICE_TASK_EXPRESSION, expression, infoNode);
  }
  
  public ObjectNode changeDelegateExpression(String id, String expression) {
    ObjectNode infoNode = processEngineConfiguration.getObjectMapper().createObjectNode();
    changeDelegateExpression(id, expression, infoNode);
    return infoNode;
  }
  
  public void changeDelegateExpression(String id, String expression, ObjectNode infoNode) {
    setElementProperty(id, SERVICE_TASK_DELEGATE_EXPRESSION, expression, infoNode);
  }
  
  public ObjectNode changeFormKey(String id, String formKey) {
    ObjectNode infoNode = processEngineConfiguration.getObjectMapper().createObjectNode();
    changeFormKey(id, formKey, infoNode);
    return infoNode;
  }
  
  public void changeFormKey(String id, String formKey, ObjectNode infoNode) {
    setElementProperty(id, USER_TASK_FORM_KEY, formKey, infoNode);
  }
  
  public ObjectNode getElementProperties(String id, ObjectNode infoNode) {
    ObjectNode propertiesNode = null;
    ObjectNode bpmnNode = getBpmnNode(infoNode);
    if (bpmnNode != null) {
      propertiesNode = (ObjectNode) bpmnNode.get(id);
    }
    return propertiesNode;
  }
  
  protected void setElementProperty(String id, String propertyName, String propertyValue, ObjectNode infoNode) {
    ObjectNode bpmnNode = createOrGetBpmnNode(infoNode);
    if (bpmnNode.has(id) == false) {
      bpmnNode.put(id, processEngineConfiguration.getObjectMapper().createObjectNode());
    }
    
    ((ObjectNode) bpmnNode.get(id)).put(propertyName, propertyValue);
  }
  
  protected ObjectNode createOrGetBpmnNode(ObjectNode infoNode) {
    if (infoNode.has(BPMN_NODE) == false) {
      infoNode.put(BPMN_NODE, processEngineConfiguration.getObjectMapper().createObjectNode());
    }
    return (ObjectNode) infoNode.get(BPMN_NODE);
  }
  
  protected ObjectNode getBpmnNode(ObjectNode infoNode) {
    return (ObjectNode) infoNode.get(BPMN_NODE);
  }

}
